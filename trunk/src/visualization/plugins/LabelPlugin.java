package visualization.plugins;

import gmmlVision.GmmlVision;
import graphics.GmmlGeneProduct;
import graphics.GmmlGraphics;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.jdom.Element;

import util.SwtUtils;
import visualization.Visualization;

/**
 * Provides label for Gene Product
 * @author thomas
 *
 */
public class LabelPlugin extends VisualizationPlugin {
	static final String NAME = "Gene product label";
	static final String DESCRIPTION = 
		"This plugin shows a label with customizable font on Gene Products.\n" +
		"The label text can be set to the Gene Product's ID or Symbol.";
	static final FontData DEFAULT_FONTDATA = new FontData("Arial narrow", 10, SWT.NORMAL);
			
	final static int STYLE_ID = 0;
	final static int STYLE_SYMBOL = 1;
	
	Label labelSidePanel;
	
	int style = STYLE_SYMBOL;
	boolean adaptFontSize = true;
	
	FontData fontData;
	
	public LabelPlugin(Visualization v) {
		super(v);		
	    setIsConfigurable(true);
		setDisplayOptions(DRAWING | TOOLTIP);
		setIsGeneric(true);
		setUseProvidedArea(true);
	}

	void setStyle(int style) {
		this.style = style;
		fireModifiedEvent();
	}
	
	public String getName() { return NAME; }
	public String getDescription() { return DESCRIPTION; }
	
	public void createSidePanelComposite(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new FillLayout());
		labelSidePanel = new Label(comp, SWT.CENTER);
	}

	public void draw(GmmlGraphics g, PaintEvent e, GC buffer) {
		if(g instanceof GmmlGeneProduct) {
			Font f = null;
			
			Region region = getVisualization().getReservedRegion(this, g);
			Rectangle area = region.getBounds();
			
			buffer.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
			buffer.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
			buffer.fillRectangle(area);
			buffer.drawRectangle(area);
			
			buffer.setClipping(region);
			
			f = SwtUtils.changeFont(f, getFontData(true), e.display);
		
			String label = getLabelText((GmmlGeneProduct) g);
			
			if(adaptFontSize) {
				SwtUtils.adjustFontSize(f, new Point(area.width, area.height), label, buffer, e.display);
			} else
				buffer.setFont(f);
			
			Point textSize = buffer.textExtent (label);
			buffer.drawString (label, 
					area.x + (int)(area.width / 2) - (int)(textSize.x / 2),
					area.y + (int)(area.height / 2) - (int)(textSize.y / 2), true);
			
//			buffer.drawRectangle( 
//					area.x + (int)(area.width / 2) - (int)(textSize.x / 2),
//					area.y + (int)(area.height / 2) - (int)(textSize.y / 2), textSize.x, textSize.y);
			
			Region none = null;
			buffer.setClipping(none);
						
			f.dispose();
			region.dispose();
		}
	}
	
	void setAdaptFontSize(boolean adapt) {
		adaptFontSize = adapt;
		fireModifiedEvent();
	}
	
	void setFontData(FontData fd) {
		if(fd != null) {
			fontData = fd;
			fireModifiedEvent();
		}
	}
	
	int getFontSize() {
		return getFontData().getHeight();
	}
	
	FontData getFontData() {
		return getFontData(false);
	}
	
	FontData getFontData(boolean adjustZoom) {
		FontData fd = fontData == null ? DEFAULT_FONTDATA : fontData;
		if(adjustZoom) {
			fd = new FontData(fd.getName(), fd.height, fd.getStyle());
			fd.setHeight((int)Math.ceil(fd.getHeight() * GmmlVision.getDrawing().getZoomFactor()));
		}
		return fd;
	}
	
	public Composite getToolTipComposite(Composite parent, GmmlGraphics g) {
		if(g instanceof GmmlGeneProduct) {
			Composite comp = new Composite(parent, SWT.NULL);
			comp.setBackground(comp.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			comp.setLayout(new FillLayout());
			Label label = new Label(comp, SWT.CENTER);
			label.setBackground(comp.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			label.setText(getLabelText((GmmlGeneProduct) g));
			return comp;
		}
		return null;
	}

	protected Composite createConfigComposite(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new GridLayout());
		
		createFontComp(comp);
		
		Group typeGroup = new Group(comp, SWT.NULL);
		typeGroup.setLayout(new RowLayout(SWT.VERTICAL));
		typeGroup.setText("Label text");
		final Button symbol = new Button(typeGroup, SWT.RADIO);
		symbol.setText("Geneproduct name");
		final Button id = new Button(typeGroup, SWT.RADIO);
		id.setText("Geneproduct ID");
		
		SelectionAdapter radioAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if		(e.widget == id) 		setStyle(STYLE_ID);
				else if (e.widget == symbol) 	setStyle(STYLE_SYMBOL);
			}
		};
		
		symbol.setSelection(style == STYLE_SYMBOL);
		id.setSelection(style == STYLE_ID);
		
		id.addSelectionListener(radioAdapter);
		symbol.addSelectionListener(radioAdapter);
		
		return comp;
	}
		
	Composite createFontComp(Composite parent) {
		Group fontSizeComp = new Group(parent, SWT.NULL);
		fontSizeComp.setText("Label font");
		fontSizeComp.setLayout(new RowLayout(SWT.VERTICAL));
		final Button font = new Button(fontSizeComp, SWT.PUSH);
		font.setText("Change label font");
		font.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FontDialog fd = new FontDialog(font.getShell());
				fd.setFontList(new FontData[] { getFontData() });
				setFontData(fd.open());
			}
		});
		final Button adapt = new Button(fontSizeComp, SWT.CHECK);
		adapt.setText("Adapt font size to genebox size");
		adapt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setAdaptFontSize(adapt.getSelection());
			}
		});
		adapt.setSelection(adaptFontSize);
		return fontSizeComp;
	}
	
	
	public void updateSidePanel(Collection<GmmlGraphics> objects) { }
	
	private String getLabelText(GmmlGeneProduct g) {
		switch(style) {
		case STYLE_ID: 		return g.getID();
		case STYLE_SYMBOL:
		default:			return g.getName();
		}
	}

	static final String XML_ATTR_STYLE = "style";
	static final String XML_ATTR_ADAPT_FONT = "adjustFontSize";
	static final String XML_ATTR_FONTDATA = "font";
	public Element toXML() {
		Element elm = super.toXML();
		elm.setAttribute(XML_ATTR_STYLE, Integer.toString(style));
		elm.setAttribute(XML_ATTR_ADAPT_FONT, Boolean.toString(adaptFontSize));
		elm.setAttribute(XML_ATTR_FONTDATA, getFontData().toString());
		return elm;
	}
	
	public void loadXML(Element xml) {
		super.loadXML(xml);
		
		String styleStr = xml.getAttributeValue(XML_ATTR_STYLE);
		String adaptStr = xml.getAttributeValue(XML_ATTR_ADAPT_FONT);
		String fontStr = xml.getAttributeValue(XML_ATTR_FONTDATA);
		try {
			setStyle(Integer.parseInt(styleStr));
			adaptFontSize = Boolean.parseBoolean(adaptStr);
			fontData = new FontData(fontStr);
		} catch(NumberFormatException e) {
			GmmlVision.log.error("Unable to get style for " + NAME, e);
		}
	}
}

