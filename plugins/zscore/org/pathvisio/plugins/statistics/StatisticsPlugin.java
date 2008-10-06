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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingworker.SwingWorker.StateValue;
import org.pathvisio.data.DataException;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.GexManager;
import org.pathvisio.data.SimpleGex;
import org.pathvisio.data.CachedData.Data;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.model.Xref;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.FileUtils;
import org.pathvisio.util.PathwayParser;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.Stats;
import org.pathvisio.util.PathwayParser.ParseException;
import org.pathvisio.util.swing.ListWithPropertiesTableModel;
import org.pathvisio.util.swing.PropertyColumn;
import org.pathvisio.util.swing.RowWithProperties;
import org.pathvisio.util.swing.SimpleFileFilter;
import org.pathvisio.util.swing.TextFieldUtils;
import org.pathvisio.visualization.colorset.Criterion;
import org.pathvisio.visualization.colorset.Criterion.CriterionException;
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
		SwingEngine se = SwingEngine.getCurrent();
		StatisticsAction statisticsAction = new StatisticsAction(se);

		Logger.log.info ("Initializing statistics plugin");
		se.registerMenuAction ("Data", statisticsAction);
	}
	
	/**
	 * Statistics action
	 */
	private static class StatisticsAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;
		SwingEngine se;
		
		public StatisticsAction(SwingEngine se) 
		{
			super();
			this.se = se;
			putValue(NAME, "Statistics...");
			putValue(SHORT_DESCRIPTION, "Do simple pathway statistics");
		}

		public void actionPerformed(ActionEvent e) 
		{
			SimpleGex gex = GexManager.getCurrent().getCurrentGex();
			if (gex == null)
			{
				JOptionPane.showMessageDialog(se.getFrame(), "Select an expression dataset first");
			}
			else
			{
				StatisticsDlg dlg = new StatisticsDlg();
				dlg.createAndShowDlg(se);
			}
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
		private JButton btnSave;
		private Result result = null;
		private JButton btnCalc;
		
		private void updateCriterion()
		{
			String[] sampleNames = GexManager.getCurrent().getCurrentGex().getSampleNames().toArray(new String[0]);
			String error = myCriterion.setExpression(txtExpr.getText(), sampleNames);
			if (error != null)
			{
				lblError.setText(error);
			}
			else
			{
				lblError.setText ("OK");
			}
		}
		
		/**
		 * Pop up the statistics dialog
		 */
		private void createAndShowDlg(final SwingEngine se)
		{
			
			final JDialog dlg = new JDialog (se.getFrame(), "Pathway statistics", false);
			
			FormLayout layout = new FormLayout (
					"4dlu, pref:grow, 4dlu, pref, 4dlu", 
					"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, fill:pref:grow");
			dlg.setLayout(layout);
			
			CellConstraints cc = new CellConstraints();
			
			dlg.add (new JLabel ("Expression: "), cc.xy(2,2));
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
			
			dlg.add (txtExpr, cc.xyw(2,4,3));
			
	
			final JList lstOperators = new JList(Criterion.tokens);
			dlg.add (new JScrollPane (lstOperators), cc.xy (2,6));
			
			lstOperators.addMouseListener(new MouseAdapter() 
			{
				public void mouseClicked(MouseEvent me) 
				{
					int selectedIndex = lstOperators.getSelectedIndex();
					if (selectedIndex >= 0)
					{
						String toInsert = Criterion.tokens[selectedIndex];
						TextFieldUtils.insertAtCursorWithSpace(txtExpr, toInsert);
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
						String toInsert = "[" + sampleNames.get(selectedIndex) + "]"; 
						TextFieldUtils.insertAtCursorWithSpace(txtExpr, toInsert);
					}
					txtExpr.requestFocus();
				}
			} );
	
			dlg.add (new JScrollPane (lstSamples), cc.xy (4,6));
			lblError = new JLabel("OK");
			dlg.add (lblError, cc.xyw (2,8,3));
			dlg.add (new JLabel ("Pathway Directory: "), cc.xy (2,10));
			final JTextField txtDir = new JTextField(40);
			txtDir.setText(PreferenceManager.getCurrent().get(GlobalPreference.DIR_PWFILES));
			dlg.add (txtDir, cc.xy(2,12));
			JButton btnDir = new JButton("Browse");
			btnDir.addActionListener(new ActionListener ()
			{
	
				public void actionPerformed(ActionEvent ae) 
				{
					JFileChooser jfc = new JFileChooser();
					jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					jfc.setCurrentDirectory(new File (txtDir.getText()));
					if (jfc.showDialog(null, "Choose") == JFileChooser.APPROVE_OPTION)
					{
						txtDir.setText("" + jfc.getSelectedFile());
					}
				}
			});
			
			dlg.add (btnDir, cc.xy (4,12));
			
			JPanel pnlButtons = new JPanel();
			
			btnCalc = new JButton ("Calculate");
			pnlButtons.add (btnCalc);

			btnSave = new JButton ("Save results");
			pnlButtons.add (btnSave);
			btnSave.setEnabled(false);

			dlg.add (pnlButtons, cc.xyw (2,14,3));
			
			final JTable tblResult = new JTable ();
			tblResult.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent me) 
				{
					int row = tblResult.getSelectedRow();
					final StatisticsResult sr = ((StatisticsTableModel)(tblResult.getModel())).getRow(row);
						
						//TODO: here I want to use SwingEngine.openPathway, but I need to 
						// be able to wait until the process is finished!
					se.openPathway(sr.getFile());
				}

			});

			dlg.add (new JScrollPane (tblResult), cc.xyw (2,16,3));
						
			btnCalc.addActionListener(new ActionListener (){
	
				public void actionPerformed(ActionEvent ae) 
				{
					File pwDir = new File (txtDir.getText());
					btnCalc.setEnabled(false);
					doCalculate (pwDir, myCriterion, tblResult, dlg);
				}
			});
			
			btnSave.addActionListener(new ActionListener () 
			{
				
				public void actionPerformed(ActionEvent ae)
				{
					JFileChooser jfc = new JFileChooser();
					jfc.setDialogTitle("Save results");
					jfc.setFileFilter(new SimpleFileFilter ("Tab delimited text", "*.txt", true));
					jfc.setDialogType(JFileChooser.SAVE_DIALOG);
					
					if (jfc.showDialog(dlg, "Save") == JFileChooser.APPROVE_OPTION)
					{
						File f = jfc.getSelectedFile();
						if (!f.toString().endsWith (".txt"))
						{
							f = new File (f + ".txt");
						}
						try
						{
							result.save (f);
						}
						catch (IOException e)
						{
							JOptionPane.showMessageDialog(dlg, "Could not save results: " + e.getMessage());
							Logger.log.error ("Could not save results", e);
						}
					}
				}
			
			});
	
			txtExpr.requestFocus();
			dlg.pack();
			dlg.setLocationRelativeTo(se.getFrame());
			dlg.setVisible(true);
		}

		/**
		 * asynchronous statistics calculation function
		 */
		private void doCalculate(final File pwDir, final Criterion crit, final JTable resultTable, JDialog parentFrame)
		{
			final ProgressMonitor pmon = new ProgressMonitor(
					parentFrame, "Pathway search", "searching pathways...",
					0, 100);
			
			btnSave.setEnabled (false);
			final StatisticsTableModel stm = new StatisticsTableModel();
			stm.setColumns(new Column[] {Column.PATHWAY_NAME, Column.R, Column.N, Column.TOTAL, Column.PCT, Column.ZSCORE});
			resultTable.setModel (stm);

			final ZScoreCalculator worker = new ZScoreCalculator(crit, stm, pwDir);
			
			worker.addPropertyChangeListener(new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent evt) 
				{
					String strPropertyName = evt.getPropertyName();
					if ("progress".equals(strPropertyName)) 
					{
						int progress = (Integer) evt.getNewValue();
						stm.packRows(resultTable, 2); // pack table rows
						pmon.setProgress(progress);
						if (pmon.isCanceled())
						{
							worker.cancel(true);
							Logger.log.info ("Calculation cancelled");
						}
					}
					else if ("note".equals(strPropertyName)) 
					{
						String note = (String) evt.getNewValue();
						pmon.setNote(note);
					}
					else if ("state".equals(strPropertyName))
					{
						if (worker.getState() == StateValue.DONE)
						{
							if (!worker.isCancelled())
							{
								try
								{
									result = worker.get();
									btnSave.setEnabled(true);
								}
								catch (ExecutionException ex)
								{
									JOptionPane.showMessageDialog(null, "Execution exception: " + ex.getMessage());
									Logger.log.error ("Execution exception", ex);
								}
								catch (InterruptedException ex)
								{
									JOptionPane.showMessageDialog(null, "Interrupted: " + ex.getMessage());
									Logger.log.error ("Interrupted.", ex);
								}
							}
							pmon.close();
							btnCalc.setEnabled(true);
						}
					}
				}

			});
			worker.execute();			
		}
	}

	private static class Result 
	{
		StatisticsTableModel stm;
		int N = 0;
		int R = 0;
		Criterion crit;
		File pwDir;
		
		void save (File f) throws IOException
		{
			PrintStream out = new PrintStream (new FileOutputStream(f));
			
			out.println ("Statistics results for " + new Date());
			out.println ("Dataset: " + GexManager.getCurrent().getCurrentGex().getDbName());
			out.println ("Pathway directory: " + pwDir);
			out.println ("Criterion: " + crit.getExpression());
			out.println ("Rows in data (N): " + N);
			out.println ("Rows meeting criterion (R): " + R);
			out.println();
			
			stm.printData(out);
		}
	}
	
	private static class ZScoreCalculator extends SwingWorker <Result, StatisticsResult> 
	{			
		private Result result;
		private String note = null;
		
		private void setNote(String value)
		{
			String oldNote = note;
			note = value;
			firePropertyChange ("note", oldNote, note);
		}
		
		ZScoreCalculator(Criterion crit, StatisticsTableModel stm, File pwDir)
		{
			result = new Result();
			result.crit = crit;
			result.stm = stm;
			result.pwDir = pwDir;
		}
		
		@Override
		protected Result doInBackground()
		{
			setProgress (0);
			setNote("Analyzing data");
			
			SimpleGex gex = GexManager.getCurrent().getCurrentGex();
			
			// first we calculate N and R
			
			try
			{
				int maxRow = gex.getMaxRow();
				for (int i = 0; i < maxRow; ++i)
				{
					if (isCancelled()) return null;
					try
					{
						Data d = gex.getRow(i);
						result.N++;
						boolean eval = result.crit.evaluate(d.getSampleData());
						if (eval)
						{
							result.R++;
						}		
//							Logger.log.trace ("Row " + i +  " (" + d.getXref() + ") = " + result);
					}
					catch (CriterionException e)
					{
						Logger.log.error ("Problem during row handling ", e);
					}
					
					setProgress ((int)(0.2 * (double)i / (double)maxRow * 100.0));
				}
			}
			catch (DataException e)
			{
				Logger.log.error ("Problem during calculation of R/N ", e);
				//TODO: better error handling
			}

			Logger.log.info ("N: " + result.N + ", R: " + result.R);
			
			// now we calculate n and r for each pwy				
			List<File> files = FileUtils.getFiles(result.pwDir, "gpml", true);
			
			XMLReader xmlReader = null;
			
			try
			{
				xmlReader = XMLReaderFactory.createXMLReader();
			}
			catch (SAXException e)
			{
				Logger.log.error("Problem while searching pathways", e);
				return null;
			}
	
			int i = 0;

			setNote ("Analyzing pathways");

			for (File file : files)
			{
				try
				{
					PathwayParser pwyParser = new PathwayParser(file, xmlReader);
					
					Gdb gdb = SwingEngine.getCurrent().getGdbManager().getCurrentGdb();
					
					Logger.log.info ("Calculating statistics for " + pwyParser.getName());
					
					List <Xref> srcRefs = new ArrayList<Xref>();
					srcRefs.addAll (pwyParser.getGenes());
					
					try
					{
						gex.cacheData(srcRefs, new ProgressKeeper(1000), gdb);
					}
					catch (DataException e)
					{
						Logger.log.error ("Exception while caching data", e);
					}

					int cPwyTotal = srcRefs.size();
					int cPwyMeasured = 0;
					
					double cPwyPositive = 0;
					
					for (Xref srcRef : srcRefs)
					{
						if (isCancelled()) return null;
						
						List<Data> rows = gex.getCachedData().getData(srcRef);
						
						if (rows != null)
						{
							int cGeneTotal = rows.size();
							if (cGeneTotal > 0) { cPwyMeasured++; }
							int cGenePositive = 0;
							
							for (Data row : rows)
							{
								if (isCancelled()) return null;
								Logger.log.info ("Data found: " + row.getXref() + ", for sample 1: " + row.getSampleData(1));
								try
								{	
									boolean eval = result.crit.evaluate(row.getSampleData());
									if (eval) cGenePositive++;
								}
								catch (CriterionException e)
								{
									Logger.log.error ("Unknown error during statistics", e);
								}
							}
						
							// Map the rows back to the corresponding genes. 
							// "yes" is counted, weighed by the # of rows per gene. 
							// This is our "r".
							
							//This line is different from MAPPFinder: if 2 out of 3 probes are positive, count only 2/3
							cPwyPositive += (double)cGenePositive / (double)cGeneTotal;
							
							//The line below is the original MAPPFinder behaviour: 
							//  count as fully positive if at least one probe is positive
							//if (cGenePositive > 0) cPwyPositive += 1;
						}
					}
					
					double z = Stats.zscore (cPwyMeasured, cPwyPositive, result.N, result.R);						
					
					StatisticsResult sr = new StatisticsResult (file, pwyParser.getName(), cPwyMeasured, (int)Math.round (cPwyPositive), cPwyTotal, z);
					publish (sr);
				}
				catch (ParseException pe)
				{
					Logger.log.warn ("Could not parse " + file + ", ignoring", pe);
				}
				i++;
				setProgress((int)((0.2 + (0.8 * (double)i / (double)files.size())) * 100.0));				
			}
			setProgress (100);
			return result;
		}
		
		@Override
		protected void process (List<StatisticsResult> srs)
		{
			for (StatisticsResult sr : srs)
			{
				result.stm.addRow (sr);
				result.stm.sort();
				
			}
		}		
	}
	
	/**
	 * Statistics calculation for a single pathway,
	 * to be shown as a row in the statistics result table
	 */
	private static class StatisticsResult implements RowWithProperties<Column> 
	{
		private int r = 0;
		private int n = 0;
		private int total = 0;
		private String name;
		private double z = 0;
		private File f;

		File getFile() { return f; }
		
		StatisticsResult (File f, String name, int n, int r, int total, double z)
		{
			this.f = f;
			this.r = r;
			this.n = n;
			this.total = total;
			this.name = name;
			this.z = z;
		}

		public String getProperty(Column prop) 
		{
			switch (prop)
			{
			case N: return "" + n;
			case R: return "" + r;
			case TOTAL: return "" + total;
			case PATHWAY_NAME: return name;
			case PVAL: return "0.01"; //TODO
			case ZSCORE: return String.format ("%3.2f", (float)z);
			case PCT: return String.format("%3.2f%%", (n == 0 ? Float.NaN : 100.0 * (float)r / (float)n));
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
		
		public void printData(PrintStream out) throws IOException
		{
			// array of columns we are going to save
			Column[] saveColumns = new Column[] {
					Column.PATHWAY_NAME, Column.R, Column.N, Column.TOTAL, Column.PCT, Column.ZSCORE 
			};

			// print table header
			{
				boolean first = true;
				for (Column col : saveColumns)
				{
					if (!first)
					{
						out.print ("\t");
					}
					first = false;
					out.print (col.title);
				}
				out.println ();
			}
			
			// print table rows	
			for (StatisticsResult sr : rows)
			{
				boolean first = true;
				for (Column col : saveColumns)
				{
					if (!first)
					{
						out.print ("\t");
					}
					first = false;
					out.print (sr.getProperty(col));
				}
				out.println();
			}
		}
		
		/**
		 * Sort results on z-score
		 */
		public void sort()
		{
			Collections.sort (rows, new Comparator<StatisticsResult>()
			{

				public int compare(StatisticsResult arg0, StatisticsResult arg1) 
				{
					return Double.compare(arg1.z, arg0.z);
				}
			});
			fireTableDataChanged();
		}
		
		/**
	    // Returns the preferred height of a row.
	    // The result is equal to the tallest cell in the row.
	     */
	    public int getPreferredRowHeight(JTable table, int rowIndex, int margin) {
	        // Get the current default height for all rows
	        int height = table.getRowHeight();
	    
	        // Determine highest cell in the row
	        for (int c=0; c<table.getColumnCount(); c++) {
	            TableCellRenderer renderer = table.getCellRenderer(rowIndex, c);
	            Component comp = table.prepareRenderer(renderer, rowIndex, c);
	            int h = comp.getPreferredSize().height + 2*margin;
	            height = Math.max(height, h);
	        }
	        return height;
	    }
	    
	    /**
	     * The height of each row is set to the preferred height of the
	     * tallest cell in that row.
	     */
	    public void packRows(JTable table, int margin) 
	    {
	        packRows(table, 0, table.getRowCount(), margin);
	    }
	    
	    /**
	    // For each row >= start and < end, the height of a
	    // row is set to the preferred height of the tallest cell
	    // in that row.
	     */
	    public void packRows(JTable table, int start, int end, int margin) {
	        for (int r=0; r<table.getRowCount(); r++) {
	            // Get the preferred height
	            int h = getPreferredRowHeight(table, r, margin);
	    
	            // Now set the row height using the preferred height
	            if (table.getRowHeight(r) != h) {
	                table.setRowHeight(r, h);
	            }
	        }
	    }

	}

	/**
	 * Enum for possible columns in the statistics result table
	 */
	private static enum Column implements PropertyColumn
	{
		N("measured (n)"),
		R("positive (r)"),
		TOTAL("total"),
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