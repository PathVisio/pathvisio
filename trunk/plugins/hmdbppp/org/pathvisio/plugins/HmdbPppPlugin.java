package org.pathvisio.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

public class HmdbPppPlugin {

	public Pathway doSuggestion()
	{
		try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }
        
        double HUB_X = 10000;
        double HUB_Y = 10000;
        double RADIUS = 5000;
        
	    Pathway result = new Pathway();
	    PathwayElement pelt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
	    pelt.setMWidth (1200);
	    pelt.setMHeight (300);
	    pelt.setMCenterX(HUB_X);
	    pelt.setMCenterY(HUB_Y);
	    pelt.setTextLabel("Androsterone");
	    pelt.setDataSource(DataSource.HMDB);
	    pelt.setGeneID("HMDB00031");
	    Xref input = pelt.getXref();
	    pelt.setCopyright("Human metabolome database (http://www.hmdb.ca)");
	    pelt.setDataNodeType("Metabolite");
	    result.add(pelt);
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

		    st.setString(1, input.getId());
		    ResultSet rs = st.executeQuery();
		    
		    double angle = 0;
		    
		    while (rs.next())
		    {
		    	//Each tuple is a pathway element
		    	PathwayElement pchild_elt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		    	

		    	// Split Ref_ID, which contains pubmed_id's to the selected metabolite rs.getString(1)
		    	String [] metabolite_pmids = null;
		    	metabolite_pmids = rs.getString(1).split("; ");
		    	for (int i = 0 ; i < metabolite_pmids.length ; i++) {
		            System.out.println(metabolite_pmids[i]);
		    	}
		    	
		    	// Get common_name for the metabolite (rs.getString(2))
		    	String common_name = null;
		    	common_name = rs.getString(2);
		    	System.out.println ("\t" + rs.getString(2));
		    	
		    	String swiss_id = rs.getString(4);
		    	
		    	int start = swiss_id.indexOf('(');
		    	int end = swiss_id.lastIndexOf(')');
				String actual_id = swiss_id.substring(start + 1, end);
				
				//Get gene_name (rs.getString())
		    	String gene_name = rs.getString(5);
		    	
		    	pchild_elt.setDataNodeType (DataNodeType.PROTEIN);
		    	pchild_elt.setTextLabel(gene_name);
		    	pchild_elt.setDataSource (DataSource.UNIPROT);
		    	pchild_elt.setGeneID(actual_id);
			    pchild_elt.setMWidth (1200);
			    pchild_elt.setMHeight (300);
			    pchild_elt.setMCenterX(HUB_X + RADIUS * Math.cos(angle));
			    pchild_elt.setMCenterY(HUB_Y + RADIUS * Math.sin(angle));
			    result.add(pchild_elt);
		    	angle += 0.3;
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
		Pathway p = hmdbPpp.doSuggestion();
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
