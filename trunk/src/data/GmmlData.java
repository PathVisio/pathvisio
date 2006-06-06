package data;

import graphics.GmmlDrawing;
import graphics.GmmlGraphics;
import graphics.GmmlMappInfo;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.eclipse.jface.dialogs.MessageDialog;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.SAXOutputter;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
*	This class handles GMML file IO and keeps a JDOM representation of the GMML document
*/
public class GmmlData
{
	/**
	 * factor to convert screen coördinates used in GenMAPP to pixel coördinates
	 * NOTE: maybe it is better to adapt gmml to store coördinates as pixels and
	 * divide the GenMAPP coördinates by this factor on conversion
	 */
	final public static int GMMLZOOM = 15;
	/**
	 * file containing the gmml schema definition
	 */
	final private static File xsdFile = new File("GMML_compat.xsd");
	
	private File xmlFile;
	/**
	 * Gets the xml file containing the Gmml pathway currently displayed
	 * @return
	 */
	public File getXmlFile () { return xmlFile; }
	
	private GmmlDrawing drawing;
	/**
	 * JDOM representation of the gmml pathway currently loaded
	 */
	public Document doc;
	
	/**
	 * Contructor for this class, creates a new gmml document
	 * @param drawing {@link GmmlDrawing} that displays the visual representation of the gmml pathway
	 */
	public GmmlData(GmmlDrawing drawing) 
	{
		this.drawing = drawing;
		doc = new Document();
		//Set the root element (pathway) and its graphics
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
		drawing.mappInfo.mapInfoLeft = drawing.mappInfo.mapInfoTop = 0;
	}
	
	/**
	 * Constructor for this class, opens a gmml pathway and adds its elements to the drawing
	 * @param file		String pointing to the gmml file to open
	 * @param drawing	{@link GmmlDrawing} that displays the visual representation of the gmml pathway
	 */
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
			 MessageDialog.openError (drawing.gmmlVision.getShell(), "Error", 
				"Parse error: " + pe.getMessage());
		}
		catch(JDOMException e)
		{
			System.out.println(file + " is invalid.");
			System.out.println(e.getMessage());
			 MessageDialog.openError (drawing.gmmlVision.getShell(), "Error", 
						"JDOM exception: " + e.getMessage());
		}
		catch(IOException e)
		{
			System.out.println("Could not access " + file);
			System.out.println(e.getMessage());
			MessageDialog.openError (drawing.gmmlVision.getShell(), "Error", 
						"IOException: " + e.getMessage());
		}
		catch(Exception e)
		{
			System.out.println("Error: " + e.getMessage());
			MessageDialog.openError (drawing.gmmlVision.getShell(), "Error", 
						"Exception: " + e.getMessage());
		}
	}
	

	/**
	 * Method to get the private property drawing
	 * @return drawing
	 */
	public GmmlDrawing getDrawing()
	{
		return drawing;
	}
	
	/**
	 * Maps the element specified to a GmmlGraphics object
	 * @param e		the JDOM {@link Element} to map
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
	 * Maps the contents of the JDOM tree to a GmmlDrawing
	 */
	public void toGmmlGraphics() {
		// Get the pathway element
		Element root = doc.getRootElement();
		drawing.setMappInfo(new GmmlMappInfo(root));
		
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
	 * Writes the JDOM document to the file specified
	 * @param file	the file to which the JDOM document should be saved
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
