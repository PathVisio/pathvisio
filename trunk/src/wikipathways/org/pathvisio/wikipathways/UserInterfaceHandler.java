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
package org.pathvisio.wikipathways;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.gui.swing.ProgressDialog;
import org.pathvisio.gui.wikipathways.PathwayPageApplet;

public class UserInterfaceHandler {
	public static final int Q_CANCEL = -1;
	public static final int Q_TRUE = 0;
	public static final int Q_FALSE = 1;

	Component parent;

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
				int status = JOptionPane.showConfirmDialog(getParent(), message, title,
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
				set(JOptionPane.showInputDialog(getParent(), message, title));
			}
		};
		invoke(r);
		return r.get();
	}

	public boolean askQuestion(String title, String message) {
		int status = JOptionPane.showConfirmDialog(getParent(), message, title, JOptionPane.YES_NO_OPTION);
		return status == JOptionPane.YES_OPTION;
	}

	public void showError(String title, String message) {
		JOptionPane.showMessageDialog(getParent(), message, title, JOptionPane.ERROR_MESSAGE);
	}

	public void showInfo(String title, String message) {
		JOptionPane.showMessageDialog(getParent(), message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public <T> void runWithProgress(final RunnableWithProgress<T> runnable, String title, boolean canCancel, boolean modal) {
		ProgressKeeper pk = runnable.getProgressKeeper();
		final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(getParent()), title, pk, canCancel, modal);

		runnable.setProgressKeeper(pk);
		SwingWorker<T, Void> sw = new SwingWorker<T, Void>() {
			protected T doInBackground() throws Exception {
				runnable.run();
				runnable.getProgressKeeper().finished();
				return runnable.get();
			}
		};

		sw.execute();

		d.setVisible(true); //If dialog is modal, method will return when progresskeeper is finished
	}

	PathwayPageApplet applet;

	public UserInterfaceHandler(PathwayPageApplet applet)
	{
		this.parent = JOptionPane.getFrameForComponent(applet);
		this.applet = applet;
	}

	public Component getParent() {
		parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		parent = SwingUtilities.getRoot(parent);
		return parent;
	}

	public void showExitMessage(String msg) {
		if(applet.isFullScreen()) {
			applet.toEmbedded();
		}
		JLabel label = new JLabel(msg, JLabel.CENTER);
		applet.getContentPane().removeAll();
		applet.getContentPane().add(label, BorderLayout.CENTER);
		applet.getContentPane().validate();
		applet.getContentPane().repaint();
	}

	public void showDocument(URL url, String target) {
		applet.getAppletContext().showDocument(url, target);
	}
}
