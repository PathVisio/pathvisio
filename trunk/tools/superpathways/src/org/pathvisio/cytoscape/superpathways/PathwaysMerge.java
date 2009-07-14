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

import giny.model.Edge;
import giny.model.GraphObject;
import giny.model.Node;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.pathvisio.preferences.GlobalPreference;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.task.TaskMonitor;

public class PathwaysMerge{
	 protected TaskMonitor taskMonitor;

	 protected boolean interrupted; // to enable cancel of the network merge

	List<CyNetwork> networks;
	
	Map<Xref, Xref> nodePairByTranslation;
	
	CustomNodeGenerator customNode;
	
	String[] colorPool;
	
	static String imageLocation = GlobalPreference.getApplicationDir() .toString()+ "/" ;
	
	/*String[] colorPool = { "0,255,0", "80,50,80", "51,255,255",
			"255,255,0", "0, 0, 255", "255, 0, 255", "255,0, 0",
			 "0,102,0", "0, 204, 204" };*/
	
	static Map<String, DataSource> mapSysCodeToBioDS;
	
	public PathwaysMerge( List<CyNetwork> nets, Map<Xref, Xref> m, String[] c) {
//		status = "Merging networks... 0%";
        taskMonitor = null;
        interrupted = false;
		networks = nets;
		nodePairByTranslation=m;
		colorPool=c;
		
		/*colors=new Color[colorPool.length];
		
		for(int i=0; i<colorPool.length; i++){
			String temp=colorPool[i];
			System.out.println(temp);
			colors[i]=stringToColor(temp);
		}*/
		
		
		mapSysCodeToBioDS=new HashMap<String, DataSource>();
		mapSysCodeToBioDS.put("Entrez Gene", BioDataSource.ENTREZ_GENE);
		mapSysCodeToBioDS.put("Ensembl Rat", BioDataSource.ENSEMBL_RAT);
		mapSysCodeToBioDS.put("TAIR", BioDataSource.TAIR);
		mapSysCodeToBioDS.put("Agilent", BioDataSource.AGILENT);
		mapSysCodeToBioDS.put("BioGrid", BioDataSource.BIOGRID);
		mapSysCodeToBioDS.put("Cint", BioDataSource.CINT);
		mapSysCodeToBioDS.put("CCDS", BioDataSource.CCDS);
		mapSysCodeToBioDS.put("CAS", BioDataSource.CAS);
		mapSysCodeToBioDS.put("ChEBI", BioDataSource.CHEBI );
		mapSysCodeToBioDS.put("HMDB", BioDataSource.HMDB );
		mapSysCodeToBioDS.put("Kegg Compound", BioDataSource.KEGG_COMPOUND);
		mapSysCodeToBioDS.put("PubChem", BioDataSource.PUBCHEM);
		mapSysCodeToBioDS.put("Chemspider", BioDataSource.CHEMSPIDER );
		mapSysCodeToBioDS.put("SGD", BioDataSource.SGD);
		mapSysCodeToBioDS.put("EC Number", BioDataSource.ENZYME_CODE);
		mapSysCodeToBioDS.put("Ecoli", BioDataSource.ECOLI );
		mapSysCodeToBioDS.put("EMBL", BioDataSource.EMBL);
		mapSysCodeToBioDS.put("Ensembl", BioDataSource.ENSEMBL);
		mapSysCodeToBioDS.put("Ensembl Mosquito", BioDataSource.ENSEMBL_MOSQUITO);
		mapSysCodeToBioDS.put("Gramene Arabidopsis", BioDataSource.GRAMENE_ARABIDOPSIS);
		mapSysCodeToBioDS.put("Ensembl B. subtilis", BioDataSource.ENSEMBL_BSUBTILIS);
		mapSysCodeToBioDS.put("Ensembl Cow", BioDataSource.ENSEMBL_COW);
		mapSysCodeToBioDS.put("Ensembl C. elegans", BioDataSource.ENSEMBL_CELEGANS);
		mapSysCodeToBioDS.put("Ensembl Dog", BioDataSource.ENSEMBL_DOG);
		mapSysCodeToBioDS.put("Ensembl Fruitfly", BioDataSource.ENSEMBL_FRUITFLY);
		mapSysCodeToBioDS.put("Ensembl Zebrafish", BioDataSource.ENSEMBL_ZEBRAFISH);
		mapSysCodeToBioDS.put("Ensembl E. coli", BioDataSource.ENSEMBL_ECOLI);
		mapSysCodeToBioDS.put("Ensembl Chicken", BioDataSource.ENSEMBL_CHICKEN);
		mapSysCodeToBioDS.put("Ensembl Human", BioDataSource.ENSEMBL_HUMAN);
		mapSysCodeToBioDS.put("Ensembl Mouse", BioDataSource.ENSEMBL_MOUSE);
		mapSysCodeToBioDS.put("Gramene Rice", BioDataSource.GRAMENE_RICE);
		mapSysCodeToBioDS.put("Ensembl Chimp", BioDataSource.ENSEMBL_CHIMP);
		mapSysCodeToBioDS.put("Ensembl Horse", BioDataSource.ENSEMBL_HORSE);
		mapSysCodeToBioDS.put("Ensembl Yeast", BioDataSource.ENSEMBL_SCEREVISIAE);
		mapSysCodeToBioDS.put("Ensembl Xenopu", BioDataSource.ENSEMBL_XENOPUS);
		mapSysCodeToBioDS.put("FlyBase", BioDataSource.FLYBASE);
		mapSysCodeToBioDS.put("GenBank", BioDataSource.GENBANK );
		mapSysCodeToBioDS.put("CodeLink", BioDataSource.CODELINK );
		mapSysCodeToBioDS.put("Gramene Genes DB", BioDataSource.GRAMENE_GENES_DB);
		mapSysCodeToBioDS.put("Gramene Literature", BioDataSource.GRAMENE_LITERATURE);
		mapSysCodeToBioDS.put("Gramene Pathway", BioDataSource.GRAMENE_PATHWAY);
		mapSysCodeToBioDS.put("GenPept", BioDataSource.GEN_PEPT);
		mapSysCodeToBioDS.put("HUGO", BioDataSource.HUGO);
		mapSysCodeToBioDS.put("HsGene", BioDataSource.HSGENE );
		mapSysCodeToBioDS.put("InterPro", BioDataSource.INTERPRO);
		mapSysCodeToBioDS.put("Illumina", BioDataSource.ILLUMINA);
		mapSysCodeToBioDS.put("IPI", BioDataSource.IPI);
		mapSysCodeToBioDS.put("IRGSP Gene", BioDataSource.IRGSP_GENE);
		mapSysCodeToBioDS.put("MGI", BioDataSource.MGI);
		mapSysCodeToBioDS.put("miRBase", BioDataSource.MIRBASE);
		mapSysCodeToBioDS.put("MaizeGDB", BioDataSource.MAIZE_GDB);
		mapSysCodeToBioDS.put("NASC Gene", BioDataSource.NASC_GENE);
		mapSysCodeToBioDS.put("NuGO wiki", BioDataSource.NUGOWIKI);
		mapSysCodeToBioDS.put("Other", BioDataSource.OTHER );
		mapSysCodeToBioDS.put("Oryzabase", BioDataSource.ORYZA_BASE);
		mapSysCodeToBioDS.put("OMIM", BioDataSource.OMIM );
		mapSysCodeToBioDS.put("Rice Ensembl Gene", BioDataSource.RICE_ENSEMBL_GENE);
		mapSysCodeToBioDS.put("PDB", BioDataSource.PDB);
		mapSysCodeToBioDS.put("Pfam", BioDataSource.PFAM );
		mapSysCodeToBioDS.put("PlantGDB", BioDataSource.PLANTGDB );
		mapSysCodeToBioDS.put("RefSeq", BioDataSource.REFSEQ );
		mapSysCodeToBioDS.put("RGD", BioDataSource.RGD );
		mapSysCodeToBioDS.put("Rfam", BioDataSource.RFAM );
		mapSysCodeToBioDS.put("Uniprot/TrEMBL", BioDataSource.UNIPROT);
		mapSysCodeToBioDS.put("dbSNP", BioDataSource.SNP );
		mapSysCodeToBioDS.put("GeneOntology", BioDataSource.GENE_ONTOLOGY );
		mapSysCodeToBioDS.put("UniGene", BioDataSource.UNIGENE );
		mapSysCodeToBioDS.put("UCSC Genome Browser", BioDataSource.UCSC);
		mapSysCodeToBioDS.put("WormBase", BioDataSource.WORMBASE );
		mapSysCodeToBioDS.put("Wikipedia", BioDataSource.WIKIPEDIA );
		mapSysCodeToBioDS.put("Wheat gene catalog", BioDataSource.WHEAT_GENE_CATALOG);
		mapSysCodeToBioDS.put("Wheat gene names", BioDataSource.WHEAT_GENE_NAMES);
		mapSysCodeToBioDS.put("Wheat gene refs", BioDataSource.WHEAT_GENE_REFERENCES);
		mapSysCodeToBioDS.put("Affy", BioDataSource.AFFY );
		mapSysCodeToBioDS.put("ZFIN", BioDataSource.ZFIN);

	}

