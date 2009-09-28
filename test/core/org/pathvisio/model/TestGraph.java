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
package org.pathvisio.model;

import java.io.File;

import junit.framework.TestCase;
import org.pathvisio.model.PathwayElement.MPoint;

/**
 * test graph properties of a pathway model
 */
public class TestGraph extends TestCase
{
	Pathway p;
	PathwayElement l, n1, n2;
	MPoint start, end;
	
	public void setUp()
	{
		p = new Pathway();	
		
		l = new PathwayElement (ObjectType.LINE);

		l.setMStartX(11.0);
		l.setMStartY(9.0);
		l.setMEndX(51.0);
		l.setMEndY(49.0);
		p.add (l);
		
		start = l.getMStart();
		end = l.getMEnd();

		n1 = new PathwayElement (ObjectType.DATANODE);
		n1.setMCenterX(10.0);
		n1.setMCenterY(10.0);
		n1.setMWidth (5.0);
		n1.setMHeight (5.0);
		p.add (n1);
		n1.setGeneratedGraphId();

		n2 = new PathwayElement (ObjectType.DATANODE);
		n2.setMCenterX(50.0);
		n2.setMCenterY(50.0);
		n2.setMWidth (5.0);
		n2.setMHeight (5.0);
		p.add (n2);
		n2.setGeneratedGraphId();
	}
	
	/**
	 * test that the isRelative() method on mPoint properly
	 * reflects the fact that that the mPoint is linked to an object or not.
	 */
	public void testRelative()
	{
		assertFalse (start.isRelative());
		assertFalse (end.isRelative());
		
		l.setStartGraphRef(n1.getGraphId());

		assertTrue (start.isRelative());
		assertFalse (end.isRelative());
		
		l.setStartGraphRef(null);
		l.setEndGraphRef(n2.getGraphId());
		
		assertFalse (start.isRelative());
		assertTrue (end.isRelative());
		
		String n2id = n2.getGraphId();
		assertTrue (p.getGraphIds().contains (n2id));
		
		// re-generate id
		n2.setGeneratedGraphId();

		assertFalse (p.getGraphIds().contains (n2id));
		
		assertFalse (start.isRelative());
		assertFalse (end.isRelative());

	}

	/**
	 * test that, if a line points to a node, and the node is removed,
	 * the line is properly unlinked.
	 */
	public void testRemove()
	{
		assertFalse (start.isRelative());
		assertEquals (11.0, start.getX(), 0.01);
		
		// link start to n1
		start.linkTo (n1, -1.0, -1.0);
		
		assertTrue (start.isRelative());
		assertEquals (7.5, start.getX(), 0.01);
		assertEquals (-1.0, start.getRelX(), 0.01);
		
		// remove n1
		p.remove(n1);
		
		assertFalse (start.isRelative());
		assertEquals (7.5, start.getX(), 0.01);
	}
	
	/**
	 * The file under test has some points specified in absolute coordinates,
	 * and some specified in relative coordinates.
	 * 
	 * There is a datanode, initialized before the lines, and a shape, initialized after the lines.
	 * 
	 * Test that the coordinates are properly calculated.
	 */
	public void testAbsRelMPoint() throws ConverterException
	{
		File fTest = new File ("testData/mpoint-test.gpml");
		assertTrue (fTest.exists());
		Pathway q = new Pathway();
		q.readFromXml(fTest, true);
		
		PathwayElement base1 = q.getElementById("bad0f");
		PathwayElement base2 = q.getElementById("da8cb");
		
		PathwayElement[] l = new PathwayElement[4];
		for (int i = 0; i < 4; ++i)
		{	
			l[i] = q.getElementById("l" + (i + 1));
			assertEquals (1750.0, l[i].getMEndX(), 0.01); 
			assertEquals (2000.0, l[i].getMEndY(), 0.01);
			assertTrue (l[i].getMEnd().isRelative());
			assertFalse (l[i].getMStart().isRelative());
		}
		assertEquals (3000.0, l[0].getMStartX());
		assertEquals (800.0, l[0].getMStartY());
		
		base1.setMCenterX(1850.0);
		base2.setMCenterX(1850.0);

		for (int i = 0; i < 4; ++i)
		{	
			assertEquals (1850.0, l[i].getMEndX(), 0.01); 
			assertEquals (2000.0, l[i].getMEndY(), 0.01);
		}
		assertEquals (3000.0, l[0].getMStartX());
		assertEquals (800.0, l[0].getMStartY());
	}
}
