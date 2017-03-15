/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2017, Arnaud Roques
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
package net.sourceforge.plantuml.svek;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;

import net.sourceforge.plantuml.Dimension2DDouble;
import net.sourceforge.plantuml.Direction;
import net.sourceforge.plantuml.Hideable;
import net.sourceforge.plantuml.ISkinParam;
import net.sourceforge.plantuml.Log;
import net.sourceforge.plantuml.Pragma;
import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.Url;
import net.sourceforge.plantuml.command.Position;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.EntityPort;
import net.sourceforge.plantuml.cucadiagram.IEntity;
import net.sourceforge.plantuml.cucadiagram.IGroup;
import net.sourceforge.plantuml.cucadiagram.Link;
import net.sourceforge.plantuml.cucadiagram.LinkArrow;
import net.sourceforge.plantuml.cucadiagram.LinkDecor;
import net.sourceforge.plantuml.cucadiagram.LinkMiddleDecor;
import net.sourceforge.plantuml.cucadiagram.LinkType;
import net.sourceforge.plantuml.cucadiagram.NoteLinkStrategy;
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizVersion;
import net.sourceforge.plantuml.graphic.AbstractTextBlock;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.HtmlColor;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.graphic.TextBlockArrow;
import net.sourceforge.plantuml.graphic.TextBlockUtils;
import net.sourceforge.plantuml.graphic.UDrawable;
import net.sourceforge.plantuml.graphic.USymbolFolder;
import net.sourceforge.plantuml.graphic.VerticalAlignment;
import net.sourceforge.plantuml.graphic.color.ColorType;
import net.sourceforge.plantuml.posimo.BezierUtils;
import net.sourceforge.plantuml.posimo.DotPath;
import net.sourceforge.plantuml.posimo.Moveable;
import net.sourceforge.plantuml.posimo.Positionable;
import net.sourceforge.plantuml.posimo.PositionableUtils;
import net.sourceforge.plantuml.svek.SvekUtils.PointListIterator;
import net.sourceforge.plantuml.svek.extremity.Extremity;
import net.sourceforge.plantuml.svek.extremity.ExtremityFactory;
import net.sourceforge.plantuml.svek.extremity.ExtremityFactoryExtends;
import net.sourceforge.plantuml.svek.image.EntityImageNoteLink;
import net.sourceforge.plantuml.ugraphic.UChangeBackColor;
import net.sourceforge.plantuml.ugraphic.UChangeColor;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UPolygon;
import net.sourceforge.plantuml.ugraphic.UShape;
import net.sourceforge.plantuml.ugraphic.UStroke;
import net.sourceforge.plantuml.ugraphic.UTranslate;

public class Line implements Moveable, Hideable {

	private final Cluster ltail;
	private final Cluster lhead;
	private final Link link;

	private final EntityPort startUid;
	private final EntityPort endUid;

	private final TextBlock startTailText;
	private final TextBlock endHeadText;
	private final TextBlock labelText;
	private boolean divideLabelWidthByTwo = false;

	private final int lineColor;
	private final int noteLabelColor;
	private final int startTailColor;
	private final int endHeadColor;

	private final StringBounder stringBounder;
	private final Bibliotekon bibliotekon;

	private DotPath dotPath;

	private Positionable startTailLabelXY;
	private Positionable endHeadLabelXY;
	private Positionable labelXY;

	private UDrawable extremity2;
	private UDrawable extremity1;

	private double dx;
	private double dy;

	private boolean opale;
	private Cluster projectionCluster;

	private final Pragma pragma;
	private final HtmlColor backgroundColor;

	@Override
	public String toString() {
		return super.toString() + " color=" + lineColor;
	}

	class DirectionalTextBlock extends AbstractTextBlock implements TextBlock {

		private final TextBlock right;
		private final TextBlock left;
		private final TextBlock up;
		private final TextBlock down;

		DirectionalTextBlock(TextBlock right, TextBlock left, TextBlock up, TextBlock down) {
			this.right = right;
			this.left = left;
			this.up = up;
			this.down = down;
		}

