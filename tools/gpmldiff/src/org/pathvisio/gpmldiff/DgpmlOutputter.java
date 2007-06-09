// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

	public void flush() throws IOException
	{
		XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
		Format f = xmlcode.getFormat();
		f.setEncoding("ISO-8859-1");
		f.setTextMode(Format.TextMode.PRESERVE);
		xmlcode.setFormat(f);
		
		//Open a filewriter
		PrintWriter writer = new PrintWriter(System.out);
		xmlcode.output(doc, writer);
	}

	public void insert(PwyElt newElt)
	{
		Element e = (new Element("Insert"));
		e.setText (newElt.summary());
		doc.getRootElement().addContent(e);
	}

	public void delete(PwyElt oldElt)
	{
		Element e = (new Element("Delete"));
		e.setText (oldElt.summary());
		doc.getRootElement().addContent(e);
	}

	public void modify(PwyElt newElt, String path, String oldVal, String newVal)
	{
		Element e = (new Element("Modify"));
		e.setText (newElt.summary());
		e.setAttribute("path", path);
		e.setAttribute("old", oldVal);
		e.setAttribute("new", newVal);
		doc.getRootElement().addContent(e);
	}
}