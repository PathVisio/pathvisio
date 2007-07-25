package org.pathvisio.util;

public abstract class RunnableWithProgress<T> implements Runnable {
	ProgressKeeper p;
	T value;
	
	public ProgressKeeper getProgressKeeper() {
		if(p == null) p = new ProgressKeeper(ProgressKeeper.PROGRESS_UNKNOWN);
		return p;
	}
	public void setProgressKeeper(ProgressKeeper progress) {
		p = progress;
	}
	
	public void run() {
		value = excecuteCode();
	}
	
	public abstract T excecuteCode();
	
	public T get() {
		return value;
	}
}