		public void drawU(UGraphic ug) {
			Direction dir = getDirection();
			if (getLinkArrow() == LinkArrow.BACKWARD) {
				dir = dir.getInv();
			}
			switch (dir) {
			case RIGHT:
				right.drawU(ug);
				break;
			case LEFT:
				left.drawU(ug);
				break;
			case UP:
				up.drawU(ug);
				break;
			case DOWN:
				down.drawU(ug);
				break;
			default:
				throw new UnsupportedOperationException();
			}
		}

		public Dimension2D calculateDimension(StringBounder stringBounder) {
			return right.calculateDimension(stringBounder);
		}

		private Direction getDirection() {
			if (isAutolink()) {
				final double startAngle = dotPath.getStartAngle();
				return Direction.LEFT;
			}
			final Point2D start = dotPath.getStartPoint();
			final Point2D end = dotPath.getEndPoint();
			final double ang = Math.atan2(end.getX() - start.getX(), end.getY() - start.getY());
			if (ang > -Math.PI / 4 && ang < Math.PI / 4) {
				return Direction.DOWN;
			}
			if (ang > Math.PI * 3 / 4 || ang < -Math.PI * 3 / 4) {
				return Direction.UP;
			}
			return end.getX() > start.getX() ? Direction.RIGHT : Direction.LEFT;
		}

	}

	private Cluster getCluster2(Bibliotekon bibliotekon, IEntity entityMutable) {
		for (Cluster cl : bibliotekon.allCluster()) {
			if (entityMutable == cl.getGroup()) {
				return cl;
			}
		}
		throw new IllegalArgumentException();
	}

