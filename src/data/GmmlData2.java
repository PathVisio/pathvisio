package data;

import java.io.*;
import java.util.*;

import javax.xml.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.xml.sax.SAXException;

import debug.Logger;

/**
 * GmmlData is responsible for exactly representing all data in GMML files.
 * It has facilities for reading from and writing to GMML and MAPP formats.
 * 
 * TODO: work out a nice interface for mapping to GmmlGraphics objects,
 * while still maintaining the possibility to use this class outside of the 
 * gmml-vision context. 
 * 
 * @author martijn van iersel
 *
 */
public class GmmlData2
{
	/**
	 * JDOM representation of xml.
	 */
	private Document doc;
	
	/**
	 * Location where XML schema for GMML can be found 
	 */
	final private static File xsdFile = new File("e:/prg/gmml/trunk/xsd/GMML_compat.xsd");
	
	/**
	 * Constructor: create a new, empty pathway.
	 *
	 */
	
	Logger log;
	
	public GmmlData2 (Logger _log)
	{
		log = _log;
		doc = new Document();
		//Set the root element (pathway) and its graphics
		Element root = new Element("Pathway");
		Element graphics = new Element("Graphics");
		root.addContent(graphics);
		root.addContent(new Element("InfoBox"));
		doc.setRootElement(root);
	}
	
	/**
	 * validates a JDOM document against the xml-schema definition specified by 'xsdFile'
	 * @param doc the document to validate
	 */
	public boolean validateDocument() {
		// validate JDOM tree if xsd file exists
		boolean result;
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
				log.info("Document is valid according to the xml schema definition '" + 
						xsdFile.toString() + "'");
				result = true;
			} catch (SAXException se) {
				log.error ("Could not parse the xml-schema definition: " + se.getMessage());
				result = false;
			} catch (JDOMException je) {
				log.error("Document is invalid according to the xml-schema definition!");
				log.error(je.getMessage());
				result = false;
			}
		} else {
			log.error ("Document is not validated because the xml schema definition '" + 
					xsdFile.toString() + "' could not be found");
			result = false;
		}
		return result;
	}
	
	/**
	 * Writes the JDOM document to the file specified
	 * @param file	the file to which the JDOM document should be saved
	 */
	public void writeToXML(File file) {
		try 
		{
			//Get the XML code
			XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
			Format f = xmlcode.getFormat();
			f.setEncoding("ISO-8859-1");
			f.setTextMode(Format.TextMode.PRESERVE);
			xmlcode.setFormat(f);			
			//Open a filewriter
			FileWriter writer = new FileWriter(file);
			//Send XML code to the filewriter
			xmlcode.output(doc, writer);
			log.info("File '" + file.toString() + "' is saved");
		}
		catch (IOException e) 
		{
			System.err.println(e);
		}
	}

	public void readFromXml(File file)
	{
		// Start XML processing
		log.info("Start reading the XML file: " + file);
		SAXBuilder builder  = new SAXBuilder(false); // no validation when reading the xml file
		// try to read the file; if an error occurs, catch the exception and print feedback
		try
		{
			// build JDOM tree
			doc = builder.build(file);
		}
		catch(JDOMParseException pe) 
		{
			 log.error(pe.getMessage());
		}
		catch(JDOMException e)
		{
			log.error(file + " is invalid.");
			log.error(e.getMessage());
		}
		catch(IOException e)
		{
			log.error("Could not access " + file);
			log.error(e.getMessage());
		}
		catch(Exception e)
		{
			log.error("Error: " + e.getMessage());
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


}
