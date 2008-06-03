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
package org.pathvisio.plugins.statistics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.Engine;
import org.pathvisio.data.GexManager;
import org.pathvisio.data.SimpleGex;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.model.Xref;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.util.FileUtils;
import org.pathvisio.util.PathwayParser;
import org.pathvisio.util.PathwayParser.ParseException;
import org.pathvisio.util.swing.ListWithPropertiesTableModel;
import org.pathvisio.util.swing.PropertyColumn;
import org.pathvisio.util.swing.RowWithProperties;
import org.pathvisio.visualization.colorset.Criterion;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class StatisticsPlugin implements Plugin 
{
	/**
	 * Plugin initialization method, registers statistics action in the Data menu
	 */
	public void init() 
	{
		StatisticsAction statisticsAction = new StatisticsAction();

		Logger.log.info ("Initializing statistics plugin");
		SwingEngine.getCurrent().registerMenuAction ("Data", statisticsAction);
	}
	
	/**
	 * Statistics action
	 */
	private static class StatisticsAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public StatisticsAction() 
		{
			super();
			putValue(NAME, "Statistics...");
			putValue(SHORT_DESCRIPTION, "Do simple pathway statistics");
		}

		public void actionPerformed(ActionEvent e) 
		{
//			JOptionPane.showMessageDialog(SwingEngine.getCurrent().getFrame(), "Action not implemented");
			StatisticsDlg dlg = new StatisticsDlg();
			dlg.createAndShowDlg();
		}
	}

	/**
	 * Dialog to let the user set parameters, start calculation and view results.
	 */
	private static class StatisticsDlg
	{
		Criterion myCriterion = new Criterion();
		JTextField txtExpr;
		JLabel lblError;
		
		private void updateCriterion()
		{
			boolean ok = myCriterion.setExpression(txtExpr.getText());
			if (!ok)
			{
				lblError.setText(myCriterion.getParseException().getMessage());
			}
			else
			{
				lblError.setText ("OK");
			}
		}
		
		/**
		 * Pop up the statistics dialog
		 */
		private void createAndShowDlg()
		{
			
			final JFrame frame = new JFrame ("Pathway statistics");
			
			FormLayout layout = new FormLayout (
					"4dlu, pref:grow, 4dlu, pref, 4dlu", 
					"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, fill:pref:grow");
			frame.setLayout(layout);
			
			CellConstraints cc = new CellConstraints();
			
			frame.add (new JLabel ("Expression: "), cc.xy(2,2));
			txtExpr = new JTextField(40);
			txtExpr.getDocument().addDocumentListener(new DocumentListener() 
			{

				public void changedUpdate(DocumentEvent e) 
				{
					updateCriterion();
				}

				public void insertUpdate(DocumentEvent e) 
				{
					updateCriterion();
				}

				public void removeUpdate(DocumentEvent e) 
				{
					updateCriterion();					
				}
			});
			
			frame.add (txtExpr, cc.xyw(2,4,3));
			
	
			final JList lstOperators = new JList(Criterion.tokens);
			frame.add (new JScrollPane (lstOperators), cc.xy (2,6));
			
			lstOperators.addMouseListener(new MouseAdapter() 
			{
				public void mouseClicked(MouseEvent me) 
				{
					int selectedIndex = lstOperators.getSelectedIndex();
					if (selectedIndex >= 0)
					{
						String expr = txtExpr.getText();
						txtExpr.setText (expr + " " + Criterion.tokens[selectedIndex]);
					}
					txtExpr.requestFocus();
				}
			} );
			
			SimpleGex gex = GexManager.getCurrent().getCurrentGex();
			final List<String> sampleNames = gex.getSampleNames();
			final JList lstSamples = new JList(sampleNames.toArray());
	
			lstSamples.addMouseListener(new MouseAdapter() 
			{
				public void mouseClicked(MouseEvent me) 
				{
					int selectedIndex = lstSamples.getSelectedIndex();
					if (selectedIndex >= 0)
					{
						String expr = txtExpr.getText();
						txtExpr.setText (expr + " [" + sampleNames.get(selectedIndex) + "]");
					}
					txtExpr.requestFocus();
				}
			} );
	
			frame.add (new JScrollPane (lstSamples), cc.xy (4,6));
			lblError = new JLabel("OK");
			frame.add (lblError, cc.xyw (2,8,3));
			frame.add (new JLabel ("Pathway Directory: "), cc.xy (2,10));
			final JTextField txtDir = new JTextField(40);
			txtDir.setText(Engine.getCurrent().getPreferences().get(GlobalPreference.DIR_PWFILES));
			frame.add (txtDir, cc.xy(2,12));
			JButton btnDir = new JButton("Browse");
			btnDir.addActionListener(new ActionListener ()
			{
	
				public void actionPerformed(ActionEvent ae) 
				{
					JFileChooser jfc = new JFileChooser();
					if (jfc.showDialog(null, "Choose") == JFileChooser.APPROVE_OPTION)
					{
						txtDir.setText("" + jfc.getSelectedFile());
					}
				}
			});
			frame.add (btnDir, cc.xy (4,12));
			JButton btnCalc = new JButton ("Calculate");
			frame.add (btnCalc, cc.xyw (2,14,3, "center, top"));
			final JTable tblResult = new JTable ();
			frame.add (new JScrollPane (tblResult), cc.xyw (2,16,3));
			
			btnCalc.addActionListener(new ActionListener (){
	
				public void actionPerformed(ActionEvent ae) 
				{
					File pwDir = new File (txtDir.getText());
					String expr = txtExpr.getText();
					
					//TODO: run in separate thread
					doCalculate (pwDir, myCriterion, tblResult, frame);
				}
			});
	
			txtExpr.requestFocus();
			frame.pack();
			frame.setLocationRelativeTo(SwingEngine.getCurrent().getFrame());
			frame.setVisible(true);
		}

	}
	
	/**
	 * asynchronous statistics calculation function
	 */
	private static void doCalculate(final File pwDir, Criterion crit, JTable resultTable, JFrame parentFrame)
	{
		final int TOTALWORK = 1000;

		final ProgressMonitor pmon = new ProgressMonitor(
				SwingEngine.getCurrent().getFrame(), "Pathway search", "searching pathways...",
				0, TOTALWORK);
		
		final StatisticsTableModel stm = new StatisticsTableModel();
		stm.setColumns(new Column[] {Column.PATHWAY_NAME, Column.R, Column.N, Column.PCT, Column.PVAL});
		resultTable.setModel (stm);

		SwingWorker<Boolean, StatisticsResult> worker = new SwingWorker<Boolean, StatisticsResult>() 
		{
			@Override
			protected Boolean doInBackground()
			{
				List<File> files = FileUtils.getFiles(pwDir, "gpml", true);
				
				XMLReader xmlReader = null;
				
				try
				{
					xmlReader = XMLReaderFactory.createXMLReader();
				}
				catch (SAXException e)
				{
					Logger.log.error("Problem while searching pathways", e);
					return false;
				}
		
				int i = 0;
				for (File file : files)
				{
					try
					{
						PathwayParser pwyParser = new PathwayParser(file, xmlReader);
						List<Xref> genes = new ArrayList<Xref>();			
						genes.addAll (pwyParser.getGenes());
						
						StatisticsResult sr = new StatisticsResult (pwyParser.getName(), genes.size(), 1);
						publish (sr);
					}
					catch (ParseException pe)
					{
						Logger.log.warn ("Could not parse " + file + ", ignoring");
					}
					pmon.setProgress((int)(TOTALWORK * i++ / files.size()));				
				}
				pmon.close();
				return true;
			}
			
			@Override
			protected void process (List<StatisticsResult> srs)
			{
				for (StatisticsResult sr : srs)
				{
					stm.addRow (sr);
				}
			}
		};
		
		worker.execute();
	}
	
	/**
	 * Statistics calculation for a single pathway,
	 * to be shown as a row in the statistics result table
	 */
	private static class StatisticsResult implements RowWithProperties<Column> 
	{
		private int r = 0;
		private int n = 0;
		private String name;

		StatisticsResult (String name, int n, int r)
		{
			this.r = r;
			this.n = n;
			this.name = name;
		}

		public String getProperty(Column prop) 
		{
			switch (prop)
			{
			case N: return "" + n;
			case R: return "" + r;
			case PATHWAY_NAME: return name;
			case PVAL: return "0.01"; //TODO
			case ZSCORE: return "0"; // TODO
			case PCT: return (n == 0 ? "Nan" : String.format("%3.2f%%", 100.0 * (float)r / (float)n));
			default : throw new IllegalArgumentException("Unknown property");
			}
		}
	}

	/**
	 * Table Model for showing statistics results 
	 */
	private static class StatisticsTableModel extends ListWithPropertiesTableModel<StatisticsPlugin.Column, StatisticsResult>
	{
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Enum for possible columns in the statistics result table
	 */
	private static enum Column implements PropertyColumn
	{
		N("n"),
		R("r"),
		PATHWAY_NAME("Pathway"),
		PCT("%"),
		PVAL("pval"),
		ZSCORE ("Z Score");
		
		private String title;
		
		private Column(String title)
		{
			this.title = title;
		}
		
		public String getTitle() 
		{
			return title;
		}
	}
	
}