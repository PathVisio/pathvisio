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
package org.pathvisio.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.data.CachedData.Data;
import org.pathvisio.data.DataException;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.GdbManager;
import org.pathvisio.data.GexManager;
import org.pathvisio.data.Sample;
import org.pathvisio.data.SimpleGex;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayElementListener;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.Xref;
import org.pathvisio.util.Resources;
import org.pathvisio.util.Utils;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.SelectionBox.SelectionEvent;
import org.pathvisio.view.SelectionBox.SelectionListener;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;

/**
 * This class fetches and distributes the backpage text to all registered
 * listeners when needed (e.g. a datanode is selected). 
 * @author thomas
 */
public class BackpageTextProvider implements ApplicationEventListener, SelectionListener, PathwayElementListener 
{
	PathwayElement input;
	final static int MAX_THREADS = 1;
	volatile ThreadGroup threads;
	volatile Thread lastThread;
	
	private GdbManager gdbManager;
	private GexManager gexManager;
	private Engine engine;
	
	public BackpageTextProvider(Engine engine, GdbManager gdbManager, GexManager gexManager) 
	{
		initializeHeader();
		engine.addApplicationEventListener(this);
		VPathway vp = engine.getActiveVPathway();
		if(vp != null) vp.addSelectionListener(this);
	
		this.engine = engine;
		this.gdbManager = gdbManager;
		this.gexManager = gexManager;
	}

	public void setInput(final PathwayElement e) 
	{
		//System.err.println("===== SetInput Called ==== " + e);
		if(e == input) return; //Don't set same input twice
		
		//Remove pathwaylistener from old input
		if(input != null) input.removeListener(this);
		
		if(e == null || e.getObjectType() != ObjectType.DATANODE) {
			input = null;
			SimpleGex gex = gexManager.getCurrentGex();
			setText(getBackpageHTML(gdbManager.getCurrentGdb(), gex, null));
		} else {
			input = e;
			input.addListener(this);
			doQuery();
		}
	}

