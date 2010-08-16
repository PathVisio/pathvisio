package org.pathvisio.gui.parameter;

import com.jgoodies.forms.builder.DefaultFormBuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.pathvisio.util.swing.SimpleFileFilter;

public class FileEditor implements Editor, DocumentListener, ActionListener
{
	JTextField txtFile;
	final ParameterModel model;
	final int index;
	final ParameterPanel parent;

	public FileEditor(ParameterModel model, int index, ParameterPanel parent, DefaultFormBuilder builder)
	{
		this.index = index;
		this.parent = parent;
		this.model = model;
		
		JButton btnBrowse = new JButton("Browse");
		txtFile = new JTextField();
		btnBrowse.addActionListener(this);
		txtFile.getDocument().addDocumentListener(this);
		txtFile.setToolTipText(model.getHint(index));
        builder.append(model.getLabel(index), txtFile, btnBrowse);
        builder.nextLine();
	}

	@Override
	public Object getValue()
	{
		return new File (txtFile.getText());
	}

	boolean ignoreEvent = false;
	
	@Override
	public void setValue(Object val)
	{
		if (ignoreEvent) return;
		txtFile.setText(((File)val).toString());
	}

	private void handleDocumentEvent(DocumentEvent arg0)
	{
		File file = new File(txtFile.getText());
		
		ignoreEvent = true;
		model.setValue(index, file);
		ignoreEvent = false;
	}

	@Override
	public void changedUpdate(DocumentEvent arg0)
	{
		handleDocumentEvent(arg0);
	}

	@Override
	public void insertUpdate(DocumentEvent arg0)
	{
		handleDocumentEvent(arg0);
	}

	@Override
	public void removeUpdate(DocumentEvent arg0)
	{
		handleDocumentEvent(arg0);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Object metaData = model.getMetaData(index); 
		JFileChooser jfc = new JFileChooser();
		if (metaData instanceof FileParameter)
		{
			FileParameter fileParameter = (FileParameter)metaData;
			jfc.setFileSelectionMode(fileParameter.getFileType());
			if (fileParameter.getFilter() != null &&
				fileParameter.getFileTypeName() != null)
				jfc.setFileFilter(new SimpleFileFilter(fileParameter.getFileTypeName(), fileParameter.getFilter(), true));
		}
		else
		{
			jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}
		jfc.setCurrentDirectory(new File(txtFile.getText()));
		if (jfc.showOpenDialog(parent.getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION)
			txtFile.setText("" + jfc.getSelectedFile());
	}

}
