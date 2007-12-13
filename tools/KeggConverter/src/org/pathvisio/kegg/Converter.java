//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2007 BiGCaT Bioinformatics

//Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. 
//You may obtain a copy of the License at 

//http://www.apache.org/licenses/LICENSE-2.0 

//Unless required by applicable law or agreed to in writing, software 
//distributed under the License is distributed on an "AS IS" BASIS, 
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//See the License for the specific language governing permissions and 
//limitations under the License.

package org.pathvisio.kegg;

import java.awt.Color;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.rpc.ServiceException;

import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.LinkDBRelation;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

public class Converter {
	static HashMap<String, PathwayElement[]> reaction2element = 
									new HashMap<String, PathwayElement[]>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "examples/map00031.xml";
		String specie = "hsa";

		//Some progress logging
		Logger.log.setStream(System.out);
		Logger.log.setLogLevel(true, true, true, true, true, true);
		Logger.log.trace("Start converting pathway " + filename);

		SAXBuilder builder  = new SAXBuilder();
		try {
			Document doc = builder.build(new File(filename));

			Element rootelement = doc.getRootElement();

			List<Element> keggElements = rootelement.getChildren();

			Pathway pathway = new Pathway();

			int progress = 0;
			for(Element child : keggElements) {
				String childName = child.getAttributeValue("name");
				String type = child.getAttributeValue("type");
			
				Element graphics = child.getChild("graphics");
				//TK: Deze kan binnen het tweede if block, als je gecontroleerd
				//hebt of het daadwerkelijk een reactie is (zie verder commentaar
				//String reactionName = child.getAttributeValue("reaction");

				Logger.log.trace(
						"Processing element " + ++progress + " out of " + 
						keggElements.size() + ": " + childName + ", " + type);

				String elementName = child.getName();
				System.out.println("element naam = " + elementName);

				//TK: Je kunt dit netter doen. Je gaat er nu van uit dat
				//alle niet-entries geen type en graphics hebben.
				//Je kunt beter simpelweg gewoon op de naam checken:
				//Trouwens, wil je zeker dat alle elementen zonder graphics overgeslagen worden?
				if("entry".equals(elementName) && graphics != null)
					//if(type != null && graphics != null) 
				{
					
					// Start converting elements

					/** types: map, enzyme, compound **/
					if(type.equals("enzyme")) 
					{						
						String enzymeCode = child.getAttributeValue("name");
						List <String> ncbi = getNcbiByEnzyme(enzymeCode, specie); //Gencodes --> ID
						PathwayElement[] pwElms = new PathwayElement[ncbi.size()];
						if (ncbi != null)
						{
							for(int i=0; i<ncbi.size(); i++ )
							{
								String textlabelGPML = ncbi.get(i); // name of gene i from online NCBI database

								PathwayElement element = new PathwayElement(ObjectType.DATANODE);														
								element.setDataSource(DataSource.ENTREZ_GENE);
								element.setGeneID(ncbi.get(i));
								element.setDataNodeType(DataNodeType.GENEPRODUCT);

								// Fetch pathwayElement 
								element = createPathwayElement(child, graphics, element, i, textlabelGPML); 							

								pathway.add(element);
								pwElms[i] = element;
							}
							String reaction = child.getAttributeValue("reaction");
							reaction2element.put(reaction, pwElms);
						}
						else { 
							String textlabelGPML = enzymeCode;
							int i = 0;

							PathwayElement element = new PathwayElement(ObjectType.DATANODE);
							element.setDataSource(null); 
							element.setGeneID("null");

							element = createPathwayElement(child, graphics, element, i, textlabelGPML); 								

							if(element != null) {
								pathway.add(element);
							}
						}
					}
					else if(type.equals("compound"))
					{
						int i = 0;

						String textlabelGPML = child.getAttributeValue("name"); // has to change to metabolite name from online KEGG database

						PathwayElement element = new PathwayElement(ObjectType.DATANODE);
						element.setDataNodeType(DataNodeType.METABOLITE);

						// Fetch pathwayElement 
						element = createPathwayElement(child, graphics, element, i, textlabelGPML); 

						pathway.add(element);
					}					
					else if(type.equals("map"))
					{
						int i = 0;

						String textlabelGPML = child.getAttributeValue("name"); 
						String typeGPML = null;

						PathwayElement element = new PathwayElement(ObjectType.LABEL);
						element.setMFontSize(150);

						// Fetch pathwayElement 
						element = createPathwayElement(child, graphics, element, i, textlabelGPML); 

						pathway.add(element);
					}
				}			
				// End converting elements
				// Start converting lines

				//TK: Hier weet je dat het geen entry is, check nu voor reaction / relation ?
				else if ("reaction".equals(elementName)){
//					String substrate = child.getChild("substrate").getAttributeValue("name");
//					String product = child.getChild("product").getAttributeValue("name");
					String reaction = child.getAttributeValue("name");	
					String reactionName = child.getAttributeValue("reaction");
					
					System.out.println("reaction " +reaction+ " found");

					// Create a list of elements in relations with reaction

					//TK: Je krijg hier een ClassCastException, omdat je list niet alleen objecten
					//van class Element bevat. Element.getContent() geeft een list terug met Element,
					//Text of CData (als ik het goed heb). Je moet altijd uitkijken met typecasten
					//van een list (dus <Element> erachter zetten). Hiermee ga je er vanuit dat je
					//list alleen maar uit objecten van die class bestaat!
					//List<Element> reactionElements = child.getContent();
					//
					//Een oplossing is om een generieke list te gebruiken (dus zonder de <Element>) en
					//de individuele objecten te casten nadat je bekeken hebt van welke classe ze zijn.
					//
					//Maar dit is waarschijnlijk niet wat je wilt, je wilt alleen de elementen, dus je
					//kunt beter de volgende methode gebruiken:
					List<Element> reactionElements = child.getChildren();
					
					for(Element relation : reactionElements) {

						PathwayElement line = new PathwayElement(ObjectType.LINE);

						// Fetch pathwayLine 
						line = createPathwayLine(child, relation, line);

						pathway.add(line);
					}								
				}
			}

			pathway.writeToXml(new File("C:/Documents and Settings/s030478/Desktop/" + filename.substring(9,17) + ".gpml"), false);
			/*			
			//Also write to png for more convenient testing:
			ImageExporter imgExport = new BatikImageExporter(ImageExporter.TYPE_PNG);
			imgExport.doExport(new File(filename + ".png"), pathway);
			 */			
			Logger.log.trace("Finished converting pathway " + filename);
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}

