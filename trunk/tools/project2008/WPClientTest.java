import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;


public class WPClientTest 
{
	public static void main(String[] args) throws XmlRpcException, IOException
	{
		// make a new WikiPathwaysClient
		WikiPathwaysClient wp = new WikiPathwaysClient();
		
		// get the pathwaylist; all the known pathways are
		// stored in a list
		List<String> pathwayNames = wp.getPathwayList();
		
		
		// path to store the pathway cache
		String path = "C:\\WPClient\\";
		
		// a for loop that downloads all individual pathways
		for (int i = 0; i < pathwayNames.size(); ++i)
		{

			// get the species and pathwayname
			String pathwayName= pathwayNames.get(i);
			String[] temporary = pathwayName.split(":");
			String species = temporary[0];
			String namePathway = temporary[1];
			
			//	make a folder for a species when it doesn't exist
			new File(path + species + "\\").mkdir();
			
			wp.downloadPathway(pathwayNames.get(i), 
				new File (path + species + "\\" + namePathway + ".gpml"));
			System.out.println("Downloaded: " + pathwayNames.get(i));
		}
	}
}
