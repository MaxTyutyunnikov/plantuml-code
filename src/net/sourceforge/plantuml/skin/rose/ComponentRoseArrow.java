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
 * Revision $Revision: 11154 $
 *
 */
package net.sourceforge.plantuml.skin.rose;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;

import net.sourceforge.plantuml.SpriteContainer;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.HtmlColor;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.skin.Area;
import net.sourceforge.plantuml.skin.ArrowConfiguration;
import net.sourceforge.plantuml.skin.ArrowDecoration;
import net.sourceforge.plantuml.skin.ArrowDirection;
import net.sourceforge.plantuml.skin.ArrowHead;
import net.sourceforge.plantuml.skin.ArrowPart;
import net.sourceforge.plantuml.ugraphic.UChangeBackColor;
import net.sourceforge.plantuml.ugraphic.UChangeColor;
import net.sourceforge.plantuml.ugraphic.UEllipse;
import net.sourceforge.plantuml.ugraphic.UFont;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.ULine;
import net.sourceforge.plantuml.ugraphic.UPolygon;
import net.sourceforge.plantuml.ugraphic.UStroke;
import net.sourceforge.plantuml.ugraphic.UTranslate;

public class ComponentRoseArrow extends AbstractComponentRoseArrow {

	private final HorizontalAlignment messagePosition;
	private final boolean niceArrow;

	public ComponentRoseArrow(HtmlColor foregroundColor, HtmlColor fontColor, UFont font, Display stringsToDisplay,
			ArrowConfiguration arrowConfiguration, HorizontalAlignment messagePosition,
			SpriteContainer spriteContainer, HorizontalAlignment textHorizontalAlignment, double maxMessageSize,
			boolean niceArrow) {
		super(foregroundColor, fontColor, font, stringsToDisplay, arrowConfiguration, spriteContainer,
				textHorizontalAlignment, maxMessageSize);
		this.messagePosition = messagePosition;
		this.niceArrow = niceArrow;
	}

	private final double spaceCrossX = 6;
	private final double diamCircle = 8;
	private final double thinCircle = 1.5;

