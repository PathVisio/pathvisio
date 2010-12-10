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
package org.pathvisio.gpmldiff;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.RootPaneContainer;

import org.pathvisio.gui.swing.WrapLayout;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;
import org.pathvisio.view.VPathwayEvent.VPathwayEventType;
import org.pathvisio.view.swing.VPathwaySwing;

class GpmlDiffWindow extends JPanel implements VPathwayListener
{

	private JScrollPane[] pwyPane = new JScrollPane[2];
	private JToolBar toolbar = null;
	private JComboBox zoomCombo = null;

	public static final int PWY_OLD = 0;
	public static final int PWY_NEW = 1;

	private VPathwaySwing[] wrapper = { null, null };
	private VPathway[] view = { null, null };
	private PwyDoc[] doc = { null, null };

	private PanelOutputter outputter = null;

	double zoomFactor = 100;
	JPanel centerPanel = null;

	GlassPane glassPane;

	public void setFile (int pwyType, File f)
	{
		// Pathway pwy = new Pathway();
		doc[pwyType] = PwyDoc.read (f);
		assert (doc[pwyType] != null);

		wrapper[pwyType] = new VPathwaySwing(pwyPane[pwyType]);

		view[pwyType] = wrapper[pwyType].createVPathway();
		view[pwyType].setEditMode(false);
		view[pwyType].addVPathwayListener(this);
		view[pwyType].setSelectionEnabled (false);
		view[pwyType].fromModel(doc[pwyType].getPathway());
		view[pwyType].setPctZoom (zoomFactor);

		outputter = null; // invalidate putative remaining outputter

		if (view[PWY_OLD] != null && view[PWY_NEW] != null)
		{
			SearchNode result = doc[PWY_OLD].findCorrespondence (doc[PWY_NEW], new BetterSim(), new BasicCost());
			outputter = new PanelOutputter(view[PWY_OLD], view[PWY_NEW]);
			doc[PWY_OLD].writeResult (result, doc[PWY_NEW], outputter);
			try
			{
				outputter.flush();
			}
			catch (IOException ex) { ex.printStackTrace(); }

			// merge models of the two pathways
			// TODO: find solution for inequal sized pathways.
			pwyPane[0].getHorizontalScrollBar().setModel(
				pwyPane[1].getHorizontalScrollBar().getModel());
			pwyPane[0].getVerticalScrollBar().setModel(
				pwyPane[1].getVerticalScrollBar().getModel());
		}
	}

	private class LoadPwyAction extends AbstractAction
	{
		private int pwyType;
		private JPanel parent;

		public LoadPwyAction (JPanel window, int value)
		{
			super ("Load " + ((value == PWY_OLD) ? "old" : "new")  + " pathway");
			String s = (value == PWY_OLD) ? "old" : "new";
			putValue (Action.SHORT_DESCRIPTION, "Load the " + s + " pathway");
			putValue (Action.LONG_DESCRIPTION, "Load the " + s + " pathway");
			pwyType = value;
			parent = window;
		}

		public void actionPerformed(ActionEvent e)
		{
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogTitle("Load a pathway");
			jfc.setDialogType (JFileChooser.OPEN_DIALOG);

			int status = jfc.showDialog (parent, "Load");
			if (status == JFileChooser.APPROVE_OPTION)
			{
				setFile (pwyType, jfc.getSelectedFile());
			}
		}
	}

	class CenterAction extends AbstractAction
	{

		public CenterAction()
		{
			super ("Center");
		}

		public void actionPerformed (ActionEvent e)
		{
		}
	}

	private class ZoomAction extends AbstractAction
	{

		double actionZoomFactor;

		public ZoomAction(double zf)
		{
			actionZoomFactor = zf;
			String descr = "Set zoom to " + (int)zf + "%";
			putValue(Action.NAME, toString());
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
		}

		public void actionPerformed(ActionEvent e)
		{
			setZoomFactor (actionZoomFactor);
		}

		public String toString()
		{
			return (int)actionZoomFactor + "%";
		}
	}

	private class ZoomToFitAction extends AbstractAction
	{

		public ZoomToFitAction()
		{
			putValue(Action.NAME, toString());
			putValue(Action.SHORT_DESCRIPTION, "Fit pathway to window");
		}

		public void actionPerformed(ActionEvent e)
		{
			zoomToFit();
		}

		public String toString()
		{
			return "Fit to window";
		}
	}