	public Line(Link link, ColorSequence colorSequence, ISkinParam skinParam, StringBounder stringBounder,
			FontConfiguration labelFont, Bibliotekon bibliotekon, Pragma pragma) {

		if (link == null) {
			throw new IllegalArgumentException();
		}
		this.startUid = link.getEntityPort1(bibliotekon);
		this.endUid = link.getEntityPort2(bibliotekon);

		Cluster ltail = null;
		if (startUid.startsWith(Cluster.CENTER_ID)) {
			ltail = getCluster2(bibliotekon, link.getEntity1());
		}
		Cluster lhead = null;
		if (endUid.startsWith(Cluster.CENTER_ID)) {
			lhead = getCluster2(bibliotekon, link.getEntity2());
		}

		if (link.getColors() != null) {
			skinParam = link.getColors().mute(skinParam);
			labelFont = labelFont.mute(link.getColors());
		}
		this.backgroundColor = skinParam.getBackgroundColor();
		this.pragma = pragma;
		this.bibliotekon = bibliotekon;
		this.stringBounder = stringBounder;
		this.link = link;
		this.ltail = ltail;
		this.lhead = lhead;

		this.lineColor = colorSequence.getValue();
		this.noteLabelColor = colorSequence.getValue();
		this.startTailColor = colorSequence.getValue();
		this.endHeadColor = colorSequence.getValue();

		final TextBlock labelOnly;
		if (Display.isNull(link.getLabel())) {
			if (getLinkArrow() == LinkArrow.NONE) {
				labelOnly = null;
			} else {
				final TextBlockArrow right = new TextBlockArrow(Direction.RIGHT, labelFont);
				final TextBlockArrow left = new TextBlockArrow(Direction.LEFT, labelFont);
				final TextBlockArrow up = new TextBlockArrow(Direction.UP, labelFont);
				final TextBlockArrow down = new TextBlockArrow(Direction.DOWN, labelFont);
				labelOnly = new DirectionalTextBlock(right, left, up, down);
			}
		} else {
			final double marginLabel = startUid.equalsId(endUid) ? 6 : 1;
			final TextBlock label = TextBlockUtils.withMargin(
					link.getLabel().create(labelFont, skinParam.getDefaultTextAlignment(HorizontalAlignment.CENTER),
							skinParam, skinParam.maxMessageSize()), marginLabel, marginLabel);
			if (getLinkArrow() == LinkArrow.NONE) {
				labelOnly = label;
			} else {
				TextBlock right = new TextBlockArrow(Direction.RIGHT, labelFont);
				right = TextBlockUtils.mergeLR(label, right, VerticalAlignment.CENTER);
				TextBlock left = new TextBlockArrow(Direction.LEFT, labelFont);
				left = TextBlockUtils.mergeLR(left, label, VerticalAlignment.CENTER);
				TextBlock up = new TextBlockArrow(Direction.UP, labelFont);
				up = TextBlockUtils.mergeTB(up, label, HorizontalAlignment.CENTER);
				TextBlock down = new TextBlockArrow(Direction.DOWN, labelFont);
				down = TextBlockUtils.mergeTB(label, down, HorizontalAlignment.CENTER);
				labelOnly = new DirectionalTextBlock(right, left, up, down);
			}
		}

		final TextBlock noteOnly;
		if (link.getNote() == null) {
			noteOnly = null;
		} else {
			noteOnly = new EntityImageNoteLink(link.getNote(), link.getNoteColors(), skinParam);
			if (link.getNoteLinkStrategy() == NoteLinkStrategy.HALF_NOT_PRINTED
					|| link.getNoteLinkStrategy() == NoteLinkStrategy.HALF_PRINTED_FULL) {
				divideLabelWidthByTwo = true;
			}
		}

		if (labelOnly != null && noteOnly != null) {
			if (link.getNotePosition() == Position.LEFT) {
				labelText = TextBlockUtils.mergeLR(noteOnly, labelOnly, VerticalAlignment.CENTER);
			} else if (link.getNotePosition() == Position.RIGHT) {
				labelText = TextBlockUtils.mergeLR(labelOnly, noteOnly, VerticalAlignment.CENTER);
			} else if (link.getNotePosition() == Position.TOP) {
				labelText = TextBlockUtils.mergeTB(noteOnly, labelOnly, HorizontalAlignment.CENTER);
			} else {
				labelText = TextBlockUtils.mergeTB(labelOnly, noteOnly, HorizontalAlignment.CENTER);
			}
		} else if (labelOnly != null) {
			labelText = labelOnly;
		} else if (noteOnly != null) {
			labelText = noteOnly;
		} else {
			labelText = null;
		}

		if (link.getQualifier1() == null) {
			startTailText = null;
		} else {
			startTailText = Display.getWithNewlines(link.getQualifier1()).create(labelFont, HorizontalAlignment.CENTER,
					skinParam);
		}

		if (link.getQualifier2() == null) {
			endHeadText = null;
		} else {
			endHeadText = Display.getWithNewlines(link.getQualifier2()).create(labelFont, HorizontalAlignment.CENTER,
					skinParam);
		}

	}

	public boolean hasNoteLabelText() {
		return labelText != null;
	}

	private LinkArrow getLinkArrow() {
		return link.getLinkArrow();
	}

