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
package org.pathvisio.gpmldiff;

import java.io.InputStream;
import org.jdom.*;
import org.jdom.input.*;

class Patch
{
	private enum ModifcationType
	{
		INSERTION,
			DELETION,
			MODIFCATION
	}
	
	private class Modification
	{
		public ModifcationType type; 
		public PathwayElement oldElt;
		public PathwayElement newElt;
		public String attr;
		public String oldValue;
		public String newValue;
	}
	
	void readFromStream (InputStream in)
	{
		SAXBuilder builder = new SAXBuilder ();
		Document doc = builder.build (in);

		for (Element e : doc.getRootElement().getChildren())
		{
			if (e.getName().equals("Modify"))
			{
				Modification mod = new Modifcation();
				GpmlFormat.mapElement (e)
			}
		}
	}

	void reverse()
	{
	}

	void applyTo (Pathway oldPwy, int fuzz)
	{
	}
}