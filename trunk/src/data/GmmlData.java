package data;

import org.jdom.JDOMException;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.*;
import org.xml.sax.SAXException;


import graphics.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.awt.Dimension;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

/**
*	This class handles GMML file-input
*/

public class GmmlData
{
	final public static int GMMLZOOM = 15;
	final static File xsdFile = new File("GMML_compat.xsd");
	
	public File xmlFile;
	GmmlDrawing drawing;
	public Document doc;
	
	private List pathwayAttributes;
	private int[] drawingDims = {800,800};
	
	/**
	 * Constructor for this class
	 * @param file - the file to read
	 */
	public GmmlData(GmmlDrawing drawing) 
	{
		this.drawing = drawing;
		doc = new Document();
		Element root = new Element("Pathway");
		Element graphics = new Element("Graphics");
		root.addContent(graphics);
		root.addContent(new Element("InfoBox"));
		doc.setRootElement(root);
		drawing.mappInfo = new GmmlMappInfo(root);
		int width = drawing.gmmlVision.sc.getSize().x;
		int height = drawing.gmmlVision.sc.getSize().y;
		drawing.mappInfo.boardWidth = width;
		drawing.mappInfo.boardHeight = height;
		drawing.mappInfo.windowWidth = width;
		drawing.mappInfo.windowHeight = height;		
	}
	
	public GmmlData(String file, GmmlDrawing drawing)
	{
		// Create the drawing
		this.drawing = drawing;
		// Start XML processing
		System.out.println("Start reading the XML file: " + file);
		SAXBuilder builder  = new SAXBuilder(false); // no validation when reading the xml file
		// try to read the file; if an error occurs, catch the exception and print feedback
		try
		{
			xmlFile = new File(file);
			// build JDOM tree
			doc = builder.build(xmlFile);
			// Validate the JDOM document
			validateDocument(doc);
			// Copy the pathway information to a GmmlDrawing
			toGmmlGraphics();
		}
		catch(JDOMParseException pe) 
		{
			 System.out.println("Parse error: " + pe.getMessage());
		}
		catch(JDOMException e)
		{
			System.out.println(file + " is invalid.");
			System.out.println(e.getMessage());
		}
		catch(IOException e)
		{
			System.out.println("Could not access " + file);
			System.out.println(e.getMessage());
		}	
	}
	

	/**
	 * Method to question the private property drawing
	 * @return drawing
	 */
	public GmmlDrawing getDrawing()
	{
		return drawing;
	}
	
