package org.pathvisio.biopax;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.biopax.paxtools.impl.level2.BioPAXFactoryImpl;
import org.biopax.paxtools.io.jena.JenaIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level2.BioPAXFactory;
import org.biopax.paxtools.model.level2.Model;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
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
			String bpText = new XMLOutputter().outputString(doc);
			JenaIOHandler ioh = new JenaIOHandler(bpf, BioPAXLevel.L2);
			model = ioh.convertFromOWL(Utils.stringToInputStream(bpText));
		}
	}
	
	public List getXml() throws ConverterException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			JenaIOHandler ioh = new JenaIOHandler();
			ioh.convertToOWL(model, out);
			String biopax = "";
			String xml = out.toString(biopax);
			
			SAXBuilder saxBuilder=new SAXBuilder();
			Reader stringReader=new StringReader(xml);
			Document doc = saxBuilder.build(stringReader);
			return (List)doc.removeContent();
		} catch(Exception e) {
			throw new ConverterException(e);
		}
	}
	
	public Model getModel() {
		return model;
	}
}
