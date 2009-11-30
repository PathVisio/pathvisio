// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package org.pathvisio.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.AttributedString;

import org.pathvisio.model.PathwayElement;

/**
 * Represents the view of a PathwayElement with ObjectType.LABEL.
 */
public class Label extends GraphicsShape
{

	public static final int M_INITIAL_FONTSIZE = 10 * 15;
	public static final int M_INITIAL_WIDTH = 80 * 15;
	public static final int M_INITIAL_HEIGHT = 20 * 15;
	public static final double M_ARCSIZE = 225;

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
	 * @param canvas - the VPathway this label will be part of
	 */
	public Label(VPathway canvas, PathwayElement o)
	{
		super(canvas, o);
	}

	public String getLabelText() {
		return gdata.getTextLabel();
	}

	String prevText = "";
//	public void adjustWidthToText() {
//		if(gdata.getTextLabel().equals(prevText)) return;
//
//		prevText = getLabelText();
//
//		Point mts = mComputeTextSize();
//
//		//Keep center location
//		double mWidth = mts.x;
//		double mHeight = mts.y;
//
//		listen = false; //Disable listener
//		gdata.setMLeft(gdata.getMLeft() - (mWidth - gdata.getMWidth())/2);
//		gdata.setMTop(gdata.getMTop() - (mHeight - gdata.getMHeight())/2);
//		gdata.setMWidth(mWidth);
//		gdata.setMHeight(mHeight);
//		listen = true; //Enable listener
//
//		setHandleLocation();
//	}

//	private Text t;
//	public void createTextControl()
//	{
//		Color background = canvas.getShell().getDisplay()
//		.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
//
//		Composite textComposite = new Composite(canvas, SWT.NONE);
//		textComposite.setLayout(new GridLayout());
//		textComposite.setLocation(getVCenterX(), getVCenterY() - 10);
//		textComposite.setBackground(background);
//
//		org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(textComposite, SWT.CENTER);
//		label.setText("Specify label:");
//		label.setBackground(background);
//		t = new Text(textComposite, SWT.SINGLE | SWT.BORDER);
//		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		t.addSelectionListener(new SelectionAdapter() {
//			public void widgetDefaultSelected(SelectionEvent e) {
//				disposeTextControl();
//			}
//		});
//
//		t.setFocus();
//
//		Button b = new Button(textComposite, SWT.PUSH);
//		b.setText("OK");
//		b.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				disposeTextControl();
//			}
//		});
//
//		textComposite.pack();
//	}

	protected Rectangle2D getTextBounds(Graphics2D g) {
		Rectangle2D tb = null;
		if(g != null) {
			 tb = g.getFontMetrics(getVFont()).getStringBounds(getLabelText(), g);
			 tb.setRect(getVLeft() + tb.getX(), getVTop() + tb.getY(), tb.getWidth(), tb.getHeight());
		} else { //No graphics context, we can only guess...
			tb = getBoxBounds(true);
		}
		return tb;
	}

	protected Rectangle2D getBoxBounds(boolean stroke)
	{
		return getVShape(stroke).getBounds2D();
	}

	protected Dimension computeTextSize(Graphics2D g) {
		Rectangle2D tb = getTextBounds(g);
		return new Dimension((int)tb.getWidth(), (int)tb.getHeight());
	}

//	protected void disposeTextControl()
//	{
//		gdata.setTextLabel(t.getText());
//		Composite c = t.getParent();
//		c.setVisible(false);
//		c.dispose();
//	}

	double getVFontSize()
	{
		return vFromM(gdata.getMFontSize());
	}

	Font getVFont() {
		String name = gdata.getFontName();
		int style = getVFontStyle();
		int size = (int)getVFontSize();
		return new Font(name, style, size);
	}

	AttributedString getVAttributedString(String text) {
		AttributedString ats = new AttributedString(text);
		if(gdata.isStrikethru()) {
			ats.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		}
		if(gdata.isUnderline()) {
			ats.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		}

		ats.addAttribute(TextAttribute.FONT, getVFont());
		return ats;
	}

	Graphics2D g2d = null; //last Graphics2D for determining text size
	public void doDraw(Graphics2D g)
	{
		if(g2d != null) g2d.dispose();
		g2d = (Graphics2D)g.create();

		if(isSelected()) {
			g.setColor(selectColor);
		} else {
			g.setColor(gdata.getColor());
		}

		Font f = getVFont();
		g.setFont(f);

		Rectangle area = getBoxBounds(true).getBounds();

		Shape outline = null;
		double lw = DEFAULT_STROKE.getLineWidth();
		switch (gdata.getOutline())
		{
		case RECTANGLE:
			outline = new Rectangle2D.Double(getVLeft(), getVTop(), getVWidth() - lw, getVHeight() - lw);
			break;
		case ROUNDED_RECTANGLE:
			outline = new RoundRectangle2D.Double(
				getVLeft(), getVTop(), getVWidth() - lw, getVHeight() - lw,
				vFromM (M_ARCSIZE), vFromM (M_ARCSIZE));
			break;
		case NONE:
			outline = null;
			break;
		}
		if (outline != null)
		{
			g.draw (outline);
		}

		// don't draw label outside box
		g.clip (new Rectangle (area.x - 1, area.y - 1, area.width + 1, area.height + 1));

		String label = gdata.getTextLabel();
		if(label != null && !"".equals(label)) {
			//Split by newline, to enable multi-line labels
			String[] lines = label.split("\n");
			for(int i = 0; i < lines.length; i++) {
				if(lines[i].equals("")) continue; //Can't have attributed string with 0 length
				AttributedString ats = getVAttributedString(lines[i]);
				Rectangle2D tb = g.getFontMetrics().getStringBounds(ats.getIterator(), 0, lines[i].length(), g);

				int yoffset = area.y;
				int xoffset = area.x + (int)(area.width / 2) - (int)(tb.getWidth() / 2);
				//Align y-center when only one line, otherwise, align to y-top
				if(lines.length == 1) {
					yoffset += (int)(area.height / 2) + (int)(tb.getHeight() / 2);
				} else {
					yoffset += (int)tb.getHeight();
				}
				g.drawString(ats.getIterator(), xoffset,
						yoffset + (int)(i * tb.getHeight()));
			}

		}
		if(isHighlighted())
		{
			Color hc = getHighlightColor();
			g.setColor(new Color (hc.getRed(), hc.getGreen(), hc.getBlue(), 128));
			g.setStroke (new BasicStroke());
			Rectangle2D r = new Rectangle2D.Double(getVLeft(), getVTop(), getVWidth(), getVHeight());
			g.fill(r);
		}
		super.doDraw(g2d);
	}

//	public void gmmlObjectModified(PathwayEvent e) {
//		if(listen) {
//			super.gmmlObjectModified(e);
//			adjustWidthToText();
//		}
//	}

	/**
	 * Outline of a label is determined by
	 * - position of the handles
	 * - size of the text
	 * Because the text can sometimes be larger than the handles
	 */
	protected Shape calculateVOutline()
	{
		Shape outline = super.calculateVOutline();
		Rectangle2D bb = getBoxBounds(true);
		Rectangle2D tb = getTextBounds(g2d);
		tb.add(bb);
		Area a = new Area(outline);
		a.add(new Area(tb));
		return a;
	}
}