	public void appendLine(GraphvizVersion graphvizVersion, StringBuilder sb, DotMode dotMode) {
		// Log.println("inverted=" + isInverted());
		// if (isInverted()) {
		// sb.append(endUid);
		// sb.append("->");
		// sb.append(startUid);
		// } else {
		sb.append(startUid.getFullString());
		sb.append("->");
		sb.append(endUid.getFullString());
		// }
		sb.append("[");
		final LinkType linkType = link.getTypePatchCluster();
		String decoration = linkType.getSpecificDecorationSvek();
		if (decoration.endsWith(",") == false) {
			decoration += ",";
		}
		sb.append(decoration);

		int length = link.getLength();
		// if (graphvizVersion == GraphvizVersion.V2_34_0 && length == 1) {
		// length = 2;
		// }
		if (pragma.horizontalLineBetweenDifferentPackageAllowed() || link.isInvis() || length != 1) {
			// if (graphvizVersion.isJs() == false) {
			sb.append("minlen=" + (length - 1));
			sb.append(",");
			// }
		}
		sb.append("color=\"" + StringUtils.getAsHtml(lineColor) + "\"");
		if (labelText != null) {
			sb.append(",");
			if (graphvizVersion.modeSafe() || dotMode == DotMode.NO_LEFT_RIGHT_AND_XLABEL) {
				sb.append("xlabel=<");
			} else {
				sb.append("label=<");
			}
			appendTable(sb, eventuallyDivideByTwo(labelText.calculateDimension(stringBounder)), noteLabelColor,
					graphvizVersion);
			sb.append(">");
			// sb.append(",labelfloat=true");
		}

		if (startTailText != null) {
			sb.append(",");
			sb.append("taillabel=<");
			appendTable(sb, startTailText.calculateDimension(stringBounder), startTailColor, graphvizVersion);
			sb.append(">");
			// sb.append(",labelangle=0");
		}
		if (endHeadText != null) {
			sb.append(",");
			sb.append("headlabel=<");
			appendTable(sb, endHeadText.calculateDimension(stringBounder), endHeadColor, graphvizVersion);
			sb.append(">");
			// sb.append(",labelangle=0");
		}

		if (link.isInvis()) {
			sb.append(",");
			sb.append("style=invis");
		}

		if (link.isConstraint() == false || link.hasTwoEntryPointsSameContainer()) {
			sb.append(",constraint=false");
		}

		if (link.getSametail() != null) {
			sb.append(",sametail=" + link.getSametail());
		}

		sb.append("];");
		SvekUtils.println(sb);
	}

	private Dimension2D eventuallyDivideByTwo(Dimension2D dim) {
		if (divideLabelWidthByTwo) {
			return new Dimension2DDouble(dim.getWidth() / 2, dim.getHeight());
		}
		return dim;
	}

	public String rankSame() {
		// if (graphvizVersion == GraphvizVersion.V2_34_0) {
		// return null;
		// }
		if (pragma.horizontalLineBetweenDifferentPackageAllowed() == false && link.getLength() == 1
		/* && graphvizVersion.isJs() == false */) {
			return "{rank=same; " + getStartUidPrefix() + "; " + getEndUidPrefix() + "}";
		}
		return null;
	}

	public static void appendTable(StringBuilder sb, Dimension2D dim, int col, GraphvizVersion graphvizVersion) {
		final int w = (int) dim.getWidth();
		final int h = (int) dim.getHeight();
		appendTable(sb, w, h, col);
	}

	public static void appendTable(StringBuilder sb, int w, int h, int col) {
		sb.append("<TABLE ");
		sb.append("BGCOLOR=\"" + StringUtils.getAsHtml(col) + "\" ");
		sb.append("FIXEDSIZE=\"TRUE\" WIDTH=\"" + w + "\" HEIGHT=\"" + h + "\">");
		sb.append("<TR");
		sb.append(">");
		sb.append("<TD");
		// sb.append(" FIXEDSIZE=\"TRUE\" WIDTH=\"" + 0 + "\" HEIGHT=\"" + 0 +
		// "\"");
		sb.append(">");
		sb.append("</TD>");
		sb.append("</TR>");
		sb.append("</TABLE>");
	}

	public final String getStartUidPrefix() {
		return startUid.getPrefix();
	}

	public final String getEndUidPrefix() {
		return endUid.getPrefix();
	}

	private UDrawable getExtremity(LinkDecor decor, PointListIterator pointListIterator, Point2D center, double angle,
			Cluster cluster, Shape shapeContact) {
		final ExtremityFactory extremityFactory = decor.getExtremityFactory(backgroundColor);

		if (cluster != null) {
			if (extremityFactory != null) {
				// System.err.println("angle=" + angle * 180 / Math.PI);
				return extremityFactory.createUDrawable(center, angle, null);
			}
			if (decor == LinkDecor.EXTENDS) {
				return new ExtremityFactoryExtends(backgroundColor).createUDrawable(center, angle, null);
			}
			return null;
		}

		if (extremityFactory != null) {
			final List<Point2D.Double> points = pointListIterator.next();
			final Point2D p0 = points.get(0);
			final Point2D p1 = points.get(1);
			final Point2D p2 = points.get(2);
			Side side = null;
			if (shapeContact != null) {
				side = shapeContact.getClusterPosition().getClosestSide(p1);
			}
			return extremityFactory.createUDrawable(p0, p1, p2, side);
		} else if (decor != LinkDecor.NONE) {
			final UShape sh = new UPolygon(pointListIterator.next());
			return new UDrawable() {
				public void drawU(UGraphic ug) {
					ug.draw(sh);
				}
			};
		}
		return null;

	}

