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
package org.pathvisio.gui.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.pathvisio.plugin.PluginManager.PluginInfo;

import com.jgoodies.forms.layout.CellConstraints;

/**
 * Creates and displays the Plugin Manager dialog,
 * showing information about each plugin,
 * where they were found, and if there was an error during initialization
 */
public class PluginManagerDlg
{
	private PvDesktop pvDesktop;


	/**
	 * call this to open the dialog
	 */
	public void createAndShowGUI()
	{
		final JFrame dlg = new JFrame();

		DefaultMutableTreeNode top = new DefaultMutableTreeNode ("Plugin Manager");
		JTree tree = new JTree(top);
		DefaultMutableTreeNode active = new DefaultMutableTreeNode ("active");
		top.add (active);

		dlg.setLayout (new BorderLayout());
		dlg.add (new JScrollPane (tree), BorderLayout.CENTER);

		JButton btnOk = new JButton();
		btnOk.setText("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				dlg.setVisible (false);
				dlg.dispose();
			}
		});
		CellConstraints cc = new CellConstraints();
		JPanel panelSouth = new JPanel();
		panelSouth.add (btnOk, cc.xyw (2, 6, 3, "center, top"));
		dlg.add (panelSouth, BorderLayout.SOUTH);
		
		DefaultMutableTreeNode errors = new DefaultMutableTreeNode ("errors");
		top.add (errors);

		// fill in the contents of the tree view
		for (PluginInfo inf : pvDesktop.getPluginManager().getPluginInfo())
		{
			if (inf.error == null)
			{
				DefaultMutableTreeNode plugin;
				plugin = new DefaultMutableTreeNode(inf.plugin == null ? "null" : inf.plugin.getSimpleName());
				plugin.add (new DefaultMutableTreeNode ("" + inf.plugin));
				plugin.add (new DefaultMutableTreeNode ("Param: " + inf.param));

				if (inf.jar != null)
					plugin.add (new DefaultMutableTreeNode ("Jar: " + inf.jar.getAbsolutePath()));
				active.add(plugin);
			}
			else
			{
				DefaultMutableTreeNode error;
				error = new DefaultMutableTreeNode("" + inf.error.getClass());
				error.add (new DefaultMutableTreeNode ("" + inf.error));
				if (inf.plugin != null)
						error.add (new DefaultMutableTreeNode ("Class: " + inf.plugin.getName()));
				error.add (new DefaultMutableTreeNode ("Param: " + inf.param));

				if (inf.jar != null)
					error.add (new DefaultMutableTreeNode ("Jar: " + inf.jar.getAbsolutePath()));
				errors.add (error);
			}
		}
		tree.expandPath(new TreePath(active.getPath()));
		tree.expandPath(new TreePath(errors.getPath()));
		tree.setRootVisible(false);
		tree.setEditable(false);
		dlg.setTitle("About Plugins");
		dlg.pack();
		dlg.setLocationRelativeTo(pvDesktop.getFrame());
		dlg.setVisible(true);
	}

	public PluginManagerDlg(PvDesktop desktop)
	{
		this.pvDesktop = desktop;
	}
}
