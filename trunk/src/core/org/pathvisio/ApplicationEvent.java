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
package org.pathvisio;

import java.util.EventObject;

public class ApplicationEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	public static final int PATHWAY_OPENED = 1;
	public static final int PATHWAY_NEW = 2;
	public static final int APPLICATION_CLOSE = 3;
	public static final int VPATHWAY_CREATED = 4;
	public static final int VPATHWAY_OPENED = 5;
	public static final int VPATHWAY_NEW = 6;

	public Object source;
	public int type;
	public boolean doit;
	
	public ApplicationEvent(Object source, int type) {
		super(source);
		this.source = source;
		this.type = type;
		doit = true;
	}
}
