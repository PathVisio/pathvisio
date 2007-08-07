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

import java.io.File;

class GpmlDiffGui
{
	public static void main (String[] argv)
	{		
		GpmlDiffWindow window = new GpmlDiffWindow();
		if (argv.length > 0)
		{
			File f = new File (argv[0]);
			if (f.exists())
			{				
				window.setFile(GpmlDiffWindow.PWY_OLD, f);
				if (argv.length > 1)
				{
					f = new File (argv[1]);
					if (f.exists())
					{
						window.setFile (GpmlDiffWindow.PWY_NEW, f);
					}
				}
			}
		}
		window.setVisible (true);
	}
}