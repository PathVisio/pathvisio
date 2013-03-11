package org.pathvisio.pluginmanager.impl.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.pathvisio.pluginmanager.impl.data.PVRepository;

public class RepoXmlFactory {

	public RepoXmlFactory() {
	}

	public void writeXml(File file, PVRepository repo) {

		// create JAXB context and instantiate marshaller
		try {
			JAXBContext context = JAXBContext.newInstance(PVRepository.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			// Write to System.out
			FileWriter writer = new FileWriter(file);
			m.marshal(repo, writer);
			writer.close();
		
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public PVRepository readXml(File file) {
		try {
			JAXBContext context = JAXBContext.newInstance(PVRepository.class);
			Unmarshaller um = context.createUnmarshaller();
			FileReader reader = new FileReader(file);
			PVRepository repo = (PVRepository) um.unmarshal(reader);
			reader.close();
			
		    return repo;
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
