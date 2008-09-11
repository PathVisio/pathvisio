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
package org.pathvisio.gui.swt;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.model.Pathway;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.preferences.swt.PreferenceDlg;
import org.pathvisio.preferences.swt.SwtPreferences;
import org.pathvisio.util.Resources;
import org.pathvisio.view.UndoManager;
import org.pathvisio.view.UndoManagerEvent;
import org.pathvisio.view.UndoManagerListener;
import org.pathvisio.view.VPathway;

/**
   This class contains a large number of JFace Actions that are both in V1 and V2.
*/   
public class CommonActions
{
	static class UndoAction extends Action implements ApplicationEventListener, UndoManagerListener
	{
		MainWindowBase window;
		public UndoAction (MainWindowBase w)
		{
			window = w;
			setText ("&Undo@Ctrl+Z");
			setToolTipText ("Undo last action");
			setImageDescriptor(ImageDescriptor.createFromURL(
					Resources.getResourceURL("undo.gif")));
			setEnabled(false);
			Engine.getCurrent().addApplicationEventListener(this);
		}
		public void run() 
		{
			if (Engine.getCurrent().getActiveVPathway() != null)
			{
				Engine.getCurrent().getActiveVPathway().undo();
			}
		}
		
		public void undoManagerEvent(UndoManagerEvent e) {
			String msg = e.getMessage();
			setToolTipText("Undo: " + msg);
			setEnabled(!msg.equals(UndoManager.CANT_UNDO));
		}

		public void applicationEvent(ApplicationEvent e) {
			if(e.getType() == ApplicationEvent.VPATHWAY_CREATED) {
				((VPathway)e.getSource()).getUndoManager().addListener(this);
				setEnabled(false);
			}
		}
	}
	
	/**
	 * {@link Action} to create a new gpml pathway
	 */
	static class NewAction extends Action 
	{
		MainWindowBase window;
		public NewAction (MainWindowBase w)
		{
			window = w;
			setText ("&New pathway@Ctrl+N");
			setToolTipText ("Create new pathway");
			setImageDescriptor(ImageDescriptor.createFromURL(
					Resources.getResourceURL("new.gif")));
		}
		public void run ()
		{			
			SwtEngine.getCurrent().newPathway();
			Engine.getCurrent().getActiveVPathway().setEditMode(true);
		}	
	}
	
	/**
	 * {@link Action} to open an gpml pathway
	 */
	static class OpenAction extends Action 
	{
		MainWindowBase window;
		boolean editOnOpen;
		
		public OpenAction (MainWindowBase w)
		{
			window = w;
			setText ("&Open pathway@Ctrl+O");
			setToolTipText ("Open pathway");
			setImageDescriptor(ImageDescriptor.createFromURL(Resources.getResourceURL("open.gif")));
		}
		public void run () 
		{
			SwtEngine.getCurrent().openPathway();
		}
	}

	/**
	 * {@link Action} to open an gpml pathway
	 */
	static class ImportAction extends Action 
	{
		MainWindowBase window;
		public ImportAction (MainWindowBase w)
		{
			window = w;
			setText ("&Import");
			setToolTipText ("Import Pathway in GenMAPP format");
		}
		public void run () 
		{
			SwtEngine.getCurrent().importPathway();
		}
	}

	/**
	 * {@link Action} to save a gpml pathway to a file specified by the user
	 */
	static class SaveAsAction extends Action 
	{
		MainWindowBase window;
		public SaveAsAction (MainWindowBase w)
		{
			window = w;
			setText ("Save pathway &As");
			setToolTipText ("Save pathway with new file name");
		}
		
		public void run () 
		{
			SwtEngine.getCurrent().savePathwayAs();
		}
	}

	
	/**
	 * {@link Action} to save a gpml pathway to a file specified by the user
	 */
	static class ExportAction extends Action 
	{
		MainWindowBase window;
		public ExportAction (MainWindowBase w)
		{
			window = w;
			setText ("&Export");
			setToolTipText ("Export Pathway to GenMAPP format");
		}
		public void run () {
			SwtEngine.getCurrent().exportPathway();
		}
	}

