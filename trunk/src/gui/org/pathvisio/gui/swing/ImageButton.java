package org.pathvisio.gui.swing;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.JButton;


public class ImageButton extends JButton{
	
	public ImageButton(Action a){
		super();
		this.setRolloverEnabled(true);
		initRolloverListener();
		Dimension dim = new Dimension(25,25);
		this.setAction(a);
		this.setSize(dim);
		this.setPreferredSize(dim);
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		this.setText(null);
		this.setMargin(new Insets(0,0,0,0));
		this.setContentAreaFilled(false);
	}

	
	protected void initRolloverListener() {
		MouseListener l = new MouseAdapter(){
			public void mouseEntered(MouseEvent e) {
				setContentAreaFilled(true);
				getModel().setRollover(true);
			}
			public void mouseExited(MouseEvent e) {
				setContentAreaFilled(false);
			}
		};
		addMouseListener(l);
	}
	
}
