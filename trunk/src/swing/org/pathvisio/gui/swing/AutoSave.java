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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


import org.pathvisio.core.Engine;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.GpmlFormat;
import org.pathvisio.core.model.Pathway;


/**
 *  Collection of methods for autosave and recovery of PathVisio files
 */
public class AutoSave
{
	private Timer timer;
	private final SwingEngine swingEngine;
	private final Engine engine;
	private final File autoSaveFile = autoSaveFileLocation();

	public AutoSave (SwingEngine se) 
	{
		engine =se.getEngine();
		swingEngine = se;
	}

	private File autoSaveFileLocation() 
	{
		String tempDir = System.getProperty("java.io.tmpdir");
		File autoSaveFile = new File(tempDir, "PathVisioAutoSave.gpml");
		return autoSaveFile;
	}

	private void autoSaveFile() throws ConverterException 
	{
		Pathway p = engine.getActivePathway();
		if (p != null) 
		{
			GpmlFormat.writeToXml (p, autoSaveFile, true);
			Logger.log.info("Autosaved");
		}
	}

	private class DoSave extends TimerTask 
	{
		public void run() 
		{
			try {
				// For reasons of thread-safety, autoSaveFile()
				// must be called on the GUI thread.
				SwingUtilities.invokeAndWait(new Runnable()
				{
					public void run()
					{
						try
						{
							autoSaveFile();
						} 
						catch (ConverterException e) 
						{
							Logger.log.error ("Autosave failed", e);
						}
					}
				});
			}
			catch (InterruptedException e)
			{
				Logger.log.error ("Autosave failed", e);
			}
			catch (InvocationTargetException e)
			{
				Logger.log.error ("Autosave failed", e);
			}
		}
	}

	/**
	 * @param period autosave period in seconds
	 */
	public void startTimer(int period) 
	{
		if (autoSaveFile.exists()) 
		{
			autoRecoveryDlg();
		}
		timer = new Timer();
		timer.schedule(new DoSave(), period * 1000, period * 1000);
	}

	public void stopTimer () 
	{
		timer.cancel();
		autoSaveFile.delete();
	}

	private void autoRecoveryDlg() 
	{
		int result = JOptionPane.showConfirmDialog(
				swingEngine.getApplicationPanel(), 
				"Sorry, it seems PathVisio crashed.\n" +
				"Recover the auto-saved file?", 
				"Crash recovery", JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION)
		{
			swingEngine.openPathway(autoSaveFile);
		}
	}
}
