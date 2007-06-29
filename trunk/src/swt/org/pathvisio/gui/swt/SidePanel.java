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
package org.pathvisio.gui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Sash;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.swt.SwtPreferences.SwtPreference;

/**
 * This class can be extended to create a sidepanel with minimize button
 * for use as component of a {@link SashForm}
 */
public class SidePanel extends Composite {
	private SashForm parentSash;
	private Composite contentComposite;
	
	/**
	 * Constructor for this class
	 * @param parent	The parent composite (needs to be an {@link SashForm} for the
	 * minimize button to work
	 * @param style
	 */
	public SidePanel(Composite parent, int style) {
		super(parent, style);
		if(parent instanceof SashForm) //Minimize button only works if parent is sashform
			parentSash = (SashForm)parent;
		createControls();
	}

	public Composite getContentComposite() { return contentComposite; }
	
	Composite buttonBar;
	Composite stackComposite;
	Composite emptyComposite;
	StackLayout stackLayout;
	Button minButton;
	/**
	 * Creates the button controls to minimize the sidepanel and a {@link Composite} for the contents
	 */
	public void createControls() {
		GridLayout topLayout = new GridLayout();
		topLayout.verticalSpacing = topLayout.marginTop = topLayout.marginWidth = 0;
		setLayout(topLayout);
		
		if(parentSash != null) {
			//Create minimize control on top of content
			buttonBar = new Composite(this, SWT.NULL);
			GridLayout barLayout = new GridLayout(2, true);
			barLayout.marginBottom = barLayout.marginHeight = barLayout.marginWidth = 1;
			buttonBar.setLayout(barLayout);
						
			minButton = new Button(buttonBar, SWT.TOGGLE);
			minButton.setToolTipText("Minimize this sidepanel");
			minButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(minButton.getSelection()) {
						minimize();
						minButton.setToolTipText("Restore this sidepanel");
					}
					else {
						restore();
						minButton.setToolTipText("Minimize this sidepanel");
					}
				}
			});
			minButton.setImage(SwtEngine.getImageRegistry().get("sidepanel.minimize"));
			final Button hideButton = new Button(buttonBar, SWT.PUSH);
			hideButton.setToolTipText("Close this sidepanel (use view menu to open again)");
			hideButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					hide();
					SwtEngine.getWindow().showRightPanelAction.setChecked(false);
				}
			});
			hideButton.setImage(SwtEngine.getImageRegistry().get("sidepanel.hide"));
			
			GridData buttonGrid = new GridData();
			buttonGrid.widthHint = 12;
			buttonGrid.heightHint =  12;
			hideButton.setLayoutData(buttonGrid);
			minButton.setLayoutData(buttonGrid);
			
			buttonBar.pack();
		}
		stackComposite = new Composite(this, SWT.NULL);
		stackComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		stackLayout = new StackLayout();
		stackComposite.setLayout(stackLayout);
		emptyComposite = new Composite(stackComposite, SWT.NULL);
		emptyComposite.setLayout(new FillLayout());
		contentComposite = new Composite(stackComposite, SWT.NULL);
		contentComposite.setLayout(new FillLayout());
		
		stackLayout.topControl = contentComposite;
	}
	
	/**
	 * Minimizes this panel, while the minimize button will still be visible
	 */
	public void minimize() {
		parentSash.setWeights(calculateWeights(WEIGHTS_MINIMIZE));
		stackLayout.topControl = emptyComposite;
		stackComposite.layout();
	}
	
	/**
	 * Hides this panel, the minimize button will not be visible anymore
	 */
	public void hide() {
		parentSash.setWeights(calculateWeights(WEIGHTS_HIDE));
	}
	
	public void show() {
		int sidePanelSize = GlobalPreference.getValueInt(SwtPreference.SWT_SIDEPANEL_SIZE);
		if(sidePanelSize == 0) sidePanelSize = 10; //Force show if initial size = 0
		parentSash.setWeights(calculateWeights(sidePanelSize));
	}
	
	/**
	 * Restores the size of the panel to its previous size
	 */
	public void restore() {
		if(oldWeights == null) return;
		parentSash.setWeights(oldWeights);
		stackLayout.topControl = contentComposite;
		stackComposite.layout();
	}
	
	private int[] oldWeights;
	private static final int WEIGHTS_HIDE = -1;
	private static final int WEIGHTS_MINIMIZE = -2;
	/**
	 * Calculates weights that have to be passed to the parent {@link SashForm#setWeights(int[])}
	 * to resize, minimize or hide this panel
	 * @param percent percentage of total size for this panel or 
	 * one of WEIGHTS_HIDE or WEIGHTS_MINIMIZE, in the first case the panel is hidden,
	 * so its weight is set to zero, in the second case the panel is miminized in a way the minimize 
	 * button is still visible
	 * @return
	 */
	private int[] calculateWeights(int percent) {
		Control[] controls = parentSash.getChildren();
		int[] weights = parentSash.getWeights();
		oldWeights = weights.clone();
		//Get the index of this control in the sashform
		int thisIndex = 0;
		for(int i = 0; i < controls.length; i++) {
			if(controls[i] == this) break;
			if(!(controls[i] instanceof Sash)) thisIndex++; //Don't count sash controls
		}
		
		int thisWeight = weights[thisIndex];
		
		//Get the index of the neighbouring composite
		int neighbourIndex = -1;
		if(thisIndex == weights.length - 1) neighbourIndex = thisIndex - 1;
		else neighbourIndex = thisIndex + 1;
		
		//Calculate widths needed to calculate new weight 
		int newWidth;
		switch(percent) {
		case WEIGHTS_MINIMIZE: newWidth = minButton.getSize().x; break;
		case WEIGHTS_HIDE: newWidth = 0; break;
		default:
			//Calculate new weights
			int percentLeft = 100 - percent;
			int sum = 0;
			for(int i = 0; i < weights.length; i++) {
				sum += weights[i];
				if(i == thisIndex) continue;
				weights[i] = (int)(((double)weights[i] / 100) * percentLeft);
			}
			weights[thisIndex] = (int)(((double)percent / 100) * sum);
			return weights;
			}
		
		int thisWidth = getSize().x;
		
		//Calculate new weights
		int newWeight = (int)(((double)newWidth / thisWidth) * thisWeight);
		//Adjust the weight of this and the next control
		weights[thisIndex] = newWeight;
		weights[neighbourIndex] += thisWeight - newWeight;
		return weights;
	}

}