	public void interrupt() {
		interrupted = true;
	}

	public void setTaskMonitor(final TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
	}

	private void updateTaskMonitor(String status, int percentage) {
		if (this.taskMonitor != null) {
			taskMonitor.setStatus(status);
			taskMonitor.setPercentCompleted(percentage);
		}
	}
	
	public CyNetwork mergeNetwork(final String title, Map<Xref, Xref> nodePairByTranslation) {
		if (networks == null || title == null) {
			throw new java.lang.NullPointerException();
		}

		if (networks.isEmpty()) {
			throw new java.lang.IllegalArgumentException("No merging network");
		}

		if (title.length() == 0) {
			throw new java.lang.IllegalArgumentException("Empty title");
		}
		// get node matching list
		List<Map<CyNetwork, Set<GraphObject>>> matchedNodeList = getMatchedList(
				networks, true, nodePairByTranslation);

		final Map<Node, Node> mapNN = new HashMap<Node, Node>();
		// save information on mapping from original nodes to merged nodes
		// to use when merge edges

		// merge nodes in the list
		final int nNode = matchedNodeList.size();
		List<Node> nodes = new Vector<Node>(nNode);
		for (int i = 0; i < nNode; i++) {

			if (interrupted) return null;
			/*updateTaskMonitor("Merging nodes...\n" + i + "/" + nNode, (i + 1)
					* 100 / nNode);*/

			final Map<CyNetwork, Set<GraphObject>> mapNetNode = matchedNodeList
					.get(i);
			final Node node = mergeNode(mapNetNode);
			if (node != null) {
				// System.out.println("gpml-type for node "+
				// node.getIdentifier()+ " is "
				// + Cytoscape.getNodeAttributes().getAttribute(
				// node.getIdentifier(), "gpml-type"));
				nodes.add(node);
			}

			final Iterator<Set<GraphObject>> itNodes = mapNetNode.values().iterator();
			while (itNodes.hasNext()) {
				final Set<GraphObject> nodes_ori = itNodes.next();
				final Iterator<GraphObject> itNode = nodes_ori.iterator();
				while (itNode.hasNext()) {
					final Node node_ori = (Node) itNode.next();
					mapNN.put(node_ori, node);
				}
			}
		}

//		updateTaskMonitor("Merging nodes completed", 100);

		// match edges
		List<Map<CyNetwork, Set<GraphObject>>> matchedEdgeList = getMatchedList(networks, false, nodePairByTranslation);
		// merge edges
		final int nEdge = matchedEdgeList.size();
		final List<Edge> edges = new Vector<Edge>(nEdge);
		for (int i = 0; i < nEdge; i++) {

			if (interrupted)
				return null;
			/*updateTaskMonitor("Merging edges...\n" + i + "/" + nEdge, (i + 1)
					* 100 / nEdge);*/

			final Map<CyNetwork, Set<GraphObject>> mapNetEdge = matchedEdgeList
					.get(i);
			// get the source and target nodes in merged network
			final Iterator<Set<GraphObject>> itEdges = mapNetEdge.values()
					.iterator();

			final Set<GraphObject> edgeSet = itEdges.next();
			if (edgeSet == null || edgeSet.isEmpty()) {
				throw new java.lang.IllegalStateException(
						"Null or empty edge set");
			}

			final Edge edge_ori = (Edge) edgeSet.iterator().next();
			final Node source = mapNN.get(edge_ori.getSource());
			final Node target = mapNN.get(edge_ori.getTarget());
			if (source == null || target == null) { // some of the node may
				// be exluded when
				// intersection or
				// difference
				continue;
			}

			final boolean directed = edge_ori.isDirected();
			final CyAttributes attributes = Cytoscape.getEdgeAttributes();
			final String interaction = (String) attributes.getAttribute(
					edge_ori.getIdentifier(), Semantics.INTERACTION);

			final Edge edge = mergeEdge(mapNetEdge, source, target,
					interaction, directed);
			edges.add(edge);
		}

		//updateTaskMonitor("Merging edges completed", 100);

		// create new network
		final CyNetwork network = Cytoscape.createNetwork(nodes, edges, title, null, false);
		
		//here the code is for replace the shared nodes with custom node(multi-color pie)
		Iterator<Node> itN=nodes.iterator();
		CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		while(itN.hasNext()){
			
		    Node temp=itN.next();
		    String colorTemp=(String)nodeAtts.getAttribute(temp.getIdentifier(), "node.fillColor");
		   
		    List<Color> colorsOnePie=new ArrayList<Color>();
		    if(colorTemp!=null){
		    	if(colorTemp.indexOf(";")!=-1){
		    		//get individual string representation of Color, and then transfer it to Color type
		    		int t=0;
		    		int index=colorTemp.indexOf(";", t);
		    		while(index!=-1){
		    			
		    			String s=colorTemp.substring(t, index);
		    			//System.out.println("individual color's string representation: "+s);
		    			if(s.equals(";")) continue;
		    			t=index+1;
		    			colorsOnePie.add(stringToColor(s));
		    			index=colorTemp.indexOf(";", t);
		    		}
		    		
		    		//System.out.println(colorTemp);
		    		Object[] cOnePie=colorsOnePie.toArray();
		    		Color[] c=new Color[colorsOnePie.size()];
		    		for(int k=0; k<colorsOnePie.size();k++){
		    			c[k]=(Color)cOnePie[k];
		    		}
		    		
		    		
		    		PieGenerator pie=new PieGenerator(c);
		    		pie.generatePie(colorsOnePie.size());
		    		customNode=new CustomNodeGenerator(imageLocation+"chart.png", temp);
		    		customNode.createCustomNode(network);
		    	}
		    }
		
		}

		updateTaskMonitor("Successfully merged the selected " + networks.size()
				+ " networks into network " + title, 100);

		cytoscape.view.CyNetworkView networkView = Cytoscape.getNetworkView(title);
		networkView.redrawGraph(true,true);

		return network;
	}

