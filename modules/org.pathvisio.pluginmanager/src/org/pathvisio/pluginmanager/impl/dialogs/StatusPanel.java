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
package org.pathvisio.pluginmanager.impl.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pathvisio.pluginmanager.impl.PluginManager;
import org.pathvisio.pluginmanager.impl.PluginManager.PluginManagerStatus;
import org.pathvisio.pluginmanager.impl.data.PVRepository;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * StatusPanel.java
 * creates JPanel object shown in the
 * plugin manager dialog tab "Status"
 * 
 * @author martina
 *
 */
public class StatusPanel extends JPanel {

	private PluginManager manager;
	
	public StatusPanel(PluginManager manager) {
		this.manager = manager;
		this.setBackground(Color.white);
		this.setLayout(new BorderLayout());
		
		if(manager.getStatus().equals(PluginManagerStatus.CONNECTION_COMPLETED_FAILURE)) {
			// TODO: add warning about connection failure
			// TODO: enable retrying connection
			this.add(getConnectionErrorPanel(), BorderLayout.NORTH);
		} else if (manager.getStatus().equals(PluginManagerStatus.CONNECTION_COMPLETED_SUCCESSFULLY)) {
			// TODO show all repositories
			this.add(getConnectionSuccessPanel(), BorderLayout.NORTH);
			this.add(getRepositoryList(), BorderLayout.CENTER);
		} else {
			// print that connection is still busy building up
			this.add(progressPanel(), BorderLayout.NORTH);
		}
	}
	
	private JPanel getConnectionSuccessPanel() {
		FormLayout layout = new FormLayout("5dlu,pref,5dlu,pref,fill:pref:grow","10dlu,pref,5dlu,pref,15dlu");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBackground(Color.white);
		CellConstraints cc = new CellConstraints();
		
		builder.addLabel(manager.getStatusMessage(), cc.xy(2, 2));
		
		builder.addSeparator("", cc.xyw(2, 5, 4));
		
		return builder.getPanel();
	}
	
	private JComponent getRepositoryList() {
		int count = manager.getOnlineRepos().size();
		String row = "10dlu,pref,5dlu,";
		for(int i = 0; i < count; i++) {
			row = row + "pref, 5dlu,";
		}
		row = row + "5dlu";

		FormLayout layout = new FormLayout("5dlu,pref,5dlu,pref,5dlu",row);
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBackground(Color.white);
		CellConstraints cc = new CellConstraints();

		builder.addLabel("Repository name", cc.xy(2, 2));
		builder.addLabel("URL", cc.xy(4, 2));
		
		int currRow = 4;
		for(PVRepository repo : manager.getOnlineRepos()) {
			JLabel name = new JLabel(repo.getName());
			Font newLabelFont=new Font(name.getFont().getName(),Font.ITALIC, name.getFont().getSize());
			name.setFont(newLabelFont);
			builder.add(name, cc.xy(2, currRow));
			
			JEditorPane link = new JEditorPane();
			link.setBackground(Color.white);
			link.setEditable(false);
			link.setContentType("text/html");
			link.setText("<html><a href=\"" + repo.getUrl() + "\">" + repo.getUrl() + "</a></html>");
			link.addHyperlinkListener(manager.getDesktop().getSwingEngine());
			builder.add(link, cc.xy(4, currRow));
			currRow = currRow + 2;
		}
		return builder.getPanel();
	}
	
	private JPanel getConnectionErrorPanel() {
		FormLayout layout = new FormLayout("5dlu,pref,5dlu,pref,fill:pref:grow","10dlu,pref,5dlu,pref,15dlu");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBackground(Color.white);
		CellConstraints cc = new CellConstraints();
		
		builder.addLabel(manager.getStatusMessage(), cc.xy(2, 2));
		
		builder.addSeparator("", cc.xyw(2, 5, 4));
		
		return builder.getPanel();
	}

	private JPanel progressPanel() {
		FormLayout layout = new FormLayout("5dlu,pref,5dlu,pref,fill:pref:grow","10dlu,pref,5dlu,pref,15dlu");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBackground(Color.white);
		CellConstraints cc = new CellConstraints();
		
		builder.addLabel(manager.getStatusMessage(), cc.xy(2, 2));
		
		builder.addSeparator("", cc.xyw(2, 5, 4));
		
		return builder.getPanel();
	}
	
}
