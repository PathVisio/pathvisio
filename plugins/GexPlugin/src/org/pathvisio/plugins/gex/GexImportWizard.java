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
package org.pathvisio.plugins.gex;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.nexes.wizard.Wizard;
import com.nexes.wizard.WizardPanelDescriptor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.bridgedb.rdb.DBConnector;
import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.data.DBConnectorSwing;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.DataSourceModel;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.FileUtils;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.ProgressKeeper.ProgressEvent;
import org.pathvisio.util.ProgressKeeper.ProgressListener;
import org.pathvisio.util.rowheader.RowNumberHeader;
import org.pathvisio.util.swing.PermissiveComboBox;
import org.pathvisio.util.swing.SimpleFileFilter;

/**
 * Wizard to guide the user through importing a large dataset from a tab delimited text file 
 * in PathVisio. For example gene expression data.
 */
public class GexImportWizard extends Wizard 
{
	private ImportInformation importInformation = new ImportInformation();
	
    FilePage fpd = new FilePage();
    HeaderPage hpd = new HeaderPage();
    ColumnPage cpd = new ColumnPage();
    ImportPage ipd = new ImportPage();
    
    private final PvDesktop standaloneEngine;
    
	public GexImportWizard (PvDesktop standaloneEngine)
	{
		this.standaloneEngine = standaloneEngine;
		
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
			else if (!file.canRead()) 
			{
				setErrorMessage("Can't access specified file containing expression data");
				txtFileComplete = false;
			}
			else try
			{
				importInformation.setTxtFile(file);
			}
			catch (IOException e)
			{
				setErrorMessage("Exception while reading file: " + e.getMessage());
				txtFileComplete = false;
			}

		    getWizard().setNextFinishButtonEnabled(txtFileComplete);

		    // add .pgex.
			String outFile = FileUtils.removeExtension(fileName) + ".pgex";

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
					PreferenceManager.getCurrent().get(GlobalPreference.DB_CONNECTSTRING_GDB)
			);
			return builder.getPanel();
		}

		public void aboutToHidePanel() 
		{
			importInformation.guessSettings();
			importInformation.setGexName (txtOutput.getText());
	    }

		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			
			if(ACTION_GDB.equals(action)) {
				standaloneEngine.getSwingEngine().selectGdb("Gene");
				txtGdb.setText(
						PreferenceManager.getCurrent().get(GlobalPreference.DB_CONNECTSTRING_GDB)
				);
			} else if(ACTION_INPUT.equals(action)) {
				
				File defaultdir = PreferenceManager.getCurrent().getFile(GlobalPreference.DIR_LAST_USED_EXPRESSION_IMPORT);
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(defaultdir);
				jfc.addChoosableFileFilter(new SimpleFileFilter("Data files", "*.txt|*.csv|*.tab", true));
				int result = jfc.showDialog(null, "Select data file");
				if (result == JFileChooser.APPROVE_OPTION)
				{
					File f = jfc.getSelectedFile();
					defaultdir = jfc.getCurrentDirectory();
					PreferenceManager.getCurrent().setFile(GlobalPreference.DIR_LAST_USED_EXPRESSION_IMPORT, defaultdir);
					txtInput.setText("" + f);
					updateTxtFile ();
				}
			} else if(ACTION_OUTPUT.equals(action)) {
				try {
					DBConnector dbConn = standaloneEngine.getGexManager().getDBConnector();
						String output = ((DBConnectorSwing)dbConn).openNewDbDialog(
								getPanelComponent(), importInformation.getGexName()	
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
		//private JButton btnAdvanced;
		
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

			//btnAdvanced = new JButton("More Options");
			//builder.add (btnAdvanced, cc.xy (5, 5));

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
			
			/*
			btnAdvanced.addActionListener(new ActionListener()
			{
				public void createAndShowDlg()
				{
					final JDialog dlg = new JDialog (getWizard().getDialog(), "More options", true);
					dlg.setLayout(new FlowLayout());

					final JRadioButton rbDecimalDot;
					final JRadioButton rbDecimalComma;
					
					ButtonGroup bgDecimal = new ButtonGroup();
					rbDecimalDot = new JRadioButton ("Use dot as decimal separator");
					rbDecimalComma = new JRadioButton ("Use comma as decimal separator");

					bgDecimal.add(rbDecimalComma);
					bgDecimal.add(rbDecimalDot);

					dlg.add(rbDecimalComma);
					dlg.add(rbDecimalDot);
					
					rbDecimalDot.setSelected(importInformation.digitIsDot());
					rbDecimalComma.setSelected(!importInformation.digitIsDot());
					
					JButton btnOk = new JButton ("OK");
					
					dlg.add (btnOk);
					btnOk.addActionListener(new ActionListener()
					{

						public void actionPerformed(ActionEvent ae) 
						{
							importInformation.setDigitIsDot (rbDecimalDot.isSelected());
							dlg.dispose();
						}
					});
					dlg.setLocationRelativeTo(getWizard().getDialog());
					dlg.pack();
					dlg.setVisible(true);
				}
				
				public void actionPerformed(ActionEvent e) 
				{
					javax.swing.SwingUtilities.invokeLater(new Runnable() 
					{
						
						public void run() {
							createAndShowDlg();
						}
					});
				}
			});
			*/
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
	    private JRadioButton rbFixedNo;
	    private JRadioButton rbFixedYes;
	    private JComboBox cbDataSource;
	    private DataSourceModel mDataSource;
	    
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
			
			rbFixedNo = new JRadioButton("Select a column to specify system code");
			rbFixedYes = new JRadioButton("Use the same system code for all rows");
			ButtonGroup bgSyscodeCol = new ButtonGroup ();
			bgSyscodeCol.add (rbFixedNo);
			bgSyscodeCol.add (rbFixedYes);
			
			cbColId = new JComboBox();
			cbColSyscode = new JComboBox();			

			mDataSource = new DataSourceModel();
			cbDataSource = new PermissiveComboBox(mDataSource);

			ctm = new ColumnTableModel(importInformation);
			tblColumn = new JTable(ctm);
			tblColumn.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			tblColumn.setDefaultRenderer(Object.class, ctm.getTableCellRenderer());
			tblColumn.setCellSelectionEnabled(false);

			tblColumn.getTableHeader().addMouseListener(new ColumnPopupListener());
			JTable rowHeader = new RowNumberHeader(tblColumn);
			rowHeader.addMouseListener(new RowPopupListener());
			JScrollPane scrTable = new JScrollPane(tblColumn);
		    
			JViewport jv = new JViewport();
		    jv.setView(rowHeader);
		    jv.setPreferredSize(rowHeader.getPreferredSize());
		    scrTable.setRowHeader(jv);
//		    scrTable.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, rowHeader
//		            .getTableHeader());
		    
			builder.addLabel ("Select primary identifier column:", cc.xy(1,1));
			builder.add (cbColId, cc.xy(3,1));

			builder.add (rbFixedNo, cc.xyw(1,3,3));
			builder.add (cbColSyscode, cc.xy(3,5));
			builder.add (rbFixedYes, cc.xyw (1,7,3));
			builder.add (cbDataSource, cc.xy (3,9));
			
			builder.add (scrTable, cc.xyw(1,11,3));
			
			ActionListener rbAction = new ActionListener() {
				public void actionPerformed (ActionEvent ae)
				{
					boolean result = (ae.getSource() == rbFixedYes);
					importInformation.setSyscodeFixed(result);
			    	columnPageRefresh();
				}
			};
			rbFixedYes.addActionListener(rbAction);
			rbFixedNo.addActionListener(rbAction);
			
			mDataSource.addListDataListener(new ListDataListener()
			{
				public void contentsChanged(ListDataEvent arg0) 
				{
					importInformation.setDataSource(mDataSource.getSelectedDataSource());
				}

				public void intervalAdded(ListDataEvent arg0) {}

				public void intervalRemoved(ListDataEvent arg0) {}
			});
			
			cbColSyscode.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{
					importInformation.setSysodeColumn(cbColSyscode.getSelectedIndex());
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
	    
	    private class ColumnPopupListener extends MouseAdapter
	    {
	    	@Override public void mousePressed (MouseEvent e) 
			{
				showPopup(e);
			}
		
			@Override public void mouseReleased (MouseEvent e)
			{
				showPopup(e);
			}

			int clickedCol;
			
			private void showPopup(MouseEvent e) 
			{
				if (e.isPopupTrigger())
				{
					JPopupMenu popup;
					popup = new JPopupMenu();
					clickedCol = tblColumn.columnAtPoint(e.getPoint()); 
					if (clickedCol != importInformation.getSyscodeColumn())
						popup.add(new SyscodeColAction());
					if (clickedCol != importInformation.getIdColumn())
						popup.add(new IdColAction());
					popup.show(e.getComponent(),
							e.getX(), e.getY());
				}
			}

			private class SyscodeColAction extends AbstractAction
			{
				public SyscodeColAction() 
				{
					putValue(Action.NAME, "SystemCode column");
				}
				
				public void actionPerformed(ActionEvent arg0) 
				{
					// if id and code column are about to be the same, swap them
					if (clickedCol == importInformation.getIdColumn())
						importInformation.setIdColumn(importInformation.getSyscodeColumn());
					importInformation.setSysodeColumn(clickedCol);
					columnPageRefresh();
				}	
			}

			private class IdColAction extends AbstractAction
			{
				public IdColAction() 
				{
					putValue(Action.NAME, "Identifier column");
				}
				
				public void actionPerformed(ActionEvent arg0) 
				{
					// if id and code column are about to be the same, swap them
					if (clickedCol == importInformation.getSyscodeColumn())
						importInformation.setSysodeColumn(importInformation.getIdColumn());
					importInformation.setIdColumn(clickedCol); 
					columnPageRefresh();
				}	
			}
	    }
		
	    private class RowPopupListener extends MouseAdapter
	    {
	    	@Override public void mousePressed (MouseEvent e) 
			{
				showPopup(e);
			}
		
			@Override public void mouseReleased (MouseEvent e)
			{
				showPopup(e);
			}

			int clickedRow;
			
			private void showPopup(MouseEvent e) 
			{
				if (e.isPopupTrigger())
				{
					JPopupMenu popup;
					popup = new JPopupMenu();
					clickedRow = tblColumn.rowAtPoint(e.getPoint()); 
					popup.add(new DataStartAction());
					popup.add(new HeaderStartAction());
					popup.show(e.getComponent(),
							e.getX(), e.getY());
				}
			}

			private class DataStartAction extends AbstractAction
			{
				public DataStartAction() 
				{
					putValue(Action.NAME, "First data row");
				}
				
				public void actionPerformed(ActionEvent arg0) 
				{
					importInformation.setFirstDataRow(clickedRow);
					columnPageRefresh();
				}	
			}

			private class HeaderStartAction extends AbstractAction
			{
				public HeaderStartAction() 
				{
					putValue(Action.NAME, "First header row");
				}
				
				public void actionPerformed(ActionEvent arg0) 
				{
					importInformation.setFirstHeaderRow(clickedRow);
					columnPageRefresh();
				}
			}
			
	    }

	    private void columnPageRefresh()
	    {
	    	String error = null;
			if (importInformation.isSyscodeFixed())
			{
				rbFixedYes.setSelected (true);
				cbColSyscode.setEnabled (false);
				cbDataSource.setEnabled (true);
			}
			else
			{
				rbFixedNo.setSelected (true);
				cbColSyscode.setEnabled (true);
				cbDataSource.setEnabled (false);

				if (importInformation.getIdColumn() == importInformation.getSyscodeColumn())
	    		{
	    			error = "System code column and Id column can't be the same";
	    		}
			}
		    getWizard().setNextFinishButtonEnabled(error == null);
		    getWizard().setErrorMessage(error == null ? "" : error);
			getWizard().setPageTitle ("Choose column types");
			
	    	ctm.refresh();
	    }
	    
	    private void refreshComboBoxes()
	    {
	    	mDataSource.setSelectedItem(importInformation.getDataSource());
			cbColId.setSelectedIndex(importInformation.getIdColumn());
			cbColSyscode.setSelectedIndex(importInformation.getSyscodeColumn());
	    }
	    
	    /** 
	     * A simple cell Renderer for combo boxes that use the
	     * column index integer as value,
	     * but will display the column name String
	     */
	    private class ColumnNameRenderer extends JLabel implements ListCellRenderer 
	    {
			public ColumnNameRenderer() 
			{
				setOpaque(true);
				setHorizontalAlignment(CENTER);
				setVerticalAlignment(CENTER);
			}
		
			/*
			* This method finds the image and text corresponding
			* to the selected value and returns the label, set up
			* to display the text and image.
			*/
			public Component getListCellRendererComponent(
			                        JList list,
			                        Object value,
			                        int index,
			                        boolean isSelected,
			                        boolean cellHasFocus) 
			{
				//Get the selected index. (The index param isn't
				//always valid, so just use the value.)
				int selectedIndex = ((Integer)value).intValue();
				
				if (isSelected) 
				{
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
				} else {
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}
				
				String[] cn = importInformation.getColNames();
				String column = cn[selectedIndex];
				setText(column);
				setFont(list.getFont());
				
				return this;
			}
		}

	    public void aboutToDisplayPanel()
	    {			
	    	// create an array of size getSampleMaxNumCols()
	    	Integer[] cn;
	    	int max = importInformation.getSampleMaxNumCols();
    		cn = new Integer[max];
    		for (int i = 0; i < max; ++i) cn[i] = i;
    		
	    	cbColId.setRenderer(new ColumnNameRenderer());
	    	cbColSyscode.setRenderer(new ColumnNameRenderer());
	    	cbColId.setModel(new DefaultComboBoxModel(cn));
	    	cbColSyscode.setModel(new DefaultComboBoxModel(cn));
			
			columnPageRefresh();
			refreshComboBoxes();
			
	    	ctm.refresh();
	    }
	    
	    @Override
	    public void aboutToHidePanel()
	    {
	    	importInformation.setSyscodeFixed(rbFixedYes.isSelected());
	    	if (rbFixedYes.isSelected())
	    	{
		    	importInformation.setDataSource(mDataSource.getSelectedDataSource());
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
	    private ProgressKeeper pk;
	    private JLabel lblTask;
	    
	    @Override
	    public void aboutToCancel()
	    {
	    	// let the progress keeper know that the user pressed cancel.
	    	pk.cancel();
	    }

		protected JPanel createContents()
		{
	    	FormLayout layout = new FormLayout(
	    			"fill:[100dlu,min]:grow",
	    			"pref, pref, fill:pref:grow"
	    	);
	    	
	    	DefaultFormBuilder builder = new DefaultFormBuilder(layout);
	    	builder.setDefaultDialogBorder();
	    	
        	pk = new ProgressKeeper((int)1E6);
        	pk.addListener(this);
			progressSent = new JProgressBar(0, pk.getTotalWork());
	        builder.append(progressSent);
	        builder.nextLine();
	        lblTask = new JLabel();
	        builder.append(lblTask);
	        
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
			getWizard().setPageTitle ("Perform import");
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
								standaloneEngine.getSwingEngine().getGdbManager().getCurrentGdb(),
								standaloneEngine.getGexManager()
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

		public void progressEvent(ProgressEvent e) 
		{
			switch(e.getType()) 
			{
				case ProgressEvent.FINISHED:
					progressSent.setValue(pk.getTotalWork());
				case ProgressEvent.TASK_NAME_CHANGED:
					lblTask.setText(pk.getTaskName());
					break;
				case ProgressEvent.REPORT:
					progressText.append(e.getProgressKeeper().getReport() + "\n");
					break;
				case ProgressEvent.PROGRESS_CHANGED:
					progressSent.setValue(pk.getProgress());
					break;
			}
		}

	}
	
	
}
