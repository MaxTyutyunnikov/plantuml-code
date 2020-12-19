/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2020, Arnaud Roques
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
package net.sourceforge.plantuml.wire;

import java.awt.geom.Dimension2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.sourceforge.plantuml.Dimension2DDouble;
import net.sourceforge.plantuml.SpriteContainerEmpty;
import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.ugraphic.UFont;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.URectangle;
import net.sourceforge.plantuml.ugraphic.UTranslate;
import net.sourceforge.plantuml.ugraphic.color.HColorUtils;

public class WBlock {

	private final String name;
	private final double forcedWidth;
	private final double forcedHeight;

	private final List<WBlock> children = new ArrayList<WBlock>();
	private UTranslate position = new UTranslate();
	private WBlock parent;

	private UTranslate futureGoto;
	private UTranslate futureMove = new UTranslate(0, 20);

	public UTranslate getAbsolutePosition(String supx, String supy) {
		if (parent == null) {
			return position;
		}
		final UTranslate p = parent.getAbsolutePosition("0", "0");

		final double x = position.getDx() + p.getDx() + parseWidth(supx);
		final double y = position.getDy() + p.getDy() + parseHeight(supy);
		return new UTranslate(x, y);

	}

	private double parseWidth(String value) {
		if (value.endsWith("%")) {
			final double p = Double.parseDouble(value.substring(0, value.length() - 1)) / 100.0;
			return getMaxDimension().getWidth() * p;
		}
		if (value.contains("%")) {
			final StringTokenizer st = new StringTokenizer(value, "%");
			final String v1 = st.nextToken();
			final String v2 = st.nextToken();
			final double p = Double.parseDouble(v1) / 100.0;
			return getMaxDimension().getWidth() * p + Double.parseDouble(v2);
		}
		return Double.parseDouble(value);
	}

	private double parseHeight(String value) {
		if (value.endsWith("%")) {
			final double p = Double.parseDouble(value.substring(0, value.length() - 1)) / 100.0;
			return getMaxDimension().getHeight() * p;
		}
		if (value.contains("%")) {
			final StringTokenizer st = new StringTokenizer(value, "%");
			final String v1 = st.nextToken();
			final String v2 = st.nextToken();
			final double p = Double.parseDouble(v1) / 100.0;
			return getMaxDimension().getHeight() * p + Double.parseDouble(v2);
		}
		return Double.parseDouble(value);
	}

	@Override
	public String toString() {
		return name + " " + position;
	}

	public WBlock(String name, double width, double height) {
		this.name = name;
		this.forcedWidth = width;
		this.forcedHeight = height;
	}

	private WBlock getChildByName(String name) {
		for (WBlock child : children) {
			if (name.equals(child.getName())) {
				return child;
			}
		}
		return null;
	}

	public WBlock getBlock(String name) {
		final int x = name.indexOf('.');
		if (x == -1) {
			return getChildByName(name);
		}
		final WBlock first = getChildByName(name.substring(0, x));
		if (first == null) {
			return null;
		}
		return first.getBlock(name.substring(x + 1));
	}

	private String getName() {
		return name;
	}

	public CommandExecutionResult newColumn(int level) {
		if (level == 0) {
			final Dimension2D max = getNaturalDimension();
			futureGoto = new UTranslate(max.getWidth() + 10, 20);
			futureMove = new UTranslate();
			return CommandExecutionResult.ok();
		}
		return getLastChild().newColumn(level - 1);
	}

	public CommandExecutionResult wgoto(int level, double x, double y) {
		if (level == 0) {
			futureGoto = new UTranslate(x, y);
			futureMove = new UTranslate();
			return CommandExecutionResult.ok();
		}
		return getLastChild().wgoto(level - 1, x, y);
	}

	public CommandExecutionResult wmove(int level, double x, double y) {
		if (level == 0) {
			futureMove = futureMove.compose(new UTranslate(x, y));
			return CommandExecutionResult.ok();
		}
		return getLastChild().wmove(level - 1, x, y);
	}

	public CommandExecutionResult addComponent(int level, String name, double width, double height) {
		if (name.contains(".")) {
			throw new IllegalArgumentException();
		}
		if (getChildByName(name) != null) {
			return CommandExecutionResult.error("Component exists already");
		}
		if (level == 0) {
			final WBlock newBlock = new WBlock(name, width, height);
			newBlock.position = getNextPosition();

			children.add(newBlock);
			newBlock.parent = this;
			return CommandExecutionResult.ok();
		}

		final WBlock last = getLastChild();
		return last.addComponent(level - 1, name, width, height);
	}

	private UTranslate getNextPosition() {
		final UTranslate result;
		if (futureGoto != null) {
			result = futureGoto.compose(futureMove);
		} else {
			final WBlock last = getLastChild();
			if (last == null) {
				result = futureMove.compose(UTranslate.dx(10));
			} else {
				final Dimension2D dim = last.getMaxDimension();
				result = last.position.compose(UTranslate.dy(dim.getHeight())).compose(futureMove);
			}
		}
		futureGoto = null;
		futureMove = new UTranslate(0, 20);
		return result;
	}

	private WBlock getLastChild() {
		if (children.size() == 0) {
			return null;
		}
		return children.get(children.size() - 1);
	}

	public void drawMe(UGraphic ug) {
		drawBox(ug);
		final UFont font = UFont.sansSerif(12);
		final FontConfiguration fc = new FontConfiguration(font, HColorUtils.BLACK, HColorUtils.BLACK, false);
		final Display display = Display.create(name.replace('_', ' '));
		final TextBlock text = display.create(fc, HorizontalAlignment.LEFT, new SpriteContainerEmpty());
		text.drawU(ug.apply(UTranslate.dx(5)));

	}

	private void drawBox(UGraphic ug) {
		ug = ug.apply(HColorUtils.BLACK);
		if (name.length() > 0) {
			final URectangle rect = new URectangle(getMaxDimension());
			ug.draw(rect);
		}
		for (WBlock child : children) {
			child.drawMe(ug.apply(child.position));
		}
	}

	private Dimension2D getMaxDimension() {
		if (children.size() > 0) {
			if (forcedWidth != 0) {
				return new Dimension2DDouble(forcedWidth, forcedHeight);
			}
			return getNaturalDimension();
		}
		final double x = forcedWidth == 0 ? 100 : forcedWidth;
		final double y = forcedHeight == 0 ? 100 : forcedHeight;
		return new Dimension2DDouble(x, y);
	}

	private Dimension2D getNaturalDimension() {
		double x = 0;
		double y = 0;
		for (WBlock child : children) {
			final Dimension2D dim = child.getMaxDimension();
			x = Math.max(x, child.position.getDx() + dim.getWidth() + 10);
			y = Math.max(y, child.position.getDy() + dim.getHeight() + 10);
		}
		return new Dimension2DDouble(x, y);
	}

	private UTranslate futureOut;

	public UTranslate getNextOut(String x1, String y1, WLinkType type) {
		final UTranslate result;
		if (x1 != null && y1 != null) {
			result = getAbsolutePosition(x1, y1);
		} else if (futureOut == null) {
			result = getAbsolutePosition("100%", "5");
		} else {
			result = futureOut;
		}

		futureOut = result.compose(UTranslate.dy(type.ySpaceForNext()));

		return result;
	}

}
