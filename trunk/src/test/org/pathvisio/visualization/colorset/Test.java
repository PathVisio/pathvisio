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
package org.pathvisio.visualization.colorset;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.pathvisio.debug.Logger;
import org.pathvisio.preferences.PreferenceManager;

import junit.framework.TestCase;

public class Test extends TestCase 
{
	public void testColorSet()
	{
		PreferenceManager.init();
		ColorSet cs = new ColorSet("Default");
		assertEquals (cs.getName(), "Default");
	}
	
	
	public void testGradient()
	{
		PreferenceManager.init();
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
	
	Map<String, Object> symbols = new HashMap<String, Object>();
	
	boolean evalExpr(String expr) throws Criterion.CriterionException
	{
		Criterion crit = new Criterion();
		crit.setExpression(expr, new ArrayList<String>(symbols.keySet()));
		return crit.evaluate (symbols);
	}
	
	boolean checkSyntax(String expr)
	{
		Criterion crit = new Criterion();
		String result = crit.setExpression(expr, new ArrayList<String>(symbols.keySet()));
		return (result == null);
	}
	
	public void testExpressions() throws Criterion.CriterionException
	{
		symbols.put ("x", 5.0);
		symbols.put ("y", -1.0);
	
		assertFalse (checkSyntax ("5 = 5 = 5"));
		assertFalse (checkSyntax ("5 < 6 > 5"));
		assertFalse (checkSyntax ("abcd"));
		assertFalse (checkSyntax ("[x] < -0.5 3"));
		assertFalse (checkSyntax ("[x] < -0.5 AND"));
		assertFalse (checkSyntax ("([x] < -0.5"));
		assertTrue  (checkSyntax ("([x] < -0.5)"));
		assertFalse (checkSyntax ("x = 5.0.0"));
		
		assertFalse (evalExpr ("[x] < -0.5"));
		assertTrue  (evalExpr ("5.0 > [y]"));
		assertTrue  (evalExpr ("[x] = 5.0"));
		assertFalse (evalExpr ("[y] = -5.0"));
		assertFalse (evalExpr ("[x] < 0 AND [y] < 0"));
		assertFalse (evalExpr ("[x] < 0 AND [y] > 0"));
		assertTrue  (evalExpr ("[x] > 0 AND [y] < 0"));
		assertFalse (evalExpr ("[x] > 0 AND [y] > 0"));
		assertTrue  (evalExpr ("[x] = 0 AND [y] = 0 OR [x] = 5.0 AND [y] = -1.0"));
		assertTrue  (evalExpr ("([x] = 0 AND [y] = 0) OR ([x] = 5.0 AND [y] = -1.0)"));
		assertFalse (evalExpr ("[x] = 0 AND ([y] = 0 OR [x] = -5.0) AND [y] = -1.0"));
		
		symbols.clear();
		symbols.put ("jouw waarde", 5.0);
		symbols.put ("mijn waarde", -1.0);
		assertTrue  (evalExpr ("[jouw waarde] < 0 OR [mijn waarde] < 0"));
		assertFalse (evalExpr ("[jouw waarde] < 0 OR [mijn waarde] > 0"));
		assertTrue  (evalExpr ("[jouw waarde] > 0 OR [mijn waarde] < 0"));
		assertTrue  (evalExpr ("[jouw waarde] > 0 OR [mijn waarde] > 0"));
	}

	/* added for bug 952 
	 * test correct dealing of NA values */
	public void testExprWithNA() throws Criterion.CriterionException
	{
		symbols.put ("var1", "NA");
		symbols.put ("var2", 1.0);
		
		assertTrue  (evalExpr ("([var1] > 0) OR ([var2] > 0)")); // NA OR true
		assertTrue  (evalExpr ("([var2] > 0) OR ([var1] > 0)")); // true OR NA				
		
		assertFalse (evalExpr ("([var2] < 0) OR ([var1] < 0)")); // false OR NA 
		assertFalse  (evalExpr ("([var1] < 0) OR ([var2] < 0)")); // NA OR false
		
		assertFalse (evalExpr ("([var2] < 0) AND ([var1] < 0)")); // false AND NA
		assertFalse  (evalExpr ("([var1] < 0) AND ([var2] < 0)")); // NA AND false
		
		assertFalse (evalExpr ("([var2] > 0) AND ([var1] > 0)")); // true AND NA	
		assertFalse  (evalExpr ("([var1] > 0) AND ([var2] > 0)")); // NA AND true
	}
	
	public void testStringLiteral () throws Criterion.CriterionException
	{
		symbols.put ("color", "red");
		symbols.put ("y", -1.0);
		assertFalse (checkSyntax ("\"red\" > [color]"));
		assertTrue  (checkSyntax ("[color] = \"red\""));
		assertTrue  (checkSyntax ("\"red\" = [color]"));
		assertTrue  (evalExpr ("[color] = \"red\""));
		assertFalse (evalExpr ("[color] = \"green\""));
		assertTrue  (evalExpr ("\"green\" <> [color]"));
		assertFalse (evalExpr ("\"red\" <> [color]"));
		assertTrue  (evalExpr ("\"green\" <> \"red\""));
		assertTrue  (evalExpr ("\"green\" = \"green\""));
	}
}
