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
package org.pathvisio.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.PathwayListener;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.SelectionBox.SelectionEvent;
import org.pathvisio.view.SelectionBox.SelectionListener;

public class BackpageTextProvider implements ApplicationEventListener, SelectionListener, PathwayListener {
	PathwayElement input;
	final static int maxThreads = 1;
	volatile ThreadGroup threads;
	volatile Thread lastThread;
	
	public BackpageTextProvider() {		
		Engine.getCurrent().addApplicationEventListener(this);
		VPathway vp = Engine.getCurrent().getActiveVPathway();
		if(vp != null) vp.addSelectionListener(this);
		
		threads = new ThreadGroup("backpage-queries");		
	}

	public void setInput(final PathwayElement e) {
		//System.err.println("===== SetInput Called ==== " + e);
		if(e == input) return; //Don't set same input twice
		
		//Remove pathwaylistener from old input
		if(input != null) input.removeListener(this);
		
		if(e == null || e.getObjectType() != ObjectType.DATANODE) {
			setText(Gdb.getBackpageHTML(null, null, null));
		} else {
			input = e;
			doQuery();
		}
	}

	private void doQuery() {
		//System.err.println("\tSetting input " + e + " using " + threads);
		//First check if the number of running threads is not too high
		//(may happen when many SelectionEvent follow very fast)
//		System.err.println("\tNr of threads: " + threads.activeCount());
		if(threads.activeCount() < maxThreads) {
				QueryThread qt = new QueryThread(input);
				qt.start();
				lastThread = qt;		
		} else {
//			System.err.println("\tQueue lastSelected " + input);
			//When we're on our maximum, remember this element
			//and ignore it when a new one is selected
		}

	}
	
	public void selectionEvent(SelectionEvent e) {
		switch(e.type) {
		case SelectionEvent.OBJECT_ADDED:
			//Just take the first DataNode in the selection
			for(int i = e.selection.size() - 1; i > -1; i--) {
				VPathwayElement o = e.selection.get(i);
				if(o instanceof GeneProduct) {
					setInput(((GeneProduct)o).getGmmlData());
					break; //Selects the last, TODO: use setGmmlDataObjects
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
	
	public void gmmlObjectModified(PathwayEvent e) {
		PathwayElement pe = e.getAffectedData();
		if(input != null) {
			String oId = input.getGeneID();
			String nId = pe.getGeneID();
			String nC = input.getSystemCode();
			String oC = input.getSystemCode();
			if(	oId != null && !oId.equals(nId) ||
				oC != null && !oC.equals(nC)) {
				doQuery();
			}				
		}
	}
	
	class QueryThread extends Thread {
		PathwayElement e;
		QueryThread(PathwayElement e) {
			super(threads, e.getGeneID() + e.hashCode());
			this.e = e;
		}
		public void run() {
//			System.err.println("+++++ Thread " + this + " started +++++");
			performTask();
			if(this.equals(lastThread) && input != e) {
//				System.err.println("Updating");
				e = input;
				performTask();
				lastThread = null;
			}
		}
		void performTask() {
			String txt = Gdb.getBackpageHTML(
					e.getGeneID(), 
					e.getSystemCode(), 
					e.getBackpageHead());
			if(input == e) setText(txt);
		}
	}
	
	String text;
	
	private void setText(String newText) {
		text = newText;
		for(BackpageListener l : listeners) {
			l.textChanged(text, newText);
		}
	}
	
	Set<BackpageListener> listeners = new HashSet<BackpageListener>();
	
	public void addListener(BackpageListener l) {
		listeners.add(l);
	}
	
	public void removeListener(BackpageListener l) {
		listeners.remove(l);
	}
}
