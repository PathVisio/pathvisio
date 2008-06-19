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
package org.pathvisio.gui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.Engine;
import org.pathvisio.data.DBConnector;
import org.pathvisio.data.DBConnectorSwing;
import org.pathvisio.data.GexManager;
import org.pathvisio.data.GexTxtImporter;
import org.pathvisio.data.ImportInformation;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.progress.SwingProgressKeeper;
import org.pathvisio.model.DataSource;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.util.ProgressKeeper.ProgressEvent;
import org.pathvisio.util.ProgressKeeper.ProgressListener;
import org.pathvisio.util.swing.SimpleFileFilter;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.nexes.wizard.Wizard;
import com.nexes.wizard.WizardPanelDescriptor;

public class GexImportWizard extends Wizard 
{
	private ImportInformation importInformation = new ImportInformation();
	
    FilePage fpd = new FilePage();
    HeaderPage hpd = new HeaderPage();
    ColumnPage cpd = new ColumnPage();
    ImportPage ipd = new ImportPage();
    
	GexImportWizard()
	{
		getDialog().setTitle ("Expression data import wizard");
		
        this.registerWizardPanel(FilePage.IDENTIFIER, fpd);
        this.registerWizardPanel(HeaderPage.IDENTIFIER, hpd);
        this.registerWizardPanel(ColumnPage.IDENTIFIER, cpd);
        this.registerWizardPanel(ImportPage.IDENTIFIER, ipd);
        
        setCurrentPanel(FilePage.IDENTIFIER);        
	}
		
	private class FilePage extends WizardPanelDescriptor implements ActionListener
	{
	    public static final String IDENTIFIER = "FILE_PAGE";
	    static final String ACTION_INPUT = "input";
	    static final String ACTION_OUTPUT = "output";
	    static final String ACTION_GDB = "gdb";
	    
	    private JTextField txtInput;
	    private JTextField txtOutput;
	    private JTextField txtGdb;
	    private JButton btnGdb;
	    private JButton btnInput;
	    private JButton btnOutput;
	    private boolean txtFileComplete = false;
	    
		/**
		 * Stores the given {@link File} pointing to the file containing the expresssion
		 * data in text form to the {@link ImportInformation} object
		 * @param file
		 */
		private void updateTxtFile() 
		{
			String fileName = txtInput.getText();
			File file = new File (fileName);
			txtFileComplete = true;
			if (!file.exists()) 
			{
				setErrorMessage("Specified file to import does not exist");
				txtFileComplete = false;
			}
			if (!file.canRead()) 
			{
				setErrorMessage("Can't access specified file containing expression data");
				txtFileComplete = false;
			}
		    getWizard().setNextFinishButtonEnabled(txtFileComplete);

		    // make outFile equal to fileName without extension, if any
		    int pos = fileName.lastIndexOf(".");
			String outFile = ((pos >= 0) ? fileName.substring(0, pos) : fileName);
		    txtOutput.setText(outFile);
			
		    if (txtFileComplete)
			{
				setErrorMessage(null);
				txtFileComplete = true;
			}
		}

		public void aboutToDisplayPanel()
		{
	        getWizard().setNextFinishButtonEnabled(txtFileComplete);
			getWizard().setPageTitle ("Choose file locations");
		}
		
	    public FilePage() 
	    {
	        super(IDENTIFIER);
	    }
	    
	    public Object getNextPanelDescriptor() 
	    {
	        return HeaderPage.IDENTIFIER;
	    }
	    
	    public Object getBackPanelDescriptor() 
	    {
	        return null;
	    }  

