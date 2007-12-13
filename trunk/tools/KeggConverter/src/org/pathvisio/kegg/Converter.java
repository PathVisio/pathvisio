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
	static KEGGLocator  locator = new KEGGLocator();
	static KEGGPortType serv = null;

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
			//Setup a connection to KEGG
			serv = locator.getKEGGPort();

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
						String[] genes = getGenes(enzymeCode, specie);
						List<PathwayElement> pwElms = new ArrayList<PathwayElement>();
						String[] reactions = child.getAttributeValue("reaction").split(" ");


						if (genes != null && genes.length > 0)							
						{
							for(int i = 0; i < genes.length; i++) {
								String geneId = genes[i];
								
								List<String> ncbi = getNcbiByGene(geneId);
								for(int j=0; j<ncbi.size(); j++ )
								{
									// Name of gene j from online NCBI database
									String textlabelGPML = getGeneSymbol(geneId); 
								
									// Fetch pathwayElement 
									PathwayElement element = createPathwayElement(child, graphics, ObjectType.DATANODE, i, textlabelGPML); 							

									element.setDataSource(DataSource.ENTREZ_GENE);
									element.setGeneID(ncbi.get(j));
									element.setDataNodeType(DataNodeType.GENEPRODUCT);

									pathway.add(element);
									pwElms.add(element);
								}
							}

							List<PathwayElement> reactionElements = getReactionElements(child);
							for(PathwayElement e : pwElms) {
								reactionElements.add(e);
							}
						}
						else { 
							String textlabelGPML = enzymeCode;
							int i = 0;

							// Fetch pathwayElement
							PathwayElement element = createPathwayElement(child, graphics, ObjectType.DATANODE, i, textlabelGPML); 								

							pathway.add(element);

							getReactionElements(child).add(element);																		
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

						getReactionElements(child).add(element);				
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

	public static String[] getGenes(String ec, String species) throws RemoteException 
	{
		//Fetch the gene IDs
		return serv.get_genes_by_enzyme(ec, species);		
	}

	public static List <String> getNcbiByGene(String gene) throws ServiceException, RemoteException 
	{
		//KEGG code --> NCBI code
		List <String> result =  new ArrayList <String>();
		LinkDBRelation[] links = serv.get_linkdb_by_entry(gene, "NCBI-GeneID", 1, 100);
		for(LinkDBRelation ldb : links) {
			result.add(ldb.getEntry_id2().substring(12));
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

		//TK: Quick hack, GraphId is not automatically generated,
		//so set one explicitly...FIXME!
		String startId = start.getGraphId();
		if(startId == null) {
			start.setGraphId(start.getParent().getUniqueId());
		}
		line.setStartGraphRef(start.getGraphId());

		// Setting end coordinates
		line.setMEndX(end.getMCenterX());
		line.setMEndY(end.getMCenterY());
		//TK: Quick hack, GraphId is not automatically generated,
		//so set one explicitly...FIXME!
		String endId = end.getGraphId();
		if(endId == null) {
			end.setGraphId(end.getParent().getUniqueId());			
		}
		line.setEndGraphRef(end.getGraphId());

		return line;
	}

	public static List<PathwayElement> getReactionElements(Element entry)
	{
		String[] reactions = entry.getAttributeValue("reaction").split(" ");

		List<PathwayElement> genes = new ArrayList <PathwayElement>(); 

		for(String reaction : reactions) {
			genes = reaction2element.get(reaction);
			if(genes == null) {
				reaction2element.put(reaction, genes = new ArrayList<PathwayElement>());
			}			
		}
		return genes;
	}

	public static String getGeneSymbol(String geneId) throws RemoteException
	{
		String result = serv.btit(geneId);
		result = result.split(" ")[1];
		result = result.substring(0, result.length()-1);
		return result;
	}
}

