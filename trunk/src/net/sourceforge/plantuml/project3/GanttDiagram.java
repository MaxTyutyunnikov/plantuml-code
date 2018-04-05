/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2017, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 * 
 *
 */
package net.sourceforge.plantuml.project3;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.plantuml.AbstractPSystem;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.Scale;
import net.sourceforge.plantuml.SpriteContainerEmpty;
import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.core.ImageData;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.HtmlColor;
import net.sourceforge.plantuml.graphic.HtmlColorSetSimple;
import net.sourceforge.plantuml.graphic.HtmlColorUtils;
import net.sourceforge.plantuml.graphic.IHtmlColorSet;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.graphic.UDrawable;
import net.sourceforge.plantuml.ugraphic.ColorMapperIdentity;
import net.sourceforge.plantuml.ugraphic.ImageBuilder;
import net.sourceforge.plantuml.ugraphic.UChangeBackColor;
import net.sourceforge.plantuml.ugraphic.UChangeColor;
import net.sourceforge.plantuml.ugraphic.UFont;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.ULine;
import net.sourceforge.plantuml.ugraphic.URectangle;
import net.sourceforge.plantuml.ugraphic.UTranslate;

public class GanttDiagram extends AbstractPSystem implements Subject {

	private final Map<TaskCode, Task> tasks = new LinkedHashMap<TaskCode, Task>();
	private final Map<String, Task> byShortName = new HashMap<String, Task>();
	private final List<GanttConstraint> constraints = new ArrayList<GanttConstraint>();
	private final IHtmlColorSet colorSet = new HtmlColorSetSimple();
	private final Collection<DayOfWeek> closedDayOfWeek = EnumSet.noneOf(DayOfWeek.class);
	private final Collection<DayAsDate> closedDayAsDate = new HashSet<DayAsDate>();
	private GCalendar calendar;

	private final Instant min = new InstantDay(0);
	private Instant max;

	public DiagramDescription getDescription() {
		return new DiagramDescription("(Project)");
	}

	private int horizontalPages = 1;
	private int verticalPages = 1;

	final public int getHorizontalPages() {
		return horizontalPages;
	}

	final public void setHorizontalPages(int horizontalPages) {
		this.horizontalPages = horizontalPages;
	}

	final public int getVerticalPages() {
		return verticalPages;
	}

	final public void setVerticalPages(int verticalPages) {
		this.verticalPages = verticalPages;
	}

	@Override
	public int getNbImages() {
		return this.horizontalPages * this.verticalPages;
	}

	public final int getDpi(FileFormatOption fileFormatOption) {
		return 96;
	}

	@Override
	protected ImageData exportDiagramNow(OutputStream os, int index, FileFormatOption fileFormatOption, long seed)
			throws IOException {
		final double margin = 10;

		// public ImageBuilder(ColorMapper colorMapper, double dpiFactor, HtmlColor mybackcolor, String metadata,
		// String warningOrError, double margin1, double margin2, Animation animation, boolean useHandwritten) {

		sortTasks();
		final Scale scale = getScale();

		final double dpiFactor = scale == null ? 1 : scale.getScale(100, 100);
		final ImageBuilder imageBuilder = new ImageBuilder(new ColorMapperIdentity(), dpiFactor, null, "", "", 0, 0,
				null, false);
		final UDrawable result = getUDrawable();
		imageBuilder.setUDrawable(result);

		return imageBuilder.writeImageTOBEMOVED(fileFormatOption, seed, os);
	}

	private void sortTasks() {
		final TaskCodeSimpleOrder order = getCanonicalOrder(1);
		final List<Task> list = new ArrayList<Task>(tasks.values());
		Collections.sort(list, new Comparator<Task>() {
			public int compare(Task task1, Task task2) {
				return order.compare(task1.getCode(), task2.getCode());
			}
		});
		tasks.clear();
		for (Task task : list) {
			tasks.put(task.getCode(), task);
		}
	}

	private UDrawable getUDrawable() {
		return new UDrawable() {
			public void drawU(UGraphic ug) {
				initMinMax();
				final TimeScale timeScale = getTimeScale();
				drawTimeHeader(ug, timeScale);
				drawTasks(ug, timeScale);
				drawConstraints(ug, timeScale);
			}
		};
	}

