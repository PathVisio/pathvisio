/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.core.model;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.SAXOutputter;
import org.jdom2.output.XMLOutputter;
import org.pathvisio.core.biopax.BiopaxElement;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.GraphLink.GraphIdContainer;
import org.xml.sax.SAXException;

/**
 * Read / write GPML files.
 * Base implementation for different GpmlFormat versions.
 * Code that is shared between multiple versions is located here.
 */
public abstract class GpmlFormatAbstract
{
	protected GpmlFormatAbstract (String xsdFile, Namespace nsGPML)
	{
		this.xsdFile = xsdFile;
		this.nsGPML = nsGPML;
	}

	private final Namespace nsGPML;
	private final String xsdFile;
	
	protected abstract Map<String, AttributeInfo> getAttributeInfo();

	public Namespace getGpmlNamespace () { return nsGPML; }

	/**
	 * name of resource containing the gpml schema definition
	 */

	protected static class AttributeInfo
	{
		/**
		 * xsd validated type. Note that in the current implementation
		 * we don't do anything with restrictions, only with the
		 * base type.
		 */
		public String schemaType;

		/**
		 * default value for the attribute
		 */
		public String def; // default

		/**
		 * use of the attribute: can be "required" or "optional"
		 */
		public String use;

		AttributeInfo (String aSchemaType, String aDef, String aUse)
		{
			schemaType = aSchemaType;
			def = aDef;
			use = aUse;
		}
	}

	private boolean isEqualsString(String def, String value)
	{
		return ((def == null && value == null) ||
				(def != null && def.equals(value)) ||
				(def == null && value != null && value.equals("")));
	}

	private boolean isEqualsNumber(String def, String value)
	{
		if (def != null && value != null) {
			Double x = Double.parseDouble(def);
			Double y = Double.parseDouble(value);
			if (Math.abs(x - y) < 1e-6)
				return true;
		}
		return false;
	}

	private boolean isEqualsColor(String def, String value)
	{
		if (def != null && value != null)
		{
			boolean aTrans = "Transparent".equals(def);
			boolean bTrans = "Transparent".equals(value);
			Color a = gmmlString2Color(def);
			Color b = gmmlString2Color(value);
			return (a.equals(b) && aTrans == bTrans);
		}
		return def == null && value == null;
	}
	
	/**
	 * Sets a certain attribute value,
	 * Does a basic check for some types,
	 * throws an exception when you're trying to set an invalid value
	 * If you're trying to set a default value, or an optional value to null,
	 * the attribute is omitted,
	 * leading to a leaner xml output.
	 *
	 * @param tag used for lookup in the defaults table
	 * @param name used for lookup in the defaults table
	 * @param el jdom element where this attribute belongs in
	 * @param value value you wan't to check and set
	 */
	protected void setAttribute(String tag, String name, Element el,
			String value) throws ConverterException {
		String key = tag + "@" + name;
		if (!getAttributeInfo().containsKey(key))
			throw new ConverterException("Trying to set invalid attribute "
					+ key);
		AttributeInfo aInfo = getAttributeInfo().get(key);
		boolean isDefault = false;
		// here we start seeing if the attribute is equal to the
		// default value
		// if so, we can leave out the attribute from the jdom
		// altogether
		if (aInfo.use.equals("optional")) {
			if (aInfo.schemaType.equals("xsd:string")
					|| aInfo.schemaType.equals("xsd:ID")
					|| aInfo.schemaType.equals("gpml:StyleType")) {
				isDefault = isEqualsString(aInfo.def, value);
			} else if (aInfo.schemaType.equals("xsd:float")
					|| aInfo.schemaType.equals("Dimension")) {
				isDefault = isEqualsNumber(aInfo.def, value);
			} else if (aInfo.schemaType.equals("gpml:ColorType")) {
				isDefault = isEqualsColor (aInfo.def, value);
			}
		}
		if (!isDefault)
			el.setAttribute(name, value);
	}

	/**
	 * Gets a certain attribute value,
	 * replaces it with a suitable default under certain conditions.
	 *
	 * @param tag used for lookup in the defaults table
	 * @param name used for lookup in the defaults table
	 * @param el jdom element to get the attribute from
	 * @throws ConverterException
	 */
	protected String getAttribute(String tag, String name, Element el) throws ConverterException
	{
		String key = tag + "@" + name;
		if (!getAttributeInfo().containsKey(key))
				throw new ConverterException("Trying to get invalid attribute " + key);
		AttributeInfo aInfo = getAttributeInfo().get(key);
		String result = ((el == null) ? aInfo.def : el.getAttributeValue(name, aInfo.def));
		return result;
	}

