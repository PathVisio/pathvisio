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
package org.pathvisio.gui.swing;

import java.awt.Component;
import java.io.File;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.actions.CommonActions;
import org.pathvisio.gui.swing.progress.ProgressDialog;
import org.pathvisio.gui.swing.progress.SwingProgressKeeper;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayExporter;
import org.pathvisio.model.PathwayImporter;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.view.VPathwayWrapper;
import org.pathvisio.view.swing.VPathwaySwing;

public class SwingEngine {	
	private MainPanel mainPanel;
	
	private static SwingEngine current;
	
	private CommonActions actions;
	
	public SwingEngine(Engine engine) {
		actions = new CommonActions(engine);
	}
	
	public static SwingEngine getCurrent() {
		if(current == null) current = new SwingEngine(Engine.getCurrent());
		return current;
	}
	
	public static void setCurrent(SwingEngine engine) {
		current = engine;
	}
	
	public CommonActions getActions() {
		return actions;
	}
	
	public MainPanel getApplicationPanel() {
		return getApplicationPanel(false);
	}
	
	public MainPanel getApplicationPanel(boolean forceNew) {
		if(forceNew || !hasApplicationPanel()) {
			mainPanel = new MainPanel();
		}
		return mainPanel;
	}
	
	public void setApplicationPanel(MainPanel mp) {
		mainPanel = mp;
	}
	
	public boolean hasApplicationPanel() {
		return mainPanel != null;
	}
	
	public static String MSG_UNABLE_IMPORT = "Unable to import GPML file.";
	public static String MSG_UNABLE_EXPORT = "Unable to export GPML file.";
	public static String MSG_UNABLE_SAVE = "Unable to save GPML file.";
	public static String MSG_UNABLE_OPEN = "Unable to open GPML file.";
	
	public void handleConverterException(String message, Component c, ConverterException e) {
		if (e.getMessage().contains("Cannot find the declaration of element 'Pathway'"))
		{
			JOptionPane.showMessageDialog(c,
					message + "\n\n" +
					"The most likely cause for this error is that you are trying to open an old Gpml file. " +
					"Please note that the Gpml format has changed as of March 2007. " +
					"The standard pathway set can be re-downloaded from http://pathvisio.org " +
					"Non-standard pathways need to be recreated or upgraded. " +
					"Please contact the authors at " + Globals.DEVELOPER_EMAIL + " if you need help with this.\n"
					, "Error", JOptionPane.ERROR_MESSAGE);
			Logger.log.error("Converter exception", e);
		}
		else
		{
			JOptionPane.showMessageDialog(c,
					message + "\nSee error log for details\n" + e.getClass(), "Error", JOptionPane.ERROR_MESSAGE);
			Logger.log.error("Converter exception", e);
		}
	}
		
	public VPathwayWrapper createWrapper() {
		 return new VPathwaySwing(getApplicationPanel().getScrollPane());
	}
	
	public boolean processTask(SwingProgressKeeper pk, ProgressDialog d, SwingWorker sw) {
		sw.execute();
		d.setVisible(true);
		try {
			return (Boolean)sw.get();
		} catch (Exception e) {
			Logger.log.error("Unable to perform task: " + pk.getTaskName(), e);
			return false;
		}
	}
	
	public boolean openPathway(final URL url) {		
		final SwingProgressKeeper pk = new SwingProgressKeeper(ProgressKeeper.PROGRESS_UNKNOWN);
		final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(getApplicationPanel()), 
				"", pk, false, true);
				
		SwingWorker sw = new SwingWorker() {
			protected Object doInBackground() throws Exception {
				pk.setTaskName("Opening pathway");
				try {
					Engine.getCurrent().setWrapper (createWrapper());
					Engine.getCurrent().openPathway(url);
					return true;
				} catch(ConverterException e) {
					handleConverterException(e.getMessage(), null, e);
					return false;
				} finally {
					pk.finished();
				}
			}
		};
		
