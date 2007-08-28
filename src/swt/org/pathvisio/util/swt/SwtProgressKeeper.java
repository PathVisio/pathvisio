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