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
package org.pathvisio.gui.swing.panels;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.model.PathwayElement.Comment;
import org.pathvisio.util.Resources;

public class CommentPanel extends PathwayElementPanel implements ActionListener {

	protected static final String ADD = "Add comment";
	protected static final String REMOVE = "Remove comment";
	private static final URL IMG_REMOVE = Resources.getResourceURL("cancel.gif");

	JPanel buttonPanel;
	JScrollPane cmtPanel;

	public CommentPanel() {
		setLayout(new BorderLayout(5, 5));

		buttonPanel = new JPanel();
		JButton add = new JButton(ADD);
		add.setActionCommand(ADD);
		add.addActionListener(this);

		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(add);

		add(buttonPanel, BorderLayout.PAGE_END);
	}

	public void setReadOnly(boolean readonly) {
		super.setReadOnly(readonly);
		setChildrenEnabled(buttonPanel, !readonly);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(ADD)) {
			getInput().addComment("Type your comment here", "");
		}
		refresh();
	}

	public void refresh() {
		if(cmtPanel != null) remove(cmtPanel);

		DefaultFormBuilder b = new DefaultFormBuilder(
				new FormLayout("fill:pref:grow")
		);
		CommentEditor firstEditor = null;
		for(Comment c : getInput().getComments()) {
			CommentEditor ce = new CommentEditor(c);
			if(firstEditor == null) firstEditor = ce;
			b.append(ce);
			b.nextLine();
		}
		if(getInput().getComments().size() == 0) {
			CommentEditor ce = new CommentEditor(null);
			firstEditor = ce;
			b.append(ce);
			b.nextLine();
		}
		JPanel p = b.getPanel();
		cmtPanel = new JScrollPane(p,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		);
		add(cmtPanel, BorderLayout.CENTER);
		validate();
	}

	private class CommentEditor extends JPanel implements ActionListener {
		Comment comment;
		JPanel btnPanel;
		JTextPane txt;

		public CommentEditor(Comment c) {
			comment = c;
			setBackground(Color.WHITE);
			setLayout(new FormLayout(
					"2dlu, fill:[100dlu,min]:grow, 1dlu, pref, 2dlu", "2dlu, pref, 2dlu"
			));
			txt = new JTextPane();
			txt.setText(comment == null ? "Type your comment here" : comment.getComment());
			txt.setBorder(BorderFactory.createEtchedBorder());
			txt.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					update();
				}
				public void insertUpdate(DocumentEvent e) {
					update();
				}
				public void removeUpdate(DocumentEvent e) {
					update();
				}
				void update() {
					if(comment == null) {
						comment = getInput().new Comment(txt.getText(), "");
						getInput().addComment(comment);
					} else {
						comment.setComment(txt.getText());
					}
				}
			});
			txt.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(comment == null) {
						txt.selectAll();
					}
				}
			});
			CellConstraints cc = new CellConstraints();
			add(txt, cc.xy(2, 2));

			btnPanel = new JPanel(new FormLayout("pref", "pref"));
			JButton btnRemove = new JButton();
			btnRemove.setActionCommand(REMOVE);
			btnRemove.addActionListener(this);
			btnRemove.setIcon(new ImageIcon(IMG_REMOVE));
			btnRemove.setBackground(Color.WHITE);
			btnRemove.setBorder(null);
			btnRemove.setToolTipText("Remove comment");

			MouseAdapter maHighlight = new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					e.getComponent().setBackground(new Color(200, 200, 255));
				}
				public void mouseExited(MouseEvent e) {
					e.getComponent().setBackground(Color.WHITE);
				}
			};
			btnRemove.addMouseListener(maHighlight);

			btnPanel.add(btnRemove, cc.xy(1, 1));

			add(btnPanel, cc.xy(4, 2));
			btnPanel.setVisible(false);

			MouseAdapter maHide = new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					if(!readonly) btnPanel.setVisible(true);
				}
				public void mouseExited(MouseEvent e) {
					if(!contains(e.getPoint())) {
						btnPanel.setVisible(false);
					}
				}
			};
			addMouseListener(maHide);
			txt.addMouseListener(maHide);
		}

		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			if(REMOVE.equals(action)) {
				getInput().removeComment(comment);
				refresh();
			}
		}
	}
}