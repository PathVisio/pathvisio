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

package org.pathvisio.cytoscape.superpathways;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
import org.bridgedb.rdb.DataDerby;
import org.bridgedb.rdb.SimpleGdb;
import org.bridgedb.rdb.SimpleGdbFactory;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.webservice.WSPathway;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;

public class CommonNodeView {

	static List<String> selectedPwsNameId;

	static SuperpathwaysClient mClient;

	// static String dbLocation = "C:/Documents and
	// Settings/xuemin/PathVisio-Data/gene databases/";
	static String dbLocation = GlobalPreference.getDataDir().toString()
			+ "\\gene databases\\";
	
	static Map<Xref, Xref> nodePairByTranslation;

	/**
	 * Create an exporter that uses the given GdbManager to lookup cross
	 * references for each datanode
	 */

	public CommonNodeView(List<String> pathwaysNameId, SuperpathwaysClient c) {
		selectedPwsNameId = pathwaysNameId;
		mClient = c;
		nodePairByTranslation=new HashMap<Xref, Xref>();

	}

	public static commonNodePathwayPair findCommonNode(String pw1NameId,
			String pw2NameId) {
		commonNodePathwayPair cnPwPair = new commonNodePathwayPair();

		int commonNode = 0;
		// get the id of the first pathway
		int index1 = pw1NameId.indexOf("(");
		int index2 = pw1NameId.indexOf(")");
		String pw1ID = pw1NameId.substring(index1 + 1, index2);

		// get the id of the second pathway
		index1 = pw2NameId.indexOf("(");
		index2 = pw2NameId.indexOf(")");
		String pw2ID = pw2NameId.substring(index1 + 1, index2);

		// Create a client to the WikiPathways web service
		WikiPathwaysClient client = mClient.getStub();

		// Download these two pathways from WikiPathways by passing their id
		WSPathway wsPathway1 = new WSPathway();
		WSPathway wsPathway2 = new WSPathway();
		try {
			wsPathway1 = client.getPathway(pw1ID);
			wsPathway2 = client.getPathway(pw2ID);

		} catch (RemoteException e) {
			Logger.log.error(
					"Unable to get the pathway due to the RemoteException", e);
		} catch (ConverterException e) {
			Logger.log.error(
					"Unable to get the pathway due to the ConverterException",
					e);
		}
		// Create two corresponding pathway objects
		Pathway pathway1 = new Pathway();
		Pathway pathway2 = new Pathway();
		try {
			pathway1 = WikiPathwaysClient.toPathway(wsPathway1);
			pathway2 = WikiPathwaysClient.toPathway(wsPathway2);
		} catch (ConverterException e) {
			Logger.log.error(
					"Unable to get the pathway due to the RemoteException", e);
		}

		List<Xref> XrefListPw1 = new ArrayList<Xref>();
		List<Xref> XrefListPw2 = new ArrayList<Xref>();
		List<String> XrefListCommonNode = new ArrayList<String>();

		// create the list of Xref for the pathway1: XrefListPw1
		System.out.println("Xref info of one pathway: " + pw1NameId);
		for (PathwayElement pw1Elm : pathway1.getDataObjects()) {
			// Only take elements with type DATANODE (genes, proteins,
			// metabolites)

			// for figuring out the problem of gpml
			// System.out.println(pw1Elm.getObjectType());

			if (pw1Elm.getObjectType() == ObjectType.DATANODE) {

				String id = pw1Elm.getGeneID();
				DataSource ds = pw1Elm.getDataSource();
				if (!checkString(id) || ds == null) {
					continue; // Skip empty id/codes
				}

				// System.out.println(pwElm.getGeneID());
				// geneIDListPw1.add(pw1Elm.getGeneID());

				// System.out.println("before translation: ");
				//System.out.println(pw1Elm.getXref().toString());

				XrefListPw1.add(pw1Elm.getXref());
			}

		}

		// deternmine which database to be loaded according to the species of
		// pathway2
		Organism organism = Organism.fromLatinName(pathway2.getMappInfo()
				.getOrganism());
		// System.out.println("The organism for pathway2 is " + organism);

		String orgCode = organism.code();
		// System.out.println("The organism code for pathway2 is " + orgCode);

		File dir = new File(dbLocation);
		// try {

		SimpleGdb gdb = null;
		String[] pgdbFileName = dir.list();

		if (pgdbFileName == null) {

			JOptionPane
					.showMessageDialog(
							mClient.getGUI(),
							"There's no database in the default folder, please choose the directory where you've loaded the databases!");

			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle("Choose the direcotry where you've loaded databases...");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			// disable the "All files" option.
			chooser.setAcceptAllFileFilterUsed(false);

			if (chooser.showOpenDialog(mClient.getPlugin().mWindow) == JFileChooser.APPROVE_OPTION) {
				System.out.println("getCurrentDirectory(): "
						+ chooser.getCurrentDirectory());
				System.out.println("getSelectedFile() : "
						+ chooser.getSelectedFile());
				dir = chooser.getSelectedFile();
				dbLocation=chooser.getSelectedFile().toString() +"\\";
				pgdbFileName = dir.list();
			}

		}
		for (int i = 0; i < pgdbFileName.length; i++) {

			String fileName = pgdbFileName[i];
			int index = fileName.indexOf("_");
			String speciesOrMetabolite = fileName.substring(0, index);
			//System.out.println(speciesOrMetabolite);
			if (speciesOrMetabolite.equals((Object) orgCode)) {
				System.out.println(dbLocation + fileName);
				File fGdb = new File(dbLocation + fileName);
				// System.out.println(speciesOrMetabolite);
				try {
					gdb = SimpleGdbFactory.createInstance("" + fGdb,
							new DataDerby(), 0);
				} catch (IDMapperException e) {
					Logger.log.error("Problem while connecting to the Gdb", e);

				}
				break;
			}

		}

		/*if (gdb == null) {
			System.out
					.println("Cannot find the corresponding database for the organism of pathway2!");
		}*/

		// create the list of Xref for the pathway2: XrefListPw2
		System.out.println("Xref info of the other pathway: " + pw1NameId);
		for (PathwayElement pw2Elm : pathway2.getDataObjects()) {
			if (pw2Elm.getObjectType() == ObjectType.DATANODE) {

				boolean isMapped = false;

				String id = pw2Elm.getGeneID();
				DataSource ds = pw2Elm.getDataSource();
				if (!checkString(id) || ds == null) {
					continue; // Skip empty id/codes
				}

				// geneIDListPw2.add(pw2Elm.getGeneID());
				// System.out.println("before translation: ");

				//System.out.println(pw2Elm.getXref().toString());
				XrefListPw2.add(pw2Elm.getXref());
				
				
				for (int k = 0; k < XrefListPw1.size(); k++) {
					try {
						List xrefs2 = gdb.getCrossRefs(pw2Elm.getXref());
						if (xrefs2.contains(XrefListPw1.get(k))) {
							isMapped = true;
							
							//this map is for later use--when merging pathways
							nodePairByTranslation.put(pw2Elm.getXref(), XrefListPw1.get(k));							
						}
					} catch (IDMapperException e) {
						Logger.log
								.error(
										"Problem while getting the all the Xrefs for the pathwayElement of pw2!",
										e);
						System.out
								.println("Problem while getting the all the Xrefs for the pathwayElement of pw2!");
						break;
					}
				}

				if ((XrefListPw1.contains(pw2Elm.getXref().toString()) || isMapped)
						&& !XrefListCommonNode.contains(pw2Elm.getXref()
								.toString())) {
					// maybe need to change later!!
					commonNode = commonNode + 1;
					XrefListCommonNode.add(pw2Elm.getXref().toString());

				}
			}
		}

		/*
		 * } catch (NullPointerException e) { Logger.log.error( "Problem while
		 * null Pointer of pdgbFileName empty", e); }
		 */

		// the following code is for printing out the common nodes returned by
		// the code
		System.out.println("The common node: ");
		for (int k = 0; k < XrefListCommonNode.size(); k++) {
			System.out.println(XrefListCommonNode.get(k));
		}

		cnPwPair.pathway1NameID = pw1NameId;
		cnPwPair.pathway2NameID = pw2NameId;
		cnPwPair.commonNodeNumber = commonNode;
		cnPwPair.geneIDListOfCommonNode = XrefListCommonNode;
		return cnPwPair;
	}

