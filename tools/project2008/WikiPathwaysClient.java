// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.pathvisio.debug.Logger;


public class WikiPathwaysClient 
{
	private static final String RPCURL = "http://137.120.89.38/wikipathways-test/wpi/wpi_rpc.php";
	//private static final String RPCURL = "http://localhost/wikipathways/wpi/wpi_rpc.php";
	private XmlRpcClient client;
	
	/**
	 * A representation of WikiPathways that allows you to obtain a list 
	 * of currently available pathways,
	 * as well as the pathways themselves
	 */
	public WikiPathwaysClient() 
	{
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		
		try
		{
			config.setServerURL(new URL(RPCURL));

			client = new XmlRpcClient();
			client.setConfig (config);
			
		}
		catch (MalformedURLException e)
		{
			// shouldn't occur
			Logger.log.error ("Malformed url:", e);
		}
	
	}
	
	/**
	 * Get a list of all available pathways on wikipathways.
	 * The returned list contains Strings of the form
	 * 
	 * Species:PathwayName
	 */
	public List<String> getPathwayList () throws XmlRpcException
	{
		beforeRequest();
		Object response = client.execute ("WikiPathways.getPathwayList", (Object[])null);
		afterRequest();
		
		Object[] objs = (Object[])response;
		List<String> result = new ArrayList<String>(); 
		for (Object o : objs)
		{
			result.add ("" + o);
		}
		return result;
	}

	public List<String> getRecentChanges (Date cutoff) throws XmlRpcException
	{		
		// turn Date into expected timestamp format:
		String timestamp = new SimpleDateFormat ("yyyyMMddHHmmss").format(cutoff);
		Object[] params = new Object[] { timestamp };
		
		beforeRequest();
		Object response = client.execute ("WikiPathways.getRecentChanges", params);
		afterRequest();
		
		Object[] objs = (Object[])response;
		List<String> result = new ArrayList<String>(); 
		for (Object o : objs)
		{
			result.add ("" + o);
		}
		return result;
	}

	static private long lastRequest = 0;
	
	/** minimum number of milliseconds between requests */ 
	private final long REQUEST_WAIT_MILLIS = 3000;
	
	/** waits before new request */
	private void beforeRequest()
	{
		long now = System.currentTimeMillis();
		long remain = (lastRequest + REQUEST_WAIT_MILLIS) - now;
		if (remain > 0)
		{
			try
			{
				Thread.sleep (remain);
			}
			catch (InterruptedException e)
			{
				// safely ignore
			}
		}
	}
	
	private void afterRequest()
	{
		lastRequest = System.currentTimeMillis();
	}
	
	/**
	 * Download a pathway from wikipathways.
	 * 
	 * @param fullName has to be of the form Homo_sapiens:pwyname, 
	 *        (as returned by getPathwayList)
	 * @param destination: the file where the pathway will be stored.
	 *        any existing files will be overwritten
	 *        
	 * @returns true if the file was succesfully downloaded
	 */
	public boolean downloadPathway (String fullName, File destination) throws XmlRpcException, IOException
	{

		String[] fields = fullName.split(":");
		if (fields.length != 2) throw new IllegalArgumentException ("fullName must be of the form species:name");
		
		Object[] params = new Object[] {
				fields[1],
				fields[0]
		};
		beforeRequest();
		Object response = client.execute ("WikiPathways.getPathway", params);
		afterRequest();
		
		try
		{
			Map<String,Object> response2 = (HashMap<String,Object>)response;
			
			String data = (String)response2.get("gpml");
			byte[] converted = Base64.decodeBase64(data.getBytes());
	
			FileOutputStream fout = new FileOutputStream(destination);
			fout.write(converted);
			fout.close();
			return true;
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}
}
