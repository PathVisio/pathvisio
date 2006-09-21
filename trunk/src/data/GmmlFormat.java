package data;

import java.util.*;

import debug.Logger;
import org.jdom.*;

public class GmmlFormat {

	/**
	 * The GMML xsd implies a certain ordering for children of the pathway element.
	 * (e.g. GeneProduct always comes before LineShape, etc.)
	 * 
	 * This Comparator can sort jdom Elements so that they are in the correct order
	 * for the xsd.
	 *  
	 * @author Martijn.vanIersel
	 */
	private static class ByElementName implements Comparator<Element>
	{
		// hashmap for quick lookups during sorting
		private HashMap<String, Integer> elementOrdering;
				
		// correctly ordered list of tag names, which are loaded into the hashmap in
		// the constructor.
		private final String[] elements = new String[] {
			"Notes", "Comment", "Graphics", "GeneProduct", "Line", "Label",
			"Shape", "Brace", "FixedShape", "ComplexShape", "InfoBox", "Legend"
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
	
	public static Logger log;

	public static Document createJdom(GmmlData data) throws ConverterException
	{
		Document doc = new Document();
		
		Element root = new Element("Pathway");
		doc.setRootElement(root);

		List<Element> elementList = new ArrayList<Element>();
    	
		for (GmmlDataObject o : data.dataObjects)
		{
			if (o.getObjectType() == ObjectType.MAPPINFO)
			{
				root.setAttribute("Name", o.getMapInfoName());
				root.setAttribute("Data-Source", "GenMAPP 2.0");
				root.setAttribute("Version", o.getVersion());
				root.setAttribute("Author", o.getAuthor());
				root.setAttribute("Maintained-By", o.getMaintainedBy());
				root.setAttribute("Email", o.getEmail());
				root.setAttribute("Availability", o.getAvailability());
				root.setAttribute("Last-Modified", o.getLastModified());

				Element notes = new Element("Notes");
				notes.addContent(o.getNotes());
				root.addContent(notes);

				Element comments = new Element("Comment");
				comments.addContent(o.getComment());
				root.addContent(comments);
				
				Element graphics = new Element("Graphics");
				root.addContent(graphics);
				
				graphics.setAttribute("BoardWidth", "" + o.getBoardWidth()* GmmlData.GMMLZOOM);
				graphics.setAttribute("BoardHeight", "" + o.getBoardHeight()* GmmlData.GMMLZOOM);
				graphics.setAttribute("WindowWidth", "" + o.getWindowWidth()* GmmlData.GMMLZOOM);
				graphics.setAttribute("WindowHeight", "" + o.getWindowHeight()* GmmlData.GMMLZOOM);
				
			}
			else
			{
				Element e = o.createJdomElement();
				if (e != null)
					elementList.add(e);
			}
		}
		
    	// now sort the generated elements in the order defined by the xsd
		Collections.sort(elementList, new ByElementName());
		for (Element e : elementList)
		{			
			root.addContent(e);
		}
		
		return doc;
	}
	
}
