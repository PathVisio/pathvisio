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
package org.pathvisio.core.gpmldiff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.GpmlFormat;
import org.pathvisio.core.model.PathwayElement;

/**
   Naive implementation of Outputter.
 */
class DgpmlOutputter extends DiffOutputter
{
	Document doc = null;
	OutputStream out;

	DgpmlOutputter(File f) throws IOException
	{
		this();
		out = new FileOutputStream(f);
	}

	DgpmlOutputter()
	{
		out = System.out;
		doc = new Document();
		doc.setRootElement (new Element("Delta"));
	}

	public void flush() throws IOException
	{
		XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
		Format f = xmlcode.getFormat();
		f.setEncoding("ISO-8859-1");
		f.setTextMode(Format.TextMode.PRESERVE);
		f.setLineSeparator(System.getProperty("line.separator"));
		xmlcode.setFormat(f);

		//Open a filewriter
		PrintWriter writer = new PrintWriter(out);
		xmlcode.output(doc, writer);
		out.flush();
	}

	public void insert(PathwayElement newElt)
	{
		Element e = (new Element("Insert"));
		try
		{
			Element f = GpmlFormat.createJdomElement(newElt);
			e.addContent (f);
		}
		catch (ConverterException ex) { Logger.log.error ("Converter exception", ex); }
		doc.getRootElement().addContent(e);
	}

	public void delete(PathwayElement oldElt)
	{
		Element e = (new Element("Delete"));
		try
		{
			Element f = GpmlFormat.createJdomElement(oldElt);
			e.addContent (f);
		}
		catch (ConverterException ex) { Logger.log.error ("Converter exception", ex); }
		doc.getRootElement().addContent(e);
	}

	PathwayElement curOldElt = null;
	PathwayElement curNewElt = null;
	Element curModifyElement = null;

	public void modifyStart (PathwayElement oldElt, PathwayElement newElt)
	{
		curOldElt = oldElt;
		curNewElt = newElt;

		curModifyElement = (new Element("Modify"));
		try
		{
			Element f = GpmlFormat.createJdomElement(curOldElt);
			curModifyElement.addContent (f);
		}
		catch (ConverterException ex) { Logger.log.error ("Converter exception", ex); }
	}

	public void modifyEnd ()
	{
		doc.getRootElement().addContent(curModifyElement);
		curOldElt = null;
		curNewElt = null;
	}

	public void modifyAttr (String attr, String oldVal, String newVal)
	{
		assert (curOldElt != null);
		assert (curNewElt != null);

		Element e = new Element ("Change");
		e.setAttribute("attr", attr);
		e.setAttribute("old", oldVal);
		e.setAttribute("new", newVal);
		curModifyElement.addContent (e);
	}
}