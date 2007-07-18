// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

import javax.swing.JApplet;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.gui.swing.GuiInit;
import org.pathvisio.gui.swing.MainPanel;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.wikipathways.WikiPathways;

public class AppletMain extends JApplet {
	private static WikiPathways wiki;
	
	public static final String PAR_PATHWAY_URL = "pathway.url";
	public void init() {
		super.init();
		
		GuiInit.init();
	
		MainPanel mainPanel = SwingEngine.getApplicationPanel();		
		parseArguments();
		
		try { 
			wiki.openPathwayURL();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		add(mainPanel);
	}
	
	public void start() {
		// TODO Auto-generated method stub
		super.start();
	}
	
	public void stop() {
		ApplicationEvent e = new ApplicationEvent(this, ApplicationEvent.APPLICATION_CLOSE);
		Engine.fireApplicationEvent(e);
		if(e.doit) {
			super.stop();
		}
	}
	
	void parseArguments() {
		String pwURL = getParameter("pathwayUrl");
		String pwName = getParameter("pwName");
		String rpcUrl = getParameter("rpcUrl");
		String pwSpecies = getParameter("pwSpecies");
		String user = getParameter("user");
		boolean pwNew = Boolean.parseBoolean(getParameter("new"));
		wiki = new WikiPathways();
		wiki.setUser(user);
		wiki.setPwName(pwName);
		wiki.setPwSpecies(pwSpecies);
		wiki.setPwURL(pwURL);
		wiki.setRpcURL(rpcUrl);
	}
}
