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
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PathwayElement.Comment;
import org.pathvisio.wikipathways.Parameter;
import org.pathvisio.wikipathways.WikiPathways;

public class CategoryApplet extends PathwayPageApplet
{
	Map<String, Category> categories;
	PathwayElement mappInfo;

	protected void createGui() {
		Pathway pathway = wiki.getPathway();
		mappInfo = pathway.getMappInfo();

		findCategories();

		final List<Category> catList = new ArrayList<Category>(categories.values());
		Collections.sort(catList);

		final JList categoryList = new JList(catList.toArray());
		categoryList.setBorder(BorderFactory.createTitledBorder("Categories"));
		final CheckListManager checkListManager = new CheckListManager(categoryList);

		int i = 0;
		for(Category cat : catList) {
			if(cat.isChecked()) {
				checkListManager.getSelectionModel().addSelectionInterval(i, i);
			}
			i++;
		}

		checkListManager.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				for(int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
					Category cat = catList.get(i);
					boolean checked = checkListManager.getSelectionModel().isSelectedIndex(i);
					cat.setChecked(checked);
				}
			}
		});


		Container content = getContentPane();
		content.add(categoryList, BorderLayout.CENTER);
	}

	void findCategories() {
		categories = new HashMap<String, Category>();

		String[] cats = wiki.getParameters().getValue(
				Parameter.CATEGORIES).split(",");
		for(String cname : cats) {
			cname = cname.trim();
			Category c = new Category(cname, mappInfo);
			categories.put(cname, c);
		}
		for(Comment c : mappInfo.getComments()) {
			if(WikiPathways.COMMENT_CATEGORY.equals(c.getSource())) {
				if(c.getComment() != null && !"".equals(c.getComment())) {
					Category cat = categories.get(c);
					if(cat == null) {
						cat = new Category(c.getComment(), mappInfo);
						categories.put(c.getComment(), cat);
					}
					cat.setComment(c);
				}
			}
		}
	}

	class Category implements Comparable<Category> {
		Comment comment;
		PathwayElement elm;
		String name;

		public Category(String name, PathwayElement mappInfo) {
			this.name = name.replaceAll("_", " ");
			elm = mappInfo;
		}

		public void setComment(Comment comment) {
			this.comment = comment;
		}

		public void setChecked(boolean checked) {
			if(checked) check();
			else uncheck();
		}
		public void check() {
			if(comment == null) {
				comment = elm.new Comment(name, WikiPathways.COMMENT_CATEGORY);
				elm.addComment(comment);
			}
		}

		public void uncheck() {
			if(comment != null) {
				elm.removeComment(comment);
				comment = null;
			}
		}

		public boolean isChecked() {
			return comment != null;
		}

		public String toString() {
			return name;
		}

		public int compareTo(Category o) {
			return name.compareTo(o.name);
		}
	}
	// @author Santhosh Kumar T - santhosh@in.fiorano.com
	class CheckListManager extends MouseAdapter implements ListSelectionListener, ActionListener{
	    private ListSelectionModel selectionModel = new DefaultListSelectionModel();
	    private JList list = new JList();
	    int hotspot = new JCheckBox().getPreferredSize().width;

	    public CheckListManager(JList list){
	        this.list = list;
	        list.setCellRenderer(new CheckListCellRenderer(list.getCellRenderer(), selectionModel));
	        list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);
	        list.addMouseListener(this);
	        selectionModel.addListSelectionListener(this);
	    }

	    public ListSelectionModel getSelectionModel(){
	        return selectionModel;
	    }

	    private void toggleSelection(int index){
	        if(index<0)
	            return;

	        if(selectionModel.isSelectedIndex(index))
	            selectionModel.removeSelectionInterval(index, index);
	        else
	            selectionModel.addSelectionInterval(index, index);
	    }

	    public void mouseClicked(MouseEvent me){
	        int index = list.locationToIndex(me.getPoint());
	        if(index<0)
	            return;
	        if(me.getX()>list.getCellBounds(index, index).x+hotspot)
	            return;
	        toggleSelection(index);
	    }

	    public void valueChanged(ListSelectionEvent e){
	        list.repaint(list.getCellBounds(e.getFirstIndex(), e.getLastIndex()));
	    }

	    public void actionPerformed(ActionEvent e){
	        toggleSelection(list.getSelectedIndex());
	    }
	}

	// @author Santhosh Kumar T - santhosh@in.fiorano.com
	public class CheckListCellRenderer extends JPanel implements ListCellRenderer{
		private ListCellRenderer delegate;
	    private ListSelectionModel selectionModel;
	    private JCheckBox checkBox = new JCheckBox();

	    public CheckListCellRenderer(ListCellRenderer renderer, ListSelectionModel selectionModel){
	        this.delegate = renderer;
	        this.selectionModel = selectionModel;
	        setLayout(new BorderLayout());
	        setOpaque(false);
	        checkBox.setOpaque(false);
	    }

	    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
	        Component renderer = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	        checkBox.setSelected(selectionModel.isSelectedIndex(index));
	        removeAll();
	        add(checkBox, BorderLayout.WEST);
	        add(renderer, BorderLayout.CENTER);
	        return this;
	    }
	}

	protected String getDefaultDescription() {
		return "Modified categories";
	}
}