	/**
	 * {@link Action} to exit the application
	 */
	static class ExitAction extends Action 
	{
		MainWindowBase window;
		public ExitAction (MainWindowBase w)
		{
			window = w;
			setText ("E&xit@Ctrl+X");
			setToolTipText ("Exit Application");
		}
		public void run () 
		{
			if (SwtEngine.getCurrent().canDiscardPathway())
			{
				window.close();
			}
		}
	}
	
	static class PreferencesAction extends Action
	{
		MainWindowBase window;
		public PreferencesAction (MainWindowBase w)
		{
			window = w;
			setText("&Preferences");
			setToolTipText("Edit preferences");
		}
		public void run () {
			org.eclipse.jface.preference.PreferenceManager pg = new PreferenceDlg();
			PreferenceDialog pd = new PreferenceDialog(window.getShell(), pg);
			pd.setPreferenceStore(new SwtPreferences(PreferenceManager.getCurrent()));
			pd.open();
			PreferenceManager.getCurrent().store();
		}
	}

	/**
	 * {@link Action} that zooms a mapp to the specified zoomfactor
	 */
	static class ZoomAction extends Action 
	{
		MainWindowBase window;
		int pctZoomFactor;
		
		/**
		 * Constructor for this class
		 * @param w {@link MainWindowBase} window this action belongs to
		 * @param newPctZoomFactor the zoom factor as percentage of original
		 */
		public ZoomAction (MainWindowBase w, int newPctZoomFactor)
		{
			window = w;
			pctZoomFactor = newPctZoomFactor;
			setText (pctZoomFactor + " %");
			setToolTipText ("Zoom mapp to " + pctZoomFactor + " %");
		}

		public void run () 
		{
			VPathway drawing = Engine.getCurrent().getActiveVPathway();
			if (drawing != null)
			{
				drawing.setPctZoom(pctZoomFactor);
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
					"No gpml file loaded! Open or create a new gpml file first");
			}
		}
	}

	/**
	 * {@link Action} that zooms a mapp to the specified zoomfactor
	 */
	static class ZoomToFitAction extends Action 
	{
		MainWindowBase window;
		
		/**
		 * Constructor for this class
		 * @param w {@link MainWindowBase} window this action belongs to
		 * @param newPctZoomFactor the zoom factor as percentage of original
		 */
		public ZoomToFitAction (MainWindowBase w)
		{
			window = w;
			setText ("Zoom to fit");
			setToolTipText("Zoom pathway to fit window");
		}
		
		public void run () 
		{
			VPathway drawing = Engine.getCurrent().getActiveVPathway();
			if (drawing != null)
			{
				double pctZoomFactor = drawing.getFitZoomFactor();
				drawing.setPctZoom(pctZoomFactor);
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
					"No gpml file loaded! Open or create a new gpml file first");
			}
		}
	}

	/**
	 * {@link Action} to open a {@link AboutDlg} window
	 */
	static class AboutAction extends Action 
	{
		MainWindowBase window;
		public AboutAction (MainWindowBase w)
		{
			window = w;
			setText ("&About");
			setToolTipText ("About " + Engine.getCurrent().getApplicationName());
		}
		public void run () {
			AboutDlg gmmlAboutBox = new AboutDlg(window.getShell(), SWT.NONE);
			gmmlAboutBox.open();
		}
	}
	
	/**
	 * {@link Action} to open a Help window
	 */
	static class HelpAction extends Action 
	{
		MainWindowBase window;
		public HelpAction (MainWindowBase w)
		{
			window = w;
			setText ("&Help@F1");
			setToolTipText ("Opens " + Engine.getCurrent().getApplicationName()+ " help in your web browser");
		}
		public void run ()
		{
			SwtEngine.getCurrent().openWebPage(Globals.HELP_URL, "Opening help page...",
						"Unable to open web browser" +
						"\nYou can open the help page manually:\n" +
						Globals.HELP_URL);
		}
	}

	static class CopyAction extends Action
	{
		MainWindowBase window;
		public CopyAction (MainWindowBase w)
		{
			window = w;
			setText ("Copy"); //No keybinding, this will conflict with other widgets
			setToolTipText ("Copy selected objects to clipboard");
		}
		public void run()
		{
			Engine.getCurrent().getActiveVPathway().copyToClipboard();
		}
	}

	static class PasteAction extends Action
	{
		MainWindowBase window;
		public PasteAction (MainWindowBase w)
		{
			window = w;
			setText ("Paste"); //No keybinding, this will conflict with other widgets
			setToolTipText ("Paste contents of clipboard");
		}
		public void run()
		{
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp.isEditMode()) {
				Engine.getCurrent().getActiveVPathway().pasteFromClipboard();
			} else {
				MessageDialog.openError(window.getShell(), 
						"Unable to paste", "You can't paste in read-only mode");
			}
		}
	}
	
	/**
	 * {@link Action} to save a gpml pathway
	 */
	static class SaveAction extends Action 
	{
		MainWindowBase window;
		public SaveAction (MainWindowBase w)
		{
			window = w;
			setText ("&Save pathway@Ctrl+S");
			setToolTipText ("Save pathway");
			setImageDescriptor(ImageDescriptor.createFromURL(Resources.getResourceURL("save.gif")));
		}
		
		public void run ()
		{
			SwtEngine.getCurrent().savePathway();
		}
	}
	
