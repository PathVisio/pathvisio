package data;

import gmmlVision.GmmlVision;
import gmmlVision.GmmlVisionMain;
import gmmlVision.GmmlVisionWindow;
import graphics.GmmlDrawing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

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
	 * factor to convert screen cordinates used in GenMAPP to pixel cordinates
	 * NOTE: maybe it is better to adapt gmml to store cordinates as pixels and
	 * divide the GenMAPP cordinates by this factor on conversion
	 */
	final public static int GMMLZOOM = 15;
	/**
	 * file containing the gmml schema definition
	 */
	final private static String xsdFile = "GMML_compat.xsd";
	
	public List<GmmlDataObject> dataObjects = new ArrayList<GmmlDataObject>();
	
	private File xmlFile;
	/**
	 * Gets the xml file containing the Gmml pathway currently displayed
	 * @return
	 */
	public File getXmlFile () { return xmlFile; }
	public void setXmlFile (File file) { xmlFile = file; }

	/**
	 * Contructor for this class, creates a new gmml document
	 * @param drawing {@link GmmlDrawing} that displays the visual representation of the gmml pathway
	 */
	public GmmlData() 
	{		
	}
	
	/*
	 * Call when making a new mapp.
	 */
	public void initMappInfo()
	{
		GmmlVisionWindow window = GmmlVision.getWindow();

		GmmlDataObject mapInfo = new GmmlDataObject();
		mapInfo.setObjectType(ObjectType.MAPPINFO);
		if (window.sc != null)
		{
			mapInfo.setBoardWidth(window.sc.getSize().x);
			mapInfo.setBoardHeight(window.sc.getSize().y);
			mapInfo.setWindowWidth(window.getShell().getSize().x);
			mapInfo.setWindowHeight(window.getShell().getSize().y);
		}
		mapInfo.setMapInfoName("New Pathway");
		dataObjects.add(mapInfo);
	}
		
	/**
	 * Constructor for this class, opens a gmml pathway and adds its elements to the drawing
	 * @param file		String pointing to the gmml file to open
	 * @param drawing	{@link GmmlDrawing} that displays the visual representation of the gmml pathway
	 */
	public GmmlData(String file) throws Exception
	{
		// Start XML processing
		GmmlVision.log.info("Start reading the Gmml file: " + file);
		// try to read the file; if an error occurs, catch the exception and print feedback

		xmlFile = new File(file);
		readFromXml(xmlFile, true);		
	}
	
	/**
	 * Maps the element specified to a GmmlGraphics object
	 * @param e		the JDOM {@link Element} to map
	 */
	private void mapElement(Element e) {
		GmmlDataObject o = GmmlDataObject.mapComplete(e);
		if(o != null) dataObjects.add(o);
	}

	/**
	 * validates a JDOM document against the xml-schema definition specified by 'xsdFile'
	 * @param doc the document to validate
	 */
	public static void validateDocument(Document doc) {
		
		ClassLoader cl = GmmlVisionMain.class.getClassLoader();
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
				// If no errors occur, the file is valid according to the gmml xml schema definition
				//TODO: open dialog to report error
				GmmlVision.log.info("Document is valid according to the xml schema definition '" + 
						xsdFile.toString() + "'");
			} catch (SAXException se) {
				GmmlVision.log.error("Could not parse the xml-schema definition", se);
			} catch (JDOMException je) {
				GmmlVision.log.error("Document is invalid according to the xml-schema definition!: " + 
						je.getMessage(), je);
			}
		} else {
			GmmlVision.log.info("Document is not validated because the xml schema definition '" + 
					xsdFile + "' could not be found in classpath");
		}
	}
	
	/**
	 * Writes the JDOM document to the file specified
	 * @param file	the file to which the JDOM document should be saved
	 */
	public void writeToXML(File file, boolean validate) {
		try 
		{			
			Document doc = GmmlFormat.createJdom(this);
			
			//Validate the JDOM document
			if (validate) validateDocument(doc);
			//			Get the XML code
			XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
			Format f = xmlcode.getFormat();
			f.setEncoding("ISO-8859-1");
			f.setTextMode(Format.TextMode.PRESERVE);
			xmlcode.setFormat(f);
			
			//Open a filewriter
			FileWriter writer = new FileWriter(file);
			//Send XML code to the filewriter
			xmlcode.output(doc, writer);
		}
		catch (IOException e) 
		{
			GmmlVision.log.error("Unable to save file " + file + ": " + e.getMessage(), e);
		}
		catch (ConverterException e)
		{
			GmmlVision.log.error("Unable to convert to GMML, file: " + file + ": " + e.getMessage(), e);
		}
	}
	
	public void readFromXml(File file, boolean validate)
	{
		// Start XML processing
		GmmlVision.log.info("Start reading the XML file: " + file);
		SAXBuilder builder  = new SAXBuilder(false); // no validation when reading the xml file
		// try to read the file; if an error occurs, catch the exception and print feedback
		try
		{
			// build JDOM tree
			Document doc = builder.build(file);

			if (validate) validateDocument(doc);
			
			// Copy the pathway information to a GmmlDrawing
			Element root = doc.getRootElement();
			
			mapElement(root); // MappInfo
			
			// Iterate over direct children of the root element
			Iterator it = root.getChildren().iterator();
			while (it.hasNext()) {
				mapElement((Element)it.next());
			}
		}
		catch(JDOMParseException pe) 
		{
			 GmmlVision.log.error(pe.getMessage());
		}
		catch(JDOMException e)
		{
			GmmlVision.log.error(file + " is invalid.");
			GmmlVision.log.error(e.getMessage());
		}
		catch(IOException e)
		{
			GmmlVision.log.error("Could not access " + file);
			GmmlVision.log.error(e.getMessage());
		}
	}
	
	public void readFromMapp (File file) throws ConverterException, SQLException, ClassNotFoundException
	{
        String inputString = file.getAbsolutePath();

        MappFormat.readFromMapp (inputString, this);
	}
	
	
	public void writeToMapp (File file) throws ConverterException
	{
		String[][] mappInfo = MappFormat.uncopyMappInfo (this);
		List<String[]> mappObjects = MappFormat.uncopyMappObjects (this);
		
		MappFormat.exportMapp (file.getAbsolutePath(), mappInfo, mappObjects);		
	}

	private List<GmmlListener> listeners = new ArrayList<GmmlListener>();
	public void addListener(GmmlListener v) { listeners.add(v); }
	public void removeListener(GmmlListener v) { listeners.remove(v); }
	public void fireObjectModifiedEvent(GmmlEvent e) 
	{
		for (GmmlListener g : listeners)
		{
			g.gmmlObjectModified(e);
		}
	}	
}
