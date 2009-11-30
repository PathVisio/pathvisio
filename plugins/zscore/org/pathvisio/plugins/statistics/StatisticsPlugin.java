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
package org.pathvisio.plugins.statistics;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.debug.Logger;
import org.pathvisio.gex.CachedData;
import org.pathvisio.gex.GexManager;
import org.pathvisio.gex.SimpleGex;
import org.pathvisio.gui.swing.ProgressDialog;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.Preference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.swing.SimpleFileFilter;
import org.pathvisio.util.swing.TextFieldUtils;
import org.pathvisio.visualization.colorset.Criterion;

/**
 * A PathVisio plugin that lets you do simple overrepresentation analysis on pathways.
 */
public class StatisticsPlugin implements Plugin
{
	/**
	 * Preferences related to this plug-in that will be stored together with
	 * other PathVisio preferences.
	 */
	enum StatisticsPreference implements Preference
	{
		STATS_DIR_LAST_USED_PATHWAY (PreferenceManager.getCurrent().get(GlobalPreference.DIR_PWFILES)),
		STATS_DIR_LAST_USED_RESULTS (PreferenceManager.getCurrent().get(GlobalPreference.DIR_LAST_USED_PGEX)),
		MAPPFINDER_COMPATIBILITY (Boolean.toString(true)),
		STATS_RESULT_INCLUDE_FILENAME(Boolean.toString(false));

		StatisticsPreference (String defaultValue)
		{
			this.defaultValue = defaultValue;
		}

		private String defaultValue;

		public String getDefault() {
			return defaultValue;
		}

	}

	/**
	 * Plugin initialization method, registers statistics action in the Data menu
	 */
	private SwingEngine swingEngine;
	private PvDesktop desktop;

	public void init(PvDesktop aDesktop)
	{
		swingEngine = aDesktop.getSwingEngine();
		desktop = aDesktop;
		StatisticsAction statisticsAction = new StatisticsAction(swingEngine,
				desktop.getGexManager());

		Logger.log.info ("Initializing statistics plugin");
		desktop.registerMenuAction ("Data", statisticsAction);
	}

	public void done() {};

	/**
	 * Statistics menu action in the Data menu
	 */
	private static class StatisticsAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;
		private final SwingEngine se;
		private final GexManager gm;

		public StatisticsAction(SwingEngine se, GexManager gm)
		{
			super();
			this.se = se;
			this.gm = gm;
			putValue(NAME, "Statistics...");
			putValue(SHORT_DESCRIPTION, "Do simple pathway statistics");
		}

