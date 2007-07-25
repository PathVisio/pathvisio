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
package org.pathvisio.util;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class ProgressKeeper {
	public static final int PROGRESS_UNKNOWN = -1;
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
		fireProgressEvent(ProgressEvent.FINISHED);
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
	
	public int getProgress() {
		return progress;
	}
	
	public void report(String message) {
		//To be implemented by subclasses if needed
	}
		
	void fireProgressEvent(int type) {
		for(ProgressListener l : listeners)
			l.progressFinished(new ProgressEvent(this, type));
	}
	
	List<ProgressListener> listeners = new ArrayList<ProgressListener>();
	
	public void addListener(ProgressListener l) {
		if(!listeners.contains(l)) listeners.add(l);
	}
	
	public class ProgressEvent extends EventObject {
		public static final int FINISHED = 0;
		
		private int type;
		public ProgressEvent(ProgressKeeper source, int type) {
			super(source);
			this.type = type;
		}
		public int getType() { return type; }
		public ProgressKeeper getProgressKeeper() { return (ProgressKeeper)getSource(); }
	}
	
	public interface ProgressListener {
		public void progressFinished(ProgressEvent e);
	}
}
