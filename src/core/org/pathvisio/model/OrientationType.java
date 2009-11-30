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

import java.util.Arrays;
import java.util.List;

/**
   Contains possible orientations for a Shape, such as
   "Top" (0 degrees) or "Right" (90 degrees)
*/
public class OrientationType {

	// warning: don't change these constants. Correct mapping to .MAPP format depends on it.
	public static final int TOP		= 0;
	public static final int RIGHT	= 1;
	public static final int BOTTOM	= 2;
	public static final int LEFT	= 3;

	// Some mappings to Gpml
	private static final List<String> ORIENTATION_MAPPINGS = Arrays.asList(new String[] {
			"top", "right", "bottom", "left"
	});

	public static int getMapping(String value)
	{
		return ORIENTATION_MAPPINGS.indexOf(value);
	}

	public static String getMapping(int value)
	{
		return (String)ORIENTATION_MAPPINGS.get(value);
	}

	public static String[] getNames() {
		return new String[] {"Top", "Right", "Bottom", "Left"};
	}
}
