package org.pathvisio.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.pathvisio.biopax.BiopaxElementManager;
import org.pathvisio.biopax.reflect.PublicationXRef;
import org.pathvisio.gui.swing.dialogs.PublicationXRefDialog;
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
		references.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					editPressed();
				}
			}
		});
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
		PublicationXRef xref = (PublicationXRef)references.getSelectedValue();
		if(xref != null) {
			PublicationXRefDialog d = new PublicationXRefDialog(xref, null, this, false);
			d.setVisible(true);
		}
		refresh();
	}

	private void removePressed() {
		for(Object o : references.getSelectedValues()) {
			biopax.removeElementReference((PublicationXRef)o);
		}
		refresh();
	}

	private void addPressed() {
		PublicationXRef xref = new PublicationXRef(biopax.getUniqueID());
		
		PublicationXRefDialog d = new PublicationXRefDialog(xref, null, this);
		d.setVisible(true);
		if(d.getExitCode().equals(PublicationXRefDialog.OK)) {
			biopax.addElementReference(xref);
			refresh();			
		}
	}
}
