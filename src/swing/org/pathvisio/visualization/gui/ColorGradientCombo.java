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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.pathvisio.visualization.colorset.ColorGradient;

public class ColorGradientCombo  
{
	
	public static JComboBox createGradientCombo() 
	{
		JComboBox combo = new JComboBox();
		combo.setRenderer(new ColorGradientRenderer());
		combo.setEditable(false);
		combo.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e) 
			{
				new Throwable().printStackTrace();
				System.out.println ("!!!");
				
			}
		});
		return combo;
	}
	
	static class ColorGradientRenderer extends JLabel implements ListCellRenderer 
	{
		private static final long serialVersionUID = 1L;
		ColorGradient current;
		Border b_selected = BorderFactory.createLineBorder(Color.BLACK, 3);
		Border b_unselected = BorderFactory.createLineBorder(Color.GRAY, 1);
		
		public ColorGradientRenderer() 
		{
			super();
			setOpaque(true);
			setBorder (UIManager.getBorder("List.noFocusBorder"));
			setPreferredSize(new Dimension(50, 30));
		}
		
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) 
		{
			setBorder(isSelected ? b_selected : b_unselected);
			current = (ColorGradient)value;
			return this;
		}
		
		public void paint(Graphics g) 
		{
			super.paint(g);
			if(current != null) 
			{
				int b = getBorder() == b_selected ? 3 : 1;
				Dimension d = getSize();
				current.paintPreview(
						(Graphics2D)g.create(), new Rectangle(b, b, d.width - b*2, d.height - b*2)
				);
			}
		}
	}
}
