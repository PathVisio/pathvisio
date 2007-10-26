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
package org.pathvisio.view.swt;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.eclipse.swt.widgets.Display;
import org.pathvisio.view.swing.VPathwaySwing;

public class VPathwaySwtAwt extends VPathwaySwing {
	private static final long serialVersionUID = 1L;

	Display display;
	public VPathwaySwtAwt(JScrollPane parent, Display display) {
		super(parent);
		this.display = display;
	}

	public void mouseClicked(final MouseEvent e) {
		if(isDisposed()) return;
		
		display.syncExec(new Runnable() {
			public void run() {
				VPathwaySwtAwt.super.mouseClicked(e);
			}
		});
	}

	public void mouseEntered(final MouseEvent e) {
		if(isDisposed()) return;
		
		requestFocus();
		display.syncExec(new Runnable() {
			public void run() {
				VPathwaySwtAwt.super.mouseEntered(e);
			}
		});
	}

	public void mouseExited(final MouseEvent e) {
		if(isDisposed()) return;
		
		display.syncExec(new Runnable() {
			public void run() {
				VPathwaySwtAwt.super.mouseExited(e);
			}
		});
	}

	public void mousePressed(final MouseEvent e) {
		if(isDisposed()) return;
		
		display.syncExec(new Runnable() {
			public void run() {
				VPathwaySwtAwt.super.mousePressed(e);
			}
		});
	}

	public void mouseReleased(final MouseEvent e) {
		if(isDisposed()) return;
		
		display.syncExec(new Runnable() {
			public void run() {
				VPathwaySwtAwt.super.mouseReleased(e);
			}
		});
	}

	public void keyPressed(final KeyEvent e) {
		if(isDisposed()) return;
		
		display.syncExec(new Runnable() {
			public void run() {
				VPathwaySwtAwt.super.keyPressed(e);
			}
		});
	}

	public void keyReleased(final KeyEvent e) {
		if(isDisposed()) return;
		
		display.syncExec(new Runnable() {
			public void run() {
				VPathwaySwtAwt.super.keyReleased(e);
			}
		});
	}

	public void keyTyped(final KeyEvent e) {
		if(isDisposed()) return;
		
		display.syncExec(new Runnable() {
			public void run() {
				VPathwaySwtAwt.super.keyTyped(e);
			}
		});
	}

	public void mouseDragged(final MouseEvent e) {
		if(isDisposed()) return;
		
		display.syncExec(new Runnable() {
			public void run() {
				VPathwaySwtAwt.super.mouseDragged(e);
			}
		});
	}

	public void mouseMoved(final MouseEvent e) {
		if(isDisposed()) return;
		
		display.syncExec(new Runnable() {
			public void run() {
				VPathwaySwtAwt.super.mouseMoved(e);
			}
		});
	}
	
	public boolean isDisposed() {
		return display == null || display.isDisposed();
	}
	
	public void registerKeyboardAction(KeyStroke k, final Action a) {
		super.registerKeyboardAction(k, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent e) {
				display.syncExec(new Runnable() {
					public void run() {
						a.actionPerformed(e);
					}
				});
			}
		});
	}
}