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
package org.pathvisio.plugins.gexview;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.*;

import org.pathvisio.Revision;
import org.pathvisio.data.*;
import org.pathvisio.gui.swing.StandaloneActions;
import org.pathvisio.util.swing.SimpleFileFilter;

public class ApplicationWindow
{
	// main window frame
	JFrame frame;
	JTable tblHeatmap;

	// both are null if there is no gex opened
	File gexFile = null;
	SimpleGex gex = null;

	public void openGex(File f)
	{
		if (!f.exists()) throw new IllegalArgumentException ("File doesn't exist!");
		gexFile = f;
		tblHeatmap.setModel(new HeatmapTableModel(gex));

		try
		{
			gex = new SimpleGex(f.toString(), false, new DataDerby());
		}
		catch (DataException e)
		{
			gex = null;
			gexFile = null;
			JOptionPane.showConfirmDialog(frame, "Data exception", "Data exception", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
		}
	}

	private class OpenAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public OpenAction()
		{
			super();
			putValue(NAME, "Open");
			putValue(SMALL_ICON, new ImageIcon (StandaloneActions.IMG_OPEN));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl O"));
		}

		public void actionPerformed(ActionEvent e)
		{
			JFileChooser jfc = new JFileChooser();
			jfc.setFileFilter(new SimpleFileFilter("pgex files", "*.pgex"));
			if (jfc.showDialog(null, "Open") == JFileChooser.APPROVE_OPTION)
			{
				File f = jfc.getSelectedFile();
				openGex(f);
			}
		}
	}

	private OpenAction openAction = new OpenAction();

	public JMenuBar createMenu()
	{
		JMenuBar result = new JMenuBar();

		JMenu file = new JMenu("File");
		JMenu help = new JMenu("Help");

		file.add(openAction);

		result.add (file);
		result.add (help);

		return result;
	}

	public void createAndShowGUI()
	{
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Gex Viewer r" + Revision.REVISION);
		frame.setJMenuBar(createMenu());

		gexFile = new File ("/home/martijn/prg/pathvisio-trunk/example-data/sample_data_1.pgex");
		try
		{
			gex = new SimpleGex (gexFile + "",
					false, new DataDerby());
		}
		catch (DataException e)
		{
			e.printStackTrace();
		}
		tblHeatmap = new JTable(new HeatmapTableModel(gex));
		//tblHeatmap.set
		frame.add (new JScrollPane (tblHeatmap));

		frame.pack();
		frame.setVisible(true);
	}

}
