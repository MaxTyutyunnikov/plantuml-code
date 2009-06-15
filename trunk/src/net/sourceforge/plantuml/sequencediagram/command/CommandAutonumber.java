/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009, Arnaud Roques (for Atos Origin).
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
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
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
 * Original Author:  Arnaud Roques (for Atos Origin).
 *
 */
package net.sourceforge.plantuml.sequencediagram.command;

import java.util.List;

import net.sourceforge.plantuml.SingleLineCommand;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;

public class CommandAutonumber extends SingleLineCommand<SequenceDiagram> {

	public CommandAutonumber(SequenceDiagram sequenceDiagram) {
		super(sequenceDiagram, "(?i)^autonumber\\s*(\\d+)?(?:\\s+(\\d+))?\\s*$");
	}

	@Override
	protected boolean executeArg(List<String> arg) {
		int start = 1;
		if (arg.get(0) != null) {
			start = Integer.parseInt(arg.get(0));
		}
		int inc = 1;
		if (arg.get(1) != null) {
			inc = Integer.parseInt(arg.get(1));
		}
		getSystem().goAutonumber(start, inc);
		return true;
	}

}
