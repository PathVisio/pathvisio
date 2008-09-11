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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingworker.SwingWorker;
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
			SimpleGex gex = GexManager.getCurrent().getCurrentGex();
			if (gex == null)
			{
				JOptionPane.showMessageDialog(SwingEngine.getCurrent().getFrame(), "Select an expression dataset first");
			}
			else
			{
				StatisticsDlg dlg = new StatisticsDlg();
				dlg.createAndShowDlg();
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
			
			final JDialog dlg = new JDialog (SwingEngine.getCurrent().getFrame(), "Pathway statistics", false);
			
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
			
			JButton btnCalc = new JButton ("Calculate");
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
					SwingEngine.getCurrent().openPathway(sr.getFile());
				}

			});

			dlg.add (new JScrollPane (tblResult), cc.xyw (2,16,3));
			
			btnCalc.addActionListener(new ActionListener (){
	
				public void actionPerformed(ActionEvent ae) 
				{
					File pwDir = new File (txtDir.getText());
					String expr = txtExpr.getText();
					
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
							PrintStream out = new PrintStream (new FileOutputStream(f));
							
							//TODO: it may have happened that the criterion was changed by the user,
							// so it needs to be stored in a temp variable  
							out.println ("Statistics results for " + new Date());
							out.println ("Dataset: " + GexManager.getCurrent().getCurrentGex().getDbName());
							out.println ("Criterion: " + myCriterion.getExpression());
							out.println();
							
							((StatisticsTableModel)(tblResult.getModel())).printData(out);
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
			dlg.setLocationRelativeTo(SwingEngine.getCurrent().getFrame());
			dlg.setVisible(true);
		}

		/**
		 * asynchronous statistics calculation function
		 */
		//TODO: make this a SwingWorker class
		private void doCalculate(final File pwDir, final Criterion crit, final JTable resultTable, JDialog parentFrame)
		{
			final int TOTALWORK = 1000;

			final ProgressMonitor pmon = new ProgressMonitor(
					parentFrame, "Pathway search", "searching pathways...",
					0, TOTALWORK);
			
			btnSave.setEnabled (false);
			final StatisticsTableModel stm = new StatisticsTableModel();
			stm.setColumns(new Column[] {Column.PATHWAY_NAME, Column.R, Column.N, Column.TOTAL, Column.PCT, Column.ZSCORE});
			resultTable.setModel (stm);

			SwingWorker<Boolean, StatisticsResult> worker = new SwingWorker<Boolean, StatisticsResult>() 
			{
				@Override
				protected Boolean doInBackground()
				{
					pmon.setProgress (0);
					pmon.setNote("Analyzing data");
					SimpleGex gex = GexManager.getCurrent().getCurrentGex();
					
					// first we calculate N and R
					
					int N = 0;
					int R = 0;
					try
					{
						int maxRow = gex.getMaxRow();
						for (int i = 0; i < maxRow; ++i)
						{
							if (pmon.isCanceled()) return false;
							Data d = gex.getRow(i);
							N++;
							boolean result = crit.evaluate(d.getSampleData());
							if (result)
							{
								R++;
							}		
//							Logger.log.trace ("Row " + i +  " (" + d.getXref() + ") = " + result);
							pmon.setProgress ((int)(0.2 * (double)i / (double)maxRow * (double)TOTALWORK));
						}
					}
					catch (Exception e)
					{
						Logger.log.error ("Problem during calculation of R/N ", e);
						//TODO: better error handling
					}

					Logger.log.info ("N: " + N + ", R: " + R);
					
					// now we calculate n and r for each pwy				
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

					pmon.setNote ("Analyzing pathways");

					for (File file : files)
					{
						try
						{
							PathwayParser pwyParser = new PathwayParser(file, xmlReader);
							
							// Step 1: map the genes in the pathway to a common system.
							// This common system can be Ensembl, but it doesn't have to be.
							// as long as the same single common system is used for all pathways
							// Make the list unique. This is our "n".
							
							Gdb gdb = SwingEngine.getCurrent().getGdbManager().getCurrentGdb();
							
							Logger.log.info ("Calculating statistics for " + pwyParser.getName());
							
							List <Xref> genes = new ArrayList<Xref>();
							genes.addAll (pwyParser.getGenes());
							Map <String, List<Data>> ensGenes = new HashMap <String, List<Data>> ();
							
							try
							{
								gex.cacheData(genes, new ProgressKeeper(1000), gdb);
							}
							catch (SQLException e)
							{
								Logger.log.error ("Exception while caching data", e);
							}

							for (Xref ref : genes)
							{
								List<Data> datas  = gex.getCachedData().getData(ref);
								
								for (String ensId : gdb.ref2EnsIds(ref))
								{
									if (pmon.isCanceled()) return false;
									Logger.log.info ("Mapping: " + ensId);
									ensGenes.put (ensId, datas);								
								}
							}
							
							int cPwyTotal = ensGenes.size();
							int cPwyMeasured = 0;
							
							// Step 2: find the corresponding rows in the Gex. There could be more than one row per gene, this is ok.
							
							double cPwyPositive = 0;
							
							for (String ensGene : ensGenes.keySet())
							{
								if (pmon.isCanceled()) return false;
								List<Data> rows = ensGenes.get (ensGene);
								
								if (rows != null)
								{
									int cGeneTotal = rows.size();
									if (cGeneTotal > 0) { cPwyMeasured++; }
									int cGenePositive = 0;
									
									for (Data row : rows)
									{
										if (pmon.isCanceled()) return false;
										Logger.log.info ("Data found: " + row.getXref() + ", for sample 1: " + row.getSampleData(1));
										try
										{	
											boolean result = crit.evaluate(row.getSampleData());
											if (result) cGenePositive++;
										}
										catch (Exception e)
										{
											Logger.log.error ("Unknown error during statistics", e);
										}
									}
								
									// Step 4: Map the rows back to the corresponding genes. "yes" is counted, weighed by the # of rows per gene. This is our "r".
									
									//This line is different from MAPPFinder: if 2 out of 3 probes are positive, count only 2/3
									cPwyPositive += (double)cGenePositive / (double)cGeneTotal;
									
									//The line below is the original MAPPFinder behaviour: 
									//  count as fully positive if at least one probe is positive
									//if (cGenePositive > 0) cPwyPositive += 1;
								}
							}
							
							double z = Stats.zscore (cPwyMeasured, cPwyPositive, N, R);						
							
							StatisticsResult sr = new StatisticsResult (file, pwyParser.getName(), cPwyMeasured, (int)Math.round (cPwyPositive), cPwyTotal, z);
							publish (sr);
						}
						catch (ParseException pe)
						{
							Logger.log.warn ("Could not parse " + file + ", ignoring", pe);
						}
						i++;
						pmon.setProgress((int)((0.2 + (0.8 * (double)i / (double)files.size())) * (double)TOTALWORK));				
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
						stm.sort();
						stm.packRows(resultTable, 2);
					}
				}
				
				@Override
				protected void done()
				{
					btnSave.setEnabled(true);
				}
			};
			
			worker.execute();
			
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