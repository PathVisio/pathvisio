package org.pathvisio.util;

public class ProgressKeeper {
	volatile String taskName;
	volatile boolean cancelled;
	
	int total;
	int progress;
	
	public ProgressKeeper(int totalWork) {
		total = totalWork;
	}
	
	public void worked(int w) {
		progress += w;
	}
	
	public final void worked(double d) {
		worked((int)d);
	}
	
	public void setTaskName(String name) {
		taskName = name;
	}
	
	public void finished() {
		progress = total;
	}
	
	public void cancel() {
		cancelled = true;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public int getTotalWork() {
		return total;
	}
	
	public void report(String message) {
		//To be implemented by subclasses if needed
	}
}
