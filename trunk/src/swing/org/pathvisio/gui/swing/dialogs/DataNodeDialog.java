package org.pathvisio.gui.swing.dialogs;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.data.DataSources;
import org.pathvisio.model.PathwayElement;

public class DataNodeDialog extends PathwayElementDialog {
	public DataNodeDialog(PathwayElement e, Frame frame, Component locationComp) {
		super(e, frame, "DataNode properties", locationComp);
	}

	JTextField symText;
	JTextField idText;
	JComboBox dbCombo;
			
	public void refresh() {
		symText.setText(getInput().getTextLabel());
		idText.setText(getInput().getGeneID());
		dbCombo.setSelectedItem(getInput().getDataSource());
		pack();
	}
	
	protected void createDialogContents(Container parent) {
		parent.setLayout(new GridLayout(3, 2));
		JLabel symLabel = new JLabel("Symbol");
		JLabel idLabel = new JLabel("Identifier");
		JLabel dbLabel = new JLabel("Database");
		symText = new JTextField();
		idText = new JTextField();
		dbCombo = new JComboBox(DataSources.dataSources);
		
		parent.add(symLabel);	parent.add(symText);
		parent.add(idLabel);	parent.add(idText);
		parent.add(dbLabel);	parent.add(dbCombo);
		
		symText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				System.out.println("Text changed");
				getInput().setTextLabel(symText.getText());
			}
			public void insertUpdate(DocumentEvent e) {	}
			public void removeUpdate(DocumentEvent e) {	}
		});
	}
}
