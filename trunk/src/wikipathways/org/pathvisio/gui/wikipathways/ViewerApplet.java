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
package org.pathvisio.gui.wikipathways;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.gui.swing.MainPanel;

public class ViewerApplet extends PathwayPageApplet {
	private MainPanel mainPanel;

	public static final String PAR_PATHWAY_URL = "pathway.url";

	protected void createToolbar() {
		// Don't create toolbar, already in mainpanel
	}

	protected void doInitWiki(ProgressKeeper pk, URL base) throws Exception {
		Logger.log.trace("AppletMain:doInitWiki");
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				mainPanel = wiki.prepareMainPanel();
			}
		});

		wiki.getEngine().setWrapper(wiki.getSwingEngine().createWrapper());
		Logger.log.trace("calling:doInitWiki");
		super.doInitWiki(pk, base);
		wiki.getPathwayView().setEditMode(false);
	}

	public void createGui() {
		mainPanel = wiki.getMainPanel();

		//Add a save to wiki button
		Action saveAction = new Actions.SaveToServerAction(uiHandler, wiki, null);
		JButton saveButton = new JButton(saveAction);
		saveButton.setText("");
		mainPanel.getToolBar().add(saveButton, 2);

		//Create a maximize button
		JButton btn = new JButton(new Actions.FullScreenAction(uiHandler, wiki, this));
		btn.setText("");
		mainPanel.getToolBar().add(btn,  mainPanel.getToolBar().getComponentCount() - 2);
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		setVisible(true);
		validate(); //We need to validate before calling setDividerLocation
		int spPercent = PreferenceManager.getCurrent().getInt(GlobalPreference.GUI_SIDEPANEL_SIZE);
		mainPanel.getSplitPane().setDividerLocation( (100 - spPercent) / 100.0 );
	}

	public void destroy() {
		getContentPane().remove(mainPanel);
		super.destroy();
	}
}
