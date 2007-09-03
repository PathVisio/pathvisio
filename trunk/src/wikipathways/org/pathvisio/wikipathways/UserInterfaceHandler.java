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
package org.pathvisio.wikipathways;

import java.net.URL;

import org.pathvisio.util.RunnableWithProgress;

/**
 * Interface that specifies methods related to user interface tasks. This interface may be used by classes where it is
 * not desirable to couple them tightly to a UI library (e.g. SWT or Swing), but where user interaction
 * is needed.
 * @author thomas
 */
public interface UserInterfaceHandler {		
	public static final int Q_CANCEL = -1;
	public static final int Q_TRUE = 0;
	public static final int Q_FALSE = 1;
	
	public void showInfo(String title, String message);
	public void showError(String title, String message);
	public String askInput(String title, String message);
	public boolean askQuestion(String title, String message);
	public int askCancellableQuestion(String title, String message);
	
	public void runWithProgress(RunnableWithProgress runnable, String title, int totalWork, boolean canCancel, boolean modal);
	public void showExitMessage(String string);
	public void showDocument(URL url, String target);
}
