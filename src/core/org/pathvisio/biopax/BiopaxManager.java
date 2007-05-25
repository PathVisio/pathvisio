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
import org.pathvisio.gui.Engine;
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
			Engine.log.info(bpText);
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
