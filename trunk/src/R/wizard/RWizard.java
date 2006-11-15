package R.wizard;

import gmmlVision.GmmlVision;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import util.SwtUtils.SimpleRunnableWithProgress;
import R.RDataIn;
import R.RCommands.RException;
import R.RCommands.RObjectContainer;


public class RWizard extends Wizard {
	public static RObjectContainer usedRObjects;
	
	public RWizard() {
		super();
		
		setWindowTitle("Create an expression dataset");
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		addPage(new PageData());
		addPage(new PageStats());
	}
	
	public boolean performFinish() {		
		PageStats ps = (PageStats)getPage("PageStats");
		ps.finishText.setText("");
		boolean ok = false;
		try {						
			SimpleRunnableWithProgress srwp = 
				new SimpleRunnableWithProgress(ps.getClass(), "performFinish", new Class[] { });
			srwp.setArgs(new Object[] { });
			srwp.setInstance(ps);
			SimpleRunnableWithProgress.setTotalWork(IProgressMonitor.UNKNOWN);
			getContainer().run(true, true, srwp);
					
			RDataIn.displayResults(RDataIn.getResultSets(ps.getResultVar()), ps.function);
			ok = true;
			
		} catch(InvocationTargetException e) {
			if(e.getCause() instanceof InterruptedException) return true;
			MessageDialog.openError(getShell(), "Error while applying function", e.getCause().getMessage());
			GmmlVision.log.error("Unable to perform pathway statistics", e);
		} catch(RException re) {
			MessageDialog.openError(getShell(), "Error while loading results", re.getMessage());
			GmmlVision.log.error("Unable to perform pathway statistics", re);
		} catch(InterruptedException ie) {
			return true; //Closes the wizard (needed because R process is killed (at least in linux)
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(!ok) ps.showConfig();
		}
		return ok;
	}
		
	public static class RWizardDialog extends WizardDialog {
		public RWizardDialog(Shell parent, IWizard wizard) {
			super(parent, wizard);
		}
			
		protected void nextPressed() {
			IWizardPage page = getCurrentPage();
			if		(page instanceof PageData) {
				boolean ok = ((PageData)page).performFinish();
				if(!ok) return;
				((PageStats)getWizard().getNextPage(page)).init();
			}
			super.nextPressed();
		}
	}
}
