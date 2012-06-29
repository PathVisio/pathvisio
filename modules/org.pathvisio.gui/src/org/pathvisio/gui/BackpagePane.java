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
package org.pathvisio.gui;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;

import org.bridgedb.Xref;
import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PathwayElementEvent;
import org.pathvisio.core.model.PathwayElementListener;
import org.pathvisio.core.model.StaticProperty;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.GraphicsShape;
import org.pathvisio.core.view.SelectionBox.SelectionEvent;
import org.pathvisio.core.view.SelectionBox.SelectionListener;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;

/**
 * The backpage panel for the Swing version of PathVisio. This pane shows annotation
 * information from the Gene Database when a datanode is clicked.
 * <p>
 * BackpagePane listens to selection events and other event types to update
 * its contents when necessary.
 * <p>
 * It uses a BackpageTextProvider to generate the html content, which
 * has to be inserted at construction time. Backpage generation may take
 * a noticable amount of time, therefore this task is always done in a background thread.
 * <p>
 * It is the responsibility of the instantiator to also call the dispose() method,
 * otherwise the background thread is not killed.
 */
public class BackpagePane extends JEditorPane implements ApplicationEventListener, SelectionListener, PathwayElementListener
{
	private final BackpageTextProvider bpt;
	private Engine engine;
	private ExecutorService executor;

	public BackpagePane(BackpageTextProvider bpt, Engine engine)
	{
		super();

		engine.addApplicationEventListener(this);
		VPathway vp = engine.getActiveVPathway();
		if(vp != null) vp.addSelectionListener(this);

		this.engine = engine;

		setEditable(false);
		setContentType("text/html");
		this.bpt = bpt;

		executor = Executors.newSingleThreadExecutor();

		//Workaround for #1313
		//Cause is java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6993691
		setEditorKit(new HTMLEditorKit() {
			protected Parser getParser() {
				try {
					Class c = Class
							.forName("javax.swing.text.html.parser.ParserDelegator");
					Parser defaultParser = (Parser) c.newInstance();
					return defaultParser;
				} catch (Throwable e) {
				}
				return null;
			}
		});
	}

	private PathwayElement input;

	public void setInput(final PathwayElement e)
	{
		//System.err.println("===== SetInput Called ==== " + e);
		if(e == input) return; //Don't set same input twice

		//Remove pathwaylistener from old input
		if(input != null) input.removeListener(this);

		if(e == null) {
			input = null;
			setText(bpt.getBackpageHTML(null));
		} else {
			input = e;
			input.addListener(this);
			doQuery();
		}
	}

	private void doQuery()
	{
		setText("Loading");
		currRef = input.getXref();

		executor.execute(new Runnable()
		{
			public void run()
			{
				if(input == null) return;
				final String txt = bpt.getBackpageHTML(input);

				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						setText(txt);
						setCaretPosition(0); // scroll to top.
					}
				});
			}
		});
	}

	public void selectionEvent(SelectionEvent e) {
		switch(e.type) {
		case SelectionEvent.OBJECT_ADDED:
			//Just take the first DataNode in the selection
			Iterator<VPathwayElement> it = e.selection.iterator();
			while(it.hasNext()) {
				VPathwayElement o = it.next();
				// works for all Graphics object
				// the backpage checks and gives the correct error if 
				// it's not a datanode or line
				if(o instanceof Graphics) {
					setInput(((Graphics)o).getPathwayElement());
					break;
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
			case VPATHWAY_CREATED:
				((VPathway)e.getSource()).addSelectionListener(this);
			break;
			case VPATHWAY_DISPOSED:
				((VPathway)e.getSource()).removeSelectionListener(this);
				// remove content of backpage when pathway is closed
				input = null;
				setText(bpt.getBackpageHTML(null));
			break;
		}
	}

	Xref currRef;

	public void gmmlObjectModified(PathwayElementEvent e) {
		PathwayElement pe = e.getModifiedPathwayElement();
		if(input != null && (e.affectsProperty(StaticProperty.GENEID) || e.affectsProperty(StaticProperty.DATASOURCE))) {
			Xref nref = new Xref (pe.getGeneID(), input.getDataSource());
			if(!nref.equals(currRef))
			{
				doQuery();
			}
		}
	}

	private boolean disposed = false;
	public void dispose()
	{
		assert (!disposed);
		engine.removeApplicationEventListener(this);
		VPathway vpwy = engine.getActiveVPathway();
		if (vpwy != null) vpwy.removeSelectionListener(this);
		executor.shutdown();
		disposed = true;
	}
}