	/**
	 * Get a list of matched nodes/edges
	 * 
	 * @param networks
	 *            Networks to be merged
	 * 
	 * @return list of map from network to node/edge
	 */
	protected List<Map<CyNetwork, Set<GraphObject>>> getMatchedList(
			final List<CyNetwork> networks, final boolean isNode, Map<Xref, Xref> nodePairByTranslation) {
		if (networks == null) {
			throw new java.lang.NullPointerException();
		}

		if (networks.isEmpty()) {
			throw new java.lang.IllegalArgumentException("No merging network");
		}

		final List<Map<CyNetwork, Set<GraphObject>>> matchedList = new Vector<Map<CyNetwork, Set<GraphObject>>>();

		final int nNet = networks.size();

		// Get the total number nodes/edge to calculate the status
		int totalGO = 0, processedGO = 0;
		for (int i = 0; i < nNet; i++) {
			final CyNetwork net = networks.get(i);
			totalGO += isNode ? net.getNodeCount() : net.getEdgeCount();
		}

		for (int i = 0; i < nNet; i++) {

			final CyNetwork net1 = networks.get(i);
			final Iterator<GraphObject> it;
			if (isNode) {
				it = net1.nodesIterator();
			} else { // edge
				it = net1.edgesIterator();
			}

			while (it.hasNext()) {
				if (interrupted)
					return null;
				/*updateTaskMonitor("Matching " + (isNode ? "nodes" : "edges")
						+ "...\n" + processedGO + "/" + totalGO, processedGO
						* 100 / totalGO);
				processedGO++;*/

				final GraphObject go1 = it.next();

				// chech whether any nodes in the matchedNodeList match with
				// this node
				// if yes, add to the list, else add a new map to the list
				boolean matched = false;
				final int n = matchedList.size();
				int j = 0;
				for (; j < n; j++) {
					final Map<CyNetwork, Set<GraphObject>> matchedGO = matchedList
							.get(j);
					final Iterator<CyNetwork> itNet = matchedGO.keySet()
							.iterator();
					while (itNet.hasNext()) {
						final CyNetwork net2 = itNet.next();
						// if (net1==net2) continue; //in fact the same
						// network may have nodes match to each other

						final Set<GraphObject> gos2 = matchedGO.get(net2);
						if (gos2 != null) {
							GraphObject go2 = gos2.iterator().next(); // since
							// there is only one node in the map
							if (isNode) { // NODE
								matched = matchNode(net1, (Node) go1, net2,
										(Node) go2,nodePairByTranslation);
							} else {// EDGE
								matched = matchEdge(net1, (Edge) go1, net2,
										(Edge) go2, nodePairByTranslation);
							}
							if (matched) {
								Set<GraphObject> gos1 = matchedGO.get(net1);
								if (gos1 == null) {
									gos1 = new HashSet<GraphObject>();
									matchedGO.put(net1, gos1);
								}
								gos1.add(go1);
								break;
							}
						}
					}
					if (matched) {
						break;
					}
				}
				if (!matched) { // no matched node found, add new map to the
					// list
					final Map<CyNetwork, Set<GraphObject>> matchedGO = new HashMap<CyNetwork, Set<GraphObject>>();
					Set<GraphObject> gos1 = new HashSet<GraphObject>();
					gos1.add(go1);
					matchedGO.put(net1, gos1);
					matchedList.add(matchedGO);
				}

			}

		}
		/*updateTaskMonitor("Matching " + (isNode ? "nodes" : "edges")
				+ " completed", 100);*/
		return matchedList;
	}

