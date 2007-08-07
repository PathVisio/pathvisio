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
package org.pathvisio.gui.swing;

import javax.swing.JEditorPane;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.data.Gdb;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.SelectionBox.SelectionEvent;
import org.pathvisio.view.SelectionBox.SelectionListener;

public class BackpagePane extends JEditorPane implements SelectionListener, ApplicationEventListener {
	PathwayElement input;
	final static int maxThreads = 5;
	volatile ThreadGroup threads;
	volatile PathwayElement lastSelected;
	
	public BackpagePane() {
		super();
		setEditable(false);
		setContentType("text/html");
		
		Engine.getCurrent().addApplicationEventListener(this);
		VPathway vp = Engine.getCurrent().getActiveVPathway();
		if(vp != null) vp.addSelectionListener(this);
		
		threads = new ThreadGroup("backpage-queries");		
	}

	public void setInput(final PathwayElement e) {
		//System.err.println("===== SetInput Called ==== " + e);
		if(e == null || e.getObjectType() != ObjectType.DATANODE) {
			setText(Gdb.getBackpageHTML(null, null, null));
		} else if(input != e) {
			//System.err.println("\tSetting input " + e + " using " + threads);
			//First check if the number of running threads is not too high
			//(may happen when many SelectionEvent follow very fast)
			//System.err.println("\tNr of threads: " + threads.activeCount());
			if(threads.activeCount() < maxThreads) {
					input = e;
					QueryThread qt = new QueryThread(e);
					qt.start();
					lastSelected = null;		
			} else {
				//System.err.println("\tQueue lastSelected " + e);
				//When we're on our maximum, remember this element
				//and ignore it when a new one is selected
				lastSelected = e;
			}
		}
	}

	private void check() {
		//System.err.println("===== Check Called === " + lastSelected);
		if(lastSelected != null) {
			//System.err.println("From checked " + lastSelected);
			setInput(lastSelected);
		}
	}
	
	public void selectionEvent(SelectionEvent e) {
		switch(e.type) {
		case SelectionEvent.OBJECT_ADDED:
			//Just take the first DataNode in the selection
			for(VPathwayElement o : e.selection) {
				if(o instanceof GeneProduct) {
					setInput(((GeneProduct)o).getGmmlData());
					break; //Selects the first, TODO: use setGmmlDataObjects
				}
			}
			break;
		case SelectionEvent.OBJECT_REMOVED:
			if(e.selection.size() != 0) break;
		case SelectionEvent.SELECTION_CLEARED:
			setInput(null);
			break;
		}
	}

	public void applicationEvent(ApplicationEvent e) {
		if(e.type == ApplicationEvent.VPATHWAY_CREATED) {
			((VPathway)e.source).addSelectionListener(this);
		}
	}
	
	class QueryThread extends Thread {
		PathwayElement e;
		QueryThread(PathwayElement e) {
			super(threads, e.getGeneID() + e.hashCode());
			this.e = e;
		}
		public void run() {
			//System.err.println("+++++ Thread " + this + " started +++++");
			setText(Gdb.getBackpageHTML(
					e.getGeneID(), 
					e.getSystemCode(), 
					e.getBackpageHead()));
			check();
			//System.err.println("+++++ Thread " + this + " ended +++++");
		}
	}
}
