// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.pluginmanager.impl.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.pathvisio.pluginmanager.impl.PluginManager;
import org.pathvisio.pluginmanager.impl.Utils;
import org.pathvisio.pluginmanager.impl.data.BundleVersion;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PluginManagerDialog extends JDialog {

	private JDialog dlg;
	private PluginManager manager;
	private JPanel availablePanel;
	private JPanel installedPanel;
	private JPanel errorPanel;
	
	public PluginManagerDialog(PluginManager manager) {
		this.manager = manager;
		dlg = this;
	}
	
	public void createAndShowGUI(JFrame parent) {
		dlg.setPreferredSize(new Dimension(700, 500));
		dlg.setTitle("Plug-in Manager");
		dlg.setLayout(new BorderLayout());
		dlg.setResizable(false);

		dlg.add(getContentPanel(), BorderLayout.CENTER);

		dlg.setAlwaysOnTop(true);
		dlg.pack();
		dlg.setLocationRelativeTo(parent);
		dlg.setVisible(true);
	}
	
	private Component getContentPanel() {
		JTabbedPane pane = new JTabbedPane();
		pane.setBackground(Color.WHITE);
		availablePanel = getAvail();
		pane.add("Available", availablePanel);
		installedPanel = getInstalled();
		pane.add("Installed", installedPanel);
		errorPanel = getErrorPanel();
		pane.add("Errors", errorPanel);
		
		return pane;
	}

	private JPanel getErrorPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.WHITE);
		CellConstraints cc = new CellConstraints();
		List<BundleVersion> plugins = new ArrayList<BundleVersion>();
		for(BundleVersion plugin : manager.getErrors()) {
			if(!plugin.isInstalled()) {
				plugins.add(plugin);
			}
		}
		
		if (plugins.isEmpty()) {
			panel.add(new JLabel("No errors occured."), BorderLayout.NORTH);
		} else {
			String rowLayout = "4dlu,pref,";
			for(int i = 0; i < plugins.size(); i++) {
				rowLayout = rowLayout + "4dlu,pref,";
			}
			rowLayout = rowLayout + "15dlu";
			FormLayout layout = new FormLayout("4dlu, pref, 4dlu, 150dlu, 4dlu", rowLayout);
			PanelBuilder builder = new PanelBuilder(layout);
			builder.setBackground(Color.white);
			if(plugins.size() > 0) {
				builder.addLabel("Problems occured:", cc.xy(2, 2));
				builder.addSeparator("", cc.xyw(2, 3, 3));
			}
			int count = 4;
			for(BundleVersion b : plugins) {
				builder.add(new JLabel("    " + b), cc.xy(2, count));
				JTextArea ta = new JTextArea(b.getBundle().getStatus().getMessage());
				ta.setForeground(Color.red);
				ta.setBackground(Color.white);
				ta.setLineWrap(true);
				ta.setEditable(false);
				builder.add(ta, cc.xy(4, count));
				builder.addSeparator("", cc.xyw(2, count+1, 3));
				count = count + 2;
			}
			
			JScrollPane pane = new JScrollPane(builder.getPanel());
			pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			panel.add(pane, BorderLayout.CENTER);
		}
		
		return panel;
	}

	private JTable available;
	private JPanel availInfo;
	
	private JPanel getAvail() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));
		panel.setBackground(Color.white);
		List<BundleVersion> plugins = new ArrayList<BundleVersion>();
		for(BundleVersion plugin : manager.getAvailablePlugins()) {
			if(!plugin.isInstalled()) {
				plugins.add(plugin);
			}
		}
		if (plugins.isEmpty()) {
			panel.setLayout(new BorderLayout());
			if(manager.getOnlineRepos().size() == 0) {
				panel.add(new JLabel("Could not connect to online repository."), BorderLayout.NORTH);
			} else {
				panel.add(new JLabel("No plugins available."), BorderLayout.NORTH);
			}
		} else {
			available = new JTable(new PluginTableModel(plugins));
			available.setBackground(Color.white);
			available.setSelectionForeground(Color.white);
			available.setSelectionBackground(new Color(245, 255, 255));
			available.setDefaultRenderer(BundleVersion.class, new PluginCell(false, manager));
			available.setDefaultEditor(BundleVersion.class, new PluginCell(false, manager));
			available.setRowHeight(70);
			available.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					int row = available.getSelectedRow();
					int column = available.getSelectedColumn();
					BundleVersion p = (BundleVersion) available.getValueAt(row, column);
					updatePluginDetails(p, availInfo);
				}
			});
			
			panel.add(new JScrollPane(available));
			availInfo = new JPanel();
			availInfo.setBackground(Color.white);
			panel.add(availInfo);
		}
		return panel;
	}
	
	protected void updatePluginDetails(BundleVersion p, JPanel panel) {
		panel.removeAll();
		panel.setLayout(new GridLayout(1,1));
		panel.add(getPluginData(p).getPanel());
		panel.revalidate();
		panel.repaint();
	}
	
	private PanelBuilder getPluginData(BundleVersion p) {
		FormLayout layout = new FormLayout("5dlu, fill:pref:grow, 5dlu","5dlu,pref,15dlu,pref,10dlu,pref,10dlu,pref,10dlu,pref,10dlu,pref,10dlu,pref,10dlu");
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.setBackground(Color.white);
		
		builder.addLabel(p.getBundle().getName(), cc.xy(2, 2));
		builder.addSeparator("", cc.xyw(2, 3, 1));
		builder.addLabel((p.getBundle().getName().equals(p.getBundle().getSymbolicName()) ? "Version: " + p.getVersion() : ("<html>Version: " + p.getVersion() + "<br>" + p.getBundle().getSymbolicName() + "</html>")), cc.xy(2, 4));
		builder.addLabel((p.getBundle().getShortDescription() != null ? Utils.printDescription(p.getBundle().getShortDescription(), 40) : ""), cc.xy(2, 6));
		builder.addLabel((p.getReleaseDate() != null ? "Release date: " + p.getReleaseDate() : ""), cc.xy(2, 10));
	
		builder.addLabel(Utils.printAuthors(p), cc.xy(2, 12));
		builder.add(getWebsiteLabel(p), cc.xy(2, 14));
		return builder;
	}
	
	
	
	private JLabel getWebsiteLabel(final BundleVersion p) {
		if(p.getBundle().getWebsite() != null && !p.getBundle().getWebsite().equals("")) {
			JLabel label = new JLabel("<html>Visit the <a href=\"" + p.getBundle().getWebsite() + "\">website</a> for more information.</html>");
			label.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent arg0) {}
				
				@Override
				public void mousePressed(MouseEvent arg0) {}
				
				@Override
				public void mouseExited(MouseEvent arg0) {}
				
				@Override
				public void mouseEntered(MouseEvent arg0) {}
				
				@Override
				public void mouseClicked(MouseEvent arg0) {
					try {
						Desktop.getDesktop().browse(new URI(p.getBundle().getWebsite()));
					} catch (Exception e) {
						new JOptionPane("Could not open website.", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			return label;
		}
		
		return new JLabel();
	}
	
	
	
	private JTable installed;
	private JPanel installedInfo;

	private JPanel getInstalled() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));
		panel.setBackground(Color.white);
		
		List<BundleVersion> plugins = new ArrayList<BundleVersion>();
		for(BundleVersion plugin : manager.getInstalledPlugins()) {
			plugins.add(plugin);
		}
		
		if (plugins.isEmpty()) {
			panel.setLayout(new BorderLayout());
			panel.add(new JLabel("No plugins installed."), BorderLayout.NORTH);
		} else {
			installed = new JTable(new PluginTableModel(plugins));
			installed.setBackground(Color.white);
			installed.setSelectionForeground(Color.white);
			installed.setSelectionBackground(new Color(245, 255, 255));
			installed.setDefaultRenderer(BundleVersion.class, new PluginCell(true, manager));
			installed.setDefaultEditor(BundleVersion.class, new PluginCell(true, manager));
			installed.setRowHeight(70);
			installed.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					int row = installed.getSelectedRow();
					int column = installed.getSelectedColumn();
					if(row != -1 && column != -1) {
						BundleVersion p = (BundleVersion) installed.getValueAt(row, column);
						updatePluginDetails(p, installedInfo);
					} else {
						installedInfo.removeAll();
						installedInfo.revalidate();
						installedInfo.repaint();
					}
				}
			});
			
			panel.add(new JScrollPane(installed));
			installedInfo = new JPanel();
			installedInfo.setBackground(Color.white);
			panel.add(installedInfo);
		}
		
		
		return panel;
	}
	
	public void updateData() {
		availablePanel.removeAll();
		availablePanel.setLayout(new GridLayout(1,1));
		availablePanel.add(getAvail());
		availablePanel.revalidate();
		availablePanel.repaint();

		installedPanel.removeAll();
		installedPanel.setLayout(new GridLayout(1,1));
		installedPanel.add(getInstalled());
		installedPanel.revalidate();
		installedPanel.repaint();
	}
}