	public boolean matchEdge(final CyNetwork net1, Edge e1,
			final CyNetwork net2, Edge e2, Map<Xref, Xref> nodePairByTranslation) {
		if (net1 == null || e1 == null || e2 == null) {
			throw new java.lang.NullPointerException();
		}

		// TODO should interaction be considered or not?
		final CyAttributes attributes = Cytoscape.getEdgeAttributes();

		Object i1 = attributes.getAttribute(e1.getIdentifier(),
				Semantics.INTERACTION);
		Object i2 = attributes.getAttribute(e2.getIdentifier(),
				Semantics.INTERACTION);

		if ((i1 == null && i2 != null) || (i1 != null && i2 == null)) {
			return false;
		}

		if (i1 != null && !i1.equals(i2))
			return false;

		if (e1.isDirected()) { // directed
			if (!e2.isDirected())
				return false;
			return matchNode(net1, e1.getSource(), net2, e2.getSource(), nodePairByTranslation)
					&& matchNode(net1, e1.getTarget(), net2, e2.getTarget(),nodePairByTranslation);
		} else { // non directed
			if (e2.isDirected())
				return false;
			if (matchNode(net1, e1.getSource(), net2, e2.getSource(),nodePairByTranslation)
					&& matchNode(net1, e1.getTarget(), net2, e2.getTarget(), nodePairByTranslation))
				return true;
			if (matchNode(net1, e1.getSource(), net2, e2.getTarget(), nodePairByTranslation)
					&& matchNode(net1, e1.getTarget(), net2, e2.getSource(), nodePairByTranslation))
				return true;
			return false;
		}
	}

