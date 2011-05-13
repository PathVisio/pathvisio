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
package org.pathvisio.desktop.visualization;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.desktop.visualization.ColorGradient;
import org.pathvisio.desktop.visualization.ColorSet;
import org.pathvisio.desktop.visualization.Criterion;
import org.pathvisio.desktop.visualization.ColorGradient.ColorValuePair;
import org.pathvisio.desktop.visualization.Criterion.CriterionException;

public class TestColorset extends TestCase
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
		ColorGradient cg = new ColorGradient ();
		assertEquals (cg.getColorValuePairs().size(), 0);
		cg.addColorValuePair(
				new ColorValuePair (
						new Color (0,0,255), -1.0)
				);
		cg.addColorValuePair(
				new ColorValuePair (
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

	double evalDouble(String expr) throws Criterion.CriterionException
	{
		Criterion crit = new Criterion();
		crit.setExpression(expr, new ArrayList<String>(symbols.keySet()));
		return (Double)crit.evaluateAsObject (symbols);
	}

	Object eval(String expr) throws Criterion.CriterionException
	{
		Criterion crit = new Criterion();
		crit.setExpression(expr, new ArrayList<String>(symbols.keySet()));
		return crit.evaluateAsObject (symbols);
	}

	boolean checkSyntax(String expr)
	{
		Criterion crit = new Criterion();
		String result = crit.setExpression(expr, new ArrayList<String>(symbols.keySet()));
		return (result == null);
	}

	public void setUp()
	{
		Logger.log.setStream(System.err);
		Logger.log.setLogLevel(true, true, true, true, true, true);
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

	public void testCalc() throws Criterion.CriterionException
	{
		assertEquals (0.0, evalDouble ("-1.0 - -1.0"), 0.01);
		assertEquals (-1.0, evalDouble ("1.0 - 2.0"), 0.01);
		assertEquals (2.0, evalDouble ("1.0 - -1.0"), 0.01);
		assertEquals (-10.0, evalDouble ("-2.5 * 4.0"), 0.01);
		assertEquals (2.0, evalDouble ("3.2 / 1.6"), 0.01);
		assertEquals (14.1, evalDouble ("3.0 * 4.0 + 2.1"), 0.01);
		assertEquals (14.1, evalDouble ("2.1 + 3.0 * 4.0"), 0.01);
		assertEquals (18.3, evalDouble ("3.0 * (4.0 + 2.1)"), 0.01);

		assertTrue (evalExpr ("2.0 + 0.01 > 2.0"));
		assertFalse (evalExpr ("2 >= 2 + 0.01 "));

	}

	public void testNot() throws Criterion.CriterionException
	{
		assertTrue  (evalExpr ("2 > 1"));
		assertFalse (evalExpr ("NOT (2 > 1)"));
		// precedence: NOT before AND
		assertFalse (evalExpr ("NOT (2 > 1) AND (1 > 2)"));
	}

	public void assertFail(String expr)
	{
		// type error:
		try {
			eval(expr);
			fail("CriterionException expected");
		}
		catch (CriterionException ex)
		{
			// success
		}
	}

	public void testFuncFail() throws CriterionException
	{
		// type errors
		assertFail ("LOG10(ARRAY(1.0))");
		assertFail ("LEN(1.0)");
		assertFail ("TTEST (1,1,1,1)");
		assertFail ("SUM(\"a\", \"b\")");
		assertFail ("TTEST (1,1,1,1)");
		assertFail ("RIGHT(1 < 2)");

		// too few arguments
		assertFail ("IF()");
		assertFail ("RIGHT()");
		assertFail ("LEN()");
		assertFail ("LOG(1)");
		assertFail ("LOG10()");

		// non-existing function
		assertFail ("NONSENSE()");
	}

	public void testStatFunc() throws CriterionException
	{
		assertEquals (2, evalDouble("AVERAGE(LOG(2, 2), LOG10(100, 0), SQRT(9))"), 0.01);
		assertEquals (2, evalDouble("SUM(1.0, 0.5, 0.25, 0.25)"), 0.01);
		assertEquals (3, evalDouble("MAX(1.0, 2.0, 3.0)"), 0.01);
		assertEquals (1, evalDouble("MIN(1.0, 2.0, 3.0)"), 0.01);
		assertEquals (2, evalDouble("SUM(SIN(0.5 * 3.14159), COS(0))"), 0.01);
		assertEquals (6, evalDouble("LOG(64, 2)"), 0.01);
		assertEquals (-1, evalDouble("IF (3 > 5, 1, -1)"), 0.01);
		assertEquals (14, evalDouble("SUMSQ(1, 2, 3)"), 0.01);
		assertEquals (0.0, evalDouble("STDEV(1, 1, 1)"), 0.01);
		assertEquals (5.0, evalDouble("STDEV(6, 8, -3, 10, 4)"), 0.01);
		assertEquals (2.0, evalDouble("STDEV(1, 3, 5)"), 0.01);
		assertEquals (0.0, evalDouble("VAR(1, 1, 1)"), 0.01);
		assertEquals (25.0, evalDouble("VAR(6, 8, -3, 10, 4)"), 0.01);
		assertEquals (4.0, evalDouble("VAR(1, 3, 5)"), 0.01);

		assertEquals(0.2319, evalDouble("TTEST(ARRAY(1,4,3,4),ARRAY(1,2,5,9),1,1)"), 0.01);
		assertEquals(0.2706, evalDouble("TTEST(ARRAY(1,4,3,4),ARRAY(1,2,5,9),1,2)"), 0.01);
		assertEquals(0.2767, evalDouble("TTEST(ARRAY(1,4,3,4),ARRAY(1,2,5,9),1,3)"), 0.01);
		assertEquals(0.4639, evalDouble("TTEST(ARRAY(1,4,3,4),ARRAY(1,2,5,9),2,1)"), 0.01);
		assertEquals(0.5413, evalDouble("TTEST(ARRAY(1,4,3,4),ARRAY(1,2,5,9),2,2)"), 0.01);
		assertEquals(0.5535, evalDouble("TTEST(ARRAY(1,4,3,4),ARRAY(1,2,5,9),2,3)"), 0.01);

		assertTrue (evalExpr("ISNUMBER(1.0) AND NOT ISNUMBER(\"hello\")"));
	}

	public void testStringFunc() throws CriterionException
	{
		assertEquals (3, evalDouble("LEN(\"abc\")"), 0.01);
		assertEquals ("e", eval("RIGHT(\"abcde\")"));
		assertEquals ("de", eval("RIGHT(\"abcde\",2)"));
		assertEquals ("bc", eval("MID(\"abcde\", 2, 2)"));
		assertEquals ("abc", eval("LEFT(\"abcde\",3)"));
		assertEquals (3.0, eval("FIND(\"ss\", \"mississippi\")"));
		assertEquals (3.0, eval("FIND(\"ss\", \"mississippi\", 3)"));
		assertEquals (6.0, eval("FIND(\"ss\", \"mississippi\", 4)"));
	}

}
