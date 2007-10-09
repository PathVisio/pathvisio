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
package org.pathvisio.gui.wikipathways;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.Engine;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.PathwayListener;
import org.pathvisio.model.PathwayElement.Comment;
import org.pathvisio.wikipathways.WikiPathways;

public class DescriptionApplet extends PathwayPageApplet {
	Comment description;
	boolean noupdate = false;
	
	protected void createGui() {
		Pathway pathway = Engine.getCurrent().getActivePathway();
		findDescription(pathway);
		PathwayElement mappInfo = pathway.getMappInfo();
		
		if(description == null) {
			mappInfo.addComment("", WikiPathways.COMMENT_DESCRIPTION);
			findDescription(pathway);   //A bit silly, we have to search again, because we
										//can't create an add a Comment object directly!
		}
		
		final JTextArea text = new JTextArea(description.getComment());
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
		
		mappInfo.addListener(new PathwayListener() {
			public void gmmlObjectModified(PathwayEvent e) {
				if(noupdate) {
					noupdate = false;
					return;
				}
				if(!text.getText().equals(description.getComment())) {
					text.setText(description.getComment());
				}
			}
		});
		
		Container content = getContentPane();
		content.add(text, BorderLayout.CENTER);
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
}