	public boolean matchNode(CyNetwork net1, Node n1, CyNetwork net2, Node n2, Map<Xref, Xref> nodePairByTranslation) {
		// boolean result=false;
		if (net1 == null || n1 == null || n2 == null || net2 == null) {
			throw new java.lang.NullPointerException();
		}
		CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		Object geneID1 = nodeAtts.getAttribute(n1.getIdentifier(), "GeneID");
		Object geneID2 = nodeAtts.getAttribute(n2.getIdentifier(), "GeneID");

		Object systemCode1 = nodeAtts.getAttribute(n1.getIdentifier(),"SystemCode");
		Object systemCode2 = nodeAtts.getAttribute(n2.getIdentifier(),"SystemCode");

		if (geneID1 == null || geneID2 == null || systemCode1 == null
				|| systemCode2 == null || geneID1.equals("")
				|| systemCode1.equals("")) {
			return false;
		} else {
			if (geneID1.equals(geneID2) && systemCode1.equals(systemCode2)) {
				return true;
			} else {

				// use Map<Xref, Xref> nodePairByTranslation to help determine
				// whether two nodes match
				Xref x1 = new Xref((String) geneID1, mapSysCodeToBioDS
						.get((String) systemCode1));
				Xref x2 = new Xref((String) geneID2, mapSysCodeToBioDS
						.get((String) systemCode2));
				// System.out.println("Xref1 in merge: "+x1.toString());
				// System.out.println("Xref2 in merge: "+x2.toString());

				if (nodePairByTranslation.containsKey(x1)) {
					if (nodePairByTranslation.get(x1).equals(x2)) {
						// System.out.println("Matched!");
						return true;
					}
				} else if (nodePairByTranslation.containsKey(x2)) {
					if (nodePairByTranslation.get(x2).equals(x1)) {
						// System.out.println("Matched!");
						return true;
					}
				} else {
					return false;
				}
				return false;
			}
			
		}

	}

