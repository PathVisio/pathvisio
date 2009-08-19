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

import java.awt.Cursor;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.pathvisio.cytoscape.superpathways.SuperpathwaysGui.ResultRow;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.pathvisio.wikipathways.webservice.WSSearchResult;

import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;

public class FindRelatedPwsDialog extends JDialog {

	SuperpathwaysClient mClient;

	private List<String> mCandidatePw;

	String anchorPwNameID;

	int mNoGeneNode;

	private Map<String, WSSearchResult[]> mNodeIdToPwsSharingNode = new HashMap<String, WSSearchResult[]>();

	List<String> selectedPws;

	public FindRelatedPwsDialog(SuperpathwaysClient client, String s,
			String anchorPw) {
		super(client.getPlugin().mWindow, s);
		mClient = client;
		setSize(250, 300);
		setModal(true);
		initComponents(anchorPw);
		setModal(false);

	}

	private void initComponents(String anchorPw) {

		anchorPwNameID = anchorPw;
		// the following items are in helpPanel Dialog
		helpPanel = new javax.swing.JPanel();
		anchorPathwayLabel = new javax.swing.JLabel();
		sharingNodeNoLabel = new javax.swing.JLabel();
		// anchorPathwayComboBox = new javax.swing.JComboBox();
		anchorPathwayNameIDLabel = new javax.swing.JLabel();

		lowerBoundSharingNodeNoComboBox = new javax.swing.JComboBox();
		candidatePathwaysSharingNodesScrollPane = new javax.swing.JScrollPane();
		candidatePathwaysSharingNodesTable = new javax.swing.JTable();

		explainHelpLabel1 = new javax.swing.JLabel();
		addHelpButton = new javax.swing.JButton();
		lowerBoundLabel = new javax.swing.JLabel();
		upperBoundLabel = new javax.swing.JLabel();
		upperBoundSharingNodeNoComboBox = new javax.swing.JComboBox();
		explainHelpLabel2 = new javax.swing.JLabel();
		// backToSearchButton = new javax.swing.JButton();
		searchHelpButton = new javax.swing.JButton();
		lastLabel = new javax.swing.JLabel();

		// the follwoing code is for Search Help tab
		anchorPathwayLabel.setForeground(new java.awt.Color(0, 0, 255));
		anchorPathwayLabel.setText("Selected Pathway: ");

		anchorPathwayNameIDLabel.setForeground(new java.awt.Color(0, 0, 255));
		anchorPathwayNameIDLabel.setText(anchorPw);

		sharingNodeNoLabel.setForeground(new java.awt.Color(0, 0, 255));
		sharingNodeNoLabel.setText("Sharing");

		// anchorPathwayComboBox.setModel(new
		// DefaultComboBoxModel(mAvailablePathwaysNameIDList.toArray()));

		lowerBoundSharingNodeNoComboBox
				.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
						"1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));

		// candidatePathwaysSharingNodesTable.setModel();

		explainHelpLabel1.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
		explainHelpLabel1.setForeground(new java.awt.Color(102, 0, 0));
		explainHelpLabel1
				.setText("Set the range of sharing nodes number for the selected pathway, a list of candidate");

		explainHelpLabel2.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
		explainHelpLabel2.setForeground(new java.awt.Color(102, 0, 0));
		explainHelpLabel2
				.setText("pathways with sharing nodes would be returned after clicking Search button.");

		addHelpButton.setText("Add");
		addHelpButton
				.setToolTipText("add the selected pathways to the 'Available Pathways' list");

		addHelpButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addHelpButtonActionPerformed(evt);
			}
		});

		lowerBoundLabel.setForeground(new java.awt.Color(0, 0, 255));
		lowerBoundLabel.setText("Minimum");

		upperBoundLabel.setForeground(new java.awt.Color(0, 0, 255));
		upperBoundLabel.setText("Maximum");

		// set the numbers for according to the selected pathway
		int index1 = anchorPw.lastIndexOf("(");
		int index2 = anchorPw.lastIndexOf(")");
		String anchorPwID = anchorPw.substring(index1 + 1, index2);

		// Create a client to the WikiPathways web service
		WikiPathwaysClient client = mClient.getStub();
		WSPathway anchorPathway = new WSPathway();

		try {
			anchorPathway = client.getPathway(anchorPwID);

		} catch (RemoteException e) {
			Logger.log.error(
					"Unable to get the pathway due to the RemoteException", e);
		} catch (ConverterException e) {
			Logger.log.error(
					"Unable to get the pathway due to the ConverterException",
					e);
		}
		// Create the corresponding pathway objects
		Pathway mAnchorPw = new Pathway();

		try {
			mAnchorPw = WikiPathwaysClient.toPathway(anchorPathway);
		} catch (ConverterException e) {
			Logger.log.error(
					"Unable to get the pathway due to the RemoteException", e);
		}

		mNoGeneNode = 0;
		for (PathwayElement pwElm : mAnchorPw.getDataObjects()) {
			if (pwElm.getObjectType() == ObjectType.DATANODE) {
				mNoGeneNode = mNoGeneNode + 1;

			}
		}
		String[] temp = new String[mNoGeneNode];
		for (int i = 1; i <= mNoGeneNode; i++) {
			temp[i - 1] = String.valueOf(i);
		}
		upperBoundSharingNodeNoComboBox
				.setModel(new javax.swing.DefaultComboBoxModel(temp));

		lastLabel.setForeground(new java.awt.Color(0, 0, 255));
		lastLabel.setText("Nodes");

		searchHelpButton.setText("Search");
		searchHelpButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				searchHelpButtonActionPerformed(evt);
			}
		});

		candidatePathwaysSharingNodesTableModel.addColumn("Pathway Name");
		candidatePathwaysSharingNodesTableModel.addColumn("ID");
		candidatePathwaysSharingNodesTableModel.addColumn("No. Shared Nodes");

		candidatePathwaysSharingNodesTable
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		candidatePathwaysSharingNodesScrollPane
				.setViewportView(candidatePathwaysSharingNodesTable);
		candidatePathwaysSharingNodesTable
				.setModel(candidatePathwaysSharingNodesTableModel);

		TableColumn column = null;
		for (int i = 0; i < 3; i++) {
			column = candidatePathwaysSharingNodesTable.getColumnModel()
					.getColumn(i);
			if (i == 0) {
				column.setPreferredWidth(150);
			} else if (i == 1) {
				column.setPreferredWidth(40);
			} else {
				column.setPreferredWidth(60);
			}

		}

		org.jdesktop.layout.GroupLayout helpPanelLayout = new org.jdesktop.layout.GroupLayout(
				helpPanel);
		helpPanel.setLayout(helpPanelLayout);
		helpPanelLayout
				.setHorizontalGroup(helpPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								helpPanelLayout
										.createSequentialGroup()
										.add(
												helpPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																helpPanelLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.add(
																				explainHelpLabel1))
														.add(
																helpPanelLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.add(
																				explainHelpLabel2))
														.add(
																helpPanelLayout
																		.createSequentialGroup()
																		.add(
																				19,
																				19,
																				19)
																		.add(
																				helpPanelLayout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(
																								anchorPathwayLabel)
																						.add(
																								sharingNodeNoLabel))
																		.add(
																				6,
																				6,
																				6)
																		.add(
																				helpPanelLayout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING,
																								false)
																						.add(
																								helpPanelLayout
																										.createSequentialGroup()
																										.add(
																												lowerBoundLabel)
																										.add(
																												18,
																												18,
																												18)
																										.add(
																												lowerBoundSharingNodeNoComboBox,
																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																										.add(
																												18,
																												18,
																												18)
																										.add(
																												upperBoundLabel)
																										.add(
																												18,
																												18,
																												18)
																										.add(
																												upperBoundSharingNodeNoComboBox,
																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
																						.add(
																								helpPanelLayout
																										.createSequentialGroup()
																										.add(
																												23,
																												23,
																												23)
																										.add(
																												anchorPathwayNameIDLabel,
																												0,
																												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE)))
																		.add(
																				18,
																				18,
																				18)
																		.add(
																				lastLabel)
																		.add(
																				18,
																				18,
																				18)
																		.add(
																				searchHelpButton)))
										.addContainerGap(24, Short.MAX_VALUE))
						.add(
								org.jdesktop.layout.GroupLayout.TRAILING,
								helpPanelLayout
										.createSequentialGroup()
										.addContainerGap(190, Short.MAX_VALUE)
										// .add(backToSearchButton)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(addHelpButton).add(26, 26, 26))
						.add(
								helpPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												candidatePathwaysSharingNodesScrollPane,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												402, Short.MAX_VALUE).add(16,
												16, 16)));

		/*
		 * helpPanelLayout.linkSize(new java.awt.Component[] { addHelpButton,
		 * backToSearchButton }, org.jdesktop.layout.GroupLayout.HORIZONTAL);
		 */

		helpPanelLayout
				.setVerticalGroup(helpPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								helpPanelLayout
										.createSequentialGroup()
										.add(33, 33, 33)
										.add(explainHelpLabel1)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.UNRELATED)
										.add(explainHelpLabel2)
										.add(44, 44, 44)
										.add(
												helpPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(anchorPathwayLabel)
														.add(
																anchorPathwayNameIDLabel,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.add(23, 23, 23)
										.add(
												helpPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(lowerBoundLabel)
														.add(
																lowerBoundSharingNodeNoComboBox,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(upperBoundLabel)
														.add(
																upperBoundSharingNodeNoComboBox,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(lastLabel)
														.add(searchHelpButton)
														.add(sharingNodeNoLabel))
										.add(53, 53, 53)
										.add(
												candidatePathwaysSharingNodesScrollPane,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												251, Short.MAX_VALUE)
										.add(29, 29, 29)
										.add(
												helpPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														// .add(backToSearchButton)
														.add(addHelpButton))
										.addContainerGap(131, Short.MAX_VALUE)));

		/*
		 * helpPanelLayout.linkSize(new java.awt.Component[] { addHelpButton,
		 * backToSearchButton }, org.jdesktop.layout.GroupLayout.VERTICAL);
		 */

		helpPanelLayout.linkSize(new java.awt.Component[] { lastLabel,
				lowerBoundLabel, lowerBoundSharingNodeNoComboBox,
				searchHelpButton, sharingNodeNoLabel, upperBoundLabel,
				upperBoundSharingNodeNoComboBox },
				org.jdesktop.layout.GroupLayout.VERTICAL);

		// setLayout(helpPanelLayout);
		setContentPane(helpPanel);
		pack();

	}

	private void searchHelpButtonActionPerformed(java.awt.event.ActionEvent evt) {

		// clean the table
		int num = candidatePathwaysSharingNodesTableModel.getRowCount();
		for (int t = 0; t < num; t++) {
			candidatePathwaysSharingNodesTableModel.removeRow(t);
		}

		candidatePathwaysSharingNodesTable
				.setModel(candidatePathwaysSharingNodesTableModel);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// debugging
		/*
		 * Class columnType =
		 * candidatePathwaysSharingNodesTableModel.getColumnClass(2);
		 * System.out.println("2. in searchHelpButtonActionPerformed, right
		 * after adding column names"); System.out.println(columnType+"");
		 */

		// String anchorPwNameAndId =
		// anchorPathwayComboBox.getSelectedItem().toString();
		int lowerBound = Integer.parseInt(lowerBoundSharingNodeNoComboBox
				.getSelectedItem().toString());
		int upperBound = Integer.parseInt(upperBoundSharingNodeNoComboBox
				.getSelectedItem().toString());

		// mCandidatePwList = findCandidatePwBySharingNodes(lowerBound,
		// upperBound);

		searchSharingNodePwsTask task = new searchSharingNodePwsTask(
				lowerBound, upperBound);

		JTaskConfig config = new JTaskConfig();
		config.displayCancelButton(true);
		// config.displayCloseButton(true);
		// config.displayStatus(true);
		config.setModal(true);
		TaskManager.executeTask(task, config);
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

	}

	private void addHelpButtonActionPerformed(java.awt.event.ActionEvent evt) {

		selectedPws = new ArrayList<String>();
		int[] selectedRowIndices = candidatePathwaysSharingNodesTable
				.getSelectedRows();

		for (int i = 0; i < selectedRowIndices.length; i++) {
			int viewRow = selectedRowIndices[i];
			int modelRow = sorter.modelIndex(viewRow);

			String pwNameId = (String) candidatePathwaysSharingNodesTableModel
					.getValueAt(modelRow, 0)
					+ "("
					+ (String) candidatePathwaysSharingNodesTableModel
							.getValueAt(modelRow, 1) + ")";
			System.out
					.println("After clicking the Add button in the 'Search Help' panel!");
			System.out.println(pwNameId);

			selectedPws.add(pwNameId);
			SuperpathwaysGui spGUI = mClient.getGUI();
			if (!spGUI.selectedPathwaysListModel.contains(pwNameId)) {
				spGUI.selectedPathwaysListModel.addElement((Object) pwNameId);
				spGUI.selectedPathwaysList
						.setModel(spGUI.selectedPathwaysListModel);

				/*if (spGUI.availablePathwaysListModel.getSize() > 0) {
					spGUI.rightButton.setEnabled(true);
				}*/
			}
		}
		// superpathwayPanel.setSelectedIndex(0);
		setVisible(false);

	}

	// private javax.swing.JComboBox anchorPathwayComboBox;

	private TableSorter sorter;

	private javax.swing.JLabel anchorPathwayLabel;

	private javax.swing.JLabel anchorPathwayNameIDLabel;

	// private javax.swing.DefaultComboBoxModel anchorPathwayComboBoxModel;

	// private javax.swing.JButton backToSearchButton;

	private javax.swing.JScrollPane candidatePathwaysSharingNodesScrollPane;

	private javax.swing.JLabel lowerBoundLabel;

	private javax.swing.JLabel explainHelpLabel1;

	private javax.swing.JLabel explainHelpLabel2;

	private javax.swing.JPanel helpPanel;

	private javax.swing.JLabel upperBoundLabel;

	private javax.swing.JComboBox upperBoundSharingNodeNoComboBox;

	private javax.swing.JButton searchHelpButton;

	private javax.swing.JLabel lastLabel;

	private javax.swing.JTable candidatePathwaysSharingNodesTable;

	private javax.swing.JLabel sharingNodeNoLabel;

	private javax.swing.JButton addHelpButton;

	private javax.swing.JComboBox lowerBoundSharingNodeNoComboBox;

	private javax.swing.table.DefaultTableModel candidatePathwaysSharingNodesTableModel = new DefaultTableModel() {
		Class[] classes = { String.class, String.class, Integer.class };

		public Class getColumnClass(int column) {
			return classes[column];
		}
	};;

	public class searchSharingNodePwsTask implements Task {

		TaskMonitor monitor;

		int lowerBound;

		int upperBound;

		boolean cancelled;

		public searchSharingNodePwsTask(int lb, int ub) {
			lowerBound = lb;
			upperBound = ub;
			cancelled = false;
		}

		/**
		 * Run the Task.
		 */
		public void run() {

			try {
				mCandidatePw = new ArrayList<String>();
				if (lowerBound > upperBound) {
					JOptionPane.showMessageDialog(helpPanel,
							"Please reset the range of sharing nodes number!");
				} else {

					Map<String, Integer> sharingNodeNumberofPws = new HashMap<String, Integer>();
					List<String> geneIDList = new ArrayList<String>();
					int percentComplete = 0;
					int t = 0;

					// Create a client to the WikiPathways web service
					WikiPathwaysClient client = mClient.getStub();

					int index1 = anchorPwNameID.lastIndexOf("(");
					int index2 = anchorPwNameID.lastIndexOf(")");
					String anchorPwID = anchorPwNameID.substring(index1 + 1,
							index2);

					WSPathway anchorPathway = new WSPathway();

					try {
						anchorPathway = client.getPathway(anchorPwID);

					} catch (RemoteException e) {
						Logger.log
								.error(
										"Unable to get the pathway due to the RemoteException",
										e);
					} catch (ConverterException e) {
						Logger.log
								.error(
										"Unable to get the pathway due to the ConverterException",
										e);
					}
					// Create two corresponding pathway objects
					Pathway mAnchorPw = new Pathway();

					try {
						mAnchorPw = WikiPathwaysClient.toPathway(anchorPathway);
					} catch (ConverterException e) {
						Logger.log
								.error(
										"Unable to get the pathway due to the RemoteException",
										e);
					}

					// the following code is get a map "mNodeIdToPwsSharingNode"
					// with key of GeneID and value of a list of pathways which
					// contain the GeneID
					for (PathwayElement pwElm : mAnchorPw.getDataObjects()) {
						// Only take elements with type DATANODE (genes,
						// proteins, metabolites)
						if (pwElm.getObjectType() == ObjectType.DATANODE) {

							if (!cancelled) {
								percentComplete = (int) (((double) t / mNoGeneNode) * 98);

								System.out.println(pwElm.getXref().toString());
								geneIDList.add(pwElm.getXref().toString());

								try {
									WSSearchResult[] PwsSharingNode = client
											.findPathwaysByXref(pwElm.getXref());
									// System.out.println("" +
									// PwsSharingNode.length);
									mNodeIdToPwsSharingNode.put(pwElm.getXref()
											.toString(), PwsSharingNode);

									if (monitor != null) {
										monitor
												.setPercentCompleted(percentComplete);
									}

								} catch (RemoteException e) {
									Logger.log
											.error(
													"Unable to find the candidate pathways due to the RemoteException",
													e);
								}
							}
						}
						t++;

					}

					// the following code is for converting the above map to
					// another map "sharingNodeNumberofPws" with key of
					// the name and id of a pathway, and value of the number of
					// shared node of this pathway and the anchor pathway
					for (int i = 0; i < mNoGeneNode; i++) {
						if (!cancelled) {
							WSSearchResult[] pwsArray = mNodeIdToPwsSharingNode
									.get(geneIDList.get(i));

							for (int j = 0; j < pwsArray.length; j++) {
								WSSearchResult pw = pwsArray[j];
								// pay attention to the following two code lines
								SuperpathwaysGui spGui = mClient.getGUI();
								ResultRow pwResultRow = spGui.new ResultRow(pw);
								String onePwNameAndId = pwResultRow
										.getProperty(ResultProperty.NAME)
										+ "("
										+ pwResultRow
												.getProperty(ResultProperty.ID)
										+ ")";

								if (sharingNodeNumberofPws
										.containsKey(onePwNameAndId)) {
									Integer oldValue = sharingNodeNumberofPws
											.get(onePwNameAndId);
									Integer newValue = new Integer(oldValue + 1);
									sharingNodeNumberofPws.put(onePwNameAndId,
											newValue);
								} else {
									sharingNodeNumberofPws.put(onePwNameAndId,
											1);
								}
							}
						}
					}

					// the following code is for displaying the result in the
					// table of "Search Help" panel
					if (!cancelled) {
						Set<String> sharingNodePwsSet = sharingNodeNumberofPws
								.keySet();
						Iterator<String> it = sharingNodePwsSet.iterator();
						while (it.hasNext()) {
							String temp = it.next();
							Integer value = sharingNodeNumberofPws.get(temp);
							if (value >= lowerBound && value <= upperBound) {
								mCandidatePw.add(temp
										+ ", sharing node number: "
										+ String.valueOf(value));
							}
						}
					}

				}

				// mCandidatePw is a list of string with elements in format
				// "Pathway
				// Name (pw id), sharing node number: a int"
				// System.out.println(mCandidatePw.size()+"");
				if (!cancelled) {
					Iterator<String> it = mCandidatePw.iterator();

					while (it.hasNext()) {
						String temp = it.next();
						// candidatePathwaysSharingNodesListModel.addElement(temp);

						// System.out.println(temp);
						// parse the string into three parts: pathway name, id,
						// and
						// No.
						// Sharing Nodes
						int index1 = temp.lastIndexOf(",");
						String temp1 = temp.substring(0, index1);

						int index2 = temp1.lastIndexOf("(");
						int index3 = temp1.lastIndexOf(")");
						String pwName = temp1.substring(0, index2);
						// System.out.println(pwName);
						String pwId = temp1.substring(index2 + 1, index3);
						// System.out.println(pwId);

						// String temp2 = temp.substring(index1+1);
						int index4 = temp.lastIndexOf(":");
						String NoSharingNode = temp.substring(index4 + 2);
						Integer NoSharingNodeInteger = Integer
								.valueOf(NoSharingNode);

						Object[] row = new Object[3];
						row[0] = pwName;
						row[1] = pwId;
						row[2] = NoSharingNodeInteger;
						// row[2] = NoSharingNode;
						candidatePathwaysSharingNodesTableModel.addRow(row);
					}

					sorter = new TableSorter(
							candidatePathwaysSharingNodesTableModel);
					candidatePathwaysSharingNodesTable.setModel(sorter);
					sorter.setTableHeader(candidatePathwaysSharingNodesTable
							.getTableHeader());

					// candidatePathwaysSharingNodesList.setModel(candidatePathwaysSharingNodesListModel);
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

					if (monitor != null) {
						monitor.setPercentCompleted(100);
					}
					// System.out.println("We reach here 2!");
				}
			} catch (Exception e) {
				Logger.log.error("Error while searching candidate pathways", e);
				JOptionPane.showMessageDialog(mClient.getGUI(), "Error: "
						+ e.getMessage() + ". See log for details", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

		}

		public void halt() {
			cancelled = true;
		}

		public void setTaskMonitor(TaskMonitor m)
				throws IllegalThreadStateException {
			monitor = m;
		}

		public String getTitle() {
			return new String(
					"Searching candidate pathways with shared nodes...");
		}
	}

}