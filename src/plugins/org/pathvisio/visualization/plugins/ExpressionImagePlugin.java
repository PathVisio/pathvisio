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
package org.pathvisio.visualization.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.jdom.Element;
import org.pathvisio.Engine;
import org.pathvisio.data.CachedData;
import org.pathvisio.data.Gex;
import org.pathvisio.data.Gdb.IdCodePair;
import org.pathvisio.data.Gex.Sample;
import org.pathvisio.util.ColorConverter;
import org.pathvisio.util.SwtUtils;
import org.pathvisio.view.Graphics;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.colorset.ColorSet;

public class ExpressionImagePlugin extends PluginWithColoredSamples {
	static final String NAME = "Colored image";
	static final String DESCRIPTION = 
		"This plugin displays one or more images on Gene Product objects and \n" +
		"colors the image(s) accoring to the expression value for the Gene Product.";
		
	static final RGB DEFAULT_TRANSPARENT = SwtUtils.color2rgb(Engine.TRANSPARENT_COLOR);
		
	List<URL> imageURLs;
	
	public ExpressionImagePlugin(Visualization v) {
		super(v);
		setDisplayOptions(DRAWING | SIDEPANEL);
		setIsConfigurable(true);
		setIsGeneric(false);
		setUseProvidedArea(true);
	}

	public String getName() { return NAME; }
	public String getDescription() { return DESCRIPTION; }

	private List<URL> defaultURLs() {
		return new ArrayList<URL>(Arrays.asList(new URL[] {
				Engine.getResourceURL("images/protein_hi.bmp"),
				Engine.getResourceURL("images/mRNA_hi.bmp") }));
	}
	
	List<URL> getImageURLs() { 
		if(imageURLs == null) imageURLs = defaultURLs();
		return imageURLs;
	}
	
	void addImageURL(URL url) {
		if(!imageURLs.contains(url))imageURLs.add(url);
	}
	
	void removeImageURL(URL url) {
		if(url.getProtocol().equals("file")) imageURLs.remove(url);
	}
	
	protected void drawSample(ConfiguredSample s, IdCodePair idc, Rectangle area, PaintEvent e, GC gc) {
		CachedData cache = Gex.getCachedData();
		ColorSet cs = s.getColorSet();

		RGB rgb = cs.getColor(cache.getAverageSampleData(idc), s.getId());
		
		drawImage((ImageSample)s, rgb, area, e, gc);
	}
	
	protected void drawLegendSample(ConfiguredSample s, Rectangle area, PaintEvent e, GC gc) {
		drawImage((ImageSample)s, new RGB(255, 255, 255), area, e, gc);
		e.gc.drawRectangle(area);
	}
	
	void drawImage(ImageSample is, RGB rgb, Rectangle area, PaintEvent e, GC gc) {
		ImageData id = is.getImageData(rgb);
		if(id != null) {
			Image image = new Image(e.display, id);
			Point scaleTo = is.getScaleSize(new Point(area.width, area.height));

			drawBackground(area, gc, e);

			Rectangle ib = image.getBounds();
			int xs = area.width - scaleTo.x;
			int ys = area.height - scaleTo.y;
			gc.drawImage(image, ib.x, ib.y, ib.width, ib.height, 
					area.x + xs / 2, area.y + ys / 2, scaleTo.x, scaleTo.y);
			image.dispose();
		}
	}
		
	void drawNoDataFound(ConfiguredSample s, Rectangle area, PaintEvent e, GC buffer) {
		Color c = new Color(e.display, s.getColorSet().getColor(ColorSet.ID_COLOR_NO_DATA_FOUND));
		buffer.setBackground(c);
		buffer.fillRectangle(area);
		c.dispose();
	}
	
	void drawBackground(Rectangle area, GC buffer, PaintEvent e) {
		buffer.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
		buffer.fillRectangle(area);
		
	}
	
	final static String XML_ELM_URL = "image";
	void loadAttributes(Element xml) {
		for(Object o : xml.getChildren(XML_ELM_URL)) {
			try {
				URL url = new URL(((Element)o).getText());
				addImageURL(url);
			} catch(Exception e) {
				Engine.log.error("couldn't load image URL for plugin", e);
			}
		}
	}
	
	void saveAttributes(Element xml) {
		for(URL url : getImageURLs()) {
			Element elm = new Element(XML_ELM_URL);
			elm.setText(url.toString());
			xml.addContent(elm);
		}
	}
	
	protected ConfiguredSample createConfiguredSample(Sample s) {
		return new ImageSample(s);
	}
	
