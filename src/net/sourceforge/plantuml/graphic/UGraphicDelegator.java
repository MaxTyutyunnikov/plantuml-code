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
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * Original Author:  Arnaud Roques
 * 
 * Revision $Revision: 10266 $
 *
 */
package net.sourceforge.plantuml.graphic;

import net.sourceforge.plantuml.Url;
import net.sourceforge.plantuml.ugraphic.ColorMapper;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UParam;
import net.sourceforge.plantuml.ugraphic.UShape;

public abstract class UGraphicDelegator implements UGraphic {

	final private UGraphic ug;
	
	public final boolean isSpecialTxt() {
		return ug.isSpecialTxt();
	}


	public UGraphicDelegator(UGraphic ug) {
		this.ug = ug;
	}

	public StringBounder getStringBounder() {
		return ug.getStringBounder();
	}

	public UParam getParam() {
		return ug.getParam();
	}

	public void draw(UShape shape) {
		ug.draw(shape);
	}

	public ColorMapper getColorMapper() {
		return ug.getColorMapper();
	}

	public void startUrl(Url url) {
		ug.startUrl(url);
	}

	public void closeAction() {
		ug.closeAction();
	}

	protected UGraphic getUg() {
		return ug;
	}
	
	public void flushUg() {
		ug.flushUg();
	}

}