	/**
	 * The GPML xsd implies a certain ordering for children of the pathway element.
	 * (e.g. DataNode always comes before LineShape, etc.)
	 *
	 * This Comparator can sort jdom Elements so that they are in the correct order
	 * for the xsd.
	 */
	protected static class ByElementName implements Comparator<Element>
	{
		// hashmap for quick lookups during sorting
		private Map<String, Integer> elementOrdering;

		// correctly ordered list of tag names, which are loaded into the hashmap in
		// the constructor.
		private final String[] elements = new String[] {
				"Comment", "BiopaxRef", "Graphics", "DataNode", "State", "Interaction", 
				"Line", "GraphicalLine", "Label", "Shape", "Group", "InfoBox", "Legend", "Biopax"
				
			};

		/*
		 * Constructor
		 */
		public ByElementName()
		{
			elementOrdering = new HashMap<String, Integer>();
			for (int i = 0; i < elements.length; ++i)
			{
				elementOrdering.put (elements[i], new Integer(i));
			}
		}

		/*
		 * As a comparison measure, returns difference of index of element names of a and b
		 * in elements array. E.g:
		 * Comment -> index 1 in elements array
		 * Graphics -> index 2 in elements array.
		 * If a.getName() is Comment and b.getName() is Graphics, returns 1-2 -> -1
		 */
		public int compare(Element a, Element b) {
			return ((Integer)elementOrdering.get(a.getName())).intValue() -
				((Integer)elementOrdering.get(b.getName())).intValue();
		}

	}
	
	protected abstract void updateMappInfoVariable(Element root, PathwayElement o) throws ConverterException;
	
	protected void updateMappInfo(Element root, PathwayElement o) throws ConverterException
	{
		setAttribute("Pathway", "Name", root, o.getMapInfoName());
		setAttribute("Pathway", "Data-Source", root, o.getMapInfoDataSource());
		setAttribute("Pathway", "Version", root, o.getVersion());
		setAttribute("Pathway", "Author", root, o.getAuthor());
		setAttribute("Pathway", "Maintainer", root, o.getMaintainer());
		setAttribute("Pathway", "Email", root, o.getEmail());
		setAttribute("Pathway", "Last-Modified", root, o.getLastModified());
		setAttribute("Pathway", "Organism", root, o.getOrganism());

		updateComments(o, root);
		updateBiopaxRef(o, root);
		updateAttributes(o, root);

		Element graphics = new Element("Graphics", nsGPML);
		root.addContent(graphics);

		double[] size = o.getMBoardSize();
		setAttribute("Pathway.Graphics", "BoardWidth", graphics, "" +size[0]);
		setAttribute("Pathway.Graphics", "BoardHeight", graphics, "" + size[1]);
		
		updateMappInfoVariable (root, o);
	}

	public abstract PathwayElement mapElement(Element e, Pathway p) throws ConverterException;

	public PathwayElement mapElement(Element e) throws ConverterException
	{
		return mapElement (e, null);
	}

	protected void mapColor(PathwayElement o, Element e) throws ConverterException
	{
    	Element graphics = e.getChild("Graphics", e.getNamespace());
    	String scol = getAttribute(e.getName() + ".Graphics", "Color", graphics);
    	o.setColor (gmmlString2Color(scol));
	}

	protected void mapShapeColor(PathwayElement o, Element e) throws ConverterException
	{
    	Element graphics = e.getChild("Graphics", e.getNamespace());
		String scol = getAttribute(e.getName() + ".Graphics", "FillColor", graphics);
    	if(scol.equals("Transparent")) {
    		o.setTransparent (true);
    	} else {
    		o.setTransparent (false);
    		o.setFillColor (gmmlString2Color(scol));
    	}
	}

