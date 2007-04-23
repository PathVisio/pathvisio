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
package org.pathvisio.graphics;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.pathvisio.preferences.GmmlPreferences;
import org.pathvisio.util.LinAlg;
import org.pathvisio.util.SwtUtils;
import org.pathvisio.model.GmmlDataObject;
import org.pathvisio.model.GmmlEvent;

public class GmmlLabel extends GmmlGraphicsShape
{
	private static final long serialVersionUID = 1L;
	
	public static final int M_INITIAL_FONTSIZE = 10 * 15;
	public static final int M_INITIAL_WIDTH = 80 * 15;
	public static final int M_INITIAL_HEIGHT = 20 * 15;
		
	double getFontSize()
	{
		return gdata.getMFontSize() * canvas.getZoomFactor();
	}
	
	void setFontSize(double v)
	{
		gdata.setMFontSize(v / canvas.getZoomFactor());
	}
				
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this label will be part of
	 */
	public GmmlLabel(GmmlDrawing canvas, GmmlDataObject o)
	{
		super(canvas, o);
		setHandleLocation();
	}
	
	public int getDrawingOrder() {
		return GmmlDrawing.DRAW_ORDER_LABEL;
	}
	
	public String getLabelText() {
		return gdata.getTextLabel();
	}
	
	String prevText = "";
	public void adjustWidthToText() {
		if(gdata.getTextLabel().equals(prevText)) return;
		
		prevText = getLabelText();
		
		Point mts = mComputeTextSize();
		
		//Keep center location
		double mWidth = mts.x;
		double mHeight = mts.y;
		
		listen = false; //Disable listener
		gdata.setMLeft(gdata.getMLeft() - (mWidth - gdata.getMWidth())/2);
		gdata.setMTop(gdata.getMTop() - (mHeight - gdata.getMHeight())/2);
		gdata.setMWidth(mWidth);
		gdata.setMHeight(mHeight);
		listen = true; //Enable listener
		
		setHandleLocation();
	}
	
	private Text t;
	public void createTextControl()
	{
		Color background = canvas.getShell().getDisplay()
		.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		
		Composite textComposite = new Composite(canvas, SWT.NONE);
		textComposite.setLayout(new GridLayout());
		textComposite.setLocation(getVCenterX(), getVCenterY() - 10);
		textComposite.setBackground(background);
		
		Label label = new Label(textComposite, SWT.CENTER);
		label.setText("Specify label:");
		label.setBackground(background);
		t = new Text(textComposite, SWT.SINGLE | SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		t.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				disposeTextControl();
			}
		});
				
		t.setFocus();
		
		Button b = new Button(textComposite, SWT.PUSH);
		b.setText("OK");
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				disposeTextControl();
			}
		});
		
		textComposite.pack();
	}
	
	Point mComputeTextSize() {
		GC gc = new GC(canvas.getDisplay());
		Font f = new Font(canvas.getDisplay(), 
				gdata.getFontName(), 
				(int)gdata.getMFontSize(), getFontStyle());
		gc.setFont (f);
		Point ts = gc.textExtent(gdata.getTextLabel());
		f.dispose();
		gc.dispose();
		
		return ts;
	}
	
	protected void disposeTextControl()
	{
		gdata.setTextLabel(t.getText());
		Composite c = t.getParent();
		c.setVisible(false);
		c.dispose();
	}
		
	double getVFontSize()
	{
		return vFromM(gdata.getMFontSize());
	}

	private int getFontStyle() {
		int style = SWT.NONE;
		
		if (gdata.isBold())
		{
			style |= SWT.BOLD;
		}
		
		if (gdata.isItalic())
		{
			style |= SWT.ITALIC;
		}
		return style;
	}
	
	public void draw(PaintEvent e, GC buffer)
	{
		int style = getFontStyle();
		
		Font f = new Font(e.display, gdata.getFontName(), (int)getVFontSize(), style);
		
		buffer.setFont (f);
		
		Point textSize = buffer.textExtent (gdata.getTextLabel());
		
		Color c = null;
		if (isSelected())
		{
			c = SwtUtils.changeColor(c, selectColor, e.display);
		}
		else if (isHighlighted())
		{
			RGB rgb = GmmlPreferences.getColorProperty(GmmlPreferences.PREF_COL_HIGHLIGHTED);
			c = SwtUtils.changeColor(c, rgb, e.display);
		}
		else 
		{
			c = SwtUtils.changeColor(c, gdata.getColor(), e.display);
		}
		buffer.setForeground (c);
		
		buffer.drawString (gdata.getTextLabel(), 
			(int) getVCenterX() - (textSize.x / 2) , 
			(int) getVCenterY() - (textSize.y / 2), true);
		
		f.dispose();
		c.dispose();
		
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}	
	
	public void gmmlObjectModified(GmmlEvent e) {
		if(listen) {
			super.gmmlObjectModified(e);
			adjustWidthToText();
		}
	}
	
	/**
	 * Outline of a label is determined by
	 * - position of the handles
	 * - size of the text
	 * Because the text can sometimes be larger than the handles
	 */
	protected Shape getVOutline()
	{
		int[] vx = new int[4];
		int[] vy = new int[4];
		
		int[] p = getVHandleLocation(handleNE).asIntArray();
		vx[0] = p[0]; vy[0] = p[1];
		p = getVHandleLocation(handleSE).asIntArray();
		vx[1] = p[0]; vy[1] = p[1];
		p = getVHandleLocation(handleSW).asIntArray();
		vx[2] = p[0]; vy[2] = p[1];
		p = getVHandleLocation(handleNW).asIntArray();
		vx[3] = p[0]; vy[3] = p[1];
		
		Polygon pol = new Polygon(vx, vy, 4);		
		Rectangle bounds = pol.getBounds();
		
		Point mq = mComputeTextSize();
		double vqx = vFromM(mq.x);
		double vqy = vFromM(mq.y);
		
		LinAlg.Point c = getVCenter();
		bounds.add(new Rectangle2D.Double(c.x - vqx / 2, c.y - vqy / 2, vqx, vqy)); 
		
		return bounds;
	}
	
}
