package org.pathvisio.model;

import java.io.File;
import java.io.OutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public interface GpmlFormatVersion 
{
	Namespace getGpmlNamespace();
	Document createJdom(Pathway data) throws ConverterException;
	Element createJdomElement(PathwayElement o) throws ConverterException;
	PathwayElement mapElement(Element e) throws ConverterException;
	void writeToXml(Pathway pwy, File file, boolean validate) throws ConverterException;
	void writeToXml(Pathway pwy, OutputStream out, boolean validate) throws ConverterException;

	/**
	 * validates a JDOM document against the xml-schema definition specified by 'xsdFile'
	 * @param doc the document to validate
	 */	
	void validateDocument(Document doc) throws ConverterException;
}
