package graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import util.SwtUtils;
import data.GmmlData;
import data.GmmlDataObject;
import data.MappFormat;

/**
 * This class implements a geneproduct and 
 * provides methods to resize and draw it.
 */
public class GmmlGeneProduct extends GmmlGraphicsShape
{
	private static final long serialVersionUID = 1L;
	private static final int INITIAL_FONTSIZE = 10;
	public static final int INITIAL_WIDTH = 80;
	public static final int INITIAL_HEIGHT = 20;
	public static final RGB INITIAL_FILL_COLOR = new RGB(255, 255, 255);
	
	double fontSizeDouble;
	int fontSize;

	// note: not the same as color!
	RGB fillColor = INITIAL_FILL_COLOR;
		
	public GmmlGeneProduct (GmmlDrawing canvas, GmmlDataObject o) {
		super(canvas, o);
		drawingOrder = GmmlDrawing.DRAW_ORDER_GENEPRODUCT;				
		
		fontSizeDouble = INITIAL_FONTSIZE * canvas.getZoomFactor();
		fontSize = (int)fontSizeDouble;
		setHandleLocation();
	}
	
	/**
	 * @deprecated: get this info from GmmlDataObject directly
	 */
	public String getName()
	{
		//Looks like the wrong way around, but in gmml the name/symbol is attribute 'GeneID'
		//NOTE: maybe change this in gmml?
		return gdata.getGeneID();
	}
	
	/**
	 * @deprecated: get this info from GmmlDataObject directly
	 */
	public String getID() 
	{
		//Looks like the wrong way around, but in gmml the ID is attribute 'Name'
		//NOTE: maybe change this in gmml?
		return gdata.getGeneProductName();
	}
	
	public void setFontSize(double size) {
		fontSizeDouble = size;
		fontSize = (int)size;
	}
	
	/**
	 * Looks up the systemcode for this gene in {@link GmmlData#sysName2Code}
	 * @param systemName	The system name (as in gmml)
	 * @return	The system code or an empty string if the system is not found
	 * 
	 * @deprecated: use GmmlDataObject.getSystemCode()
	 */
	public String getSystemCode()
	{
		String systemCode = "";
		if(MappFormat.sysName2Code.containsKey(gdata.getDataSource())) 
			systemCode = MappFormat.sysName2Code.get(gdata.getDataSource());
		return systemCode;
	}
	
	private Text t;
	public void createTextControl()
	{		
		Color background = canvas.getShell().getDisplay()
		.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		
		Composite textComposite = new Composite(canvas, SWT.NONE);
		textComposite.setLayout(new GridLayout());
		textComposite.setLocation(getCenterX(), getCenterY() - 10);
		textComposite.setBackground(background);
		
		Label label = new Label(textComposite, SWT.CENTER);
		label.setText("Specify gene name:");
		label.setBackground(background);
		t = new Text(textComposite, SWT.SINGLE | SWT.BORDER);
				
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
	
	protected void disposeTextControl()
	{	
		markDirty();
		gdata.setGeneID (t.getText());
		markDirty();
		//TODO: implement listener. 
		//canvas.updatePropertyTable(this);
		Composite c = t.getParent();
		c.setVisible(false);
		c.dispose();
		
		canvas.redrawDirtyRect();
	}
		
	public void adjustToZoom(double factor)
	{
		super.adjustToZoom(factor);
		fontSizeDouble *= factor;
		fontSize = (int)fontSizeDouble;
	}

	public void draw(PaintEvent e, GC buffer)
	{
		Color c = null;
		Font f = null;
		
		if(isSelected())
		{
			c = SwtUtils.changeColor(c, selectColor, e.display);
		}
		else 
		{
			c = SwtUtils.changeColor(c, gdata.getColor(), e.display);
		}
		
		buffer.setForeground(c);
		buffer.setLineStyle (SWT.LINE_SOLID);
		buffer.setLineWidth (1);		
		
		Rectangle area = new Rectangle(
				(int)gdata.getLeft(), 
				(int)gdata.getTop(), 
				(int)gdata.getWidth(), 
				(int)gdata.getHeight());
		
		buffer.drawRectangle (area);
		
		buffer.setClipping ( area.x - 1, area.y - 1, area.width + 1, area.height + 1);
		
		f = SwtUtils.changeFont(f, new FontData(gdata.getFontName(), fontSize, SWT.NONE), e.display);
		buffer.setFont(f);
		
		String label = getName();
		Point textSize = buffer.textExtent (label);
		buffer.drawString (label, 
				area.x + (int)(area.width / 2) - (int)(textSize.x / 2),
				area.y + (int)(area.height / 2) - (int)(textSize.y / 2), true);
				
				
		Region r = null;
		buffer.setClipping(r);
		
		c.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
	public void drawHighlight(PaintEvent e, GC buffer)
	{
		if(isHighlighted())
		{
			Color c = null;
			c = SwtUtils.changeColor(c, highlightColor, e.display);
			buffer.setForeground(c);
			buffer.setLineWidth(2);
			buffer.drawRectangle (
					(int)(gdata.getLeft()) - 1,
					(int)(gdata.getTop()) - 1,
					(int)gdata.getWidth() + 3,
					(int)gdata.getHeight() + 3
				);
			if(c != null) c.dispose();
		}
	}
}
