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
	static HashMap<String, List<PathwayElement>> reaction2element = 
									new HashMap<String, List<PathwayElement>>();
									
	static HashMap<String, PathwayElement> compound2element = 
										new HashMap<String, PathwayElement>();
										
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
				String elementName = child.getName();
				String childName = child.getAttributeValue("name");
				String type = child.getAttributeValue("type");
			
				Logger.log.trace(
						"Processing element " + ++progress + " out of " + 
						keggElements.size() + ": " + childName + ", " + type);

				System.out.println("element naam = " + elementName);

				//Trouwens, wil je zeker dat alle elementen zonder graphics overgeslagen worden?
				if("entry".equals(elementName))
				{
					
					// Start converting elements
					Element graphics = child.getChild("graphics");

					/** types: map, enzyme, compound **/
					
					if(type.equals("enzyme") && graphics != null) 
					{						
						String enzymeCode = child.getAttributeValue("name");
						List <String> ncbi = getNcbiByEnzyme(enzymeCode, specie); //Gencodes --> ID
						PathwayElement[] pwElms = new PathwayElement[ncbi.size()];
						String[] reactions = child.getAttributeValue("reaction").split(" ");
						
						if (ncbi != null && ncbi.size() > 0)							
						{
							for(int i=0; i<ncbi.size(); i++ )
							{
								String textlabelGPML = ncbi.get(i); // name of gene i from online NCBI database
								
								// Fetch pathwayElement 
								PathwayElement element = createPathwayElement(child, graphics, ObjectType.DATANODE, i, textlabelGPML); 							
																						
								element.setDataSource(DataSource.ENTREZ_GENE);
								element.setGeneID(ncbi.get(i));
								element.setDataNodeType(DataNodeType.GENEPRODUCT);

								pathway.add(element);
								pwElms[i] = element;
							}
							for(String reaction : reactions) {
								List<PathwayElement> genes = reaction2element.get(reaction);
								if(genes == null) {
									reaction2element.put(reaction, genes = new ArrayList<PathwayElement>());
								}
								for(PathwayElement e : pwElms) {
									genes.add(e);
								}
							}
						}
						else { 
							String textlabelGPML = enzymeCode;
							int i = 0;

							// Fetch pathwayElement
							PathwayElement element = createPathwayElement(child, graphics, ObjectType.DATANODE, i, textlabelGPML); 								

							pathway.add(element);
							
							for(String reaction : reactions) {
								List<PathwayElement> genes = reaction2element.get(reaction);
								if(genes == null) {
									reaction2element.put(reaction, genes = new ArrayList<PathwayElement>());
								}
								genes.add(element);
							}													
						}
					}
					else if(type.equals("compound"))
					{						
						String compoundName = child.getAttributeValue("name"); 
						int i = 0;
						
						// Fetch pathwayElement 
						PathwayElement element = createPathwayElement(child, graphics, ObjectType.DATANODE, i, compoundName); 
						
						element.setDataNodeType(DataNodeType.METABOLITE);

						pathway.add(element);
						compound2element.put(compoundName, element);
					}					
					else if(type.equals("map"))
					{
						String textlabelGPML = graphics.getAttributeValue("name"); 
						String typeGPML = null;
						int i = 0;
						
						// Fetch pathwayElement 
						PathwayElement element = createPathwayElement(child, graphics, ObjectType.LABEL, i, textlabelGPML); 

						element.setMFontSize(150);

						pathway.add(element);
					}
					else if(type.equals("ortholog"))
					{
						String textlabelGPML = graphics.getAttributeValue("name");
						int i = 0;

						// Fetch pathwayElement
						PathwayElement element = createPathwayElement(child, graphics, ObjectType.DATANODE, i, textlabelGPML); 								

						element.setDataNodeType(DataNodeType.UNKOWN);
						
						pathway.add(element);
						
						String[] reactions = child.getAttributeValue("reaction").split(" ");
						
						for(String reaction : reactions) {
							List<PathwayElement> genes = reaction2element.get(reaction);
							if(genes == null) {
								reaction2element.put(reaction, genes = new ArrayList<PathwayElement>());
							}
							genes.add(element);
						}	
					}
				}			
				// End converting elements
				// Start converting lines

				else if ("reaction".equals(elementName)){
					String reactionName = child.getAttributeValue("name");
					
					System.out.println("reaction " +reactionName+ " found");

					// Create a list of elements in relations with reaction
					List<Element> reactionElements = child.getChildren();
					List<PathwayElement> dataNodes = reaction2element.get(reactionName);
					
					for(Element relation : reactionElements) {
						
						String compoundName = relation.getAttributeValue("name");
						PathwayElement start = null;
						PathwayElement end = null;
						
						if (relation.getName().equals("substrate")){
							PathwayElement substrate = compound2element.get(compoundName);
							start = substrate;
							end = dataNodes.get(0);
						}
						else {
							PathwayElement product = compound2element.get(compoundName);
							start = dataNodes.get(0);
							end = product;
						}
						
						if (start!=null && end!=null){
							// Fetch pathwayLine 
							PathwayElement line = createPathwayLine(start, end);								
							pathway.add(line);
						} else {
							Logger.log.error("No DataNodes to connect to for reaction " + reactionName + " in " + relation.getName());
						}
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

	public static PathwayElement createPathwayElement(Element entry, Element graphics, int objectType, int i, String textlabelGPML)
	{
		// Create new pathway element
		PathwayElement element = new PathwayElement(objectType);

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

	public static PathwayElement createPathwayLine(PathwayElement start, PathwayElement end)
	{
		// Create new pathway line
		PathwayElement line = new PathwayElement(ObjectType.LINE);
		
		line.setColor(Color.BLACK);

		// Setting start coordinates
		line.setMStartX(start.getMCenterX());
		line.setMStartY(start.getMCenterY());
		line.setStartGraphRef(start.getGraphId());

		// Setting end coordinates
		line.setMEndX(end.getMCenterX());
		line.setMEndY(end.getMCenterY());
		line.setEndGraphRef(end.getGraphId());

		return line;
	}

}