	@Override
	public void drawInternalU(UGraphic ug, Area area) {
		final Dimension2D dimensionToUse = area.getDimensionToUse();
		final StringBounder stringBounder = ug.getStringBounder();
		final int textHeight = (int) getTextHeight(stringBounder);
		ug = ug.apply(new UChangeColor(getForegroundColor()));

		final double x2 = dimensionToUse.getWidth() - 1;

		if (getArrowConfiguration().isDotted()) {
			ug = stroke(ug, 2, 2);
		}

		//
		double start = 0;
		double len = x2;
		final ArrowDirection direction2 = getDirection2();
		double arrowHeadPosition = direction2 == ArrowDirection.LEFT_TO_RIGHT_NORMAL ? x2 - 1 : 2;
		final ArrowDecoration decorationStart = getArrowConfiguration().getDecorationStart();
		if (decorationStart == ArrowDecoration.CIRCLE) {
			if (direction2 == ArrowDirection.LEFT_TO_RIGHT_NORMAL) {
				start += diamCircle / 2;
				len -= diamCircle / 2;
			} else if (direction2 == ArrowDirection.RIGHT_TO_LEFT_REVERSE) {
				len -= diamCircle / 2;
			}
		}

		final ArrowDecoration decorationEnd = getArrowConfiguration().getDecorationEnd();
		switch (decorationEnd) {
		case CROSSX_toberemoved:
			if (direction2 == ArrowDirection.LEFT_TO_RIGHT_NORMAL) {
				len -= spaceCrossX + getArrowDeltaX() / 2;
			} else if (direction2 == ArrowDirection.RIGHT_TO_LEFT_REVERSE) {
				start += spaceCrossX + getArrowDeltaX() / 2;
				len -= spaceCrossX + getArrowDeltaX() / 2;
			}
			break;

		case CIRCLE:
			if (direction2 == ArrowDirection.LEFT_TO_RIGHT_NORMAL) {
				len -= diamCircle / 2;
				arrowHeadPosition -= diamCircle / 2 + thinCircle;
			} else if (direction2 == ArrowDirection.RIGHT_TO_LEFT_REVERSE) {
				start += diamCircle / 2;
				len -= diamCircle / 2;
				arrowHeadPosition += diamCircle / 2 + thinCircle;
			}
			break;
		default:
		}
		if (decorationEnd != ArrowDecoration.CROSSX_toberemoved
				&& getArrowConfiguration().getHead() == ArrowHead.NORMAL
				&& getArrowConfiguration().getPart() == ArrowPart.FULL) {
			if (direction2 == ArrowDirection.LEFT_TO_RIGHT_NORMAL) {
				len -= getArrowDeltaX() / 2;
			} else if (direction2 == ArrowDirection.RIGHT_TO_LEFT_REVERSE) {
				start += getArrowDeltaX() / 2;
				len -= getArrowDeltaX() / 2;
			}
		}

		ug.apply(new UTranslate(start, textHeight)).draw(new ULine(len, 0));
		if (getArrowConfiguration().isDotted()) {
			ug = ug.apply(new UStroke());
		}
		if (direction2 == ArrowDirection.LEFT_TO_RIGHT_NORMAL) {
			if (getArrowConfiguration().isAsync()) {
				if (getArrowConfiguration().getPart() != ArrowPart.BOTTOM_PART) {
					ug.apply(new UTranslate(arrowHeadPosition, textHeight)).draw(
							new ULine(-getArrowDeltaX(), -getArrowDeltaY()));
				}
				if (getArrowConfiguration().getPart() != ArrowPart.TOP_PART) {
					ug.apply(new UTranslate(arrowHeadPosition, textHeight)).draw(
							new ULine(-getArrowDeltaX(), getArrowDeltaY()));
				}
			} else if (decorationEnd == ArrowDecoration.CROSSX_toberemoved) {
				ug = ug.apply(new UStroke(2));
				ug.apply(new UTranslate(x2 - getArrowDeltaX() - spaceCrossX, textHeight - getArrowDeltaX() / 2)).draw(
						new ULine(getArrowDeltaX(), getArrowDeltaX()));
				ug.apply(new UTranslate(x2 - getArrowDeltaX() - spaceCrossX, textHeight + getArrowDeltaX() / 2)).draw(
						new ULine(getArrowDeltaX(), -getArrowDeltaX()));
				ug = ug.apply(new UStroke());
			} else {
				final UPolygon polygon = getPolygonNormal(textHeight, arrowHeadPosition);
				ug.apply(new UChangeBackColor(getForegroundColor())).draw(polygon);
			}

			if (decorationStart == ArrowDecoration.CIRCLE) {
				ug = ug.apply(new UStroke(thinCircle)).apply(new UChangeColor(getForegroundColor()));
				final UEllipse circle = new UEllipse(diamCircle, diamCircle);
				ug.apply(new UTranslate(-diamCircle / 2 - 0.5, textHeight - diamCircle / 2 - thinCircle / 2)).draw(
						circle);
				ug = ug.apply(new UStroke());
			}
			if (decorationEnd == ArrowDecoration.CIRCLE) {
				ug = ug.apply(new UStroke(thinCircle)).apply(new UChangeColor(getForegroundColor()));
				final UEllipse circle = new UEllipse(diamCircle, diamCircle);
				ug.apply(new UTranslate(x2 - diamCircle / 2 + 0.5, textHeight - diamCircle / 2 - thinCircle / 2)).draw(
						circle);
				ug = ug.apply(new UStroke());
			}
		} else {
			if (getArrowConfiguration().isAsync()) {
				if (getArrowConfiguration().getPart() != ArrowPart.BOTTOM_PART) {
					ug.apply(new UTranslate(arrowHeadPosition - 1, textHeight)).draw(
							new ULine(getArrowDeltaX(), -getArrowDeltaY()));
				}
				if (getArrowConfiguration().getPart() != ArrowPart.TOP_PART) {
					ug.apply(new UTranslate(arrowHeadPosition - 1, textHeight)).draw(
							new ULine(getArrowDeltaX(), getArrowDeltaY()));
				}
			} else if (decorationEnd == ArrowDecoration.CROSSX_toberemoved) {
				ug = ug.apply(new UStroke(2));
				ug.apply(new UTranslate(spaceCrossX, textHeight - getArrowDeltaX() / 2)).draw(
						new ULine(getArrowDeltaX(), getArrowDeltaX()));
				ug.apply(new UTranslate(spaceCrossX, textHeight + getArrowDeltaX() / 2)).draw(
						new ULine(getArrowDeltaX(), -getArrowDeltaX()));
				ug = ug.apply(new UStroke());
			} else {
				final UPolygon polygon = getPolygonReverse(textHeight);
				ug.apply(new UChangeBackColor(getForegroundColor())).apply(new UTranslate(arrowHeadPosition, 0))
						.draw(polygon);
			}

			if (decorationStart == ArrowDecoration.CIRCLE) {
				ug = ug.apply(new UStroke(thinCircle)).apply(new UChangeColor(getForegroundColor()));
				final UEllipse circle = new UEllipse(diamCircle, diamCircle);
				ug.apply(new UTranslate(x2 - diamCircle / 2 + 0.5, textHeight - diamCircle / 2 - thinCircle / 2)).draw(
						circle);
				ug = ug.apply(new UStroke());
			}
			if (decorationEnd == ArrowDecoration.CIRCLE) {
				ug = ug.apply(new UStroke(thinCircle)).apply(new UChangeColor(getForegroundColor()));
				final UEllipse circle = new UEllipse(diamCircle, diamCircle);
				ug.apply(new UTranslate(-diamCircle / 2 - 0.5, textHeight - diamCircle / 2 - thinCircle / 2)).draw(
						circle);
				ug = ug.apply(new UStroke());
			}
		}
		final double textPos;
		if (messagePosition == HorizontalAlignment.CENTER) {
			final double textWidth = getTextBlock().calculateDimension(stringBounder).getWidth();
			textPos = (dimensionToUse.getWidth() - textWidth) / 2;
		} else if (messagePosition == HorizontalAlignment.RIGHT) {
			final double textWidth = getTextBlock().calculateDimension(stringBounder).getWidth();
			textPos = dimensionToUse.getWidth() - textWidth - getMarginX2()
					- (direction2 == ArrowDirection.LEFT_TO_RIGHT_NORMAL ? getArrowDeltaX() : 0);
		} else {
			textPos = getMarginX1() + (direction2 == ArrowDirection.RIGHT_TO_LEFT_REVERSE ? getArrowDeltaX() : 0);
		}
		getTextBlock().drawU(ug.apply(new UTranslate(textPos, 0)));
	}

