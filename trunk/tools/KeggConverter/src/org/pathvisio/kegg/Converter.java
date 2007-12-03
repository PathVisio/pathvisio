package org.pathvisio.kegg;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import javax.xml.rpc.ServiceException;

import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.LinkDBRelation;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

public class Converter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "examples/map00031.xml";
		String specie = "hsa";

		SAXBuilder builder  = new SAXBuilder();
		try {
			Document doc = builder.build(new File(filename));

			Element rootelement =doc.getRootElement();

			List<Element> grandChildren=rootelement.getChildren();

			Pathway pathway = new Pathway();

			for(Element child : grandChildren) {

				String type = child.getAttributeValue("type");
				if(type != null) {
					/** types: map, enzyme, compound **/

					if(type.equals("enzyme")) 
					{
						Element graphics = child.getChild("graphics");

						String enzymeCode = child.getAttributeValue("name");
						String[] ncbi = getNcbiByEnzyme(enzymeCode, specie); //Gencodes --> ID

						if (ncbi.equals("null") == false ){
							for(int i=0; i<ncbi.length; i++ )
							{
								String textlabelGPML = ncbi[i]; // naam van gen i uit online NCBI database
								String colorGPML = child.getAttributeValue("fgcolor");
								String centerXGPML = child.getAttributeValue("x");
								String centerYGPML = child.getAttributeValue("y");
								String widthGPML = child.getAttributeValue("width");
								String heightGPML = child.getAttributeValue("height");

								Double height = Double.parseDouble(heightGPML);
								Double centerY = Double.parseDouble(centerYGPML) - i*height;

								PathwayElement element = new PathwayElement(ObjectType.DATANODE);
								String id = element.getGraphId();
								element.setTextLabel(textlabelGPML);
								element.setDataNodeType("Geneproduct");
//								element.setColor(colorGPML);
								element.setMCenterX(Double.parseDouble(centerXGPML));
								element.setMCenterY(centerY);
								element.setMWidth(Double.parseDouble(widthGPML));
								element.setMHeight(height);
								element.setDataSource("Entrez Gene"); // No idea what may be the problem
								element.setGeneID(ncbi[i]);

								if(element != null) {
									pathway.add(element);
								}
							}
						}
						else { 
							String textlabelGPML = child.getAttributeValue("name");
							String colorGPML = child.getAttributeValue("fgcolor");
							String centerXGPML = child.getAttributeValue("x");
							String centerYGPML = child.getAttributeValue("y");
							String widthGPML = child.getAttributeValue("width");
							String heightGPML = child.getAttributeValue("height");

							PathwayElement element = new PathwayElement(ObjectType.DATANODE);
							String id = element.getGraphId();
							element.setTextLabel(textlabelGPML);
							element.setDataNodeType("Geneproduct");
//							element.setColor(colorGPML);
							element.setMCenterX(Double.parseDouble(centerXGPML));
							element.setMCenterY(Double.parseDouble(centerYGPML));
							element.setMWidth(Double.parseDouble(widthGPML));
							element.setMHeight(Double.parseDouble(heightGPML));
							element.setDataSource("Entrez Gene"); // No idea what may be the problem
							element.setGeneID("null");

							if(element != null) {
								pathway.add(element);
							}
						}
					}
					else if(type.equals("compound"))
					{
						String textlabelGPML = "mieauw"; // naam van metabolite uit online KEGG database

						Element graphics = child.getChild("graphics");
						String colorGPML = child.getAttributeValue("fgcolor");
						String centerXGPML = child.getAttributeValue("x");
						String centerYGPML = child.getAttributeValue("y");
						String widthGPML = child.getAttributeValue("width");
						String heightGPML = child.getAttributeValue("height");

						PathwayElement element = new PathwayElement(ObjectType.DATANODE);
						String id = element.getGraphId();
						element.setTextLabel(textlabelGPML);
						element.setDataNodeType("Metabolite");
//						element.setColor(colorGPML);
						element.setMCenterX(Double.parseDouble(centerXGPML));
						element.setMCenterY(Double.parseDouble(centerYGPML));
						element.setMWidth(Double.parseDouble(widthGPML));
						element.setMHeight(Double.parseDouble(heightGPML));

						pathway.add(element);
					}					
					else if(type.equals("map"))
					{
						String textlabelGPML = child.getAttributeValue("name"); 
						String typeGPML = null;
						String centerXGPML = child.getAttributeValue("x");
						String centerYGPML = child.getAttributeValue("y");
						String widthGPML = child.getAttributeValue("width");
						String heightGPML = child.getAttributeValue("height");

						PathwayElement element = new PathwayElement(ObjectType.LABEL);
						String id = element.getGraphId();
						element.setMFontSize(150);
						element.setTextLabel(textlabelGPML);
						element.setMCenterX(Double.parseDouble(centerXGPML));
						element.setMCenterY(Double.parseDouble(centerYGPML));
						element.setMWidth(Double.parseDouble(widthGPML));
						element.setMHeight(Double.parseDouble(heightGPML));

						pathway.add(element);
					}
				}
			}	
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static String[] getNcbiByEnzyme(String ec, String species) throws ServiceException, RemoteException 
	{
		//Setup a connection to KEGG
		KEGGLocator  locator = new KEGGLocator();
		KEGGPortType serv;
		serv = locator.getKEGGPort();

		//Fetch the gene names
		String[] genes = serv.get_genes_by_enzyme(ec, species);

		//KEGG code --> NCBI code
		String test =  "null";
		if(genes.length != 0){
			
			for(String gene : genes) {
				LinkDBRelation[] links = serv.get_linkdb_by_entry(gene, "NCBI-GeneID", 1, 100);
				for(LinkDBRelation ldb : links) {
					test = ldb.getEntry_id2();//moet array zijn? nu wordt er overgeschreven per loop
				}
			}
		}	
		return new String[] { test };  //mismatch bij test is string[],  wordt hier omgezet van string sting[]? 
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
	
}

