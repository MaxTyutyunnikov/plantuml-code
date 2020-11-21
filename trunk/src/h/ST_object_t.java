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

import smetana.core.CStar;
import smetana.core.UnsupportedStructAndPtr;
import smetana.core.__ptr__;
import smetana.core.__struct__;
import smetana.core.amiga.StarStruct;

public class ST_object_t extends UnsupportedStructAndPtr implements ST_Node_t___or_object_t {

	private final StarStruct parent;

	public ST_object_t() {
		this(null);
	}

	public ST_object_t(StarStruct parent) {
		this.parent = parent;
	}


	public final ST_pointf pos = new ST_pointf(this);
	public final ST_pointf sz = new ST_pointf(this);
	public CStar<ST_xlabel_t> lbl;

	@Override
	public ST_object_t getStruct() {
		return this;
	}

	@Override
	public ST_object_t getPtr() {
		return this;
	}

	@Override
	public boolean isSameThan(StarStruct other) {
		ST_object_t other2 = (ST_object_t) other;
		return this == other2;
	}

	@Override
	public __ptr__ setPtr(String fieldName, __ptr__ newData) {
		if (fieldName.equals("pos")) {
			this.pos.copyDataFrom(newData);
			return newData;
		}
		if (fieldName.equals("lbl")) {
			this.lbl = (CStar<ST_xlabel_t>) newData;
			return this.lbl;
		}
		return super.setPtr(fieldName, newData);
	}

	@Override
	public void setStruct(String fieldName, __struct__ newData) {
		if (fieldName.equals("pos")) {
			this.pos.copyDataFrom(newData);
			return;
		}
		if (fieldName.equals("sz")) {
			this.sz.copyDataFrom(newData);
			return;
		}
		super.setStruct(fieldName, newData);
	}

}

// typedef struct {
// pointf pos; /* Position of lower-left corner of object */
// pointf sz; /* Size of object; may be zero for a point */
// xlabel_t *lbl; /* Label attached to object, or NULL */
// } object_t;