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
import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.progress.ProgressDialog;
import org.pathvisio.gui.swing.progress.SwingProgressKeeper;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.wikipathways.UserInterfaceHandler;

public class SwingUserInterfaceHandler implements UserInterfaceHandler {
	Component parent;
	
	public SwingUserInterfaceHandler(Component parent) {
		this.parent = parent;
	}
	
	private abstract class RunnableValue <T> implements Runnable {
		T value;
		public T get() { return value; }
		public void set(T value) { this.value = value; }
	}
	
	private void invoke(Runnable r) {
		try {
			if(SwingUtilities.isEventDispatchThread()) {
				r.run();
			} else {
				SwingUtilities.invokeAndWait(r);
			}
		} catch(Exception e) {
			Logger.log.error("Unable to invoke runnable", e);
		}
	}
	public int askCancellableQuestion(final String title, final String message) {
		RunnableValue<Integer> r = new RunnableValue<Integer>() {
			public void run() {
				int status = JOptionPane.showConfirmDialog(parent, message, title, 
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				set(status);
			}
		};
		invoke(r);
		switch(r.get()) {
		case JOptionPane.YES_OPTION:
			return Q_TRUE;
		case JOptionPane.NO_OPTION:
			return Q_FALSE;
		case JOptionPane.CANCEL_OPTION:
			return Q_CANCEL;
		}
		return Q_FALSE;
	}

	public String askInput(final String title, final String message) {
		RunnableValue<String> r = new RunnableValue<String>() {
			public void run() {
				set(JOptionPane.showInputDialog(parent, message, title));
			}
		};
		invoke(r);
		return r.get();
	}

	public boolean askQuestion(String title, String message) {
		int status = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
		return status == JOptionPane.YES_OPTION;
	}

	public void showError(String title, String message) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
	}

	public void showInfo(String title, String message) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
		
	public void runWithProgress(final RunnableWithProgress runnable, String title, int totalWork, boolean canCancel, boolean modal) {
		SwingProgressKeeper pk = new SwingProgressKeeper(totalWork);
		final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(parent), title, pk, canCancel, modal);
				
		runnable.setProgressKeeper(pk);
		SwingWorker sw = new SwingWorker() {
			protected Object doInBackground() throws Exception {
				runnable.run();
				runnable.getProgressKeeper().finished();
				return runnable.get();
			}
		};
		
		sw.execute();
		
		d.setVisible(true); //If dialog is modal, method will return when progresskeeper is finished
	}
	
	public void showExitMessage(String string) {
		showInfo("Exit", string);
	}
	
	public void showDocument(URL url, String target) {
		try {
			BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
			bs.showDocument(url);
		} catch (UnavailableServiceException e) {
			Logger.log.error("Unable to get javax.jnlp.BasicService, are you not using webstart?");
			showError("Error", "Show Document not yet implemented");
		} 
	}
}
