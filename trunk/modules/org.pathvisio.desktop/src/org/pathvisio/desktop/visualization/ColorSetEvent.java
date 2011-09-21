// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.desktop.visualization;

import java.util.EventObject;

/**
   This event can get triggered if the model maintained by the ColorSetManger changes,
   i.e. when a ColorSet is modified, when a new ColorSet is added or when a ColorSet is removed.
   If an object wants to receive this type of events it should register itself with the
   global ColorSetManager class.
 */
public class ColorSetEvent extends EventObject
{

	public static final int COLORSET_ADDED = 0;
	public static final int COLORSET_REMOVED = 1;
	public static final int COLORSET_MODIFIED = 2;

	private int type;

	public int getType () { return type; }

	/**
	   Create a new ColorSetEvent of the specified type. The source is the
	   object generating the event, usually ColorSetManager.class or one of
	   Objects underlying the ColorSet data model.

	   note: Source can't be null
	 */
	public ColorSetEvent(Object source, int type)
	{
		super (source);
		this.type = type;
	}
}