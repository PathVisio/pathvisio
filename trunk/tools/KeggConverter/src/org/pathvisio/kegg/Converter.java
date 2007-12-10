// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.kegg;

import java.awt.Color;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.LinkDBRelation;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.BatikImageExporter;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.model.LineType;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

public class Converter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "examples/map00350.xml";
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
				String name = child.getAttributeValue("name");
				String type = child.getAttributeValue("type");
				
				Logger.log.trace(
						"Processing element " + ++progress + " out of " + 
						keggElements.size() + ": " + name + ", " + type);

				Element graphics = child.getChild("graphics");
				if(type != null && graphics != null) 
				{
					/** types: map, enzyme, compound **/
					if(type.equals("enzyme")) 
					{						
						String enzymeCode = child.getAttributeValue("name");
						List <String> ncbi = getNcbiByEnzyme(enzymeCode, specie); //Gencodes --> ID

						if (ncbi != null)
						{
							for(int i=0; i<ncbi.size(); i++ )
							{
								String textlabelGPML = ncbi.get(i); // name of gene i from online NCBI database
								
								PathwayElement element = new PathwayElement(ObjectType.DATANODE);
								String id = element.getGraphId();								
								element.setDataSource(DataSource.ENTREZ_GENE);
								element.setGeneID(ncbi.get(i));
								element.setDataNodeType(DataNodeType.GENEPRODUCT);

								// Fetch pathwayElement 
								element = createPathwayElement(child, graphics, element, i, textlabelGPML); 							
								
								if(element != null) {
									pathway.add(element);
								}
							}
						}
						else { 
							String textlabelGPML = enzymeCode;
							int i = 0;
							
							PathwayElement element = new PathwayElement(ObjectType.DATANODE);
							String id = element.getGraphId();
							//TK: Je hoeft deze niet te setten als ze niet bekend zijn
							//ze worden automatisch op null of een lege string gezet
							element.setDataSource(null); 
							element.setGeneID("null");

							// Fetch pathwayElement 
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
						String id = element.getGraphId();
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
						String id = element.getGraphId();
						element.setMFontSize(150);
						
						// Fetch pathwayElement 
						element = createPathwayElement(child, graphics, element, i, textlabelGPML); 
						
						pathway.add(element);
					}
					
					
					if(child.getName().equals("reaction")){
						//loopt van substraat naar gen en vervolgens van gen naar product
						String substrate = child.getChild("substrate").getAttributeValue("name");
						String product = child.getChild("product").getAttributeValue("name");
						String reaction= child.getAttributeValue("name");
						
						//zoek in in de entries naar de bijbehorende metabolieten of enzymen
						//pijlen van substraat naar gen
						for (Element child2 : keggElements) {
							
							String substrateX = "";
							String substrateY = "";
							String geneX = "";
							String geneY = "";
							
							
							if (name.equals("substrate")){
								substrateX = child2.getAttributeValue("x");
								substrateY = child2.getAttributeValue("y");
								idSubstrate = child.getAttributeValue("id");	
							}
							if (name.equals("reaction")){
								geneX = child2.getAttributeValue("x");
								geneY = child2.getAttributeValue("y");
								idGene = child.getAttributeValue("id");
							}
							
							PathwayElement element = new PathwayElement(ObjectType.LINE);
							element.setColor(Color.BLACK);
							element.setMStartX(Double.parseDouble(substrateX));
							element.setMStartY(Double.parseDouble(substrateY));
							element.setMEndX(Double.parseDouble(geneX));
							element.setMEndY(Double.parseDouble(geneY));
							element.setStartGraphRef(idSubstrate);
							element.setEndGraphRef(idGene);
							element.setEndLineType(LineType.ARROW);
							
							pathway.add(element);
						}
						//pijlen van gen naar product
						for (Element child3 : keggElements) {
							
							String substrateX = "";
							String substrateY = "";
							String productX = "";
							String productY = "";
							
							if (name.equals("reaction")){
								substrateX = child3.getAttributeValue("x");
								substrateY = child3.getAttributeValue("y");
								idSubstrate = child.getAttributeValue("id");
							}
							if (name.equals("product")){
								productX = child3.getAttributeValue("x");
								productY = child3.getAttributeValue("y");
								idProduct = child.getAttributeValue("id");
							}
							
							PathwayElement element = new PathwayElement(ObjectType.LINE);
							element.setColor(Color.BLACK);
							element.setMStartX(Double.parseDouble(substrateX));
							element.setMStartY(Double.parseDouble(substrateY));
							element.setMEndX(Double.parseDouble(productX));
							element.setMEndY(Double.parseDouble(productY));
							element.setStartGraphRef(idSubstrate);
							element.setEndGraphRef(idProduct);
							element.setEndLineType(LineType.ARROW);
							
							pathway.add(element);
						}					
					}
				
				}
			}
			
			pathway.writeToXml(new File(filename + ".gpml"), false);
			
			//Also write to png for more convenient testing:
			ImageExporter imgExport = new BatikImageExporter(ImageExporter.TYPE_PNG);
			imgExport.doExport(new File(filename + ".png"), pathway);
			
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

	public static PathwayElement createPathwayElement(Element child, Element graphics, PathwayElement element, int i, String textlabelGPML)
	{
		//Create new pathway element
	
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
		String ID = child.getAttributeValue("id");
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
		element.setGraphId(ID);
		
		
		// Set textlabel
		element.setTextLabel(textlabelGPML);			
		
		return element;
	}
	
	
}