	public Node mergeNode(final Map<CyNetwork, Set<GraphObject>> mapNetNode) {

		if (mapNetNode == null || mapNetNode.isEmpty()) {
			return null;
		}

		final Iterator<Set<GraphObject>> itNodes = mapNetNode.values().iterator();

		Set<GraphObject> nodes = new HashSet<GraphObject>();
		// 'nodes' will contains all the matched nodes
		while (itNodes.hasNext()) {
			nodes.addAll(itNodes.next());
		}

		final Iterator<GraphObject> itNode = nodes.iterator();
		String id = new String(itNode.next().getIdentifier());

		CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		String gpmlType = nodeAtts.getAttribute(id, "gpml-type").toString();

		if ((!gpmlType.equals("1")) && (!gpmlType.equals("7"))) {
			return null;
		} else {

			if (nodes.size() > 1) { // if more than 1 nodes to be merged, assign
				// the id as the combination of all identifiers
				
				//System.out.println("there are several nodes matched: ");
				//System.out.print(id);
				
				while (itNode.hasNext()) {
					final Node node = (Node) itNode.next();
					//System.out.print("+"+node.getIdentifier());
					id += "_" + node.getIdentifier();
				}

				//System.out.println();
				
				// if node with this id exist, get new one
				String appendix = "";
				int app = 0;
				while (Cytoscape.getCyNode(id + appendix) != null) {
					appendix = "" + ++app;
				}
				id += appendix;
			}

			// Get the node with id or create a new node
			// for attribute confilict handling, introduce a conflict node here?
			final Node node = Cytoscape.getCyNode(id, true);

			Set<CyNetwork> setOfNets = mapNetNode.keySet();
			Iterator<CyNetwork> itNets = setOfNets.iterator();
			String color = null;

			if (nodes.size() == 1) {
				// choose one color for the node according to the network's
				// position in the list of nets

				Set<GraphObject> temp = new HashSet<GraphObject>();
				temp.add(node);
				while (itNets.hasNext()) {
					CyNetwork n = itNets.next();
					Set<GraphObject> o = mapNetNode.get(n);
					if (temp.equals(o)) {
						if (networks.contains(n)) {
							int index = networks.indexOf(n);
							// System.out.println(index + "");
							
							color = colorPool[index];
							
							nodeAtts.setAttribute(id, "Source Pathway", networks.get(index).getTitle());
							break;
						}
					}
				}
				
				nodeAtts.setAttribute(id, "node.fillColor", color);
			} else {
				if (setOfNets.size() == 1) {
					CyNetwork n = itNets.next();
					if (networks.contains(n)) {
						int index = networks.indexOf(n);
						// System.out.println(index + "");
						color = colorPool[index];
						nodeAtts.setAttribute(id, "node.fillColor", color);
						nodeAtts.setAttribute(id, "Source Pathway", networks.get(index).getTitle());
					}

				} else {
					//System.out.println("for one pie----------------------");
					//Set<String> colorPie = new HashSet<String>();
					String combinedColor="";
					String sourcePws="";
					while (itNets.hasNext()) {
						CyNetwork n = itNets.next();

						if (networks.contains(n)) {
							int index = networks.indexOf(n);
							sourcePws=sourcePws+networks.get(index).getTitle()+", ";
							//System.out.println(colorPool[index]);
							//colorPie.add(colorPool[index]);
							combinedColor=combinedColor+ colorPool[index]+ ";";

						}
					}
					
					//System.out.println(combinedColor);
					nodeAtts.setAttribute(id, "Source Pathway", sourcePws);
					nodeAtts.setAttribute(id, "node.shape", "Rectangle");
					nodeAtts.setAttribute(id, "node.fillColor", combinedColor);
				}
			}

			return node;
		}
	}

