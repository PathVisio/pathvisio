package org.pathvisio.visualization.colorset;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

public class ColorGradientCombo  {
	public static JComboBox createGradientCombo() {
		JComboBox combo = new JComboBox();
		combo.setRenderer(new ColorGradientRenderer());
		combo.setEditable(false);
		return combo;
	}
	
	static class ColorGradientRenderer extends JLabel implements ListCellRenderer {
		ColorGradient current;
		Border b_selected = BorderFactory.createLineBorder(Color.BLACK, 3);
		Border b_unselected = BorderFactory.createLineBorder(Color.GRAY, 1);
		
		public ColorGradientRenderer() {
			setPreferredSize(new Dimension(50, 30));
		}
		
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			setBorder(isSelected ? b_selected : b_unselected);
			current = (ColorGradient)value;
			return this;
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			if(current != null) {
				int b = getBorder() == b_selected ? 3 : 1;
				Dimension d = getSize();
				current.paintPreview(
						(Graphics2D)g.create(), new Rectangle(b, b, d.width - b*2, d.height - b*2)
				);
			}
		}
	}
}
