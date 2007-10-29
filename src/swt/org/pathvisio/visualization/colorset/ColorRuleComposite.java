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
package org.pathvisio.visualization.colorset;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.pathvisio.util.swt.SwtUtils;

class ColorRuleComposite extends ColorSetObjectComposite 
{
	private final int colorLabelSize = 15;
	private RuleComposite critComp;
	private Text exprText;
	private CLabel colorLabel;
	private org.eclipse.swt.graphics.Color color;
	
	public ColorRuleComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	protected void refresh()
	{
		critComp.refresh();
		changeColorLabel(getInput() == null ? null : SwtUtils.color2rgb(getInput().getColor()));
	}
	
	ColorRule getInput() {
		return (ColorRule)input;
	}
			
	public void setInput(ColorSetObject o) {
		super.setInput(o);
		if(o == null) critComp.setInput(null);
		else critComp.setInput(((ColorRule)o).getCriterion());
		refresh();
	}
	
	private RGB askColor() {
		ColorDialog dg = new ColorDialog(getShell());
		dg.setRGB(SwtUtils.color2rgb(getInput().getColor()));
		return dg.open();
	}
	
	private void changeColor(RGB rgb) {
		if(rgb != null) {
			ColorRule c = getInput();
			if(c != null) c.setColor(SwtUtils.rgb2color(rgb));
			changeColorLabel(rgb);
		}
	}
	
	private void changeColorLabel(RGB rgb) {
		if(rgb != null) {
			color = SwtUtils.changeColor(color, rgb, colorLabel.getDisplay());
			colorLabel.setBackground(color);
		}
	}
	
	protected void createContents()
	{
		setLayout(new GridLayout());
		
		Composite colorComp = createColorComp(this);
		colorComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				    
	    critComp = new RuleComposite(this, null);
	    critComp.setLayoutData(new GridData(GridData.FILL_BOTH));
	    critComp.fetchSymbolsFromGex();
	}
	
	private Composite createColorComp(Composite parent) {
		Composite colorComp = new Composite(parent, SWT.NULL);
		colorComp.setLayout(new GridLayout(3, false));
		
		final GridData colorLabelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		colorLabelGrid.widthHint = colorLabelGrid.heightHint = colorLabelSize;
		
		Label label = new Label(colorComp, SWT.CENTER);
		label.setText("Color:");

		colorLabel = new CLabel(colorComp, SWT.SHADOW_IN);
		colorLabel.setLayoutData(colorLabelGrid);
		colorLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));

		Button colorButton = new Button(colorComp, SWT.PUSH);
		colorButton.addListener(SWT.Selection | SWT.Dispose, new Listener() {
			public void handleEvent(Event e) {
				switch(e.type) {
				case SWT.Selection:
					RGB rgb = askColor();
					changeColor(rgb);
				break;
				case SWT.Dispose:
					color.dispose();
				break;
				}
			}
		});
		
		colorButton.setLayoutData(colorLabelGrid);
		colorButton.setText("...");
		
		return colorComp;
	}
}
