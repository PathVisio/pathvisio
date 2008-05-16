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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.pathvisio.data.GexTxtImporter;
import org.pathvisio.data.ImportInformation;
import org.pathvisio.gui.swing.progress.SwingProgressKeeper;
import org.pathvisio.model.DataSource;

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
		
	private class FilePage extends WizardPanelDescriptor 
	{
	    public static final String IDENTIFIER = "FILE_PAGE";

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
		private void setTxtFile(File file) 
		{
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
			if (txtFileComplete)
			{
				importInformation.setTxtFile(file);
				String fileName = file.toString();
				txtInput.setText(file.toString());
				txtOutput.setText(fileName.replace(fileName.substring(
						fileName.lastIndexOf(".")), ""));
				importInformation.setDbName (txtOutput.getText());
				importInformation.guessSettings();
				setErrorMessage(null);
				txtFileComplete = true;
			}
		}

		public void aboutToDisplayPanel()
		{
	        getWizard().setNextFinishButtonEnabled(txtFileComplete);
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
			JPanel result = new JPanel();
			
			result.setLayout (new BorderLayout());
			
			txtInput = new JTextField();
		    txtOutput = new JTextField();
		    txtGdb = new JTextField();
		    btnGdb = new JButton ("Browse");
		    btnInput = new JButton ("Browse");
		    btnOutput = new JButton ("Browse");
		    
			JPanel gridPanel = new JPanel();
			gridPanel.setLayout (new GridLayout (3,3));
			
			gridPanel.add (new JLabel ("Input file"));
			gridPanel.add (txtInput);
			gridPanel.add (btnInput);
			gridPanel.add (new JLabel ("Output file"));
			gridPanel.add (txtOutput);
			gridPanel.add (btnOutput);
			gridPanel.add (new JLabel ("Gene database"));
			gridPanel.add (txtGdb);
			gridPanel.add (btnGdb);
	
			result.add (new JLabel("File locations"), BorderLayout.NORTH);
			result.add (gridPanel, BorderLayout.CENTER);
			
			btnInput.addActionListener(new ActionListener()
			{
				public void actionPerformed (ActionEvent ae)
				{
					//TODO: more sensible default dir
					File defaultdir = new File ("/home/martijn/prg/pathvisio-trunk/example-data/sample_data_1.txt");
					JFileChooser jfc = new JFileChooser();
					jfc.setSelectedFile(defaultdir);
					int result = jfc.showDialog(null, "Select input file");
					if (result == JFileChooser.APPROVE_OPTION)
					{
						File f = jfc.getSelectedFile();
						setTxtFile (f);
					}
				}
			});
			
			return result;
		}

		public void aboutToHidePanel() 
		{
	        setTxtFile(new File (txtInput.getText()));
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
			JPanel result = new JPanel();
			result.setLayout (new BorderLayout());
			
			JPanel topPanel = new JPanel();
			topPanel.setLayout (new BorderLayout());
			
			JPanel settingsPanel = new JPanel();
			
			Box bxGroup = Box.createVerticalBox();
			ButtonGroup bgSeparator = new ButtonGroup();
			rbSepTab = new JRadioButton ("tab");
			rbSepComma = new JRadioButton ("comma");
			rbSepSemi = new JRadioButton ("semicolon");
			rbSepSpace = new JRadioButton ("space");
			rbSepOther = new JRadioButton ("other");
			bxGroup.add (rbSepTab);
			bxGroup.add (rbSepComma);
			bxGroup.add (rbSepSemi);
			bxGroup.add (rbSepSpace);
			Box b1 = Box.createHorizontalBox();
			b1.add (rbSepOther);
			final JTextField txtOther = new JTextField(3);
			b1.add (txtOther);
			bxGroup.add (b1);
			bgSeparator.add (rbSepTab);
			bgSeparator.add (rbSepComma);
			bgSeparator.add (rbSepSemi);
			bgSeparator.add (rbSepSpace);
			bgSeparator.add (rbSepOther);

			settingsPanel.add (bxGroup);

			topPanel.add (settingsPanel, BorderLayout.NORTH);
			ptm = new PreviewTableModel(importInformation);
			tblPreview = new JTable(ptm);
			tblPreview.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			JScrollPane scrTable = new JScrollPane(tblPreview);
			topPanel.add (scrTable, BorderLayout.CENTER);
			
			result.add (new JLabel("Header page"), BorderLayout.NORTH);
			result.add (topPanel, BorderLayout.CENTER);
			
			txtOther.addActionListener(new ActionListener () {

				public void actionPerformed(ActionEvent arg0) 
				{
					importInformation.setDelimiter (txtOther.getText());
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
			return result;
		}
	    
	    public void aboutToDisplayPanel()
	    {
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
			JPanel result = new JPanel();
			
			rbSyscodeYes = new JRadioButton("Select a column to specify system code");
			rbSyscodeNo = new JRadioButton("Use the same system code for all rows");
			ButtonGroup radioGroup = new ButtonGroup ();
			radioGroup.add (rbSyscodeYes);
			radioGroup.add (rbSyscodeNo);
			
			cbColId = new JComboBox();
			cbColSyscode = new JComboBox();			

			cbDataSource = new DataSourceCombo();
			cbDataSource.initItems();
			
			JPanel groupPanel = new JPanel();
			groupPanel.setLayout (new BoxLayout (groupPanel, BoxLayout.PAGE_AXIS));
			Border etch = BorderFactory.createEtchedBorder();
			groupPanel.setBorder (etch);
			groupPanel.add (rbSyscodeYes);
			groupPanel.add (cbColSyscode);
			groupPanel.add (rbSyscodeNo);
			groupPanel.add (cbDataSource);
			
			ctm = new ColumnTableModel(importInformation);
			tblColumn = new JTable(ctm);
			tblColumn.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			JScrollPane scrTable = new JScrollPane(tblColumn);
			
			Box topPanel = Box.createVerticalBox();
			
			Box b1 = Box.createHorizontalBox();
			b1.add (new JLabel("Select primary identifier column:"));
			b1.add (cbColId);

			topPanel.add (new JLabel("Column page"));
			topPanel.add (b1);
			topPanel.add (groupPanel);
			
			result.add (topPanel, BorderLayout.NORTH);
			result.add (scrTable, BorderLayout.CENTER);
			
			ActionListener rbAction = new ActionListener() {
				public void actionPerformed (ActionEvent ae)
				{
					boolean result = (ae.getSource() == rbSyscodeYes);
					importInformation.setSyscodeColumn(result);
					refreshSyscodeColumn();
			    	ctm.refresh();
				}
			};
			rbSyscodeNo.addActionListener(rbAction);
			rbSyscodeYes.addActionListener(rbAction);
			
			cbDataSource.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{
					DataSource ds = cbDataSource.getSelectedDataSource();
					importInformation.setDataSource(ds);
			    	ctm.refresh();
				}
			});
			cbColSyscode.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{
					importInformation.setCodeColumn(cbColSyscode.getSelectedIndex());
			    	ctm.refresh();
				}
			});
			cbColId.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{
					importInformation.setIdColumn(cbColId.getSelectedIndex());
			    	ctm.refresh();
				}
			});
			return result;
		}

	    private void refreshSyscodeColumn()
	    {
			if (importInformation.getSyscodeColumn())
			{
				rbSyscodeYes.setSelected (true);
				cbColSyscode.setEnabled (true);
				cbDataSource.setEnabled (false);
			}
			else
			{
				rbSyscodeNo.setSelected (true);
				cbColSyscode.setEnabled (false);
				cbDataSource.setEnabled (true);
			}
	    }
	    
	    private void refreshComboBoxes()
	    {
	    	cbDataSource.setSelectedDataSource (importInformation.getDataSource());
			cbColId.setSelectedIndex(importInformation.getIdColumn());
			cbColSyscode.setSelectedIndex(importInformation.getCodeColumn());
	    }
	    
	    public void aboutToDisplayPanel()
	    {			
	    	cbColId.removeAllItems();
	    	cbColSyscode.removeAllItems();
			for (String s : importInformation.getColNames())
			{
				cbColId.addItem(s);
				cbColSyscode.addItem(s);
			}
			
			refreshSyscodeColumn();
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
	
	private class ImportPage extends WizardPanelDescriptor 
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
	    private JLabel progressText;
	    private SwingProgressKeeper pk;
	    
	    @Override
		protected JPanel createContents()
		{
	    	JPanel result = new JPanel();
			
        	pk = new SwingProgressKeeper((int)1E6);
	    	progressSent = pk.getJProgressBar();
	        result.add (progressSent);
	        
	        progressText = new JLabel();
	        result.add (progressText);
	        
			result.add(new JLabel("Import page"), BorderLayout.CENTER);
			return result;
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
	        setProgressValue(0);
	        setProgressText("Connecting to Server...");

	        getWizard().setNextFinishButtonEnabled(false);
	        getWizard().setBackButtonEnabled(false);
	    }

	    public void displayingPanel() 
	    {
        	
            Thread t = new Thread() 
            {
	            public void run() 
	            {
//	                try 
//	                {
	                	GexTxtImporter.importFromTxt(importInformation, pk);
	                	
//	                    Thread.sleep(2000);
//	                    setProgressValue(25);
//	                    setProgressText("Server Connection Established");
//	                    Thread.sleep(500);
//	                    setProgressValue(50);
//	                    setProgressText("Transmitting Data...");
//	                    Thread.sleep(3000);
//	                    setProgressValue(75);
//	                    setProgressText("Receiving Acknowledgement...");
//	                    Thread.sleep(1000);
//	                    setProgressValue(100);
//	                    setProgressText("Data Successfully Transmitted");
//	
	                    getWizard().setNextFinishButtonEnabled(true);
	                    getWizard().setBackButtonEnabled(true);
//	                } 
//	                catch (InterruptedException e) 
//	                {
//	                    setProgressValue(0);
//	                    setProgressText("An Error Has Occurred");
//	                    
//	                    getWizard().setBackButtonEnabled(true);
//	                }
	            }
	        };
	
	        t.start();
	    }

	}
}
