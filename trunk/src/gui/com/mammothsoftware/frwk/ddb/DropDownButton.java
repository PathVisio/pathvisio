// Copyright (C) 2005 Mammoth Software LLC
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//
// Contact the author at: info@mammothsoftware.com
package com.mammothsoftware.frwk.ddb;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 * A Drop Down Button.
 *
 * @author m. bangham
 * Copyright 2005 Mammoth Software LLC
 */
public class DropDownButton extends JButton implements ActionListener {

	private JPopupMenu popup = new JPopupMenu();
	private JToolBar tb = new ToolBar();
	private JButton mainButton;
	private JButton arrowButton;
	private ActionListener mainButtonListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Component component = popup.getComponent(0);
			if (component instanceof JMenuItem) {
				JMenuItem item = (JMenuItem)component;
				item.doClick(0);
			}
		}
	};

	public DropDownButton(Icon icon) {
		this();
		mainButton = new RolloverButton(icon, 25, false);
		arrowButton = new RolloverButton(new DownArrow(), 11, false);
		init();
	}

	public DropDownButton(Icon icon, int size) {
		this();
		mainButton = new RolloverButton(icon, size, false);
		arrowButton = new RolloverButton(new DownArrow(), 11, false);
		init();
	}

	public DropDownButton(RolloverButton mainButton, RolloverButton arrowButton) {
		this();
		this.mainButton = mainButton;
		this.arrowButton = arrowButton;
		init();
	}

	private DropDownButton() {
		super();
		setBorder(null);
	}

	public void setToolTipText(String text) {
		mainButton.setToolTipText(text);
		arrowButton.setToolTipText(text);
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mainButton.setEnabled(enabled);
		arrowButton.setEnabled(enabled);
	}

	public void updateUI() {
		super.updateUI();
		setBorder(null);
	}

	protected Border getRolloverBorder() {
		return BorderFactory.createRaisedBevelBorder();
	}

	private void initRolloverListener() {
		MouseListener l = new MouseAdapter(){
			Border mainBorder = null;
			Border arrowBorder = null;
			public void mouseEntered(MouseEvent e) {
				mainBorder = mainButton.getBorder();
				arrowBorder = mainButton.getBorder();
				mainButton.setBorder(new CompoundBorder(getRolloverBorder(), mainBorder));
				arrowButton.setBorder(new CompoundBorder(getRolloverBorder(), arrowBorder));
				mainButton.getModel().setRollover(true);
				arrowButton.getModel().setRollover(true);
			}
			public void mouseExited(MouseEvent e) {
				mainButton.setBorder(mainBorder);
				arrowButton.setBorder(arrowBorder);
				mainButton.getModel().setRollover(false);
				arrowButton.getModel().setRollover(false);
			}
		};
		mainButton.addMouseListener(l);
		arrowButton.addMouseListener(l);
	}

	private void init() {
		initRolloverListener();

      Icon disDownArrow = new DisabledDownArrow();
      arrowButton.setDisabledIcon(disDownArrow);
      arrowButton.setMaximumSize(new Dimension(11,100));
      mainButton.addActionListener(this);
      arrowButton.addActionListener(this);

      setMargin(new Insets(0, 0, 0, 0));


      // Windows draws border around buttons, but not toolbar buttons
      // Using a toolbar keeps the look consistent.
      tb.setBorder(null);
      tb.setMargin(new Insets(0, 0, 0, 0));
      tb.setFloatable(false);
      tb.add(mainButton);
      tb.add(arrowButton);
      add(tb);

      setFixedSize(mainButton, arrowButton);

	}
	/*
	 * Forces the width of this button to be the sum of the widths of the main
	 * button and the arrow button. The height is the max of the main button or
	 * the arrow button.
	 */
	private void setFixedSize(JButton mainButton, JButton arrowButton) {
      int width = (int)(mainButton.getPreferredSize().getWidth() +
      					arrowButton.getPreferredSize().getWidth());
      int height = (int)Math.max(mainButton.getPreferredSize().getHeight(),
      					arrowButton.getPreferredSize().getHeight());

      setMaximumSize(new Dimension(width, height));
      setMinimumSize(new Dimension(width, height));
      setPreferredSize(new Dimension(width, height));
	}

	/**
	 * Removes a component from the popup
	 * @param component
	 */
	public void removeComponent(Component component) {
		popup.remove(component);
	}

	/**
	 * Adds a component to the popup
	 * @param component
	 * @return
	 */
	public Component addComponent(Component component) {
		return popup.add(component);
	}

	/**
	 * Indicates that the first item in the menu should be executed
	 * when the main button is clicked
	 * @param isRunFirstItem True for on, false for off
	 */
	public void setRunFirstItem(boolean isRunFirstItem) {
		mainButton.removeActionListener(this);
		if (!isRunFirstItem) {
			mainButton.addActionListener(this);
		}
		else
			mainButton.addActionListener(mainButtonListener);
	}

   /*------------------------------[ ActionListener ]---------------------------------------------------*/

   public void actionPerformed(ActionEvent ae){
        JPopupMenu popup = getPopupMenu();
        popup.show(this, 0, this.getHeight());
    }

   protected JPopupMenu getPopupMenu() { return popup; }

   private static class DownArrow implements Icon {

      Color arrowColor = Color.black;

      public void paintIcon(Component c, Graphics g, int x, int y) {
          g.setColor(arrowColor);
          g.drawLine(x, y, x+4, y);
          g.drawLine(x+1, y+1, x+3, y+1);
          g.drawLine(x+2, y+2, x+2, y+2);
      }

      public int getIconWidth() {
          return 6;
      }

      public int getIconHeight() {
          return 4;
      }

  }

   private static class DisabledDownArrow extends DownArrow {

      public DisabledDownArrow() {
          arrowColor = new Color(140, 140, 140);
      }

      public void paintIcon(Component c, Graphics g, int x, int y) {
          super.paintIcon(c, g, x, y);
          g.setColor(Color.white);
          g.drawLine(x+3, y+2, x+4, y+1);
          g.drawLine(x+3, y+3, x+5, y+1);
      }
  }

   private static class ToolBar extends JToolBar
   {

	   public void updateUI()
	   {
		   super.updateUI();
		   setBorder(null);
	   }
   }

}
