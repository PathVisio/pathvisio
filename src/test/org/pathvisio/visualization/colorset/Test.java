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

import junit.framework.TestCase;
import java.util.List;
import java.awt.Color;
import org.pathvisio.visualization.colorset.ColorGradient.ColorValuePair;

public class Test extends TestCase 
{
	public void testColorSet()
	{
		ColorSet cs = new ColorSet("Default");
		assertEquals (cs.getName(), "Default");
	}
	
	
	public void testGradient()
	{
		ColorSet cs = new ColorSet("Test");
		ColorGradient cg = new ColorGradient (cs);
		assertEquals (cg.getColorValuePairs().size(), 0);
		cg.addColorValuePair(
				cg.new ColorValuePair (
						new Color (0,0,255), -1.0)
				);
		cg.addColorValuePair(
				cg.new ColorValuePair (
						new Color (255,0,0), 1.0)
				);
		assertEquals (cg.getColorValuePairs().size(), 2);
		assertEquals (cg.getColor(0.0), new Color (127,0,127));
	}
}
