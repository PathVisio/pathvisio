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
package org.pathvisio.gui.swing;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.bridgedb.bio.Organism;
import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.Globals;
import org.pathvisio.data.GdbManager;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.Pathway.StatusFlagEvent;
import org.pathvisio.model.PathwayExporter;
import org.pathvisio.model.PathwayImporter;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.Utils;
import org.pathvisio.util.swing.Compat;
import org.pathvisio.view.VPathwayWrapper;
import org.pathvisio.view.swing.VPathwaySwing;

/**
 * SwingEngine ties together a number of global objects needed both in the
 * WikiPathways applet and in the Standalone App, but not in command-line tools.
 *
 * It keeps the main panel, the main frame and has
 * helper functions for opening, closing, importing and exporting Pathways.
 */
public class SwingEngine implements ApplicationEventListener, Pathway.StatusFlagListener {
	private MainPanel mainPanel;

	private CommonActions actions;
	private JFrame frame; // may be null (for applet...)

	private Engine engine;
	private GdbManager gdbManager = null;
	private final Compat compat;

	public Engine getEngine() { return engine; }

	public SwingEngine(Engine engine)
	{
		this.engine = engine;
		gdbManager = new GdbManager();
		actions = new CommonActions(this);
		engine.addApplicationEventListener(this);
		compat = new Compat(this);
		engine.addApplicationEventListener(compat);
	}

	public GdbManager getGdbManager()
	{
		return gdbManager;
	}

	public CommonActions getActions() {
		return actions;
	}

	public MainPanel getApplicationPanel() {
		return getApplicationPanel(false);
	}

	public MainPanel getApplicationPanel(boolean forceNew) {
		if(forceNew || !hasApplicationPanel()) {
			mainPanel = new MainPanel(this);
		}
		return mainPanel;
	}

	public void setApplicationPanel(MainPanel mp) {
		if(mainPanel != null) {
			Container parent = mainPanel.getParent();
			if(parent != null) parent.remove(mainPanel);
		}
		mainPanel = mp;
	}

	public boolean hasApplicationPanel() {
		return mainPanel != null;
	}

