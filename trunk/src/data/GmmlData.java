package data;

import gmmlVision.GmmlVision;
import gmmlVision.GmmlVisionWindow;
import graphics.GmmlDrawing;
import graphics.GmmlMappInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
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
	final private static File xsdFile = new File("GMML_compat.xsd");
	
	private File xmlFile;
	/**
	 * Gets the xml file containing the Gmml pathway currently displayed
	 * @return
	 */
	public File getXmlFile () { return xmlFile; }
	public void setXmlFile (File file) { xmlFile = file; }
	
	private GmmlDrawing drawing;

	private Document doc;
	/**
	 * JDOM representation of the gmml pathway
	 */
	public Document getDocument() { return doc; }
		
	/**
	 * Contructor for this class, creates a new gmml document
	 * @param drawing {@link GmmlDrawing} that displays the visual representation of the gmml pathway
	 */
	public GmmlData(GmmlDrawing drawing) 
	{
		doc = new Document();
		//Set the root element (pathway) and its graphics
		Element root = new Element("Pathway");
		Element graphics = new Element("Graphics");
		root.addContent(graphics);
		root.addContent(new Element("InfoBox"));
		doc.setRootElement(root);
		
		this.drawing = drawing;
		GmmlVisionWindow window = GmmlVision.getWindow();
		GmmlMappInfo mpi = new GmmlMappInfo(drawing, doc.getRootElement());		
		mpi.setBoardSize(window.sc.getSize());
		mpi.setWindowSize(window.getShell().getSize());
		mpi.setName("New Pathway");
	}
		
	/**
	 * Constructor for this class, opens a gmml pathway and adds its elements to the drawing
	 * @param file		String pointing to the gmml file to open
	 * @param drawing	{@link GmmlDrawing} that displays the visual representation of the gmml pathway
	 */
	public GmmlData(String file, GmmlDrawing drawing) throws Exception
	{
		// Create the drawing
		this.drawing = drawing;
		// Start XML processing
		GmmlVision.log.info("Start reading the Gmml file: " + file);
		SAXBuilder builder  = new SAXBuilder(false); // no validation when reading the xml file
		// try to read the file; if an error occurs, catch the exception and print feedback

		xmlFile = new File(file);
		readFromXml(xmlFile);
		// build JDOM tree
		// Validate the JDOM document
		validateDocument(doc);
		// Copy the pathway information to a GmmlDrawing
		toGmmlGraphics();
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
	private void mapElement(Element e) {
		// Check if a GmmlGraphics exists for this element
		// Assumes that classname = 'Gmml' + Elementname
		try {
			Class cl = Class.forName("graphics.Gmml"+e.getName());
			Constructor con = cl.getConstructor(new Class[] { Element.class, GmmlDrawing.class });
			GmmlVision.log.trace("Mapping gmml element " + e.getName());
			Object obj = con.newInstance(new Object[] { e, drawing });
		}
		catch (ClassNotFoundException cnfe)
		{
			GmmlVision.log.error(e.getName() + " could not be mapped", cnfe);
		}
		catch (NoSuchMethodException nsme)
		{
			GmmlVision.log.trace("The GmmlGraphics class representing '" + e.getName() + 
					"' has no constructor for a JDOM element");
		}
		catch (Exception ex)
		{
			GmmlVision.log.error("while mapping gmml elements: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Maps the contents of the JDOM tree to a GmmlDrawing
	 */
	public void toGmmlGraphics() {
		// Get the pathway element
		Element root = doc.getRootElement();
		drawing.setMappInfo(new GmmlMappInfo(drawing, root));
		
		drawing.setSize(drawing.getMappInfo().getBoardSize());
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
		}
		catch (IOException e) 
		{
			GmmlVision.log.error("Unable to save file " + file + ": " + e.getMessage(), e);
		}
	}
	
	public void readFromXml(File file)
	{
		// Start XML processing
		GmmlVision.log.info("Start reading the XML file: " + file);
		SAXBuilder builder  = new SAXBuilder(false); // no validation when reading the xml file
		// try to read the file; if an error occurs, catch the exception and print feedback
		try
		{
			// build JDOM tree
			doc = builder.build(file);
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
	
	public void readFromMapp (File file) throws ConverterException
	{
        String inputString = file.getAbsolutePath();

        String[][] mappObjects = MappFile.importMAPPObjects(inputString);
        String[][] mappInfo = MappFile.importMAPPInfo(inputString);

        // Copy the info table to the new gmml pathway
        
        // Copy the objects table to the new gmml pahtway
    	MappToGmml.copyMappInfo(mappInfo, doc);
        MappToGmml.copyMappObjects(mappObjects, doc);        	
	}
	
	public void writeToMapp (File file) throws ConverterException
	{
		String[][] mappInfo = MappToGmml.uncopyMappInfo (doc);
		List mappObjects = MappToGmml.uncopyMappObjects (doc);
		
		MappFile.exportMapp (file.getAbsolutePath(), mappInfo, mappObjects);		
	}
	
	public final static String[] systemCodes = new String[] 	{ 
		"D", "F", "G", "I", "L", "M",
		"Q", "R", "S", "T", "U",
		"W", "Z", "X", "O"
	};
	
	public final static String[] systemNames = new String[] {
		"SGD", "FlyBase", "GenBank", "InterPro" ,"LocusLink", "MGI",
		"RefSeq", "RGD", "SwissProt", "GeneOntology", "UniGene",
		"WormBase", "ZFIN", "Affy", "Other"
	};
	
	/**
	 * {@link HashMap} containing mappings from system name (as used in Gmml) to system code
	 */
	public static final HashMap<String,String> sysName2Code = initSysName2Code();

	/**
	 * Initializes the {@link HashMap} containing the mappings between system name (as used in gmml)
	 * and system code
	 */
	private static HashMap<String, String> initSysName2Code()
	{
		HashMap<String, String> sn2c = new HashMap<String,String>();
		for(int i = 0; i < systemNames.length; i++)
			sn2c.put(systemNames[i], systemCodes[i]);
		return sn2c;
	}
}
