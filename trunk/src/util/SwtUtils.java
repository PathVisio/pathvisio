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
	
	public static Font changeFont(Font fOld, FontData fd, Display display)
	{
		if(fOld != null && !fOld.isDisposed())
		{
			fOld.dispose();
			fOld = null;
		}
		return new Font(display, fd);
	}
	
	public static class SimpleRunnableWithProgress implements IRunnableWithProgress {
		Method doMethod;
		private Object[] args;
		static IProgressMonitor monitor;
		Object instance;
		
		static String taskName = "";
		static int totalWork = 1000;
				
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
		
		public SimpleRunnableWithProgress(Class fromClass, String method, Class[] parameters) {
			this(fromClass, method, parameters, null, null);
		}
		
		public void setArgs(Object[] args) { this.args = args; }
		
		public void setInstance(Object obj) { instance = obj; } 
		
		public static IProgressMonitor getMonitor() { return monitor; }
		
		public static int getTotalWork() { return totalWork; }
		
		public static boolean isCancelled() {
			if(monitor != null) return monitor.isCanceled();
			else return false; //Not canceled if no monitor
		}
		public static void setMonitorInfo(String tn, int tw) {
			taskName = tn;
			totalWork = tw;
		}
		
		public static void setTotalWork(int tw) {
			totalWork = tw;
		}
		
		public static void setTaskName(String tn) {
			taskName = tn;
		}
		
		public void run(IProgressMonitor monitor) throws InterruptedException,
			InvocationTargetException {
			SimpleRunnableWithProgress.monitor = monitor;
			
			if(args == null || doMethod == null) {
				InterruptedException ex = new InterruptedException("missing method or arguments, see error log for details");
				GmmlVision.log.error("unable to invoke " + doMethod, ex);
				throw ex;
			}
					
			monitor.beginTask(taskName, totalWork);
			try {
				doMethod.invoke(instance, args);
			} catch (IllegalAccessException e) {
				throw new InvocationTargetException(e, "Unable to invoke method " + doMethod);
			} catch (IllegalArgumentException e) {
				throw new InvocationTargetException(e, "Unable to invoke method " + doMethod);
			}
			monitor.done();
			SimpleRunnableWithProgress.monitor = null;
		}
		
		public static void monitorWorked(final int w) {
			GmmlVision.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					monitor.worked(w);
				}
			});
		}
		
		public static void monitorSetTaskName(final String taskName) {
			GmmlVision.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					monitor.setTaskName(taskName);
				}
			});
		}
		
		public void openMessageDialog(final String title, final String msg) {
			GmmlVision.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(GmmlVision.getWindow().getShell(), title, msg);
				}
			});
		}
	}
}
