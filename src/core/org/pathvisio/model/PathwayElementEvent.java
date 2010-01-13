// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.pathvisio.model;


/**
 * This event is used to notify {@link PathwayElementListener}s of changes to properties of a PathwayElement.  
 * <p>
 * There are three variations on this event:
 * <ol>
 * <li>Only a single, known property may have changed, that is not a coordinate change.
 * <li>Only one of the coordinate properties (x, y, width, height) may have changed.
 * <li>Any property may have changed.
 * </ol>
 * 
 * Variation 2 is introduced for performance reasons. Coordinate changes generate a lot of events (e.g. resizing or dragging an object) and
 * typically change in groups (if MLeft changes, MCenterX also changes). Listeners that are interested in coordinate changes, may filter out
 * changes to these properties by using the {@link #isCoordinateChange()} property. Listeners that are not interested in coordinate changes
 * may use the {@link #affectsProperty(Property)} method to find out if a property of interest may have changed.
 * 
 * @author Mark Woon
 */
public final class PathwayElementEvent {
	private final PathwayElement pwElement;
	private final Object property;
	private final boolean coordinateChange;

	protected static PathwayElementEvent createSinglePropertyEvent(PathwayElement pwe, Object property) {
		return new PathwayElementEvent(pwe, property, false);
	}
	
	protected static PathwayElementEvent createAllPropertiesEvent(PathwayElement pwe) {
		return new PathwayElementEvent(pwe, null, false);
	}
	
	protected static PathwayElementEvent createCoordinatePropertyEvent(PathwayElement pwe) {
		return new PathwayElementEvent(pwe, null, true);
	}
	
	/**
	 * Constructor.
	 *
	 * @param elem the PathwayElement that's been modified
	 * @param prop the Property on the element that's been modified
	 * @param coordinateChange Flag to indicate this event applies to a coordinate change.
	 */
	private PathwayElementEvent(PathwayElement elem, Object prop, boolean coordinateChange) {
		pwElement = elem;
		property = prop;
		this.coordinateChange = coordinateChange;
	}

	/**
	 * Returns true if this event was caused by a coordinate change (e.g. movement or resize operation).
	 */
	public boolean isCoordinateChange() {
		return coordinateChange;
	}

	/**
	 * Gets the PathwayElement whose properties have been modified.
	 */
	public PathwayElement getModifiedPathwayElement() {
		return pwElement;
	}


	/**
	 * Check if the given property may have been modified in this event. Note that this method does
	 * not apply to coordinate properties (position, size), these need to be checked with {@link #isCoordinateChange()}.
	 * @param prop The property to check.
	 * @return true if the property may have been modified, false if not.
	 */
	public boolean affectsProperty(Property prop) {
		return property == null || property.equals(prop);
	}
}