		public void actionPerformed(ActionEvent e)
		{
			SimpleGex gex = gm.getCurrentGex();
			if (gex == null)
			{
				JOptionPane.showMessageDialog(se.getFrame(), "Select an expression dataset first");
			}
			else
			{
				StatisticsDlg dlg = new StatisticsDlg();
				dlg.createAndShowDlg(se, gm);
			}
		}
	}

	/**
	 * Dialog to let the user set parameters, start calculation and view results.
	 */
	private static class StatisticsDlg
	{
		/**
		 * the panel for entering an expression, complete
		 * with list boxes for selecting operator and sample.
		 * TODO: figure out if this can be re-used in the color rule panel
		 */
		private static class CriterionPanel extends JPanel
		{
			private JTextField txtExpr;
			private JLabel lblError;
			private Criterion myCriterion = new Criterion();
			private final List<String> sampleNames;

			public Criterion getCriterion()
			{
				return myCriterion;
			}

			private void updateCriterion()
			{
				String error = myCriterion.setExpression(
						txtExpr.getText(), sampleNames);
				if (error != null)
				{
					lblError.setText(error);
				}
				else
				{
					lblError.setText ("OK");
				}
			}

			private CriterionPanel(List<String> aSampleNames)
			{
				super();
				sampleNames = aSampleNames;

				FormLayout layout = new FormLayout (
						"4dlu, min:grow, 4dlu, min:grow, 4dlu",
						"4dlu, pref, 4dlu, pref, 4dlu, [50dlu,min]:grow, 4dlu, pref, 4dlu");
				layout.setColumnGroups(new int[][]{{2,4}});
				setLayout(layout);
				CellConstraints cc = new CellConstraints();
				add (new JLabel ("Expression: "), cc.xy(2,2));
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

				add (txtExpr, cc.xyw(2,4,3));

				final JList lstOperators = new JList(Criterion.TOKENS);
				add (new JScrollPane (lstOperators), cc.xy (2,6));

				lstOperators.addMouseListener(new MouseAdapter()
				{
					public void mouseClicked(MouseEvent me)
					{
						int selectedIndex = lstOperators.getSelectedIndex();
						if (selectedIndex >= 0)
						{
							String toInsert = Criterion.TOKENS[selectedIndex];
							TextFieldUtils.insertAtCursorWithSpace(txtExpr, toInsert);
						}
						// after clicking on the list, move focus back to text field so
						// user can continue typing
						txtExpr.requestFocusInWindow();
						// on Mac L&F, requesting focus leads to selecting the whole field
						// move caret a bit to work around. Last char is a space anyway.
						txtExpr.setCaretPosition(txtExpr.getDocument().getLength() - 1);
					}
				} );

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
						// after clicking on the list, move focus back to text field so
						// user can continue typing
						txtExpr.requestFocusInWindow();
						// on Mac L&F, requesting focus leads to selecting the whole field
						// move caret a bit to work around. Last char is a space anyway.
						txtExpr.setCaretPosition(txtExpr.getDocument().getLength() - 1);
					}
				} );

				add (new JScrollPane (lstSamples), cc.xy (4,6));
				lblError = new JLabel("OK");
				add (lblError, cc.xyw (2,8,3));

				txtExpr.requestFocus();
			}
		}

		private CriterionPanel critPanel;
		private JButton btnSave;
		private StatisticsResult result = null;
		private JButton btnCalc;
		private GexManager gm;
		private SwingEngine se;
		private JDialog dlg;
		private JTable tblResult;
		private JLabel lblResult;

		/**
		 * Save the statistics results to tab delimted text
		 */
		private void doSave()
		{
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogTitle("Save results");
			jfc.setFileFilter(new SimpleFileFilter ("Tab delimited text", "*.txt", true));
			jfc.setDialogType(JFileChooser.SAVE_DIALOG);
			jfc.setCurrentDirectory(PreferenceManager.getCurrent().getFile(StatisticsPreference.STATS_DIR_LAST_USED_RESULTS));
			if (jfc.showDialog(dlg, "Save") == JFileChooser.APPROVE_OPTION)
			{
				File f = jfc.getSelectedFile();
				PreferenceManager.getCurrent().setFile(StatisticsPreference.STATS_DIR_LAST_USED_RESULTS, jfc.getCurrentDirectory());
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

		/**
		 * Pop up the statistics dialog
		 */
		private void createAndShowDlg(SwingEngine aSwingEngine, GexManager gm)
		{
			this.se = aSwingEngine;
			this.gm = gm;
			dlg = new JDialog (se.getFrame(), "Pathway statistics", false);

			FormLayout layout = new FormLayout (
					"4dlu, pref:grow, 4dlu, pref, 4dlu",
					"4dlu, fill:[pref,250dlu], 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, fill:min:grow");
			dlg.setLayout(layout);

			CellConstraints cc = new CellConstraints();

			critPanel = new CriterionPanel(gm.getCurrentGex().getSampleNames());
			dlg.add (critPanel, cc.xyw (2,2,3));

			dlg.add (new JLabel ("Pathway Directory: "), cc.xy (2,4));
			final JTextField txtDir = new JTextField(40);
			txtDir.setText(PreferenceManager.getCurrent().get(StatisticsPreference.STATS_DIR_LAST_USED_PATHWAY));
			dlg.add (txtDir, cc.xy(2,6));
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
						String newVal = "" + jfc.getSelectedFile();
						txtDir.setText(newVal);
						PreferenceManager.getCurrent().set(StatisticsPreference.STATS_DIR_LAST_USED_PATHWAY, newVal);
					}
				}
			});

			dlg.add (btnDir, cc.xy (4,6));

			JPanel pnlButtons = new JPanel();

			btnCalc = new JButton ("Calculate");
			pnlButtons.add (btnCalc);

			btnSave = new JButton ("Save results");
			pnlButtons.add (btnSave);
			btnSave.setEnabled(false);

			dlg.add (pnlButtons, cc.xyw (2,8,3));

			lblResult = new JLabel(); //Label for adding general results after analysis is done

			dlg.add(lblResult, cc.xyw(2, 10, 3));

			tblResult = new JTable ();
			tblResult.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent me)
				{
					int row = tblResult.getSelectedRow();
					final StatisticsPathwayResult sr = ((StatisticsTableModel)(tblResult.getModel())).getRow(row);

						//TODO: here I want to use SwingEngine.openPathway, but I need to
						// be able to wait until the process is finished!
					se.openPathway(sr.getFile());
				}
			});

			dlg.add (new JScrollPane (tblResult), cc.xyw (2,12,3));

			btnCalc.addActionListener(new ActionListener ()
			{
				public void actionPerformed(ActionEvent ae)
				{
					File pwDir = new File (txtDir.getText());
					btnCalc.setEnabled(false);
					doCalculate (pwDir, critPanel.getCriterion());
				}
			});

			btnSave.addActionListener(new ActionListener ()
			{
				public void actionPerformed(ActionEvent ae)
				{
					doSave();
				}
			});

			dlg.pack();
			dlg.setSize(600, 600); //TODO store preference
			dlg.setLocationRelativeTo(se.getFrame());
			dlg.setVisible(true);
		}

		/**
		 * asynchronous statistics calculation function
		 */
		private void doCalculate(final File pwDir, final Criterion crit)
		{
			btnSave.setEnabled (false);

			ProgressKeeper pk = new ProgressKeeper(100);
			final ZScoreWorker worker = new ZScoreWorker(crit, pwDir, gm.getCachedData(), se.getGdbManager().getCurrentGdb(), pk);
			ProgressDialog d = new ProgressDialog(
					JOptionPane.getFrameForComponent(dlg),
					"Calculating Z-scores", pk, true, true
			);
			worker.execute();
			d.setVisible(true);
		}

		private class ZScoreWorker extends SwingWorker <StatisticsResult, Void>
		{
			private final ZScoreCalculator calculator;
			private ProgressKeeper pk;

			// temporary model that will be filled with intermediate results.
			private StatisticsTableModel temp;
			private boolean useMappFinder;

			ZScoreWorker(Criterion crit, File pwDir, CachedData cache, IDMapper gdb, ProgressKeeper pk)
			{
				this.pk = pk;
				calculator = new ZScoreCalculator (crit, pwDir, cache, gdb, pk);
				temp = new StatisticsTableModel();
				temp.setColumns(new Column[] {Column.PATHWAY_NAME, Column.R, Column.N, Column.TOTAL, Column.PCT, Column.ZSCORE});
				tblResult.setModel(temp);
				useMappFinder = PreferenceManager.getCurrent().getBoolean(StatisticsPreference.MAPPFINDER_COMPATIBILITY);
			}

			@Override
			protected StatisticsResult doInBackground() throws IDMapperException
			{
				StatisticsResult result;

				if (useMappFinder)
				{
					result = calculator.calculateMappFinder();
				}
				else
				{
					result = calculator.calculateAlternative();
				}
				return result;
			}

			@Override
			protected void done()
			{
				if (!pk.isCancelled())
				{
					StatisticsResult result;
					try {
						result = get();
						if (result.stm.getRowCount() == 0)
						{
							JOptionPane.showMessageDialog(null,
							"0 results found, did you choose the right directory?");
						}
						else
						{
							// replace temp tableModel with definitive one
							tblResult.setModel(result.stm);
							lblResult.setText(
								"<html>Rows in data (N): " + result.getBigN() +
								"<br>Rows meeting criterion (R): " + result.getBigR()
							);
							StatisticsDlg.this.result = result;
							//dlg.pack();
						}
					}
					catch (InterruptedException e)
					{
						JOptionPane.showMessageDialog(null,
								"Exception while calculating statistics\n" + e.getMessage());
						Logger.log.error ("Statistics calculation exception", e);
					}
					catch (ExecutionException e)
					{
						JOptionPane.showMessageDialog(null,
							"Exception while calculating statistics\n" + e.getMessage());
						Logger.log.error ("Statistics calculation exception", e);
					}
				}
				btnCalc.setEnabled(true);
				btnSave.setEnabled(true);
			}
		}

	}

}