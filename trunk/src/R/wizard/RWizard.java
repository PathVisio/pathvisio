package R.wizard;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;


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
			ps.performFinish();
		} catch(Exception e) {
			MessageDialog.openError(getShell(), "Error while applying function", e.getMessage());
			return false;
		}
		return true;
	}
	
	public static class RWizardDialog extends WizardDialog {
		public RWizardDialog(Shell parent, IWizard wizard) {
			super(parent, wizard);
		}
		
		protected void nextPressed() {
			System.out.println(getCurrentPage());
			if(getCurrentPage() instanceof PageData) {
				boolean ok = ((PageData)getCurrentPage()).performFinish();
				if(!ok) return;
			}
			super.nextPressed();
		}
	}
}
