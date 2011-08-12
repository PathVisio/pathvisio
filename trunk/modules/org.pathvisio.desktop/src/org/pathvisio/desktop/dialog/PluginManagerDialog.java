package org.pathvisio.desktop.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.apache.felix.bundlerepository.Resource;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.OnlineRepository;

public class PluginManagerDialog extends JDialog {

	private JDialog dlg;
	private PvDesktop pvDesktop;
	private JPanel mainPanel;
	
	public PluginManagerDialog(PvDesktop pvDesktop) {
		this.pvDesktop = pvDesktop;
		dlg = this;
	}
	
	public void createAndShowGUI() {
		dlg.setPreferredSize(new Dimension(610, 430));
		dlg.setTitle("Plug-in Manager");
		dlg.setLayout(new BorderLayout());
		dlg.setResizable(false);
		
		// top panel = search bar
		
		// center panel = description
		mainPanel = new JPanel();
		dlg.add(getMainPanel("overview", null), BorderLayout.CENTER);
		
		// west panel = overview
		dlg.add(getOverviewPanel(), BorderLayout.WEST);
		dlg.setAlwaysOnTop(true);
		dlg.pack();
		dlg.setLocationRelativeTo(pvDesktop.getFrame());
		dlg.setVisible(true);
	}

	private Component getOverviewPanel() {
		
		DefaultMutableTreeNode top = new DefaultMutableTreeNode ("Plug-in Manager       ");
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(createTreeSelectionListener());
		
		createNodes(top);
		
		JScrollPane treeView = new JScrollPane(tree);
		treeView.setBackground(Color.white);
		 
		return treeView;
	}
	
	private JTree tree;
	
	private TreeSelectionListener createTreeSelectionListener() {
		TreeSelectionListener listener = new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

				if (node == null)
					return;

				Object nodeInfo = node.getUserObject();
				if(nodeInfo.equals("Plug-in Manager       ")) {
					// TODO: overview page
					System.out.println("OVERVIEW");
					updateMainPanel("overview", null);
				} else if (nodeInfo.equals("Available Plug-ins")) {
					// TODO: show all available plug-ins
					System.out.println("ALL PLUGINS");
					System.out.println(pvDesktop.getPluginManager().getRepositoryManager().getAvailableResources());
					updateMainPanel("avail", null);
				} else if (nodeInfo instanceof OnlineRepository) {
					// TODO: show available plug-ins for specific Repository
					System.out.println("PLUGINS FOR REPOSITORY " + nodeInfo);
					updateMainPanel("avail", (OnlineRepository) nodeInfo);
				} else if (nodeInfo.equals("Installed Plug-ins")) {
					// TODO: show list of installed plugins
					System.out.println("INSTALLED PLUGINS");
					System.out.println(pvDesktop.getPluginManager().getRepositoryManager().getInstalledResources());
					updateMainPanel("install", null);
					
				}
				
			}
		};
		return listener;
	}
	
	private void updateMainPanel(String msg, OnlineRepository repo) {
		mainPanel.removeAll();
		mainPanel = getMainPanel(msg, repo);
		mainPanel.updateUI();
		mainPanel.revalidate();
	}

	private JPanel getMainPanel(String msg, OnlineRepository repo) {
		mainPanel.setBackground(Color.WHITE);
		if(repo == null) {
			if(msg.equals("overview")) {
				JLabel label = new JLabel("Overview");
				mainPanel.add(label);
			} else if (msg.equals("avail")) {
				List<Resource> resources = new ArrayList<Resource>();
				for(Resource res : pvDesktop.getPluginManager().getRepositoryManager().getAvailableResources()) {
					resources.add(res);
				}
				
				JTable table = new JTable(new ResourceTableModel(resources));
				table.setSelectionForeground(new Color(245, 245, 245));
				table.setSelectionBackground(Color.white);
				table.setDefaultRenderer(Resource.class, new ResourceCell(false, pvDesktop));
				table.setDefaultEditor(Resource.class, new ResourceCell(false, pvDesktop));
				table.setRowHeight(70);
				
				mainPanel.add(new JScrollPane(table));
			} else if (msg.equals("install")) {
				List<Resource> resources = new ArrayList<Resource>();
				for(Resource res : pvDesktop.getPluginManager().getRepositoryManager().getInstalledResources()) {
					resources.add(res);
				}
				
				JTable table = new JTable(new ResourceTableModel(resources));
				table.setSelectionForeground(new Color(245, 245, 245));
				table.setSelectionBackground(Color.white);
				table.setDefaultRenderer(Resource.class, new ResourceCell(true, pvDesktop));
				table.setDefaultEditor(Resource.class, new ResourceCell(true, pvDesktop));
				table.setRowHeight(70);
				
				mainPanel.add(new JScrollPane(table));
			}
		} else {
			
		}
		return mainPanel;
	}

	private void createNodes(DefaultMutableTreeNode top) {
	    DefaultMutableTreeNode repositories = null;
	    DefaultMutableTreeNode installed = null;
	    
	    repositories = new DefaultMutableTreeNode("Available Plug-ins");
	    top.add(repositories);

	    installed = new DefaultMutableTreeNode("Installed Plug-ins");
	    top.add(installed);
	    
//	    for(Repository repo : pvDesktop.getPluginManager().getPreferences().getRepositories()) {
//	    	DefaultMutableTreeNode repoNode = new DefaultMutableTreeNode(repo);
//	    	repositories.add(repoNode);
//	    }
	}
}
