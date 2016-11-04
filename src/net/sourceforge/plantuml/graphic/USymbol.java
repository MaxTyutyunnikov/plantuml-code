/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2017, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
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
package net.sourceforge.plantuml.graphic;

import java.awt.geom.Dimension2D;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.plantuml.ColorParam;
import net.sourceforge.plantuml.Dimension2DDouble;
import net.sourceforge.plantuml.FontParam;
import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.creole.Stencil;

public abstract class USymbol {

	private static final Map<String, USymbol> all = new HashMap<String, USymbol>();

	public final static USymbol STORAGE = record("STORAGE", SkinParameter.STORAGE, new USymbolStorage());
	public final static USymbol DATABASE = record("DATABASE", SkinParameter.DATABASE, new USymbolDatabase());
	public final static USymbol CLOUD = record("CLOUD", SkinParameter.CLOUD, new USymbolCloud());
	public final static USymbol CARD = record("CARD", SkinParameter.CARD, new USymbolCard(SkinParameter.CARD));
	public final static USymbol FRAME = record("FRAME", SkinParameter.FRAME, new USymbolFrame());
	public final static USymbol NODE = record("NODE", SkinParameter.NODE, new USymbolNode());
	public final static USymbol ARTIFACT = record("ARTIFACT", SkinParameter.ARTIFACT, new USymbolArtifact());
	public final static USymbol PACKAGE = record("PACKAGE", SkinParameter.PACKAGE, new USymbolFolder(
			SkinParameter.PACKAGE));
	public final static USymbol FOLDER = record("FOLDER", SkinParameter.FOLDER, new USymbolFolder(SkinParameter.FOLDER));
	public final static USymbol FILE = record("FILE", SkinParameter.FILE, new USymbolFile());
	public final static USymbol RECTANGLE = record("RECTANGLE", SkinParameter.CARD, new USymbolRect(SkinParameter.CARD,
			HorizontalAlignment.CENTER));
	public final static USymbol AGENT = record("AGENT", SkinParameter.AGENT, new USymbolRect(SkinParameter.AGENT,
			HorizontalAlignment.CENTER));
	public final static USymbol ACTOR = record("ACTOR", SkinParameter.ACTOR, new USymbolActor());
	public final static USymbol USECASE = null;
	public final static USymbol COMPONENT1 = record("COMPONENT1", SkinParameter.COMPONENT1, new USymbolComponent1());
	public final static USymbol COMPONENT2 = record("COMPONENT2", SkinParameter.COMPONENT2, new USymbolComponent2());
	public final static USymbol BOUNDARY = record("BOUNDARY", SkinParameter.BOUNDARY, new USymbolBoundary());
	public final static USymbol ENTITY_DOMAIN = record("ENTITY_DOMAIN", SkinParameter.ENTITY_DOMAIN,
			new USymbolEntityDomain(2));
	public final static USymbol CONTROL = record("CONTROL", SkinParameter.CONTROL, new USymbolControl(2));
	public final static USymbol INTERFACE = record("INTERFACE", SkinParameter.INTERFACE, new USymbolInterface());
	public final static USymbol QUEUE = record("QUEUE", SkinParameter.QUEUE, new USymbolQueue());
	public final static USymbol TOGETHER = record("TOGETHER", SkinParameter.QUEUE, new USymbolTogether());

	abstract public SkinParameter getSkinParameter();

	public USymbol withStereoAlignment(HorizontalAlignment alignment) {
		return this;
	}

	public FontParam getFontParam() {
		return getSkinParameter().getFontParam();
	}

	public FontParam getFontParamStereotype() {
		return getSkinParameter().getFontParamStereotype();

	}

	public ColorParam getColorParamBack() {
		return getSkinParameter().getColorParamBack();
	}

	public ColorParam getColorParamBorder() {
		return getSkinParameter().getColorParamBorder();
	}

	public static USymbol getFromString(String s) {
		if (s == null) {
			return null;
		}
		final USymbol result = all.get(StringUtils.goUpperCase(s.replaceAll("\\W", "")));
		if (result == null) {
			if (s.equalsIgnoreCase("component")) {
				return COMPONENT2;
			}
			return null;
		}
		return result;
	}

	private static USymbol record(String code, SkinParameter skinParameter, USymbol symbol) {
		all.put(StringUtils.goUpperCase(code), symbol);
		return symbol;
	}

	public abstract TextBlock asSmall(TextBlock name, TextBlock label, TextBlock stereotype, SymbolContext symbolContext);

	public abstract TextBlock asBig(TextBlock label, TextBlock stereotype, double width, double height,
			SymbolContext symbolContext);

	static class Margin {
		private final double x1;
		private final double x2;
		private final double y1;
		private final double y2;

		Margin(double x1, double x2, double y1, double y2) {
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
		}

		double getWidth() {
			return x1 + x2;
		}

		double getHeight() {
			return y1 + y2;
		}

		public Dimension2D addDimension(Dimension2D dim) {
			return new Dimension2DDouble(dim.getWidth() + x1 + x2, dim.getHeight() + y1 + y2);
		}

		public double getX1() {
			return x1;
		}

		public double getY1() {
			return y1;
		}
	}

	public boolean manageHorizontalLine() {
		return false;
	}

	public int suppHeightBecauseOfShape() {
		return 0;
	}

	public int suppWidthBecauseOfShape() {
		return 0;
	}

	final Stencil getRectangleStencil(final Dimension2D dim) {
		return new Stencil() {
			public double getStartingX(StringBounder stringBounder, double y) {
				return 0;
			}

			public double getEndingX(StringBounder stringBounder, double y) {
				return dim.getWidth();
			}
		};
	}

}