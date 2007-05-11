// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

/*
 * PropertyType.java
 *
 * Created on 6 december 2006, 9:50
 *
 */

package org.pathvisio.model;

/**
 *
 * @author Martijn
 */
public enum PropertyType 
{
	// all
	NOTES ("Notes", "Notes", PropertyClass.STRING),
	COMMENT ("Comment", "Comment", PropertyClass.STRING),

	// line, shape, brace, geneproduct, label
	COLOR ("Color", "Color", PropertyClass.COLOR),
			
	// shape, brace, geneproduct, label
	CENTERX ("CenterX", "Center X", PropertyClass.DOUBLE),
	CENTERY ("CenterY", "Center Y", PropertyClass.DOUBLE),
	WIDTH ("Width", "Width", PropertyClass.DOUBLE),
	HEIGHT ("Height", "Height", PropertyClass.DOUBLE),
			
	// shape
	TRANSPARENT ("Transparent", "Transparent", PropertyClass.BOOLEAN),
	FILLCOLOR ("FillColor", "Fill Color", PropertyClass.COLOR),
	SHAPETYPE ("ShapeType", "Shape Type", PropertyClass.SHAPETYPE),
	ROTATION ("Rotation", "Rotation", PropertyClass.ANGLE),
			
	// line
	STARTX ("StartX", "Start X", PropertyClass.DOUBLE), 
	STARTY ("StartY", "Start Y", PropertyClass.DOUBLE), 
	ENDX ("EndX", "End X", PropertyClass.DOUBLE), 
	ENDY ("EndY", "End Y", PropertyClass.DOUBLE),
			
	LINETYPE ("LineType", "Line Type", PropertyClass.LINETYPE), 
	LINESTYLE ("LineStyle", "Line Style", PropertyClass.LINESTYLE),
			
	// brace
	ORIENTATION ("Orientation", "Orientation", PropertyClass.ORIENTATION),
			
	// gene product
	GENEID ("GeneID", "Database Identifier", PropertyClass.DB_ID), //TODO: change tag 
	SYSTEMCODE ("SystemCode", "Database Name", PropertyClass.DATASOURCE), 
	XREF ("Xref", "Xref", PropertyClass.STRING), // unused 
	BACKPAGEHEAD ("BackpageHead", "Backpage head", PropertyClass.STRING), 
	TYPE ("Type", "Type", PropertyClass.GENETYPE),
			
	// label
	TEXTLABEL ("TextLabel", "Text Label", PropertyClass.STRING), 
	FONTNAME ("FontName", "Font Name", PropertyClass.FONT),
	FONTWEIGHT ("FontWeight", "Bold", PropertyClass.BOOLEAN), 
	FONTSTYLE ("FontStyle", "Italic", PropertyClass.BOOLEAN), 
	FONTSIZE ("FontSize", "Font Size", PropertyClass.DOUBLE),

	// mappinfo
	MAPINFONAME ("MapInfoName", "Map Info Name", PropertyClass.STRING),
	ORGANISM ("Organism", "Organism", PropertyClass.ORGANISM), 
	DATA_SOURCE ("Data-Source", "Data-Source", PropertyClass.STRING),
	VERSION ("Version", "Version", PropertyClass.STRING), 
	AUTHOR ("Author", "Author", PropertyClass.STRING), 
	MAINTAINED_BY ("Maintained-By", "Maintainer", PropertyClass.STRING),
	EMAIL ("Email", "Email", PropertyClass.STRING), 
	LAST_MODIFIED ("Last-Modified", "Last Modified", PropertyClass.STRING), 
	AVAILABILITY ("Availability", "Availability", PropertyClass.STRING),
	BOARDWIDTH ("BoardWidth", "Board Width", PropertyClass.DOUBLE), 
	BOARDHEIGHT ("BoardHeight", "Board Height", PropertyClass.DOUBLE), 
	WINDOWWIDTH ("WindowWidth", "Window Width", PropertyClass.DOUBLE, true), 
	WINDOWHEIGHT ("WindowHeight", "Window Height", PropertyClass.DOUBLE, true),

	// other
	GRAPHID ("GraphId", "GraphId", PropertyClass.STRING), 
	STARTGRAPHREF ("StartGraphRef", "StartGraphRef", PropertyClass.STRING), 
	ENDGRAPHREF ("EndGraphRef", "EndGraphRef", PropertyClass.STRING),
	GROUPID ("GroupId", "GroupId", PropertyClass.STRING),
	GROUPREF ("GroupRef", "GroupRef", PropertyClass.STRING); 

	private String tag, desc;
	private PropertyClass type;
	private boolean hidden;
	
	PropertyType (String _tag, String _desc, PropertyClass _type, boolean _hidden)
	{
		tag = _tag;
		type = _type;
		desc = _desc;
		hidden = _hidden;
	}

	PropertyType (String _tag, String _desc, PropertyClass _type)
	{
		this(_tag, _desc, _type, false);
	}

	public String tag()
	{
		return tag;
	}
	
	public String desc()
	{
		return desc;
	}
	
	public PropertyClass type()
	{
		return type;
	}	

	public boolean hidden()
	{
		return hidden;
	}	
}
