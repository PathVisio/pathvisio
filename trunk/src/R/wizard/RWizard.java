package R.wizard;

import java.lang.reflect.InvocationTargetException;

import gmmlVision.GmmlVision;
import gmmlVision.sidepanels.TabbedSidePanel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import R.RCommands.RException;
import R.RDataIn.ResultSet;

import data.GmmlGex;
import data.ImportExprDataWizard.ImportPage;
import util.SwtUtils.SimpleRunnableWithProgress;
import util.tableviewer.PathwayTable;


public class RWizard extends Wizard {
	
	
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
		try {
			SimpleRunnableWithProgress srwp = 
				new SimpleRunnableWithProgress(ps.getClass(), "performFinish", new Class[] { });
			srwp.setArgs(new Object[] { });
			srwp.setInstance(ps);
			SimpleRunnableWithProgress.setTotalWork(IProgressMonitor.UNKNOWN);
			getContainer().run(true, true, srwp);
			
			//Load resultset and display in sidepanel
			ResultSet rs = new ResultSet(ps.getResultVar());
			
			TabbedSidePanel sp = GmmlVision.getWindow().getSidePanel();
			PathwayTable table = new PathwayTable(sp.getTabFolder(), SWT.NULL);
			table.setTableData(rs);
			sp.addTab(table, rs.getName(), true);
			sp.selectTab(rs.getName());
			
		} catch(InvocationTargetException e) {
			MessageDialog.openError(getShell(), "Error while applying function", e.getCause().getMessage());
			GmmlVision.log.error("Unable to perform pathway statistics", e);
			return false;
		} catch(RException re) {
			MessageDialog.openError(getShell(), "Error while loading results", re.getMessage());
			GmmlVision.log.error("Unable to perform pathway statistics", re);
			return false;
		} catch(InterruptedException ie) {
			return false;
		}
		return true;
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
