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

import java.awt.Color;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
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
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.layout.LayoutProperties;
import cytoscape.layout.Tunable;


public class CommonNodeView {

	// List<String> selectedPwsNameId;
    List<String> mSelectedPwsID;
	
	List<String> mSelectedPwsNameID;

	SuperpathwaysClient mClient;

	Map<Xref, Xref> nodePairByTranslation;

	List<String> colorPool;
	
	protected boolean interrupted; // to enable cancel of the network merge

	static String dbLocation = GlobalPreference.getDataDir().toString()
			+ "/gene databases/";

	public CommonNodeView(List<String> pwId, List<String> pwNameID, SuperpathwaysClient c) {
		mSelectedPwsID = pwId;
		mClient = c;
		mSelectedPwsNameID=pwNameID;
		nodePairByTranslation = new HashMap<Xref, Xref>();
		interrupted = false;

	}
	
	public void interrupt() {
		interrupted = true;
	}

	public List<String> getColorPool() {
		return colorPool;
	}

	public Map<Xref, Xref> getNodePairByTranslation() {
		return nodePairByTranslation;
	}

	public commonNodePathwayPair findCommonNode(String pw1ID, String pw1NameID, String pw2ID, String pw2NameID) {
		commonNodePathwayPair cnPwPair = new commonNodePathwayPair();

		int commonNode = 0;

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
		System.out.println("Xref info of one pathway: " + pw1ID);
		for (PathwayElement pw1Elm : pathway1.getDataObjects()) {
			// Only take elements with type DATANODE (genes, proteins,
			// metabolites)

			if (pw1Elm.getObjectType() == ObjectType.DATANODE) {

				String id = pw1Elm.getGeneID();
				DataSource ds = pw1Elm.getDataSource();
				if (!checkString(id) || ds == null) {
					continue; // Skip empty id/codes
				}
				// System.out.println("before translation: ");
				// System.out.println(pw1Elm.getXref().toString());

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

		IDMapper gdb = null;
		// String[] pgdbFileName = dir.list();
		File[] pgdbFileName = dir.listFiles();

		if (pgdbFileName == null) {

			JOptionPane
					.showMessageDialog(
							mClient.getGUI(),
							"There's no database in the default folder, please choose the directory where you've loaded the databases!");

			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser
					.setDialogTitle("Choose the direcotry where you've loaded databases...");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			// disable the "All files" option.
			chooser.setAcceptAllFileFilterUsed(false);

			if (chooser.showOpenDialog(mClient.getPlugin().mWindow) == JFileChooser.APPROVE_OPTION) {
				System.out.println("getCurrentDirectory(): "
						+ chooser.getCurrentDirectory());
				System.out.println("getSelectedFile() : "
						+ chooser.getSelectedFile());
				dir = chooser.getSelectedFile();
				dbLocation = chooser.getSelectedFile().toString() + "/";
				pgdbFileName = dir.listFiles();
			}

		}
		for (int i = 0; i < pgdbFileName.length; i++) {
			
			if (interrupted)
				return null;

			// String fileName = pgdbFileName[i];
			File file = pgdbFileName[i];
			if (file.isDirectory())
				continue; // skip directories
			String fileName = file.getName();
			int index = fileName.indexOf("_");
			if (index < 0)
				continue; // Skip this file, not the pgdb naming
			// scheme
			String speciesOrMetabolite = fileName.substring(0, index);
			// System.out.println(speciesOrMetabolite);
			if (speciesOrMetabolite.equals((Object) orgCode)) {
				System.out.println(dbLocation + fileName);
				// File fGdb = new File(dbLocation + fileName);
				// System.out.println(speciesOrMetabolite);
				try {
					Class.forName ("org.bridgedb.rdb.IDMapperRdb");
					gdb = BridgeDb.connect ("idmapper-pgdb:" + file);
				} catch (IDMapperException e) {
					Logger.log.error("Problem while connecting to the Gdb", e);
				}
				catch (ClassNotFoundException e) {
					Logger.log.error("Problem while connecting to the Gdb", e);
				}
				break;
			}

		}

		// create the list of Xref for the pathway2: XrefListPw2
		System.out.println("Xref info of the other pathway: " + pw2ID);
		for (PathwayElement pw2Elm : pathway2.getDataObjects()) {
			if (pw2Elm.getObjectType() == ObjectType.DATANODE) {

				if (interrupted)
					return null;
				
				boolean isMapped = false;

				String id = pw2Elm.getGeneID();
				DataSource ds = pw2Elm.getDataSource();
				if (!checkString(id) || ds == null)
					continue; // Skip empty
				// id/codes

				// System.out.println(pw2Elm.getXref().toString());
				XrefListPw2.add(pw2Elm.getXref());

				for (int k = 0; k < XrefListPw1.size(); k++) {
					try {
					    Set<Xref> xrefs2 = gdb.mapID(pw2Elm.getXref());
						if (xrefs2.contains(XrefListPw1.get(k))) {
							isMapped = true;

							System.out.println(pw2Elm.getXref().toString()
									+ "======" + XrefListPw1.get(k).toString());
							// this map is for later use--when merging pathways
							nodePairByTranslation.put(pw2Elm.getXref(),
									XrefListPw1.get(k));
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

		// the following code is for printing out the common nodes returned by
		// the code
		System.out.println("The common node: ");
		for (int k = 0; k < XrefListCommonNode.size(); k++) {
			System.out.println(XrefListCommonNode.get(k));
		}

		cnPwPair.pathway1NameID = pw1NameID;
		cnPwPair.pathway2NameID = pw2NameID;
		cnPwPair.commonNodeNumber = commonNode;
		cnPwPair.geneIDListOfCommonNode = XrefListCommonNode;
		return cnPwPair;
	}

	public List<commonNodePathwayPair> findCommonNodeForPathwaysGroup() {
		List<commonNodePathwayPair> commonNodeInfoPwGroup = new ArrayList<commonNodePathwayPair>();

		System.out.println("For Databases: " + dbLocation);

		Object[] arrayOfSelectedPwsId = mSelectedPwsID.toArray();
		Object[] arrayOfSelectedPwsNameId = mSelectedPwsNameID.toArray();
		
		int len = arrayOfSelectedPwsId.length;

		for (int i = 0; i < len; i++) {
			for (int j = i + 1; j < len; j++) {
				
				if (interrupted)
					return null;
				
				commonNodeInfoPwGroup.add(findCommonNode(
						(String) arrayOfSelectedPwsId[i], (String) arrayOfSelectedPwsNameId[i],
						(String) arrayOfSelectedPwsId[j], (String) arrayOfSelectedPwsNameId[j]));
			}
		}

		return commonNodeInfoPwGroup;
	}

	public Map<String, Color> drawCommonNodeView() {

		if (interrupted)
			return null;
		
		Map<String, Color> pwNameToColor=new HashMap<String, Color>();
		
		colorPool = new ArrayList<String>();

		String[] shapePool = { "Diamond", "Hexagon", "Parallelogram",
				"Round Rectange", "Rectangle", "Ellipse", "Triangle", "Octagon" };

		List<commonNodePathwayPair> cnInfoPwGroup = findCommonNodeForPathwaysGroup();

		CyNetwork cyNetwork = Cytoscape
				.createNetwork("Common Node View", false);
		

		CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		CyAttributes edgeAtts = Cytoscape.getEdgeAttributes();
		
		if (interrupted)
			return null;
		CyNode[] groupPwsIcons = new CyNode[mSelectedPwsID.size()];
		for (int i = 0; i < mSelectedPwsID.size(); i++) {
			groupPwsIcons[i] = Cytoscape.getCyNode(mSelectedPwsNameID.get(i), true);
			cyNetwork.addNode(groupPwsIcons[i]);
			nodeAtts.setAttribute(mSelectedPwsNameID.get(i), "node.fontSize", "10");
			
		}

		if (interrupted)
			return null;
		CyEdge[] groupEdges = new CyEdge[cnInfoPwGroup.size()];
		int[] commonNodeNumber = new int[cnInfoPwGroup.size()];
		int numberOfEdges = 0;
		for (int j = 0; j < cnInfoPwGroup.size(); j++) {
			commonNodePathwayPair temp = cnInfoPwGroup.get(j);
			if (temp.commonNodeNumber != 0) {
				CyNode n1 = Cytoscape.getCyNode(temp.pathway1NameID, false);
				CyNode n2 = Cytoscape.getCyNode(temp.pathway2NameID, false);
				groupEdges[numberOfEdges] = Cytoscape.getCyEdge(n1, n2,	Semantics.INTERACTION, "pp", true);
				commonNodeNumber[numberOfEdges] = temp.commonNodeNumber;
				cyNetwork.addEdge(groupEdges[numberOfEdges]);
				edgeAtts.setAttribute(groupEdges[numberOfEdges].getIdentifier(), "weight", commonNodeNumber[numberOfEdges]);
				numberOfEdges++;
			}
		}

		int numberOfSelectedPws = mSelectedPwsID.size();
		double division1 = 360 / numberOfSelectedPws;
		double division2 = 50 / numberOfSelectedPws;
		
		for (int i = 0; i < mSelectedPwsID.size(); i++) {

			// String temp=getRandomColorInString();
			
			//use hsv and convert it to rgb
			double h2=i*division1;
			double s2=i*division2;
			double v2=i*division2;
			
			int h=Double.valueOf(h2).intValue();
			int s=Double.valueOf(s2).intValue();
			int v=Double.valueOf(v2).intValue();
			System.out.println("value of h "+ h);
			RGB tempRGB=hsvToRgb(h, 100, 100);
			String temp=String.valueOf(Double.valueOf(tempRGB.r).intValue())+", "+String.valueOf(Double.valueOf(tempRGB.g).intValue())+", "+String.valueOf(Double.valueOf(tempRGB.b).intValue());
			System.out.println("after conversion hsv to rgb"+ temp);
			
			nodeAtts.setAttribute(groupPwsIcons[i].getIdentifier(),
					"node.fillColor", temp);
			
			//this part is for storing the info <pwName, corresponding color> into the pwNameToColor
			String pwNameID=mSelectedPwsNameID.get(i);
			int index2 = pwNameID.lastIndexOf("(");
			String pwName = pwNameID.substring(0, index2);
			System.out.println(Double.valueOf(tempRGB.r).intValue()+";"+Double.valueOf(tempRGB.g).intValue()+";"+Double.valueOf(tempRGB.b).intValue());
			Color result=new Color(Double.valueOf(tempRGB.r).intValue(), Double.valueOf(tempRGB.g).intValue(),Double.valueOf(tempRGB.b).intValue());
			pwNameToColor.put(pwName,result);
			
			colorPool.add(temp);
			nodeAtts.setAttribute(groupPwsIcons[i].getIdentifier(),
					"node.shape", shapePool[i % shapePool.length]);
			// re-use shapes from the shapePool when the number of
			// selected Pws is larger than 8
		}
		
		for (int j = 0; j < numberOfEdges; j++) {
			edgeAtts.setAttribute(groupEdges[j].getIdentifier(), "edge.label",
					String.valueOf(commonNodeNumber[j]));
		}
		

		// display the common node view
		Cytoscape.createNetworkView(cyNetwork, "Common Node View");
		
		
		
		//the following code is for set the edge-weighted spring embedded layout (weight=the number of shared nodes)
		CyLayoutAlgorithm alg = CyLayouts.getLayout("force-directed"); 
		LayoutProperties props = alg.getSettings(); 
		Tunable weightAttribute = props.get("edge_attribute");
	    weightAttribute.setValue("weight");
	    alg.updateSettings(); 
	    Cytoscape.getCurrentNetworkView().applyLayout(alg);


		return pwNameToColor;
	}

	private static boolean checkString(String string) {
		return string != null && string.length() > 0;
	}

	/*
	 * public static Color getRandomColor() { Random numGen = new Random();
	 * return new Color(numGen.nextInt(256), numGen.nextInt(256),
	 * numGen.nextInt(256)); }
	 */

	public static String getRandomColorInString() {
		Random numGen = new Random();
		return new String(numGen.nextInt(256) + ", " + numGen.nextInt(256)
				+ ", " + numGen.nextInt(256));
	}

	public static RGB hsvToRgb(int h, int s, int v) {
		RGB result = new RGB();
		int i;
		double f, p, q, t;

		// Make sure our arguments stay in-range
		h = Math.max(0, Math.min(360, h));
		s = Math.max(0, Math.min(100, s));
		v = Math.max(0, Math.min(100, v));

		// We accept saturation and value arguments from 0 to 100 because that's
		// how Photoshop represents those values. Internally, however, the
		// saturation and value are calculated from a range of 0 to 1. We make
		// That conversion here.
		double s2 = (double)s / 100;
		double v2 = (double)v / 100;

		if (s2 == 0) {
			// Achromatic (grey)
			result.r = Math.round(v2 * 255);
			result.g = Math.round(v2 * 255);
			result.b = Math.round(v2 * 255);
			return result;
		}

		double h2 = (double)h / 60; // sector 0 to 5
		double i2 = Math.floor(h2);
		i = Double.valueOf(i2).intValue();
		f = h2 - i; // factorial part of h
		p = v2 * (1 - s2);
		q = v2 * (1 - s2 * f);
		t = v2 * (1 - s2 * (1 - f));

		switch (i) {

		case 0:
			result.r = v2;
			result.g = t;
			result.b = p;
			break;
		case 1:
			result.r = q;
			result.g = v2;
			result.b = p;
			break;
		case 2:
			result.r = p;
			result.g = v2;
			result.b = t;
			break;
		case 3:
			result.r = p;
			result.g = q;
			result.b = v2;
			break;
		case 4:
			result.r = t;
			result.g = p;
			result.b = v2;
			break;
		default: // case 5:
			result.r = v2;
			result.g = p;
			result.b = q;
		}

		result.r = Math.round(result.r * 255);
		result.g = Math.round(result.g * 255);
		result.b = Math.round(result.b * 255);

		return result;

	}

	public static class commonNodePathwayPair {
		public String pathway1NameID;

		public String pathway2NameID;

		public int commonNodeNumber;

		public List<String> geneIDListOfCommonNode = null;
	}

	public static class RGB {
		public double r;

		public double g;

		public double b;
	}
}