		protected JPanel createContents()
		{			
			txtInput = new JTextField(40);
		    txtOutput = new JTextField(40);
		    txtGdb = new JTextField(40);
		    btnGdb = new JButton ("Browse");
		    btnInput = new JButton ("Browse");
		    btnOutput = new JButton ("Browse");
		    
		    FormLayout layout = new FormLayout (
		    		"right:pref, 3dlu, pref, 3dlu, pref",
		    		"p, 3dlu, p, 3dlu, p");
		    
		    PanelBuilder builder = new PanelBuilder(layout);
		    builder.setDefaultDialogBorder();
		    
		    CellConstraints cc = new CellConstraints();
			
			builder.addLabel ("Input file", cc.xy (1,1));
			builder.add (txtInput, cc.xy (3,1));
			builder.add (btnInput, cc.xy (5,1));
			builder.addLabel ("Output file", cc.xy (1,3));
			builder.add (txtOutput, cc.xy (3,3));
			builder.add (btnOutput, cc.xy (5,3));
			builder.addLabel ("Gene database", cc.xy (1,5));
			builder.add (txtGdb, cc.xy (3,5));
			builder.add (btnGdb, cc.xy (5,5));
			
			btnInput.addActionListener(this);
			btnInput.setActionCommand(ACTION_INPUT);
			btnOutput.addActionListener(this);
			btnOutput.setActionCommand(ACTION_OUTPUT);
			btnGdb.addActionListener(this);
			btnGdb.setActionCommand(ACTION_GDB);
			
			txtInput.getDocument().addDocumentListener(new DocumentListener()
			{
				public void changedUpdate(DocumentEvent arg0) 
				{
					updateTxtFile();
				}

				public void insertUpdate(DocumentEvent arg0) 
				{
					updateTxtFile();
				}

				public void removeUpdate(DocumentEvent arg0) 
				{
					updateTxtFile();					
				}
				
			});
			txtGdb.setText(
					Engine.getCurrent().getPreferences().get(GlobalPreference.DB_GDB_CURRENT)
			);
			return builder.getPanel();
		}

