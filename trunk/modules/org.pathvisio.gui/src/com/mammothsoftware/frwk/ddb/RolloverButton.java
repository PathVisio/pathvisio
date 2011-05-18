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

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 * A button that uses a mouse listener to indicate rollover.
 *
 * @author m. bangham
 * Copyright 2005 Mammoth Software LLC
 */
public class RolloverButton extends JButton {

	public RolloverButton() {
		init();
		initRolloverListener();
	}

	public RolloverButton(Icon icon, int size) {
		super(icon);
		init();
		initRolloverListener();

		setFixedSize(size);
	}

	public RolloverButton(Icon icon, int size, boolean isRollover) {
		super(icon);
		init();
		if (isRollover) initRolloverListener();

		setFixedSize(size);
	}

	public RolloverButton(int size, boolean isRollover) {
		super();
		init();
		if (isRollover) initRolloverListener();

		setFixedSize(size);
	}

	public RolloverButton(Action action, int size) {
		// Note: using setAction(action) causes both icon and text
		// to be displayed on toolbar.
		super((Icon)action.getValue(Action.SMALL_ICON));
		init();
		initRolloverListener();
		addActionListener(action);
		setFixedSize(size);
	}

	private void init() {
		setRequestFocusEnabled(false);
		setRolloverEnabled(true);
	}

	protected void setFixedSize(int size) {
		setPreferredSize(new Dimension(size, size));
		setMaximumSize(new Dimension(size, size));
		setMinimumSize(new Dimension(size, size));
	}

	protected void initRolloverListener() {
		MouseListener l = new MouseAdapter(){
			Border curBorder = null;
			public void mouseEntered(MouseEvent e) {
				curBorder = getBorder();
				/* Borders can have different insets - get the size and force it
				 * so the new rollover border doesn't change the button size. */
				setBorder(new CompoundBorder(getRolloverBorder(), curBorder));
				getModel().setRollover(true);
			}
			public void mouseExited(MouseEvent e) {
				setBorder(curBorder);
				getModel().setRollover(false);
			}
		};
		addMouseListener(l);
	}

	protected Border getRolloverBorder() {
		Border border = BorderFactory.createRaisedBevelBorder();

		return border;
	}
}
