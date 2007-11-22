package org.pathvisio.kegg;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class Converter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "examples/map00031.xml";
		
		SAXBuilder builder  = new SAXBuilder();
		try {
			Document doc = builder.build(new File(filename));
			System.out.println(doc);
			Element rootelement =doc.getRootElement();
			System.out.println(rootelement);
			List<Element> kindjes=rootelement.getChildren();
			System.out.println(kindjes);
			for(Element child : kindjes) {
				System.out.println(child);
				Element substrate = child.getChild("substrate");
				System.out.println("Name: " + child.getAttributeValue("name"));
				System.out.println("\t" + substrate);
			}

			
			
			
			
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

}