	public static List<commonNodePathwayPair> findCommonNodeForPathwaysGroup() {
		List<commonNodePathwayPair> commonNodeInfoPwGroup = new ArrayList<commonNodePathwayPair>();

		System.out.println("For Databases: " + dbLocation);

		Object[] arrayOfSelectedPwsNameId = selectedPwsNameId.toArray();
		int len = arrayOfSelectedPwsNameId.length;

		for (int i = 0; i < len; i++) {
			for (int j = i + 1; j < len; j++) {
				commonNodeInfoPwGroup.add(findCommonNode(
						(String) arrayOfSelectedPwsNameId[i],
						(String) arrayOfSelectedPwsNameId[j]));
			}
		}

		return commonNodeInfoPwGroup;
	}

	public static void drawCommonNodeView() {

		String[] colorPool = { "153,255,51", "255, 51, 255", "51,255,255",
				"255,255,255", "255,255,153", "102, 102, 255", "255,102,51",
				"255,255,0", "0,102,0", "0, 204, 204" };
		String[] shapePool = { "Diamond", "Hexagon", "Parallelogram",
				"Round Rectange", "Rectangle", "Ellipse", "Triangle", "Octagon" };

		List<commonNodePathwayPair> cnInfoPwGroup = findCommonNodeForPathwaysGroup();

		CyNetwork cyNetwork = Cytoscape.createNetwork("Common Node View", false);

		/*
		 * String[] groupPwsNameID=(String [])selectedPwsNameId.toArray();
		 * CyNode[] groupPwsIcons; for (int i=0; i<groupPwsNameID.length; i++){
		 * groupPwsIcons[i] = Cytoscape.getCyNode(groupPwsNameID[i], true);
		 * cyNetwork.addNode(groupPwsIcons[i]); }
		 */

		CyNode[] groupPwsIcons = new CyNode[selectedPwsNameId.size()];
		for (int i = 0; i < selectedPwsNameId.size(); i++) {
			groupPwsIcons[i] = Cytoscape.getCyNode(selectedPwsNameId.get(i),
					true);
			cyNetwork.addNode(groupPwsIcons[i]);
		}

		CyEdge[] groupEdges = new CyEdge[cnInfoPwGroup.size()];
		int[] commonNodeNumber = new int[cnInfoPwGroup.size()];
		int numberOfEdges = 0;
		for (int j = 0; j < cnInfoPwGroup.size(); j++) {
			commonNodePathwayPair temp = cnInfoPwGroup.get(j);
			if (temp.commonNodeNumber != 0) {
				CyNode n1 = Cytoscape.getCyNode(temp.pathway1NameID, false);
				CyNode n2 = Cytoscape.getCyNode(temp.pathway2NameID, false);
				groupEdges[numberOfEdges] = Cytoscape.getCyEdge(n1, n2,
						Semantics.INTERACTION, "pp", true);
				commonNodeNumber[numberOfEdges] = temp.commonNodeNumber;
				cyNetwork.addEdge(groupEdges[numberOfEdges]);
				numberOfEdges++;
			}
		}

		// Where nodeID was the String id of the node in question, shapeNames is
		// a string representing the desired shape: "Triangle", etc.
		CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		for (int i = 0; i < selectedPwsNameId.size(); i++) {
			nodeAtts.setAttribute(groupPwsIcons[i].getIdentifier(),
					"node.fillColor", colorPool[i]);
			nodeAtts.setAttribute(groupPwsIcons[i].getIdentifier(),
					"node.shape", shapePool[i]); // "Triangle");
		}

		Cytoscape.getCurrentNetworkView().redrawGraph(false, true);

		CyAttributes edgeAtts = Cytoscape.getEdgeAttributes();
		for (int j = 0; j < numberOfEdges; j++) {
			edgeAtts.setAttribute(groupEdges[j].getIdentifier(), "edge.label",
					String.valueOf(commonNodeNumber[j]));
		}
		// edgeAtts.setAttribute("yellow","node.shape","Diamond");

		// display the common node view
		Cytoscape.createNetworkView(cyNetwork, "Common Node View");

	}

	private static boolean checkString(String string) {
		return string != null && string.length() > 0;
	}

	public static boolean checkCommonNodeByIdMapping(List<String> XrefListPws,
			String onePathwayXref) {
		boolean result = false;

		return result;
	}

	public static class commonNodePathwayPair {
		public String pathway1NameID;

		public String pathway2NameID;

		public int commonNodeNumber;

		public List<String> geneIDListOfCommonNode = null;
	}
}
