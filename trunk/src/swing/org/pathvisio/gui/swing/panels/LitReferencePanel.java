package org.pathvisio.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.pathvisio.biopax.BiopaxElementManager;
import org.pathvisio.biopax.reflect.PublicationXRef;
import org.pathvisio.model.PathwayElement;

public class LitReferencePanel extends PathwayElementPanel implements ActionListener {
	static final String ADD = "Add";
	static final String REMOVE = "Remove";
	static final String EDIT = "Edit";
	
	BiopaxElementManager biopax;
	List<PublicationXRef> xrefs;
	
	JList references;
	
	public LitReferencePanel() {
		setLayout(new BorderLayout(5, 5));
		xrefs = new ArrayList<PublicationXRef>();
		
		references = new JList();
		references.setBorder(BorderFactory.createTitledBorder("References"));
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.PAGE_AXIS));
		
		JButton add = new JButton(ADD);
		JButton remove=  new JButton(REMOVE);
		JButton edit = new JButton(EDIT);
		add.addActionListener(this);
		remove.addActionListener(this);
		edit.addActionListener(this);
		buttons.add(add);
		buttons.add(remove);
		buttons.add(edit);
		add(new JScrollPane(references), BorderLayout.CENTER);
		add(buttons, BorderLayout.LINE_END);
	}
	
	public void setInput(PathwayElement e) {
		if(e != getInput()) {
			biopax = new BiopaxElementManager(e);
		}
		super.setInput(e);
	}
	
	public void refresh() {
		xrefs = biopax.getPublicationXRefs();
		references.setListData(xrefs.toArray());
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(ADD)) {
			addPressed();
		} else if(e.getActionCommand().equals(REMOVE)) {
			removePressed();
		} else if(e.getActionCommand().equals(EDIT)) {
			editPressed();
		}
	}
	
	private void editPressed() {
		// TODO Auto-generated method stub
		
	}

	private void removePressed() {
		for(Object o : references.getSelectedValues()) {
			biopax.removeElementReference((PublicationXRef)o);
		}
		
	}

	private void addPressed() {
		PublicationXRef xref = new PublicationXRef(biopax.getUniqueID());
		//TODO: dialog to fill in information
		biopax.addElementReference(xref);
		refresh();
	}
}