//	static class BiopaxAction extends Action 
//	{
//		MainWindowBase window;
//		public BiopaxAction (MainWindowBase w)
//		{
//			window = w;
//			setText ("Edit &BioPAX code");
//			setToolTipText ("Edit BioPAX code");
//		}
//		
//		public void run () {
//			BiopaxDialog d = new BiopaxDialog(window.getShell());
//			d.setPathway(Engine.getCurrent().getActivePathway());
//			d.open();
//		}
//	}
	
	/**
	 * {@link Action} to switch between edit and view mode
	 */
	static class SwitchEditModeAction extends Action implements ApplicationEventListener
	{
		final String ttChecked = "Exit edit mode";
		final String ttUnChecked = "Switch to edit mode to edit the pathway content";
		MainWindowBase window;
		public SwitchEditModeAction (MainWindowBase w)
		{
			super("&Edit mode", IAction.AS_CHECK_BOX);
			setImageDescriptor(ImageDescriptor.createFromURL(Resources.getResourceURL("edit.gif")));
			setToolTipText(ttUnChecked);
			window = w;
			
			Engine.getCurrent().addApplicationEventListener(this);
		}

		public void run ()
		{
			if(Engine.getCurrent().hasVPathway())
			{
				VPathway drawing = Engine.getCurrent().getActiveVPathway();
				Pathway pathway = Engine.getCurrent().getActivePathway();
				if(isChecked())
				{
					// give a warning that this can't be edited.
					if (pathway.getSourceFile() != null && !pathway.getSourceFile().canWrite())
					{
						MessageDialog.openWarning(
								window.getShell(), "Read-only Warning",
								"You're trying to edit a Read-only file.\n" +
						"When you want to save your changes, you have to save to a different file.");
					}
					//Switch to edit mode:
					// this results in an event that causes UI to change automatically
					drawing.setEditMode(true);
				}
				else
				{
					//Switch to view mode: hide edit toolbar, show backpage browser in sidebar
					// this results in an event that causes UI to change automatically
					drawing.setEditMode(false);
				}
			}
			else //No gpml pathway loaded, deselect action and do nothing
			{
				setChecked(false);
			}
			window.getCoolBarManager().update(true);
		}
		
		public void setChecked(boolean check) {
			super.setChecked(check);
			setToolTipText(check ? ttChecked : ttUnChecked);
		}

		public void switchEditMode(boolean edit) {
			setChecked(edit);
			run();

		}

		public void applicationEvent(ApplicationEvent e) {
			if(e.getType() == ApplicationEvent.VPATHWAY_OPENED) {
				Engine.getCurrent().getActiveVPathway().setEditMode(isChecked());
			}
			else if(e.getType() == ApplicationEvent.VPATHWAY_NEW) {
				switchEditMode(true);
			}
		}
	}
}
