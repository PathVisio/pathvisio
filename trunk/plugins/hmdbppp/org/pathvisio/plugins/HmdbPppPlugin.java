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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.pathvisio.biopax.reflect.PublicationXRef;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

/**
 * Generates Putative Pathway Parts based on a 
 * HMDB metabolic network parsed and stored in MySQL by Andra.
 */
public class HmdbPppPlugin {

    private static final double HUB_X = 2000;
    private static final double HUB_Y = 2000;
    private static final double RADIUS = 1000;
    
	public Pathway doSuggestion(PathwayElement input)
	{
		try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }
        
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
        Connection conn;
		try {
		    conn = 
		       DriverManager.getConnection("jdbc:mysql://localhost/hmdb?" + 
		                                   "user=pathvisio");
		    

		    
		    PreparedStatement st = conn.prepareStatement (
		    		"SELECT DISTINCT " +
		    			"h.` Ref_ID`,h.` Common_Name`,h.` Accession_No`, " +
		    			"m.Swissprot_ID, m.Gene_Name, m.References " +
		    		"FROM " +
		    			"hmdb h, macromolecular_interacting_partner m " +
		    		"WHERE " +
		    			"h.` Accession_No` = ? " +
		    			"AND h.` Accession_No` = m.hmdbid");

		    st.setString(1, ref.getId());
		    ResultSet rs = st.executeQuery();
		    
		    double angle = 0;
		    int noRecords = 0;
		    while (rs.next()){
		    	noRecords += 1;
		    }
		    ResultSet rsf = st.executeQuery();
		    double incrementStep = 0;
		    incrementStep = (2* Math.PI)/noRecords;
		    while (rsf.next())
		    {
		    	//Each tuple is a pathway element
		    	PathwayElement pchildElt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		    	PathwayElement connectElement = PathwayElement.createPathwayElement(ObjectType.LINE);
		    	
		    	// Get common_name for the metabolite (rs.getString(2))
		    	String commonName = null;
		    	commonName = rsf.getString(2);
		    	System.out.println ("\t" + rsf.getString(2));
		    	
		    	String swissId = rsf.getString(4);
		    	
		    	int start = swissId.indexOf('(');
		    	int end = swissId.lastIndexOf(')');
				String actualId = swissId.substring(start + 1, end);
				
				//Get gene_name (rs.getString())
		    	String geneName = rsf.getString(5);
		    	connectElement.setMStartX(HUB_X);
		    	connectElement.setMStartY(HUB_Y);
		    	connectElement.setMEndX(HUB_X + RADIUS * Math.cos(angle));
		    	connectElement.setMEndY(HUB_Y + RADIUS * Math.sin(angle));
		    	pchildElt.setDataNodeType (DataNodeType.PROTEIN);
		    	pchildElt.setTextLabel(geneName);
		    	pchildElt.setDataSource (DataSource.UNIPROT);
		    	pchildElt.setGeneID(actualId);
			    pchildElt.setMWidth (1200);
			    pchildElt.setMHeight (300);
			    pchildElt.setMCenterX(HUB_X + RADIUS * Math.cos(angle));
			    pchildElt.setMCenterY(HUB_Y + RADIUS * Math.sin(angle));
			    result.add(pchildElt);
			    result.add(connectElement);
		    	angle += incrementStep;
		    	System.out.println(angle);
		    	
		    	// Split Ref_ID, which contains pubmed_id's to the selected metabolite rs.getString(1)
		    	String [] metabolitePmids = null;
		    	metabolitePmids = rsf.getString(1).split("; ");
		    	for (int i = 0 ; i < metabolitePmids.length ; i++) {
		    		PublicationXRef xref = new PublicationXRef();
		    		xref.setPubmedId(metabolitePmids[i]);
		    		pchildElt.getBiopaxReferenceManager().addElementReference(xref);
		            System.out.println(metabolitePmids[i]);
		    	}
		    }
		    
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		return result;
	}
	
	
	/**
	 * @param args
	 * @throws ConverterException 
	 */
	public static void main(String[] args) throws IOException, ConverterException
	{
		HmdbPppPlugin hmdbPpp = new HmdbPppPlugin();
	    
		PathwayElement test = PathwayElement.createPathwayElement(ObjectType.DATANODE);
	    test.setDataNodeType(DataNodeType.METABOLITE);
	    test.setTextLabel("Androsterone");
		test.setGeneID("HMDB00031");
		test.setDataSource(DataSource.HMDB);
		
		Pathway p = hmdbPpp.doSuggestion(test);
		
		File tmp = File.createTempFile("hmdbppp", ".gpml");
		p.writeToXml(tmp, true);
		
		BufferedReader br = new BufferedReader(new FileReader(tmp));
		String line;
		while ((line = br.readLine()) != null)
		{
			System.out.println (line);
			
			
		}
	}

}
