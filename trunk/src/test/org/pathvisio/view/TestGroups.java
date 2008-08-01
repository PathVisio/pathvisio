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
package org.pathvisio.view;

import org.pathvisio.Engine;
import org.pathvisio.model.ConnectorType;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

import junit.framework.TestCase;

public class TestGroups extends TestCase 
{
	static final int DATANODE_COUNT = 10;
	
	public void setUp()
	{
		Engine.init();
		vpwy = new VPathway(null);
		pwy = new Pathway();
		vpwy.fromGmmlData(pwy);
		
		for (int i = 0; i < DATANODE_COUNT; ++i)
		{	
			dn[i] = PathwayElement.createPathwayElement(ObjectType.DATANODE);
			dn[i].setMCenterX(i * 400);
			dn[i].setMCenterY(1000);
			vDn[i] = (GeneProduct)addElement (vpwy, dn[i]);
		}
		vLn[0] = (Line)addConnector (vpwy, dn[0], dn[1]);
		vLn[1] = (Line)addConnector (vpwy, dn[0], dn[2]);
	}
	
	private VPathway vpwy;
	private Pathway pwy;
	
	private GeneProduct[] vDn = new GeneProduct[DATANODE_COUNT];
	private Line[] vLn = new Line[2];
	private PathwayElement[] dn = new PathwayElement[DATANODE_COUNT];
	
	/** helper for adding elements to a vpathway */
	private VPathwayElement addElement(VPathway vpwy, PathwayElement pelt)
	{
		vpwy.getPathwayModel().add(pelt);
		
		for (VPathwayElement ve : vpwy.getDrawingObjects())
		{
			if (ve instanceof Graphics && ((Graphics)ve).getGmmlData() == pelt)
			{
				return ve;
			}
		}
		fail("PathwayElement not found through view after adding it to the model.");
		return null;
	}
	
	/** helper for adding connectors to a vpathway */
	private VPathwayElement addConnector (VPathway vpwy, PathwayElement l1, PathwayElement l2)
	{
		PathwayElement elt = PathwayElement.createPathwayElement(ObjectType.LINE);
		elt.setConnectorType(ConnectorType.ELBOW);
		elt.setStartGraphRef(l1.getGraphId());
		elt.setEndGraphRef(l2.getGraphId());
		elt.setMStartX(l1.getMCenterX());
		elt.setMStartY(l1.getMCenterY());
		elt.setMEndX(l2.getMCenterX());
		elt.setMEndY(l2.getMCenterY());
		
		return addElement (vpwy, elt);
	}
	
	public void testNesting()
	{
		vpwy.clearSelection();
		assertNull (dn[0].getGroupRef());
		assertNull (dn[1].getGroupRef());
		vDn[0].select();
		vDn[1].select();
		vLn[0].select();
		// create a group
		vpwy.toggleGroup(vpwy.getSelectedGraphics());
		String ref1 = dn[0].getGroupRef();
		assertNotNull(ref1);
		assertEquals (ref1, dn[1].getGroupRef());
		PathwayElement grp1 = vpwy.getPathwayModel().getGroupById(ref1);
		
		Group vg = (Group)vpwy.getPathwayElementView(grp1);
		
		vpwy.clearSelection();
		vg.select();
		vDn[2].select();
		// create a 2nd, nested group
		vpwy.toggleGroup(vpwy.getSelectedGraphics());
		
	}
	
	public void testDrag()
	{
//		vGrp[0].move (50, 50);
//		assertEquals (dn[0].getX(), 0);
	}
	
	public void testSelect()
	{
//		selectObjectAt (100, 100);
//		assertTrue (vGrp[0].isSelected());
//		selectObjectAt (100, 100);
//		assertFalse (vGrp[0].isSelected());
//		assertTrue (vDn[0].isSelected());
//		selectObjectAt (100, 100);
//		assertTrue (vGrp[0].isSelected());
	}
		
	public void testAlign()
	{
	}
	
	public void testDelete()
	{
	}
	
	public void testCopy()
	{
	}
	
	public void testUndo()
	{
	}
	
}
