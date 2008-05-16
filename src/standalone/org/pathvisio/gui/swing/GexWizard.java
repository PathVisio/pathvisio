package org.pathvisio.gui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.pathvisio.data.ImportInformation;

import com.nexes.wizard.Wizard;
import com.nexes.wizard.WizardPanelDescriptor;

public class GexWizard extends Wizard 
{
	private ImportInformation importInformation;
	
	GexWizard()
	{
		getDialog().setTitle ("Expression data import wizard");
		
        WizardPanelDescriptor fpd = new FilePageDescriptor();
        this.registerWizardPanel(FilePageDescriptor.IDENTIFIER, fpd);
        WizardPanelDescriptor hpd = new HeaderPageDescriptor();
        this.registerWizardPanel(HeaderPageDescriptor.IDENTIFIER, hpd);
        WizardPanelDescriptor cpd = new ColumnPageDescriptor();
        this.registerWizardPanel(ColumnPageDescriptor.IDENTIFIER, cpd);
        WizardPanelDescriptor ipd = new ImportPageDescriptor();
        this.registerWizardPanel(ImportPageDescriptor.IDENTIFIER, ipd);
        
        setCurrentPanel(FilePageDescriptor.IDENTIFIER);
	}
		
	static private class FilePageDescriptor extends WizardPanelDescriptor 
	{
	    public static final String IDENTIFIER = "FILE_PAGE";
	    
	    public FilePageDescriptor() 
	    {
	        super(IDENTIFIER);
	    }
	    
	    public Object getNextPanelDescriptor() 
	    {
	        return HeaderPageDescriptor.IDENTIFIER;
	    }
	    
	    public Object getBackPanelDescriptor() 
	    {
	        return null;
	    }  

		protected JPanel createContents()
		{
			JPanel result = new JPanel();
			
			result.setLayout (new BorderLayout());
			
			JPanel gridPanel = new JPanel();
			gridPanel.setLayout (new GridLayout (3,3));
			
			gridPanel.add (new JLabel ("Input file"));
			gridPanel.add (new JTextField());
			gridPanel.add (new JButton ("Browse"));
			gridPanel.add (new JLabel ("Output file"));
			gridPanel.add (new JTextField());
			gridPanel.add (new JButton ("Browse"));
			gridPanel.add (new JLabel ("Gene database"));
			gridPanel.add (new JTextField());
			gridPanel.add (new JButton ("Browse"));
	
			result.add (new JLabel("File locations"), BorderLayout.NORTH);
			result.add (gridPanel, BorderLayout.CENTER);
			
			return result;
		}

	}
	
	static private class HeaderPageDescriptor extends WizardPanelDescriptor 
	{
	    public static final String IDENTIFIER = "HEADER_PAGE";
		
	    public HeaderPageDescriptor() 
	    {
	        super(IDENTIFIER);
	    }
	    
	    public Object getNextPanelDescriptor() 
	    {
	        return ColumnPageDescriptor.IDENTIFIER;
	    }
	    
	    public Object getBackPanelDescriptor() 
	    {
	        return FilePageDescriptor.IDENTIFIER;
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

			JPanel previewPanel = new JPanel();
			previewPanel.add (new JTable());
			
			topPanel.add (settingsPanel, BorderLayout.NORTH);
			topPanel.add (previewPanel, BorderLayout.CENTER);
			
			result.add (new JLabel("Header page"), BorderLayout.NORTH);
			result.add (topPanel, BorderLayout.CENTER);
			return result;
		}

	}
	
	static private class ColumnPageDescriptor extends WizardPanelDescriptor 
	{
	    public static final String IDENTIFIER = "COLUMN_PAGE";
		
	    public ColumnPageDescriptor() 
	    {
	        super(IDENTIFIER);
	    }
	    
	    public Object getNextPanelDescriptor() 
	    {
	        return ImportPageDescriptor.IDENTIFIER;
	    }
	    
	    public Object getBackPanelDescriptor() 
	    {
	        return HeaderPageDescriptor.IDENTIFIER;
	    }  

	    @Override
		protected JPanel createContents() 
		{
			JPanel result = new JPanel();
			result.add(new JLabel("Column page"), BorderLayout.CENTER);
			return result;
		}
	}
	
	static private class ImportPageDescriptor extends WizardPanelDescriptor 
	{
	    public static final String IDENTIFIER = "IMPORT_PAGE";
		
	    public ImportPageDescriptor() 
	    {
	        super(IDENTIFIER);
	    }
	    
	    public Object getNextPanelDescriptor() 
	    {
	        return FINISH;
	    }
	    
	    public Object getBackPanelDescriptor() 
	    {
	        return ColumnPageDescriptor.IDENTIFIER;
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
