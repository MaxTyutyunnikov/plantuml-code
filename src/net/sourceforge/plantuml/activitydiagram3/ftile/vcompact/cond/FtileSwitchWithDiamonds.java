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
package net.sourceforge.plantuml.activitydiagram3.ftile.vcompact.cond;

import java.awt.geom.Dimension2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.plantuml.activitydiagram3.ftile.Ftile;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileGeometry;
import net.sourceforge.plantuml.activitydiagram3.ftile.Swimlane;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.ugraphic.UChange;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UTranslate;

public class FtileSwitchWithDiamonds extends FtileSwitchNude {

	protected final Ftile diamond1;
	protected final Ftile diamond2;

	public FtileSwitchWithDiamonds(List<Ftile> tiles, Swimlane in, Ftile diamond1, Ftile diamond2,
			StringBounder stringBounder) {
		super(tiles, in);
		this.diamond1 = diamond1;
		this.diamond2 = diamond2;
	}

	@Override
	public Collection<Ftile> getMyChildren() {
		final Collection<Ftile> result = new ArrayList<Ftile>(super.getMyChildren());
		result.add(diamond1);
		result.add(diamond2);
		return Collections.unmodifiableCollection(result);
	}

	public int getYdelta1a(StringBounder stringBounder) {
		return 20;
	}

	public int getYdelta1b(StringBounder stringBounder) {
		return 10;
	}

	@Override
	protected FtileGeometry calculateDimensionInternalSlow(StringBounder stringBounder) {
		final FtileGeometry dim1 = diamond1.calculateDimension(stringBounder);
		final FtileGeometry dim2 = diamond2.calculateDimension(stringBounder);

		final FtileGeometry dimNude = super.calculateDimensionInternalSlow(stringBounder);

		final FtileGeometry all = dim1.appendBottom(dimNude).appendBottom(dim2);

		return all.addDim(0, getYdelta1a(stringBounder) + getYdelta1b(stringBounder));

	}

	@Override
	public void drawU(UGraphic ug) {
		final StringBounder stringBounder = ug.getStringBounder();

		ug.apply(getTranslateDiamond1(stringBounder)).draw(diamond1);
		super.drawU(ug.apply(getUTranslateMain(stringBounder)));
		ug.apply(getTranslateDiamond2(stringBounder)).draw(diamond2);
	}

	private UChange getUTranslateMain(StringBounder stringBounder) {
		final FtileGeometry dimDiamond1 = diamond1.calculateDimension(stringBounder);
		return new UTranslate(0, dimDiamond1.getHeight() + getYdelta1a(stringBounder));
	}

	protected UTranslate getTranslateDiamond1(StringBounder stringBounder) {
		final double y1 = 0;
		final FtileGeometry dimTotal = calculateDimensionInternal(stringBounder);
		final FtileGeometry dimDiamond1 = diamond1.calculateDimension(stringBounder);
		final double x1 = dimTotal.getLeft() - dimDiamond1.getLeft();
		return new UTranslate(x1, y1);
	}

	protected UTranslate getTranslateDiamond2(StringBounder stringBounder) {
		final FtileGeometry dimTotal = calculateDimensionInternal(stringBounder);
		final FtileGeometry dimDiamond2 = diamond2.calculateDimension(stringBounder);
		final double y2 = dimTotal.getHeight() - dimDiamond2.getHeight();
		final double x2 = dimTotal.getLeft() - dimDiamond2.getWidth() / 2;
		return new UTranslate(x2, y2);
	}

}
