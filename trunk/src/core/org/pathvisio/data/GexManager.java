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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.colorset.ColorSetManager;

public class GexManager 
{
	private static SimpleGex currentGex = null; 
	public static SimpleGex getCurrentGex() { return currentGex; }
	
	
	public static void setCurrentGex (SimpleGex gex, boolean fireEvent)
	{
		if (currentGex != null) currentGex.close();
		currentGex = gex;
		if(fireEvent)
			fireExpressionDataEvent(new ExpressionDataEvent(SimpleGex.class, ExpressionDataEvent.CONNECTION_OPENED));
		loadXML();
	}
	
	public static void setCurrentGex (String dbName, boolean create, boolean fireEvent) throws DataException
	{
		DBConnector connector;
		try
		{
			connector = getDBConnector();		
		}
		catch (IllegalAccessException e)
		{
			throw new DataException (e);
		}
		catch (InstantiationException e)
		{
			throw new DataException (e);
		}
		catch (ClassNotFoundException e)
		{
			throw new DataException (e);
		}
		SimpleGex gex = new SimpleGex (dbName, create, connector);
		setCurrentGex (gex, fireEvent);
	}
	
	private static DBConnector getDBConnector() throws 
		ClassNotFoundException, 
		InstantiationException, 
		IllegalAccessException 
	{
		return Engine.getCurrent().getDbConnector(DBConnector.TYPE_GEX);
	}

	
	public static void close()
	{
		saveXML();
		currentGex.close(false);
		fireExpressionDataEvent(new ExpressionDataEvent(SimpleGex.class, ExpressionDataEvent.CONNECTION_CLOSED));	
	}
	
	/**
	 * Fire a {@link ExpressionDataEvent} to notify all {@link ExpressionDataListener}s registered
	 * to this class
	 * @param e
	 */
	protected static void fireExpressionDataEvent(ExpressionDataEvent e) 
	{
		for(ExpressionDataListener l : listeners) l.expressionDataEvent(e);
	}
	
	public interface ExpressionDataListener 
	{
		public void expressionDataEvent(ExpressionDataEvent e);
	}
	
	static List<ExpressionDataListener> listeners;
	
	/**
	 * Add a {@link ExpressionDataListener}, that will be notified if an
	 * event related to expression data occurs
	 * @param l The {@link ExpressionDataListener} to add
	 */
	public static void addListener(ExpressionDataListener l) 
	{
		if(listeners == null) listeners = new ArrayList<ExpressionDataListener>();
		listeners.add(l);
	}
	
	public static class ExpressionDataEvent extends EventObject 
	{
		private static final long serialVersionUID = 1L;
		public static final int CONNECTION_OPENED = 0;
		public static final int CONNECTION_CLOSED = 1;

		public Object source;
		public int type;
		
		public ExpressionDataEvent(Object source, int type) {
			super(source);
			this.source = source;
			this.type = type;
		}
	}
	
	public static final String XML_ELEMENT = "expression-data-visualizations";

	public static InputStream getXmlInput()
	{
		File xmlFile = new File(currentGex.getDbName() + ".xml");
		try {
			if(!xmlFile.exists()) xmlFile.createNewFile();
			InputStream in = new FileInputStream(xmlFile);
			return in;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static OutputStream getXmlOutput() {
		try {
			File f = new File(currentGex.getDbName() + ".xml");
			OutputStream out = new FileOutputStream(f);
			return out;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void saveXML() {
		if(!currentGex.isConnected()) return;
		
		OutputStream out = getXmlOutput();
		
		Document xmlDoc = new Document();
		Element root = new Element(XML_ELEMENT);
		xmlDoc.setRootElement(root);
		
		root.addContent(VisualizationManager.getNonGenericXML());
		root.addContent(ColorSetManager.getXML());
		
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
		try {
			xmlOut.output(xmlDoc, out);
			out.close();
		} catch(IOException e) {
			Logger.log.error("Unable to save visualization settings", e);
		}
	}
	
	public static void loadXML() {
		Document doc = getXML();
		Element root = doc.getRootElement();
		Element vis = root.getChild(VisualizationManager.XML_ELEMENT);
		VisualizationManager.loadNonGenericXML(vis);
		Element cs = root.getChild(ColorSetManager.XML_ELEMENT);
		ColorSetManager.fromXML(cs);
	}
	
	public static Document getXML() {
		InputStream in = getXmlInput();
		Document doc;
		Element root;
		try {
			SAXBuilder parser = new SAXBuilder();
			doc = parser.build(in);
			in.close();
			
			root = doc.getRootElement();
		} catch(Exception e) {
			doc = new Document();
			root = new Element(XML_ELEMENT);
			doc.setRootElement(root);
			
		}		
		return doc;
	}
	
}
