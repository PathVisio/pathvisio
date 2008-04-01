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
		
		for (int i = 0; i < 5; ++i)
		{
			wp.downloadPathway(pathwayNames.get(i), 
				new File ("/home/martijn/Desktop/pw " + i + ".gpml"));
		}
	}
}
