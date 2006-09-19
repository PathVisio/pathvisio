package R;

import gmmlVision.GmmlVision;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class RWindow extends ApplicationWindow {
	public static String stdPwObjName = "pathways";
	public static String stdExprObjName = "data";
	
	RData rData;
	
	Button doPws;
	Button doExpr;
	Button doCrit;
	
	Text pwDir;
	Text exportFile;
	Text pwObj;
	Text exprObj;

	Button exportBrowse;
	Button pwBrowse;
	Button doExport;
	
	public RWindow(Shell shell) {
		super(shell);
		rData = new RData();
	}
	
	public RWindow(Shell shell, RData rData) {
		super(shell);
		this.rData = rData;
	}
	
	public boolean close() {
//		Don't shutdown R thread for now (problematic...)
//		RController.endR();
		return super.close();
	}
	
	private void setListeners() {
		doPws.addSelectionListener(doButtonAdapter);
		doExpr.addSelectionListener(doButtonAdapter);
		
		pwDir.addModifyListener(textListener);
		pwObj.addModifyListener(textListener);
		exprObj.addModifyListener(textListener);
		exportFile.addModifyListener(textListener);
		
		pwBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog fd = new DirectoryDialog(getShell());
				fd.setFilterPath(pwDir.getText());
				String dir = fd.open();
				if(dir != null) pwDir.setText(dir);
			}
		});

		exportBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(getShell());
				fd.setFilterExtensions(new String[] { "RData" });
				String file = fd.open();
				if(file != null) exportFile.setText(file);
			}
		});
		
		doExport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					rData.doExport(RController.getRengine());
				} catch(Exception re) {
					MessageDialog.openError(getShell(), "Error when exporting data", re.getMessage());
					GmmlVision.log.error("when exporting data to R", re);
				}
			}
		});
	}
	
	ModifyListener textListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if		(e.widget == pwDir) 
				rData.pwDir = pwDir.getText();
			else if	(e.widget == exportFile)
				rData.exportFile = exportFile.getText();
			else if (e.widget == pwObj)
				rData.pwsName = pwObj.getText();
			else if (e.widget == exprObj)
				rData.dsName = exprObj.getText();
		}
	};
		
	SelectionAdapter doButtonAdapter = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if		(e.widget == doPws) rData.exportPws = doPws.getSelection();
			else if	(e.widget == doExpr) rData.exportData = doExpr.getSelection();
			else if	(e.widget == doCrit) rData.incCrit = doCrit.getSelection();
		}
	};
	
		
	public Composite createContents(Composite parent) {
		Shell shell = parent.getShell();
		shell.setSize(400, 400);
		
		Composite content = new Composite(parent, SWT.NULL);
		content.setLayout(new FillLayout());
		
		TabFolder tf = new TabFolder(content, SWT.NULL);
		addExportTab(tf);
		
		setListeners();
		setInitials();
		return content;
	}
	
	private void setInitials() {
		exprObj.setText(stdExprObjName);
		pwObj.setText(stdPwObjName);
		pwDir.setText(GmmlVision.getPreferences().getString("directories.gmmlFiles"));
	}
	
	private void addExportTab(TabFolder tabFolder) {
		Composite content = new Composite(tabFolder, SWT.NULL);
		content.setLayout(new GridLayout());
		
		Composite settings = new Composite(content, SWT.NULL);
		settings.setLayoutData(new GridData(GridData.FILL_BOTH));
		settings.setLayout(new GridLayout(2, false));
		
		GridData groupGrid = new GridData(GridData.FILL_BOTH);
		GridData checkGrid = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		GridData span2Cols = new GridData(GridData.FILL_HORIZONTAL);
		span2Cols.horizontalSpan = 2;
		
		doPws = new Button(settings, SWT.CHECK);	
		doPws.setLayoutData(checkGrid);
		Group pwsGroup = new Group(settings, SWT.NONE);
		pwsGroup.setText("Export pathways");
		pwsGroup.setLayoutData(groupGrid);
		pwsGroup.setLayout(new GridLayout(3, false));
		{
			Label ol = new Label(pwsGroup, SWT.CENTER);
			ol.setText("R object name:");
			pwObj = new Text(pwsGroup, SWT.SINGLE | SWT.BORDER);
			pwObj.setLayoutData(span2Cols);
			
			Label dl = new Label(pwsGroup, SWT.CENTER);
			dl.setText("Directory");
			pwDir = new Text(pwsGroup, SWT.BORDER | SWT.SINGLE);
			pwDir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			pwBrowse = new Button(pwsGroup, SWT.PUSH);
			pwBrowse.setText("...");
		}
		
		doExpr = new Button(settings, SWT.CHECK);
		doExpr.setLayoutData(checkGrid);
		Group dataGroup = new Group(settings, SWT.NONE);
		dataGroup.setLayout(new GridLayout(3, false));
		dataGroup.setLayoutData(groupGrid);
		dataGroup.setText("Export expression data");
		{
			Label ol = new Label(dataGroup, SWT.CENTER);
			ol.setText("R object name:");
			exprObj = new Text(dataGroup, SWT.SINGLE | SWT.BORDER);
			exprObj.setLayoutData(span2Cols);
			
			Label cl = new Label(dataGroup, SWT.LEFT);
			cl.setText("Include criteria:");
			doCrit = new Button(dataGroup, SWT.CHECK);
			doCrit.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, 
					false, false, 2, 0));
		}
				
		Composite export = new Composite(content, SWT.NULL);
		export.setLayout(new GridLayout(4, false));
		export.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label exportLabel = new Label(export, SWT.CENTER);
		exportLabel.setText("Export to file");
		exportFile = new Text(export, SWT.SINGLE | SWT.BORDER);
		exportFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		exportBrowse = new Button(export, SWT.PUSH);
		exportBrowse.setText("Browse");
		doExport = new Button(export, SWT.PUSH);
		doExport.setText("Export");
		
		TabItem ti = new TabItem(tabFolder, SWT.NULL);
		ti.setText("Export to R");
		ti.setControl(content);
	}
}
