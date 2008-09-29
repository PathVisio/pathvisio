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
package org.pathvisio.wikipathways;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.rpc.ServiceException;

import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.Organism;
import org.pathvisio.model.Pathway;
import org.pathvisio.view.MIMShapes;
import org.pathvisio.wikipathways.webservice.WSAuth;
import org.pathvisio.wikipathways.webservice.WSCurationTag;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;
import org.pathvisio.wikipathways.webservice.WikiPathwaysLocator;
import org.pathvisio.wikipathways.webservice.WikiPathwaysPortType;

/**
 * A client API that provides access to the WikiPathways SOAP webservice.
 * For more documentation on the webservice, see:
 * http://www.wikipathways.org/index.php/Help:WikiPathways_Webservice
 * @author thomas
 *
 */
public class WikiPathwaysClient {
	private WikiPathwaysPortType port;
	
	private WSAuth auth;
	
	public WikiPathwaysClient() throws ServiceException {
		this(null);
	}
	
	public WikiPathwaysClient(URL portAddress) throws ServiceException {
		if(portAddress != null) {
			port = new WikiPathwaysLocator().getWikiPathwaysSOAPPort_Http(portAddress);
		} else {
			port = new WikiPathwaysLocator().getWikiPathwaysSOAPPort_Http();
		}
		MIMShapes.registerShapes();
	}
	
	/**
	 * Get a pathway from WikiPathways.
	 * @see #toPathway(WSPathway)
	 */
	public WSPathway getPathway(String name, Organism species) throws RemoteException, ConverterException {
		return getPathway(name, species, 0);
	}
	
	/**
	 * List all pathways on WikiPathways
	 */
	public WSPathwayInfo[] listPathways() throws RemoteException {
		WSPathwayInfo[] r = port.listPathways();
		if(r == null) r = new WSPathwayInfo[0];
		return r;
	}
	
	/**
	 * Get a specific revision of a pathway from WikiPathways
	 * @see #toPathway(WSPathway)
	 */
	public WSPathway getPathway(String name, Organism species, int revision) throws RemoteException, ConverterException {
		WSPathway wsp = port.getPathway(name, species.latinName(), revision);
		return wsp;
	}
	
	/**
	 * Utility method to create a pathway model from the webservice class
	 * WSPathway.
	 * @param wsp The WSPathway object returned by the webservice.
	 * @return The org.pathvisio.model.Pathway model representation of the GPML code.
	 * @throws ConverterException 
	 */
	public static Pathway toPathway(WSPathway wsp) throws ConverterException {
		Pathway p = new Pathway();
		p.readFromXml(new StringReader(wsp.getGpml()), true);
		return p;
	}
	
	/**
	 * Update a pathway on WikiPathways.
	 * Note: you need to login first, see: {@link #login(String, String)}.
	 * @param pwName The name of the pathway on WikiPathways
	 * @param pathway The updated pathway data
	 * @param description A description of the changes
	 * @param revision The revision these changes were based on (to prevent conflicts)
	 */
	public void updatePathway(String pwName, Pathway pathway, String description, int revision) throws ConverterException, RemoteException {
		String species = pathway.getMappInfo().getOrganism();
		ByteArrayOutputStream out = new ByteArrayOutputStream(); 
		GpmlFormat.writeToXml(pathway, out, true);
		String gpml = out.toString();
		port.updatePathway(pwName, species, description, gpml, revision, auth);
	}
	
	/**
	 * Apply a curation tag to a pathway. Will overwrite existing tags with the same name.
	 * @param pwName The name of the pathway to apply the tag to
	 * @param species The species of the pathway to apply the tag to
	 * @param tagName The name of the tag (e.g. CurationTag:Approved)
	 * @param tagText The tag text
	 * @param revision The revision to apply the tag to
	 * @throws RemoteException
	 */
	public void saveCurationTag(String pwName, Organism species, String tagName, String tagText, int revision) throws RemoteException {
		port.saveCurationTag(pwName, species.latinName(), tagName, tagText, revision, auth);
	}
	
	/**
	 * Apply a curation tag to a pathway. Will overwrite existing tags with the same name.
	 * @param pwName The name of the pathway to apply the tag to
	 * @param species The species of the pathway to apply the tag to
	 * @param tagName The name of the tag (e.g. CurationTag:Approved)
	 * @param tagText The tag text
	 * @throws RemoteException
	 */
	public void saveCurationTag(String pwName, Organism species, String tagName, String text) throws RemoteException {
		saveCurationTag(pwName, species, tagName, text, 0);
	}
	
	/**
	 * Remove the given curation tag from the pathway
	 * @param pwName The name of the pathway to apply the tag to
	 * @param species The species of the pathway to apply the tag to
	 * @param tagName The name of the tag (e.g. CurationTag:Approved)
	 * @throws RemoteException
	 */
	public void removeCurationTag(String pwName, Organism species, String tagName) throws RemoteException {
		port.removeCurationTag(pwName, species.latinName(), tagName, auth);
	}
	
	/**
	 * Get all curation tags for the given pathway
	 * @param pwName The name of the pathway
	 * @param species The species of the pathway
	 * @return An array with the curation tags.
	 * @throws RemoteException
	 */
	public WSCurationTag[] getCurationTags(String pwName, Organism species) throws RemoteException {
		WSCurationTag[] tags = port.getCurationTags(pwName, species.latinName());
		if(tags == null) tags = new WSCurationTag[0];
		return tags;
	}
	
	/**
	 * Login using your WikiPathways account. You need to login in order
	 * to make changes to pathways.
	 * @param name The username of the WikiPathways account
	 * @param pass The password of the WikiPathways account
	 * @throws RemoteException
	 */
	public void login(String name, String pass) throws RemoteException {
		auth = new WSAuth(name, port.login(name, pass));
	}
	
	/**
	 * Get a list of recently changed pathways.
	 * @param cutoff Only return changes since this date.
	 * @throws RemoteException
	 */
	public WSPathwayInfo[] getRecentChanges(Date cutoff) throws RemoteException {
		// turn Date into expected timestamp format, in GMT:
		SimpleDateFormat sdf = new SimpleDateFormat ("yyyyMMddHHmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String timestamp = sdf.format(cutoff);
		WSPathwayInfo[] changes = port.getRecentChanges(timestamp);
		if(changes == null) changes = new WSPathwayInfo[0];
		return changes;
	}
}
