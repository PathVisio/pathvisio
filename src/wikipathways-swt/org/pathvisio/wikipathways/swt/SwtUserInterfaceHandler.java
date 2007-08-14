package org.pathvisio.wikipathways.swt;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.debug.Logger;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.util.swt.ProgressKeeperDialog;
import org.pathvisio.util.swt.SwtProgressKeeper;
import org.pathvisio.wikipathways.UserInterfaceHandler;

public class SwtUserInterfaceHandler implements UserInterfaceHandler {
	Shell shell;
	public SwtUserInterfaceHandler(Shell s) {
		shell = s;
	}
	
	public int askCancellableQuestion(String title, String message) {
		final MessageDialog dialog = new MessageDialog(shell, title, null, message, 
				SWT.ICON_QUESTION, new String[] {"Cancel", "No", "Yes" }, 2);
		threadSave(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		
		int status = dialog.getReturnCode();
		switch(status) {
		case 0: return Q_CANCEL;
		case 1: return Q_FALSE;
		case 2: return Q_TRUE;
		}
		return Q_CANCEL;
	}

	public String askInput(String title, String message) {
		final InputDialog dialog = new InputDialog(shell, title, message, "", null);
		threadSave(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		return dialog.getValue();
	}

	public boolean askQuestion(String title, String message) {
		return MessageDialog.openQuestion(shell, title, message);
	}

	public void runWithProgress(RunnableWithProgress runnable, String title, int totalWork, final boolean canCancel, boolean modal) {
		final SwtProgressKeeper progress = new SwtProgressKeeper(totalWork, runnable);
		runnable.setProgressKeeper(progress);
		final ProgressKeeperDialog dialog = new ProgressKeeperDialog(shell);
		threadSave( new Runnable() {
			public void run() {
				try {
					dialog.run(true, canCancel, progress);
				} catch(Exception e) {
					Logger.log.error("Error while running task", e);
					Throwable t = e;
					if(e instanceof InvocationTargetException) t = ((InvocationTargetException)e).getCause();
					showError("Error while running task", t.toString() + ": " + t.getMessage());
				}
			}
		});
		System.err.println("finished running");
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

	public void showError(final String title, final String message) {
		threadSave(new Runnable() {
			public void run() {
				MessageDialog.openError(shell, title, message);	
			}
		});
	}

	public void showExitMessage(final String string) {
		threadSave(new Runnable() {
			public void run() {
				MessageDialog.openInformation(shell, "Exit", string);	
			}
		});
	}

	public void showInfo(final String title, final String message) {
		threadSave(new Runnable() {
			public void run() {
				MessageDialog.openInformation(shell, title, message);	
			}
		});
	}
	
	private void threadSave(Runnable r) {
		Display d = shell.getDisplay();
		if(Thread.currentThread() == d.getThread()) {
			r.run();
		} else {
			d.syncExec(r);
		}
	}

}
