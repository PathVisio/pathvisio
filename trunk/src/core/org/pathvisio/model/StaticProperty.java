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
public enum StaticProperty
{
	// all
	COMMENTS ("Comments", "Comments", StaticPropertyType.COMMENTS, 101),

	// line, shape, datanode, label
	COLOR ("Color", "Color", StaticPropertyType.COLOR, 202),
	// shape, datanode, label
	CENTERX ("CenterX", "Center X", StaticPropertyType.DOUBLE, 103),
	CENTERY ("CenterY", "Center Y", StaticPropertyType.DOUBLE, 104),

	// shape, datanode, label, modification
	WIDTH ("Width", "Width", StaticPropertyType.DOUBLE, 105),
	HEIGHT ("Height", "Height", StaticPropertyType.DOUBLE, 106),

	// modification
	RELX ("relX", "Relative X", StaticPropertyType.DOUBLE, 107),
	RELY ("relY", "Relative Y", StaticPropertyType.DOUBLE, 108),
	GRAPHREF ("GraphRef", "GraphRef", StaticPropertyType.STRING, 109),

	// shape, modification
	TRANSPARENT ("Transparent", "Transparent", StaticPropertyType.BOOLEAN, 210),
	FILLCOLOR ("FillColor", "Fill Color", StaticPropertyType.COLOR, 211),
	SHAPETYPE ("ShapeType", "Shape Type", StaticPropertyType.SHAPETYPE, 112),

	// shape
	ROTATION ("Rotation", "Rotation", StaticPropertyType.ANGLE, 113),

	// line
	STARTX ("StartX", "Start X", StaticPropertyType.DOUBLE, 114),
	STARTY ("StartY", "Start Y", StaticPropertyType.DOUBLE, 115),
	ENDX ("EndX", "End X", StaticPropertyType.DOUBLE, 116),
	ENDY ("EndY", "End Y", StaticPropertyType.DOUBLE, 117),

	STARTLINETYPE ("StartLineType", "Start Line Type", StaticPropertyType.LINETYPE, 118),
	ENDLINETYPE ("EndLineType", "End Line Type", StaticPropertyType.LINETYPE, 119),

	// line, shape and modification
	LINESTYLE ("LineStyle", "Line Style", StaticPropertyType.LINESTYLE, 120),

	// brace
	ORIENTATION ("Orientation", "Orientation", StaticPropertyType.ORIENTATION, 121),

	// datanode
	GENEID ("GeneID", "Database Identifier", StaticPropertyType.DB_ID, 122),
	DATASOURCE ("SystemCode", "Database Name", StaticPropertyType.DATASOURCE, 123),
	GENMAPP_XREF ("Xref", "Xref", StaticPropertyType.STRING, 124), // deprecated, maintained for backward compatibility with GenMAPP.
	BACKPAGEHEAD ("BackpageHead", "Backpage head", StaticPropertyType.STRING, 125),
	TYPE ("Type", "Type", StaticPropertyType.GENETYPE, 126),

	MODIFICATIONTYPE ("ModificationType", "ModificationType", StaticPropertyType.STRING, 127),

	// label, modification, datanode
	TEXTLABEL ("TextLabel", "Text Label", StaticPropertyType.STRING, 128),

	// label
	FONTNAME ("FontName", "Font Name", StaticPropertyType.FONT, 129),
	FONTWEIGHT ("FontWeight", "Bold", StaticPropertyType.BOOLEAN, 130),
	FONTSTYLE ("FontStyle", "Italic", StaticPropertyType.BOOLEAN, 131),
	FONTSIZE ("FontSize", "Font Size", StaticPropertyType.DOUBLE, 132),
	OUTLINE ("Outline", "Outline", StaticPropertyType.OUTLINETYPE, 133),

	// mappinfo
	MAPINFONAME ("MapInfoName", "Title", StaticPropertyType.STRING, 134),
	ORGANISM ("Organism", "Organism", StaticPropertyType.ORGANISM, 135),
	MAPINFO_DATASOURCE ("Data-Source", "Data-Source", StaticPropertyType.STRING, 136),
	VERSION ("Version", "Version", StaticPropertyType.STRING, 137),
	AUTHOR ("Author", "Author", StaticPropertyType.STRING, 138),
	MAINTAINED_BY ("Maintained-By", "Maintainer", StaticPropertyType.STRING, 139),
	EMAIL ("Email", "Email", StaticPropertyType.STRING, 140),
	LAST_MODIFIED ("Last-Modified", "Last Modified", StaticPropertyType.STRING, 141),
	AVAILABILITY ("Availability", "Availability", StaticPropertyType.STRING, 142),
	BOARDWIDTH ("BoardWidth", "Board Width", StaticPropertyType.DOUBLE, 143),
	BOARDHEIGHT ("BoardHeight", "Board Height", StaticPropertyType.DOUBLE, 144),
	WINDOWWIDTH ("WindowWidth", "Window Width", StaticPropertyType.DOUBLE, 145, true),
	WINDOWHEIGHT ("WindowHeight", "Window Height", StaticPropertyType.DOUBLE, 146, true),

	// other
	GRAPHID ("GraphId", "GraphId", StaticPropertyType.STRING, 147),
	STARTGRAPHREF ("StartGraphRef", "StartGraphRef", StaticPropertyType.STRING, 148),
	ENDGRAPHREF ("EndGraphRef", "EndGraphRef", StaticPropertyType.STRING, 149),
	GROUPID ("GroupId", "GroupId", StaticPropertyType.STRING, 150),
	GROUPREF ("GroupRef", "GroupRef", StaticPropertyType.STRING, 151),
	GROUPSTYLE ("GroupStyle", "Group style", StaticPropertyType.GROUPSTYLETYPE, 152),
	BIOPAXREF( "BiopaxRef", "BiopaxRef", StaticPropertyType.BIOPAXREF, 153),
	ZORDER ( "Z order", "ZOrder", StaticPropertyType.INTEGER, 154);

	private String tag, desc;
	private StaticPropertyType type;
	private boolean hidden;
	private int order;

	private StaticProperty (String aTag, String aDesc, StaticPropertyType aType, int anOrder, boolean isHidden)
	{
		tag = aTag;
		type = aType;
		desc = aDesc;
		hidden = isHidden;
		order = anOrder;
	}

	private StaticProperty (String aTag, String aDesc, StaticPropertyType aType, int anOrder)
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
	public StaticPropertyType type()
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

	public static StaticProperty getByTag(String value)
	{
		return tagMapping.get (value);
	}

	static private Map<String, StaticProperty> tagMapping = initTagMapping();

	static private Map<String, StaticProperty> initTagMapping()
	{
		Map<String, StaticProperty> result = new HashMap<String, StaticProperty>();
		for (StaticProperty o : StaticProperty.values())
		{
			result.put (o.tag(), o);
		}
		return result;
	}

}
