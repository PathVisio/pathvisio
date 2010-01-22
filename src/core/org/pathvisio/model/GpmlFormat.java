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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import org.bridgedb.bio.BioDataSource;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.pathvisio.debug.Logger;
import org.xml.sax.InputSource;


/**
 * class responsible for interaction with Gpml format.
 * Contains all gpml-specific constants,
 * and should be the only class (apart from svgFormat)
 * that needs to import jdom
 */
public class GpmlFormat implements PathwayImporter, PathwayExporter
{
	/**
	 * The factor that is used to convert pixel coordinates
	 * to the GPML model coordinates. E.g. if you want to convert the
	 * width from pixels to GPML model coordinates you use:
	 *
	 * double mWidth = width * pixel2model;
	 */
	public static final double PIXEL_TO_MODEL = 15;

	static private final GpmlFormatVersion CURRENT = GpmlFormatImpl1.GPML_2008A;

	public static final Namespace RDF = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	public static final Namespace RDFS = Namespace.getNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
	public static final Namespace BIOPAX = Namespace.getNamespace("bp", "http://www.biopax.org/release/biopax-level2.owl#");
	public static final Namespace OWL = Namespace.getNamespace("owl", "http://www.w3.org/2002/07/owl#");

	static {
		BioDataSource.init();
	}

	public Pathway doImport(File file) throws ConverterException
	{
		Pathway pathway = new Pathway();
		readFromXml(pathway, file, true);
		return pathway;
	}

	public void doExport(File file, Pathway pathway) throws ConverterException
	{
		writeToXml(pathway, file, true);
	}

	public String[] getExtensions() {
		return new String[] { "gpml", "xml" };
	}

	public String getName() {
		return "GPML file";
	}

	public static Document createJdom(Pathway data) throws ConverterException
	{
		return CURRENT.createJdom(data);
	}

	static public Element createJdomElement(PathwayElement o) throws ConverterException
	{
		return CURRENT.createJdomElement(o);
	}

	public static PathwayElement mapElement(Element e) throws ConverterException
	{
		return CURRENT.mapElement(e);
	}

	/**
	 * Writes the JDOM document to the file specified
	 * @param file	the file to which the JDOM document should be saved
	 * @param validate if true, validate the dom structure before writing to file. If there is a validation error,
	 * 		or the xsd is not in the classpath, an exception will be thrown.
	 */
	static public void writeToXml(Pathway pwy, File file, boolean validate) throws ConverterException
	{
		CURRENT.writeToXml(pwy, file, validate);
	}

	static public void writeToXml(Pathway pwy, OutputStream out, boolean validate) throws ConverterException
	{
		CURRENT.writeToXml(pwy, out, validate);
	}

	static public void readFromXml(Pathway pwy, File file, boolean validate) throws ConverterException
	{
		InputStream inf;
		try
		{
			inf = new FileInputStream (file);
		}
		catch (FileNotFoundException e)
		{
			throw new ConverterException (e);
		}
		readFromXmlImpl (pwy, new InputSource(inf), validate);
	}

	static public void readFromXml(Pathway pwy, InputStream in, boolean validate) throws ConverterException
	{
		readFromXmlImpl (pwy, new InputSource(in), validate);
	}

	static public void readFromXml(Pathway pwy, Reader in, boolean validate) throws ConverterException
	{
		readFromXmlImpl (pwy, new InputSource(in), validate);
	}

	private static void readFromXmlImpl(Pathway pwy, InputSource in, boolean validate) throws ConverterException
	{
		// Start XML processing

		SAXBuilder builder  = new SAXBuilder(false); // no validation when reading the xml file
		// try to read the file; if an error occurs, catch the exception and print feedback
		try
		{
			Logger.log.trace ("Build JDOM tree");
			// build JDOM tree
			Document doc = builder.build(in);

			// Copy the pathway information to a VPathway
			Element root = doc.getRootElement();
			if (!root.getName().equals("Pathway"))
			{
				throw new ConverterException ("Not a Pathway file");
			}

			Namespace ns = root.getNamespace();
			GpmlFormatImpl1[] formats = new GpmlFormatImpl1[] { GpmlFormatImpl1.GPML_2007, GpmlFormatImpl1.GPML_2008A };
			boolean recognized = false;
			for (GpmlFormatImpl1 format : formats)
			{
				if (ns.equals(format.getGpmlNamespace()))
				{
					Logger.log.info ("Recognized format " + ns);

					Logger.log.trace ("Start Validation");
					if (validate) format.validateDocument(doc);
					Logger.log.trace ("Copy map elements");

					format.readFromRoot (root, pwy);
					recognized = true;
					break;
				}
			}
			if (!recognized)
			{
				throw new ConverterException ("This file looks like a pathway, " +
						"but the namespace " + ns + " was not recognized. This application might be out of date.");
			}
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
		catch(NullPointerException e)
		{
			throw new ConverterException (e);
		}
		catch(IllegalArgumentException e) {
			throw new ConverterException (e);
		}
		catch(Exception e) { //Make all types of exceptions a ConverterException
			throw new ConverterException (e);
		}
	}

}