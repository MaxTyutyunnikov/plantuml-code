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
package net.sourceforge.plantuml.activitydiagram3.command;

import net.sourceforge.plantuml.activitydiagram3.ActivityDiagram3;
import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.command.SingleLineCommand2;
import net.sourceforge.plantuml.command.regex.RegexConcat;
import net.sourceforge.plantuml.command.regex.RegexLeaf;
import net.sourceforge.plantuml.command.regex.RegexOptional;
import net.sourceforge.plantuml.command.regex.RegexOr;
import net.sourceforge.plantuml.command.regex.RegexResult;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.descdiagram.command.CommandLinkElement;
import net.sourceforge.plantuml.graphic.Rainbow;

public class CommandRepeatWhile3 extends SingleLineCommand2<ActivityDiagram3> {

	public CommandRepeatWhile3() {
		super(getRegexConcat());
	}

	static RegexConcat getRegexConcat() {
		return new RegexConcat(//
				new RegexLeaf("^"), //
				new RegexLeaf("repeat[%s]?while"), //
				new RegexLeaf("[%s]*"), //
				new RegexOr(//
						new RegexConcat(new RegexLeaf("TEST3", "\\((.*?)\\)"), //
								new RegexLeaf("[%s]*(is|equals?)[%s]*"), //
								new RegexLeaf("WHEN3", "\\((.+?)\\)"), //
								new RegexLeaf("[%s]*(not)[%s]*"), //
								new RegexLeaf("OUT3", "\\((.+?)\\)")), //
						new RegexConcat(new RegexLeaf("TEST4", "\\((.*?)\\)"), //
								new RegexLeaf("[%s]*(not)[%s]*"), //
								new RegexLeaf("OUT4", "\\((.+?)\\)")), //
						new RegexConcat(new RegexLeaf("TEST2", "\\((.*?)\\)"), //
								new RegexLeaf("[%s]*(is|equals?)[%s]*"), //
								new RegexLeaf("WHEN2", "\\((.+?)\\)") //
						), //
						new RegexLeaf("TEST1", "(?:\\((.*)\\))?") //
				), //
				new RegexLeaf("[%s]*"), //
				new RegexOptional(new RegexConcat( //
						new RegexOr(//
								new RegexLeaf("->"), //
								new RegexLeaf("COLOR", CommandLinkElement.STYLE_COLORS_MULTIPLES)), //
						new RegexLeaf("[%s]*"), //
						new RegexOr(//
								new RegexLeaf("LABEL", "(.*)"), //
								new RegexLeaf("")) //
						)), //
				new RegexLeaf(";?$"));
	}

	@Override
	protected CommandExecutionResult executeArg(ActivityDiagram3 diagram, RegexResult arg) {
		final Display test = Display.getWithNewlines(arg.getLazzy("TEST", 0));
		final Display yes = Display.getWithNewlines(arg.getLazzy("WHEN", 0));
		final Display out = Display.getWithNewlines(arg.getLazzy("OUT", 0));

		final String colorString = arg.get("COLOR", 0);
		final Rainbow rainbow;
		if (colorString == null) {
			rainbow = Rainbow.none();
		} else {
			rainbow = Rainbow.build(diagram.getSkinParam(), colorString, diagram.getSkinParam()
					.colorArrowSeparationSpace());
		}

		final Display linkLabel = Display.getWithNewlines(arg.get("LABEL", 0));
		return diagram.repeatWhile(test, yes, out, linkLabel, rainbow);
	}

}