	public void solveLine(final String svg, final int fullHeight, MinFinder corner1) {
		if (this.link.isInvis()) {
			return;
		}

		int idx = getIndexFromColor(svg, this.lineColor);
		if (idx == -1) {
			return;
			// throw new IllegalStateException();
		}
		idx = svg.indexOf("d=\"", idx);
		if (idx == -1) {
			throw new IllegalStateException();
		}
		final int end = svg.indexOf("\"", idx + 3);
		final String path = svg.substring(idx + 3, end);

		if (DotPath.isPathConsistent(path) == false) {
			return;
		}
		dotPath = new DotPath(path, fullHeight);

		if (projectionCluster != null) {
			// System.err.println("Line::solveLine1 projectionCluster=" + projectionCluster.getClusterPosition());
			projectionCluster.manageEntryExitPoint(stringBounder);
			// System.err.println("Line::solveLine2 projectionCluster=" + projectionCluster.getClusterPosition());
			// if (lhead != null)
			// System.err.println("Line::solveLine ltail=" + lhead.getClusterPosition());
			// if (ltail != null)
			// System.err.println("Line::solveLine ltail=" + ltail.getClusterPosition());
		}
		dotPath = dotPath.simulateCompound(lhead, ltail);

		PointListIterator pointListIterator = new PointListIterator(svg.substring(end), fullHeight);

		final LinkType linkType = link.getType();
		this.extremity1 = getExtremity(linkType.getDecor2(), pointListIterator, dotPath.getStartPoint(),
				dotPath.getStartAngle() + Math.PI, ltail, bibliotekon.getShape(link.getEntity1()));
		this.extremity2 = getExtremity(linkType.getDecor1(), pointListIterator, dotPath.getEndPoint(),
				dotPath.getEndAngle(), lhead, bibliotekon.getShape(link.getEntity2()));
		if (extremity1 instanceof Extremity && extremity2 instanceof Extremity) {
			final Point2D p1 = ((Extremity) extremity1).somePoint();
			final Point2D p2 = ((Extremity) extremity2).somePoint();
			if (p1 != null && p2 != null) {
				// http://plantuml.sourceforge.net/qa/?qa=4240/some-relations-point-wrong-direction-when-the-linetype-ortho
				final double dist1start = p1.distance(dotPath.getStartPoint());
				final double dist1end = p1.distance(dotPath.getEndPoint());
				final double dist2start = p2.distance(dotPath.getStartPoint());
				final double dist2end = p2.distance(dotPath.getEndPoint());
				if (dist1start > dist1end && dist2end > dist2start) {
					pointListIterator = new PointListIterator(svg.substring(end), fullHeight);
					this.extremity2 = getExtremity(linkType.getDecor1(), pointListIterator, dotPath.getEndPoint(),
							dotPath.getEndAngle(), lhead, bibliotekon.getShape(link.getEntity2()));
					this.extremity1 = getExtremity(linkType.getDecor2(), pointListIterator, dotPath.getStartPoint(),
							dotPath.getStartAngle() + Math.PI, ltail, bibliotekon.getShape(link.getEntity1()));
				}
			}

		}

		if (this.labelText != null) {
			final Point2D pos = getXY(svg, this.noteLabelColor, fullHeight);
			if (pos != null) {
				corner1.manage(pos);
				this.labelXY = TextBlockUtils.asPositionable(labelText, stringBounder, pos);
			}
		}

		if (this.startTailText != null) {
			final Point2D pos = getXY(svg, this.startTailColor, fullHeight);
			if (pos != null) {
				corner1.manage(pos);
				this.startTailLabelXY = TextBlockUtils.asPositionable(startTailText, stringBounder, pos);
			}
		}

		if (this.endHeadText != null) {
			final Point2D pos = getXY(svg, this.endHeadColor, fullHeight);
			if (pos != null) {
				corner1.manage(pos);
				this.endHeadLabelXY = TextBlockUtils.asPositionable(endHeadText, stringBounder, pos);
				corner1.manage(pos.getX() - 15, pos.getY());
			}
		}

		if (isOpalisable() == false) {
			setOpale(false);
		}
	}