	public Edge mergeEdge(final Map<CyNetwork, Set<GraphObject>> mapNetEdge,
			final Node source, final Node target, final String interaction,
			final boolean directed) {
		// TODO: refactor in Cytoscape3
		if (mapNetEdge == null || mapNetEdge.isEmpty() || source == null
				|| target == null) {
			return null;
		}

		// Get the edge or create a new one attribute confilict handling?
		final Edge edge = Cytoscape.getCyEdge(source, target,
				Semantics.INTERACTION, interaction, true, directed); // ID
		// and
		// canonicalName
		// set
		// when
		// created
		final String id = edge.getIdentifier();

		// set other attributes as indicated in attributeMapping
		// setAttribute(id, mapNetEdge, edgeAttributeMapping);

		return edge;
	}
	
	public static Color stringToColor(String temp){
		int index1=temp.indexOf(",");
		int index2=temp.lastIndexOf(",");
		
		String t1=temp.substring(0, index1);
		String t2=temp.substring(index1+1, index2);
		String t3=temp.substring(index2+1);
		//System.out.println(t1 +":" +t2+":"+t3);
		
		int r=Integer.valueOf(t1.trim());
		int g=Integer.valueOf(t2.trim());
		int b=Integer.valueOf(t3.trim());
		
		return new Color(r, g, b);
	}

	
}