	protected ConfiguredSample createConfiguredSample(Element xml) throws Exception {
		return new ImageSample(xml);
	}
	
	Composite createOptionsComp(Composite parent) {
		return new Composite(parent, SWT.NULL);
	}
		
	protected SampleConfigComposite createSampleConfigComp(Composite parent) {
		return new ImageConfigComposite(parent, SWT.NULL);
	}
	
	protected class ImageConfigComposite extends SampleConfigComposite {		
		ListViewer imageList;
		CLabel colorLabel, imageLabel;
		Color replaceColor;
		Image image;
		Button aspectButton;
		Spinner spinner;
		
		public ImageConfigComposite(Composite parent, int style) {
			super(parent, style);
		}
		
		ImageSample getInput() {
			return input == null || input.length == 0 ? null : (ImageSample)input[0];
		}
		
		void createContents() {
			setLayout(new FillLayout());
			Group group = new Group(this, SWT.NULL);
			group.setText("Image to display for this sample");
			group.setLayout(new FillLayout(SWT.HORIZONTAL));
			
			createListComp(group);
			createImageComp(group);
			
			imageList.setInput(getImageURLs());
			setInput(null);
		}
		
		String shortenURL(String urlString) {
			//Remove middle path (after first /, to before last /)
			if(urlString == null) return "null";
			String[] parts = urlString.split("/");
			if(parts.length <= 2) {
				return urlString;
			} else {
				String shorten = "";
				for(int i = 0; i < parts.length; i ++) {
					if(i == 1) shorten = parts[i - 1] + "/" + parts[i] + "/.../";
					else if (i == parts.length - 1) shorten += parts[i];
				}
				return shorten;
			}
		}
		
