/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * Project Info:  http://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 * 
 * This file is part of Smetana.
 * Smetana is a partial translation of Graphviz/Dot sources from C to Java.
 *
 * (C) Copyright 2009-2022, Arnaud Roques
 *
 * This translation is distributed under the same Licence as the original C program:
 * 
 *************************************************************************
 * Copyright (c) 2011 AT&T Intellectual Property 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: See CVS logs. Details at http://www.graphviz.org/
 *************************************************************************
 *
 * THE ACCOMPANYING PROGRAM IS PROVIDED UNDER THE TERMS OF THIS ECLIPSE PUBLIC
 * LICENSE ("AGREEMENT"). [Eclipse Public License - v 1.0]
 * 
 * ANY USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM CONSTITUTES
 * RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT.
 * 
 * You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package h;

import smetana.core.CString;
import smetana.core.UnsupportedStructAndPtr;
import smetana.core.__ptr__;
import smetana.core.amiga.StarStruct;

// UNUSED ?
public class ST_textspan_t extends UnsupportedStructAndPtr {

	private final StarStruct parent;

	public ST_textspan_t() {
		this(null);
	}

	public ST_textspan_t(StarStruct parent) {
		this.parent = parent;
	}

	public CString str;

	// "textfont_t *font",
	// "void *layout",
	// "void (*free_layout) (void *layout)",
	// "double yoffset_layout, yoffset_centerline",
	public final ST_pointf size = new ST_pointf(this);
	public int just;



	@Override
	public void setInt(String fieldName, int data) {
		if (fieldName.equals("just")) {
			this.just = data;
			return;
		}
		super.setInt(fieldName, data);
	}

	@Override
	public __ptr__ setPtr(String fieldName, __ptr__ newData) {
		if (fieldName.equals("str")) {
			this.str = (CString) newData;
			return str;
		}
		return super.setPtr(fieldName, newData);
	}

}

// typedef struct {
// char *str; /* stored in utf-8 */
// textfont_t *font;
// void *layout;
// void (*free_layout) (void *layout); /* FIXME - this is ugly */
// double yoffset_layout, yoffset_centerline;
// pointf size;
// char just; /* 'l' 'n' 'r' */ /* FIXME */
// } textspan_t;