		public void aboutToHidePanel() 
		{
			importInformation.setTxtFile(new File (txtInput.getText()));
			importInformation.guessSettings();
			importInformation.setDbName (txtOutput.getText());
	    }

		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			
			if(ACTION_GDB.equals(action)) {
				StandaloneActions.selectGeneDbAction.actionPerformed(
						new ActionEvent(this, 1, "")
				);
				txtGdb.setText(
						Engine.getCurrent().getPreferences().get(GlobalPreference.DB_GDB_CURRENT)
				);
			} else if(ACTION_INPUT.equals(action)) {
				
				File defaultdir = Engine.getCurrent().getPreferences().getFile(GlobalPreference.DIR_LAST_USED_EXPRESSION_IMPORT);
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(defaultdir);
				jfc.addChoosableFileFilter(new SimpleFileFilter("Data files", "*.txt|*.csv", true));
				int result = jfc.showDialog(null, "Select data file");
				if (result == JFileChooser.APPROVE_OPTION)
				{
					File f = jfc.getSelectedFile();
					defaultdir = jfc.getCurrentDirectory();
					Engine.getCurrent().getPreferences().setFile(GlobalPreference.DIR_LAST_USED_EXPRESSION_IMPORT, defaultdir);
					txtInput.setText("" + f);
					updateTxtFile ();
				}
			} else if(ACTION_OUTPUT.equals(action)) {
				try {
					DBConnector dbConn = GexManager.getCurrent().getDBConnector();
						String output = ((DBConnectorSwing)dbConn).openNewDbDialog(
								getPanelComponent(), importInformation.getDbName()	
						);
						if(output != null) {
							txtOutput.setText(output);
						}
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(
							getPanelComponent(), "The database connector is not supported"
							
					);
					Logger.log.error("No gex database connector", ex);
				}
			}
		}

	}
	
	private class HeaderPage extends WizardPanelDescriptor 
	{
	    public static final String IDENTIFIER = "HEADER_PAGE";
		private PreviewTableModel ptm;
		private JTable tblPreview;
		private JRadioButton rbSepTab;
		private JRadioButton rbSepComma;
		private JRadioButton rbSepSemi;
		private JRadioButton rbSepSpace;
		private JRadioButton rbSepOther;
		
	    public HeaderPage() 
	    {
	        super(IDENTIFIER);
	    }
	    
	    public Object getNextPanelDescriptor() 
	    {
	        return ColumnPage.IDENTIFIER;
	    }
	    
	    public Object getBackPanelDescriptor() 
	    {
	        return FilePage.IDENTIFIER;
	    }  
	    
	    @Override
		protected Component createContents()
		{
		    FormLayout layout = new FormLayout (
		    		"pref, 3dlu, pref, 3dlu, pref, pref:grow",
		    		"p, 3dlu, p, 3dlu, p, 15dlu, fill:[100dlu,min]:grow");
		    
		    PanelBuilder builder = new PanelBuilder(layout);
		    builder.setDefaultDialogBorder();
		    
		    CellConstraints cc = new CellConstraints();
			
			rbSepTab = new JRadioButton ("tab");
			rbSepComma = new JRadioButton ("comma");
			rbSepSemi = new JRadioButton ("semicolon");
			rbSepSpace = new JRadioButton ("space");
			rbSepOther = new JRadioButton ("other");
			ButtonGroup bgSeparator = new ButtonGroup();
			bgSeparator.add (rbSepTab);
			bgSeparator.add (rbSepComma);
			bgSeparator.add (rbSepSemi);
			bgSeparator.add (rbSepSpace);
			bgSeparator.add (rbSepOther);
			
			builder.add (rbSepTab, cc.xy(1,1));
			builder.add (rbSepComma, cc.xy(1,3));
			builder.add (rbSepSemi, cc.xy(1,5));
			builder.add (rbSepSpace, cc.xy(3,1));
			builder.add (rbSepOther, cc.xy(3,3));
			
			final JTextField txtOther = new JTextField(3);
			builder.add (txtOther, cc.xy(5, 3));

			ptm = new PreviewTableModel(importInformation);
			tblPreview = new JTable(ptm);
			tblPreview.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			JScrollPane scrTable = new JScrollPane(tblPreview);
			
			builder.add (scrTable, cc.xyw(1,7,6));
			
			txtOther.addActionListener(new ActionListener () {

				public void actionPerformed(ActionEvent arg0) 
				{
					importInformation.setDelimiter (txtOther.getText());
					importInformation.guessSettings();
					ptm.refresh();
					rbSepOther.setSelected (true);
				}

				
			})
			;
						
			rbSepComma.addActionListener(new ActionListener()
			{
				public void actionPerformed (ActionEvent ae)
				{
					importInformation.setDelimiter(",");
					ptm.refresh();
				}
				
			});
			rbSepTab.addActionListener(new ActionListener()
			{
				public void actionPerformed (ActionEvent ae)
				{
					importInformation.setDelimiter("\t");
					ptm.refresh();
				}
				
			});
			rbSepSemi.addActionListener(new ActionListener()
			{
				public void actionPerformed (ActionEvent ae)
				{
					importInformation.setDelimiter(";");
					ptm.refresh();
				}
			});
			rbSepSpace.addActionListener(new ActionListener()
			{
				public void actionPerformed (ActionEvent ae)
				{
					importInformation.setDelimiter(" ");
					ptm.refresh();
				}
				
			});
			return builder.getPanel();
		}
	    
	    public void aboutToDisplayPanel()
	    {
			getWizard().setPageTitle ("Choose data delimiter");

	    	hpd.ptm.refresh();
	    	String del = importInformation.getDelimiter();
	    	if (del.equals ("\t"))
	    	{
	    		rbSepTab.setSelected(true);
	    	}
	    	else if (del.equals (","))
			{
	    		rbSepComma.setSelected(true);
			}
	    	else if (del.equals (";"))
			{
	    		rbSepSemi.setSelected(true);
			}
	    	else if (del.equals (" "))
			{
	    		rbSepSpace.setSelected(true);
			}
	    	else
	    	{
	    		rbSepOther.setSelected (true);
	    	}
	    }
	}
	
	private class ColumnPage extends WizardPanelDescriptor 
	{
	    public static final String IDENTIFIER = "COLUMN_PAGE";

	    private ColumnTableModel ctm;
		private JTable tblColumn;
		
	    private JComboBox cbColId;
	    private JComboBox cbColSyscode;
	    private JRadioButton rbSyscodeYes;
	    private JRadioButton rbSyscodeNo;
	    private DataSourceCombo cbDataSource;
	    
	    public ColumnPage() 
	    {
	        super(IDENTIFIER);
	    }
	    
	    public Object getNextPanelDescriptor() 
	    {
	        return ImportPage.IDENTIFIER;
	    }
	    
	    public Object getBackPanelDescriptor() 
	    {
	        return HeaderPage.IDENTIFIER;
	    }  

	    @Override
		protected JPanel createContents() 
		{
		    FormLayout layout = new FormLayout (
		    		"pref, 7dlu, pref:grow",
		    		"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, fill:[100dlu,min]:grow");
		    
		    PanelBuilder builder = new PanelBuilder(layout);
		    builder.setDefaultDialogBorder();
		    
		    CellConstraints cc = new CellConstraints();
			
			rbSyscodeYes = new JRadioButton("Select a column to specify system code");
			rbSyscodeNo = new JRadioButton("Use the same system code for all rows");
			ButtonGroup bgSyscodeCol = new ButtonGroup ();
			bgSyscodeCol.add (rbSyscodeYes);
			bgSyscodeCol.add (rbSyscodeNo);
			
			cbColId = new JComboBox();
			cbColSyscode = new JComboBox();			

			cbDataSource = new DataSourceCombo();
			cbDataSource.initItems();

			ctm = new ColumnTableModel(importInformation);
			tblColumn = new JTable(ctm);
			tblColumn.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			tblColumn.setDefaultRenderer(Object.class, ctm.getTableCellRenderer());
			JScrollPane scrTable = new JScrollPane(tblColumn);

			builder.addLabel ("Select primary identifier column:", cc.xy(1,1));
			builder.add (cbColId, cc.xy(3,1));

			builder.add (rbSyscodeYes, cc.xyw(1,3,3));
			builder.add (cbColSyscode, cc.xy(3,5));
			builder.add (rbSyscodeNo, cc.xyw (1,7,3));
			builder.add (cbDataSource, cc.xy (3,9));
			
			builder.add (scrTable, cc.xyw(1,11,3));
			
			ActionListener rbAction = new ActionListener() {
				public void actionPerformed (ActionEvent ae)
				{
					boolean result = (ae.getSource() == rbSyscodeYes);
					importInformation.setSyscodeColumn(result);
			    	columnPageRefresh();
				}
			};
			rbSyscodeNo.addActionListener(rbAction);
			rbSyscodeYes.addActionListener(rbAction);
			
			cbDataSource.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{
					DataSource ds = cbDataSource.getSelectedDataSource();
					importInformation.setDataSource(ds);
					columnPageRefresh();
				}
			});
			cbColSyscode.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{
					importInformation.setCodeColumn(cbColSyscode.getSelectedIndex());
					columnPageRefresh();
				}
			});
			cbColId.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{
					importInformation.setIdColumn(cbColId.getSelectedIndex());
			    	columnPageRefresh();
				}
			});
			return builder.getPanel();
		}
	    
	    private void columnPageRefresh()
	    {
	    	String error = null;
			if (importInformation.getSyscodeColumn())
			{
				rbSyscodeYes.setSelected (true);
				cbColSyscode.setEnabled (true);
				cbDataSource.setEnabled (false);

				if (importInformation.getIdColumn() == importInformation.getCodeColumn())
	    		{
	    			error = "System code column and Id column can't be the same";
	    		}
			}
			else
			{
				rbSyscodeNo.setSelected (true);
				cbColSyscode.setEnabled (false);
				cbDataSource.setEnabled (true);
			}
		    getWizard().setNextFinishButtonEnabled(error == null);
		    getWizard().setErrorMessage(error == null ? "" : error);
			getWizard().setPageTitle ("Choose column types");
			
	    	ctm.refresh();
	    }
	    
	    private void refreshComboBoxes()
	    {
	    	cbDataSource.setSelectedDataSource (importInformation.getDataSource());
			cbColId.setSelectedIndex(importInformation.getIdColumn());
			cbColSyscode.setSelectedIndex(importInformation.getCodeColumn());
	    }
	    
	    public void aboutToDisplayPanel()
	    {			
	    	String[] cn = importInformation.getColNames();
	    	cbColId.setModel(new DefaultComboBoxModel(cn));
	    	cbColSyscode.setModel(new DefaultComboBoxModel(cn));
			
			columnPageRefresh();
			refreshComboBoxes();
			
	    	ctm.refresh();
	    }
	    
	    @Override
	    public void aboutToHidePanel()
	    {
	    	importInformation.setSyscodeColumn(rbSyscodeYes.isSelected());
	    	if (rbSyscodeYes.isSelected())
	    	{
	    	}
	    	else
	    	{
		    	importInformation.setDataSource(
		    			DataSource.getByFullName(("" + cbDataSource.getSelectedItem())));
	    	}
	    }
	}
	
	private class ImportPage extends WizardPanelDescriptor implements ProgressListener
	{
	    public static final String IDENTIFIER = "IMPORT_PAGE";
		
	    public ImportPage() 
	    {
	        super(IDENTIFIER);
	    }
	    
	    public Object getNextPanelDescriptor() 
	    {
	        return FINISH;
	    }
	    
	    public Object getBackPanelDescriptor() 
	    {
	        return ColumnPage.IDENTIFIER;
	    }  
	    
	    private JProgressBar progressSent;
	    private JTextArea progressText;
	    private SwingProgressKeeper pk;
	    
		protected JPanel createContents()
		{
	    	FormLayout layout = new FormLayout(
	    			"fill:[100dlu,min]:grow",
	    			"pref, fill:pref:grow"
	    	);
	    	
	    	DefaultFormBuilder builder = new DefaultFormBuilder(layout);
	    	builder.setDefaultDialogBorder();
	    	
        	pk = new SwingProgressKeeper((int)1E6);
        	pk.addListener(this);
	    	progressSent = pk.getJProgressBar();
	        builder.append(progressSent);
	        builder.nextLine();
	        
	        progressText = new JTextArea();
	       
			builder.append(new JScrollPane(progressText));
			return builder.getPanel();
		}
	    
	    public void setProgressValue(int i)
	    {
	        progressSent.setValue(i);
	    }

	    public void setProgressText(String msg) 
	    {
	        progressText.setText(msg);
	    }

	    public void aboutToDisplayPanel() 
	    {
			getWizard().setPageTitle ("Preform import");
	        setProgressValue(0);
	        setProgressText("");

	        getWizard().setNextFinishButtonEnabled(false);
	        getWizard().setBackButtonEnabled(false);
	    }

	    public void displayingPanel() 
	    {
			SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
				protected Void doInBackground() throws Exception {
					pk.setTaskName("Importing pathway");
					try 
					{
						GexTxtImporter.importFromTxt(
								importInformation, 
								pk, 
								SwingEngine.getCurrent().getGdbManager().getCurrentGdb()
						);

						getWizard().setNextFinishButtonEnabled(true);
						getWizard().setBackButtonEnabled(true);
					} 
					catch (Exception e) 
					{
						setProgressValue(0);
						setProgressText("An Error Has Occurred");

						getWizard().setBackButtonEnabled(true);
					} finally {
						pk.finished();
					}
					return null;
				}
			};
			sw.execute();
	    }

		public void progressEvent(ProgressEvent e) {
			if(e.getType() == ProgressEvent.REPORT) {
				progressText.append(e.getProgressKeeper().getReport() + "\n");
			}
		}

	}
}
