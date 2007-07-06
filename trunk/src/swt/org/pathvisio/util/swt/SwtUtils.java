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
package org.pathvisio.util.swt;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.Engine;
import org.pathvisio.gui.swt.SwtEngine;
import org.pathvisio.model.Pathway;
import org.pathvisio.view.VPathway;

public class SwtUtils {

	public static GridData getColorLabelGrid() {
		GridData colorLabelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		colorLabelGrid.widthHint = colorLabelGrid.heightHint = 15;
		return colorLabelGrid;
	}
	
	/**
	 * Change the given {@link Color}; this method disposes the old color for you
	 * @param cOld	the old {@link Color}
	 * @param rgbNew	the {@link RGB} to construct the new color
	 * @param display	the display to assign the color to
	 * @return	a brand new {@link Color}
	 */
	public static Color changeColor(Color cOld, RGB rgbNew, Display display)
	{
		if(cOld != null && !cOld.isDisposed())
		{
			cOld.dispose();
			cOld = null;
		}
		if(rgbNew == null) rgbNew = new RGB(0,0,0);
		return new Color(display, rgbNew);
	}
	
	public static RGB color2rgb(java.awt.Color c) {
		if(c == null) c = java.awt.Color.BLACK;
		return new RGB(c.getRed(), c.getGreen(), c.getBlue());
	}
	
	public static java.awt.Color rgb2color(RGB rgb) {
		return new java.awt.Color(rgb.red, rgb.green, rgb.blue);
	}
	
	/**
	 * Change the given {@link Color}; this method disposes the old color for you
	 * @param cOld	the old {@link Color}
	 * @param rgbNew	the {@link Pathway.Color} to construct the new color
	 * @param display	the display to assign the color to
	 * @return	a brand new {@link Color}
	 */
	public static Color changeColor(Color cOld, java.awt.Color rgbNew, Display display)
	{
		if(cOld != null && !cOld.isDisposed())
		{
			cOld.dispose();
			cOld = null;
		}
		if(rgbNew == null) rgbNew = new java.awt.Color(0,0,0);
		return new Color(display, color2rgb(rgbNew));
	}
	
	/**
	 * Change the given {@link Font}; this method disposes the old font for you
	 * @param fOld	the old {@link Font}
	 * @param fd	the {@link FontData} to construct the font with
	 * @param display	the display to assign the font to
	 * @return	a brand new {@link Font}
	 */
	public static Font changeFont(Font fOld, FontData fd, Display display)
	{
		if(fOld != null && !fOld.isDisposed())
		{
			fOld.dispose();
		}
		return fd != null ? new Font(display, fd) : null;
	}
	
	public static Image changeImage(Image iOld, ImageData iNew, Display display)
	{
		if(iOld != null && !iOld.isDisposed())
		{
			iOld.dispose();
		}
		return iNew != null ? new Image(display, iNew) : null;
	}
	
