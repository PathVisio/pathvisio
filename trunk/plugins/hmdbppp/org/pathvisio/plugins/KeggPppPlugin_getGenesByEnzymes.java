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

import javax.xml.rpc.ServiceException;

import keggapi.KEGGLocator;
import keggapi.KEGGPortType;

import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;


/**
 * Generates Putative Pathway Parts based on a 
 * Phasar.
 */
public class KeggPppPlugin_getGenesByEnzymes {

    private static final double HUB_X = 4000;
    private static final double HUB_Y = 4000;
    private static final double RADIUS = 2500;
    
	public Pathway doSuggestion(PathwayElement input) throws IOException, ServiceException
	{

        
	    Pathway result = new Pathway();
	    PathwayElement pelt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
	    pelt.setMWidth (1200);
	    pelt.setMHeight (300);
	    pelt.setMCenterX(HUB_X);
	    pelt.setMCenterY(HUB_Y);
	    pelt.setTextLabel(input.getTextLabel());
	    pelt.setDataSource(input.getDataSource());
	    pelt.setGeneID(input.getGeneID());
	    pelt.setCopyright("KEGG (http://www.genome.jp/kegg/");
	    pelt.setDataNodeType(input.getDataNodeType());
	    result.add(pelt);
	    Xref ref = input.getXref();

       
		double angle = 0;
		int noRecords = 0;

		
        KEGGLocator    locator  = new KEGGLocator();
        KEGGPortType   serv     = locator.getKEGGPort();
		
		String[] results  = null;
        results = serv.get_genes_by_enzyme(input.getGeneID(), input.getOrganism());
        System.out.println(results.length +":"+input.getGeneID()+" "+input.getOrganism());
        noRecords = results.length;
		double incrementStep = (2* Math.PI)/noRecords;
        for (int i = 0; i < results.length; i++) {
            PathwayElement pchildElt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
            PathwayElement connectElement = PathwayElement.createPathwayElement(ObjectType.LINE);
	    	connectElement.setMStartX(HUB_X);
	    	connectElement.setMStartY(HUB_Y);
	    	connectElement.setMEndX(HUB_X + RADIUS * Math.cos(angle));
	    	connectElement.setMEndY(HUB_Y + RADIUS * Math.sin(angle));
	    	pchildElt.setDataNodeType (DataNodeType.GENEPRODUCT);
	    	String btitTextLabel = serv.btit(results[i]);
	    	System.out.println(btitTextLabel);
	    	String[] textLabel_intermediate = btitTextLabel.split("; ");
	    	String[] textLabel = textLabel_intermediate[0].split(" ");
	    	pchildElt.setTextLabel(textLabel[1]);
	    	pchildElt.setDataSource(DataSource.KEGG_COMPOUND);
	    	pchildElt.setGeneID(textLabel[0]);
	    	pchildElt.setMWidth (1200);
		    pchildElt.setMHeight (300);
		    pchildElt.setMCenterX(HUB_X + RADIUS * Math.cos(angle));
		    pchildElt.setMCenterY(HUB_Y + RADIUS * Math.sin(angle));
		    result.add(pchildElt);
		    result.add(connectElement);
	    	angle += incrementStep;
        }
		return result;
	}
	
	
	/**
	 * @param args
	 * @throws ConverterException 
	 * @throws ServiceException 
	 */
	public static void main(String[] args) throws IOException, ConverterException, ServiceException
	{
		KeggPppPlugin_getGenesByEnzymes keggPpp = new KeggPppPlugin_getGenesByEnzymes();
	    
		PathwayElement test = PathwayElement.createPathwayElement(ObjectType.DATANODE);
	    test.setDataNodeType(DataNodeType.PROTEIN);
	    test.setTextLabel("ALDH1A2");
		test.setGeneID("ec:1.3.99.6");
		test.setOrganism("hsa");
		test.setDataSource(DataSource.ENTREZ_GENE);
		
		Pathway p = keggPpp.doSuggestion(test); 
		
		File tmp = File.createTempFile("keggppp", ".gpml");
		p.writeToXml(tmp, true);
		
		BufferedReader br = new BufferedReader(new FileReader(tmp));
		String line;
		while ((line = br.readLine()) != null)
		{
			System.out.println (line);
			
			
		}
	}

}
