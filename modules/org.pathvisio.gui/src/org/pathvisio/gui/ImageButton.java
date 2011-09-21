// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.JButton;

/**
 * this is the buttons on the drop-down panel
 *
 * @author bing
 */
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
