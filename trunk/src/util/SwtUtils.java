package util;

import gmmlVision.GmmlVision;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class SwtUtils {

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
			fOld = null;
		}
		return new Font(display, fd);
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
		
		static String taskName = "";		//Taskname to display in the progress monitor
		static int totalWork = 1000;		//Total work to be performed
				
		/**
		 * Constructor for this class<BR>
		 * Sets the signature of the method to be called and its argument values
		 * @param fromClass	the Class to which the method belongs that has to be called
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
		 * @return
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
		 * @return
		 */
		public static void setTotalWork(int tw) {
			totalWork = tw;
		}
		
		/**
		 * Set the task name that is displayed on the progress monitor
		 * @return
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
				GmmlVision.log.error("unable to invoke " + doMethod, ex);
				throw ex;
			}
					
			monitor.beginTask(taskName, totalWork);

			runException = null;
			if(runAsSyncExec) {//Invoke in syncExec, method may access widgets from this thread
				GmmlVision.getWindow().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						runException = doInvoke();
					}
				});
			} else {
				runException = doInvoke();
			}
			
			monitor.done();
			SimpleRunnableWithProgress.monitor = null;
			
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
			GmmlVision.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
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
			GmmlVision.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
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
			GmmlVision.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(GmmlVision.getWindow().getShell(), title, msg);
				}
			});
		}
	}
}
