package org.pathvisio.gui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.pathvisio.data.ImportInformation;

import com.nexes.wizard.Wizard;
import com.nexes.wizard.WizardPanelDescriptor;

public class GexWizard extends Wizard 
{
	private ImportInformation importInformation;
    FilePage fpd = new FilePage();
    HeaderPage hpd = new HeaderPage();
    ColumnPage cpd = new ColumnPage();
    ImportPage ipd = new ImportPage();
    
	GexWizard()
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
					File defaultdir = new File ("/home/martijn/prg/pathvisio-trunk/example-data/");
					JFileChooser jfc = new JFileChooser();
					jfc.setSelectedFile(defaultdir);
					int result = jfc.showDialog(null, "Select input file");
					if (result == JFileChooser.APPROVE_OPTION)
					{
						File f = jfc.getSelectedFile();
						txtInput.setText(f.toString());
				    	hpd.ptm.setTextFile(f);
//				    	//TODO: also set ptm.setTextFile if you don't use browse button.
					}
				}
			});
			
			return result;
		}

	}
	
	private class HeaderPage extends WizardPanelDescriptor 
	{
	    public static final String IDENTIFIER = "HEADER_PAGE";
		PreviewTableModel ptm;
		JTable tblPreview;
		
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
			ButtonGroup g1 = new ButtonGroup();
			Box radioGroup1 = Box.createVerticalBox();
			
			JRadioButton r1 = new JRadioButton ("Header row");
			JRadioButton r2 = new JRadioButton ("No header row");
			JRadioButton r3 = new JRadioButton ();
			g1.add (r1);
			g1.add (r2);
			g1.add (r3);
			radioGroup1.add (r1);
			radioGroup1.add (r2);
			radioGroup1.add (r3);
			radioGroup1.add (new JButton ("Advanced..."));
			Box radioGroup2 = Box.createVerticalBox();
			ButtonGroup g2 = new ButtonGroup();
			JRadioButton r4 = new JRadioButton ("Tab separated values (TSV)");
			JRadioButton r5 = new JRadioButton ("Comma separated values (CSV)");
			JRadioButton r6 = new JRadioButton ();
			radioGroup2.add (r4);
			radioGroup2.add (r5);
			radioGroup2.add (r6);
			radioGroup2.add (new JButton ("Advanced..."));
			g2.add (r4);
			g2.add (r5);
			g2.add (r6);

			settingsPanel.add (radioGroup1);
			settingsPanel.add (radioGroup2);

			topPanel.add (settingsPanel, BorderLayout.NORTH);
			ptm = new PreviewTableModel();
			tblPreview = new JTable(ptm);
			tblPreview.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			JScrollPane scrTable = new JScrollPane(tblPreview);
			topPanel.add (scrTable, BorderLayout.CENTER);
			
			result.add (new JLabel("Header page"), BorderLayout.NORTH);
			result.add (topPanel, BorderLayout.CENTER);
			
			r5.addActionListener(new ActionListener()
			{
				public void actionPerformed (ActionEvent ae)
				{
					ptm.setSeparator(",");
				}
				
			});
			r4.addActionListener(new ActionListener()
			{
				public void actionPerformed (ActionEvent ae)
				{
					ptm.setSeparator("\t");
				}
				
			});
			return result;
		}

	}
	
	private class ColumnPage extends WizardPanelDescriptor 
	{
	    public static final String IDENTIFIER = "COLUMN_PAGE";
		
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
			result.add(new JLabel("Column page"), BorderLayout.CENTER);
			return result;
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
	    
	    @Override
		protected JPanel createContents()
		{
			JPanel result = new JPanel();
			result.add(new JLabel("Import page"), BorderLayout.CENTER);
			return result;
		}
	}
}
