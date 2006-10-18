package R.wizard;

import gmmlVision.GmmlVision;
import gmmlVision.sidepanels.TabbedSidePanel;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import util.SwtUtils.SimpleRunnableWithProgress;
import util.tableviewer.PathwayTable;
import R.RCommands.RException;
import R.RDataIn.ResultSet;


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
		ps.finishText.setText("");
		boolean ok = false;
		try {
			SimpleRunnableWithProgress srwp = 
				new SimpleRunnableWithProgress(ps.getClass(), "performFinish", new Class[] { });
			srwp.setArgs(new Object[] { });
			srwp.setInstance(ps);
			SimpleRunnableWithProgress.setTotalWork(IProgressMonitor.UNKNOWN);
			getContainer().run(true, true, srwp);
			
			//Not possible to cancel the evaluation of an R command for now, so disable cancel
			((RWizardDialog)getContainer()).getButton(WizardDialog.CANCEL).setEnabled(false);
			getContainer().updateButtons();
			
			//Load resultset and display in sidepanel
			ResultSet rs = new ResultSet(ps.getResultVar());
			
			TabbedSidePanel sp = GmmlVision.getWindow().getSidePanel();
			PathwayTable table = new PathwayTable(sp.getTabFolder(), SWT.NULL);
			table.setTableData(rs);
			
			String nm = getTabItemName(rs.getName(), sp);
			sp.addTab(table, nm, true);
			sp.selectTab(nm);
			ok = true;
			
		} catch(InvocationTargetException e) {
			MessageDialog.openError(getShell(), "Error while applying function", e.getCause().getMessage());
			GmmlVision.log.error("Unable to perform pathway statistics", e);
			ps.showConfig();
		} catch(RException re) {
			MessageDialog.openError(getShell(), "Error while loading results", re.getMessage());
			GmmlVision.log.error("Unable to perform pathway statistics", re);
		} catch(InterruptedException ie) {
			return true; //Closes the wizard (needed because R process is killed (at least in linux)
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(!ok) ps.showConfig();
		return ok;
	}
	
	private String getTabItemName(String prefName, TabbedSidePanel tsp) {
		HashMap<String, CTabItem> tabItems = tsp.getTabItemHash();
		if(!tabItems.containsKey(prefName)) return prefName;
		SortedSet<String> matches = new TreeSet<String>();
		for(CTabItem ti : tabItems.values())
			if(ti.getText().startsWith(prefName)) matches.add(ti.getText());
		String last = matches.last();
		int replaceFrom = last.lastIndexOf("(");
		if(replaceFrom < 0) return last + " (1)";
		
		int num = Integer.parseInt(last.substring(replaceFrom + 1, replaceFrom + 2));
		return last.substring(0, replaceFrom) + " (" + ++num + ")";
	}
	
	public static class RWizardDialog extends WizardDialog {
		public RWizardDialog(Shell parent, IWizard wizard) {
			super(parent, wizard);
		}
		
		public Button getButton(int id) {
			return super.getButton(id);
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
