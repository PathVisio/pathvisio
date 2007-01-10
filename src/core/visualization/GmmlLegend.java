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
package visualization;

import gmmlVision.GmmlVision;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import util.SwtUtils;
import visualization.GmmlLegend.CollapseGroup.CollapseListener;
import visualization.Visualization.PluginSet;
import visualization.VisualizationManager.VisualizationEvent;
import visualization.VisualizationManager.VisualizationListener;
import visualization.colorset.ColorCriterion;
import visualization.colorset.ColorGradient;
import visualization.colorset.ColorSet;
import visualization.colorset.ColorSetManager;
import visualization.colorset.ColorSetObject;
import visualization.colorset.ColorGradient.ColorValuePair;

public class GmmlLegend extends ScrolledComposite implements VisualizationListener {
	static final String FONT = "arial narrow";
	static final int FONTSIZE = 8;

	Button combine;
	Boolean doCombine = true;
	
	ColorSet colorSet;

	ColorSetComposite colorSets;
	PluginComposite plugins;
	
	CollapseGroup colorSetGroup;
	CollapseGroup pluginGroup;

	Combo colorSetCombo;

	public GmmlLegend(Composite parent, int style)
	{
		super(parent, style);

		createContents();
		initialize();
		VisualizationManager.addListener(this);
	}

	public void setInput(ColorSet input) {
		colorSet = input;
		refreshContent();
	}

	public void initialize() {
		combine.setSelection(true);
		
		String[] names = ColorSetManager.getColorSetNames();
		colorSetCombo.setItems(names);
		if(names.length == 0) {
			colorSetCombo.setEnabled(false);
		}
		else {
			colorSetCombo.setEnabled(!doCombine);
			colorSetCombo.select(0);
		}
		
		refreshContent();
	}
	
	void setCombine(boolean comb) {
		doCombine = comb;
		colorSetCombo.setEnabled(!doCombine);
		setInput(ColorSetManager.getColorSets().get(colorSetCombo.getSelectionIndex()));
	}
	
	void refreshContent() {		
		colorSets.refresh();
		plugins.refresh();
		rearrange();
	}

