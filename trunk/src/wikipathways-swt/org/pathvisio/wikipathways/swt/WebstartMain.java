package org.pathvisio.wikipathways.swt;

import java.net.MalformedURLException;
import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swt.GuiMain;
import org.pathvisio.gui.swt.MainWindow;
import org.pathvisio.gui.swt.SwtEngine;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.wikipathways.Parameter;
import org.pathvisio.wikipathways.UserInterfaceHandler;
import org.pathvisio.wikipathways.WikiPathways;

public class WebstartMain {
	String[] args;
	MainWindowWikipathways window;
	WikiPathways wiki;
	UserInterfaceHandler uiHandler;
	
	public void createAndShowGui() {
		SwtEngine.setCurrent(new SwtEngine());
		Engine.setCurrent(new Engine());
		
		wiki = new WikiPathways(uiHandler);
		window = new MainWindowWikipathways(wiki);
		SwtEngine.getCurrent().setWindow(window);
		
		Thread t = new Thread() {
			public void run() {
				while(window.getShell() == null) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				initWiki();
			}
		};
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		
		window.setReadOnly(wiki.isReadOnly());
		window.setBlockOnOpen(true);
		addSaveButton(window);
		window.open();
		Display.getDefault().dispose();
	}
	
	private void initWiki() {
		uiHandler = new SwtUserInterfaceHandler(window.getShell());
		wiki.setUiHandler(uiHandler);
		final RunnableWithProgress r = new RunnableWithProgress() {
			public Object excecuteCode() {				
				parseCommandLine(args);
								
				try {
					wiki.init(SwtEngine.getCurrent().createWrapper(), 
							getProgressKeeper(), getDocumentBase());
				} catch(Exception e) {
					Logger.log.error("Error while starting editor", e);
					uiHandler.showError("Error while initializing editor", e.getClass() + ": " + e.getMessage());
				};
				return null;
			}
		};
		uiHandler.runWithProgress(r, "Starting editor", ProgressKeeper.PROGRESS_UNKNOWN, false, true);
	}
		
	void addSaveButton(MainWindow w) {
		ToolBarContributionItem tc = (ToolBarContributionItem)w.getCoolBarManager().find("CommonActions");
		
		tc.getToolBarManager().add(new ControlContribution("SaveToWiki") {
			protected Control createControl(Composite parent) {
				final Button b = new Button(parent, SWT.PUSH);
				//b.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
				b.setText("Save to " + Globals.SERVER_NAME);
				b.setToolTipText("Save current pathway as '" + wiki.getPwName() + "' on " + Globals.SERVER_NAME);
				b.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						wiki.saveUI();
					}
				});
				return b;
			}
		});
	}
	
	void parseCommandLine(String[] args) {
		for(int i = 0; i < args.length - 1; i++) {
			//Check for parameters
			String a = args[i];
			if(a.startsWith("-")) {
				if	(a.equalsIgnoreCase("-pwName")) {
					Logger.log.trace("Parsed -pwName argument" + args[i+1]);
					Parameter.PW_NAME.setValue(args[i+1]);
				}
				else if	(a.equalsIgnoreCase("-pwUrl")) {
					Logger.log.trace("Parsed -pwUrl argument" + args[i+1]);
					Parameter.PW_URL.setValue(args[i+1]);
				}
				else if	(a.equalsIgnoreCase("-rpcUrl")) {
					Logger.log.trace("Parsed -rpcUrl argument" + args[i+1]);
					Parameter.RPC_URL.setValue(args[i+1]);
				}
				else if (a.equalsIgnoreCase("-pwSpecies")) {
					Logger.log.trace("Parsed -pwSpecies argument" + args[i+1]);
					Parameter.PW_SPECIES.setValue(args[i+1]);
				}
				else if (a.equalsIgnoreCase("-user")) {
					Logger.log.trace("Parsed -user argument" + args[i+1]);
					Parameter.USER.setValue(args[i+1]);
				}
				else if (a.equalsIgnoreCase("-new")) {
					Logger.log.trace("Parsed -new flag");
					String value = args[i+1];
					if(value.equalsIgnoreCase("true") || value.equals("1")) {
						Parameter.PW_NEW.setValue(Boolean.toString(true));
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		GuiMain.initiate();
		WebstartMain main = new WebstartMain();
		main.args = args;
		main.createAndShowGui();
	}
	
	private URL getDocumentBase() {
		try {
			BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
			return bs.getCodeBase();
		} catch (UnavailableServiceException e) {
			Logger.log.error("Unable to get javax.jnlp.BasicService, are you not using webstart?");
		}
		try {
			return new URL("http://www.wikipathways.org");
		} catch(MalformedURLException ue) {
			Logger.log.error("Unable to create URL");
		}
		return null;
	}
}
