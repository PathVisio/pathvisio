package debug;

import gmmlVision.GmmlVision;

public class StopWatch {
	boolean running;
	long start;
	long last;
	
	public void start() {
		start = System.currentTimeMillis();
		running = true;
	}
	
	public long stop() {
		last = System.currentTimeMillis() - start;
		running = false;
		return last;
	}
		
	public long look() {
		if(running) return System.currentTimeMillis() - start;
		return last;
	}
	
	public void stopToLog(String msg) {
		GmmlVision.log.trace(msg + "\t" + stop());
	}
}
