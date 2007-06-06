package org.pathvisio.gpmldiff;

import java.io.*;
import org.jdom.*;
import org.jdom.output.*;

/**
   Naive implementation of Outputter.
 */
class DgpmlOutputter extends DiffOutputter
{
	Document doc = null;
	
	DgpmlOutputter(File f)
	{
		//TODO: open file
	}
	
	DgpmlOutputter()
	{
		doc = new Document();
		doc.setRootElement (new Element("Delta"));
	}

	public void flush()
	{
		XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
		Format f = xmlcode.getFormat();
		f.setEncoding("ISO-8859-1");
		f.setTextMode(Format.TextMode.PRESERVE);
		xmlcode.setFormat(f);
		
		//Open a filewriter
		PrinterWriter writer = new PrinterWriter();
		xmlcode.output(doc, writer);
	}

	public void insert(PwyElt newElt)
	{
		Element e = (new Element("Insert"));
		e.setText (newElt.summary());
		doc.getRootElement().addChild(e);
	}

	public void delete(PwyElt oldElt)
	{
		Element e = (new Element("Delete"));
		e.setText (oldElt.summary());
		doc.getRootElement().addChild(e);
	}

	public void modify(PwyElt newElt, String path, String oldVal, String newVal)
	{
		Element e = (new Element("Modify"));
		e.setText (newElt.summary());
		e.setAttributeValue("path", path);
		e.setAttributeValue("old", oldVal);
		e.setAttributeValue("new", newVal);
		doc.getRootElement().addChild(e);
	}
}