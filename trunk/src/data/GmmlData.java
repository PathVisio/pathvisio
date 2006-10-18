package data;

import gmmlVision.GmmlVision;
import gmmlVision.GmmlVisionMain;
import gmmlVision.GmmlVisionWindow;
import graphics.GmmlDrawing;

import java.io.*;
import java.util.*;

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
	 * name of resource containing the gmml schema definition
	 */
	final private static String xsdFile = "GMML_compat.xsd";
	
	/**
	 * List of contained dataObjects
	 */
	public List<GmmlDataObject> dataObjects = new ArrayList<GmmlDataObject>();
	
	/**
	 * Getter for dataobjects contained. There is no setter, you
	 * have to add dataobjects individually
	 * @return
	 */
	public List<GmmlDataObject> getDataObjects() 
	{
		return dataObjects;
	}
	
	/**
	 * Add dataObject; You don't need to call this
	 * explicitly, because this is called automatically
	 * when setting the parent.
	 * @param o The object to add
	 */
	public void addDataObject (GmmlDataObject o)
	{
		dataObjects.add(o);
	}
	
	/**
	 * You don't need to call this explicitly,
	 * GmmlDataObjects are automatically removed when
	 * changing its parent through setParent. 
	 * @param o the object to remove
	 */
	public void removeDataObject (GmmlDataObject o)
	{
		dataObjects.remove(o);
	}
	
	/**
	 * Stores references of line endpoints to other objects
	 */
	private HashMap<String, List<GmmlDataObject>> graphRefs = new HashMap<String, List<GmmlDataObject>>();
	public void addRef (String ref, GmmlDataObject target)
	{
		if (graphRefs.containsKey(ref))
		{
			List<GmmlDataObject> l = graphRefs.get(ref);
			l.add(target);
		}
		else
		{
			List<GmmlDataObject> l = new ArrayList<GmmlDataObject>();
			l.add(target);		
			graphRefs.put(ref, l);
		}
	}
	
	public void removeRef (String ref, GmmlDataObject target)
	{
		if (!graphRefs.containsKey(ref)) throw new IllegalArgumentException();
		
		graphRefs.get(ref).remove(target);
		if (graphRefs.get(ref).size() == 0)
			graphRefs.remove(ref);
	}
	
	/**
	 * Returns all lines that refer to an object with a particular graphId.
	 */
	public List<GmmlDataObject> getReferringObjects (String id)
	{
		return graphRefs.get(id);
	}
	
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

		GmmlDataObject mapInfo = new GmmlDataObject(ObjectType.MAPPINFO);
		mapInfo.setParent(this);
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
	public GmmlData(String file) throws ConverterException
	{
		// Start XML processing
		GmmlVision.log.info("Start reading the Gmml file: " + file);
		// try to read the file; if an error occurs, catch the exception and print feedback

		xmlFile = new File(file);
		readFromXml(xmlFile, true);		
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
	public void writeToXml(File file, boolean validate) throws ConverterException 
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
		try
		{
			FileWriter writer = new FileWriter(file);
			//Send XML code to the filewriter
			xmlcode.output(doc, writer);
		}
		catch (IOException ie)
		{
			ConverterException ce = new ConverterException("IO Exception while converting");
			ce.setStackTrace(ie.getStackTrace());
			throw ce;
		}
	}
	
	public void readFromXml(File file, boolean validate) throws ConverterException
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
			
			GmmlFormat.mapElement(root, this); // MappInfo
			
			// Iterate over direct children of the root element
			Iterator it = root.getChildren().iterator();
			while (it.hasNext()) {
				GmmlFormat.mapElement((Element)it.next(), this);
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
	
	public void readFromMapp (File file) throws ConverterException
	{
        String inputString = file.getAbsolutePath();

        MappFormat.readFromMapp (inputString, this);
	}
	
	public void writeToMapp (File file) throws ConverterException
	{
		String[] mappInfo = MappFormat.uncopyMappInfo (this);
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
