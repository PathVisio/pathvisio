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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.util.BrowseButtonActionListener;

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
	
	public RunLocalPluginDialog(PvDesktop desktop)
	{
		super();
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
						desktop.getPluginManagerExternal().installLocalPlugins(file);
						dlg.dispose();
					}
				}
			}
		});
	
		btnPanel.add(startBtn);
		btnPanel.add(cancelBtn);
		return btnPanel;
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
	
//	private class StatusDialog extends JDialog 
//	{
//		
//		private JDialog dlg;
//		private PvDesktop desktop;
//		
//		public StatusDialog(PvDesktop desktop) 
//		{
//			super();
//			dlg = this;
//			this.desktop = desktop;
//		}
//		
//		public void createAndShowGUI(Map<String, String> status) 
//		{
//			init();
//			
//			// splits up bundles that started with out problems
//			// and bundles that couldn't be started
//			Map<String, String> running = new HashMap<String, String>();
//			Map<String, String> problems = new HashMap<String, String>();
//			
//			for(String str : status.keySet()) 
//			{
//				if(status.get(str).equals("started") || status.get(str).equals("already installed")) running.put(str, status.get(str));
//				else problems.put(str, status.get(str));
//			}
//
//			String rowLayout = "4dlu,pref,";
//			for(int i = 0; i < running.size(); i++) {
//				rowLayout = rowLayout + "4dlu,pref,";
//			}
//			rowLayout = rowLayout + "4dlu, 30dlu,pref,";
//			for(int i = 0; i < problems.size(); i++) {
//				rowLayout = rowLayout + "4dlu,pref,";
//			}
//			rowLayout = rowLayout + "15dlu";
//			FormLayout layout = new FormLayout("4dlu, pref, 4dlu, 150dlu, 4dlu", rowLayout);
//			PanelBuilder builder = new PanelBuilder(layout);
//			builder.setBackground(Color.white);
//			if(running.size() > 0) {
//				builder.addLabel("Bundles started:", cc.xy(2, 2));
//				builder.addSeparator("", cc.xyw(2, 3, 3));
//			}
//			int count = 4;
//			for(String b : running.keySet()) {
//				builder.add(new JLabel("    " + b), cc.xy(2, count));
//				JTextArea ta = new JTextArea(status.get(b));
//				ta.setForeground(Color.GREEN);
//				ta.setBackground(Color.white);
//				ta.setLineWrap(true);
//				ta.setEditable(false);
//				builder.add(ta, cc.xy(4, count));
//				builder.addSeparator("", cc.xyw(2, count+1, 3));
//				count = count + 2;
//			}
//			count = count+1;
//			if(problems.size() > 0) {
//				builder.addLabel("Problems occured in:", cc.xy(2, count));
//				builder.addSeparator("", cc.xyw(2, count+1, 3));
//			}
//			count = count+2;
//			
//			for(String b : problems.keySet()) {
//				builder.add(new JLabel("    " + b), cc.xy(2, count));
//				JTextArea ta = new JTextArea(status.get(b));
//				ta.setBackground(Color.white);
//				ta.setForeground(Color.red);
//				ta.setLineWrap(true);
//				ta.setEditable(false);
//				builder.add(ta, cc.xy(4, count));
//				builder.addSeparator("", cc.xyw(2, count+1, 3));
//				count = count + 2;
//			}
//			
//			JScrollPane pane = new JScrollPane(builder.getPanel());
//			pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//			pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//			dlg.add(pane, BorderLayout.CENTER);
//			dlg.add(createBtnPanel(), BorderLayout.SOUTH);
//
//			dlg.setVisible(true);
//		}
//		
//		private JPanel createBtnPanel() 
//		{
//			JPanel btnPanel = new JPanel();
//			btnPanel.setBackground(Color.white);
//			
//			JButton cancelBtn = new JButton("OK");
//			cancelBtn.addActionListener(new ActionListener() 
//			{				
//				@Override
//				public void actionPerformed(ActionEvent e)
//				{
//					dlg.dispose();
//				}
//			});
//			
//			btnPanel.add(cancelBtn);
//			return btnPanel;
//		}
//		
//		private void init() 
//		{
//			dlg.setLayout(new BorderLayout());
//			dlg.setPreferredSize(new Dimension(600, 450));
//			dlg.setTitle("Status");
//			dlg.setBackground(Color.white);
//			dlg.setLayout(new BorderLayout());
//			dlg.setResizable(false);
//			dlg.setAlwaysOnTop(true);
//			dlg.setModal(true);
//			dlg.pack();
//			dlg.setLocationRelativeTo(desktop.getFrame());
//		}
//	}
}
