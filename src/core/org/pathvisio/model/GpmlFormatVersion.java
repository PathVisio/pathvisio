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
