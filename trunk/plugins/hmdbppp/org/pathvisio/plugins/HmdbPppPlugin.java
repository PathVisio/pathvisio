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


/**
 * Generates Putative Pathway Parts based on a 
 * HMDB metabolic network parsed and stored in MySQL by Andra.
 */
public class HmdbPppPlugin {

    private static final double HUB_X = 4000;
    private static final double HUB_Y = 4000;
    private static final double RADIUS = 2500;
    
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
		
		String urlString = "http://www.hmdb.ca/cgi-bin/extractor_runner.cgi?metabolites_hmdb_id="+ref.getId()+"&format=csv&select_enzymes_gene_name=on&select_enzymes_swissprot_id=on";
		 System.out.println(urlString);
		URL url = new URL(urlString);
		InputStream in = url.openStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		while ((br.readLine()) != null ) {
			noRecords += 1;
		}
		double incrementStep = (2* Math.PI)/noRecords;
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
