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
 * Revision $Revision: 9611 $
 *
 */
package net.sourceforge.plantuml;

import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.flashcode.FlashCodeFactory;
import net.sourceforge.plantuml.flashcode.FlashCodeUtils;
import net.sourceforge.plantuml.graphic.GraphicStrings;
import net.sourceforge.plantuml.graphic.HorizontalAlignement;
import net.sourceforge.plantuml.graphic.HtmlColorUtils;
import net.sourceforge.plantuml.mjpeg.MJPEGGenerator;
import net.sourceforge.plantuml.pdf.PdfConverter;
import net.sourceforge.plantuml.ugraphic.Sprite;
import net.sourceforge.plantuml.ugraphic.UFont;
import net.sourceforge.plantuml.version.Version;

public abstract class UmlDiagram extends AbstractPSystem implements PSystem {

	private boolean rotation;
	private boolean hideUnlinkedData;

	private int minwidth = Integer.MAX_VALUE;

	private Display title;
	private Display header;
	private Display footer;
	private HorizontalAlignement headerAlignement = HorizontalAlignement.RIGHT;
	private HorizontalAlignement footerAlignement = HorizontalAlignement.CENTER;
	private final Pragma pragma = new Pragma();
	private Scale scale;

	private final SkinParam skinParam = new SkinParam(getUmlDiagramType());

	final public void setTitle(Display strings) {
		this.title = strings;
	}

	final public Display getTitle() {
		return title;
	}

	final public int getMinwidth() {
		return minwidth;
	}

	final public void setMinwidth(int minwidth) {
		this.minwidth = minwidth;
	}

	final public boolean isRotation() {
		return rotation;
	}

	final public void setRotation(boolean rotation) {
		this.rotation = rotation;
	}

	public final ISkinParam getSkinParam() {
		return skinParam;
	}

	public void setParam(String key, String value) {
		skinParam.setParam(key.toLowerCase(), value);
	}

	public final Display getHeader() {
		return header;
	}

	public final void setHeader(Display header) {
		this.header = header;
	}

	public final Display getFooter() {
		return footer;
	}

	public final void setFooter(Display footer) {
		this.footer = footer;
	}

	public final HorizontalAlignement getHeaderAlignement() {
		return headerAlignement;
	}

	public final void setHeaderAlignement(HorizontalAlignement headerAlignement) {
		this.headerAlignement = headerAlignement;
	}

	public final HorizontalAlignement getFooterAlignement() {
		return footerAlignement;
	}

	public final void setFooterAlignement(HorizontalAlignement footerAlignement) {
		this.footerAlignement = footerAlignement;
	}

	abstract public UmlDiagramType getUmlDiagramType();

	public Pragma getPragma() {
		return pragma;
	}

	final public void setScale(Scale scale) {
		this.scale = scale;
	}

	final public Scale getScale() {
		return scale;
	}

	public final double getDpiFactor(FileFormatOption fileFormatOption) {
		if (getSkinParam().getDpi() == 96) {
			return 1.0;
		}
		return getSkinParam().getDpi() / 96.0;
	}

	public final int getDpi(FileFormatOption fileFormatOption) {
		return getSkinParam().getDpi();
	}

	public final boolean isHideUnlinkedData() {
		return hideUnlinkedData;
	}

	public final void setHideUnlinkedData(boolean hideUnlinkedData) {
		this.hideUnlinkedData = hideUnlinkedData;
	}

	final public void exportDiagram(OutputStream os, CMapData cmap, int index, FileFormatOption fileFormatOption)
			throws IOException {
		List<BufferedImage> flashcodes = null;
		try {
			if ("split".equalsIgnoreCase(getSkinParam().getValue("flashcode"))
					&& fileFormatOption.getFileFormat() == FileFormat.PNG) {
				final String s = getSource().getPlainString();
				flashcodes = getFlashCodeUtils().exportSplitCompress(s);
			} else if ("compress".equalsIgnoreCase(getSkinParam().getValue("flashcode"))
					&& fileFormatOption.getFileFormat() == FileFormat.PNG) {
				final String s = getSource().getPlainString();
				flashcodes = getFlashCodeUtils().exportFlashcodeCompress(s);
			} else if (getSkinParam().getValue("flashcode") != null
					&& fileFormatOption.getFileFormat() == FileFormat.PNG) {
				final String s = getSource().getPlainString();
				flashcodes = getFlashCodeUtils().exportFlashcodeSimple(s);
			}
		} catch (IOException e) {
			Log.error("Cannot generate flashcode");
			e.printStackTrace();
			flashcodes = null;
		}
		if (fileFormatOption.getFileFormat() == FileFormat.PDF) {
			exportDiagramInternalPdf(os, cmap, index, flashcodes);
			return;
		}
		if (fileFormatOption.getFileFormat() == FileFormat.MJPEG) {
			// exportDiagramInternalMjpeg(os);
			// return;*
			throw new UnsupportedOperationException();
		}
		try {
			lastInfo = exportDiagramInternal(os, cmap, index, fileFormatOption, flashcodes);
		} catch (Exception e) {
			e.printStackTrace();
			exportDiagramError(os, e, fileFormatOption);
		}
	}

