package org.pathvisio.util.swt;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.util.ProgressKeeper;

public class ProgressKeeperDialog extends ProgressMonitorDialog {
	ProgressKeeper progress;
	
	public ProgressKeeperDialog(Shell shell) {
		super(shell);
	}

	public void run(boolean fork, boolean cancellable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		if(runnable instanceof ProgressKeeper) progress = (ProgressKeeper)runnable;
		super.run(fork, cancellable, runnable);
	}
	
	protected void cancelPressed() {
		if(progress != null) progress.cancel();
		super.cancelPressed();
	}
}