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
	
	public void report(String message) {
		super.report(message);
		progressBar.setString(message);
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
