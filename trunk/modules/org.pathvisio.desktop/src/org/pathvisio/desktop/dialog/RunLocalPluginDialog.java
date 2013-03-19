// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2013 BiGCaT Bioinformatics
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
package org.pathvisio.desktop.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.util.BrowseButtonActionListener;
import org.pathvisio.gui.dialogs.OkCancelDialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * dialog that allows the user to specify a directory
 * all OSGi bundles in that directory will be installed 
 * and started
 * status information on each will be shown in the
 * StatusDialog dialog
 * 
 * @author martina
 *
 */
public class RunLocalPluginDialog extends JDialog 
{

	private JDialog dlg;
	private PvDesktop desktop;
	private JTextField tfDir;
	private CellConstraints cc = new CellConstraints();
	private List<File> files;
	private Map<File, JCheckBox> cbs;
	
	public RunLocalPluginDialog(PvDesktop desktop)
	{
		super();
		cbs = new HashMap<File, JCheckBox>();
		files = new ArrayList<File>();
		dlg = this;
		this.desktop = desktop;
	}
	
	public void createAndShowGUI() 
	{
		init();
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		FormLayout layout = new FormLayout("4dlu, pref, 4dlu, min, 4dlu, min, 4dlu", "4dlu, pref, 4dlu, pref");
		panel.setLayout(layout);
		
		panel.add(new JLabel("Install local plugins. Please select directory."), cc.xy(2, 2));
		tfDir = new JTextField();
		JButton browseBtn = new JButton("Browse");
		browseBtn.addActionListener(new BrowseButtonActionListener(tfDir, dlg, JFileChooser.DIRECTORIES_ONLY));
		
		panel.add(tfDir, cc.xy(2, 4));
		panel.add(browseBtn, cc.xy(4, 4));
	
		dlg.add(createBtnPanel(), BorderLayout.SOUTH);
		dlg.add(panel, BorderLayout.CENTER);

		dlg.setVisible(true);
	}
	
	private JPanel createBtnPanel() 
	{
		JPanel btnPanel = new JPanel();
		btnPanel.setBackground(Color.white);
		
		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() 
		{	
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				dlg.dispose();
			}
		});
		
		JButton startBtn = new JButton("Start");
		startBtn.addActionListener(new ActionListener() 
		{	
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if(!tfDir.getText().equals("")) 
				{
					File file = new File(tfDir.getText());
					if(file.exists()) 
					{
						for(File f : file.listFiles()) {
							if(f.getName().endsWith(".jar")) {
								files.add(f);
							}
						}
						dlg.dispose();
						if(files.isEmpty()) {
							JOptionPane.showMessageDialog(desktop.getFrame(), "No jar files found in this directory");
						} else {
							ShowFilesDialog showDlg = new ShowFilesDialog(desktop, desktop.getFrame());
							showDlg.setDialogComponent(createFileList());
							showDlg.pack();
							showDlg.setVisible(true);
							if(showDlg.getExitCode().equals(ShowFilesDialog.OK)) {
								List<File> plugins = new ArrayList<File>();
								List<File> dependencies = new ArrayList<File>();
								for(File f : cbs.keySet()) {
									if(cbs.get(f).isSelected()) {
										plugins.add(f);
									} else {
										dependencies.add(f);
									}
								}
								if(!plugins.isEmpty()) {
									desktop.getPluginManagerExternal().installLocalPlugins(plugins, dependencies);
								} else {
									JOptionPane.showMessageDialog(desktop.getFrame(), "No pathvisio plugins specified.");
								}
							}
						}
					}
				}
			}
		});
	
		btnPanel.add(startBtn);
		btnPanel.add(cancelBtn);
		return btnPanel;
	}
	
	private Component createFileList() {

		String rowLayout = "4dlu,pref,15dlu,4dlu,";
		for(int i = 0; i < files.size(); i++) {
			rowLayout = rowLayout + "pref,4dlu,";
		}
		rowLayout = rowLayout + "4dlu";
		
		FormLayout layout = new FormLayout("4dlu,pref,4dlu,pref,5dlu", rowLayout);
		CellConstraints cc = new CellConstraints();
		
		PanelBuilder panel = new PanelBuilder(layout);
		panel.setBackground(Color.white);
		panel.addLabel("Please select all files that implement the PathVisio plugin interface.", cc.xy(2, 2));
		panel.addSeparator("", cc.xyw(2, 3, 3));
		
		int row = 5;
		for(File f : files) {
			panel.add(new JLabel(f.getName()), cc.xy(2, row));
			JCheckBox cb = new JCheckBox();
			cb.setBackground(Color.white);
			panel.add(cb, cc.xy(4, row));
			cbs.put(f,cb);
			row = row + 2;
		}
		
		JScrollPane pane = new JScrollPane(panel.getPanel());
		pane.setBackground(Color.white);
		
		return pane;
	}
	
	private void init() 
	{
		dlg.setPreferredSize(new Dimension(600, 200));
		dlg.setTitle("Install local plugin");
		dlg.setBackground(Color.white);
		dlg.setLayout(new BorderLayout());
		dlg.setResizable(false);
		dlg.setAlwaysOnTop(true);
		dlg.setModal(true);
		dlg.pack();
		dlg.setLocationRelativeTo(desktop.getFrame());
	}
	
	private class ShowFilesDialog extends OkCancelDialog 
	{
		
		public ShowFilesDialog(PvDesktop desktop, Component listFiles) 
		{
			super(desktop.getFrame(), "Install local plugins", listFiles, true, true);
		}
	}
}
