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
package org.pathvisio.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.org.apache.xerces.internal.impl.xs.dom.DOMParser;

import javax.xml.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;



/**
 * Generates Putative Pathway Parts based on a 
 * HMDB metabolic network parsed and stored in MySQL by Andra.
 */
public class PhasarPppPlugin {

    private static final double HUB_X = 4000;
    private static final double HUB_Y = 4000;
    private static final double RADIUS = 2500;
    
    static class MyHandler extends DefaultHandler {
    }
    
	public Pathway doSuggestion(PathwayElement input) throws IOException
	{
	/*	try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }*/
        
	    Pathway result = new Pathway();
	    PathwayElement pelt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
	    pelt.setMWidth (1200);
	    pelt.setMHeight (300);
	    pelt.setMCenterX(HUB_X);
	    pelt.setMCenterY(HUB_Y);
	    pelt.setTextLabel(input.getTextLabel());
	    pelt.setDataSource(input.getDataSource());
	    pelt.setGeneID(input.getGeneID());
	    pelt.setCopyright("Human metabolome database (http://www.hmdb.ca)");
	    pelt.setDataNodeType(input.getDataNodeType());
	    result.add(pelt);
	    Xref ref = input.getXref();

       
		double angle = 0;
		int noRecords = 0;
		String aLine;
		
		//InputStream urlInputStream = null;
 
		
		
		/*String urlString = "http://www.bioinformatics.nl/biometa/webservice/connections/getsuggestion/"+ref.getId();
		 System.out.println(urlString);
		URL url = new URL(urlString);
		//urlInputStream = url.openConnection().getInputStream();
				
		InputStream in = url.openStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String tempPhasarResult = null;
		
		
		while ((aLine = br.readLine()) != null ) {
			System.out.println(aLine);
			tempPhasarResult += aLine;
		}
		
		File tmp = File.createTempFile("phasartempresult", ".xml");
		System.out.println(tmp.getName());*/
		DOMParser parser = new DOMParser();
		try {
			parser.parse("/temp/phasar.xml");
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		org.w3c.dom.Document doc  = parser.getDocument();
		NodeList list = doc.getElementsByTagName("identifier");
		double incrementStep = (2* Math.PI)/list.getLength();
		for(int i = 0, length = list.getLength(); i < length; i++){
			PathwayElement pchildElt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
	    	PathwayElement connectElement = PathwayElement.createPathwayElement(ObjectType.LINE);
	    	
	    	connectElement.setMStartX(HUB_X);
	    	connectElement.setMStartY(HUB_Y);
	    	connectElement.setMEndX(HUB_X + RADIUS * Math.cos(angle));
	    	connectElement.setMEndY(HUB_Y + RADIUS * Math.sin(angle));
	    	
			Element neighbour  = (Element)list.item(i);
			Attr identifier = neighbour.getAttributeNode("name");
			Attr type = neighbour.getAttributeNode("type");
			//System.out.println(identifier.getTextContent()+":"+type.getTextContent());
	    	if (type.getTextContent() == "enzyme") {
	    		pchildElt.setDataNodeType (DataNodeType.GENEPRODUCT);
	    		pchildElt.setDataSource (DataSource.ENZYME_CODE);
	    	}
	    	if (type.getTextContent() == "compound") {
	    		pchildElt.setDataNodeType (DataNodeType.METABOLITE);
	    		pchildElt.setDataSource (DataSource.KEGG_COMPOUND);
	    	}
	    	pchildElt.setTextLabel(identifier.getTextContent()); //TODO find the appropriate textLabel instead of identifier
	        pchildElt.setGeneID(identifier.getTextContent());    	
	    	
		    pchildElt.setMWidth (1200);
		    pchildElt.setMHeight (300);
		    pchildElt.setMCenterX(HUB_X + RADIUS * Math.cos(angle));
		    pchildElt.setMCenterY(HUB_Y + RADIUS * Math.sin(angle));
		    result.add(pchildElt);
		    result.add(connectElement);
	    	angle += incrementStep;
	    	
	    	

		}
		/*double incrementStep = (2* Math.PI)/noRecords;
		int row = 0;
		in.close();
		InputStream in2 = url.openStream();
		BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
		while ((aLine = br2.readLine()) != null ) {
			String[] velden = null;
			velden = aLine.split(",");
			if (row >0) { //Each row except the header
				PathwayElement pchildElt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		    	PathwayElement connectElement = PathwayElement.createPathwayElement(ObjectType.LINE);
		    	String swissId = velden[3];
		    	System.out.println(swissId);
		    	String geneName = velden[2];
		    	connectElement.setMStartX(HUB_X);
		    	connectElement.setMStartY(HUB_Y);
		    	connectElement.setMEndX(HUB_X + RADIUS * Math.cos(angle));
		    	connectElement.setMEndY(HUB_Y + RADIUS * Math.sin(angle));
		    	pchildElt.setDataNodeType (DataNodeType.PROTEIN);
		    	pchildElt.setTextLabel(geneName);
		    	pchildElt.setDataSource (DataSource.UNIPROT);
		    	pchildElt.setGeneID(swissId);
			    pchildElt.setMWidth (1200);
			    pchildElt.setMHeight (300);
			    pchildElt.setMCenterX(HUB_X + RADIUS * Math.cos(angle));
			    pchildElt.setMCenterY(HUB_Y + RADIUS * Math.sin(angle));
			    result.add(pchildElt);
			    result.add(connectElement);
		    	angle += incrementStep;
			}
			
			row++; //exclude row with headings
		}*/
		return result;
	}
	
	
	/**
	 * @param args
	 * @throws ConverterException 
	 */
	public static void main(String[] args) throws IOException, ConverterException
	{
		PhasarPppPlugin phasarPpp = new PhasarPppPlugin();
	    
		PathwayElement test = PathwayElement.createPathwayElement(ObjectType.DATANODE);
	    test.setDataNodeType(DataNodeType.PROTEIN);
	    test.setTextLabel("MAPK");
		test.setGeneID("EC.2.7.11.24:");
		test.setDataSource(DataSource.ENZYME_CODE);
		
		Pathway p = phasarPpp.doSuggestion(test);
		
		File tmp = File.createTempFile("phasarppp", ".gpml");
		p.writeToXml(tmp, true);
		
		BufferedReader br = new BufferedReader(new FileReader(tmp));
		String line;
		while ((line = br.readLine()) != null)
		{
			System.out.println (line);
			
			
		}
	}

}
