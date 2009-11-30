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
package org.pathvisio.biopax;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;

import org.biopax.paxtools.impl.level2.BioPAXFactoryImpl;
import org.biopax.paxtools.io.jena.JenaIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level2.BioPAXFactory;
import org.biopax.paxtools.model.level2.Model;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.util.Utils;

public class BiopaxManager {
	Model model;

	/**
	 * Create a Biopax object from the given JDOM element
	 * @param e The element containing BioPAX code
	 */
	public BiopaxManager(Document doc) {
		BioPAXFactory bpf = new BioPAXFactoryImpl();
		if(doc == null) { //Create new model
			model = bpf.createModel();
		} else { //Parse jdom
			String bpText = new XMLOutputter(Format.getPrettyFormat()).outputString(doc);
			Logger.log.info(bpText);
			model = modelFromString(bpText);
		}
	}

	public Document getDocument() throws ConverterException {
		try {
			String xml = getXml();
			SAXBuilder saxBuilder=new SAXBuilder();
			Reader stringReader=new StringReader(xml);
			return saxBuilder.build(stringReader);
		} catch(Exception e) {
			throw new ConverterException(e);
		}
	}

	public static Model modelFromString(String xml) {
		BioPAXFactory bpf = new BioPAXFactoryImpl();
		JenaIOHandler ioh = new JenaIOHandler(bpf, BioPAXLevel.L2);
		return ioh.convertFromOWL(Utils.stringToInputStream(xml));
	}

	public String getXml() throws ConverterException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			JenaIOHandler ioh = new JenaIOHandler();
			ioh.convertToOWL(model, out);
			return out.toString();
		} catch(Exception e) {
			throw new ConverterException(e);
		}
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model m) {
		model = m;
	}
}
