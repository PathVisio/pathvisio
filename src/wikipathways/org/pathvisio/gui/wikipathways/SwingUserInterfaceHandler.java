package org.pathvisio.gui.wikipathways;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.gui.swing.progress.ProgressDialog;
import org.pathvisio.gui.swing.progress.SwingProgressKeeper;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.wikipathways.UserInterfaceHandler;

public class SwingUserInterfaceHandler implements UserInterfaceHandler {
	Component parent;
	
	public SwingUserInterfaceHandler(Component parent) {
		this.parent = parent;
	}
	
	public int askCancellableQuestion(String title, String message) {
		int status = JOptionPane.showConfirmDialog(parent, message, title, 
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		switch(status) {
		case JOptionPane.YES_OPTION:
			return Q_TRUE;
		case JOptionPane.NO_OPTION:
			return Q_FALSE;
		case JOptionPane.CANCEL_OPTION:
			return Q_CANCEL;
		}
		return Q_FALSE;
	}

	public String askInput(String title, String message) {
		return JOptionPane.showInputDialog(parent, message, title);
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
}
