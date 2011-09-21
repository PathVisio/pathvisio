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
package org.pathvisio.plugins.gexview;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import org.bridgedb.IDMapperException;
import org.bridgedb.gui.SimpleFileFilter;
import org.bridgedb.rdb.construct.DataDerby;
import org.pathvisio.core.Revision;
import org.pathvisio.core.util.Resources;
import org.pathvisio.desktop.gex.SimpleGex;

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
		catch (IDMapperException e)
		{
			gex = null;
			gexFile = null;
			JOptionPane.showConfirmDialog(frame, "Data exception", "Data exception", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
		}
	}

	private class OpenAction extends AbstractAction
	{
		public OpenAction()
		{
			super();
			putValue(NAME, "Open");
			putValue(SMALL_ICON, new ImageIcon (Resources.getResourceURL("open.gif")));
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

	private class ExitAction extends AbstractAction
	{
		public ExitAction()
		{
			super();
			putValue(NAME, "Exit");
		}
		
		public void actionPerformed(ActionEvent e) 
		{
			frame.setVisible(false);
		}
	}

	private OpenAction openAction = new OpenAction();

	public JMenuBar createMenu()
	{
		JMenuBar result = new JMenuBar();

		JMenu file = new JMenu("File");
		JMenu help = new JMenu("Help");

		file.add(openAction);
		file.add (new ExitAction());
		
		result.add (file);
		result.add (help);

		return result;
	}
	
	private File propsFile = new File (System.getProperty("user.home"), ".gexview.properties"); 
	private Properties props = new Properties();
	
	private void init()
	{
		try {
			props.load(new FileReader (propsFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			// safe to ignore, continue with default props
		} catch (IOException e1) {
			e1.printStackTrace();
			// safe to ignore, continue with default props
		}
	}

	private void shutDown()
	{
		try {
			props.store(new FileWriter (propsFile), "");
		} catch (IOException e) {
			e.printStackTrace();
			// safe to ignore, unfortunately we didn't save props
		}
		System.out.println ("Clean shutdown");
	}

	public void createAndShowGUI()
	{
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Gex Viewer r" + Revision.REVISION);
		frame.setJMenuBar(createMenu());

		gexFile = new File ("example-data/sample_data_1.pgex");
		try
		{
			gex = new SimpleGex (gexFile + "",
					false, new DataDerby());
		}
		catch (IDMapperException e)
		{
			e.printStackTrace();
		}
		tblHeatmap = new JTable(new HeatmapTableModel(gex));
		//tblHeatmap.set
		frame.add (new JScrollPane (tblHeatmap));
		
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent arg0) 
			{
				shutDown();
			}
		});
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	

}