	private boolean isOpalisable() {
		return dotPath.getBeziers().size() <= 1;
	}

	private Point2D.Double getXY(String svg, int color, int height) {
		final int idx = getIndexFromColor(svg, color);
		if (idx == -1) {
			return null;
		}
		return SvekUtils.getMinXY(SvekUtils.extractPointsList(svg, idx, height));

	}

	private int getIndexFromColor(String svg, int color) {
		String s = "stroke=\"" + StringUtils.goLowerCase(StringUtils.getAsHtml(color)) + "\"";
		int idx = svg.indexOf(s);
		if (idx != -1) {
			return idx;
		}
		s = ";stroke:" + StringUtils.goLowerCase(StringUtils.getAsHtml(color)) + ";";
		idx = svg.indexOf(s);
		if (idx != -1) {
			return idx;
		}
		s = "fill=\"" + StringUtils.goLowerCase(StringUtils.getAsHtml(color)) + "\"";
		idx = svg.indexOf(s);
		if (idx != -1) {
			return idx;
		}
		Log.info("Cannot find color=" + color + " " + StringUtils.goLowerCase(StringUtils.getAsHtml(color)));
		return -1;

	}

	public void drawU(UGraphic ug, HtmlColor color) {
		if (opale) {
			return;
		}

		double x = 0;
		double y = 0;
		final Url url = link.getUrl();
		if (url != null) {
			ug.startUrl(url);
		}

		if (link.isAutoLinkOfAGroup()) {
			final Cluster cl = bibliotekon.getCluster((IGroup) link.getEntity1());
			if (cl != null) {
				x += cl.getWidth();
				x -= dotPath.getStartPoint().getX() - cl.getMinX();
			}
		}

		x += dx;
		y += dy;

		if (link.isInvis()) {
			return;
		}

		if (this.link.getColors() != null) {
			final HtmlColor newColor = this.link.getColors().getColor(ColorType.ARROW, ColorType.LINE);
			if (newColor != null) {
				color = newColor;
			}

		} else if (this.link.getSpecificColor() != null) {
			color = this.link.getSpecificColor();
		}

		ug = ug.apply(new UChangeBackColor(null)).apply(new UChangeColor(color));
		final LinkType linkType = link.getType();
		UStroke stroke = linkType.getStroke();
		if (link.getColors() != null && link.getColors().getSpecificLineStroke() != null) {
			stroke = link.getColors().getSpecificLineStroke();
		}
		ug = ug.apply(stroke);
		double moveEndY = 0;

		if (dotPath == null) {
			Log.info("DotPath is null for " + this);
			return;
		}
		DotPath todraw = dotPath;
		if (link.getEntity2().isGroup() && link.getEntity2().getUSymbol() instanceof USymbolFolder) {
			final Cluster endCluster = bibliotekon.getCluster((IGroup) link.getEntity2());
			if (endCluster != null) {
				final double deltaFolderH = endCluster
						.checkFolderPosition(dotPath.getEndPoint(), ug.getStringBounder());
				todraw = new DotPath(dotPath);
				todraw.moveEndPoint(0, deltaFolderH);
				moveEndY = deltaFolderH;
			}
		}

		ug.apply(new UTranslate(x, y)).draw(todraw);

		ug = ug.apply(new UStroke()).apply(new UChangeColor(color));

		if (this.extremity2 != null) {
			UGraphic ug2 = ug;
			if (linkType.getDecor1().isFill()) {
				ug2 = ug2.apply(new UChangeBackColor(color));
			} else {
				ug2 = ug2.apply(new UChangeBackColor(null));
			}
			// System.err.println("Line::draw EXTREMITY1");
			this.extremity2.drawU(ug2.apply(new UTranslate(x, y)));
		}
		if (this.extremity1 != null) {
			UGraphic ug2 = ug;
			if (linkType.getDecor2().isFill()) {
				ug2 = ug2.apply(new UChangeBackColor(color));
			} else {
				ug2 = ug2.apply(new UChangeBackColor(null));
			}
			// System.err.println("Line::draw EXTREMITY2");
			this.extremity1.drawU(ug2.apply(new UTranslate(x, y)));
		}
		if (this.labelText != null && this.labelXY != null
				&& link.getNoteLinkStrategy() != NoteLinkStrategy.HALF_NOT_PRINTED) {
			this.labelText.drawU(ug.apply(new UTranslate(x + this.labelXY.getPosition().getX(), y
					+ this.labelXY.getPosition().getY())));
		}
		if (this.startTailText != null && this.startTailLabelXY != null && this.startTailLabelXY.getPosition() != null) {
			this.startTailText.drawU(ug.apply(new UTranslate(x + this.startTailLabelXY.getPosition().getX(), y
					+ this.startTailLabelXY.getPosition().getY())));
		}
		if (this.endHeadText != null && this.endHeadLabelXY != null && this.endHeadLabelXY.getPosition() != null) {
			this.endHeadText.drawU(ug.apply(new UTranslate(x + this.endHeadLabelXY.getPosition().getX(), y
					+ this.endHeadLabelXY.getPosition().getY())));
		}

		if (linkType.getMiddleDecor() != LinkMiddleDecor.NONE) {
			final PointAndAngle middle = dotPath.getMiddle();
			final double angleRad = middle.getAngle();
			final double angleDeg = -angleRad * 180.0 / Math.PI;
			final UDrawable mi = linkType.getMiddleDecor().getMiddleFactory().createUDrawable(angleDeg - 45);
			mi.drawU(ug.apply(new UTranslate(x + middle.getX(), y + middle.getY())));
		}

		if (url != null) {
			ug.closeAction();
		}
	}

