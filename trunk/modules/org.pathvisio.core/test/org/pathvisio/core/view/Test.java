// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.core.view;

//import java.awt.Toolkit;
//import java.awt.datatransfer.Clipboard;
//import java.awt.datatransfer.DataFlavor;
//import java.awt.datatransfer.UnsupportedFlavorException;
//import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.pathvisio.core.biopax.BiopaxReferenceManager;
import org.pathvisio.core.biopax.PublicationXref;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.preferences.PreferenceManager;

public class Test extends TestCase {

	Pathway pwy = null;
	VPathway vPwy = null;
	PathwayElement eltDn = null, eltSh = null, eltLi = null, eltLa = null;
	Graphics vDn = null, vSh = null, vLi = null, vLa = null;

	public void setUp()
	{
		PreferenceManager.init();
    	pwy = new Pathway();
    	eltDn = PathwayElement.createPathwayElement(ObjectType.DATANODE);
    	eltDn.setMCenterX(3000);
    	eltDn.setMCenterY(3000);
    	eltDn.setGeneID("1234");
    	eltDn.setTextLabel("Gene");
    	eltDn.setMWidth(1000);
    	eltDn.setMHeight(1000);
    	eltSh = PathwayElement.createPathwayElement(ObjectType.SHAPE);
    	eltSh.setMCenterX(6000);
    	eltSh.setMCenterY(3000);
    	eltSh.setMWidth(300);
    	eltSh.setMHeight(700);
    	eltLi = PathwayElement.createPathwayElement(ObjectType.LINE);
    	eltLi.setMStartX(500);
    	eltLi.setMStartY(1000);
    	eltLi.setMEndX(2500);
    	eltLi.setMEndY(4000);
    	eltLa = PathwayElement.createPathwayElement(ObjectType.LABEL);
    	eltLa.setMCenterX(6000);
    	eltLa.setMCenterY(6000);
    	eltLa.setMWidth(300);
    	eltLa.setMHeight(700);
    	eltLa.setTextLabel("Test");
    	pwy.add(eltDn);
    	pwy.add(eltSh);
    	pwy.add(eltLi);
    	pwy.add(eltLa);
    	vPwy = new VPathway(null);
    	vPwy.fromModel(pwy);

    	for(VPathwayElement e : vPwy.getDrawingObjects())
    	{
    		if(e instanceof Graphics) {
    			PathwayElement pe = ((Graphics)e).getPathwayElement();
    			if			(pe == eltDn) {
    				vDn = (Graphics)e;
    			} else if 	(pe == eltSh) {
    				vSh = (Graphics)e;
    			} else if	(pe == eltLi) {
    				vLi = (Graphics)e;
    			} else if	(pe == eltLa) {
    				vLa = (Graphics)e;
    			}
    		}
    	}

    	assertFalse(vDn == null);
    	assertFalse(vSh == null);
    	assertFalse(vLi == null);
    	assertFalse(vLa == null);
	}

	public void testCopyPaste()
	{
    	Pathway pTarget = new Pathway();
    	VPathway vpTarget = new VPathway(null);
    	vpTarget.fromModel(pTarget);

		vPwy.selectObject(vDn);
		vPwy.copyToClipboard();

		vpTarget.pasteFromClipboard();

		PathwayElement pasted = null;
		for(PathwayElement e : pTarget.getDataObjects()) {
			if("1234".equals(e.getGeneID())) {
				pasted = e;
			}
		}
		//TODO: does not work if VPathwayWrapper is not VPathwaySwing.
//		assertNotNull(pasted);

		//Now copy mappinfo
//		PathwayElement info = pSource.getMappInfo();
//		info.setMapInfoName("test pathway");
//		vpSource.selectObject(vpSource.getPathwayElementView(info));
//		vpSource.copyToClipboard();

//		vpTarget.pasteFromClipboard();

		//test if mappinfo has been pasted to the target pathway
//		assertTrue("test pathway".equals(pTarget.getMappInfo().getMapInfoName()));
    }

    public void testOrderAction()
    {
    	assertTrue(eltDn.getZOrder() > eltLa.getZOrder());
    	assertTrue(eltLa.getZOrder() > eltSh.getZOrder());
    	assertTrue(eltSh.getZOrder() > eltLi.getZOrder());

    	vPwy.moveGraphicsTop(Arrays.asList(new Graphics[] {vLa}));
    	vPwy.moveGraphicsBottom(Arrays.asList(new Graphics[] {vSh}));

    	assertTrue(eltLa.getZOrder() > eltDn.getZOrder());
    	assertTrue(eltDn.getZOrder() > eltLi.getZOrder());
    	assertTrue(eltLi.getZOrder() > eltSh.getZOrder());
    }

