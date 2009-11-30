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
//
package org.pathvisio.model;

/**
 * Used to notify listeners of changes to the model, i.e a Pathway or PathwayElement.
 * This can mean the addition or removal of whole elements, or just a modification to
 * one of the properties of an existing element.
 *
 * This event is currently used both by PathwayListener's and  PathwayElementListener's.
 * That may change in the future.
 */
public class PathwayEvent
{
	//TODO: make enum
	public static final int MODIFIED_GENERAL = 0;
	public static final int MODIFIED_SHAPE = 1;

	/**
	 * Sent to listeners of Pathway when an object was deleted
	 */
	public static final int DELETED = 2;

	/**
	 * Sent to listeners of Pathway when a new object was added
	 */
	public static final int ADDED = 3;

	public static final int PROPERTY = 4; // e.g. name change
	public static final int WINDOW = 5;

	private PathwayElement affectedData;
	public PathwayElement getAffectedData () { return affectedData; }

	private int type;
	public int getType() { return type; }

	public PathwayEvent (PathwayElement object, int t)
	{
		affectedData = object;
		type = t;
	}
}