	public boolean isInverted() {
		return link.isInverted();
	}

	private double getDecorDzeta() {
		final LinkType linkType = link.getType();
		final int size1 = linkType.getDecor1().getMargin();
		final int size2 = linkType.getDecor2().getMargin();
		return size1 + size2;
	}

	public double getHorizontalDzeta(StringBounder stringBounder) {
		if (startUid.equalsId(endUid)) {
			return getDecorDzeta();
		}
		final ArithmeticStrategy strategy;
		if (isHorizontal()) {
			strategy = new ArithmeticStrategySum();
		} else {
			return 0;
		}
		if (labelText != null) {
			strategy.eat(labelText.calculateDimension(stringBounder).getWidth());
		}
		if (startTailText != null) {
			strategy.eat(startTailText.calculateDimension(stringBounder).getWidth());
		}
		if (endHeadText != null) {
			strategy.eat(endHeadText.calculateDimension(stringBounder).getWidth());
		}
		return strategy.getResult() + getDecorDzeta();
	}

	private boolean isHorizontal() {
		return link.getLength() == 1;
	}

	public double getVerticalDzeta(StringBounder stringBounder) {
		if (startUid.equalsId(endUid)) {
			return getDecorDzeta();
		}
		if (isHorizontal()) {
			return 0;
		}
		final ArithmeticStrategy strategy = new ArithmeticStrategySum();
		if (labelText != null) {
			strategy.eat(labelText.calculateDimension(stringBounder).getHeight());
		}
		if (startTailText != null) {
			strategy.eat(startTailText.calculateDimension(stringBounder).getHeight());
		}
		if (endHeadText != null) {
			strategy.eat(endHeadText.calculateDimension(stringBounder).getHeight());
		}
		return strategy.getResult() + getDecorDzeta();
	}