	public void handleConverterException(String message, Component c, Throwable e) {
		if (e.getMessage() != null &&
				e.getMessage().contains("Cannot find the declaration of element 'Pathway'"))
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

	//TODO: deprecate
	public boolean processTask(ProgressKeeper pk, ProgressDialog d, SwingWorker<Boolean, Boolean> sw) {
		sw.execute();
		d.setVisible(true);
		try {
			return sw.get();
		} catch (ExecutionException e)
		{
			handleConverterException("Exception during conversion", null, e.getCause());
			return false;
		} catch (InterruptedException e) {
			handleConverterException("Conversion was cancelled or interrupted", null, e);
			return false;
		}
	}

	public boolean openPathway(final URL url) {
		final ProgressKeeper pk = new ProgressKeeper();
		final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(getApplicationPanel()),
				"", pk, false, true);

		SwingWorker<Boolean, Boolean> sw = new SwingWorker<Boolean, Boolean>() {
			protected Boolean doInBackground() {
				pk.setTaskName("Opening pathway");
				try {
					engine.setWrapper (createWrapper());
					engine.openPathway(url);
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

	public boolean openPathway(final File f)
	{
		final ProgressKeeper pk = new ProgressKeeper();
		final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(getApplicationPanel()),
				"", pk, false, true);

		engine.setWrapper (createWrapper());
		SwingWorker<Boolean, Boolean> sw = new SwingWorker<Boolean, Boolean>() {
			protected Boolean doInBackground() {
				pk.setTaskName("Opening pathway");
				try {
					engine.openPathway(f);
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
		final ProgressKeeper pk = new ProgressKeeper();
		final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(getApplicationPanel()),
				"", pk, false, true);

		SwingWorker<Boolean, Boolean> sw = new SwingWorker<Boolean,Boolean>() {
			protected Boolean doInBackground() {
				pk.setTaskName("Importing pathway");
				try {
					boolean editMode = engine.hasVPathway() ? engine.getActiveVPathway().isEditMode() : true;
					engine.setWrapper (createWrapper());
					engine.importPathway(f);
					engine.getActiveVPathway().setEditMode(editMode);
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
		engine.setWrapper (createWrapper());
		engine.newPathway();
	}

	public boolean exportPathway() {
		//Open file dialog
		JFileChooser jfc = new JFileChooser();
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setDialogTitle("Export pathway");
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		jfc.setCurrentDirectory(PreferenceManager.getCurrent().getFile(GlobalPreference.DIR_LAST_USED_EXPORT));

		SortedSet<PathwayExporter> exporters = new TreeSet<PathwayExporter>(
				new Comparator<PathwayExporter>() {
					public int compare(PathwayExporter o1, PathwayExporter o2) {
						return o1.getName().compareTo(o2.getName());
					}
				}
		);
		exporters.addAll(engine.getPathwayExporters().values());

		FileFilter selectedFilter = null;
		for(PathwayExporter exp : exporters) {
			FileFilter ff = new ImporterExporterFileFilter(exp);
			jfc.addChoosableFileFilter(ff);
			if(exp instanceof GpmlFormat) {
				selectedFilter = ff;
			}
		}
		if(selectedFilter != null) jfc.setFileFilter(selectedFilter);

		int status = jfc.showDialog(getApplicationPanel(), "Export");
		if(status == JFileChooser.APPROVE_OPTION) {
			File f = jfc.getSelectedFile();
			PreferenceManager.getCurrent().setFile(GlobalPreference.DIR_LAST_USED_EXPORT,
					jfc.getCurrentDirectory());

			ImporterExporterFileFilter ff = (ImporterExporterFileFilter)jfc.getFileFilter();
			if(!f.toString().toUpperCase().endsWith("." + ff.getDefaultExtension().toUpperCase())) {
				f = new File(f.toString() + "." + ff.getDefaultExtension());
			}
			return exportPathway(f);

		}
		return false;
	}

	public boolean exportPathway(final File f) {
		if(mayOverwrite(f)) {
			final ProgressKeeper pk = new ProgressKeeper();
			final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(getApplicationPanel()),
					"", pk, false, true);

			// create a clone so we can safely act on it in a worker thread.
			final Pathway clone = engine.getActivePathway().clone();

			SwingWorker<Boolean, Boolean> sw = new SwingWorker<Boolean, Boolean>() {
				protected Boolean doInBackground() {
					try {
						pk.setTaskName("Exporting pathway");
						engine.exportPathway(f, clone);
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
		jfc.setCurrentDirectory(PreferenceManager.getCurrent().getFile(GlobalPreference.DIR_LAST_USED_IMPORT));

		SortedSet<PathwayImporter> importers = new TreeSet<PathwayImporter>(
				new Comparator<PathwayImporter>() {
					public int compare(PathwayImporter o1, PathwayImporter o2) {
						return o1.getName().compareTo(o2.getName());
					}
				}
		);
		importers.addAll(engine.getPathwayImporters().values());
		FileFilter selectedFilter = null;
		for(PathwayImporter imp : importers) {
			FileFilter ff = new ImporterExporterFileFilter(imp);
			jfc.addChoosableFileFilter(ff);
			if(imp instanceof GpmlFormat) {
				selectedFilter = ff;
			}
		}
		if(selectedFilter != null) jfc.setFileFilter(selectedFilter);

		int status = jfc.showDialog(getApplicationPanel(), "Import");
		if(status == JFileChooser.APPROVE_OPTION) {
			File f = jfc.getSelectedFile();
			PreferenceManager.getCurrent().setFile(GlobalPreference.DIR_LAST_USED_IMPORT,
					jfc.getCurrentDirectory());
			ImporterExporterFileFilter ff = (ImporterExporterFileFilter)jfc.getFileFilter();
			if(!f.toString().toUpperCase().endsWith(ff.getDefaultExtension().toUpperCase())) {
				f = new File(f.toString() + "." + ff.getDefaultExtension());
			}
			return importPathway(f);

		}
		return false;
	}

	/**
	 * Opens a file chooser dialog, and opens the chosen pathway.
	 * @return true if a pathway was openend, false if the operation was
	 * cancelled
	 */
	public boolean openPathway()
	{
		//Open file dialog
		JFileChooser jfc = new JFileChooser();
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setDialogTitle("Open pathway");
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		jfc.setCurrentDirectory(PreferenceManager.getCurrent().getFile(GlobalPreference.DIR_LAST_USED_OPEN));

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

		//TODO: use constants for extensions
		int status = jfc.showDialog(getApplicationPanel(), "Open pathway");
		if(status == JFileChooser.APPROVE_OPTION) {
			File f = jfc.getSelectedFile();
			PreferenceManager.getCurrent().setFile(GlobalPreference.DIR_LAST_USED_OPEN,
				jfc.getCurrentDirectory());
			if(!(f.toString().toUpperCase().endsWith("GPML") || f.toString().toUpperCase().endsWith("XML")))
			{
				f = new File(f.toString() + ".gpml");
			}
			return openPathway(f);
		}
		return false;
	}

	public boolean mayOverwrite(File f) {
		boolean allow = true;
		if(f.exists()) {
			int status = JOptionPane.showConfirmDialog(frame, "File " + f.getName() + " already exists, overwrite?",
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
		jfc.setCurrentDirectory(PreferenceManager.getCurrent().getFile(GlobalPreference.DIR_LAST_USED_SAVE));
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
		int status = jfc.showDialog(frame, "Save");
		if(status == JFileChooser.APPROVE_OPTION) {
			File toFile = jfc.getSelectedFile();
			PreferenceManager.getCurrent().setFile(GlobalPreference.DIR_LAST_USED_SAVE,
					jfc.getCurrentDirectory());
			String fn = toFile.toString();
			if(!fn.toLowerCase().endsWith(Engine.PATHWAY_FILE_EXTENSION)) {
				toFile = new File(fn + "." + Engine.PATHWAY_FILE_EXTENSION);
			}
			try {
				if(mayOverwrite(toFile)) {
					engine.savePathway(toFile);
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
		Pathway pathway = engine.getActivePathway();

		boolean result = true;

        // Overwrite the existing xml file.
		// If the target file is read-only, let the user select a new pathway
		if (pathway.getSourceFile() != null && pathway.getSourceFile().canWrite())
		{
			try {
				engine.savePathway(pathway.getSourceFile());
			} catch (ConverterException e) {
				handleConverterException(e.getMessage(), null, e);
			}
		}
		else {
			result = savePathwayAs();
		}

		return result;
	}

	/**
	 * Call this when the user is about to perform an
	 * action that could lead to discarding the current pathway.
	 * (For example when creating a new pathway)
	 *
	 * Checks if there are any unsaved changes, and
	 * asks the user if they want to save those changes.
	 *
	 * @return true if the user allows discarding the pathway, possibly after saving.
	 */
	public boolean canDiscardPathway()
	{
		Pathway pathway = engine.getActivePathway();
        // checking not necessary if there is no pathway or if pathway is not changed.

		if (pathway == null || !pathway.hasChanged()) return true;
		int result = JOptionPane.showConfirmDialog
			(frame, "Save changes?",
					"Your pathway has changed. Do you want to save?",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		if (result == JOptionPane.CANCEL_OPTION) // cancel
		{
			return false;
		}
		else if (result == JOptionPane.YES_OPTION) // yes
		{
			// return false if save is cancelled.
			return (savePathway());
		}
		// yes or no
		return true;
	}

	public void applicationEvent(ApplicationEvent e)
	{
		if(e.getType() == ApplicationEvent.PATHWAY_OPENED)
		{
			updateTitle();
			engine.getActivePathway().addStatusFlagListener(SwingEngine.this);
		}
		else if (e.getType() == ApplicationEvent.PATHWAY_NEW)
		{
			updateTitle();
			engine.getActivePathway().addStatusFlagListener(SwingEngine.this);
		}
	}

	public void updateTitle()
	{
		if (frame != null)
		{
			if (engine.getActivePathway() == null)
			{
				frame.setTitle(engine.getApplicationName());
			}
			else
			{
				boolean changeStatus = engine.getActivePathway().hasChanged();
				// get filename, or (New Pathway) if current pathway hasn't been opened yet
				String fname = (engine.getActivePathway().getSourceFile() == null) ? "(New Pathway)" :
					engine.getActivePathway().getSourceFile().getName();
				frame.setTitle(
					(changeStatus ? "*" : "") + fname + " - " +
					engine.getApplicationName()
					);
			}
		}
	}

	public void statusFlagChanged(StatusFlagEvent e)
	{
		updateTitle();
	}

	public void setFrame(JFrame frame)
	{
		this.frame = frame;
	}

	public JFrame getFrame()
	{
		return frame;
	}

	private Browser browser = null;

	/**
	 * Set the browser launcher that will be used to open urls in the
	 * system's default web browser.
	 */
	public void setUrlBrowser(Browser b) {
		this.browser = b;
	}

	/**
	 * Opens an URL in the system's default browser if a browser is set
	 * @see #setUrlBrowser
	 * @throws UnsupportedOperationException when there is no browser set.
	 */
	public void openUrl(URL url) throws UnsupportedOperationException {
		if(browser != null) browser.openUrl(url);
	}

	/**
	 * Simple interface to allow different browser launcher implementations.
	 * Note: Java 1.6 provides an easy method to open an url in the default system browser
	 * using {@link Desktop#browse(java.net.URI)}, we should use this in the future.
	 * @author thomas
	 */
	public interface Browser {
		public void openUrl(URL url);
	}

	private boolean disposed = false;
	/**
	 * free all resources (such as listeners) held by this class.
	 * Owners of this class must explicitly dispose of it to clean up.
	 */
	public void dispose()
	{
		assert (!disposed);
		engine.removeApplicationEventListener(this);
		engine.removeApplicationEventListener(compat);
		disposed = true;
	}

	/**
	 * Returns the organism set in the active pathway.
	 * May return null if there is no current organism set
	 */
	public Organism getCurrentOrganism()
	{
		String organism = getEngine().getActivePathway().getMappInfo().getOrganism();
		return Organism.fromLatinName(organism);
	}
}
