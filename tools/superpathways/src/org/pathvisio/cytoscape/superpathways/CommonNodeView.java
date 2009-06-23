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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
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

	public CommonNodeView(List<String> pathwaysNameId, SuperpathwaysClient c) {
		selectedPwsNameId = pathwaysNameId;
		mClient = c;

	}

	public static commonNodePathwayPair findCommonNode(String pw1NameId, String pw2NameId) {
		commonNodePathwayPair cnPwPair = new commonNodePathwayPair();
		
		int commonNode=0;
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

		List<String> geneIDListPw1 = new ArrayList<String>();
		List<String> geneIDListPw2 = new ArrayList<String>();
		List<String> geneIDListCommonNode = new ArrayList<String>();

		// Get all genes, proteins and metabolites for a pathway
		
		//System.out.println("Xref info of one pathway: ");
		for (PathwayElement pw1Elm : pathway1.getDataObjects()) {
			// Only take elements with type DATANODE (genes, proteins,
			// metabolites)
			if (pw1Elm.getObjectType() == ObjectType.DATANODE) {

				// System.out.println(pwElm.getGeneID());
				//geneIDListPw1.add(pw1Elm.getGeneID());
				
				//System.out.println(pw1Elm.getXref().toString());
				geneIDListPw1.add(pw1Elm.getXref().toString());

			}

		}

		//System.out.println("The common node: ");
		System.out.println("Xref info of the other pathway: ");
		for (PathwayElement pw2Elm : pathway2.getDataObjects()) {
			if (pw2Elm.getObjectType() == ObjectType.DATANODE) {
				//geneIDListPw2.add(pw2Elm.getGeneID());
				geneIDListPw2.add(pw2Elm.getXref().toString());
				System.out.println(pw2Elm.getXref().toString());
				
				if (geneIDListPw1.contains(pw2Elm.getXref().toString())) {
					commonNode = commonNode + 1;
					geneIDListCommonNode.add(pw2Elm.getXref().toString());
					//System.out.println(pw2Elm.getXref().toString());
				}
			}
		}
		cnPwPair.pathway1NameID=pw1NameId;
		cnPwPair.pathway2NameID=pw2NameId;
		cnPwPair.commonNodeNumber=commonNode;
		cnPwPair.geneIDListOfCommonNode=geneIDListCommonNode;
		return cnPwPair;
	}
	
	public static List<commonNodePathwayPair> findCommonNodeForPathwaysGroup(){
		List<commonNodePathwayPair> commonNodeInfoPwGroup=new ArrayList<commonNodePathwayPair>();
		
		Object[] arrayOfSelectedPwsNameId=selectedPwsNameId.toArray();
		int len=arrayOfSelectedPwsNameId.length;
		
		for (int i=0; i<len; i++){
			for (int j=i+1; j<len; j++){
				commonNodeInfoPwGroup.add(findCommonNode((String)arrayOfSelectedPwsNameId[i], (String)arrayOfSelectedPwsNameId[j]));
			}
		}
		
		return commonNodeInfoPwGroup;
	}
	
	public static void drawCommonNodeView(){
		
		String[] colorPool={"153,255,51", "255, 51, 255", "51,255,255", "255,255,255", "255,255,153", "102, 102, 255", "255,102,51", "255,255,0", "0,102,0", "0, 204, 204"};
		String[] shapePool={"Diamond", "Hexagon", "Parallelogram", "Round Rectange", "Rectangle", "Ellipse", "Triangle", "Octagon"};
		
		List<commonNodePathwayPair> cnInfoPwGroup=findCommonNodeForPathwaysGroup();
		
		CyNetwork cyNetwork = Cytoscape.createNetwork("Common Node View", false);

		/*String[] groupPwsNameID=(String [])selectedPwsNameId.toArray();
		CyNode[] groupPwsIcons;
		for (int i=0; i<groupPwsNameID.length; i++){
			groupPwsIcons[i] = Cytoscape.getCyNode(groupPwsNameID[i], true);
			cyNetwork.addNode(groupPwsIcons[i]);
		}*/
		
		CyNode[] groupPwsIcons=new CyNode[selectedPwsNameId.size()];
		for (int i=0; i<selectedPwsNameId.size(); i++){
			groupPwsIcons[i] = Cytoscape.getCyNode(selectedPwsNameId.get(i), true);
			cyNetwork.addNode(groupPwsIcons[i]);
		}
		
		CyEdge[] groupEdges=new CyEdge[cnInfoPwGroup.size()];
		int[] commonNodeNumber=new int[cnInfoPwGroup.size()];
		int numberOfEdges=0;
		for(int j=0; j<cnInfoPwGroup.size(); j++){
			commonNodePathwayPair temp=cnInfoPwGroup.get(j);
			if(temp.commonNodeNumber!=0){
				CyNode n1=Cytoscape.getCyNode(temp.pathway1NameID, false);
				CyNode n2=Cytoscape.getCyNode(temp.pathway2NameID, false);
				groupEdges[numberOfEdges]=Cytoscape.getCyEdge(n1, n2, Semantics.INTERACTION, "pp", true);
				commonNodeNumber[numberOfEdges]=temp.commonNodeNumber;
				cyNetwork.addEdge(groupEdges[numberOfEdges]);
				numberOfEdges++;
			}
		}
		

		
		//Where nodeID was the String id of the node in question, shapeNames is
		//a string representing the desired shape: "Triangle", etc. 
		CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		for (int i=0; i<selectedPwsNameId.size(); i++){
			nodeAtts.setAttribute(groupPwsIcons[i].getIdentifier(),"node.fillColor",colorPool[i]);
			nodeAtts.setAttribute(groupPwsIcons[i].getIdentifier(),"node.shape",shapePool[i]);             //"Triangle");
		}
		
		Cytoscape.getCurrentNetworkView().redrawGraph(false, true); 
		
		
		CyAttributes edgeAtts = Cytoscape.getEdgeAttributes();
		for (int j=0; j<numberOfEdges; j++){
			edgeAtts.setAttribute(groupEdges[j].getIdentifier(),"edge.label",String.valueOf(commonNodeNumber[j]));
		}
		//edgeAtts.setAttribute("yellow","node.shape","Diamond");             
		
		//display the common node view
		Cytoscape.createNetworkView(cyNetwork);

		
	}
	
	public static class commonNodePathwayPair {
		public String pathway1NameID;
		public String pathway2NameID;
		public int commonNodeNumber;
		public List<String> geneIDListOfCommonNode = null;
	}
}