	/**
	 * calculate the best zoom factor to fit both pathways,
	 * then zoom to it.
	 */
	void zoomToFit ()
	{
		double result = -1;
		if (view[0] != null)
		{
			result = view[0].getFitZoomFactor();
		}
		if (view[1] != null)
		{
			double temp = view[1].getFitZoomFactor();
			if (view[0] != null && temp < result)
			{
				result = temp;
			}
		}
		if (result > 0)
		{
			setZoomFactor (result);
		}
		zoomCombo.setSelectedItem( (int)zoomFactor + "%");
	}
	/**
	 * Set the zoom factor for both panels at once.
	 */
	void setZoomFactor (double factor)
	{
		zoomFactor = factor;
		if(view[0] != null)
		{
			view[0].setPctZoom(zoomFactor);
		}
		if(view[1] != null)
		{
			view[1].setPctZoom(zoomFactor);
			glassPane.setPctZoom (view[1].getPctZoom());
		}
	}

	void addToolbarActions()
	{
		toolbar.setLayout (new WrapLayout (1, 1));

		toolbar.add(new JLabel("Zoom:", JLabel.LEFT));
		zoomCombo = new JComboBox(new Object[] {
				new ZoomToFitAction(),
				new ZoomAction(20),
				new ZoomAction(30),
				new ZoomAction(50),
				new ZoomAction(75),
				new ZoomAction(100),
				new ZoomAction(120),
				new ZoomAction(150) });
		zoomCombo.setMaximumSize(zoomCombo.getPreferredSize());
		zoomCombo.setEditable(true);
		zoomCombo.setSelectedIndex(5); // 100%
		zoomCombo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JComboBox combo = (JComboBox) e.getSource();
				Object s = combo.getSelectedItem();
				if (s instanceof Action)
				{
					((Action) s).actionPerformed(e);
				}
				else if (s instanceof String)
				{
					String zs = (String) s;
					try
					{
						double zf = Double.parseDouble(zs);
						setZoomFactor(zf);
					}
					catch (Exception ex)
					{
						// Ignore bad input
					}
				}
			}
		});
		toolbar.add (zoomCombo);


// toolbar.addSeparator();
// toolbar.add (new CenterAction());
	}


	void addFileActions()
	{
		toolbar.addSeparator();
		toolbar.add (new LoadPwyAction(this, PWY_OLD));
		toolbar.add (new LoadPwyAction(this, PWY_NEW));
	}

	GpmlDiffWindow (RootPaneContainer parent)
	{
		setLayout (new BorderLayout ());

		JPanel subpanel = new JPanel();
 		subpanel.setLayout (new BoxLayout(subpanel, BoxLayout.X_AXIS));
		add (subpanel, BorderLayout.CENTER);

		toolbar = new JToolBar();
		addToolbarActions();
		add(toolbar, BorderLayout.PAGE_START);

		glassPane = new GlassPane(this);
		parent.setGlassPane (glassPane);
		glassPane.setVisible(true);
		Toolkit.getDefaultToolkit().addAWTEventListener(
			glassPane, AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);


		for (int i = 0; i < 2; ++i)
		{
			pwyPane[i] = new JScrollPane();
		}

		subpanel.add (pwyPane[PWY_OLD]);
		pwyPane[PWY_OLD].setPreferredSize (new Dimension (400, 300));
		subpanel.add (pwyPane[PWY_NEW]);
		pwyPane[PWY_NEW].setPreferredSize (new Dimension (400, 300));

		glassPane.setViewPorts (pwyPane[0].getViewport(), pwyPane[1].getViewport());

		validate();
	}

	public void vPathwayEvent (VPathwayEvent e)
	{
		if (e.getType() == VPathwayEventType.ELEMENT_CLICKED_DOWN)
		{
			if (outputter != null)
			{
				PanelOutputter.ModData mod = outputter.modsByElt.get (e.getAffectedElement());
				if (mod != null)
				{
					switch (mod.type)
					{
					case CHANGED:
						glassPane.setModifyHint (mod.hints, mod.x1, mod.y1, mod.x2, mod.y2);
						break;
					case ADDED:
						glassPane.setAddHint (mod.hints, mod.x2, mod.y2);
						break;
					case REMOVED:
						glassPane.setRemoveHint (mod.hints, mod.x1, mod.y1);
						break;
					}
				}
				else
				{
					glassPane.clearHint ();
				}
			}
		}
	}
}