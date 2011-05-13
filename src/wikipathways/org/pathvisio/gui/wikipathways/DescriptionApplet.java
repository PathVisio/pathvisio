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
package org.pathvisio.gui.wikipathways;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PathwayElementEvent;
import org.pathvisio.core.model.PathwayElementListener;
import org.pathvisio.core.model.PathwayElement.Comment;
import org.pathvisio.wikipathways.WikiPathways;

public class DescriptionApplet extends PathwayPageApplet {
	Comment description;
	boolean noupdate = false;

	protected void createGui() {
		Pathway pathway = wiki.getPathway();
		findDescription(pathway);
		PathwayElement mappInfo = pathway.getMappInfo();

		if(description == null) {
			mappInfo.addComment("", WikiPathways.COMMENT_DESCRIPTION);
			findDescription(pathway);   //A bit silly, we have to search again, because we
										//can't create an add a Comment object directly!
		}

		final JTextArea text = new JTextArea(description.getComment());
		text.setLineWrap(true);
		text.setBorder(BorderFactory.createTitledBorder("Description"));
		text.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				descriptionChanged(text.getText());
			}
			public void insertUpdate(DocumentEvent e) {
				descriptionChanged(text.getText());
			}
			public void removeUpdate(DocumentEvent e) {
				descriptionChanged(text.getText());
			}
		});

		mappInfo.addListener(new PathwayElementListener() {
			public void gmmlObjectModified(PathwayElementEvent e) {
				if(noupdate) {
					noupdate = false;
					return;
				}
				if(!text.getText().equals(description.getComment())) {
					text.setText(description.getComment());
				}
			}
		});

		//Add an undo manager
		final UndoManager undo = new UndoManager();
		Document doc = text.getDocument();

		// Listen for undo and redo events
		doc.addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent evt) {
				undo.addEdit(evt.getEdit());
			}
		});

		// Create an undo action and add it to the text component
		text.getActionMap().put("Undo",
				new AbstractAction("Undo") {
			public void actionPerformed(ActionEvent evt) {
				try {
					if (undo.canUndo()) {
						undo.undo();
					}
				} catch (CannotUndoException e) {
				}
			}
		});

		// Bind the undo action to ctl-Z
		text.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

		// Create a redo action and add it to the text component
		text.getActionMap().put("Redo",
				new AbstractAction("Redo") {
			public void actionPerformed(ActionEvent evt) {
				try {
					if (undo.canRedo()) {
						undo.redo();
					}
				} catch (CannotRedoException e) {
				}
			}
		});

		// Bind the redo action to ctl-Y
		text.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");

		Container content = getContentPane();
		content.add(new JScrollPane(text), BorderLayout.CENTER);
	}

	private void descriptionChanged(String newText) {
		noupdate = true;
		description.setComment(newText);
	}

	private void findDescription(Pathway pathway) {
		for(Comment c : pathway.getMappInfo().getComments()) {
			if(WikiPathways.COMMENT_DESCRIPTION.equals(c.getSource())) {
				description = c;
			}
		}
	}

	protected String getDefaultDescription() {
		return "Modified description";
	}
}
