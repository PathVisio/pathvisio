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
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.DataSource;
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

			/*
			Misschien is een andere naam beter,
			grandChildren zegt niet zoveel, wat stellen de elementen
			voor (e.g. keggElements?)
			Je kunt in Eclipse makkelijk de naam van een variabele
			veranderen door de naam te selecteren en ALT-SHIFT-R
			in te typen.
			*/
			
			List<Element> keggElements=rootelement.getChildren();

			Pathway pathway = new Pathway();

			for(Element child : keggElements) {

				String type = child.getAttributeValue("type");
				if(type != null) {
					/** types: map, enzyme, compound **/

					if(type.equals("enzyme")) 
					{
						Element graphics = child.getChild("graphics");

						String enzymeCode = child.getAttributeValue("name");
						List <String> ncbi = getNcbiByEnzyme(enzymeCode, specie); //Gencodes --> ID

						if (ncbi.equals("null") == false ){
							for(int i=0; i<ncbi.size(); i++ )
							{
								String textlabelGPML = ncbi.get(i); // naam van gen i uit online NCBI database
								String typeDatanode = "GeneProduct";
								
								PathwayElement element = new PathwayElement(ObjectType.DATANODE);
								String id = element.getGraphId();
								element.setDataSource(DataSource.ENTREZ_GENE); // No idea what may be the problem
								element.setGeneID(ncbi.get(i));

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
							element.setDataSource(null); // No idea what may be the problem
							element.setGeneID("null");

							// Fetch pathwayElement 
							PathwayElement element = getPathwayElement(child, element, i, textlabelGPML, typeDatanode); 
							
							if(element != null) {
								pathway.add(element);
							}
						}
					}
					else if(type.equals("compound"))
					{
						Element graphics = child.getChild("graphics");
						int i = 0;
						
						String textlabelGPML = ""; // naam van metabolite uit online KEGG database
						String typeDatanode = "Metabolite";
						
						PathwayElement element = new PathwayElement(ObjectType.DATANODE);
						String id = element.getGraphId();
						
						// PathwayElement erbij halen
						
						pathway.add(element);
					}					
					else if(type.equals("map"))
					{
						Element graphics = child.getChild("graphics");
						int i = 0;
						
						String textlabelGPML = child.getAttributeValue("name"); 
						String typeGPML = null;
						
						PathwayElement element = new PathwayElement(ObjectType.LABEL);
						String id = element.getGraphId();
						element.setMFontSize(150);
						
						// PathwayElement erbij halen
						
						pathway.add(element);
					}
				}
			}	
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
					result.add(ldb.getEntry_id2());
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
	
	public static PathwayElement createPathwayElement(Element child, PathwayElement element, int i, String textlabelGPML, String typeDatanode)
	{
		//Create new pathway element
	
		// Set Color
		// Convert a hexadecimal color into an awt.Color object
		// Remove the # before converting
		String colorStringGPML = child.getAttributeValue("fgcolor");
		Color colorGPML = GpmlFormat.gmmlString2Color(colorStringGPML.substring(1));
		
		// Set x, y, width, height 
		String centerXGPML = child.getAttributeValue("x");
		String centerYGPML = child.getAttributeValue("y");
		String widthGPML = child.getAttributeValue("width");
		String heightGPML = child.getAttributeValue("height");
		
		double height = Double.parseDouble(heightGPML);
		double centerY = Double.parseDouble(centerYGPML) - i*height;
		
		element.setMCenterX(Double.parseDouble(centerXGPML));
		element.setMCenterY(centerY);
		element.setMWidth(Double.parseDouble(widthGPML));
		element.setMHeight(height);
		
		// Set textlabel
		element.setTextLabel(textlabelGPML);
		 
		element.setDataNodeType(typeDatanode);
		
		return element;
	}
	
	
}

