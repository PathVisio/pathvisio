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
package org.pathvisio.core.model;

/**
 * LineStyle, either Solid, dashed or double. Not to be confused with LineType, 
 * which defines the appearance of the arrow head.
 */
public class LineStyle {
	public static final int SOLID = 0;
	public static final int DASHED = 1;
	public static final int DOUBLE = 2;

	public static String[] getNames() {
		return new String[] {"Solid", "Dashed", "Double"};
	}
	
	//dynamic property key for LineStyle.DOUBLE, until GPML is updated
	//TODO: remove after next GPML update
	final static String DOUBLE_LINE_KEY = "org.pathvisio.DoubleLineProperty";

}
