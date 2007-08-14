package org.pathvisio.util.swt;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.pathvisio.util.ProgressKeeper;

public class SwtProgressKeeper extends ProgressKeeper implements IRunnableWithProgress {
	IProgressMonitor monitor;
	Runnable runnable;
	
	public SwtProgressKeeper(int totalWork) {
		this(totalWork, null);
	}
	
	public SwtProgressKeeper(int totalWork, Runnable r) {
		super(totalWork);
		runnable = r;
	}
	
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		this.monitor = monitor;
		if(runnable != null) {
			monitor.beginTask(getTaskName(), getTotalWork());
			runnable.run();
		}
	}
	
	public void worked(int w) {
		super.worked(w);
		monitor.worked(w);
	}
	
	public void setTaskName(String name) {
		super.setTaskName(name);
		monitor.setTaskName(name);
	}
	
	public void finished() {
		super.finished();
		monitor.done();
	}
	
	public void report(String message) {
		super.report(message);
		setTaskName(message);
	}
}