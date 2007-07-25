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

import java.awt.event.ActionEvent;
import java.net.CookieHandler;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.pathvisio.Engine;
import org.pathvisio.gui.swing.GuiInit;
import org.pathvisio.gui.swing.MainPanel;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.wikipathways.Parameter;
import org.pathvisio.wikipathways.UserInterfaceHandler;
import org.pathvisio.wikipathways.WikiPathways;

public class AppletMain extends JApplet {	
	private static final long serialVersionUID = 1L;

	private static WikiPathways wiki;

	public static final String PAR_PATHWAY_URL = "pathway.url";
	public void init() {
		Engine.log.trace("init applet");
		final UserInterfaceHandler uiHandler = new SwingUserInterfaceHandler(JOptionPane.getFrameForComponent(this));
		final MainPanel mainPanel = SwingEngine.getApplicationPanel();
		RunnableWithProgress r = new RunnableWithProgress() {
			public Object excecuteCode() {
				GuiInit.init();
								
				Action saveAction = new ExitAction(true);
				Action discardAction = new ExitAction(false);
				
				JToolBar tb = mainPanel.getToolBar();
					
				tb.addSeparator();		
				tb.add(saveAction);
				tb.add(discardAction);
				
				wiki = new WikiPathways(uiHandler);
				parseArguments();
				loadCookies();
								
				try { 
					wiki.init(SwingEngine.createWrapper());
				} catch(Exception e) {
					Engine.log.error("Unable to load pathway", e);
					JOptionPane.showMessageDialog(
							AppletMain.this, e.getClass() + ": " + e.getMessage(), "Error while initializing editor", JOptionPane.ERROR_MESSAGE);
				};
				return null;
			}
		};
		uiHandler.runWithProgress(r, "Loading pathway", ProgressKeeper.PROGRESS_UNKNOWN, false, false);
		getContentPane().add(mainPanel);
	}
	
	public void start() {
		Engine.log.trace("start applet");
		// TODO Auto-generated method stub
		super.start();
	}
	
	public void stop() {
		Engine.log.trace("stop applet");
		// TODO Auto-generated method stub
		super.stop();
	}

	public void destroy() {
		Engine.log.trace("destroy applet");
/*		ApplicationEvent e = new ApplicationEvent(this, ApplicationEvent.APPLICATION_CLOSE);
		Engine.fireApplicationEvent(e);
		if(e.doit) {
			super.destroy();
		}*/
	}
	
	void loadCookies() {
		System.out.println("Loading cookies");

		//wikipathwaysUserName=Thomas; wikipathwaysUserID=2; wikipathwaysToken=d8fa40c604ac290a5e2f65830279f518; wikipathways_session=6e153458660cf2cc888d37ec0e6f164b
		
		try {
			CookieHandler handler = CookieHandler.getDefault();
			if (handler != null)    {
				URL url = getDocumentBase();
				Map<String, List<String>> headers = handler.get(url.toURI(), new HashMap<String, List<String>>());
				List<String> values = headers.get("Cookie");
				for (Iterator<String> iter=values.iterator(); iter.hasNext();) {
					String c = iter.next();
					String[] cvalues = c.split(";");
					for(String cv : cvalues) {
						String[] keyvalue = cv.split("=");
						if(keyvalue.length == 2) {
							System.out.println("COOKIE: " + keyvalue[0] + " | " + keyvalue[1]);
							wiki.addCookie(keyvalue[0].trim(), keyvalue[1].trim());
						}
					}
				}
			}
		} catch(Exception e) {
			Engine.log.error("Unable to load cookies", e);
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
	
	class ExitAction extends AbstractAction {
		boolean doSave;
		public ExitAction(boolean save) {
			super("Finish", new ImageIcon(save ? Engine.getResourceURL("icons/apply.gif") : Engine.getResourceURL("icons/cancel.gif")));
			this.doSave = save;
			String descr = doSave ? "Save pathway and close editor" : "Discard pathway and close editor";
			putValue(Action.SHORT_DESCRIPTION, descr);
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("DEBUG: exit pressed, " + doSave);
			boolean saved = true;
			if(doSave) {
				saved = wiki.saveUI();
			}
			if(saved) getAppletContext().showDocument(getDocumentBase(), "_parent");
		}
	}
}
