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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.FileDialog;
import org.pathvisio.Globals;
import org.pathvisio.biopax.gui.BiopaxDialog;
import org.pathvisio.gui.swt.Engine.ApplicationEvent;
import org.pathvisio.gui.swt.Engine.ApplicationEventListener;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayExporter;
import org.pathvisio.preferences.swt.PreferenceDlg;
import org.pathvisio.preferences.swt.Preferences;
import org.pathvisio.view.VPathway;

/**
   This class contains a large number of JFace Actions that are both in V1 and V2.

   These actions just bind keyboard shortcuts, menu items and toolbar
   icons to actions elsewhere in the program. Therefore these should
   be a light as possible, delegating functionality to appropriate
   other classes such as
   - Engine (for open / save / new / import / export)
   - PreferenceManager (for preferences)
   - Engine.getVPathway() (for undo, redo, copy, paste).
*/   
public class CommonActions 
{
	static class UndoAction extends Action
	{
		MainWindowBase window;
		public UndoAction (MainWindowBase w)
		{
			window = w;
			setText ("&Undo@Ctrl+Z");
			setToolTipText ("Undo last action");
		}
		public void run() 
		{
			if (Engine.getPathway() != null)
			{
				Engine.getPathway().undo();
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
					Engine.getResourceURL("icons/new.gif")));
		}
		public void run ()
		{			
			Engine.newPathway();
		}	
	}
	
	/**
	 * {@link Action} to create a new gpml pathway
	 */
	static class SvgExportAction extends Action 
	{
		MainWindowBase window;
		public SvgExportAction (MainWindowBase w)
		{
			window = w;
			setText ("Export to SVG");
			setToolTipText ("Export to Scalable Vector Graphics (SVG) " +
					"for publication-quality images");
		}
		public void run () 
		{
			//TODO: move to engine, merge with "save"
			VPathway drawing = Engine.getVPathway();
			Pathway gmmlData = Engine.getPathway();
			// Check if a gpml pathway is loaded
			if (drawing != null)
			{
				FileDialog fd = new FileDialog(window.getShell(), SWT.SAVE);
				fd.setText("Save");
				fd.setFilterExtensions(new String[] {"*." + Engine.SVG_FILE_EXTENSION, "*.*"});
				fd.setFilterNames(new String[] {Engine.SVG_FILTER_NAME, "All files (*.*)"});
				
				File xmlFile = gmmlData.getSourceFile();
				if(xmlFile != null) {
					String name = xmlFile.getName();
					if (name.endsWith("." + Engine.PATHWAY_FILE_EXTENSION))
					{
						name = name.substring(0, name.length() - 
							Engine.PATHWAY_FILE_EXTENSION.length()) +
							Engine.SVG_FILE_EXTENSION;
					}
					fd.setFileName(name);
					fd.setFilterPath(xmlFile.getPath());
				} else {
					fd.setFileName(Engine.getPreferences().getString(Preferences.PREF_DIR_PWFILES));
				}
				String fileName = fd.open();
				// Only proceed if user selected a file
				
				if(fileName == null) return;
				
				// Append .svg extension if not already present
				if(!fileName.endsWith("." + Engine.SVG_FILE_EXTENSION)) 
					fileName += "." + Engine.SVG_FILE_EXTENSION;
				
				File checkFile = new File(fileName);
				boolean confirmed = true;
				// If file exists, ask overwrite permission
				if(checkFile.exists())
				{
					confirmed = MessageDialog.openQuestion(window.getShell(),"",
					"File already exists, overwrite?");
				}
				if(confirmed)
				{
					try
					{
						gmmlData.writeToSvg(checkFile);
					}
					catch (ConverterException e)
					{
						String msg = "While writing svg to " 
							+ checkFile.getAbsolutePath();					
						MessageDialog.openError (window.getShell(), "Error", 
								"Error: " + msg + "\n\n" + 
								"See the error log for details.");
						Engine.log.error(msg, e);
					}
				}
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
					"No pathway to save! Open or create a new pathway first");
			}			
		}
	}

	/**
	 * {@link Action} to open an gpml pathway
	 */
	static class OpenAction extends Action 
	{
		MainWindowBase window;
		public OpenAction (MainWindowBase w)
		{
			window = w;
			setText ("&Open pathway@Ctrl+O");
			setToolTipText ("Open pathway");
			setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/open.gif")));
		}
		public void run () 
		{
			FileDialog fd = new FileDialog(window.getShell(), SWT.OPEN);
			fd.setText("Open");
			String pwpath = Engine.getPreferences().getString(Preferences.PREF_DIR_PWFILES);
			fd.setFilterPath(pwpath);
			fd.setFilterExtensions(new String[] {"*." + Engine.PATHWAY_FILE_EXTENSION, "*.*"});
			fd.setFilterNames(new String[] {Engine.PATHWAY_FILTER_NAME, "All files (*.*)"});
	        String fnMapp = fd.open();
	        // Only open pathway if user selected a file
	        
	        if(fnMapp != null) { 
	        	Engine.openPathway(fnMapp);
	        }
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
			if (Engine.canDiscardPathway())
			{
				FileDialog fd = new FileDialog(window.getShell(), SWT.OPEN);
				fd.setText("Import");
				fd.setFilterPath(Engine.getPreferences().getString(Preferences.PREF_DIR_PWFILES));
				fd.setFilterExtensions(new String[] {"*." + Engine.GENMAPP_FILE_EXTENSION, "*.*"});
				fd.setFilterNames(new String[] {Engine.GENMAPP_FILTER_NAME, "All files (*.*)"});
				String fnMapp = fd.open();
				// Only open pathway if user selected a file
				
				if(fnMapp != null) { 
					Engine.openPathway(fnMapp); 
				}
			}
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
			Engine.savePathwayAs();
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
			//TODO: move to engine, merge with "save" or "saveAs"
			VPathway drawing = Engine.getVPathway();
			Pathway gmmlData = Engine.getPathway();
			// Check if a gpml pathway is loaded
			if (drawing != null)
			{
				FileDialog fd = new FileDialog(window.getShell(), SWT.SAVE);
				fd.setText("Export");
				
				class FileType implements Comparable<FileType> {
					final String name;
					final String ext;
					public FileType(String n, String e) { name = n; ext = e; }
					public int compareTo(FileType o) {
						return name.compareTo(o.name);
					}
				}
				
				ArrayList<FileType> fts = new ArrayList<FileType>();
				HashMap<String, PathwayExporter> exporters = Engine.getGpmlExporters();
								
				for(String ext : exporters.keySet()) {
					fts.add(new FileType(
								exporters.get(ext).getName() + " (*." + ext + ")",
								"*." + ext));
				}
				Collections.sort(fts);
				String[] exts = new String[fts.size()];
				String[] nms = new String[fts.size()];
				for(int i = 0; i < fts.size(); i++) {
					FileType ft = fts.get(i);
					exts[i] = ft.ext;
					nms[i] = ft.name;
				}
				fd.setFilterExtensions(exts);
				fd.setFilterNames(nms);
								
				File xmlFile = gmmlData.getSourceFile();
				if(xmlFile != null) {
					String name = xmlFile.getName();
					if (name.endsWith("." + Engine.PATHWAY_FILE_EXTENSION))
					{
						name = name.substring(0, name.length() - 
							Engine.PATHWAY_FILE_EXTENSION.length() - 1);
					}
					fd.setFileName(name);
					fd.setFilterPath(xmlFile.getPath());
				} else {
					fd.setFileName(Engine.getPreferences().getString(Preferences.PREF_DIR_PWFILES));
				}
				String fileName = fd.open();
				// Only proceed if user selected a file
				if(fileName == null) return;
				
				int dot = fileName.lastIndexOf('.');
				String ext = Engine.GENMAPP_FILE_EXTENSION;
				if(dot >= 0) {
					ext = fileName.substring(dot + 1, fileName.length());
				}
				PathwayExporter exporter = Engine.getGpmlExporter(ext);
				
				if(exporter == null) 
					MessageDialog.openError (window.getShell(), "Error", 
					"No exporter for '" + ext +  "' files");
								
				File checkFile = new File(fileName);
				boolean confirmed = true;
				// If file exists, ask overwrite permission
				if(checkFile.exists())
				{
					confirmed = MessageDialog.openQuestion(window.getShell(),"",
					"File already exists, overwrite?");
				}
				if(confirmed)
				{
					try
					{
						//gmmlData.writeToMapp(checkFile);
						exporter.doExport(checkFile, gmmlData);
					}
					catch (ConverterException e)
					{
						String msg = "While exporting to " 
							+ checkFile.getAbsolutePath();					
						MessageDialog.openError (window.getShell(), "Error", 
								"Error: " + msg + "\n\n" + 
								"See the error log for details.");
						Engine.log.error(msg, e);
					}
				}
			}
			else
			{
				MessageDialog.openError (window.getShell(), "Error", 
					"No pathway to save! Open or create a new pathway first");
			}			
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
			if (Engine.canDiscardPathway())
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
			PreferenceManager pg = new PreferenceDlg();
			PreferenceDialog pd = new PreferenceDialog(window.getShell(), pg);
			pd.setPreferenceStore(Engine.getPreferences());
			pd.open();
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
			if(pctZoomFactor == MainWindowBase.ZOOM_TO_FIT) 
			{
				setText ("Zoom to fit");
				setToolTipText("Zoom mapp to fit window");
			}
			else
			{
				setText (pctZoomFactor + " %");
				setToolTipText ("Zoom mapp to " + pctZoomFactor + " %");
			}
		}
		public void run () {
			VPathway drawing = Engine.getVPathway();
			if (drawing != null)
			{
				double newPctZoomFactor = pctZoomFactor;
				if(pctZoomFactor == MainWindowBase.ZOOM_TO_FIT) 
				{
					Point shellSize = window.sc.getSize();
					Point drawingSize = drawing.getSize();
					newPctZoomFactor = (int)Math.min(
							drawing.getPctZoom() * (double)shellSize.x / drawingSize.x,
							drawing.getPctZoom() * (double)shellSize.y / drawingSize.y
					);
				} 
				drawing.setPctZoom(newPctZoomFactor);
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
			setToolTipText ("About " + Globals.APPLICATION_VERSION_NAME);
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
			setToolTipText ("Opens " + Globals.APPLICATION_VERSION_NAME + " help in your web browser");
		}
		public void run ()
		{
			Engine.openWebPage(Globals.HELP_URL, "Opening help page in broswer",
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
			setText ("Copy@Ctrl+C");
			setToolTipText ("Copy selected objects to clipboard");
		}
		public void run()
		{
			Engine.getVPathway().copyToClipboard();
		}
	}

	static class PasteAction extends Action
	{
		MainWindowBase window;
		public PasteAction (MainWindowBase w)
		{
			window = w;
			setText ("Paste@Ctrl+V");
			setToolTipText ("Paste contents of clipboard");
		}
		public void run()
		{
			Engine.getVPathway().pasteFromClipboad();
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
			setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/save.gif")));
		}
		
		public void run ()
		{
			Engine.savePathway();
		}
	}
	
	static class BiopaxAction extends Action 
	{
		MainWindowBase window;
		public BiopaxAction (MainWindowBase w)
		{
			window = w;
			setText ("Edit &BioPAX code");
			setToolTipText ("Edit BioPAX code");
		}
		
		public void run ()
		{
			BiopaxDialog d = new BiopaxDialog(window.getShell());
			d.setPathway(Engine.getPathway());
			d.open();
		}
	}

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
			setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/edit.gif")));
			setToolTipText(ttUnChecked);
			window = w;
			
			Engine.addApplicationEventListener(this);
		}
		
		public void run ()
		{
			if(Engine.isDrawingOpen())
			{
				VPathway drawing = Engine.getVPathway();
				Pathway pathway = Engine.getPathway();
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
					//Switch to edit mode: show edit toolbar, show property table in sidebar
					drawing.setEditMode(true);
					window.showEditActionsCI(true);
					window.showAlignActionsCI(true);
					window.rightPanel.getTabFolder().setSelection(1);
				}
				else
				{
					//Switch to view mode: hide edit toolbar, show backpage browser in sidebar
					drawing.setEditMode(false);
					window.showEditActionsCI(false);
					window.showAlignActionsCI(false);
					window.rightPanel.getTabFolder().setSelection(0);
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
			if(e.type == ApplicationEvent.OPEN_PATHWAY) {
				Engine.getVPathway().setEditMode(isChecked());
			}
			else if(e.type == ApplicationEvent.NEW_PATHWAY) {
				switchEditMode(true);
			}
		}
	}

}