	private TimeScale getTimeScale() {
		if (calendar == null) {
			return new TimeScaleBasic();
		}
		return new TimeScaleBasic2(getCalendarSimple());
		// return new TimeScaleWithoutWeekEnd(calendar);
	}

	private GCalendarSimple getCalendarSimple() {
		return (GCalendarSimple) calendar;
	}

	public LoadPlanable getDefaultPlan() {
		return new LoadPlanable() {
			public int getLoadAt(Instant instant) {
				if (calendar == null) {
					return 100;
				}
				final DayAsDate day = getCalendarSimple().toDayAsDate((InstantDay) instant);
				final DayOfWeek dayOfWeek = day.getDayOfWeek();
				if (closedDayOfWeek.contains(dayOfWeek) || closedDayAsDate.contains(day)) {
					return 0;
				}
				return 100;
			}
		};
	}

	private void drawConstraints(final UGraphic ug, TimeScale timeScale) {
		for (GanttConstraint constraint : constraints) {
			constraint.getUDrawable(timeScale).drawU(ug);
		}

	}

	private void drawTimeHeader(final UGraphic ug, TimeScale timeScale) {

		final double yTotal = initTaskAndResourceDraws(timeScale);

		final double xmin = timeScale.getStartingPosition(min);
		final double xmax = timeScale.getEndingPosition(max);
		ug.apply(new UChangeColor(HtmlColorUtils.LIGHT_GRAY)).draw(new ULine(xmax - xmin, 0));
		ug.apply(new UChangeColor(HtmlColorUtils.LIGHT_GRAY)).apply(new UTranslate(0, getHeaderHeight() - 3))
				.draw(new ULine(xmax - xmin, 0));
		if (calendar == null) {
			drawSimpleDayCounter(ug, timeScale, yTotal);
		} else {
			drawCalendar(ug, timeScale, yTotal);
		}

	}

	private final HtmlColor veryLightGray = new HtmlColorSetSimple().getColorIfValid("#E0E8E8");

	private double getHeaderHeight() {
		if (calendar != null) {
			return Y_WEEKDAY + Y_NUMDAY;
		}
		return 16;
	}

	private static final int Y_WEEKDAY = 16;
	private static final int Y_NUMDAY = 28;

	private void drawCalendar(final UGraphic ug, TimeScale timeScale, final double yTotal) {
		timeScale = new TimeScaleBasic();
		final ULine vbar = new ULine(0, yTotal - Y_WEEKDAY);
		Month lastMonth = null;
		final GCalendarSimple calendarAll = getCalendarSimple();
		final Instant max2 = calendarAll.fromDayAsDate(calendar.toDayAsDate((InstantDay) max));
		for (Instant i = min; i.compareTo(max2.increment()) <= 0; i = i.increment()) {
			final DayAsDate day = calendarAll.toDayAsDate((InstantDay) i);
			final DayOfWeek dayOfWeek = day.getDayOfWeek();
			final boolean isWorkingDay = getDefaultPlan().getLoadAt(i) > 0;
			final String d1 = "" + day.getDayOfMonth();
			final TextBlock num = getTextBlock(d1, 10);
			final double x1 = timeScale.getStartingPosition(i);
			final double x2 = timeScale.getEndingPosition(i);
			if (i.compareTo(max2.increment()) < 0) {
				final TextBlock weekDay = getTextBlock(dayOfWeek.shortName(), 10);

				if (isWorkingDay) {
					drawCenter(ug.apply(new UTranslate(0, Y_NUMDAY)), num, x1, x2);
					drawCenter(ug.apply(new UTranslate(0, Y_WEEKDAY)), weekDay, x1, x2);
				} else {
					final URectangle rect = new URectangle(x2 - x1 - 1, yTotal - Y_WEEKDAY);
					ug.apply(new UChangeColor(null)).apply(new UChangeBackColor(veryLightGray))
							.apply(new UTranslate(x1 + 1, Y_WEEKDAY)).draw(rect);
				}
				if (lastMonth != day.getMonth()) {
					final int delta = 5;
					if (lastMonth != null) {
						final TextBlock lastMonthBlock = getTextBlock(lastMonth.name(), 12);
						lastMonthBlock.drawU(ug.apply(new UTranslate(x1
								- lastMonthBlock.calculateDimension(ug.getStringBounder()).getWidth() - delta, 0)));
					}
					final TextBlock month = getTextBlock(day.getMonth().name(), 12);
					month.drawU(ug.apply(new UTranslate(x1 + delta, 0)));
					ug.apply(new UChangeColor(HtmlColorUtils.LIGHT_GRAY)).apply(new UTranslate(x1, 0))
							.draw(new ULine(0, Y_WEEKDAY));
				}
				lastMonth = day.getMonth();
			}
			ug.apply(new UChangeColor(HtmlColorUtils.LIGHT_GRAY)).apply(new UTranslate(x1, Y_WEEKDAY)).draw(vbar);
		}
	}

