/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2023, Arnaud Roques
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
package net.sourceforge.plantuml.sequencediagram.teoz;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.real.Real;
import net.sourceforge.plantuml.real.RealUtils;
import net.sourceforge.plantuml.sequencediagram.Doll;
import net.sourceforge.plantuml.sequencediagram.Participant;
import net.sourceforge.plantuml.sequencediagram.ParticipantEnglober;
import net.sourceforge.plantuml.skin.Context2D;
import net.sourceforge.plantuml.ugraphic.UGraphic;

public class Dolls {

	private final Map<ParticipantEnglober, Doll> alls = new LinkedHashMap<>();

	public Dolls(TileArguments tileArguments) {

		for (Participant p : tileArguments.getLivingSpaces().participants()) {
			final ParticipantEnglober englober = tileArguments.getLivingSpaces().get(p).getEnglober();
			if (englober != null)
				for (ParticipantEnglober pe : englober.getGenealogy())
					addParticipant(p, pe, tileArguments);

		}
	}

	private void addParticipant(Participant p, ParticipantEnglober englober, TileArguments tileArguments) {
		Doll already = alls.get(englober);
		if (already == null) {
			already = Doll.createTeoz(englober, tileArguments);
			alls.put(englober, already);
		}
		already.addParticipant(p);
	}

	private Doll getParent(Doll doll) {
		final ParticipantEnglober parent = doll.getParticipantEnglober().getParent();
		if (parent == null)
			return null;
		return alls.get(parent);
	}

	public int size() {
		return alls.size();
	}

	public double getOffsetForEnglobers(StringBounder stringBounder) {
		double result = 0;
		for (Doll doll : alls.values()) {
			double height = doll.getTitlePreferredHeight();
			final Doll group = getParent(doll);
			if (group != null)
				height += group.getTitlePreferredHeight();

			if (height > result)
				result = height;

		}
		return result;
	}

	public void addConstraints(StringBounder stringBounder) {
		for (Doll doll : alls.values()) {
			doll.addInternalConstraints(stringBounder);
		}

		for (Doll doll : alls.values()) {
			doll.addConstraintAfter(stringBounder);
		}

	}

	public void drawEnglobers(UGraphic ug, double height, Context2D context) {
		for (Doll doll : alls.values())
			doll.drawMe(ug, height, context, getParent(doll));

	}

	public Real getMinX(StringBounder stringBounder) {
		if (size() == 0)
			throw new IllegalStateException();

		final List<Real> result = new ArrayList<>();
		for (Doll doll : alls.values())
			result.add(doll.getMinX(stringBounder));

		return RealUtils.min(result);
	}

	public Real getMaxX(StringBounder stringBounder) {
		if (size() == 0)
			throw new IllegalStateException();

		final List<Real> result = new ArrayList<>();
		for (Doll doll : alls.values())
			result.add(doll.getMaxX(stringBounder));

		return RealUtils.max(result);
	}

}