	public void manageCollision(Collection<Shape> allShapes) {

		for (Shape sh : allShapes) {
			final Positionable cl = PositionableUtils.addMargin(sh, 8, 8);
			if (startTailText != null && startTailLabelXY != null && PositionableUtils.intersect(cl, startTailLabelXY)) {
				startTailLabelXY = PositionableUtils.moveAwayFrom(cl, startTailLabelXY);
			}
			if (endHeadText != null && endHeadLabelXY != null && PositionableUtils.intersect(cl, endHeadLabelXY)) {
				endHeadLabelXY = PositionableUtils.moveAwayFrom(cl, endHeadLabelXY);
			}
		}

		// final Positionable start = getStartTailPositionnable();
		// if (start != null) {
		// for (Shape sh : allShapes) {
		// if (cut(start, sh)) {
		// avoid(startTailLabelXY, start, sh);
		// }
		// }
		// }
		//
		// final Positionable end = getEndHeadPositionnable();
		// if (end != null) {
		// for (Shape sh : allShapes) {
		// if (cut(end, sh)) {
		// avoid(endHeadLabelXY, end, sh);
		// }
		// }
		// }

	}

	private void avoid(Point2D.Double move, Positionable pos, Shape sh) {
		final Oscillator oscillator = new Oscillator();
		final Point2D.Double orig = new Point2D.Double(move.x, move.y);
		while (cut(pos, sh)) {
			final Point2D.Double m = oscillator.nextPosition();
			move.setLocation(orig.x + m.x, orig.y + m.y);
		}
	}

	private boolean cut(Positionable pos, Shape sh) {
		return BezierUtils.intersect(pos, sh) || tooClose(pos);
	}

	private boolean tooClose(Positionable pos) {
		final double dist = dotPath.getMinDist(BezierUtils.getCenter(pos));
		final Dimension2D dim = pos.getSize();
		// Log.println("dist=" + dist);
		return dist < (dim.getWidth() / 2 + 2) || dist < (dim.getHeight() / 2 + 2);
	}

	public void moveSvek(double deltaX, double deltaY) {
		this.dx += deltaX;
		this.dy += deltaY;
	}

	public final DotPath getDotPath() {
		final DotPath result = new DotPath(dotPath);
		result.moveSvek(dx, dy);
		return result;
	}

	public int getLength() {
		return link.getLength();
	}

	public void setOpale(boolean opale) {
		this.link.setOpale(opale);
		this.opale = opale;

	}

	public boolean isOpale() {
		return opale;
	}

	public boolean isHorizontalSolitary() {
		return link.isHorizontalSolitary();
	}

	public boolean isLinkFromOrTo(IEntity group) {
		return link.getEntity1() == group || link.getEntity2() == group;
	}

	public boolean hasEntryPoint() {
		return link.hasEntryPoint();
	}

	public void setProjectionCluster(Cluster cluster) {
		this.projectionCluster = cluster;

	}

	public boolean isHidden() {
		return link.isHidden();
	}

	public boolean sameConnections(Line other) {
		return link.sameConnections(other.link);
	}

	private boolean isAutolink() {
		return link.getEntity1() == link.getEntity2();
	}

	public Point2D getMyPoint(IEntity entity) {
		if (link.getEntity1() == entity) {
			return moveDelta(dotPath.getStartPoint());
		}
		if (link.getEntity2() == entity) {
			return moveDelta(dotPath.getEndPoint());
		}
		throw new IllegalArgumentException();
	}

	private Point2D moveDelta(Point2D pt) {
		return new Point2D.Double(pt.getX() + dx, pt.getY() + dy);
	}

	public boolean isLink(Link link) {
		return this.link == link;
	}

	public Point2D getStartContactPoint() {
		final Point2D start = dotPath.getStartPoint();
		if (start == null) {
			return null;
		}
		return new Point2D.Double(dx + start.getX(), dy + start.getY());
	}

	public Point2D getEndContactPoint() {
		final Point2D end = dotPath.getEndPoint();
		if (end == null) {
			return null;
		}
		return new Point2D.Double(dx + end.getX(), dy + end.getY());
	}

	public IEntity getOther(IEntity entity) {
		if (link.contains(entity)) {
			return link.getOther(entity);
		}
		return null;
	}

}