	private TextBlock getTextBlock(final String text, int size) {
		return Display.getWithNewlines(text).create(getFontConfiguration(size), HorizontalAlignment.LEFT,
				new SpriteContainerEmpty());
	}

	private void drawCenter(final UGraphic ug, final TextBlock text, final double x1, final double x2) {
		final double width = text.calculateDimension(ug.getStringBounder()).getWidth();
		final double delta = (x2 - x1) - width;
		if (delta < 0) {
			return;
		}
		text.drawU(ug.apply(new UTranslate(x1 + delta / 2, 0)));
	}

	private void drawSimpleDayCounter(final UGraphic ug, TimeScale timeScale, final double yTotal) {
		final ULine vbar = new ULine(0, yTotal);
		for (Instant i = min; i.compareTo(max.increment()) <= 0; i = i.increment()) {
			final TextBlock num = Display.getWithNewlines(i.toShortString()).create(getFontConfiguration(10),
					HorizontalAlignment.LEFT, new SpriteContainerEmpty());
			final double x1 = timeScale.getStartingPosition(i);
			final double x2 = timeScale.getEndingPosition(i);
			final double width = num.calculateDimension(ug.getStringBounder()).getWidth();
			final double delta = (x2 - x1) - width;
			if (i.compareTo(max.increment()) < 0) {
				num.drawU(ug.apply(new UTranslate(x1 + delta / 2, 0)));
			}
			ug.apply(new UChangeColor(HtmlColorUtils.LIGHT_GRAY)).apply(new UTranslate(x1, 0)).draw(vbar);
		}
	}

	private double initTaskAndResourceDraws(TimeScale timeScale) {
		double y = getHeaderHeight();
		for (Task task : tasks.values()) {
			final TaskDraw draw;
			if (task instanceof TaskSeparator) {
				draw = new TaskDrawSeparator((TaskSeparator) task, timeScale, y, min, max);
			} else {
				draw = new TaskDrawRegular((TaskImpl) task, timeScale, y);
			}
			task.setTaskDraw(draw);
			y += draw.getHeight();

		}
		for (Resource res : resources.values()) {
			final ResourceDraw draw = new ResourceDraw(this, res, timeScale, y, min, max);
			res.setTaskDraw(draw);
			y += draw.getHeight();

		}
		return y;
	}

	private void initMinMax() {
		// min = tasks.values().iterator().next().getStart();
		max = tasks.values().iterator().next().getEnd();
		for (Task task : tasks.values()) {
			if (task instanceof TaskSeparator) {
				continue;
			}
			final Instant start = task.getStart();
			final Instant end = task.getEnd();
			// if (min.compareTo(start) > 0) {
			// min = start;
			// }
			if (max.compareTo(end) < 0) {
				max = end;
			}
		}
	}

	private void drawTasks(final UGraphic ug, TimeScale timeScale) {
		for (Task task : tasks.values()) {
			final TaskDraw draw = task.getTaskDraw();
			draw.drawU(ug.apply(new UTranslate(0, draw.getY())));
			draw.drawTitle(ug.apply(new UTranslate(0, draw.getY())));
		}
		for (Resource res : resources.values()) {
			final ResourceDraw draw = res.getResourceDraw();
			draw.drawU(ug.apply(new UTranslate(0, draw.getY())));
		}
	}