	protected void updateColor(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null)
		{
			Element jdomGraphics = e.getChild("Graphics", e.getNamespace());
			if(jdomGraphics != null)
			{
				setAttribute(e.getName() + ".Graphics", "Color", jdomGraphics, color2HexBin(o.getColor()));
			}
		}
	}

	protected void updateShapeColor(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null)
		{
			Element jdomGraphics = e.getChild("Graphics", e.getNamespace());
			if(jdomGraphics != null)
			{
				String val = o.isTransparent() ? "Transparent" : color2HexBin(o.getFillColor());
				setAttribute (e.getName() + ".Graphics", "FillColor", jdomGraphics, val);
			}
		}
	}

	protected void mapComments(PathwayElement o, Element e) throws ConverterException
	{
		for (Object f : e.getChildren("Comment", e.getNamespace()))
		{
			o.addComment(((Element)f).getText(), getAttribute("Comment", "Source", (Element)f));
		}
	}

	protected void updateComments(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null)
		{
			for (PathwayElement.Comment c : o.getComments())
			{
				Element f = new Element ("Comment", e.getNamespace());
				f.setText (c.getComment());
				setAttribute("Comment", "Source", f, c.getSource());
				e.addContent(f);
			}
		}
	}

	protected void mapAttributes(PathwayElement o, Element e) throws ConverterException
	{
		for (Object f : e.getChildren("Attribute", e.getNamespace()))
		{
			o.setDynamicProperty(
					getAttribute("Attribute", "Key", (Element)f),
					getAttribute("Attribute", "Value", (Element)f));
		}
	}

	protected void updateAttributes(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null)
		{
			for (String key : o.getDynamicPropertyKeys())
			{
				Element a = new Element ("Attribute", e.getNamespace());
				setAttribute ("Attribute", "Key", a, key);
				setAttribute ("Attribute", "Value", a, o.getDynamicProperty(key));
				e.addContent (a);
			}
		}
	}

	protected void mapGraphId (GraphIdContainer o, Element e)
	{
		String id = e.getAttributeValue("GraphId");
		//Never add graphid until all elements are mapped, to prevent duplcate ids!
//		if((id == null || id.equals("")) && o.getGmmlData() != null) {
//			id = o.getGmmlData().getUniqueGraphId();
//		}
		if(id != null) {
			o.setGraphId (id);
		}
	}

	protected void updateGraphId (GraphIdContainer o, Element e)
	{
		String id = o.getGraphId();
		// id has to be unique!
		if (id != null && !id.equals(""))
		{
			e.setAttribute("GraphId", o.getGraphId());
		}
	}

	protected void mapGroupRef (PathwayElement o, Element e)
	{
		String id = e.getAttributeValue("GroupRef");
		if(id != null && !id.equals("")) {
			o.setGroupRef (id);
		}

	}

	protected void updateGroupRef (PathwayElement o, Element e)
	{
		String id = o.getGroupRef();
		if (id != null && !id.equals(""))
		{
			e.setAttribute("GroupRef", o.getGroupRef());
		}
	}

	protected void mapGroup (PathwayElement o, Element e) throws ConverterException
	{
		//ID
		String id = e.getAttributeValue("GroupId");
		if((id == null || id.equals("")) && o.getParent() != null)
			{id = o.getParent().getUniqueGroupId();}
		o.setGroupId (id);

		//GraphId
		mapGraphId(o, e);

		//Style
		o.setGroupStyle(GroupStyle.fromName(getAttribute("Group", "Style", e)));
		//Label
		String textLabel = getAttribute("Group", "TextLabel", e);
		if(textLabel != null) {
			o.setTextLabel (textLabel);
		}
	}

	protected void updateGroup (PathwayElement o, Element e) throws ConverterException
	{
		//ID
		String id = o.createGroupId();
		if (id != null && !id.equals(""))
			{e.setAttribute("GroupId", o.createGroupId());}

		//GraphId
		updateGraphId(o, e);

		//Style
		setAttribute("Group", "Style", e, o.getGroupStyle().getName());
		//Label
		setAttribute ("Group", "TextLabel", e, o.getTextLabel());
	}

	protected abstract void mapMappInfoDataVariable (PathwayElement o, Element e) throws ConverterException;
	
	protected void mapMappInfoData(PathwayElement o, Element e) throws ConverterException
	{
		o.setMapInfoName (getAttribute("Pathway", "Name", e));
		o.setOrganism (getAttribute("Pathway", "Organism", e));
		o.setMapInfoDataSource (getAttribute("Pathway", "Data-Source", e));
		o.setVersion (getAttribute("Pathway", "Version", e));
		o.setAuthor (getAttribute("Pathway", "Author", e));
		o.setMaintainer (getAttribute("Pathway", "Maintainer", e));
		o.setEmail (getAttribute("Pathway", "Email", e));
		o.setLastModified (getAttribute("Pathway", "Last-Modified", e));

		mapMappInfoDataVariable(o, e);
	}
	
	protected void updateBiopax(PathwayElement o, Element e) throws ConverterException
	{
		Document bp = ((BiopaxElement)o).getBiopax();
		if(e != null && bp != null) {
			List<Content> content = bp.getRootElement().cloneContent();
			for(Content c : content) {
				if(c instanceof Element) {
					Element elm = (Element)c;
					if(elm.getNamespace().equals(GpmlFormat.BIOPAX)) {
						e.addContent(c);
					} else if(elm.getName().equals("RDF") && elm.getNamespace().equals(GpmlFormat.RDF)) {
						for(Object ce : elm.getChildren()) {
							if(((Element)ce).getNamespace().equals(GpmlFormat.BIOPAX)) {
								e.addContent((Element)ce);
							}
						}
					} else {
						Logger.log.info("Skipped non-biopax element" + c);
					}
				}
			}
		}
	}

	protected void mapBiopaxRef(PathwayElement o, Element e) throws ConverterException
	{
		for (Object f : e.getChildren("BiopaxRef", e.getNamespace()))
		{
			o.addBiopaxRef(((Element)f).getText());
		}
	}

	protected void updateBiopaxRef(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null)
		{
			for (String ref : o.getBiopaxRefs())
			{
				Element f = new Element ("BiopaxRef", e.getNamespace());
				f.setText (ref);
				e.addContent(f);
			}
		}
	}

	/**
	 * Converts a string containing either a named color (as specified in gpml) or a hexbinary number
	 * to an {@link Color} object
	 * @param strColor
	 */
    public static Color gmmlString2Color(String strColor)
    {
    	if(COLOR_MAPPINGS.contains(strColor))
    	{
    		double[] color = (double[])RGB_MAPPINGS.get(COLOR_MAPPINGS.indexOf(strColor));
    		return new Color((int)(255*color[0]),(int)(255*color[1]),(int)(255*color[2]));
    	}
    	else
    	{
    		try
    		{
    			strColor = padding(strColor, 6, '0');
        		int red = Integer.valueOf(strColor.substring(0,2),16);
        		int green = Integer.valueOf(strColor.substring(2,4),16);
        		int blue = Integer.valueOf(strColor.substring(4,6),16);
        		return new Color(red,green,blue);
    		}
    		catch (Exception e)
    		{
    			Logger.log.error("while converting color: " +
    					"Color " + strColor + " is not valid, element color is set to black", e);
    		}
    	}
    	return new Color(0,0,0);
    }

	/**
	 * Converts an {@link Color} object to a hexbinary string
	 * @param color
	 */
	public static String color2HexBin(Color color)
	{
		String red = padding(Integer.toBinaryString(color.getRed()), 8, '0');
		String green = padding(Integer.toBinaryString(color.getGreen()), 8, '0');
		String blue = padding(Integer.toBinaryString(color.getBlue()), 8, '0');
		String hexBinary = Integer.toHexString(Integer.valueOf(red + green + blue, 2));
		return padding(hexBinary, 6, '0');
	}

    /**
     * Prepends character c x-times to the input string to make it length n
     * @param s	String to pad
     * @param n	Number of characters of the resulting string
     * @param c	character to append
     * @return	string of length n or larger (if given string s > n)
     */
    public static String padding(String s, int n, char c)
    {
    	while(s.length() < n)
    	{
    		s = c + s;
    	}
    	return s;
    }

	public static final List<double[]> RGB_MAPPINGS = Arrays.asList(new double[][] {
			{0, 1, 1},		// aqua
			{0, 0, 0},	 	// black
			{0, 0, 1}, 		// blue
			{1, 0, 1},		// fuchsia
			{.5, .5, .5,},	// gray
			{0, .5, 0}, 	// green
			{0, 1, 0},		// lime
			{.5, 0, 0},		// maroon
			{0, 0, .5},		// navy
			{.5, .5, 0},	// olive
			{.5, 0, .5},	// purple
			{1, 0, 0}, 		// red
			{.75, .75, .75},// silver
			{0, .5, .5}, 	// teal
			{1, 1, 1},		// white
			{1, 1, 0},		// yellow
			{0, 0, 0}		// transparent (actually irrelevant)
		});

	public static final List<String> COLOR_MAPPINGS = Arrays.asList(new String[]{
			"Aqua", "Black", "Blue", "Fuchsia", "Gray", "Green", "Lime",
			"Maroon", "Navy", "Olive", "Purple", "Red", "Silver", "Teal",
			"White", "Yellow", "Transparent"
		});

	public void readFromRoot(Element root, Pathway pwy) throws ConverterException
	{
		mapElement(root, pwy); // MappInfo

		// Iterate over direct children of the root element
		for (Object e : root.getChildren())
		{
			mapElement((Element)e, pwy);
		}
		Logger.log.trace ("End copying map elements");

		//Add graphIds for objects that don't have one
		addGraphIds(pwy);

		//Convert absolute point coordinates of linked points to
		//relative coordinates
		convertPointCoordinates(pwy);
	}

	private static void addGraphIds(Pathway pathway) throws ConverterException {
		for(PathwayElement pe : pathway.getDataObjects()) {
			String id = pe.getGraphId();
			if(id == null || "".equals(id))
			{
				if (pe.getObjectType() == ObjectType.LINE || pe.getObjectType() == ObjectType.GRAPHLINE)
				{
					// because we forgot to write out graphId's on Lines on older pathways
					// generate a graphId based on hash of coordinates
					// so that pathways with branching history still have the same id.
					// This part may be removed for future versions of GPML (2010+)

					StringBuilder builder = new StringBuilder();
					builder.append(pe.getMStartX());
					builder.append(pe.getMStartY());
					builder.append(pe.getMEndX());
					builder.append(pe.getMEndY());
					builder.append(pe.getStartLineType());
					builder.append(pe.getEndLineType());

					String newId;
					int i = 1;
					do
					{
						newId = "id" + Integer.toHexString((builder.toString() + ("_" + i)).hashCode());
						i++;
					}
					while (pathway.getGraphIds().contains(newId));
					pe.setGraphId(newId);
				}
			}
		}
	}

	private static void convertPointCoordinates(Pathway pathway) throws ConverterException
	{
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(pe.getObjectType() == ObjectType.LINE || pe.getObjectType() == ObjectType.GRAPHLINE) {
				String sr = pe.getStartGraphRef();
				String er = pe.getEndGraphRef();
				if(sr != null && !"".equals(sr) && !pe.getMStart().relativeSet()) {
					GraphIdContainer idc = pathway.getGraphIdContainer(sr);
					Point2D relative = idc.toRelativeCoordinate(
							new Point2D.Double(
								pe.getMStart().getRawX(),
								pe.getMStart().getRawY()
							)
					);
					pe.getMStart().setRelativePosition(relative.getX(), relative.getY());
				}
				if(er != null && !"".equals(er) && !pe.getMEnd().relativeSet()) {
					GraphIdContainer idc = pathway.getGraphIdContainer(er);
					Point2D relative = idc.toRelativeCoordinate(
							new Point2D.Double(
								pe.getMEnd().getRawX(),
								pe.getMEnd().getRawY()
							)
					);
					pe.getMEnd().setRelativePosition(relative.getX(), relative.getY());
				}
                ((MLine)pe).getConnectorShape().recalculateShape(((MLine)pe));
			}
		}
	}

	/**
	 * validates a JDOM document against the xml-schema definition specified by 'xsdFile'
	 * @param doc the document to validate
	 */
	public void validateDocument(Document doc) throws ConverterException
	{
		ClassLoader cl = Pathway.class.getClassLoader();
		InputStream is = cl.getResourceAsStream(xsdFile);
		if(is != null) {
			Schema schema;
			try {
				SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				StreamSource ss = new StreamSource (is);
				schema = factory.newSchema(ss);
				ValidatorHandler vh =  schema.newValidatorHandler();
				SAXOutputter so = new SAXOutputter(vh);
				so.output(doc);
				// If no errors occur, the file is valid according to the gpml xml schema definition
				Logger.log.info("Document is valid according to the xml schema definition '" +
						xsdFile.toString() + "'");
			} catch (SAXException se) {
				Logger.log.error("Could not parse the xml-schema definition", se);
				throw new ConverterException (se);
			} catch (JDOMException je) {
				Logger.log.error("Document is invalid according to the xml-schema definition!: " +
						je.getMessage(), je);
				XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());

				Logger.log.error("The invalid XML code:\n" + xmlcode.outputString(doc));
				throw new ConverterException (je);
			}
		} else {
			Logger.log.error("Document is not validated because the xml schema definition '" +
					xsdFile + "' could not be found in classpath");
			throw new ConverterException ("Document is not validated because the xml schema definition '" +
					xsdFile + "' could not be found in classpath");
		}
	}

}
