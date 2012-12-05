/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2012, Arnaud Roques
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
 * Revision $Revision: 5183 $
 *
 */
package net.sourceforge.plantuml.svek.image;

import java.awt.geom.Dimension2D;
import java.util.List;

import net.sourceforge.plantuml.ColorParam;
import net.sourceforge.plantuml.Dimension2DDouble;
import net.sourceforge.plantuml.FontParam;
import net.sourceforge.plantuml.ISkinParam;
import net.sourceforge.plantuml.SkinParamUtils;
import net.sourceforge.plantuml.Url;
import net.sourceforge.plantuml.cucadiagram.ILeaf;
import net.sourceforge.plantuml.cucadiagram.Stereotype;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.HorizontalAlignement;
import net.sourceforge.plantuml.graphic.HtmlColor;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.graphic.TextBlockUtils;
import net.sourceforge.plantuml.svek.AbstractEntityImage;
import net.sourceforge.plantuml.svek.ShapeType;
import net.sourceforge.plantuml.ugraphic.Shadowable;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.URectangle;
import net.sourceforge.plantuml.ugraphic.UStroke;

public class EntityImageActivity extends AbstractEntityImage {

	public static final int CORNER = 25;
	final private TextBlock desc;
	final private static int MARGIN = 10;
	final private List<Url> url;

	public EntityImageActivity(ILeaf entity, ISkinParam skinParam) {
		super(entity, skinParam);
		final Stereotype stereotype = entity.getStereotype();

		this.desc = TextBlockUtils.create(entity.getDisplay(), new FontConfiguration(SkinParamUtils.getFont(getSkinParam(), FontParam.ACTIVITY, stereotype), SkinParamUtils.getFontColor(getSkinParam(), FontParam.ACTIVITY, stereotype)), HorizontalAlignement.CENTER, skinParam);
		this.url = entity.getUrls();
	}

	@Override
	public Dimension2D getDimension(StringBounder stringBounder) {
		final Dimension2D dim = desc.calculateDimension(stringBounder);
		return Dimension2DDouble.delta(dim, MARGIN * 2);
	}

	public void drawU(UGraphic ug, double xTheoricalPosition, double yTheoricalPosition) {
		if (url.size()>0) {
			ug.startUrl(url.get(0));
		}
		final StringBounder stringBounder = ug.getStringBounder();
		final Dimension2D dimTotal = getDimension(stringBounder);

		final double widthTotal = dimTotal.getWidth();
		final double heightTotal = dimTotal.getHeight();
		final Shadowable rect = new URectangle(widthTotal, heightTotal, CORNER, CORNER);
		if (getSkinParam().shadowing()) {
			rect.setDeltaShadow(4);
		}

		ug.getParam().setStroke(new UStroke(1.5));
		ug.getParam().setColor(SkinParamUtils.getColor(getSkinParam(), ColorParam.activityBorder, getStereo()));
		HtmlColor backcolor = getEntity().getSpecificBackColor();
		if (backcolor == null) {
			backcolor = SkinParamUtils.getColor(getSkinParam(), ColorParam.activityBackground, getStereo());
		}
		ug.getParam().setBackcolor(backcolor);

		ug.draw(xTheoricalPosition, yTheoricalPosition, rect);
		ug.getParam().setStroke(new UStroke());

		final double x = xTheoricalPosition + MARGIN;
		final double y = yTheoricalPosition + MARGIN;
		desc.drawU(ug, x, y);
		if (url.size()>0) {
			ug.closeAction();
		}
	}

	public ShapeType getShapeType() {
		return ShapeType.ROUND_RECTANGLE;
	}

	public int getShield() {
		return 0;
	}

}
