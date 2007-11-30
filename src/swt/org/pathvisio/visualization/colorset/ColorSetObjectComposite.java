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
package org.pathvisio.visualization.colorset;

import org.eclipse.swt.widgets.Composite;

/**
   abstract base class for Composites that are used
   to configure ColorSetObjects.
   Used by the ColorSetComposite
 */
abstract class ColorSetObjectComposite extends Composite 
{
	protected ColorSetObject input;
	
	ColorSetObjectComposite(Composite parent, int style)
	{
		super(parent, style);
		createContents();
	}

	/**
	  Set the input, i.e. the ColorSetObject that is going
	  to get configured by this Composite.
	 */
	void setInput(ColorSetObject input)
	{
		this.input = input;
	}
	
	abstract protected void createContents();

	/**
	   refresh this composite, after the model has changed.
	 */
	abstract protected void refresh();
}
