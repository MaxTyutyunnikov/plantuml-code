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
package net.sourceforge.plantuml;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import net.sourceforge.plantuml.braille.BrailleCharFactory;
import net.sourceforge.plantuml.braille.UGraphicBraille;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.ugraphic.UFont;

/**
 * Format for output files generated by PlantUML.
 * 
 * @author Arnaud Roques
 * 
 */
public enum FileFormat {
	PNG, SVG, EPS, EPS_TEXT, ATXT, UTXT, XMI_STANDARD, XMI_STAR, XMI_ARGO, SCXML, PDF, MJPEG, ANIMATED_GIF, HTML, HTML5, VDX, LATEX, LATEX_NO_PREAMBLE, BASE64, BRAILLE_PNG;

	/**
	 * Returns the file format to be used for that format.
	 * 
	 * @return a string starting by a point.
	 */
	public String getFileSuffix() {
		if (name().startsWith("XMI")) {
			return ".xmi";
		}
		if (this == MJPEG) {
			return ".avi";
		}
		if (this == LATEX_NO_PREAMBLE) {
			return ".latex";
		}
		if (this == ANIMATED_GIF) {
			return ".gif";
		}
		if (this == BRAILLE_PNG) {
			return ".braille.png";
		}
		if (this == EPS_TEXT) {
			return EPS.getFileSuffix();
		}
		return "." + StringUtils.goLowerCase(name());
	}

	final static BufferedImage imDummy = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
	final static Graphics2D gg = imDummy.createGraphics();

	public StringBounder getDefaultStringBounder() {
		if (this == BRAILLE_PNG) {
			return new StringBounder() {
				public Dimension2D calculateDimension(UFont font, String text) {
					final int nb = BrailleCharFactory.build(text).size();
					final double quanta = UGraphicBraille.QUANTA;
					final double height = 5 * quanta;
					final double width = 3 * nb * quanta + 1;
					return new Dimension2DDouble(width, height);
				}
			};

		}
		return new StringBounder() {
			public Dimension2D calculateDimension(UFont font, String text) {
				// Log.info("FileFormat::calculateDimension text="+text);
				final FontMetrics fm = gg.getFontMetrics(font.getFont());
				// Log.info("FileFormat::calculateDimension fm="+fm);
				final Rectangle2D rect = fm.getStringBounds(text, gg);
				// Log.info("FileFormat::calculateDimension rect="+rect);
				return new Dimension2DDouble(rect.getWidth(), rect.getHeight());
			}
		};
	}

	/**
	 * Check if this file format is Encapsulated PostScript.
	 * 
	 * @return <code>true</code> for EPS.
	 */
	public boolean isEps() {
		if (this == EPS) {
			return true;
		}
		if (this == EPS_TEXT) {
			return true;
		}
		return false;
	}

	public String changeName(String fileName, int cpt) {
		if (cpt == 0) {
			return changeName(fileName, getFileSuffix());
		}
		return changeName(fileName, "_" + String.format("%03d", cpt) + getFileSuffix());
	}

	private String changeName(String fileName, String replacement) {
		String result = fileName.replaceAll("\\.\\w+$", replacement);
		if (result.equals(fileName)) {
			result = fileName + replacement;
		}
		return result;
	}

	public File computeFilename(File pngFile, int i) {
		if (i == 0) {
			return pngFile;
		}
		final File dir = pngFile.getParentFile();
		return new File(dir, computeFilename(pngFile.getName(), i));
		// String name = pngFile.getName();
		// name = name.replaceAll("\\" + getFileSuffix() + "$", "_" + String.format("%03d", i) + getFileSuffix());
		// return new File(dir, name);

	}

	public String computeFilename(String name, int i) {
		if (i == 0) {
			return name;
		}
		return name.replaceAll("\\" + getFileSuffix() + "$", "_" + String.format("%03d", i) + getFileSuffix());
	}

}
