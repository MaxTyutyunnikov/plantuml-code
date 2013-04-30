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
 * Revision $Revision: 5441 $
 *
 */
package net.sourceforge.plantuml.statediagram.command;

import net.sourceforge.plantuml.Direction;
import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.classdiagram.command.CommandLinkClass;
import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.command.SingleLineCommand2;
import net.sourceforge.plantuml.command.regex.RegexConcat;
import net.sourceforge.plantuml.command.regex.RegexLeaf;
import net.sourceforge.plantuml.command.regex.RegexResult;
import net.sourceforge.plantuml.cucadiagram.Code;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.IEntity;
import net.sourceforge.plantuml.cucadiagram.LeafType;
import net.sourceforge.plantuml.cucadiagram.Link;
import net.sourceforge.plantuml.cucadiagram.LinkDecor;
import net.sourceforge.plantuml.cucadiagram.LinkType;
import net.sourceforge.plantuml.cucadiagram.Stereotype;
import net.sourceforge.plantuml.graphic.HtmlColorUtils;
import net.sourceforge.plantuml.statediagram.StateDiagram;

public class CommandLinkState extends SingleLineCommand2<StateDiagram> {

	public CommandLinkState() {
		super(getRegex());
	}

	static RegexConcat getRegex() {
		return new RegexConcat(new RegexLeaf("^"), //
				getStatePattern("ENT1"), //
				new RegexLeaf("\\s*"), //
				new RegexConcat(//
						new RegexLeaf("ARROW_CROSS_START", "(x)?"), //
						new RegexLeaf("ARROW_BODY1", "(-+)"), //
						new RegexLeaf("ARROW_STYLE1",
						"(?:\\[((?:#\\w+|dotted|dashed|bold|hidden)(?:,#\\w+|,dotted|,dashed|,bold|,hidden)*)\\])?"), //
						new RegexLeaf("ARROW_DIRECTION", "(left|right|up|down|le?|ri?|up?|do?)?"), //
						new RegexLeaf("ARROW_STYLE2",
						"(?:\\[((?:#\\w+|dotted|dashed|bold|hidden)(?:,#\\w+|,dotted|,dashed|,bold|,hidden)*)\\])?"), //
						new RegexLeaf("ARROW_BODY2", "(-*)"), //
						new RegexLeaf("\\>"), //
						new RegexLeaf("ARROW_CIRCLE_END", "(o\\s+)?")), //
				new RegexLeaf("\\s*"), //
				getStatePattern("ENT2"), //
				new RegexLeaf("\\s*"), //
				new RegexLeaf("LABEL", "(?::\\s*([^\"]+))?"), //
				new RegexLeaf("$"));
	}

	private static RegexLeaf getStatePattern(String name) {
		return new RegexLeaf(
				name,
				"([\\p{L}0-9_.]+|[\\p{L}0-9_.]+\\[H\\]|\\[\\*\\]|\\[H\\]|(?:==+)(?:[\\p{L}0-9_.]+)(?:==+))\\s*(\\<\\<.*\\>\\>)?\\s*(#\\w+)?");
	}

	@Override
	protected CommandExecutionResult executeArg(StateDiagram system, RegexResult arg) {
		final String ent1 = arg.get("ENT1", 0);
		final String ent2 = arg.get("ENT2", 0);

		final IEntity cl1 = getEntityStart(system, ent1);
		final IEntity cl2 = getEntityEnd(system, ent2);

		if (arg.get("ENT1", 1) != null) {
			cl1.setStereotype(new Stereotype(arg.get("ENT1", 1)));
		}
		if (arg.get("ENT1", 2) != null) {
			cl1.setSpecificBackcolor(HtmlColorUtils.getColorIfValid(arg.get("ENT1", 2)));
		}
		if (arg.get("ENT2", 1) != null) {
			cl2.setStereotype(new Stereotype(arg.get("ENT2", 1)));
		}
		if (arg.get("ENT2", 2) != null) {
			cl2.setSpecificBackcolor(HtmlColorUtils.getColorIfValid(arg.get("ENT2", 2)));
		}

		String queue = arg.get("ARROW_BODY1", 0) + arg.get("ARROW_BODY2", 0);
		final Direction dir = getDirection(arg);

		if (dir == Direction.LEFT || dir == Direction.RIGHT) {
			queue = "-";
		}

		final int lenght = queue.length();

		final boolean crossStart = arg.get("ARROW_CROSS_START", 0) != null;
		final boolean circleEnd = arg.get("ARROW_CIRCLE_END", 0) != null;
		final LinkType linkType = new LinkType(circleEnd ? LinkDecor.ARROW_AND_CIRCLE : LinkDecor.ARROW,
				crossStart ? LinkDecor.CIRCLE_CROSS : LinkDecor.NONE);

		Link link = new Link(cl1, cl2, linkType, Display.getWithNewlines(arg.get("LABEL", 0)), lenght);
		if (dir == Direction.LEFT || dir == Direction.UP) {
			link = link.getInv();
		}
		CommandLinkClass.applyStyle(arg.getLazzy("ARROW_STYLE", 0), link);
		system.addLink(link);

		return CommandExecutionResult.ok();
	}

//	public static void applyStyle(String arrowStyle, Link link) {
//		if (arrowStyle == null) {
//			return;
//		}
//		final StringTokenizer st = new StringTokenizer(arrowStyle, ",");
//		while (st.hasMoreTokens()) {
//			final String s = st.nextToken();
//			if (s.equalsIgnoreCase("dashed")) {
//				link.goDashed();
//			} else if (s.equalsIgnoreCase("bold")) {
//				link.goBold();
//			} else if (s.equalsIgnoreCase("dotted")) {
//				link.goDotted();
//			} else if (s.equalsIgnoreCase("hidden")) {
//				link.goHidden();
//			} else {
//				link.setSpecificColor(s);
//			}
//		}
//	}

	private Direction getDirection(RegexResult arg) {
		final String arrowDirection = arg.get("ARROW_DIRECTION", 0);
		if (arrowDirection != null) {
			return StringUtils.getQueueDirection(arrowDirection);
		}
		return null;
	}

	private IEntity getEntityStart(StateDiagram system, String code) {
		if (code.startsWith("[*]")) {
			return system.getStart();
		}
		if (code.equalsIgnoreCase("[H]")) {
			return system.getHistorical();
		}
		if (code.endsWith("[H]")) {
			return system.getHistorical(Code.of(code.substring(0, code.length() - 3)));
		}
		if (code.startsWith("=") && code.endsWith("=")) {
			code = removeEquals(code);
			return system.getOrCreateLeaf1(Code.of(code), LeafType.SYNCHRO_BAR);
		}
		return system.getOrCreateLeaf1(Code.of(code), null);
	}

	private String removeEquals(String code) {
		while (code.startsWith("=")) {
			code = code.substring(1);
		}
		while (code.endsWith("=")) {
			code = code.substring(0, code.length() - 1);
		}
		return code;
	}

	private IEntity getEntityEnd(StateDiagram system, String code) {
		if (code.startsWith("[*]")) {
			return system.getEnd();
		}
		if (code.endsWith("[H]")) {
			return system.getHistorical(Code.of(code.substring(0, code.length() - 3)));
		}
		if (code.startsWith("=") && code.endsWith("=")) {
			code = removeEquals(code);
			return system.getOrCreateLeaf1(Code.of(code), LeafType.SYNCHRO_BAR);
		}
		return system.getOrCreateLeaf1(Code.of(code), null);
	}

}