		return processTask(pk, d, sw);
	}
	
	public boolean importPathway(final File f) {
		final SwingProgressKeeper pk = new SwingProgressKeeper(ProgressKeeper.PROGRESS_UNKNOWN);
		final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(getApplicationPanel()), 
				"", pk, false, true);
				
		SwingWorker sw = new SwingWorker() {
			protected Object doInBackground() throws Exception {
				pk.setTaskName("Importing pathway");
				try {
					Engine eng = Engine.getCurrent();
					boolean editMode = eng.hasVPathway() ? eng.getActiveVPathway().isEditMode() : true;
					eng.setWrapper (createWrapper());
					eng.importPathway(f);
					eng.getActiveVPathway().setEditMode(editMode);
					return true;
				} catch(ConverterException e) {
					handleConverterException(e.getMessage(), null, e);
					return false;
				} finally {
					pk.finished();
				}
			}
		};
		
	return processTask(pk, d, sw);

	}
	
	public void newPathway() {
		Engine.getCurrent().setWrapper (createWrapper());
		Engine.getCurrent().newPathway();
	}

	public boolean exportPathway() {
		//Open file dialog
		JFileChooser jfc = new JFileChooser();
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setDialogTitle("Export pathway");
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);

		for(final PathwayExporter exp : Engine.getCurrent().getPathwayExporters().values()) {
			jfc.addChoosableFileFilter(new ImporterExporterFileFilter(exp));
		}

		int status = jfc.showDialog(getApplicationPanel(), "Export");
		if(status == JFileChooser.APPROVE_OPTION) {	
			File f = jfc.getSelectedFile();
			ImporterExporterFileFilter ff = (ImporterExporterFileFilter)jfc.getFileFilter();
			if(!f.toString().toUpperCase().endsWith(ff.getDefaultExtension().toUpperCase())) {
				f = new File(f.toString() + "." + ff.getDefaultExtension());
			}
			return SwingEngine.getCurrent().exportPathway(f);

		}
		return false;
	}
	
	public boolean exportPathway(final File f) {
		if(mayOverwrite(f)) {
			final SwingProgressKeeper pk = new SwingProgressKeeper(ProgressKeeper.PROGRESS_UNKNOWN);
			final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(getApplicationPanel()), 
					"", pk, false, true);

			SwingWorker sw = new SwingWorker() {
				protected Object doInBackground() throws Exception {
					try {
						pk.setTaskName("Exporting pathway");
						Engine.getCurrent().exportPathway(f);
						return true;
					} catch(ConverterException e) {
						handleConverterException(e.getMessage(), null, e);
						return false;
					} finally {
						pk.finished();
					}
				}
			};

			return processTask(pk, d, sw);
		}
		return false;
	}

	public boolean importPathway() {
		//Open file dialog
		JFileChooser jfc = new JFileChooser();
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setDialogTitle("Import pathway");
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);

		for(final PathwayImporter imp : Engine.getCurrent().getPathwayImporters().values()) {
			jfc.addChoosableFileFilter(new ImporterExporterFileFilter(imp));
		}

		int status = jfc.showDialog(getApplicationPanel(), "Import");
		if(status == JFileChooser.APPROVE_OPTION) {	
			File f = jfc.getSelectedFile();
			ImporterExporterFileFilter ff = (ImporterExporterFileFilter)jfc.getFileFilter();
			if(!f.toString().toUpperCase().endsWith(ff.getDefaultExtension().toUpperCase())) {
				f = new File(f.toString() + "." + ff.getDefaultExtension());
			}
			return SwingEngine.getCurrent().importPathway(f);

		}
		return false;
	}

	public boolean mayOverwrite(File f) {
		boolean allow = true;
		if(f.exists()) {
			int status = JOptionPane.showConfirmDialog(null, "File " + f.getName() + " already exists, overwrite?", 
					"File already exists", JOptionPane.YES_NO_OPTION);
			allow = status == JOptionPane.YES_OPTION;
		}
		return allow;
	}

	public boolean savePathwayAs() {
		//Open file dialog
		JFileChooser jfc = new JFileChooser();
		jfc.setAcceptAllFileFilterUsed(true);
		jfc.setDialogTitle("Save pathway");
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		jfc.addChoosableFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String ext = f.toString().substring(f.toString().length() - 4);
				if(ext.equalsIgnoreCase("xml") || ext.equalsIgnoreCase("gpml")) {
					return true;
				}
				return false;
			}
			public String getDescription() {
				return "GPML files (*.gpml, *.xml)";
			}
			
		});
		int status = jfc.showDialog(null, "Save");
		if(status == JFileChooser.APPROVE_OPTION) {
			File toFile = jfc.getSelectedFile();
			String fn = toFile.toString();
			if(!fn.toLowerCase().endsWith(Engine.PATHWAY_FILE_EXTENSION)) {
				toFile = new File(fn + "." + Engine.PATHWAY_FILE_EXTENSION);
			}
			try {
				if(mayOverwrite(toFile)) {
					Engine.getCurrent().savePathway(toFile);
					return true;
				}
			} catch(ConverterException e) {
				handleConverterException(e.getMessage(), null, e);
			}
		}
		return false;
	}
	
	public boolean savePathway()
	{
		Pathway pathway = Engine.getCurrent().getActivePathway();
		
		boolean result = true;	
		
        // Overwrite the existing xml file.
		// If the target file is read-only, let the user select a new pathway
		if (pathway.getSourceFile() != null && pathway.getSourceFile().canWrite())
		{
			try {
				Engine.getCurrent().savePathway(pathway.getSourceFile());
			} catch (ConverterException e) {
				handleConverterException(e.getMessage(), null, e);
			}
		}
		else {
			result = savePathwayAs();
		}

		return result;
	}
}
