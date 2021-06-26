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
package net.sourceforge.plantuml.project.lang;

import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.command.regex.IRegex;
import net.sourceforge.plantuml.command.regex.RegexLeaf;
import net.sourceforge.plantuml.command.regex.RegexResult;
import net.sourceforge.plantuml.project.Failable;
import net.sourceforge.plantuml.project.GanttDiagram;
import net.sourceforge.plantuml.project.time.DayOfWeek;
import net.sourceforge.plantuml.ugraphic.color.HColor;

public class SubjectDayOfWeek implements Subject {

	public IRegex toRegex() {
		return new RegexLeaf("SUBJECT", "(" + DayOfWeek.getRegexString() + ")");
	}

	public Failable<? extends Object> getMe(GanttDiagram project, RegexResult arg) {
		final String s = arg.get("SUBJECT", 0);
		return Failable.ok(DayOfWeek.fromString(s));
	}

	public Collection<? extends SentenceSimple> getSentences() {
		return Arrays.asList(new AreClose(), new InColor());
	}

	class AreClose extends SentenceSimple {

		public AreClose() {
			super(SubjectDayOfWeek.this, Verbs.are(), new ComplementClose());
		}

		@Override
		public CommandExecutionResult execute(GanttDiagram project, Object subject, Object complement) {
			final DayOfWeek day = (DayOfWeek) subject;
			project.closeDayOfWeek(day);
			return CommandExecutionResult.ok();
		}

	}

	class InColor extends SentenceSimple {

		public InColor() {
			super(SubjectDayOfWeek.this, Verbs.isOrAre(), new ComplementInColors2());
		}

		@Override
		public CommandExecutionResult execute(GanttDiagram project, Object subject, Object complement) {
			final HColor color = ((CenterBorderColor) complement).getCenter();
			final DayOfWeek day = (DayOfWeek) subject;
			project.colorDay(day, color);

			return CommandExecutionResult.ok();
		}

	}

}
