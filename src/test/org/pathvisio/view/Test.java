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

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

//import org.eclipse.swt.SWT;
//import org.eclipse.swt.layout.FillLayout;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Shell;

//import org.pathvisio.model.Pathway;
//import org.pathvisio.model.PathwayElement;
//import org.pathvisio.model.ObjectType;

public class Test extends TestCase {
	
	VPathway drawing;
//    private Shell shell;
	
	public void setUp()
	{
		//~ shell = new Shell(Display.getDefault());
        //~ shell.setLayout(new FillLayout());
        //~ drawing = new VPathway(shell, SWT.NO_BACKGROUND);
	}

    protected void tearDown() throws Exception {
        //~ shell.dispose();
    }
    
	//~ public void testInit()
	//~ {
		//~ Pathway data = new Pathway();
		//~ drawing.fromGmmlData(data);
		//~ assertEquals (drawing.getGmmlData(), data);
		
		//~ data.add(new PathwayElement(ObjectType.DATANODE));
	//~ }
    
    public void testDrawingOrder() {
    	Pathway p = new Pathway();
    	PathwayElement d1 = new PathwayElement(ObjectType.DATANODE);
    	PathwayElement s1 = new PathwayElement(ObjectType.SHAPE);
    	PathwayElement s2 = new PathwayElement(ObjectType.SHAPE);
    	PathwayElement s3 = new PathwayElement(ObjectType.SHAPE);
    	p.add(d1);
    	p.add(s1);
    	p.add(s2);
    	p.add(s3);
    	
    	assertTrue(d1.getZOrder() == s1.getZOrder() - 1);
    	assertTrue(s1.getZOrder() == s2.getZOrder() - 1);
    	
    	VPathway vp = new VPathway(null);
    	vp.fromGmmlData(p);
    	
    	VPathwayElement gd1, gs1, gs2, gs3;
    	gd1 = null;
    	gs1 = null;
    	gs2 = null;
    	gs3 = null;
    	
    	List<VPathwayElement> elements = vp.getDrawingObjects();
    	for(VPathwayElement e : elements) {
    		if(e instanceof Graphics) {
    			PathwayElement pe = ((Graphics)e).getGmmlData();
    			if			(pe == d1) {
    				gd1 = e;
    			} else if 	(pe == s1) {
    				gs1 = e;
    			} else if	(pe == s2) {
    				gs2 = e;
    			} else if	(pe == s3) {
    				gs3 = e;
    			}
    		}
    	}
    	
    	assertFalse(gd1 == null);
    	assertFalse(gs1 == null);
    	assertFalse(gs2 == null);
    	assertFalse(gs3 == null);
    	
    	VPathwayElement hs2 = gs2.getHandles()[0];
    	
    	//Test natrual / z order
    	Collections.sort(elements);    	    	
    	checkDrawingOrder(new VPathwayElement[] { gs1, gs2, gs3, gd1, hs2 }, elements);
    	
    	//Test selected order
    	gs2.select();
    	Collections.sort(elements);
    	checkDrawingOrder(new VPathwayElement[] { gs1, gs3, gd1, gs2, hs2 }, elements);
    	
    	//Test reset after unselected
    	gs2.deselect();
    	Collections.sort(elements);
    	checkDrawingOrder(new VPathwayElement[] { gs1, gs2, gs3, gd1, hs2 }, elements);
    	
    }
    
    public void checkDrawingOrder(VPathwayElement[] order, List<VPathwayElement> elements) {
    	int[] indices = new int[order.length];
    	for(int i = 0; i < order.length; i++) {
    		indices[i] = elements.indexOf(order[i]);
    	}
    	for(int i = 0; i < indices.length  - 1; i++) {
    		assertTrue("Element " + order[i] + "(" + indices[i] + ") should be below element " + order[i+1] + "(" + indices[i+1] + ")", 
    				indices[i] < indices[i+1]);
    	}
    }
}