	private void exportDiagramError(OutputStream os, Exception exception, FileFormatOption fileFormat)
			throws IOException {
		final UFont font = new UFont("SansSerif", Font.PLAIN, 12);
		final List<String> strings = new ArrayList<String>();
		strings.add("An error has occured : " + exception);
		strings.add(" ");
		strings.add("PlantUML (" + Version.versionString() + ") cannot parse result from dot/GraphViz.");
		strings.add(" ");
		strings.add("This may be caused by :");
		strings.add(" - a bug in PlantUML");
		strings.add(" - a problem in GraphViz");
		strings.add(" ");
		strings.add("You should send this diagram and this image to <b>plantuml@gmail.com</b> to solve this issue.");
		strings.add("You can try to turn arround this issue by simplifing your diagram.");
		strings.add(" ");
		strings.add(exception.toString());
		for (StackTraceElement ste : exception.getStackTrace()) {
			strings.add("  " + ste.toString());

		}
		final GraphicStrings graphicStrings = new GraphicStrings(strings, font, HtmlColorUtils.BLACK,
				HtmlColorUtils.WHITE, false);
		graphicStrings.writeImage(os, fileFormat);
	}

	private FlashCodeUtils getFlashCodeUtils() {
		return FlashCodeFactory.getFlashCodeUtils();
	}

	private void exportDiagramInternalMjpeg(OutputStream os) throws IOException {
		final File f = new File("c:/test.avi");
		final int nb = 150;
		final double framerate = 30;
		final MJPEGGenerator m = new MJPEGGenerator(f, 640, 480, framerate, nb);

		for (int i = 0; i < nb; i++) {
			final AffineTransform at = new AffineTransform();
			final double coef = (nb - 1 - i) * 1.0 / nb;
			at.setToShear(coef, coef);
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			exportDiagram(baos, null, 0, new FileFormatOption(FileFormat.PNG, at));
			baos.close();
			final BufferedImage im = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
			m.addImage(im);
		}
		m.finishAVI();

	}

	private UmlDiagramInfo lastInfo;

	private void exportDiagramInternalPdf(OutputStream os, CMapData cmap, int index, List<BufferedImage> flashcodes)
			throws IOException {
		final File svg = FileUtils.createTempFile("pdf", ".svf");
		final File pdfFile = FileUtils.createTempFile("pdf", ".pdf");
		final OutputStream fos = new BufferedOutputStream(new FileOutputStream(svg));
		exportDiagram(fos, cmap, index, new FileFormatOption(FileFormat.SVG));
		fos.close();
		PdfConverter.convert(svg, pdfFile);
		FileUtils.copyToStream(pdfFile, os);
	}

	protected abstract UmlDiagramInfo exportDiagramInternal(OutputStream os, CMapData cmap, int index,
			FileFormatOption fileFormatOption, List<BufferedImage> flashcodes) throws IOException;

	final protected void exportCmap(File suggestedFile, final CMapData cmapdata) throws FileNotFoundException {
		final String name = changeName(suggestedFile.getAbsolutePath());
		final File cmapFile = new File(name);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(cmapFile);
			pw.print(cmapdata.asString(cmapFile.getName().substring(0, cmapFile.getName().length() - 6)));
			pw.close();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	static String changeName(String name) {
		return name.replaceAll("(?i)\\.\\w{3}$", ".cmapx");
	}

	@Override
	public String getWarningOrError() {
		if (lastInfo == null) {
			return null;
		}
		final double actualWidth = lastInfo.getWidth();
		if (actualWidth == 0) {
			return null;
		}
		final String value = getSkinParam().getValue("widthwarning");
		if (value == null) {
			return null;
		}
		if (value.matches("\\d+") == false) {
			return null;
		}
		final int widthwarning = Integer.parseInt(value);
		if (actualWidth > widthwarning) {
			return "The image is " + ((int) actualWidth) + " pixel width. (Warning limit is " + widthwarning + ")";
		}
		return null;
	}

	// public final Map<String, Sprite> getSprites() {
	// return Collections.unmodifiableMap(sprites);
	// }
	// private final Map<String, Sprite> sprites = new HashMap<String, Sprite>();

	public void addSprite(String name, Sprite sprite) {
		skinParam.addSprite(name, sprite);
	}
}
