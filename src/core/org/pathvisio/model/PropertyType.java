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

import java.util.HashMap;
import java.util.Map;

/**
 * Types for Static properties on {@link PathwayElement}
 */
public enum PropertyType
{
	// all
	COMMENTS ("Comments", "Comments", PropertyClass.COMMENTS, 101),

	// line, shape, datanode, label
	COLOR ("Color", "Color", PropertyClass.COLOR, 202),
	// shape, datanode, label
	CENTERX ("CenterX", "Center X", PropertyClass.DOUBLE, 103),
	CENTERY ("CenterY", "Center Y", PropertyClass.DOUBLE, 104),

	// shape, datanode, label, modification
	WIDTH ("Width", "Width", PropertyClass.DOUBLE, 105),
	HEIGHT ("Height", "Height", PropertyClass.DOUBLE, 106),

	// modification
	RELX ("relX", "Relative X", PropertyClass.DOUBLE, 107),
	RELY ("relY", "Relative Y", PropertyClass.DOUBLE, 108),
	GRAPHREF ("GraphRef", "GraphRef", PropertyClass.STRING, 109),

	// shape, modification
	TRANSPARENT ("Transparent", "Transparent", PropertyClass.BOOLEAN, 210),
	FILLCOLOR ("FillColor", "Fill Color", PropertyClass.COLOR, 211),
	SHAPETYPE ("ShapeType", "Shape Type", PropertyClass.SHAPETYPE, 112),

	// shape
	ROTATION ("Rotation", "Rotation", PropertyClass.ANGLE, 113),

	// line
	STARTX ("StartX", "Start X", PropertyClass.DOUBLE, 114),
	STARTY ("StartY", "Start Y", PropertyClass.DOUBLE, 115),
	ENDX ("EndX", "End X", PropertyClass.DOUBLE, 116),
	ENDY ("EndY", "End Y", PropertyClass.DOUBLE, 117),

	STARTLINETYPE ("StartLineType", "Start Line Type", PropertyClass.LINETYPE, 118),
	ENDLINETYPE ("EndLineType", "End Line Type", PropertyClass.LINETYPE, 119),

	// line, shape and modification
	LINESTYLE ("LineStyle", "Line Style", PropertyClass.LINESTYLE, 120),

	// brace
	ORIENTATION ("Orientation", "Orientation", PropertyClass.ORIENTATION, 121),

	// datanode
	GENEID ("GeneID", "Database Identifier", PropertyClass.DB_ID, 122),
	DATASOURCE ("SystemCode", "Database Name", PropertyClass.DATASOURCE, 123),
	GENMAPP_XREF ("Xref", "Xref", PropertyClass.STRING, 124), // deprecated, maintained for backward compatibility with GenMAPP.
	BACKPAGEHEAD ("BackpageHead", "Backpage head", PropertyClass.STRING, 125),
	TYPE ("Type", "Type", PropertyClass.GENETYPE, 126),

	MODIFICATIONTYPE ("ModificationType", "ModificationType", PropertyClass.STRING, 127),

	// label, modification, datanode
	TEXTLABEL ("TextLabel", "Text Label", PropertyClass.STRING, 128),

	// label
	FONTNAME ("FontName", "Font Name", PropertyClass.FONT, 129),
	FONTWEIGHT ("FontWeight", "Bold", PropertyClass.BOOLEAN, 130),
	FONTSTYLE ("FontStyle", "Italic", PropertyClass.BOOLEAN, 131),
	FONTSIZE ("FontSize", "Font Size", PropertyClass.DOUBLE, 132),
	OUTLINE ("Outline", "Outline", PropertyClass.OUTLINETYPE, 133),

	// mappinfo
	MAPINFONAME ("MapInfoName", "Title", PropertyClass.STRING, 134),
	ORGANISM ("Organism", "Organism", PropertyClass.ORGANISM, 135),
	MAPINFO_DATASOURCE ("Data-Source", "Data-Source", PropertyClass.STRING, 136),
	VERSION ("Version", "Version", PropertyClass.STRING, 137),
	AUTHOR ("Author", "Author", PropertyClass.STRING, 138),
	MAINTAINED_BY ("Maintained-By", "Maintainer", PropertyClass.STRING, 139),
	EMAIL ("Email", "Email", PropertyClass.STRING, 140),
	LAST_MODIFIED ("Last-Modified", "Last Modified", PropertyClass.STRING, 141),
	AVAILABILITY ("Availability", "Availability", PropertyClass.STRING, 142),
	BOARDWIDTH ("BoardWidth", "Board Width", PropertyClass.DOUBLE, 143),
	BOARDHEIGHT ("BoardHeight", "Board Height", PropertyClass.DOUBLE, 144),
	WINDOWWIDTH ("WindowWidth", "Window Width", PropertyClass.DOUBLE, 145, true),
	WINDOWHEIGHT ("WindowHeight", "Window Height", PropertyClass.DOUBLE, 146, true),

	// other
	GRAPHID ("GraphId", "GraphId", PropertyClass.STRING, 147),
	STARTGRAPHREF ("StartGraphRef", "StartGraphRef", PropertyClass.STRING, 148),
	ENDGRAPHREF ("EndGraphRef", "EndGraphRef", PropertyClass.STRING, 149),
	GROUPID ("GroupId", "GroupId", PropertyClass.STRING, 150),
	GROUPREF ("GroupRef", "GroupRef", PropertyClass.STRING, 151),
	GROUPSTYLE ("GroupStyle", "Group style", PropertyClass.GROUPSTYLETYPE, 152),
	BIOPAXREF( "BiopaxRef", "BiopaxRef", PropertyClass.BIOPAXREF, 153),
	ZORDER ( "Z order", "ZOrder", PropertyClass.INTEGER, 154);

	private String tag, desc;
	private PropertyClass type;
	private boolean hidden;
	private int order;

	private PropertyType (String aTag, String aDesc, PropertyClass aType, int anOrder, boolean isHidden)
	{
		tag = aTag;
		type = aType;
		desc = aDesc;
		hidden = isHidden;
		order = anOrder;
	}

	private PropertyType (String aTag, String aDesc, PropertyClass aType, int anOrder)
	{
		this(aTag, aDesc, aType, anOrder, false);
	}

	/**
	 * @return Name of GPML attribute related to this property.
	 */
	public String tag()
	{
		return tag;
	}

	/**
	 * @return Description used e.g. in property table
	 */
	public String desc()
	{
		return desc;
	}

	/**
	 * @return Data type of this property
	 */
	public PropertyClass type()
	{
		return type;
	}

	/**
	 * @return true if this is attribute should not be shown in property table
	 */
	public boolean isHidden()
	{
		return hidden;
	}

	public void setHidden(boolean hide) {
		hidden = hide;
	}

	/**
	 * @return Logical sort order for display in Property table. Related properties sort together
	 */
	public int getOrder()
	{
		return order;
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