	private UPolygon getPolygonNormal(final int textHeight, final double x2) {
		final UPolygon polygon = new UPolygon();
		if (getArrowConfiguration().getPart() == ArrowPart.TOP_PART) {
			polygon.addPoint(x2 - getArrowDeltaX(), textHeight - getArrowDeltaY());
			polygon.addPoint(x2, textHeight);
			polygon.addPoint(x2 - getArrowDeltaX(), textHeight);
		} else if (getArrowConfiguration().getPart() == ArrowPart.BOTTOM_PART) {
			polygon.addPoint(x2 - getArrowDeltaX(), textHeight + 1);
			polygon.addPoint(x2, textHeight + 1);
			polygon.addPoint(x2 - getArrowDeltaX(), textHeight + getArrowDeltaY() + 1);
		} else {
			polygon.addPoint(x2 - getArrowDeltaX(), textHeight - getArrowDeltaY());
			polygon.addPoint(x2, textHeight);
			polygon.addPoint(x2 - getArrowDeltaX(), textHeight + getArrowDeltaY());
			if (niceArrow) {
				polygon.addPoint(x2 - getArrowDeltaX() + 4, textHeight);
			}
		}
		return polygon;
	}

	private UPolygon getPolygonReverse(final int textHeight) {
		final UPolygon polygon = new UPolygon();
		if (getArrowConfiguration().getPart() == ArrowPart.TOP_PART) {
			polygon.addPoint(getArrowDeltaX(), textHeight - getArrowDeltaY());
			polygon.addPoint(0, textHeight);
			polygon.addPoint(getArrowDeltaX(), textHeight);
		} else if (getArrowConfiguration().getPart() == ArrowPart.BOTTOM_PART) {
			polygon.addPoint(getArrowDeltaX(), textHeight + 1);
			polygon.addPoint(0, textHeight + 1);
			polygon.addPoint(getArrowDeltaX(), textHeight + getArrowDeltaY() + 1);
		} else {
			polygon.addPoint(getArrowDeltaX(), textHeight - getArrowDeltaY());
			polygon.addPoint(0, textHeight);
			polygon.addPoint(getArrowDeltaX(), textHeight + getArrowDeltaY());
			if (niceArrow) {
				polygon.addPoint(getArrowDeltaX() - 4, textHeight);
			}
		}
		return polygon;
	}

	public Point2D getStartPoint(StringBounder stringBounder, Dimension2D dimensionToUse) {
		final int textHeight = (int) getTextHeight(stringBounder);
		if (getDirection2() == ArrowDirection.LEFT_TO_RIGHT_NORMAL) {
			return new Point2D.Double(getPaddingX(), textHeight + getPaddingY());
		}
		return new Point2D.Double(dimensionToUse.getWidth() + getPaddingX(), textHeight + getPaddingY());
	}

	public Point2D getEndPoint(StringBounder stringBounder, Dimension2D dimensionToUse) {
		final int textHeight = (int) getTextHeight(stringBounder);
		if (getDirection2() == ArrowDirection.LEFT_TO_RIGHT_NORMAL) {
			return new Point2D.Double(dimensionToUse.getWidth() + getPaddingX(), textHeight + getPaddingY());
		}
		return new Point2D.Double(getPaddingX(), textHeight + getPaddingY());
	}

	final private ArrowDirection getDirection2() {
		return getArrowConfiguration().getArrowDirection();
	}

	@Override
	public double getPreferredHeight(StringBounder stringBounder) {
		return getTextHeight(stringBounder) + getArrowDeltaY() + 2 * getPaddingY();
	}

	@Override
	public double getPreferredWidth(StringBounder stringBounder) {
		return getTextWidth(stringBounder) + getArrowDeltaX();
	}

}
