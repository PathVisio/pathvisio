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

/*
 * PropertyType.java
 *
 * Created on 6 december 2006, 9:50
 *
 */

package org.pathvisio.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Types for Static properties on {@link PathwayElement}
 */
public enum PropertyType 
{
	// all
	COMMENTS ("Comments", "Comments", PropertyClass.COMMENTS),

	// line, shape, datanode, label
	COLOR ("Color", "Color", PropertyClass.COLOR),
			
	// shape, datanode, label
	CENTERX ("CenterX", "Center X", PropertyClass.DOUBLE),
	CENTERY ("CenterY", "Center Y", PropertyClass.DOUBLE),
	
	// shape, datanode, label, modification
	WIDTH ("Width", "Width", PropertyClass.DOUBLE),
	HEIGHT ("Height", "Height", PropertyClass.DOUBLE),

	// modification
	RELX ("relX", "Relative X", PropertyClass.DOUBLE),
	RELY ("relY", "Relative Y", PropertyClass.DOUBLE),
	GRAPHREF ("GraphRef", "GraphRef", PropertyClass.STRING),

	// shape, modification
	TRANSPARENT ("Transparent", "Transparent", PropertyClass.BOOLEAN),
	FILLCOLOR ("FillColor", "Fill Color", PropertyClass.COLOR),
	SHAPETYPE ("ShapeType", "Shape Type", PropertyClass.SHAPETYPE),
	
	// shape
	ROTATION ("Rotation", "Rotation", PropertyClass.ANGLE),
			
	// line
	STARTX ("StartX", "Start X", PropertyClass.DOUBLE), 
	STARTY ("StartY", "Start Y", PropertyClass.DOUBLE), 
	ENDX ("EndX", "End X", PropertyClass.DOUBLE), 
	ENDY ("EndY", "End Y", PropertyClass.DOUBLE),
			
	ENDLINETYPE ("EndLineType", "End Line Type", PropertyClass.LINETYPE), 
	STARTLINETYPE ("StartLineType", "Start Line Type", PropertyClass.LINETYPE), 

	// line, shape and modification
	LINESTYLE ("LineStyle", "Line Style", PropertyClass.LINESTYLE),
			
	// brace
	ORIENTATION ("Orientation", "Orientation", PropertyClass.ORIENTATION),
			
	// datanode
	GENEID ("GeneID", "Database Identifier", PropertyClass.DB_ID),  
	DATASOURCE ("SystemCode", "Database Name", PropertyClass.DATASOURCE), 
	GENMAPP_XREF ("Xref", "Xref", PropertyClass.STRING), // deprecated, maintained for backward compatibility with GenMAPP. 
	BACKPAGEHEAD ("BackpageHead", "Backpage head", PropertyClass.STRING), 
	TYPE ("Type", "Type", PropertyClass.GENETYPE),
		
	MODIFICATIONTYPE ("ModificationType", "ModificationType", PropertyClass.STRING),
	
	// label, modification, datanode
	TEXTLABEL ("TextLabel", "Text Label", PropertyClass.STRING),
	
	// label
	FONTNAME ("FontName", "Font Name", PropertyClass.FONT),
	FONTWEIGHT ("FontWeight", "Bold", PropertyClass.BOOLEAN), 
	FONTSTYLE ("FontStyle", "Italic", PropertyClass.BOOLEAN), 
	FONTSIZE ("FontSize", "Font Size", PropertyClass.DOUBLE),
	OUTLINE ("Outline", "Outline", PropertyClass.OUTLINETYPE),

	// mappinfo
	MAPINFONAME ("MapInfoName", "Title", PropertyClass.STRING),
	ORGANISM ("Organism", "Organism", PropertyClass.ORGANISM), 
	MAPINFO_DATASOURCE ("Data-Source", "Data-Source", PropertyClass.STRING),
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
	GROUPREF ("GroupRef", "GroupRef", PropertyClass.STRING),
	GROUPSTYLE ("GroupStyle", "Group style", PropertyClass.GROUPSTYLETYPE),
	BIOPAXREF( "BiopaxRef", "BiopaxRef", PropertyClass.BIOPAXREF),
	ZORDER ( "Z order", "ZOrder", PropertyClass.INTEGER);

	private String tag, desc;
	private PropertyClass type;
	private boolean hidden;
	
	PropertyType (String aTag, String aDesc, PropertyClass aType, boolean isHidden)
	{
		tag = aTag;
		type = aType;
		desc = aDesc;
		hidden = isHidden;
	}

	PropertyType (String aTag, String aDesc, PropertyClass aType)
	{
		this(aTag, aDesc, aType, false);
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

	public boolean isHidden()
	{
		return hidden;
	}

	public void setHidden(boolean hide) {
		hidden = hide;
	}
	
	public static PropertyType getByTag(String value)
	{
		return tagMapping.get (value);
	}

	static private Map<String, PropertyType> tagMapping = initTagMapping();

	static private Map<String, PropertyType> initTagMapping()
	{
		Map<String, PropertyType> result = new HashMap<String, PropertyType>();
		for (PropertyType o : PropertyType.values())
		{
			result.put (o.tag(), o);
		}
		return result;
	}

}
