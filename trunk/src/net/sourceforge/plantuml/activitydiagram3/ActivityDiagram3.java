/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2013, Arnaud Roques
 *
 * Project Info:  http://plantuml.sourceforge.net
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
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * Original Author:  Arnaud Roques
 *
 * Revision $Revision: 9786 $
 *
 */
package net.sourceforge.plantuml.activitydiagram3;

import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.FontParam;
import net.sourceforge.plantuml.ISkinParam;
import net.sourceforge.plantuml.UmlDiagram;
import net.sourceforge.plantuml.UmlDiagramType;
import net.sourceforge.plantuml.activitydiagram3.ftile.Swimlanes;
import net.sourceforge.plantuml.api.ImageDataSimple;
import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.core.ImageData;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.HorizontalAlignement;
import net.sourceforge.plantuml.graphic.HtmlColor;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.graphic.StringBounderUtils;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.graphic.TextBlockCompressed;
import net.sourceforge.plantuml.graphic.TextBlockInterceptorTextBlockable;
import net.sourceforge.plantuml.graphic.TextBlockUtils;
import net.sourceforge.plantuml.sequencediagram.NotePosition;
import net.sourceforge.plantuml.svek.DecorateEntityImage2;
import net.sourceforge.plantuml.ugraphic.UFont;
import net.sourceforge.plantuml.ugraphic.UGraphic;

public class ActivityDiagram3 extends UmlDiagram {

	static {
		final BufferedImage imDummy = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		dummyStringBounder = StringBounderUtils.asStringBounder(imDummy.createGraphics());
	}

	private static final StringBounder dummyStringBounder;

	private final Swimlanes swinlanes = new Swimlanes(getSkinParam());

	public CommandExecutionResult swimlane(String name) {
		swinlanes.swimlane(name);
		return CommandExecutionResult.ok();
	}

	private void setCurrent(Instruction ins) {
		swinlanes.setCurrent(ins);
	}

	private Instruction current() {
		return swinlanes.getCurrent();
	}

	private LinkRendering nextLinkRenderer() {
		return swinlanes.nextLinkRenderer();
	}

	private void setNextLinkRenderer(LinkRendering link) {
		swinlanes.setNextLinkRenderer(link);
	}
	
	public void addActivity(Display activity, HtmlColor color) {
		current().add(new InstructionSimple(activity, color, nextLinkRenderer(), swinlanes.getCurrentSwimlane()));
		setNextLinkRenderer(null);
	}
	
	public void start() {
		current().add(new InstructionStart(swinlanes.getCurrentSwimlane()));
	}

	public void stop() {
		current().add(new InstructionStop(swinlanes.getCurrentSwimlane()));
	}


	public String getDescription() {
		return "activity3";
	}

	@Override
	public UmlDiagramType getUmlDiagramType() {
		return UmlDiagramType.ACTIVITY;
	}

	protected ImageData exportDiagramInternal(OutputStream os, int index, FileFormatOption fileFormatOption,
			List<BufferedImage> flashcodes) throws IOException {
		TextBlock result = swinlanes;
		result = addTitle(result);
		result = addHeaderAndFooter(result);
		final ISkinParam skinParam = getSkinParam();
		final double dpiFactor = getDpiFactor(fileFormatOption);

		// final TextBlock tb = new TextBlockCompressed(new TextBlockInterceptorTextBlockable(result));
		// final TextBlock tb = new TextBlockInterceptorTextBlockable(result);
		final UGraphic ug = TextBlockUtils.getPrinted(result, fileFormatOption, skinParam.getColorMapper(), dpiFactor,
				getSkinParam().getBackgroundColor());
		// // ug = new UGraphicOnlySimpleActivity(ug);
		// // ug = new UGraphicInterceptorTextBlockableOnlySimpleActivity(ug);
		ug.writeImage(os, getMetadata(), getDpi(fileFormatOption));
		final Dimension2D dim = TextBlockUtils.getMinMax(result, dummyStringBounder).getDimension();
		return new ImageDataSimple((int) dim.getWidth(), (int) dim.getHeight());
	}

	private TextBlock addTitle(TextBlock original) {
		final Display title = getTitle();
		if (title == null) {
			return original;
		}
		final TextBlock text = TextBlockUtils.create(title, new FontConfiguration(getFont(FontParam.TITLE),
				getFontColor(FontParam.TITLE, null)), HorizontalAlignement.CENTER, getSkinParam());

		return new DecorateEntityImage2(original, text, HorizontalAlignement.CENTER);
	}

	private TextBlock addHeaderAndFooter(TextBlock original) {
		final Display footer = getFooter();
		final Display header = getHeader();
		if (footer == null && header == null) {
			return original;
		}
		final TextBlock textFooter = footer == null ? null : TextBlockUtils
				.create(footer, new FontConfiguration(getFont(FontParam.FOOTER), getFontColor(FontParam.FOOTER, null)),
						getFooterAlignement(), getSkinParam());
		final TextBlock textHeader = header == null ? null : TextBlockUtils
				.create(header, new FontConfiguration(getFont(FontParam.HEADER), getFontColor(FontParam.HEADER, null)),
						getHeaderAlignement(), getSkinParam());

		return new DecorateEntityImage2(original, textHeader, getHeaderAlignement(), textFooter, getFooterAlignement());
	}

