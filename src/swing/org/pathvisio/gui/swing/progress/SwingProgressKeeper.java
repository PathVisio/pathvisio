package org.pathvisio.gui.swing.progress;

import javax.swing.JProgressBar;

import org.pathvisio.util.ProgressKeeper;

public class SwingProgressKeeper extends ProgressKeeper {
	JProgressBar progressBar;
	
	public SwingProgressKeeper(int totalWork) {
		super(totalWork);
		progressBar = new JProgressBar(0, totalWork < 1 ? 1 : totalWork);
		if(totalWork == ProgressKeeper.PROGRESS_UNKNOWN) {
			progressBar.setIndeterminate(true);
		}
	}
	
	public void report(String name) {
		super.setTaskName(name);
		progressBar.setString(name);
	}
	
	public void worked(int w) {
		super.worked(w);
		progressBar.setValue(getProgress());
	}
	
	public JProgressBar getJProgressBar() {
		return progressBar;
	}
	
	public void finished() {
		progressBar.setValue(getTotalWork());
		super.finished();
	}
}
