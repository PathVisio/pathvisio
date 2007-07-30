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
package org.pathvisio.gpmldiff;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import org.pathvisio.gui.swing.*;
import org.pathvisio.view.VPathway;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.ConverterException;
import org.pathvisio.view.swing.VPathwaySwing;
import org.pathvisio.debug.Logger;

class GpmlDiffWindow extends JFrame
{
	private static final int WINDOW_WIDTH = 1000;
	private static final int WINDOW_HEIGHT = 500;

	private JScrollPane[] pwyPane = new JScrollPane[2];
	private JMenuBar menubar;

	private static final int PWY_OLD = 0;
	private static final int PWY_NEW = 1;

	private VPathwaySwing[] wrapper = new VPathwaySwing[2];
	private VPathway[] view = new VPathway[2];

	private class LoadPwyAction extends AbstractAction
	{
		private int pwyType;
		private JFrame parent;
		
		public LoadPwyAction (JFrame window, int value)
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
				Pathway pwy = new Pathway();
				try
				{
					pwy.readFromXml(jfc.getSelectedFile(), true);
					
					wrapper[pwyType] = new VPathwaySwing(pwyPane[pwyType]);

					view[pwyType] = wrapper[pwyType].createVPathway();
					view[pwyType].fromGmmlData(pwy);
				}
				
				catch (ConverterException ce)
				{
					JOptionPane.showMessageDialog (
						parent,
						"Exception while opening gpml file.\n" +
						"Please check that the file you opened is a valid Gpml file.",
						"Open Error", JOptionPane.ERROR_MESSAGE);
					Logger.log.error ("Error opening gpml", ce);
				}
			}
		}
	}

	class CloseAction extends AbstractAction
	{
		JFrame parent;
		public CloseAction(JFrame _parent)
		{
			super("Close");
			parent = _parent;
		}

		public void actionPerformed (ActionEvent e)
		{
			parent.dispose();
			System.exit(0);
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
	
	void addMenuActions()
	{
		JMenu filemenu = new JMenu ("File");
		filemenu.add (new LoadPwyAction(this, PWY_OLD));
		filemenu.add (new LoadPwyAction(this, PWY_NEW));
		filemenu.add (new CloseAction(this));

		JMenu viewmenu = new JMenu ("View");
		viewmenu.add (new CommonActions.ZoomAction(VPathway.ZOOM_TO_FIT));
		viewmenu.add (new CommonActions.ZoomAction(30));
		viewmenu.add (new CommonActions.ZoomAction(50));
		viewmenu.add (new CommonActions.ZoomAction(75));
		viewmenu.add (new CommonActions.ZoomAction(100));
		viewmenu.add (new CommonActions.ZoomAction(120));
		viewmenu.addSeparator();
		viewmenu.add (new CenterAction());
		
		menubar.add (filemenu);
		menubar.add (viewmenu);					  
	}
	
	GpmlDiffWindow ()
	{
		super ();

		setSize (WINDOW_WIDTH, WINDOW_HEIGHT);
		Container contents = getContentPane();
		contents.setLayout (new BorderLayout());
		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		
		menubar = new JMenuBar();
		addMenuActions ();
		setJMenuBar (menubar);

		for (int i = 0; i < 2; ++i)
		{
			pwyPane[i] = new JScrollPane();
		}

		contents.add (pwyPane[PWY_OLD], BorderLayout.WEST);
		contents.add (pwyPane[PWY_NEW], BorderLayout.EAST);
	}

}