		Composite createListComp(Composite parent) {
			Composite listComp = new Composite(parent, SWT.NULL);
			listComp.setLayout(new GridLayout());
			
			imageList = new ListViewer(listComp, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			imageList.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
			imageList.setContentProvider(new ArrayContentProvider());
			imageList.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return shortenURL(((URL)element).toString());
				}
			});
			imageList.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					URL url = (URL)((IStructuredSelection)event.getSelection()).getFirstElement();
					getInput().setURL(url);
					refreshImage();
				}
			});
			
			Composite buttonComp = new Composite(listComp, SWT.NULL);
			buttonComp.setLayout(new RowLayout());
			final Button add = new Button(buttonComp, SWT.PUSH);
			add.setText("Add image...");
			final Button remove = new Button(buttonComp, SWT.PUSH);
			remove.setText("Remove image");
			
			SelectionAdapter buttonAdapter = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(e.widget == add) addImage();
					else removeImage();
				}
			};
			remove.addSelectionListener(buttonAdapter);
			add.addSelectionListener(buttonAdapter);
			return listComp;
		}

		Composite createImageComp(Composite parent) {
			Group imageGroup = new Group(parent, SWT.NULL);
			imageGroup.setLayout(new GridLayout());
			imageGroup.setText("Image settings");
			
			imageLabel = new CLabel(imageGroup, SWT.CENTER);
			GridData grid = new GridData(GridData.FILL_BOTH);
			grid.heightHint = grid.widthHint = 70;
			imageLabel.setLayoutData(grid);
			imageLabel.addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					refreshImage();
				}
			});
			Composite buttons = new Composite(imageGroup, SWT.NULL);
			buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			buttons.setLayout(new GridLayout(2, false));
			
			aspectButton = new Button(buttons, SWT.CHECK);
			GridData span = new GridData();
			span.horizontalSpan = 2;
			aspectButton.setLayoutData(span);
			aspectButton.setText("Maintain aspect ratio");
			aspectButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					getInput().setMaintainAspect(aspectButton.getSelection());
					refreshImage();
				}
			});
				
			Composite cc = new Composite(buttons, SWT.NULL);
			cc.setLayoutData(span);
			cc.setLayout(new GridLayout(3, false));
			colorLabel = new CLabel(cc, SWT.NULL);
			colorLabel.setLayoutData(SwtUtils.getColorLabelGrid());
			Button colorButton = new Button(cc, SWT.PUSH);
			colorButton.setText("...");
			colorButton.setLayoutData(SwtUtils.getColorLabelGrid());
			colorButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					changeColorLabel();
				}
			});
			Label label = new Label(cc, SWT.WRAP);
			label.setText("Color to replace with expression data color");
			
			Label spl = new Label(buttons, SWT.NULL);
			spl.setText("Tolerance:");
			spinner = new Spinner(buttons, SWT.NULL);
			spinner.setMaximum(255);
			spinner.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setTolerance();
				}
			});
		
			return imageGroup;
		}
		
		void setTolerance() {
			getInput().setTolerance(spinner.getSelection());
			refreshImage();
		}
		
		void changeColorLabel() {
			ColorDialog cd = new ColorDialog(getShell());
			cd.setRGB(getInput().getReplaceColor());
			RGB rgb = cd.open();
			if(rgb != null) {
				getInput().setReplaceColor(rgb);
				setColorLabel();
			}
		}
		
		void setColorLabel() {
			RGB rgb = getInput().getReplaceColor();
			replaceColor = SwtUtils.changeColor(replaceColor, rgb, getDisplay());
			colorLabel.setBackground(replaceColor);
			refreshImage();
		}
		
		void refreshImage() {
			if(input == null || input.length < 1) {
				image = null;
			} else {
				Point size = imageLabel.getSize();
				ImageData imgd = null;
				if(size.x > 0 && size.y > 0) {
					int b = 8;
					size.x -= size.x > b ? b : 0; 
					size.y -= size.y > b ? b : 0; 
					imgd = getInput().getImageData(size, imageLabel.getBackground().getRGB());
				}
				image = SwtUtils.changeImage(image, imgd, getDisplay());
			}
			imageLabel.setImage(image);
		}
		
		void addImage() {
			FileDialog fd = new FileDialog(getShell());
			String fn = fd.open();
			if(fn == null) return;
			try {
				new ImageData(fn);
				addImageURL(new File(fn).toURL());
				imageList.refresh();
			} catch(Exception e) {
				MessageDialog.openError(getShell(), "Unable to open image file", e.toString());
				Engine.log.error("Unable to load image", e);
			}
		}
		
		void removeImage() {
			URL url = (URL)((IStructuredSelection)imageList.getSelection()).getFirstElement();
			removeImageURL(url);
		}
				
		public void refresh() {
			if(input == null || input.length != 1) {
				setAllEnabled(false);
				refreshImage();
			}
			else {
				setAllEnabled(true);
				URL url = getInput().getURL();
				if(url != null) imageList.setSelection(new StructuredSelection(url));
				else imageList.setSelection(new StructuredSelection(imageList.getElementAt(0)));
				aspectButton.setSelection(getInput().getMaintainAspect());
				setColorLabel();
				spinner.setSelection(getInput().getTolerance());
			}
		}
		
		public void dispose() {
			if(replaceColor != null && !replaceColor.isDisposed()) replaceColor.dispose();
			if(image != null && !image.isDisposed()) image.dispose();
			super.dispose();
		}
	}
	
	protected class ImageSample extends ConfiguredSample {
		ImageData cacheImageData;
		URL imageURL;
		RGB replaceColor = DEFAULT_TRANSPARENT;
		int tolerance; //range 0 - 255;
		boolean aspectRatio = true;
		
		public ImageSample(int idSample, String name, int dataType) {
			super(idSample, name, dataType);
		}
		
		public ImageSample(Sample s) {
			super(s.getId(), s.getName(), s.getDataType());
		}
		
		public ImageSample(Element xml) throws Exception {
			super(xml);
		}
		
		public void setURL(URL url) { 
			imageURL = url;
			cacheImageData = null;
			fireModifiedEvent();
		}
		
		public URL getURL() { 
			return imageURL == null ? imageURL = imageURLs.get(0) : imageURL; 
		}
		
		public void setReplaceColor(RGB rgb) { 
			if(rgb != null) replaceColor = rgb;
			fireModifiedEvent();
		}
		public RGB getReplaceColor() { return replaceColor; }
		public void setMaintainAspect(boolean maintain) { 
			aspectRatio = maintain;
			fireModifiedEvent();
		}
		public boolean getMaintainAspect() { return aspectRatio;}
		public void setTolerance(int tol) { 
			fireModifiedEvent();
			tolerance = tol; 
		}
		public int getTolerance() { return tolerance; }
		
		public ImageData getImageData() {
			if(imageURL == null) return null;
			if(cacheImageData == null) {
				InputStream in = getInputStream(imageURL);
				cacheImageData = new ImageData(in);
			}
			return (ImageData)cacheImageData.clone();
		}
		
		public ImageData getImageData(Point size) {
			return getImageData(size, null);
		}
		
		public ImageData getImageData(RGB replaceWith) {
			ImageData img = getImageData();
			if(img == null) return null;
			if(replaceWith != null) img = doReplaceColor(img, replaceWith);
			return img;
		}
		
		public ImageData getImageData(Point size, RGB replaceWith) {
			ImageData img = getImageData();
			if(img == null) return null;
			
			img = getImageData(replaceWith);
			
			size = getScaleSize(size);
			
			img = img.scaledTo(size.x, size.y);
			return img;
		}
		
		public Point getScaleSize(Point target) {
			if(aspectRatio) {
				ImageData img = getImageData();
				double r = (double)img.height / img.width;
				int min = Math.min(target.x, target.y);
				if(min == target.x) target.y = (int)(min * r);
				else target.x = (int)(min * r);
			}
			return target;
		}
		
		ImageData doReplaceColor(ImageData img, RGB replaceWith) {
			PaletteData pd = img.palette;
			if(pd.isDirect) 
				replaceDirect(img, getReplaceColor(), replaceWith, getTolerance());
			else 
				replaceIndexed(img, getReplaceColor(), replaceWith, getTolerance());
			return img;
		}
		
		void replaceDirect(ImageData imgd, RGB tr, RGB rp, int tol) {
			PaletteData pd = imgd.palette;
			int rpvalue = pd.getPixel(rp);
			int[] line = new int[imgd.width];
			for (int y = 0; y < imgd.height; y++) {
				imgd.getPixels(0, y, imgd.width, line, 0);
				for (int x = 0; x < line.length; x++) {
					if(compareRGB(tr, pd.getRGB(line[x]), tol)) {
						imgd.setPixel(x, y, rpvalue);
					}
				}
			};
		}

		void replaceIndexed(ImageData imgd, RGB tr, RGB rp, int tol) {
			RGB[] rgbs = imgd.palette.getRGBs();
			RGB[] newRgbs = new RGB[rgbs.length];
			for(int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				if( compareRGB(tr, rgbs[i], tol)) {
					rgb = rp;
				}
				newRgbs[i] = rgb;
			}
			imgd.palette = new PaletteData(newRgbs);
		}
		
		boolean compareRGB(RGB rgb1, RGB rgb2, int tolerance) {
			return 	rgb2.red >= rgb1.red - tolerance &&
					rgb2.red <= rgb1.red + tolerance &&
					rgb2.green >= rgb1.green - tolerance &&
					rgb2.green <= rgb1.green + tolerance &&
					rgb2.blue >= rgb1.blue - tolerance &&
					rgb2.blue <= rgb1.blue + tolerance;
		}
		
		InputStream getInputStream(URL url) {
			try {
				URLConnection con = url.openConnection();
				return con.getInputStream();
			} catch(IOException e) {
				Engine.log.error("Unable to open connection to image", e);
			}
			return null;
		}
		
		final static String XML_ATTR_ASPECT = "maintain-aspect-ratio";
		final static String XML_ATTR_TOLERANCE = "tolerance";
		final static String XML_ATTR_IMAGE = "image-url";
		final static String XML_ATTR_REPLACE = "replace-color";
		protected void saveAttributes(Element xml) {
			xml.setAttribute(XML_ATTR_ASPECT, Boolean.toString(getMaintainAspect()));
			xml.setAttribute(XML_ATTR_TOLERANCE, Integer.toString(getTolerance()));
			xml.setAttribute(XML_ATTR_IMAGE, getURL().toString());
			xml.addContent(ColorConverter.createColorElement(XML_ATTR_REPLACE, SwtUtils.rgb2color(getReplaceColor())));
		}
		protected void loadAttributes(Element xml) {
			try {
				setMaintainAspect(Boolean.parseBoolean(xml.getAttributeValue(XML_ATTR_ASPECT)));
				setTolerance(Integer.parseInt(xml.getAttributeValue(XML_ATTR_TOLERANCE)));
				setURL(new URL(xml.getAttributeValue(XML_ATTR_IMAGE)));
				setReplaceColor(SwtUtils.color2rgb(ColorConverter.parseColorElement(xml.getChild(XML_ATTR_REPLACE))));
			} catch(Exception e) {
				Engine.log.error("Unable to load plugin", e);
			}
		}
		
	}
	
	public Composite visualizeOnToolTip(Composite parent, Graphics g) { return null; }

	@Override
	public void visualizeOnDrawing(Graphics g, PaintEvent e, GC gc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visualizeOnSidePanel(Collection<Graphics> objects) {
		// TODO Auto-generated method stub
		
	}

}