    /**
     * Test sorting of vpathway elements
     *
     * handles should be on top.
     * VPoints should be below handles
     * Any Graphics type should be below non-Graphics types
     */
    public void testVpwySort()
    {
    	assertTrue(eltDn.getZOrder() > eltSh.getZOrder());
    	assertTrue(eltSh.getZOrder() > eltLi.getZOrder());

    	vDn.select();
    	VPathwayElement h = ((GeneProduct)vDn).getHandles()[0];
//    	VPoint pnt = ((Line)vLi).getEnd();

    	vPwy.addScheduled();
    	List<VPathwayElement> elements = vPwy.getDrawingObjects();

    	//Test natural / z order
    	Collections.sort(elements);
    	checkDrawingOrder(new VPathwayElement[] { vLi, vSh, vLa, vDn, h }, elements);

    	//order should not change when selecting
    	vLi.select();
    	Collections.sort(elements);
    	checkDrawingOrder(new VPathwayElement[] { vLi, vSh, vLa, vDn, h }, elements);

    	//Test reset after unselected
    	vLi.deselect();
    	Collections.sort(elements);
    	checkDrawingOrder(new VPathwayElement[] { vLi, vSh, vLa, vDn, h }, elements);
    }

    public void checkDrawingOrder(VPathwayElement[] order, List<VPathwayElement> elements) {
    	int[] indices = new int[order.length];
    	for(int i = 0; i < order.length; i++) {
    		indices[i] = elements.indexOf(order[i]);
    	}
    	for(int i = 0; i < indices.length  - 1; i++) {
    		assertTrue("Element " + i + "(" + indices[i] + ") should be below element " + (1+i) + "(" + indices[i+1] + ")",
    				indices[i] < indices[i+1]);
    	}
    }

    public void testDelete()
    {
    	assertTrue (vPwy.getDrawingObjects().contains(vSh));
    	assertTrue (pwy.getDataObjects().contains(eltSh));

    	vPwy.removeDrawingObject(vSh, true);

    	assertFalse (vPwy.getDrawingObjects().contains(vSh));
    	assertFalse (pwy.getDataObjects().contains(eltSh));

    	assertTrue (vPwy.getDrawingObjects().contains(vLa));
    	assertTrue (pwy.getDataObjects().contains(eltLa));
    	assertTrue (vPwy.getDrawingObjects().contains(vLi));
    	assertTrue (pwy.getDataObjects().contains(eltLi));

    	vPwy.removeDrawingObjects (Arrays.asList(new VPathwayElement[] { vLa, vLi } ), true);
    	
    	assertFalse (vPwy.getDrawingObjects().contains(vLa));
    	assertFalse (pwy.getDataObjects().contains(eltLa));
    	assertFalse (vPwy.getDrawingObjects().contains(vLi));
    	assertFalse (pwy.getDataObjects().contains(eltLi));

    	assertTrue (vPwy.getDrawingObjects().contains(vDn));
    	assertTrue (pwy.getDataObjects().contains(eltDn));

    	vPwy.removeDrawingObjects (Arrays.asList(new VPathwayElement[] { vDn } ));
    	
    	assertFalse (vPwy.getDrawingObjects().contains(vDn));
    	assertTrue (pwy.getDataObjects().contains(eltDn));
    }

    public void testUndoAction()
    {
    	//TODO
    }

    public void testGroupingAction()
    {
    	//TODO
    }

    public void testSelection()
    {
    	//TODO
    }

    public void testNewAction()
	{
    	//TODO
	}

    public void testNudgeAction()
    {
    	//TODO
    }

    public void testAddAnchorAction()
    {
    	//TODO
    }

    public void testConnector()
    {
    	//TODO
    }

    public void testLitRef()
    {
    	// test that addition of a reference in the model leads to the creation of a Citation object
    	// in the view.
    	// See also bug 855: http://www.bigcat.unimaas.nl/tracprojects/pathvisio/ticket/855

    	assertNull (vDn.getCitation());

		BiopaxReferenceManager m = eltDn.getBiopaxReferenceManager();
		PublicationXref cit = new PublicationXref();
		cit.setPubmedId("18651794"); // Just a dummy value, no query is sent
		m.addElementReference(cit);

    	assertNotNull (vDn.getCitation());
    	assertEquals (vDn.getCitation().getRefMgr().getPublicationXRefs().get(0).getPubmedId(),
    			"18651794");

    	// now remove it again
    	m.removeElementReference(cit);

    	assertNull (vDn.getCitation());
    }

}
