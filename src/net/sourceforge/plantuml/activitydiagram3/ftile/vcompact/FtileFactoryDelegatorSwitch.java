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
package net.sourceforge.plantuml.activitydiagram3.ftile.vcompact;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.plantuml.ColorParam;
import net.sourceforge.plantuml.FontParam;
import net.sourceforge.plantuml.LineBreakStrategy;
import net.sourceforge.plantuml.activitydiagram3.Branch;
import net.sourceforge.plantuml.activitydiagram3.LinkRendering;
import net.sourceforge.plantuml.activitydiagram3.ftile.Diamond;
import net.sourceforge.plantuml.activitydiagram3.ftile.Ftile;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileFactory;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileFactoryDelegator;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileMinWidth;
import net.sourceforge.plantuml.activitydiagram3.ftile.Swimlane;
import net.sourceforge.plantuml.activitydiagram3.ftile.vcompact.cond.FtileSwitchNude;
import net.sourceforge.plantuml.activitydiagram3.ftile.vcompact.cond.FtileSwitchWithDiamonds;
import net.sourceforge.plantuml.activitydiagram3.ftile.vertical.FtileDiamond;
import net.sourceforge.plantuml.activitydiagram3.ftile.vertical.FtileDiamondInside;
import net.sourceforge.plantuml.creole.CreoleMode;
import net.sourceforge.plantuml.creole.CreoleParser;
import net.sourceforge.plantuml.creole.Sheet;
import net.sourceforge.plantuml.creole.SheetBlock1;
import net.sourceforge.plantuml.creole.SheetBlock2;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.HtmlColor;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.graphic.TextBlockUtils;
import net.sourceforge.plantuml.svek.ConditionStyle;

public class FtileFactoryDelegatorSwitch extends FtileFactoryDelegator {

	public FtileFactoryDelegatorSwitch(FtileFactory factory) {
		super(factory);
	}

	@Override
	public Ftile createSwitch(Swimlane swimlane, List<Branch> branches, LinkRendering afterEndwhile,
			LinkRendering topInlinkRendering, Display labelTest) {
		// final ConditionStyle conditionStyle = skinParam().getConditionStyle();
		//
		// final HtmlColor borderColor = getRose().getHtmlColor(skinParam(), ColorParam.activityDiamondBorder);
		// final HtmlColor backColor = getRose().getHtmlColor(skinParam(), ColorParam.activityDiamondBackground);
		// final Rainbow arrowColor = HtmlColorAndStyle.build(skinParam());
		//
		// final FontConfiguration fcArrow = new FontConfiguration(skinParam(), FontParam.ARROW, null);
		//
		// final FontParam testParam = conditionStyle == ConditionStyle.INSIDE ? FontParam.ACTIVITY_DIAMOND
		// : FontParam.ARROW;
		// final FontConfiguration fcTest = new FontConfiguration(skinParam(), testParam, null)
		// .changeColor(fontColor(FontParam.ACTIVITY_DIAMOND));
		//
		// return FtileSwitch.create(swimlane, borderColor, backColor, arrowColor, getFactory(), conditionStyle,
		// branches,
		// fcArrow, topInlinkRendering, afterEndwhile, fcTest);

		// return createNude(swimlane, branches);
		return createWithDiamonds(swimlane, branches, labelTest);
	}

	private Ftile createNude(Swimlane swimlane, List<Branch> branches) {
		final List<Ftile> ftiles = new ArrayList<Ftile>();
		for (Branch branch : branches) {
			ftiles.add(new FtileMinWidth(branch.getFtile(), 30));
		}
		return new FtileSwitchNude(ftiles, swimlane);
	}

	private Ftile createWithDiamonds(Swimlane swimlane, List<Branch> branches, Display labelTest) {
		final List<Ftile> ftiles = new ArrayList<Ftile>();
		for (Branch branch : branches) {
			ftiles.add(new FtileMinWidth(branch.getFtile(), 30));
		}
		final Ftile diamond1 = getDiamond1(swimlane, branches.get(0), labelTest);
		final Ftile diamond2 = getDiamond2(swimlane, branches.get(0));
		return new FtileSwitchWithDiamonds(ftiles, swimlane, diamond1, diamond2, getStringBounder());
	}

	private Ftile getDiamond1(Swimlane swimlane, Branch branch0, Display test) {
		final HtmlColor borderColor = getRose().getHtmlColor(skinParam(), ColorParam.activityDiamondBorder);
		final HtmlColor backColor = branch0.getColor() == null ? getRose().getHtmlColor(skinParam(),
				ColorParam.activityDiamondBackground) : branch0.getColor();

		final FontConfiguration fcDiamond = new FontConfiguration(skinParam(), FontParam.ACTIVITY_DIAMOND, null);

		final TextBlock tbTest = (Display.isNull(test) || test.isWhite()) ? TextBlockUtils.empty(0, 0) : test.create(
				fcDiamond, branch0.skinParam().getDefaultTextAlignment(HorizontalAlignment.LEFT), branch0.skinParam());

		return new FtileDiamondInside(branch0.skinParam(), backColor, borderColor, swimlane, tbTest);
	}

	private Ftile getDiamond2(Swimlane swimlane, Branch branch0) {
		final HtmlColor borderColor = getRose().getHtmlColor(skinParam(), ColorParam.activityDiamondBorder);
		final HtmlColor backColor = branch0.getColor() == null ? getRose().getHtmlColor(skinParam(),
				ColorParam.activityDiamondBackground) : branch0.getColor();

		return new FtileDiamondInside(branch0.skinParam(), backColor, borderColor, swimlane, TextBlockUtils.empty(0, 0));
	}

	private HtmlColor fontColor(FontParam param) {
		return skinParam().getFontHtmlColor(null, param);
	}

}