	  public static ImageData convertImageToSWT(BufferedImage bufferedImage) {
		    if (bufferedImage.getColorModel() instanceof DirectColorModel) {
		      DirectColorModel colorModel = (DirectColorModel) bufferedImage
		          .getColorModel();
		      PaletteData palette = new PaletteData(colorModel.getRedMask(),
		          colorModel.getGreenMask(), colorModel.getBlueMask());
		      ImageData data = new ImageData(bufferedImage.getWidth(),
		          bufferedImage.getHeight(), colorModel.getPixelSize(),
		          palette);
		      WritableRaster raster = bufferedImage.getRaster();
		      int[] pixelArray = new int[4];
		      for (int y = 0; y < data.height; y++) {
		        for (int x = 0; x < data.width; x++) {
		          raster.getPixel(x, y, pixelArray);
		          int pixel = palette.getPixel(new RGB(pixelArray[0],
		              pixelArray[1], pixelArray[2]));
		          data.setPixel(x, y, pixel);
		        }
		      }
		      return data;
		    } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
		      IndexColorModel colorModel = (IndexColorModel) bufferedImage
		          .getColorModel();
		      int size = colorModel.getMapSize();
		      byte[] reds = new byte[size];
		      byte[] greens = new byte[size];
		      byte[] blues = new byte[size];
		      colorModel.getReds(reds);
		      colorModel.getGreens(greens);
		      colorModel.getBlues(blues);
		      RGB[] rgbs = new RGB[size];
		      for (int i = 0; i < rgbs.length; i++) {
		        rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
		            blues[i] & 0xFF);
		      }
		      PaletteData palette = new PaletteData(rgbs);
		      ImageData data = new ImageData(bufferedImage.getWidth(),
		          bufferedImage.getHeight(), colorModel.getPixelSize(),
		          palette);
		      data.transparentPixel = colorModel.getTransparentPixel();
		      WritableRaster raster = bufferedImage.getRaster();
		      int[] pixelArray = new int[1];
		      for (int y = 0; y < data.height; y++) {
		        for (int x = 0; x < data.width; x++) {
		          raster.getPixel(x, y, pixelArray);
		          data.setPixel(x, y, pixelArray[0]);
		        }
		      }
		      return data;
		    }
		    return null;
		  }
	  
	public static void setCompositeAndChildrenEnabled(Composite comp, boolean enable) {
		comp.setEnabled(enable);
		for(Control c : comp.getChildren()) {
			c.setEnabled(enable);
			if(c instanceof Composite)
				setCompositeAndChildrenEnabled((Composite) c, enable);
		}
	}
	
	public static void setCompositeAndChildrenBackground(Composite comp, Color color) {
		comp.setBackground(color);
		for(Control c : comp.getChildren()) {
			c.setBackground(color);
			if(c instanceof Composite)
				setCompositeAndChildrenBackground((Composite) c, color);
		}
	}
	
	static int[] incrs;
	static int ii;
	static int pixratio;
	public static Font adjustFontSize(Font f, Point toFit, String text, GC gc, Display display) {
		VPathway d = Engine.getActiveVPathway();
		pixratio = (int)Math.ceil(3 * (d == null ? 1 : d.getZoomFactor()));
		ii = 3;
		incrs = new int[3];
//		System.err.println(">>>>>>>>>>>> Starting adjust <<<<<<<<<<<<,");
//		System.err.println("INITIAL: "+ f.getFontData()[0].getHeight());
		f = setFontSize(f.getFontData()[0].getHeight(), f, gc, display);
		return findFontSize(f, toFit, text, gc, display);		
	}
	
	static int getIncrement(Point toFit, String text, GC gc) {	
		int borderX = 3;
		int borderY = 0;
		
		Point size = gc.textExtent(text);
		
		int dx = size.x - toFit.x + borderX;
		int dy = size.y - toFit.y + borderY;
		if(dx == 0 && dy == 0) return 0; //rare case
		return add(pix2point(-Math.max(dx, dy)));
	}
	
	static Font findFontSize(Font f, Point toFit, String text, GC gc, Display display) {
		int incr = getIncrement(toFit, text, gc);
//		System.err.println("incr: " + incr);
		if(incr != 0 && checkIncrs()) {
			int size = f.getFontData()[0].getHeight() + incr;
//			System.err.println("Size: " + size);
			if(size < 0) {
				pixratio++;
//				System.err.println("Increasing pixratio: " + pixratio);
			} else {
				f = setFontSize(size, f, gc,display);
			}
			f = findFontSize(f, toFit, text, gc, display);
		}
		return f;
	}
	
	static boolean checkIncrs() {
		//System.err.println(incrs[0] + " : " + incrs[1] + " : " + incrs[2]);
		return !(incrs[0] == incrs[2]);
	}
	
	static int add(int incr) {
		incrs[0] = incrs[1];
		incrs[1] = incrs[2];
		incrs[2] = incr;
		return incr;
	}
	
	static int pix2point(int pix) { 
		//System.err.println("pix: " + pix);
		//System.err.println("point: " + (double)pix/pixratio);
		return pix / pixratio; 
	}
	
	public static FontData awtFont2FontData(java.awt.Font f) {
		int style = SWT.NORMAL;
		if(f.isBold()) style |= SWT.BOLD;
		if(f.isItalic()) style |= SWT.ITALIC;
		return new FontData(f.getName(), f.getSize(), style);
	}
	
	public static java.awt.Font fontData2awtFont(FontData fd) {
		int style = java.awt.Font.PLAIN;
		if((fd.getStyle() & SWT.BOLD) != 0) style |= java.awt.Font.BOLD;
		if((fd.getStyle() & SWT.ITALIC) != 0) style |= java.awt.Font.ITALIC;
		return new java.awt.Font(fd.getName(), fd.getHeight(), style);		
	}
	
	static Font setFontSize(int size, Font f, GC gc, Display display) {
		FontData fd = f.getFontData()[0];
		fd.setHeight(size);
		f = changeFont(f, fd, display);
		gc.setFont(f);
		return f;
	}
	
	public static int getAverageCharWidth(Display d) {
		GC gc = new GC(d);
		int w = gc.getFontMetrics().getAverageCharWidth();
		gc.dispose();
		return w;
	}
	
	/**
	 * Rotates the {@link GC} around the objects center
	 * @param gc	the {@link GC} to rotate
	 * @param tr	a {@link Transform} that can be used for rotation
	 * @param rotation The rotation in degrees
	 * @param x The x-coordinate of the rotation center
	 * @param y The y-coordinate of the rotation center
	 */
	public static void rotateGC(GC gc, Transform tr, float rotation, int x, int y) {
		tr.translate(x, y);
		tr.rotate(rotation);	
		tr.translate(-x, -y);
		gc.setTransform(tr);
	}
	
	public static class FileInputDialog extends InputDialog {
		FileDialog fd;
		public FileInputDialog(Shell parentShell, String dialogTitle, 
				String dialogMessage, String initialValue, 
				IInputValidator validator, FileDialog fileDialog) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator); 
			fd = fileDialog;
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			Button browse = new Button(parent, SWT.PUSH);
			browse.setText("Browse");
			browse.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String fn = fd.open();
					if(fn != null) getText().setText(fn);
				}
			});
			return composite;
		}
	}
	
	/**
	 * This class is a re-usable implementation of {@link IRunnableWithProgress} and can invoke
	 * any given {@link Method} that needs to be runned in a seperate thread using a progress monitor
	 * @author thomas
	 *
	 */
	public static class SimpleRunnableWithProgress implements IRunnableWithProgress {
		Method doMethod; 					//The method to perform
		private Object[] args;				//The arguments to pass to the method
		Object instance;					//The instance of the class that contains the method to be run
		boolean runAsSyncExec;
		
		volatile static IProgressMonitor monitor;	//The progress monitor
		
		static final String INIT_TASKNAME = "";
		static final int INIT_TOTALWORK = 1000;
		static String taskName = INIT_TASKNAME;		//Taskname to display in the progress monitor
		static int totalWork = INIT_TOTALWORK;		//Total work to be performed
				
		/**
		 * Constructor for this class<BR>
		 * Sets the signature of the method to be called and its argument values
		 * @param fromClass the Class to which the method belongs that
		 * has to be called. you can get this with instance.getClass()
		 * or StaticClass.class
		 * @param method	the method to be called
		 * @param parameters	the classes of the method's arguments
		 * @param args	the argument values to pass to the method
		 * @param instance	an instance of the class to which the method belongs (null for static methods)
		 */
		public SimpleRunnableWithProgress(Class fromClass, String method, 
				Class[] parameters,	Object[] args, Object instance) {
			super();
			this.args = args;
			this.instance = instance;
			try {
				doMethod = fromClass.getMethod(method, parameters);
			} catch(NoSuchMethodException e) {
				openMessageDialog("Error: method not found", e.getMessage());
			}
		}
		
		/**
		 * Constructor for this class<BR>
		 * Sets the signature of the method to be called
		 * @param fromClass	the Class to which the method belongs that has to be called
		 * @param method	the method to be called
		 * @param parameters	the classes of the method's arguments
		 */
		public SimpleRunnableWithProgress(Class fromClass, String method, Class[] parameters) {
			this(fromClass, method, parameters, null, null);
		}
		
		/**
		 * Set the values of the to be called method's arguments
		 * @param args
		 */
		public void setArgs(Object[] args) { this.args = args; }
		
		/**
		 * Set an instance of the to be called method's class
		 * @param obj
		 */
		public void setInstance(Object obj) { instance = obj; } 
		
		/**
		 * Get the progress monitor currently used
		 * @return the currently used progress monitor, or null if none is used
		 */
		public static IProgressMonitor getMonitor() { return monitor; }
		
		/**
		 * Get the total work to be performed
		 */
		public static int getTotalWork() { return totalWork; }
		
		/**
		 * Returns whether cancelation of current operation has been requested
		 * @return true if the monitor is cancelled, false if running or null
		 */
		public static boolean isCancelled() {
			if(monitor != null) return monitor.isCanceled();
			else return false; //Not canceled if no monitor
		}
		
		/**
		 * Set the monitor information (before starting the process)
		 * @param tn	the taskname to be displayed (may be changed while running)
		 * @param tw	the total work to be performed
		 */
		public static void setMonitorInfo(String tn, int tw) {
			taskName = tn;
			totalWork = tw;
		}
		
		/**
		 * Set the total work to be performed
		 */
		public static void setTotalWork(int tw) {
			totalWork = tw;
		}
		
		/**
		 * Set the task name that is displayed on the progress monitor
		 */
		public static void setTaskName(String tn) {
			taskName = tn;
		}
		
		public void setRunAsSyncExec(boolean useSyncExec) {
			runAsSyncExec = useSyncExec;
		}
		
		Throwable runException;
		public void run(IProgressMonitor monitor) throws InterruptedException,
			InvocationTargetException {
			
			SimpleRunnableWithProgress.monitor = monitor;
			
			if(args == null || doMethod == null) {
				InterruptedException ex = new InterruptedException("missing method or arguments, see error log for details");
				Engine.log.error("unable to invoke " + doMethod, ex);
				throw ex;
			}
					
			monitor.beginTask(taskName, totalWork);

			runException = null;
			if(runAsSyncExec) {//Invoke in syncExec, method may access widgets from this thread
				SwtEngine.getWindow().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						runException = doInvoke();
					}
				});
			} else {
				runException = doInvoke();
			}
			
			monitor.done();
			resetMonitor();
			
			if(runException != null) {
				if		(runException instanceof IllegalAccessException)
					throw new InvocationTargetException(runException, "Unable to invoke method " + doMethod);
				else if	(runException instanceof IllegalArgumentException)
					throw new InvocationTargetException(runException, "Unable to invoke method " + doMethod);
				else if (runException instanceof InvocationTargetException)
					throw (InvocationTargetException)runException;
				else
					throw new InvocationTargetException(runException);
			}
		}
		
		void resetMonitor() {
			monitor = null;
			totalWork = INIT_TOTALWORK;
			taskName = INIT_TASKNAME;
		}
		
		private Throwable doInvoke() {
			try { doMethod.invoke(instance, args); } catch(Throwable t) { return t; }
			return null;
		}

		/**
		 * Notify the {@link IProgressMonitor} that given number of work has been performed<BR>
		 * Equivalent to calling {@link IProgressMonitor#worked(int)} from an {@link Display#asyncExec(Runnable)}
		 * @see IProgressMonitor#worked(int)
		 * @param w
		 */
		public static void monitorWorked(final int w) {
			SwtEngine.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if(monitor != null) monitor.worked(w);
				}
			});
		}
		
		/**
		 * Sets the task name of the progress monitor to the given value
		 * * Equivalent to calling {@link IProgressMonitor#setTaskName(String)} from an {@link Display#asyncExec(Runnable)}
		 * @see IProgressMonitor#setTaskName(String)
		 */
		public static void monitorSetTaskName(final String taskName) {
			SwtEngine.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if(monitor != null) monitor.setTaskName(taskName);
				}
			});
		}
		
		/**
		 * Opens a message dialog from withing a {@link Display#asyncExec(Runnable)}
		 * @param title	the title of the dialog
		 * @param msg	the message to be displayed on the dialog
		 * @see MessageDialog#openInformation(org.eclipse.swt.widgets.Shell, String, String)
		 */
		public void openMessageDialog(final String title, final String msg) {
			SwtEngine.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(SwtEngine.getWindow().getShell(), title, msg);
				}
			});
		}
	}
}
