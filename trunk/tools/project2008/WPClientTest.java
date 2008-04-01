import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;


public class WPClientTest 
{
	public static void main(String[] args) throws XmlRpcException, IOException
	{
		WikiPathwaysClient wp = new WikiPathwaysClient();
		
		List<String> pathwayNames = wp.getPathwayList();
		
		for (int i = 0; i < pathwayNames.size(); ++i)
		{
			// path om files in op te slaan
			String path = "C:\\WPClient\\";
			// soort extracten
			String pathwaynaam = pathwayNames.get(i);
			String[] temp = pathwaynaam.split(":");
			String soort = temp[0];
			
			// naam extracten
			String naamPathway = temp[1];
			
			//	mapje aanmaken als deze nog niet bestaat;
			new File(path + soort + "\\").mkdir();
			
			wp.downloadPathway(pathwayNames.get(i), 
				new File (path + soort + "\\" + naamPathway + ".gpml"));
			System.out.println("Downloaded: " + pathwayNames.get(i));
		}
	}
}
