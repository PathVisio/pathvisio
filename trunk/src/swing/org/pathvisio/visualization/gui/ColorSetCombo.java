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
package org.pathvisio.visualization.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.pathvisio.visualization.colorset.ColorSet;
import org.pathvisio.visualization.colorset.ColorSetManager;

public class ColorSetCombo extends JComboBox implements ActionListener {
	public Object NEW = new Object();
	
	ColorSetManager csMgr;
	
	public ColorSetCombo(ColorSetManager csMgr) {
		super();
		this.csMgr = csMgr;
		refresh();
		setRenderer(new ColorSetRenderer());
		addActionListener(this);
	}
	
	public void refresh() {
		ArrayList<Object> csClone = new ArrayList<Object>();
		csClone.addAll(csMgr.getColorSets());
		csClone.add(NEW);
		setModel(new DefaultComboBoxModel(csClone.toArray()));
		if(csMgr.getColorSets().size() == 0) {
			setSelectedItem(null);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if(getSelectedItem() == NEW) {
			ColorSet cs = new ColorSet(csMgr);
			ColorSetDlg dlg = new ColorSetDlg(cs, null, this);
			dlg.setVisible(true);
			csMgr.addColorSet(cs);
			refresh();
			setSelectedItem(cs);
		}
	}
	
	public void setSelectedIndex(int anIndex) {
		if(getItemAt(anIndex) == NEW) {
			anIndex = anIndex - 1;
		}
		super.setSelectedIndex(anIndex);
	}
	
	public ColorSet getSelectedColorSet() {
		Object o = getSelectedItem();
		if(o instanceof ColorSet) {
			return (ColorSet)o;
		} else {
			return null;
		}
	}
	
	class ColorSetRenderer extends JLabel implements ListCellRenderer 
	{
		private static final long serialVersionUID = 1L;

		ColorSet current;
		
		ColorSetRenderer()
		{
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
		}
		
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) 
		{
			if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

			if(value == NEW) {
				setText("New...");
				current = null;
			} else if(value == null) {
				setText("<null>");
				current = null;
			} else {
				current = (ColorSet)value;
				setText(current.getName());
			}

	        Border border = null;
	        if (cellHasFocus) {
	            if (isSelected) {
	                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
	            }
	            if (border == null) {
	                border = UIManager.getBorder("List.focusCellHighlightBorder");
	            }
	        } else {
	            border = UIManager.getBorder("List.noFocusBorder");
	        }
	        setBorder(border);
		
			return this;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if(current != null) {
				Dimension size = getSize();
				if(size.width != 0 && size.height != 0) {
					Rectangle bounds = new Rectangle(0, 0, size.width, size.height);
					current.paintPreview((Graphics2D)g.create(), bounds);
				}
			}
		}
	}
}
