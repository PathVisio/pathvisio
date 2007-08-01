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

import java.awt.Component;
import java.net.CookieHandler;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.GuiInit;
import org.pathvisio.gui.swing.MainPanel;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.wikipathways.Parameter;
import org.pathvisio.wikipathways.UserInterfaceHandler;
import org.pathvisio.wikipathways.WikiPathways;

public class AppletMain extends JApplet {	
	private static final long serialVersionUID = 1L;

	private WikiPathways wiki;
	private MainPanel mainPanel;
	private UserInterfaceHandler uiHandler;
	
	public static final String PAR_PATHWAY_URL = "pathway.url";
	public void init() {
		for(Component c : getContentPane().getComponents()) {
			System.out.println("COMPONENT: " + c);
		}
		SwingEngine.setCurrent(new SwingEngine());
		Engine.setCurrent(new Engine());
		
		System.out.println("Engines: " + Engine.getCurrent().hashCode() + " | " + SwingEngine.getCurrent().hashCode());
		System.out.println("INIT CALLED....");
		Logger.log.trace("INIT CALLED....");
				
		uiHandler = new SwingUserInterfaceHandler(JOptionPane.getFrameForComponent(this));
		
//		try {
//			SwingUtilities.invokeAndWait(new Runnable() {
//				public void run() {
					mainPanel = SwingEngine.getCurrent().getApplicationPanel();
					System.out.println("MainPanel: " + mainPanel.hashCode());
					Action saveAction = new Actions.ExitAction(this, wiki, true);
					Action discardAction = new Actions.ExitAction(this, wiki, false);
					
					mainPanel.getToolBar().addSeparator();
					mainPanel.addToToolbar(saveAction, MainPanel.TB_GROUP_HIDE_ON_EDIT);
					mainPanel.addToToolbar(discardAction);

					getContentPane().add(mainPanel);
					mainPanel.setVisible(true);
//				}
//			});
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		System.out.println("INIT ENDED....");
		Logger.log.trace("INIT ENDED....");
	}
		
	public void start() {
		System.out.println("START CALLED....");
		Logger.log.trace("START CALLED....");
		
		final RunnableWithProgress r = new RunnableWithProgress() {
			public Object excecuteCode() {				
				GuiInit.init();
												
				wiki = new WikiPathways(uiHandler);
				parseArguments();
				loadCookies();
								
				try {
					wiki.init(SwingEngine.getCurrent().createWrapper());
				} catch(Exception e) {
					Logger.log.error("Unable to load pathway", e);
					JOptionPane.showMessageDialog(
							AppletMain.this, e.getClass() + ": " + e.getMessage(), "Error while initializing editor", JOptionPane.ERROR_MESSAGE);
				};
				System.out.println("VPathway: " + Engine.getCurrent().getActiveVPathway());
				System.out.println(Engine.getCurrent() + " | " + SwingEngine.getCurrent());
				return null;
			}
		};
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					int spPercent = GlobalPreference.getValueInt(GlobalPreference.GUI_SIDEPANEL_SIZE);
					double spSize = (100 - spPercent) / 100.0;
					System.out.println(spSize);
					mainPanel.getSplitPane().setDividerLocation(spSize);
					
					uiHandler.runWithProgress(r, "Loading pathway", ProgressKeeper.PROGRESS_UNKNOWN, false, true);
				}
			});
		} catch (Exception e) {
			Logger.log.error("Unable to start applet", e);
		}
				
		System.out.println("START ENDED....");
		Logger.log.trace("START ENDED....");
	}
	
	public void stop() {
		System.out.println("STOP CALLED....");
		Logger.log.trace("STOP CALLED....");
						
		System.out.println("STOP ENDED....");
		Logger.log.trace("STOP ENDED....");
	}

	public void destroy() {
		System.out.println("DESTROY CALLED....");
		Logger.log.trace("DESTROY CALLED....");
		ApplicationEvent e = new ApplicationEvent(this, ApplicationEvent.APPLICATION_CLOSE);
		Engine.getCurrent().fireApplicationEvent(e);
		if(e.doit) {
			super.destroy();
		}
		System.out.println("DESTROY ENDED....");
		Logger.log.trace("DESTROY ENDED....");
	}
	
	public void endWithMessage(String msg) {
		JLabel label = new JLabel(msg, JLabel.CENTER);
		getContentPane().add(label);
		getContentPane().validate();
		
		getAppletContext().showDocument(getDocumentBase(), "_parent");
	}
	
	void loadCookies() {
		Logger.log.trace("Loading cookies");

		//wikipathwaysUserName=Thomas; wikipathwaysUserID=2; wikipathwaysToken=d8fa40c604ac290a5e2f65830279f518; wikipathways_session=6e153458660cf2cc888d37ec0e6f164b
		
		try {
			CookieHandler handler = CookieHandler.getDefault();
			if (handler != null)    {
				URL url = getDocumentBase();
				Map<String, List<String>> headers = handler.get(url.toURI(), new HashMap<String, List<String>>());
				if(headers == null) {
					Logger.log.error("Unable to load cookies: headers null");
					return;
				}
				List<String> values = headers.get("Cookie");
				for (String c : values) {
					String[] cvalues = c.split(";");
					for(String cv : cvalues) {
						String[] keyvalue = cv.split("=");
						if(keyvalue.length == 2) {
							Logger.log.trace("COOKIE: " + keyvalue[0] + " | " + keyvalue[1]);
							wiki.addCookie(keyvalue[0].trim(), keyvalue[1].trim());
						}
					}
				}
			}
		} catch(Exception e) {
			Logger.log.error("Unable to load cookies", e);
		}
//			JSObject myBrowser = (JSObject) JSObject.getWindow(this);
//	        JSObject myDocument =  (JSObject) myBrowser.getMember("document");
//	        String cookie = (String)myDocument.getMember("cookie");
//	        String[] cstr = cookie.split(";");
//	        for(String c : cstr) {
//	        	String[] vstr = c.split("=");
//	        	if(vstr.length == 2) {
//	        		wiki.addCookie(vstr[0].trim(), vstr[1].trim());
//	        	}
//	        }
	}
	
	void parseArguments() {
		for(Parameter p : Parameter.values()) {
			p.setValue(getParameter(p.getName()));
		}
	}
}
