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
 * Revision $Revision: 4749 $
 *
 */
package net.sourceforge.plantuml.cucadiagram.dot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.plantuml.ISkinParam;
import net.sourceforge.plantuml.UmlDiagramType;
import net.sourceforge.plantuml.cucadiagram.Entity;
import net.sourceforge.plantuml.cucadiagram.EntityType;
import net.sourceforge.plantuml.cucadiagram.Group;
import net.sourceforge.plantuml.cucadiagram.GroupHierarchy;
import net.sourceforge.plantuml.cucadiagram.Link;
import net.sourceforge.plantuml.cucadiagram.Rankdir;
import net.sourceforge.plantuml.skin.VisibilityModifier;

final public class DotData {

	final private List<Link> links;
	final private Map<String, Entity> entities;
	final private UmlDiagramType umlDiagramType;
	final private ISkinParam skinParam;
	final private Rankdir rankdir;
	final private GroupHierarchy groupHierarchy;
	final private Group topParent;

	final private Map<EntityType, DrawFile> staticImages = new HashMap<EntityType, DrawFile>();
	final private Map<VisibilityModifier, DrawFile> visibilityImages = new EnumMap<VisibilityModifier, DrawFile>(
			VisibilityModifier.class);

	public DotData(Group topParent, List<Link> links, Map<String, Entity> entities, UmlDiagramType umlDiagramType,
			ISkinParam skinParam, Rankdir rankdir, GroupHierarchy groupHierarchy) {
		this.topParent = topParent;
		this.links = links;
		this.entities = entities;
		this.umlDiagramType = umlDiagramType;
		this.skinParam = skinParam;
		this.rankdir = rankdir;
		this.groupHierarchy = groupHierarchy;
	}

	public boolean hasUrl() {
		return true;
	}

	public Map<EntityType, DrawFile> getStaticImages() {
		return staticImages;
	}

	public void putAllStaticImages(Map<EntityType, DrawFile> staticImages) {
		this.staticImages.putAll(staticImages);
	}

	public Map<VisibilityModifier, DrawFile> getVisibilityImages() {
		return visibilityImages;
	}

	public void putAllVisibilityImages(Map<VisibilityModifier, DrawFile> visibilityImages) {
		this.visibilityImages.putAll(visibilityImages);
	}

	public UmlDiagramType getUmlDiagramType() {
		return umlDiagramType;
	}

	public ISkinParam getSkinParam() {
		return skinParam;
	}

	public Rankdir getRankdir() {
		return rankdir;
	}

	public GroupHierarchy getGroupHierarchy() {
		return groupHierarchy;
	}

	public List<Link> getLinks() {
		return links;
	}

	public Map<String, Entity> getEntities() {
		return entities;
	}

	public final Set<Entity> getAllLinkedTo(final Entity ent1) {
		final Set<Entity> result = new HashSet<Entity>();
		result.add(ent1);
		int size = 0;
		do {
			size = result.size();
			for (Entity ent : entities.values()) {
				if (isDirectyLinked(ent, result)) {
					result.add(ent);
				}
			}
		} while (size != result.size());
		result.remove(ent1);
		return Collections.unmodifiableSet(result);
	}

	private boolean isDirectyLinked(Entity ent1, Collection<Entity> others) {
		for (Entity ent2 : others) {
			if (isDirectlyLinkedSlow(ent1, ent2)) {
				return true;
			}
		}
		return false;
	}

	private boolean isDirectlyLinkedSlow(Entity ent1, Entity ent2) {
		for (Link link : links) {
			if (link.isBetween(ent1, ent2)) {
				return true;
			}
		}
		return false;
	}

	public boolean isThereLink(Group g) {
		for (Link l : links) {
			if (l.getEntity1() == g.getEntityCluster() || l.getEntity2() == g.getEntityCluster()) {
				return true;
			}
		}
		return false;
	}

	public List<Link> getAutoLinks(Group g) {
		final List<Link> result = new ArrayList<Link>();
		for (Link l : links) {
			if (l.isAutolink(g)) {
				result.add(l);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<Link> getToEdgeLinks(Group g) {
		final List<Link> result = new ArrayList<Link>();
		for (Link l : links) {
			if (l.isToEdgeLink(g)) {
				result.add(l);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<Link> getFromEdgeLinks(Group g) {
		final List<Link> result = new ArrayList<Link>();
		for (Link l : links) {
			if (l.isFromEdgeLink(g)) {
				result.add(l);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public final Group getTopParent() {
		return topParent;
	}

	public boolean isEmpty(Group g) {
		return groupHierarchy.isEmpty(g);
	}

}