	private FontConfiguration getFontConfiguration(int size) {
		UFont font = UFont.serif(size);
		if (size > 10) {
			font = font.bold();
		}
		return new FontConfiguration(font, HtmlColorUtils.BLACK, HtmlColorUtils.BLACK, false);
	}

	public Task getExistingTask(String id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		Task result = byShortName.get(id);
		if (result != null) {
			return result;
		}
		final TaskCode code = new TaskCode(id);
		return tasks.get(code);
	}

	public void setTaskOrder(final Task task1, final Task task2) {
		final TaskInstant end1 = new TaskInstant(task1, TaskAttribute.END);
		task2.setStart(end1.getInstantPrecise());
		addContraint(new GanttConstraint(end1, new TaskInstant(task2, TaskAttribute.START)));
	}

	public Task getOrCreateTask(String codeOrShortName, String shortName, boolean linkedToPrevious) {
		if (codeOrShortName == null) {
			throw new IllegalArgumentException();
		}
		Task result = shortName == null ? null : byShortName.get(shortName);
		if (result != null) {
			return result;
		}
		result = byShortName.get(codeOrShortName);
		if (result != null) {
			return result;
		}
		final TaskCode code = new TaskCode(codeOrShortName);
		result = tasks.get(code);
		if (result == null) {
			Task previous = null;
			if (linkedToPrevious) {
				previous = getLastCreatedTask();
			}
			result = new TaskImpl(code, getDefaultPlan());
			tasks.put(code, result);
			if (byShortName != null) {
				byShortName.put(shortName, result);
			}
			if (previous != null) {
				setTaskOrder(previous, result);
			}
		}
		return result;
	}

	private Task getLastCreatedTask() {
		final List<Task> all = new ArrayList<Task>(tasks.values());
		for (int i = all.size() - 1; i >= 0; i--) {
			if (all.get(i) instanceof TaskImpl) {
				return all.get(i);
			}
		}
		return null;
	}

	public void addSeparator(String comment) {
		TaskSeparator separator = new TaskSeparator(comment, tasks.size());
		tasks.put(separator.getCode(), separator);
	}

	private TaskCodeSimpleOrder getCanonicalOrder(int hierarchyHeader) {
		final List<TaskCode> codes = new ArrayList<TaskCode>();
		for (TaskCode code : tasks.keySet()) {
			if (code.getHierarchySize() >= hierarchyHeader) {
				codes.add(code.truncateHierarchy(hierarchyHeader));
			}
		}
		return new TaskCodeSimpleOrder(codes, hierarchyHeader);
	}

	private int getMaxHierarchySize() {
		int max = Integer.MIN_VALUE;
		for (TaskCode code : tasks.keySet()) {
			max = Math.max(max, code.getHierarchySize());
		}
		return max;
	}

	public void addContraint(GanttConstraint constraint) {
		constraints.add(constraint);
	}

	public IHtmlColorSet getIHtmlColorSet() {
		return colorSet;
	}

	public void setStartingDate(DayAsDate start) {
		this.calendar = new GCalendarSimple(start);
	}

	public DayAsDate getStartingDate() {
		return this.calendar.getStartingDate();
	}

	public void closeDayOfWeek(DayOfWeek day) {
		closedDayOfWeek.add(day);
	}

	public void closeDayAsDate(DayAsDate day) {
		closedDayAsDate.add(day);
	}

	public Instant convert(DayAsDate day) {
		return calendar.fromDayAsDate(day);
	}

	private final Map<String, Resource> resources = new LinkedHashMap<String, Resource>();

	public void affectResource(Task result, String resourceName) {
		Resource resource = getResource(resourceName);
		result.addResource(resource);
	}

	public Resource getResource(String resourceName) {
		Resource resource = resources.get(resourceName);
		if (resource == null) {
			resource = new Resource(resourceName, getDefaultPlan());
		}
		resources.put(resourceName, resource);
		return resource;
	}

	public int getLoadForResource(Resource res, Instant i) {
		int result = 0;
		for (Task task : tasks.values()) {
			if (task instanceof TaskSeparator) {
				continue;
			}
			final TaskImpl task2 = (TaskImpl) task;
			result += task2.loadForResource(res, i);
		}
		return result;
	}

}
