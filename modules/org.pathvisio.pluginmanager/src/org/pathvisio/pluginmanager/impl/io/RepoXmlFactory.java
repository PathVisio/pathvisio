// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2013 BiGCaT Bioinformatics
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

package org.pathvisio.pluginmanager.impl.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.pathvisio.pluginmanager.impl.data.PVRepository;

/**
 * reads and writes the local repository file
 * @author martina
 *
 */
public class RepoXmlFactory {

	/**
	 * writes out the local PVRepository
	 * uses JAXB to write XML file
	 */
	public void writeXml(File file, PVRepository repo) throws Exception {
		JAXBContext context = JAXBContext.newInstance(PVRepository.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		FileWriter writer = new FileWriter(file);
		m.marshal(repo, writer);
		writer.close();
	}
	
	/**
	 * reads the local PVRepository file
	 * uses JAXB to read XML file
	 */
	public PVRepository readXml(File file) throws Exception {
		JAXBContext context = JAXBContext.newInstance(PVRepository.class);
		Unmarshaller um = context.createUnmarshaller();
		FileReader reader = new FileReader(file);
		PVRepository repo = (PVRepository) um.unmarshal(reader);
		reader.close();
			
		return repo;
	}
}