	void rearrange() {
		layout();
		colorSetGroup.refresh();
		pluginGroup.refresh();
		setMinSize(getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	void createContents() {	
		Composite contents = new Composite(this, SWT.NULL);
		setContent(contents);
		setExpandHorizontal(true);
		setExpandVertical(true);
		contents.setLayout(new GridLayout());

		Composite comboComp = createColorSetCombo(contents);
		comboComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite legendComp = createLegendComp(contents);
		legendComp.setLayoutData(new GridData(GridData.FILL_BOTH));

		setChildrenBackground(contents);
	}

	void setChildrenBackground(Composite comp) {
		comp.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		for(Control c : comp.getChildren()) {
			c.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			if(c instanceof Composite) setChildrenBackground((Composite) c);
		}
	}

	Composite createColorSetCombo(Composite parent) {
		Composite comboComp = new Composite(parent, SWT.NULL);
		comboComp.setLayout(new GridLayout(2, false));
		combine = new Button(parent, SWT.CHECK);
		GridData span = new GridData(GridData.FILL_HORIZONTAL);
		span.horizontalSpan = 2;
		combine.setLayoutData(span);
		combine.setText("Show all color-sets");
		combine.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setCombine(combine.getSelection());
			}
		});
		Label label = new Label(comboComp, SWT.NULL);
		label.setText("Show color-set:");
		colorSetCombo = new Combo(comboComp, SWT.READ_ONLY);
		colorSetCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		colorSetCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setInput(ColorSetManager.getColorSets().get(colorSetCombo.getSelectionIndex()));
			}
		});
		return comboComp;
	}

	Composite createLegendComp(Composite parent) {
		Composite legendComp = new Composite(parent, SWT.NULL);
		GridLayout legendGrid = new GridLayout();
		legendGrid.marginWidth = legendGrid.marginLeft = legendGrid.marginRight = 0;
		legendComp.setLayout(legendGrid);

		colorSetGroup = new CollapseGroup(this, legendComp, SWT.NULL);
		pluginGroup = new CollapseGroup(this, legendComp, SWT.NULL);

		colorSets = new ColorSetComposite(colorSetGroup.getGroup(), SWT.NONE);
		plugins = new PluginComposite(pluginGroup.getGroup(), SWT.NONE);

		colorSetGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pluginGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		colorSetGroup.setText("Color-sets");
		pluginGroup.setText("Plug-ins");

		colorSetGroup.getGroup().setLayout(new FillLayout());
		pluginGroup.getGroup().setLayout(new FillLayout());
		
		CollapseListener cl = new CollapseListener() {
			public void collapsed(visualization.GmmlLegend.CollapseGroup.CollapseEvent e) {
				rearrange();
			}
			public void expanded(visualization.GmmlLegend.CollapseGroup.CollapseEvent e) {
				rearrange();
			}
		};
		
		colorSetGroup.addCollapseListener(cl);
		pluginGroup.addCollapseListener(cl);

		return legendComp;
	}

	private class PluginComposite extends Composite
	{
		public PluginComposite(Composite parent, int style) {
			super(parent, style);
			setLayout(new GridLayout());
			setChildrenBackground(this);
		}
		
		public void refresh() {
			for(Control c : getChildren()) c.dispose();
			
			Visualization v = VisualizationManager.getCurrent();
			if(v == null) return;
			
			for(PluginSet ps : v.getPluginSetsDrawingOrder()) {
				if(ps.isDrawing()) {
					Group g = new Group(this, SWT.NULL);
					g.setBackground(getBackground());
					g.setLayoutData(new GridData(GridData.FILL_BOTH));
					g.setLayout(new FillLayout());
					g.setText(ps.getInstance().getName());
					Composite c = ps.getDrawingPlugin().createLegendComposite(g);
					if(c == null) g.dispose();
				}
			}
			layout();
		}
	}
	
	private class ColorSetComposite extends Composite
	{
			public ColorSetComposite(Composite parent, int style) {
				super(parent, style);
				setLayout(new GridLayout());
				setChildrenBackground(this);
			}
			
			public void refresh() {
				for(Control c : getChildren()) c.dispose();
				
				if(doCombine) {
					for(ColorSet cs : ColorSetManager.getColorSets()) {
						drawColorSet(this, cs);
					}
				} else {
					if(colorSet == null) return;
					drawColorSet(this, colorSet);
				}
				
				layout();
			}
			
			void drawColorSet(Composite parent, ColorSet cs) {
				ColorSetGroup csg = new ColorSetGroup(parent, SWT.NULL);
				csg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				csg.setInput(cs);
			}
	}

	private class ColorSetGroup extends Composite
	{
		CriteriaComposite criteria;
		GradientCanvas gradients;
		Group group;
		
		ColorSet colorSet;
		
		public ColorSetGroup(Composite parent, int style) {
			super(parent, style);
			createContents();
		}
		
		void setInput(ColorSet cs) {
			colorSet = cs;
			criteria.setInput(cs);
			gradients.setInput(cs);
			refresh();
		}
		
		void createContents() {
			setLayout(new FillLayout());
			group = new Group(this, SWT.NULL);
			group.setLayout(new GridLayout());
			criteria = new CriteriaComposite(group, SWT.NULL);
			criteria.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			gradients = new GradientCanvas(group, SWT.NULL);
			gradients.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			setChildrenBackground(this);
		}
		
		public void refresh() {
			if(colorSet != null) group.setText(colorSet.getName());
			criteria.refresh();
			gradients.refresh();
		}
		
	}
	
	private class CriteriaComposite extends Composite
	{
		ColorSet colorSet;
		
		public CriteriaComposite(Composite parent, int style)
		{
			super(parent, style);
			setChildrenBackground(this);
		}

		void setInput(ColorSet cs) {
			colorSet = cs;
			refresh();
		}
		
		final static int CLABEL_SIZE = 10;
		public void refresh()
		{
			for(Control c : getChildren()) c.dispose();

			if(colorSet == null) return;
			drawColorSet(this, colorSet);
		}
		
		void drawColorSet(Composite parent, ColorSet colorSet) {			
			Color c = null;
			
			parent.setLayout(new GridLayout(2, false));
			
			//Draw label for special criteria ('no gene found', 'no criteria met')
			String[] specialLabels = {"No criteria met", "Gene not found", "No data found"};
			RGB[] specialColors = {
					colorSet.getColor(ColorSet.ID_COLOR_NO_CRITERIA_MET), 
					colorSet.getColor(ColorSet.ID_COLOR_NO_GENE_FOUND),
					colorSet.getColor(ColorSet.ID_COLOR_NO_DATA_FOUND) };

			for(int i = 0; i < specialColors.length; i++)
			{
				c = SwtUtils.changeColor(c, specialColors[i], getDisplay());
				createCriterionLabel(parent, specialLabels[i], c);
			}

			//Draw CLabel for every criterion
			for(ColorSetObject co : colorSet.getObjects())
			{
				if(!(co instanceof ColorCriterion)) continue; //skip objects other than criretia
				ColorCriterion cc = (ColorCriterion)co;
				c = SwtUtils.changeColor(c, cc.getColor(), getDisplay());
				createCriterionLabel(parent, cc.getName(), c);
			}
			
			if(c != null) c.dispose();
		}
		
		private void createCriterionLabel(Composite parent, String text, Color c)
		{
			GridData clabelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			clabelGrid.widthHint = CLABEL_SIZE;
			clabelGrid.heightHint = CLABEL_SIZE;

			CLabel cLabel = new CLabel(parent, SWT.SHADOW_IN);
			Label label = new Label(parent, SWT.LEFT);

			label.setBackground(getBackground());
			label.setText(text);

			cLabel.setBackground(c);
			cLabel.setLayoutData(clabelGrid);
		}
	}

	private class GradientCanvas extends Canvas implements PaintListener
	{	
		ColorSet colorSet;
		
		public GradientCanvas(Composite parent, int style)
		{
			super(parent, style);
			addPaintListener(this);
		}

		public void setInput(ColorSet input) {
			colorSet = input;
			refresh();
		}
		
		public void refresh() {
			layout();
			redraw();
		}

		int getNrGradients() {
			int n = 0;
			for(ColorSetObject co : colorSet.getObjects()) {
				if(co instanceof ColorGradient) n++;
			}
			return n;
		}

		public void paintControl (PaintEvent e)
		{
			if(colorSet == null) return;
			
			//Divide canvas in nr-gradients rows
			Rectangle cla = getClientArea();
			Point size = new Point(cla.width, cla.height);
			int n = getNrGradients();
			int i = 0;
			for(ColorSetObject co : colorSet.getObjects()) {
				if(co instanceof ColorGradient) {
					ColorGradient cg = (ColorGradient)co;
					Rectangle area = new Rectangle(
							0, i++ * size.y / n,
							size.x, size.y / n++);
					drawColorGradient(e, cg, area);
				}
			}
		}

		public Point computeSize(int wHint, int hHint) {
			if(colorSet == null) return new Point(0,0);
						
			int charw = SwtUtils.getAverageCharWidth(getDisplay());
			int x = 0;
			int nr = 0;
			for(ColorSetObject co : colorSet.getObjects()) {
				if(co instanceof ColorGradient) {
					x = Math.max(x, co.getName().length() * charw);
					nr++;
				}
			}
			int y = nr * (MAX_BAR_HEIGHT + MARGIN_VERTICAL + LABEL_WIDTH);
			return new Point(x, y);
		}
		
		public Point computeSize(int wHint, int hHint, boolean changed) {
			return computeSize(wHint, hHint);
		}
		
		final static int LABEL_WIDTH = 20;
		final static int MAX_BAR_HEIGHT = 10;
		final static int MARGIN_VERTICAL = 20;
		final static int MARGIN_HORIZONTAL = 15;
		final static int MARKER_LENGTH = 4;
		public void drawColorGradient(PaintEvent e, ColorGradient cg, Rectangle r)
		{
			Color c = null;
			RGB oldBackground = getBackground().getRGB();

			double[] minmax = cg.getMinMax();
			double min = minmax[0];
			double max = minmax[1];

			if((float)max == (float)min) {
				return;
			}

			// Get region to draw
			int yStart = r.y + MARGIN_VERTICAL;
			int barHeight = MAX_BAR_HEIGHT;
			int start = r.x + MARGIN_HORIZONTAL;
			int end = r.width - MARGIN_HORIZONTAL;

			int n = end - start;

			// Fill squares with color cg.getColor()
			for(int i = start; i < end; i++) {
				double colorValue = min + (i-start) * (max - min) / n;
				RGB rgb = cg.getColor(colorValue);
				if(rgb != null) {
					c = SwtUtils.changeColor(c, rgb, e.display);
					e.gc.setBackground(c);
					e.gc.fillRectangle(i, yStart, 1, barHeight);
				}
			}

			Font f = new Font(e.display, FONT, FONTSIZE, SWT.NONE);
			e.gc.setFont(f);

			int markerCenter = yStart + barHeight;
			c = SwtUtils.changeColor(c, oldBackground, e.display);
			e.gc.setBackground(c);
			for(ColorValuePair cvp : cg.getColorValuePairs())
			{
				int x = (int)(start + (cvp.getValue() - min) / (max - min) * (end - start));
				e.gc.drawLine(x, markerCenter - MARKER_LENGTH, x, markerCenter + MARKER_LENGTH);
				Point labelSize = e.gc.textExtent(Double.toString(cvp.getValue()));
				e.gc.drawString(Double.toString(cvp.getValue()), x - labelSize.x / 2, 
						markerCenter + labelSize.y / 2, true);
			}
			
			String label = cg.getName();
			Point labelSize = e.gc.textExtent(label);
			e.gc.drawString(label, (end - start) / 2 - labelSize.x / 2, 
					yStart - barHeight - labelSize.y / 2, true);	

			c.dispose();
			f.dispose();
		}
	}

	public void visualizationEvent(VisualizationEvent e) {
		switch(e.type) {
		case VisualizationEvent.COLORSET_ADDED:
		case VisualizationEvent.COLORSET_REMOVED:
			initialize();
			break;
		default:
			refreshContent();
		}
	}
	
	static class CollapseGroup extends Composite {
		static final int SWITCH_SIZE = 9;
		GmmlLegend legend;
		Group group;
		Composite stackComp;
		StackLayout stackLayout;
		Label groupLabel;
		Label switchLabel;
		
		boolean expanded = true;
		
		public CollapseGroup(GmmlLegend legend, Composite parent, int style) {
			super(parent, style);
			this.legend = legend;
			GridLayout grid = new GridLayout(2, false);
			grid.horizontalSpacing = 3;
			grid.marginBottom = grid.marginHeight = 0;
			grid.marginLeft = grid.marginRight = grid.marginBottom = grid.marginTop = 0;
			setLayout(grid);
			createContents();
		}
		
		void createContents() {
			switchLabel = new Label(this, SWT.NULL);
			GridData labelGrid = new GridData(	GridData.HORIZONTAL_ALIGN_BEGINNING | 
												GridData.VERTICAL_ALIGN_BEGINNING);
			labelGrid.widthHint = labelGrid.heightHint = SWITCH_SIZE;
			switchLabel.setLayoutData(labelGrid);
			switchLabel.addMouseListener(new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					if(expanded) collapse();
					else expand();
				}
			});
			stackComp = new Composite(this, SWT.NULL);
			stackComp.setLayoutData(new GridData(GridData.FILL_BOTH));
			stackLayout = new StackLayout();
			stackComp.setLayout(stackLayout);
					
			groupLabel = new Label(stackComp, SWT.NULL);
			group = new Group(stackComp, SWT.NULL);
			
			expand();
		}
		
		public Group getGroup() {
			return group;
		}
		
		public Label getSwitchLabel() {
			return switchLabel;
		}
		
		public void setText(String text) {
			groupLabel.setText(text);
			group.setText(text);
		}
		
		void collapse() {
			setExpanded(false);
		}
		
		void expand() {
			setExpanded(true);
		}

		public void layout(boolean changed, boolean all) {
			super.layout(changed, all);
			getParent().layout(changed, all);
		}
		
		public Point computeSize(int wHint, int hHint, boolean changed) {
			int x = super.computeSize(wHint, hHint, changed).x;
			int y = expanded ?
					group.computeSize(wHint, hHint, changed).y :
					groupLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			return new Point(x, y);
		}
		
		public void refresh() {
			stackLayout.topControl = expanded ? group : groupLabel;
			stackComp.layout();
			Object ld = getLayoutData();
			if(ld instanceof GridData) {
				GridData gd = (GridData) ld;
				gd.heightHint = computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			}
			layout();
			switchLabel.setImage(GmmlVision.getImageRegistry().get(
					expanded ? "tree.expanded" : "tree.collapsed"));
		}
		
		void setExpanded(boolean exp) {
			expanded = exp;
			
			refresh();
			
			for(CollapseListener l : listeners) 
				l.expanded(new CollapseEvent(this, expanded ? CollapseEvent.EXPANDED : CollapseEvent.COLLAPSED));
		}
		
		List<CollapseListener> listeners = new ArrayList<CollapseListener>();
		
		void addCollapseListener(CollapseListener l) {
			listeners.add(l);
		}
				
		static class CollapseEvent extends EventObject {
			static final int COLLAPSED = 0;
			static final int EXPANDED = 1;
			int type;
			public CollapseEvent(Object source, int type) {
				super(source);
				this.type = type;
			}		
		}
		
		static interface CollapseListener {
			public void collapsed(CollapseEvent e);
			public void expanded(CollapseEvent e);
		}
	}
}