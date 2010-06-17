/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009, Arnaud Roques
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
 * Original Author:  Arnaud Roques
 *
 * Revision $Revision: 4771 $
 *
 */
package net.sourceforge.plantuml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.plantuml.code.Transcoder;
import net.sourceforge.plantuml.preproc.Defines;

public class SourceFileReader extends AbstractSourceReader {

	private final File file;
	private final File outputDirectory;
	private final List<String> config;
	private final String charset;
	private FileFormat fileFormat;

	public SourceFileReader(File file) throws IOException {
		this(file, file.getAbsoluteFile().getParentFile());
	}

	public SourceFileReader(final File file, File outputDirectory) throws IOException {
		this(new Defines(), file, outputDirectory, Collections.<String> emptyList(), null, FileFormat.PNG);
	}

	public SourceFileReader(final File file, File outputDirectory, FileFormat fileFormat) throws IOException {
		this(new Defines(), file, outputDirectory, Collections.<String> emptyList(), null, fileFormat);
	}

	public SourceFileReader(Defines defines, final File file, File outputDirectory, List<String> config,
			String charset, FileFormat fileFormat) throws IOException {
		super(defines);
		this.charset = charset;
		this.config = config;
		if (file.exists() == false) {
			throw new IllegalArgumentException();
		}
		FileSystem.getInstance().setCurrentDir(file.getAbsoluteFile().getParentFile());
		if (outputDirectory == null) {
			outputDirectory = file.getAbsoluteFile().getParentFile();
		} else if (outputDirectory.isAbsolute() == false) {
			outputDirectory = FileSystem.getInstance().getFile(outputDirectory.getName());
		}
		if (outputDirectory.exists() == false) {
			outputDirectory.mkdirs();
		}
		this.file = file;
		this.outputDirectory = outputDirectory;
		this.fileFormat = fileFormat;
	}

	public List<GeneratedImage> getGeneratedImages() throws IOException, InterruptedException {
		Log.info("Reading file: " + file);

		int cpt = 0;
		final List<GeneratedImage> result = new ArrayList<GeneratedImage>();

		for (StartUml startUml : getAllStartUml(config)) {
			String newName = startUml.getFilename();

			if (newName == null) {
				newName = changeName(file.getName(), cpt++, fileFormat);
			}

			final File suggested = new File(outputDirectory, newName);
			suggested.getParentFile().mkdirs();

			for (File f : startUml.getSystem().createFiles(suggested, fileFormat)) {
				final String desc = "[" + file.getName() + "] " + startUml.getSystem().getDescription();
				final GeneratedImage generatedImage = new GeneratedImage(f, desc);
				result.add(generatedImage);
			}

		}

		Log.info("Number of image(s): " + result.size());

		return Collections.unmodifiableList(result);
	}

	public List<String> getEncodedUrl() throws IOException, InterruptedException {
		final List<String> result = new ArrayList<String>();
		final Transcoder transcoder = new Transcoder();
		for (StartUml startUml : getAllStartUml(config)) {
			final String source = startUml.getSystem().getSource().getPlainString();
			final String encoded = transcoder.encode(source);
			result.add(encoded);
		}
		return Collections.unmodifiableList(result);
	}

	static String changeName(String name, int cpt, FileFormat fileFormat) {
		if (cpt == 0) {
			return name.replaceAll("\\.\\w+$", fileFormat.getFileSuffix());
		}
		return name.replaceAll("\\.\\w+$", "_" + String.format("%03d", cpt) + fileFormat.getFileSuffix());
	}

	@Override
	protected Reader getReader() throws FileNotFoundException, UnsupportedEncodingException {
		if (charset == null) {
			Log.info("Using default charset");
			return new InputStreamReader(new FileInputStream(file));
		}
		Log.info("Using charset " + charset);
		return new InputStreamReader(new FileInputStream(file), charset);
	}

	public final void setFileFormat(FileFormat fileFormat) {
		this.fileFormat = fileFormat;
	}

}