	private final UFont getFont(FontParam fontParam) {
		final ISkinParam skinParam = getSkinParam();
		return skinParam.getFont(fontParam, null);
	}

	private final HtmlColor getFontColor(FontParam fontParam, String stereo) {
		final ISkinParam skinParam = getSkinParam();
		return skinParam.getFontHtmlColor(fontParam, stereo);
	}

	public void fork() {
		final InstructionFork instructionFork = new InstructionFork(current());
		current().add(instructionFork);
		setCurrent(instructionFork);
	}

	public CommandExecutionResult forkAgain() {
		if (current() instanceof InstructionFork) {
			((InstructionFork) current()).forkAgain();
			return CommandExecutionResult.ok();
		}
		return CommandExecutionResult.error("Cannot find fork");
	}

	public CommandExecutionResult endFork() {
		if (current() instanceof InstructionFork) {
			setCurrent(((InstructionFork) current()).getParent());
			return CommandExecutionResult.ok();
		}
		return CommandExecutionResult.error("Cannot find fork");
	}

	public void split() {
		final InstructionSplit instructionSplit = new InstructionSplit(current());
		current().add(instructionSplit);
		setCurrent(instructionSplit);
	}

	public CommandExecutionResult splitAgain() {
		if (current() instanceof InstructionSplit) {
			((InstructionSplit) current()).splitAgain();
			return CommandExecutionResult.ok();
		}
		return CommandExecutionResult.error("Cannot find split");
	}

	public CommandExecutionResult endSplit() {
		if (current() instanceof InstructionSplit) {
			setCurrent(((InstructionSplit) current()).getParent());
			return CommandExecutionResult.ok();
		}
		return CommandExecutionResult.error("Cannot find split");
	}

	public void startIf(Display test, Display whenThen) {
		final InstructionIf instructionIf = new InstructionIf(current(), test, whenThen, nextLinkRenderer());
		current().add(instructionIf);
		setCurrent(instructionIf);
	}

	public CommandExecutionResult endif() {
		if (current() instanceof InstructionIf) {
			((InstructionIf) current()).endif(nextLinkRenderer());
			setNextLinkRenderer(null);
			setCurrent(((InstructionIf) current()).getParent());
			return CommandExecutionResult.ok();
		}
		return CommandExecutionResult.error("Cannot find if");
	}

	public CommandExecutionResult else2(Display whenElse) {
		if (current() instanceof InstructionIf) {
			((InstructionIf) current()).swithToElse(whenElse, nextLinkRenderer());
			setNextLinkRenderer(null);
			return CommandExecutionResult.ok();
		}
		return CommandExecutionResult.error("Cannot find if");
	}

	public void startRepeat() {
		final InstructionRepeat instructionRepeat = new InstructionRepeat(current(), nextLinkRenderer());
		current().add(instructionRepeat);
		setCurrent(instructionRepeat);

	}

	public CommandExecutionResult repeatWhile(Display label) {
		if (current() instanceof InstructionRepeat) {
			final InstructionRepeat instructionRepeat = (InstructionRepeat) current();
			instructionRepeat.setTest(label, nextLinkRenderer());
			setCurrent(instructionRepeat.getParent());
			this.setNextLinkRenderer(null);
			return CommandExecutionResult.ok();
		}
		return CommandExecutionResult.error("Cannot find repeat");

	}

	public void doWhile(Display test, Display yes) {
		final InstructionWhile instructionWhile = new InstructionWhile(current(), test, nextLinkRenderer(), yes);
		current().add(instructionWhile);
		setCurrent(instructionWhile);
	}

	public CommandExecutionResult endwhile(Display out) {
		if (current() instanceof InstructionWhile) {
			((InstructionWhile) current()).endwhile(nextLinkRenderer(), out);
			setNextLinkRenderer(null);
			setCurrent(((InstructionWhile) current()).getParent());
			return CommandExecutionResult.ok();
		}
		return CommandExecutionResult.error("Cannot find while");
	}

	final public CommandExecutionResult kill() {
		if (current().kill() == false) {
			return CommandExecutionResult.error("kill cannot be used here");
		}
		return CommandExecutionResult.ok();
	}

	public void startGroup(Display name) {
		final InstructionGroup instructionGroup = new InstructionGroup(current(), name);
		current().add(instructionGroup);
		setCurrent(instructionGroup);
	}

	public CommandExecutionResult endGroup() {
		if (current() instanceof InstructionGroup) {
			setCurrent(((InstructionGroup) current()).getParent());
			return CommandExecutionResult.ok();
		}
		return CommandExecutionResult.error("Cannot find group");
	}

	public void setNextLink(LinkRendering linkRenderer) {
		if (current() instanceof InstructionList) {
			final Instruction last = ((InstructionList) current()).getLast();
			if (last instanceof InstructionWhile) {
				((InstructionWhile) last).afterEndwhile(linkRenderer);
			}
		}
		this.setNextLinkRenderer(linkRenderer);
	}

	public CommandExecutionResult addNote(Display note, NotePosition position) {
		current().addNote(note, position);
		return CommandExecutionResult.ok();
	}

}