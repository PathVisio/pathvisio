/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.core.view;

import java.awt.geom.Point2D;

import junit.framework.TestCase;

import org.pathvisio.core.model.ConnectorType;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.preferences.PreferenceManager;

/**
 * Test various operations related to groups, such as 
 * adding to / removing from them, selecting them, etc. 
 */
public class TestGroups extends TestCase
{
	static final int DATANODE_COUNT = 10;

	public void setUp()
	{
		PreferenceManager.init();
		vpwy = new VPathway(null);
		pwy = new Pathway();
		vpwy.fromModel(pwy);

		for (int i = 0; i < DATANODE_COUNT; ++i)
		{
			dn[i] = PathwayElement.createPathwayElement(ObjectType.DATANODE);
			dn[i].setMCenterX(i * 1000);
			dn[i].setMCenterY(3000);
			dn[i].setMWidth(500);
			dn[i].setMHeight(500);
			vDn[i] = (GeneProduct)addElement (vpwy, dn[i]);
			dn[i].setGeneratedGraphId();
		}
		vLn[0] = (Line)addConnector (vpwy, dn[0], dn[1]);
		vLn[1] = (Line)addConnector (vpwy, dn[0], dn[2]);

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
		grp1 = vpwy.getPathwayModel().getGroupById(ref1);
		vGrp1 = (Group)vpwy.getPathwayElementView(grp1);
		grp1.setGeneratedGraphId();
	}

	private VPathway vpwy;
	private Pathway pwy;

	private GeneProduct[] vDn = new GeneProduct[DATANODE_COUNT];
	private Line[] vLn = new Line[2];
	private PathwayElement[] dn = new PathwayElement[DATANODE_COUNT];
	private PathwayElement grp1 = null;
	private Group vGrp1 = null;

	/** helper for adding elements to a vpathway */
	private VPathwayElement addElement(VPathway vpwy, PathwayElement pelt)
	{
		vpwy.getPathwayModel().add(pelt);

		Graphics result = vpwy.getPathwayElementView(pelt);
		assertNotNull ("PathwayElement not found through view after adding it to the model.", result);
		return result;
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
		vGrp1.select();
		vDn[2].select();
		// create a 2nd, nested group
		vpwy.toggleGroup(vpwy.getSelectedGraphics());

		String ref2 = dn[2].getGroupRef();
		assertNotNull(ref2);
		assertEquals (ref2, grp1.getGroupRef());
		PathwayElement grp2 = vpwy.getPathwayModel().getGroupById(ref2);
		Group vGrp2 = (Group)vpwy.getPathwayElementView(grp2);
	}

	public void testDrag()
	{
		vpwy.clearSelection();
		double startX = vDn[0].getVCenterX();
		vGrp1.select();
		vpwy.selection.vMoveBy(50, 50);
		double endX = vDn[0].getVCenterX();
		//TODO: make this test work
//		assertEquals (startX, endX - 50, 0.1);
	}

	public void testSelect()
	{
		//TODO: make this test work
		Point2D p1 = new Point2D.Double (vDn[0].getVCenterX()-10, vDn[0].getVCenterY()-10);
		Point2D p2 = new Point2D.Double (vDn[0].getVCenterX(), vDn[0].getVCenterY());
		vpwy.startSelecting(p1);
		vpwy.doClickSelect(p2, false);
//		assertTrue (vDn[0].isSelected());
//		assertTrue (vGrp1.isSelected());
		vpwy.startSelecting(p1);
		vpwy.doClickSelect(p2, false);
//		assertFalse (vGrp1.isSelected());
//		assertTrue (vDn[0].isSelected());
		vpwy.startSelecting(p1);
		vpwy.doClickSelect(p2, false);
//		assertTrue (vGrp1.isSelected());
	}

	/**
	 * Test aligning something that is in a group
	 */
	public void testAlign()
	{
		vDn[0].select();
		vDn[1].select();
		vpwy.layoutSelected(LayoutType.ALIGN_LEFT);
	}

	/**
	 * Test removal of a group
	 */
	public void testDelete()
	{
		Line vLn3 = (Line)addConnector (vpwy, dn[0], grp1);
		
		double oldEx = vLn3.getVEndX();
		double oldEy = vLn3.getVEndY();
		assertEquals (vGrp1.getVCenterX(), oldEx, 0.01);
		assertEquals (vGrp1.getVCenterY(), oldEy, 0.01);
		assertEquals (grp1.getGraphId(), vLn3.getPathwayElement().getEndGraphRef());
		assertNotNull (vLn3.getPathwayElement().getEndGraphRef());
		
		vpwy.clearSelection();
		vGrp1.select();
		vpwy.toggleGroup(vpwy.getSelectedGraphics());
		assertNull (dn[0].getGroupRef());
		
		// assure that line hasn't moved by deletion of group (bug #1058)
		assertEquals (oldEx, vLn3.getVEndX(), 0.01);
		assertEquals (oldEy, vLn3.getVEndY(), 0.01);
		assertNull (vLn3.getPathwayElement().getEndGraphRef());
		
	}

}
