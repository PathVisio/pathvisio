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
package org.pathvisio.gui.wikipathways;

import java.awt.Component;
import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

import org.pathvisio.debug.Logger;

public class WebstartUserInterfaceHandler extends SwingUserInterfaceHandler {
	
	public WebstartUserInterfaceHandler(Component parent) {
		super(parent);
	}

	public void showDocument(URL url, String target) {
		try {
			BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
			bs.showDocument(url);
		} catch (UnavailableServiceException e) {
			Logger.log.error("Unable to get javax.jnlp.BasicService, are you not using webstart?");
		} 
	}
}
