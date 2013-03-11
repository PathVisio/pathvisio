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

/*
 * Developed by Panagiotis Peikidis
 * http://pekalicious.com/blog/custom-jpanel-cell-with-jbuttons-in-jtable/
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.pluginmanager.impl.PluginManager;
import org.pathvisio.pluginmanager.impl.data.BundleVersion;
import org.pathvisio.pluginmanager.impl.util.Resources;

public class PluginCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

	private BundleVersion bundleVersion;
	
	private JPanel panel;
	private JPanel showButton;
	
	private JLabel text;
	
	private JButton button;
	
	private PluginManager manager;
	
	public PluginCell(boolean installed, PluginManager manager) {
		this.manager = manager;
		text = new JLabel();

		showButton = getButtonPanel(installed);
		panel = new JPanel(new BorderLayout());
		URL imgURL = Resources.getResourceURL("plugin.png");
		if (imgURL != null) {
			ImageIcon icon = new ImageIcon(imgURL);
			JLabel label = new JLabel("   ", icon, JLabel.CENTER);
			panel.add(label, BorderLayout.WEST);
	    }
		panel.add(text, BorderLayout.CENTER);
		panel.add(showButton, BorderLayout.EAST);
	}
	
	private void updateData(BundleVersion res, boolean isSelected, JTable table) {
		this.bundleVersion = res;
		
		text.setText("<html><b>" + res.getName() + "<br><i>" + res.getVersion() + "</i></b>" + "</html>");
		if (isSelected) {
			showButton.setBackground(table.getSelectionBackground());
			showButton.setBorder(new LineBorder(table.getSelectionBackground(), 5));
			panel.setBackground(table.getSelectionBackground());
		}else{
			showButton.setBackground(table.getSelectionForeground());
			showButton.setBorder(new LineBorder(table.getSelectionForeground(), 5));
			panel.setBackground(table.getSelectionForeground());
		}		
	}
	
	private JPanel getButtonPanel(boolean installed) {
		button = new JButton();
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		if(!installed) {
			button.setText("  Install ");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
//					JOptionPane.showMessageDialog(null, "Install:  " + bundleVersion.getBundle().getSymbolicName());
//					System.out.println("ACTION");
					manager.installPluginFromRepo(bundleVersion);
				}
			});
		} else {
			button.setText(" Uninstall ");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
//					LocalRepository repo = pvDesktop.getPluginManager().getRepositoryManager().getLocalRepository();
//					repo.uninstallResource(resource);
//					JOptionPane.showMessageDialog(null, "Remove:  " + resource.getSymbolicName());
				}
			});
		}
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(Box.createVerticalGlue());
		panel.add(button, JPanel.CENTER_ALIGNMENT);
		panel.add(Box.createVerticalGlue());
		return panel;
	}
	
	

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		BundleVersion feed = (BundleVersion)value;
		updateData(feed, true, table);
		return panel;
	}

	public Object getCellEditorValue() {
		return null;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		BundleVersion feed = (BundleVersion)value;

		updateData(feed, isSelected, table);
		return panel;
	}
}