	public static List <String> getNcbiByEnzyme(String ec, String species) throws ServiceException, RemoteException 
	{
		//Setup a connection to KEGG
		KEGGLocator  locator = new KEGGLocator();
		KEGGPortType serv;
		serv = locator.getKEGGPort();

		//Fetch the gene names
		String[] genes = serv.get_genes_by_enzyme(ec, species);

		//KEGG code --> NCBI code
		List <String> result =  new ArrayList <String>();
		if(genes.length != 0){

			for(String gene : genes) {
				LinkDBRelation[] links = serv.get_linkdb_by_entry(gene, "NCBI-GeneID", 1, 100);
				for(LinkDBRelation ldb : links) {
					result.add(ldb.getEntry_id2().substring(12));
				}
			}
		}

		return result;  
	}

	public static String[] getCompoundsByEnzyme(String ec) throws ServiceException, RemoteException 
	{
		//Setup a connection to KEGG
		KEGGLocator  locator = new KEGGLocator();
		KEGGPortType serv;
		serv = locator.getKEGGPort();

		//Fetch the compounds names
		String[] compounds = serv.get_compounds_by_enzyme(ec);

		//Distinguish substrate from product
		// dependent on outcome get_compounds_by_enzyme  

		//KEGG code --> chemical name
		// no direct way @ http://www.genome.jp/kegg/soap/doc/keggapi_javadoc/keggapi/KEGGPortType.html
		// though via versa is possible

		return new String[] {};
	}

	public static PathwayElement createPathwayElement(Element entry, Element graphics, PathwayElement element, int i, String textlabelGPML)
	{
		// Create new pathway element

		// Set Color
		// Convert a hexadecimal color into an awt.Color object
		// Remove the # before converting
		String colorStringGPML = graphics.getAttributeValue("fgcolor");
		Color colorGPML;
		if (colorStringGPML != null)
		{
			colorGPML = GpmlFormat.gmmlString2Color(colorStringGPML.substring(1));
		}
		else
		{
			colorGPML = Color.BLACK;
		}
		element.setColor(colorGPML);

		// Set x, y, width, height
		String centerXGPML = graphics.getAttributeValue("x");
		String centerYGPML = graphics.getAttributeValue("y");
		String widthGPML = graphics.getAttributeValue("width");
		String heightGPML = graphics.getAttributeValue("height");

		double height = Double.parseDouble(heightGPML);
		double width = Double.parseDouble(widthGPML);
		double centerY = Double.parseDouble(centerYGPML) - i*height;
		double centerX = Double.parseDouble(centerXGPML);

		element.setMCenterX(centerX*GpmlFormat.pixel2model);
		element.setMCenterY(centerY*GpmlFormat.pixel2model);
		//To prevent strange gene box sized,
		//try using the default
		element.setMWidth(width*GpmlFormat.pixel2model);
		element.setMHeight(height*GpmlFormat.pixel2model);

		// Set graphID
		String graphId = entry.getAttributeValue("id");
//		element.setGraphId(graphId);

		// Set textlabel
		element.setTextLabel(textlabelGPML);			

		return element;
	}

	public static PathwayElement createPathwayLine(Element reaction, Element relation, PathwayElement line)
	{
		String reactionName = reaction.getAttributeValue("name");
		
		//TK: dit gaat niet lukken! De lijn heeft geen coordinaten, dus die
		//moet je uit de pathway elementen halen waarnaar hij linkt!
		//Daarvoor moet je onthouden welke enzymes of compounds je al toegevoegd hebt
		//en die aan de hand van de relation of reaction proberen terug te vinden...
		
		PathwayElement[] genes = reaction2element.get(reactionName);
		PathwayElement end = genes[0];
		
		line.setColor(Color.BLACK);

		// Setting start coordinates
		line.setMStartX(Double.parseDouble(startX));
		line.setMStartY(Double.parseDouble(startY));
		line.setStartGraphRef(startId);

		// Setting end coordinates
		line.setMEndX(Double.parseDouble(endX));
		line.setMEndY(Double.parseDouble(endY));
		line.setEndGraphRef(endId);

		return line;

//		even niet nodig
//		line.setEndLineType(LineType.ARROW);
//		if (childName.equals(product)){
//		endX = child.getAttributeValue("x");
//		endY = child.getAttributeValue("y");
//		endId = child.getAttributeValue("id");

//		if (reactionchildName.equals(reaction)){
//		startX = child.getAttributeValue("x");
//		startY = child.getAttributeValue("y");
//		startId = child.getAttributeValue("id");
//		}
//		}


	}

}

