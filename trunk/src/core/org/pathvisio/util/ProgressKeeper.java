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
package org.pathvisio.util;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * A method to keep track of progress of a background task.
 * Can handle cancellation, task names and progress reports.
 *
 * This class is UI independent and should be used by long-running methods
 * that can be run either from the command line, or from the GUI, in the latter
 * case a ProgressDialog may be used to let the user monitor and cancel the task.
 */
public class ProgressKeeper {
	private static final int PROGRESS_UNKNOWN = -1;
	private static final int PROGRESS_FINISHED = -2;

	volatile String taskName;
	volatile boolean cancelled;
	volatile String report;
	volatile int progress;

	int total;

	/**
	 * create a ProgressKeeper of work of indeterminate length
	 */
	public ProgressKeeper() {
		total = PROGRESS_UNKNOWN;
	}

	/**
	 * create a ProgressKeeper of work of specified length
	 */
	public ProgressKeeper(int totalWork) {
		total = totalWork;
	}

	/** returns true if work is of indeterminate length */
	public boolean isIndeterminate()
	{
		return (total == PROGRESS_UNKNOWN);
	}

	//TODO use setProgress instead?
	public void worked(int w) {
		if(!isFinished()) {
			progress += w;
			fireProgressEvent(ProgressEvent.PROGRESS_CHANGED);
			if(progress >= total) {
				progress = total; //to trigger event
				finished();
			}
		}
	}

	public void setProgress(int val)
	{
		if(!isFinished())
		{
			progress = val;
			fireProgressEvent(ProgressEvent.PROGRESS_CHANGED);
			if(progress >= total) {
				progress = total; //to trigger event
				finished();
			}
		}
	}

	public void setTaskName(String name) {
		taskName = name;
		fireProgressEvent(ProgressEvent.TASK_NAME_CHANGED);
	}

	public String getTaskName() { return taskName; }

	public void finished() {
		if(!isFinished()) { //Only fire event once
			progress = PROGRESS_FINISHED;
			fireProgressEvent(ProgressEvent.FINISHED);
		}
	}

	public boolean isFinished() {
		return progress == PROGRESS_FINISHED;
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

	public void report(String message)
	{
		report = message;
		fireProgressEvent(ProgressEvent.REPORT);
	}

	public String getReport() { return report; }

	void fireProgressEvent(final int type)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				for(ProgressListener l : listeners)
					l.progressEvent(new ProgressEvent(ProgressKeeper.this, type));
			}
		});
	}

	List<ProgressListener> listeners = new ArrayList<ProgressListener>();

	public void addListener(ProgressListener l) {
		if(!listeners.contains(l)) listeners.add(l);
	}

	public List<ProgressListener> getListeners() {
		return listeners;
	}

	/**
	 * notifies of changes to this ProgressKeeper,
	 * when
	 *
	 * - report message has changed
	 * - progress percentage has changed
	 * - task has finished
	 * - task name has changed
	 */
	public class ProgressEvent extends EventObject {

		public static final int FINISHED = 0;
		public static final int TASK_NAME_CHANGED = 1;
		public static final int REPORT = 2;
		public static final int PROGRESS_CHANGED = 3;

		private int type;
		public ProgressEvent(ProgressKeeper source, int type) {
			super(source);
			this.type = type;
		}
		public int getType() { return type; }
		public ProgressKeeper getProgressKeeper() { return (ProgressKeeper)getSource(); }
	}

	/** Implement this if you wish to receive ProgressEvents.*/
	public interface ProgressListener {
		public void progressEvent(ProgressEvent e);
	}
}
