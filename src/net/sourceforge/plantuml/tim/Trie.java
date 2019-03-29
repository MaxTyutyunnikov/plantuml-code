/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2020, Arnaud Roques
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
 */
package net.sourceforge.plantuml.tim;

import java.util.HashMap;
import java.util.Map;

public class Trie {

	private final Map<Character, Trie> brothers = new HashMap<Character, Trie>();

	private boolean terminalWord = false;

	public void add(String s) {
		add(this, s);
	}

	private static void add(Trie current, String s) {
		if (s.length() == 0) {
			throw new UnsupportedOperationException();
		}

		while (s.length() > 0) {
			final Character added = s.charAt(0);
			final Trie child = current.getOrCreate(added);
			s = s.substring(1);
			if (s.length() == 0) {
				child.terminalWord = true;
			}
			current = child;
		}
	}

	private Trie getOrCreate(Character added) {
		Trie result = brothers.get(added);
		if (result == null) {
			result = new Trie();
			brothers.put(added, result);
		}
		return result;
	}

	public String getLonguestMatchStartingIn(String s) {
		return getLonguestMatchStartingIn(this, s);
	}

	private static String getLonguestMatchStartingIn(Trie current, String s) {
		final StringBuilder result = new StringBuilder();
		while (current != null) {
			if (s.length() == 0) {
				if (current.terminalWord) {
					return result.toString();
				} else {
					return "";
				}
			}
			final Trie child = current.brothers.get(s.charAt(0));
			if (child == null) {
				if (current.terminalWord) {
					return result.toString();
				} else {
					return "";
				}
			}
			result.append(s.charAt(0));
			current = child;
			s = s.substring(1);
		}
		return "";

	}

}
