package data;

import gmmlVision.GmmlVision;
import gmmlVision.GmmlVisionMain;
import gmmlVision.GmmlVisionWindow;
import graphics.GmmlDrawing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
* This class is the model for pathway data. It is responsible for
* storing all information necessary for maintaining, loading and saving
* pathway data.
* 
* GmmlData contains multiple GmmlDataObjects. GmmlData is guaranteed
* to always have exactly one object of the type MAPPINFO.
*/
public class GmmlData
{
	/**
	 * factor to convert screen cordinates used in GenMAPP to pixel cordinates
	 * NOTE: maybe it is better to adapt gpml to store cordinates as pixels and
	 * divide the GenMAPP cordinates by this factor on conversion
	 */
	final public static int GMMLZOOM = 15;
	
	/**
	 * name of resource containing the gpml schema definition
	 */
	final private static String xsdFile = "GPML.xsd";
	
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
	
	private GmmlDataObject mappInfo = null;
	
	/**
	 * get the one and only MappInfo object.
	 * There is no setter, a MappInfo object is automatically
	 * created in the constructor.
	 * 
	 * @return a GmmlDataObject with ObjectType set to mappinfo.
	 */
	public GmmlDataObject getMappInfo()
	{
		return mappInfo;
	}
	
	/**
	 * Add dataObject; You shouldn't to call this
	 * directly, this can only be called by GmmlDataObject.setParent()
	 * 
	 * fires GmmlEvent.ADDED event <i>after</i> addition of the object
	 * 
	 * @param o The object to add
	 */
	public void addDataObject (GmmlDataObject o)
	{
		if (o.getObjectType() == ObjectType.MAPPINFO && o != mappInfo)
			throw new IllegalArgumentException("Can't add more mappinfo objects");
		dataObjects.add(o);
		fireObjectModifiedEvent(new GmmlEvent(o, GmmlEvent.ADDED));
	}
	
	/**
	 * You shouldn't call this directly,
	 * GmmlDataObjects are automatically removed when
	 * changing its parent through setParent.
	 *
	 * fires GmmlEvent.DELETED event <i>before</i> removal of the object
	 *  
	 * @param o the object to remove
	 */
	public void removeDataObject (GmmlDataObject o)
	{
		if (o.getObjectType() == ObjectType.MAPPINFO)
			throw new IllegalArgumentException("Can't remove mappinfo object!");
		fireObjectModifiedEvent(new GmmlEvent(o, GmmlEvent.DELETED));
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
	
	private File sourceFile = null;
	
	/**
	 * Gets the xml file containing the Gpml/mapp pathway currently displayed
	 * @return
	 */
	public File getSourceFile () { return sourceFile; }
	private void setSourceFile (File file) { sourceFile = file; }

	/**
	 * Contructor for this class, creates a new gpml document
	 * @param drawing {@link GmmlDrawing} that displays the visual representation of the gpml pathway
	 */
	public GmmlData() 
	{
		mappInfo = new GmmlDataObject(ObjectType.MAPPINFO);
		mappInfo.setParent(this);
	}
	
	/*
	 * Call when making a new mapp.
	 */
	public void initMappInfo()
	{
		GmmlVisionWindow window = GmmlVision.getWindow();
		if (window.sc != null)
		{
			mappInfo.setBoardWidth(window.sc.getSize().x);
			mappInfo.setBoardHeight(window.sc.getSize().y);
			mappInfo.setWindowWidth(window.getShell().getSize().x);
			mappInfo.setWindowHeight(window.getShell().getSize().y);
		}
		mappInfo.setMapInfoName("New Pathway");
	}
		
	/**
	 * Constructor for this class, opens a gpml pathway and adds its elements to the drawing
	 * @param file		String pointing to the gpml file to open
	 * @param drawing	{@link GmmlDrawing} that displays the visual representation of the gpml pathway
	 * 
	 * @deprecated - use general constructor, then specify readFromXml or readFromMapp
	 */
	public GmmlData(String file) throws ConverterException
	{
		// Start XML processing
		GmmlVision.log.info("Start reading the Gpml file: " + file);
		// try to read the file; if an error occurs, catch the exception and print feedback

		sourceFile = new File(file);
		readFromXml(sourceFile, true);		
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
				// If no errors occur, the file is valid according to the gpml xml schema definition
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
			setSourceFile (file);
		}
		catch (IOException ie)
		{
			throw new ConverterException(ie);
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
			
			setSourceFile (file);
		}
		catch(JDOMParseException pe) 
		{
			 throw new ConverterException (pe);
		}
		catch(JDOMException e)
		{
			throw new ConverterException (e);
		}
		catch(IOException e)
		{
			throw new ConverterException (e);
		}
	}
	
	public void readFromMapp (File file) throws ConverterException
	{
        String inputString = file.getAbsolutePath();

        MappFormat.readFromMapp (inputString, this);
        
        setSourceFile (file);
	}
	
	public void writeToMapp (File file) throws ConverterException
	{
		String[] mappInfo = MappFormat.uncopyMappInfo (this);
		List<String[]> mappObjects = MappFormat.uncopyMappObjects (this);
		
		MappFormat.exportMapp (file.getAbsolutePath(), mappInfo, mappObjects);
		setSourceFile (file);
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
	
	/**
	 * Get the systemcodes of all genes in this pathway
	 * @return	{@link ArrayList<String>} containing a systemcode for every gene on the mapp
	 */
	public ArrayList<String> getSystemCodes()
	{
		ArrayList<String> systemCodes = new ArrayList<String>();
		for(GmmlDataObject o : dataObjects)
		{
			if(o.getObjectType() == ObjectType.GENEPRODUCT)
			{
				systemCodes.add(o.getSystemCode());
			}
		}
		return systemCodes;
	}

}