	private void doQuery() 
	{
		currRef = input.getXref();
		
		//System.err.println("\tSetting input " + e + " using " + threads);
		//First check if the number of running threads is not too high
		//(may happen when many SelectionEvent follow very fast)
//		System.err.println("\tNr of threads: " + threads.activeCount());
		if(threads == null || threads.isDestroyed()) {
			threads = new ThreadGroup("backpage-queries" + System.currentTimeMillis());
		}
		if(threads.activeCount() < MAX_THREADS) {
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
			Iterator<VPathwayElement> it = e.selection.iterator();
			while(it.hasNext()) {
				VPathwayElement o = it.next();
				if(o instanceof GeneProduct) {
					setInput(((GeneProduct)o).getPathwayElement());
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

	public void applicationEvent(ApplicationEvent e) 
	{
		switch (e.getType())
		{
			case ApplicationEvent.VPATHWAY_CREATED:
			{
				((VPathway)e.getSource()).addSelectionListener(this);
			}
			break;
			case ApplicationEvent.VPATHWAY_DISPOSED:
			{
				((VPathway)e.getSource()).removeSelectionListener(this);
			}
		}
	}
	
	Xref currRef;
	
	public void gmmlObjectModified(PathwayEvent e) {
		PathwayElement pe = e.getAffectedData();
		if(input != null) {
			Xref nref = new Xref (pe.getGeneID(), input.getDataSource());
			if(!nref.equals(currRef)) 
			{
				doQuery();
			}				
		}
	}
	
	private class QueryThread extends Thread {
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
//			System.err.println("+++++ Thread " + this + " ended +++++");
		}
		void performTask() 
		{
			if(e == null) return;
			SimpleGex gex = gexManager.getCurrentGex();
			String txt = getBackpageHTML(gdbManager.getCurrentGdb(), gex, e);
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
	
	/**
	 * returs backpage html containing the gene information
	 * from the first child that has it,
	 * and the crossref table composed of all child results.
	 */
	private String getBackpageHTML(Gdb gdb, SimpleGex gex, PathwayElement e) 
	{
		String bpHead = (e == null ? "" : e.getBackpageHead());
		Xref ref = (e == null ? null : e.getXref());
		// type will be displayed in the header, make either "Metabolite" or "Gene";
		String type = (e == null ? "" : e.getDataNodeType()); 
		if (!type.equals("Metabolite"))
		{
			type = "Gene";
		}
		String text = getBackpagePanelHeader();
		if (text == null) text = "";
		
		if( ref == null || ref.getId() == null || ref.getDataSource() == null) return text;
		
		if (bpHead == null) bpHead = "";
		text += "<H1>" + type + " information</H1><P>";
		text += bpHead.equals("") ? bpHead : "<H2>" + bpHead + "</H2><P>";
	
		// find first gene database that has non-null bpInfo.
		String bpInfo;
		try
		{
			bpInfo = gdb.getBpInfo(ref);
		}
		catch (DataException ex)
		{
			bpInfo = "Exception occurred, see log for details</br>";
			Logger.log.error ("Error fetching backpage info", ex);
		}
		
		text += bpInfo == null ? "<I>No " + type + " information found</I>" : bpInfo;

		//Get the expression data information if available
		if(gex != null) {
			text += "<H1>Expression data</H1>";
			text += getDataString(ref, gdb, gex);
		}
		
		try
		{
			text += getCrossRefText (gdb, ref);
		}
		catch (DataException ex)
		{
			text += "Exception occured while getting cross-references</br>" 
				+ ex.getMessage();
		}

		return text + "</body></html>";
	}

	/**
	 * Header file, containing style information
	 */
	final private static String HEADERFILE = "header.html";

	private String backpagePanelHeader;

	private String getBackpagePanelHeader()
	{
		return backpagePanelHeader;
	}

	/**
	 * Reads the header of the HTML content displayed in the browser. This header is displayed in the
	 * file specified in the {@link HEADERFILE} field
	 */
	private void initializeHeader() 
	{
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(
					Resources.getResourceURL(HEADERFILE).openStream()));
			String line;
			backpagePanelHeader = "";
			while((line = input.readLine()) != null) {
				backpagePanelHeader += line.trim();
			}
		} catch (Exception e) {
			Logger.log.error("Unable to read header file for backpage browser: " + e.getMessage(), e);
		}
	}	

	/**
	 * Gets all available expression data for the given gene id and returns a string
	 * containing this data in a HTML table
	 * @param idc	the {@link Xref} containing the id and code of the geneproduct to look for
	 * @return		String containing the expression data in HTML format or a string displaying a
	 * 'no expression data found' message in HTML format
	 */
	private String getDataString(Xref idc, Gdb gdb, SimpleGex gex)
	{
		String noDataFound = "<P><I>No expression data found";
		String exprInfo = "<P><B>Gene id on mapp: " + idc.getId() + "</B><TABLE border='1'>";
		
		String colNames = "<TR><TH>Sample name";
		if(!gex.isConnected()) return noDataFound;
		
		List<Data> pwData = gex.getCachedData(idc);
		
		if(pwData == null) return noDataFound;
		
		for(Data d : pwData){
			colNames += "<TH>" + d.getXref().getId();
		}
		
		String dataString = "";
		for(Sample s : gex.getSamples().values())
		{
			dataString += "<TR><TH>" + s.getName();
			for(Data d : pwData)
			{
				dataString += "<TH>" + d.getSampleData(s.getId());
			}
		}
		
		return exprInfo + colNames + dataString + "</TABLE>";
	}
	
	private String getCrossRefText(Gdb gdb, Xref ref) throws DataException 
	{
		List<Xref> crfs = gdb.getCrossRefs(ref);
		if(crfs.size() == 0) return "";
		StringBuilder crt = new StringBuilder("<H1>Cross references</H1><P>");
		for(Xref cr : crfs) {
			String idtxt = cr.getId();
			String url = cr.getUrl();
			if(url != null) {
				int os = Utils.getOS();
				if(os == Utils.OS_WINDOWS) {
					//In windows: open in new browser window
					idtxt = "<a href='" + url + "' target='_blank'>" + idtxt + "</a>";
				} else {
					//This doesn't work under ubuntu, so no new windoe there
					idtxt = "<a href='" + url + "'>" + idtxt + "</a>";
				}

			}
			String dbName = cr.getDataSource().getFullName();
			crt.append( idtxt + ", " + (dbName != null ? dbName : cr.getDataSource().getSystemCode()) + "<br>");
		}
		return crt.toString();
	}

	private boolean disposed = false;
	public void dispose()
	{
		assert (!disposed);
		engine.removeApplicationEventListener(this);
		disposed = true;
	}
	
}