	/**
	 * Maps the element specified to a GmmlGraphcis object
	 * @param e - the element to map
	 */
	public void mapElement(Element e) {
		// Check if a GmmlGraphics exists for this element
		// Assumes that classname = 'Gmml' + Elementname
		try {
			Class cl = Class.forName("graphics.Gmml"+e.getName());
			Constructor con = cl.getConstructor(new Class[] { Element.class, GmmlDrawing.class });
			System.out.println(e.getName());
			Object obj = con.newInstance(new Object[] { e, drawing });
			drawing.addElement((GmmlGraphics)obj);
		}
		catch (ClassNotFoundException cnfe)
		{
//			System.out.println(e.getName() + " could not be mapped");
		}
		catch (NoSuchMethodException nsme)
		{
			System.out.println("The GmmlGraphics class representing '" + e.getName() + 
					"' has no constructor for a JDOM element");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Maps the contents of the JDom tree to a GmmlDrawing
	 */
	public void toGmmlGraphics() {
		// Get the pathway element
		Element root = doc.getRootElement();
		drawing.mappInfo = new GmmlMappInfo(root);
		
		drawing.setSize(drawing.mappInfo.boardWidth, drawing.mappInfo.boardHeight);
		drawing.dims = new Dimension(drawing.mappInfo.boardWidth, drawing.mappInfo.boardHeight);
//		drawing.gmmlVision.getShell().setSize(drawing.mappInfo.windowWidth, drawing.mappInfo.windowHeight);
		
		// Iterate over direct children of the root element
		Iterator it = root.getChildren().iterator();
		while (it.hasNext()) {
			mapElement((Element)it.next());
		}
	}
	
	/**
	 * validates a JDOM document against the xml-schema definition specified by 'xsdFile'
	 * @param doc the document to validate
	 */
	public static void validateDocument(Document doc) {
		// validate JDOM tree if xsd file exists
		if(xsdFile.canRead()) {
	
			Schema schema;
			try {
				SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				StreamSource ss = new StreamSource(xsdFile);
				schema = factory.newSchema(ss);
				ValidatorHandler vh =  schema.newValidatorHandler();
				SAXOutputter so = new SAXOutputter(vh);
				so.output(doc);
				// If no errors occur, the file is valid according to the gmml xml schema definition
				System.out.println("Document is valid according to the xml schema definition '" + 
						xsdFile.toString() + "'");
			} catch (SAXException se) {
				System.out.println("Could not parse the xml-schema definition: " + se.getMessage());
			} catch (JDOMException je) {
				System.out.println("Document is invalid according to the xml-schema definition!");
				System.out.println(je.getMessage());
			}
		} else {
			System.out.println("Document is not validated because the xml schema definition '" + 
					xsdFile.toString() + "' could not be found");
		}
	}
	
	/**
	 * Writes the JDom tree to the file specified
	 * @param file - the file as which the JDom tree should be saved
	 */
	public void writeToXML(File file) {
		try 
		{
			//Validate the JDOM document
			validateDocument(doc);
			//Get the XML code
			XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
			//Open a filewriter
			FileWriter writer = new FileWriter(file);
			//Send XML code to the filewriter
			xmlcode.output(doc, writer);
			System.out.println("File '" + file.toString() + "' is saved");
		}
		catch (IOException e) 
		{
			System.err.println(e);
		}
	}
}

// OLD GMML READER
//	private void readGMML(Object o, int depth)
//	{
//		// check wether the argument o is an xml Element
//		if (o instanceof Element)
//		{
//			// convert 0 to Element
//			Element element = (Element) o; 
//			System.out.println("Element: " + element.getName());
//			// now we know o is an xml Element, we can checkout 
//			// the pathway it is describing
//			readPathway(element);
//		}
//		
//		// check wether the argument o is  a document
//		else if (o instanceof Document)
//		{
//			// convert o to Document
//			Document doc = (Document) o;
//			System.out.println("Document");
//			
//			// The document is an .xml-document consisting of elements.
//			//	Get the elements and execute readGMML for each 
//			// element in the list obtained
//			List childelements = doc.getContent();
//			Iterator it = childelements.iterator();
//			while (it.hasNext())
//			{
//				Object child = it.next();
//				readGMML(child, depth ++);
//			}
//		}
//		else // worst case...
//		{
//			System.out.println("Unexpected type: " + o.getClass());
//		}
//		
//		System.out.println("Reached end of document");
//	} // end public void checkGMML(Object o, int depth)
//	
//	// Method to read pathway attributes; when the attributes are read, 
//	// the child elements are identified and added to the pathway object
//	private void readPathway(Element e)
//	{
//		String name = e.getName();
//
//		// The string array pathwayAttributes containes all known pathway attributes
//		String[] pathwayAttributes = {"name", "organism", "data-source", "version", "author",
//			"maintained-by", "email", "availbability", "last-modified"};
//
//		// check if we're reading a pathway
//		if (name.equalsIgnoreCase("pathway"))
//		{
//			System.out.println("Found a pathway, extracting data...");
//			
//			// Get the attributes of this pathway and check their validity
//			List attributes = e.getAttributes();
//			Iterator it = attributes.iterator();
//			
//			while (it.hasNext())
//			{
//				// check out next object
//				Object o = it.next();
//				if(o instanceof Attribute)
//				{
//					// the object is an pathway attribute
//					Attribute a = (Attribute) o;
//					String aName = a.getName();
//					String aValue = a.getValue();
//					// determine whether a is a known attribute
//					boolean  aknown = false;
//					for (int i = 0; ((i < pathwayAttributes.length) && (!aknown)); i++)
//					{
//						if (aName.equalsIgnoreCase(pathwayAttributes[i]))
//						{
//							// attribute is known
//							aknown = true;
//							// add attribute
////							drawing.addAttribute(a);
//						
//						} // end if
//					} // end for
//					if (!aknown)
//					{
//						// unknown attribute found
//						System.out.println("Ignored unknown an attribute! Attribute name: " + aName + "; value: " +  aValue);
//					} // end if
//					
//				} // end if(o instanceof Attribute)
//			} // end while (it.hasNext())
//			
//			// get child objects and readGMML for each of them
//			List children = e.getContent();
//			it = children.iterator();
//		
//			while (it.hasNext())
//			{
//				//check out next object
//				Object o = it.next();
//				readPathwayChilds(o, 1);
//			}
//			
//			// now, all pathway data is extracted
//			System.out.println("All data extracted, done...");
//			
//		} // end if (name.equalsIgnoreCase("pathway"))
//		
//		else // first object is not a pathway
//		{
//			System.out.println("Unsupported first level element found!");
//		}
//	} // end private void readPathway(Element e)
//
//	private void readPathwayChilds(Object o, int depth)
//	{
//		// check if the object is an xml Element
//		if (o instanceof Element)
//		{
//			Element e = (Element) o;
//			String eName = e.getName();
//			
//			// Check element name; if it corresponds to a known gmml element get element attributes
//			// Check the known gmml elements in this order: 
//			// Graphics, GeneProduct, Line, LineShape, Arc, Label, Shape, CellShape, Brace, CellComponent, ProteinComplex
//			
//			if("graphics".equalsIgnoreCase(eName))
//			{
//				int[] dims = checkGraphicsAttributes(e);
//				drawing.setSize(dims[0]/15, dims[1]/15);
//				drawing.setPreferredSize(new Dimension(dims[0]/15, dims[1]/15));
//	//			drawing.width = dims[0];
//	//			drawing.heigth = dims[1];
//				System.out.println("Dimensions set to " + dims[0] + ", " + dims[1]);
//			}
//			else if ("geneproduct".equalsIgnoreCase(eName))
//			{
//				checkGeneproductAttributes(e);		
//			}
//			else if ("line".equalsIgnoreCase(eName))
//			{
//				checkLineAttributes(e);
//			}
//			else if ("lineshape".equalsIgnoreCase(eName))
//			{
//				checkLineShapeAttributes(e);
//			}
//			else if ("arc".equalsIgnoreCase(eName))
//			{
//				checkArcAttributes(e);
//			}
//			else if ("label".equalsIgnoreCase(eName))
//			{
//				checkLabelAttributes(e);
//			}
//			else if ("shape".equalsIgnoreCase(eName))
//			{
//				checkShapeAttributes(e);			
//			}
//			else if ("cellshape".equalsIgnoreCase(eName))
//			{
//				checkCellShapeAttributes(e); // to be implemented!
//			}
//			else if ("brace".equalsIgnoreCase(eName))
//			{
//				checkBraceAttributes(e);
//			}
//			else if ("cellcomponent".equalsIgnoreCase(eName))
//			{
//				checkCellComponentAttributes(e);	// to be implemented!		
//			}			
//			else if ("proteincomplex".equalsIgnoreCase(eName))
//			{
//				checkProteinComplexAttributes(e);// to be implemented!			
//			}			
//						
//		} // end if (o instanceof Element)
//
//			
//			
//	} // end private void readPathwayChilds(Object o, int depth)
//	
//	// method to check graphics attributes
//	// graphics attributes are boardwidth and boardheight, which are returned
//	private int[] checkGraphicsAttributes(Element e)
//	{
//		List attributes = e.getAttributes();
//		Iterator it = attributes.iterator();
//		
//		int width = 0;
//		int height = 0;
//		
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//				
//				if ("boardwidth".equalsIgnoreCase(aName))
//				{
//					width = Integer.parseInt(aValue);
//				} // end if
//				else if ("boardheight".equalsIgnoreCase(aName))
//				{
//					height = Integer.parseInt(aValue);
//				} // end else if
//			} // end if
//		} // end while
//
//		int[] dim = {width, height};		
//		return dim;
//	} // private int[] checkGraphicsAttributes(Element e)
//
//	// method to check geneproduct attributes
//	private void checkGeneproductAttributes(Element e)
//	{
//		GmmlGeneProduct gp = new GmmlGeneProduct(drawing);
//		
//		List a = e.getAttributes();
//		Iterator it = a.iterator();
//		while(it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute at = (Attribute) o;
//				String aName = at.getName();
//				String aValue = at.getValue();
//				
//				if ("geneid".equalsIgnoreCase(aName))
//				{
//					gp.geneID = aValue;
//				}
//				else if ("type".equalsIgnoreCase(aName))
//				{
//					// to be implemented				
//				}
//				else if ("geneproduct-data-source".equalsIgnoreCase(aName))
//				{
//					// to be implemented
//				}
//				else if ("xref".equalsIgnoreCase(aName))
//				{
//					gp.xref = aValue;
//				}
//				else if ("backpagehead".equalsIgnoreCase(aName))
//				{
//					// to be implemented				
//				}
//			} // end if
//		} // end while
//		
//		// the geneproduct element contains more elements itself
//		// obtain child elements and check names
//		// possible child elements are graphics, comment and notes
//		
//		List child = e.getContent();
//		it = child.iterator();
//
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			
//			// check if child element is an xml Element
//			if (o instanceof Element)
//			{
//				Element el = (Element) o;
//				String eName = el.getName();
//				
//				if ("graphics".equalsIgnoreCase(eName))
//				{
//					checkGeneProductGraphicsAttributes(gp, el);
//				}
//				else if ("comment".equalsIgnoreCase(eName))
//				{
//					// to be implemented
//				}
//				else if ("notes".equalsIgnoreCase(eName))
//				{
//					// to be implemented
//				}
//			} // end if
//		} // end while
//		
//		
//	} // end private void checkGeneproductAttributes(Element e)
//	
//	
//	// method...
//	private void checkGeneProductGraphicsAttributes(GmmlGeneProduct gp, Element e)
//	{
//		int cx = 0;
//		int cy = 0;
//	
//			List alist = e.getAttributes();
//			Iterator it = alist.iterator();
//			while (it.hasNext())
//			{
//				Object o = it.next();
//				if (o instanceof Attribute)
//				{
//					Attribute a = (Attribute) o;
//					String aName = a.getName();
//					String aValue = a.getValue();
//					
//					if ("centerx".equalsIgnoreCase(aName))
//					{
//						gp.centerx = Integer.parseInt(aValue)/15;
//					}
//					else if ("centery".equalsIgnoreCase(aName))
//					{
//						gp.centery = Integer.parseInt(aValue)/15;
//					}
//					else if ("width".equalsIgnoreCase(aName))
//					{
//						gp.width = Double.parseDouble(aValue)/15;
//					}
//					else if ("height".equalsIgnoreCase(aName))
//					{
//						gp.height = Double.parseDouble(aValue)/15;
//					}
//				} // end if
//			} // end while
//			
//			// graphics wil be defined from left upper corner
//			
//			gp.constructRectangle();
//			gp.canvas = drawing;
//			// now, a gene product component can be added to the drawing
//			drawing.addElement(gp);
//			
//	} // end private void checkGeneProductSubGraphics(String ref, String geneID)
//
//	private void checkLineAttributes(Element e)
//	{
//		GmmlLine l = new GmmlLine(drawing);
//		
//		List alist = e.getAttributes();
//		Iterator it = alist.iterator();
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//				if ("style".equalsIgnoreCase(aName))
//				{
//					if ("solid".equalsIgnoreCase(aValue))
//					{
//						l.style = 0;						
//					}
//					else if ("broken".equalsIgnoreCase(aValue))
//					{
//						l.style = 1;
//					}
//				} // end if style
//				else if ("type".equalsIgnoreCase(aName))
//				{
//					if ("line".equalsIgnoreCase(aValue))
//					{
//						l.type = 0;
//					}
//					else if ("arrow".equalsIgnoreCase(aValue))
//					{
//						l.type = 1;
//					}					
//				} // end else if type
//			} // end if o instance of 
//		} // end while it.hasNext
//		
//		// a line element has sub elements; check them
//		List children = e.getContent();
//		it = children.iterator();
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Element)
//			{			
//				Element sube = (Element) o;
//				String subeName = sube.getName();
//				
//				if ("graphics".equalsIgnoreCase(subeName))
//				{	
//					checkLineGraphicsAttributes(l, sube);
//				} // end if "graphics..."
//				else if ("comment".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//				else if ("notes".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//			} // end if o instance of...
//		} // end while
//		
//	} // end private void checkLineAttributes(Element e)
//
//	private void checkLineGraphicsAttributes(GmmlLine l, Element e)
//	{
//	
//		List alist = e.getAttributes();
//		Iterator it = alist.iterator();
//		
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//										
//				if ("startx".equalsIgnoreCase(aName))
//				{
//					l.startx = Double.parseDouble(aValue)/15;
//				}
//				else if ("starty".equalsIgnoreCase(aName))
//				{
//					l.starty = Double.parseDouble(aValue)/15;
//				}
//				else if ("endx".equalsIgnoreCase(aName))
//		 		{
//					l.endx = Double.parseDouble(aValue)/15;
//				}
//				else if ("endy".equalsIgnoreCase(aName))
//				{
//					l.endy = Double.parseDouble(aValue)/15;
//				}
//				else if ("color".equalsIgnoreCase(aName))
//				{
//					l.color = GmmlColor.convertStringToColor(aValue);
//				}
//			} // end if 
//		}// end while
//		
//		// create a line in class GmmlLine
//		l.constructLine();
//		l.canvas = drawing;
//		// line attributes complete, add line to drawing
//		drawing.addElement(l);
////		v.updateUI();
//	} // end private void checkLineGraphicsAttributes()
//
//	private void checkLineShapeAttributes(Element e)
//	{
//		GmmlLineShape ls = new GmmlLineShape(drawing);
//		
//		List alist = e.getAttributes();
//		Iterator it = alist.iterator();
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//				if ("type".equalsIgnoreCase(aName))
//				{
//					if ("tbar".equalsIgnoreCase(aValue))
//					{
//						ls.type = 0;						
//					}
//					else if ("receptorbound".equalsIgnoreCase(aValue))
//					{
//						ls.type = 1;
//					}
//					else if ("ligandbound".equalsIgnoreCase(aValue))
//					{
//						ls.type = 2;
//					}
//					else if ("receptorsquare".equalsIgnoreCase(aValue))
//					{
//						ls.type = 3;
//					}
//					else if ("ligandsquare".equalsIgnoreCase(aValue))
//					{
//						ls.type = 4;
//					}
//				} // end if style
//			} // end if o instance of 
//		} // end while it.hasNext
//		
//		// a lineshape element has sub elements; check them
//		List children = e.getContent();
//		it = children.iterator();
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Element)
//			{			
//				Element sube = (Element) o;
//				String subeName = sube.getName();
//				
//				if ("graphics".equalsIgnoreCase(subeName))
//				{	
//					checkLineShapeGraphicsAttributes(ls, sube);
//				} // end if "graphics..."
//				else if ("comment".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//				else if ("notes".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//			} // end if o instance of...
//		} // end while
//	
//	} // end private void checkLineShapeAttributes(e)
//	
//	private void checkLineShapeGraphicsAttributes(GmmlLineShape ls, Element e)
//	{
//		List alist = e.getAttributes();
//		Iterator it = alist.iterator();
//		
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//										
//				if ("startx".equalsIgnoreCase(aName))
//				{
//					ls.startx = Double.parseDouble(aValue);
//				}
//				else if ("starty".equalsIgnoreCase(aName))
//				{
//					ls.starty = Double.parseDouble(aValue);
//				}
//				else if ("endx".equalsIgnoreCase(aName))
//		 		{
//					ls.endx = Double.parseDouble(aValue);
//				}
//				else if ("endy".equalsIgnoreCase(aName))
//				{
//					ls.endy = Double.parseDouble(aValue);
//				}
//				else if ("color".equalsIgnoreCase(aName))
//				{
//				ls.color = GmmlColor.convertStringToColor(aValue);
//				}
//			} // end if 
//		}// end while
//		
//		ls.canvas = drawing;
//		// line attributes complete, add lineshape to pathway
//		drawing.addElement(ls);
//
//	} // end private void checkLineShapeGraphicsAttributes(style, sube)
//
//	private void checkArcAttributes(Element e)
//	{
//		List childlist = e.getContent();
//		Iterator childit = childlist.iterator();
//		
//		while (childit.hasNext())
//		{
//			Object oc = childit.next();
//			if (oc instanceof Element)
//			{
//				Element el = (Element) oc;
//				String eName = el.getName();
//								
//				if ("graphics".equalsIgnoreCase(eName))
//				{
//					checkArcGraphicsAttributes(el);
//				} // end if graphics
//				else if ("comment".equalsIgnoreCase(eName)) 
//				{
//					// to be implemented
//				}//end if Comment
//				else if ("notes".equalsIgnoreCase(eName)) 
//				{	
//					// to be implemented
//				}
//			} // end if
//		} // end while
//			
//	} // end private void checkArcAttributes(Element e)
//
//	private void checkArcGraphicsAttributes(Element e)
//	{
//		GmmlArc arc = new GmmlArc(drawing);
//
//		List alist = e.getAttributes();
//		Iterator it = alist.iterator();
//		
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//										
//				if ("startx".equalsIgnoreCase(aName))
//				{
//					arc.x = Double.parseDouble(aValue);
//				}
//				else if ("starty".equalsIgnoreCase(aName))
//				{
//					arc.y = Double.parseDouble(aValue);
//				}
//				else if ("width".equalsIgnoreCase(aName))
//		 		{
//					arc.width = Double.parseDouble(aValue);
//				}
//				else if ("height".equalsIgnoreCase(aName))
//				{
//					arc.height = Double.parseDouble(aValue);
//				}
//				else if ("color".equalsIgnoreCase(aName))
//				{
//					arc.color = GmmlColor.convertStringToColor(aValue);
//				}
//				else if ("rotation".equalsIgnoreCase(aName))
//				{
//					arc.rotation = Double.parseDouble(aValue);
//				}				
//			} // end if 
//		}// end while
//		
//		arc.constructArc();
//		arc.canvas = drawing;
//		// arc attributes complete, add arc to pathway
//		drawing.addElement(arc);
//	} // end private void checkArcGraphicsAttributes(Element e)
//											
//	private void checkLabelAttributes(Element e)
//	{
//		GmmlLabel l = new GmmlLabel(drawing);
//		
//		List alist = e.getAttributes();
//		Iterator it = alist.iterator();
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//				if ("textlabel".equalsIgnoreCase(aName))
//				{
//					l.text = aValue;
//				} // end if 
//			} // end if o instance of 
//		} // end while it.hasNext
//
//		// a label element has sub elements; check them
//		List children = e.getContent();
//		it = children.iterator();
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Element)
//			{			
//				Element sube = (Element) o;
//				String subeName = sube.getName();
//				
//				if ("graphics".equalsIgnoreCase(subeName))
//				{	
//					checkLabelGraphicsAttributes(l, sube);
//				} // end if "graphics..."
//				else if ("comment".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//				else if ("notes".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//			} // end if o instance of...
//		} // end while
//				
//	} // end private void checkLabelAttributes(Element e)
//	
//	private void checkLabelGraphicsAttributes(GmmlLabel l, Element e)
//	{
//		int cx = 0;
//		int cy = 0;
//			
//		List alist = e.getAttributes();
//		Iterator it = alist.iterator();
//		
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//										
//				if ("centerx".equalsIgnoreCase(aName))
//				{
//					l.centerx = (int)Integer.parseInt(aValue)/15;
//				}
//				else if ("centery".equalsIgnoreCase(aName))
//				{
//					l.centery = (int)Integer.parseInt(aValue)/15;
//				}
//				else if ("width".equalsIgnoreCase(aName))
//		 		{
//					l.width = (int)Integer.parseInt(aValue)/15;
//				}
//				else if ("height".equalsIgnoreCase(aName))
//				{
//					l.height = (int)Integer.parseInt(aValue)/15;
//				}
//				else if ("color".equalsIgnoreCase(aName))
//				{
//					l.color = GmmlColor.convertStringToColor(aValue);
//				}
//				else if ("fontname".equalsIgnoreCase(aName))
//				{
//					l.font = aValue;
//				}
//				else if ("fontstyle".equalsIgnoreCase(aName))
//				{
//					l.fontStyle = aValue;
//				}
//				else if ("fontweight".equalsIgnoreCase(aName))
//				{
//					l.fontWeight = aValue;
//				}
//				else if ("fontsize".equalsIgnoreCase(aName))
//				{
//					l.fontSize = Integer.parseInt(aValue);
//				}				
//			} // end if 
//		}// end while
//		
//		l.canvas = drawing;
//
//		// label attributes complete, add label to pathway
//		drawing.addElement(l);
//		
//	}
//	
//	private void checkShapeAttributes(Element e)
//	{
//		GmmlShape s = new GmmlShape(drawing);
//		
//		List alist = e.getAttributes();
//		Iterator it = alist.iterator();
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//				
//				if ("type".equalsIgnoreCase(aName))
//				{
//					if ("rectangle".equalsIgnoreCase(aValue))
//					{
//						s.type = 0;
//					}
//					else if ("oval".equalsIgnoreCase(aValue))
//					{
//						s.type = 1;
//					}
//				} // end if 
//			} // end if o instance of 
//		} // end while it.hasNext
//
//		// a shape element has sub elements; check them
//		List children = e.getContent();
//		it = children.iterator();
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Element)
//			{			
//				Element sube = (Element) o;
//				String subeName = sube.getName();
//				
//				if ("graphics".equalsIgnoreCase(subeName))
//				{	
//					checkShapeGraphicsAttributes(s, sube);
//				} // end if "graphics..."
//				else if ("comment".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//				else if ("notes".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//			} // end if o instance of...
//		} // end while
//		
//	} // end private void checkShapeAttributes(Element e)
//
//	private void checkShapeGraphicsAttributes(GmmlShape s,  Element e)
//	{
//			
//		List alist = e.getAttributes();
//		Iterator it = alist.iterator();
//		
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//										
//				if ("centerx".equalsIgnoreCase(aName))
//				{
//					s.centerx = Double.parseDouble(aValue)/15;
//				}
//				else if ("centery".equalsIgnoreCase(aName))
//				{
//					s.centery = Double.parseDouble(aValue)/15;
//				}
//				else if ("width".equalsIgnoreCase(aName))
//		 		{
//					s.width = Double.parseDouble(aValue)/15;
//				}
//				else if ("height".equalsIgnoreCase(aName))
//				{
//					s.height = Double.parseDouble(aValue)/15;
//				}
//				else if ("color".equalsIgnoreCase(aName))
//				{
//					s.color = GmmlColor.convertStringToColor(aValue);
//				}
//				else if ("rotation".equalsIgnoreCase(aName))
//				{
//					s.rotation = Double.parseDouble(aValue);
//				}				
//			} // end if 
//		}// end while
//
//		s.canvas = drawing;
//		
//		// shape attributes complete, add shape component to drawing
//		drawing.addElement(s);
//
//	} // end private void checkShapeGraphicsAttributes(int type, Element e)
//
//	private void checkCellShapeAttributes(Element e)
//	{
//		// to be implemented!	
//	} // end private void checkCellShapeAttributes(Element e)
//	
//	private void checkBraceAttributes(Element e)
//	{
//		GmmlBrace b = new GmmlBrace();
//		// a brace element has sub elements; check them
//		List children = e.getContent();
//		Iterator it = children.iterator();
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Element)
//			{			
//				Element sube = (Element) o;
//				String subeName = sube.getName();
//				
//				if ("graphics".equalsIgnoreCase(subeName))
//				{	
//					checkBraceGraphicsAttributes(b, sube);
//				} // end if "graphics..."
//				else if ("comment".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//				else if ("notes".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//			} // end if o instance of...
//		} // end while
//
//	} // end private void checkBraceAttributes(Element e)
//
//	private void checkBraceGraphicsAttributes(GmmlBrace b, Element e)
//	{
//		double cx = 0;
//		double cy = 0;
//		double width = 0;
//		double height = 0;
//		double picpointOffset = 0;
//		
//		int orientation = 0;
//			
//		String color = "";
//		
//		List alist = e.getAttributes();
//		Iterator it = alist.iterator();
//		
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//										
//				if ("centerx".equalsIgnoreCase(aName))
//				{
//					b.cX = Double.parseDouble(aValue);
//				}
//				else if ("centery".equalsIgnoreCase(aName))
//				{
//					b.cY = Double.parseDouble(aValue);
//				}
//				else if ("width".equalsIgnoreCase(aName))
//		 		{
//					b.width = Double.parseDouble(aValue);
//				}
//				else if ("color".equalsIgnoreCase(aName))
//				{
//					b.color = GmmlColor.convertStringToColor(aValue);
//				}
//				else if ("picpointoffset".equalsIgnoreCase(aName))
//				{
//					b.ppo = Double.parseDouble(aValue);
//				}
//				else if ("orientation".equalsIgnoreCase(aName))
//				{
//					if ("top".equalsIgnoreCase(aValue))
//					{
//						b.orientation = 0;
//					}
//					else if ("right".equalsIgnoreCase(aValue))
//					{
//						b.orientation = 1;
//					}
//					else if ("bottom".equalsIgnoreCase(aValue))
//					{
//						b.orientation = 2;
//					}
//					else if ("top".equalsIgnoreCase(aValue))
//					{	
//						b.orientation = 3;
//					}
//				}									
//			} // end if 
//		}// end while
//
//		// brace attributes complete, add arc component to drawing
////2DO	drawing.add()
//
//	} // end private void checkBraceGraphicsAttributes(Element e)
//	
//	
//	private void checkCellComponentAttributes(Element e)
//	{
//		int type = 0;
//		
//		List alist = e.getAttributes();
//		Iterator it = alist.iterator();
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Attribute)
//			{
//				Attribute a = (Attribute) o;
//				String aName = a.getName();
//				String aValue = a.getValue();
//				
//				if ("type".equalsIgnoreCase(aName))
//				{
//					if ("organc".equalsIgnoreCase(aValue))
//					{
//						type = 0;
//					}
//					else if ("organb".equalsIgnoreCase(aValue))
//					{
//						type = 1;
//					}
//					else if ("organc".equalsIgnoreCase(aValue))
//					{
//						type = 2;
//					}
//					else if ("ribosome".equalsIgnoreCase(aValue))
//					{
//						type = 2;
//					}
//				} // end if 
//			} // end if o instance of 
//		} // end while it.hasNext
//
//		// a label element has sub elements; check them
//		List children = e.getContent();
//		it = children.iterator();
//		while (it.hasNext())
//		{
//			Object o = it.next();
//			if (o instanceof Element)
//			{			
//				Element sube = (Element) o;
//				String subeName = sube.getName();
//				
//				if ("graphics".equalsIgnoreCase(subeName))
//				{	
//					checkCellComponentGraphicsAttributes(type, sube);
//				} // end if "graphics..."
//				else if ("comment".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//				else if ("notes".equalsIgnoreCase(subeName))
//				{
//					// to be implemented
//				}
//			} // end if o instance of...
//		} // end while
//
//	} // end private void checkCellComponentAttributes(Element e)
//
//	private void checkCellComponentGraphicsAttributes(int type, Element e)
//	{
//		// to be implemented
//		// cellshape object has not been implemented yet!	
//		
//	} // end private void checkCellComponentGraphicsAttributes(int type, Element e)
//	
//	private void checkProteinComplexAttributes(Element e)
//	{
//		// to be implemented!
//		// proteincomplex object has not been implemented yet!
//	} // end private void checkProteinComplexAttributes(Element e)
//	
//} // end of class GmmlReader2
