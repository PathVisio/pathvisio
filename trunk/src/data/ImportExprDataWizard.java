package data;

import gmmlVision.GmmlVision;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import preferences.GmmlPreferences;
import util.TableColumnResizer;

/**
 * This class is a {@link Wizard} that guides the user trough the process to
 * create an expression dataset from a delimited text file
 */
public class ImportExprDataWizard extends Wizard {
	ImportInformation importInformation;

	public ImportExprDataWizard() {
		importInformation = new ImportInformation();

		setWindowTitle("Create an expression dataset");
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		addPage(new FilePage());
		addPage(new HeaderPage());
		addPage(new ColumnPage());
		addPage(new ImportPage());
	}

	boolean importFinished;

	public boolean performFinish() {
		if (!importFinished) {
			ImportPage ip = (ImportPage) getPage("ImportPage");
			getContainer().showPage(ip);
			try {
				// Start import process
				getContainer().run(true, true,
						new GmmlGex.ImportRunnableWithProgress(
								importInformation,
								(ImportPage) getPage("ImportPage")));
			} catch (Exception e) {
				GmmlVision.log.error("while running expression data import process: " + e.getMessage(), e);
			} // TODO: handle exception
			ip.setTitle("Import finished");
			ip.setDescription("Press finish to return to Gmml-Visio");
			importFinished = true;
			return false;
		}
		if (importFinished
				&& getContainer().getCurrentPage().getName().equals(
						"ImportPage")) {
			return true;
		}
		return false;
	}

	public boolean performCancel() {
		return true; // Do nothing, just close wizard
	}

	/**
	 * This is the wizard page to specify the location of the text file
	 * containing expression data and the location to store the new expression
	 * dataset
	 */
	public class FilePage extends WizardPage {
		boolean txtFileComplete;

		boolean gexFileComplete;

		public FilePage() {
			super("FilePage");
			setTitle("File locations");
			setDescription("Specify the locations of the file containing the expression data "
					+ "and where to store the expression dataset");
			setPageComplete(false);
		}

		public void createControl(Composite parent) {
			final FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);

			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayout(new GridLayout(2, false));

			GridData labelGrid = new GridData(GridData.FILL_HORIZONTAL);
			labelGrid.horizontalSpan = 2;

			Label txtLabel = new Label(composite, SWT.FLAT);
			txtLabel
					.setText("Specify location of text file containing expression data");
			txtLabel.setLayoutData(labelGrid);

			final Text txtText = new Text(composite, SWT.SINGLE | SWT.BORDER);
			txtText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Button txtButton = new Button(composite, SWT.PUSH);
			txtButton.setText("Browse");

			Label gexLabel = new Label(composite, SWT.FLAT);
			gexLabel.setText("Specify location to save the expression dataset");
			gexLabel.setLayoutData(labelGrid);

			final Text gexText = new Text(composite, SWT.SINGLE | SWT.BORDER);
			gexText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Button gexButton = new Button(composite, SWT.PUSH);
			gexButton.setText("Browse");

			txtButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					fileDialog
							.setText("Select tab delimited text file containing expression data");
					fileDialog.setFilterExtensions(new String[] { "*.txt",
							"*.*" });
					fileDialog.setFilterNames(new String[] { "Text file",
							"All files" });
					fileDialog.setFilterPath(GmmlVision.getPreferences().getString(GmmlPreferences.PREF_DIR_EXPR));
					String file = fileDialog.open();
					if (file != null) {
						txtText.setText(file);
						gexText.setText(file.replace(file.substring(file
								.lastIndexOf(".")), ""));
					}
				}
			});

			gexButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					try {
						DBConnector dbcon = GmmlGex.getDBConnector();
						String dbName = dbcon.openNewDbDialog(getShell(), gexText.getText());
						if(dbName != null) gexText.setText(dbName);
						
					} catch(Exception ex) {
						MessageDialog.openError(getShell(), "Error", "Unable to open connection dialog");
						GmmlVision.log.error("", ex);
					}
				}
			});

			txtText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					setTxtFile(new File(txtText.getText()));
					setPageComplete(txtFileComplete && gexFileComplete);
				}
			});

			gexText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					setDbName(gexText.getText());
					setPageComplete(txtFileComplete && gexFileComplete);
				}
			});

			composite.pack();
			setControl(composite);
		}

		/**
		 * Stores the given {@link File} pointing to the file containing the expresssion
		 * data in text form to the {@link ImportInformation} object
		 * @param file
		 */
		private void setTxtFile(File file) {
			if (!file.exists()) {
				setErrorMessage("Specified file to import does not exist");
				txtFileComplete = false;
				return;
			}
			if (!file.canRead()) {
				setErrorMessage("Can't access specified file containing expression data");
				txtFileComplete = false;
				return;
			}
			importInformation.setTxtFile(file);
			setErrorMessage(null);
			txtFileComplete = true;
		}

		/**
		 * Sets the name of the database to save the
		 * expression database to the {@link ImportInformation} object
		 * @param file
		 */
		private void setDbName(String name) {
			importInformation.dbName = name;
			setMessage("Expression dataset location: " + name);
			gexFileComplete = true;
		}

		public IWizardPage getNextPage() {
			setPreviewTableContent(previewTable); //Content of previewtable depends on file locations
			return super.getNextPage();
		}
	}

	Table previewTable;
	
	/**
	 * This {@link WizardPage} is used to ask the user information about on which line the
	 * column headers are and on which line the data starts
	 */
	public class HeaderPage extends WizardPage {

		public HeaderPage() {
			super("HeaderPage");
			setTitle("Header information");
			setDescription("Specify the line with the column headers and from where the data starts");
			setPageComplete(true);
		}
		
		Spinner startSpinner;
		Spinner headerSpinner;
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayout(new GridLayout(2, false));

			Label headerLabel = new Label(composite, SWT.FLAT);
			headerLabel.setText("Column headers at line: ");
			headerSpinner = new Spinner(composite, SWT.BORDER);
			headerSpinner.setMinimum(1);
			headerSpinner.setSelection(importInformation.headerRow);

			Label startLabel = new Label(composite, SWT.FLAT);
			startLabel.setText("Data starts at line: ");
			startSpinner = new Spinner(composite, SWT.BORDER);
			startSpinner.setMinimum(1);
			startSpinner.setSelection(importInformation.firstDataRow);

			Group tableGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
			GridData groupGrid = new GridData(GridData.FILL_BOTH);
			groupGrid.horizontalSpan = 2;
			groupGrid.widthHint = 300;
			tableGroup.setLayoutData(groupGrid);
			tableGroup.setLayout(new FillLayout());
			tableGroup.setText("Preview of file to import");

			previewTable = new Table(tableGroup, SWT.SINGLE | SWT.BORDER);
			previewTable.setLinesVisible(true);
			previewTable.setHeaderVisible(true);
			TableColumn nrCol = new TableColumn(previewTable, SWT.LEFT);
			nrCol.setText("line");
			TableColumn txtCol = new TableColumn(previewTable, SWT.LEFT);
			txtCol.setText("data");
			nrCol.setWidth(40);
			nrCol.setResizable(false);
			previewTable.addControlListener(new TableColumnResizer(previewTable, tableGroup, new int[] {0, 100}));

			composite.pack();
			setControl(composite);
		}

		public IWizardPage getNextPage() {
			importInformation.headerRow = headerSpinner.getSelection();
			importInformation.firstDataRow = startSpinner.getSelection();
			setColumnTableContent(columnTable);
			setColumnControlsContent();
			return super.getNextPage();
		}
	}

	Table columnTable;
	List columnList;
	Combo codeCombo;
	Combo idCombo;

	/**
	 * This is the wizard page to specify column information, e.g. which
	 * are the gene id and systemcode columns
	 */
	public class ColumnPage extends WizardPage {

		public ColumnPage() {
			super("ColumnPage");
			setTitle("Column information");
			setDescription("Specify which columns contain the gene information and "
					+ "which columns should not be treated as numeric data");
			setPageComplete(true);
		}

		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayout(new GridLayout(1, false));

			Label idLabel = new Label(composite, SWT.FLAT);
			idLabel.setText("Select column with gene identifiers");
			idCombo = new Combo(composite, SWT.READ_ONLY);

			Label sysLabel = new Label(composite, SWT.FLAT);
			sysLabel.setText("Select column with Systemcode");
			codeCombo = new Combo(composite, SWT.READ_ONLY);

			Label columnLabel = new Label(composite, SWT.FLAT | SWT.WRAP);
			columnLabel
					.setText("Select the columns containing data that should NOT be treated"
							+ " as NUMERIC from the list below");

			columnList = new List(composite, SWT.BORDER | SWT.MULTI
					| SWT.V_SCROLL);
			GridData listGrid = new GridData(GridData.FILL_HORIZONTAL);
			listGrid.heightHint = 150;
			columnList.setLayoutData(listGrid);
			columnList.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					importInformation.setStringCols(columnList
							.getSelectionIndices());
				}
			});
			idCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					importInformation.idColumn = idCombo.getSelectionIndex();
				}
			});
			codeCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					importInformation.codeColumn = codeCombo
							.getSelectionIndex();
				}
			});

			Group tableGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
			GridData tableGrid = new GridData(GridData.FILL_BOTH);
			tableGrid.heightHint = 100;
			tableGroup.setLayoutData(tableGrid);
			tableGroup.setLayout(new FillLayout());
			tableGroup.setText("Preview of file to import");

			columnTable = new Table(tableGroup, SWT.SINGLE | SWT.BORDER);
			columnTable.setLinesVisible(true);
			columnTable.setHeaderVisible(true);
			columnTable.addControlListener(new TableColumnResizer(columnTable, tableGroup));

			composite.pack();
			setControl(composite);
		}
	}

	/**
	 * Sets the content of the Controls on the {@link ColumnPage}
	 */
	public void setColumnControlsContent() {
		columnList.setItems(importInformation.getColNames());
		columnList.setSelection(importInformation.getStringCols());
		idCombo.setItems(importInformation.getColNames());
		idCombo.select(importInformation.idColumn);
		codeCombo.setItems(importInformation.getColNames());
		codeCombo.select(importInformation.codeColumn);
	}

	/**
	 * Sets the content of the given preview table (containing 2 columns: linenumber and textdata)
	 * @param previewTable
	 */
	public void setPreviewTableContent(Table previewTable) {
		previewTable.removeAll();
		try {
			int n = 50; // nr of lines to include in the preview
			BufferedReader in = importInformation.getBufferedReader();
			String line;
			int i = 1;
			while ((line = in.readLine()) != null && i <= n) {
				TableItem ti = new TableItem(previewTable, SWT.NULL);
				ti.setText(0, Integer.toString(i++));
				ti.setText(1, line);
			}
		} catch (IOException e) { // TODO: handle IOException
			GmmlVision.log.error("while generating preview for importing expression data: " + e.getMessage(), e);
		}
		previewTable.pack();
	}

	/**
	 * Sets teh content of the given columnTable (previews how the data will be divided in columns)
	 * @param columnTable
	 */
	public void setColumnTableContent(Table columnTable) {
		columnTable.removeAll();
		for (TableColumn col : columnTable.getColumns())
			col.dispose();
		for (String colName : importInformation.getColNames()) {
			TableColumn tc = new TableColumn(columnTable, SWT.NONE);
			tc.setText(colName);
		}
		try {
			int n = 50; // nr of lines to include in the preview
			BufferedReader in = importInformation.getBufferedReader();
			String line;
			for (int i = 0; i < importInformation.firstDataRow - 1; i++)
				in.readLine(); // Go to line where data starts
			int j = 1;
			while ((line = in.readLine()) != null && j++ < n) {
				TableItem ti = new TableItem(columnTable, SWT.NULL);
				ti.setText(line.split(ImportInformation.DELIMITER));
			}
		} catch (IOException e) { // TODO: handle IOException
			GmmlVision.log.error("while generating preview for importing expression data: " + e.getMessage(), e);
		}
		columnTable.pack();
	}

	/**
	 * This page shows the progress and status of the import process
	 */
	public class ImportPage extends WizardPage {
		Text progressText;

		public ImportPage() {
			super("ImportPage");
			setTitle("Create expression dataset");
			setDescription("Press finish button to create the expression dataset");
			setPageComplete(true);

		}

		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayout(new FillLayout());

			progressText = new Text(composite, SWT.READ_ONLY | SWT.BORDER
					| SWT.WRAP);
			progressText.setText("Ready to import data" + Text.DELIMITER);
			progressText.append("> Using gene database: "
					+ GmmlGdb.getDbName()
					+ Text.DELIMITER);
			progressText
					.append("> If this is not the correct gene database, close this window"
							+ " and change the gene database in the menu 'data' -> 'choose gene database\n'");
			setControl(composite);
		}

		public void println(String text) {
			appendProgressText(text, true);
		}

		public void print(String text) {
			appendProgressText(text, false);
		}
		
		public void appendProgressText(final String updateText,
				final boolean newLine) {
			if (progressText != null && !progressText.isDisposed())
				progressText.getDisplay().asyncExec(new Runnable() {
					public void run() {
						progressText.append(updateText
								+ (newLine ? progressText.getLineDelimiter()
										: ""));
					}
				});
		}

		public IWizardPage getPreviousPage() {
			// User pressed back, probably to change settings and redo the
			// importing, so set importFinished to false
			importFinished = false;
			return super.getPreviousPage();
		}
	}

	/**
	 * {@link ControlAdapter} to fit the size of the table columns (for both previewTable and
	 * columnTable) to the size of its parent composite
	 */
	class TableControlAdapter extends ControlAdapter {
		Table table;

		public TableControlAdapter(Table table) {
			this.table = table;
		}

		public void controlResized(ControlEvent e) {
			TableColumn[] cols = table.getColumns();
			Rectangle area = table.getParent().getClientArea();
			Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int width = area.width - 2 * table.getBorderWidth();
			if (preferredSize.y > area.height + table.getHeaderHeight()) {
				// Subtract the scrollbar width from the total column width
				// if a vertical scrollbar will be required
				Point vBarSize = table.getVerticalBar().getSize();
				width -= vBarSize.x;
			}
			Point oldSize = table.getSize();
			if (oldSize.x > area.width) {
				// table is getting smaller so make the columns
				// smaller first and then resize the table to
				// match the client area width
				if (cols.length == 2) { // This is the previewTable
					cols[1].setWidth(width - cols[0].getWidth());
				} else { // This is the columnTable
					for (TableColumn col : cols) {
						col.setWidth(width / cols.length);
					}
				}
				table.setSize(area.width, area.height);
			} else {
				// table is getting bigger so make the table
				// bigger first and then make the columns wider
				// to match the client area width
				table.setSize(area.width, area.height);
				if (cols.length == 2) { // This is the previewTable
					cols[1].setWidth(width - cols[0].getWidth());
				} else { // This is the columnTable
					for (TableColumn col : cols) {
						col.setWidth(width / cols.length);
					}
				}
			}
		}
	}

	/**
	 * This class contains the information needed to start importing a delimited
	 * text file to an expression dataset. This information is gathered using
	 * the {@link ImportExprDataWizard}
	 */
	class ImportInformation {
		/**
		 * Points to the text file containing the expression data
		 */
		private File txtFile;
		/**
		 * Sets the text file containing the expression data
		 * @param txtFile {@link File} to set
		 */
		public void setTxtFile(File txtFile)
		{
			//Close the connection to the previous file if exist
			if(in != null) {
				try { in.close(); } catch(Exception e) { 
					GmmlVision.log.error("on closing file " + this.txtFile + ": " + e.getMessage(), e);
				}
				in = null;
			}
			this.txtFile = txtFile;
		}
		/**
		 * Get the private {@link File} txtFile
		 * @return {@link File} object pointing to the text file that contains the
		 * expression data
		 */
		public File getTxtFile() { return txtFile; } 
		
		/**
		 * The database name in which the expression data is saved
		 */
		public String dbName;

		/**
		 * linenumber (first line is 1) of the line where the data begins
		 */
		int firstDataRow;
		/**
		 * linenumber (first line is 1) of the line containing the column headers
		 */
		int headerRow;
		/**
		 * Column number (first column is 0) of the column containing the gene identifier
		 */
		int idColumn;

		/**
		 * Column number (first column is 0) of the column containing the systemcode
		 */
		int codeColumn;

		/**
		 * Delimiter used to seperate columns in the text file containing expression data
		 * TODO: let the user specify a delimiter
		 */
		static final String DELIMITER = "\t";

		/**
		 * Column numbers (first column is 0) of the columns of which the data should not be treated
		 * as numberic
		 */
		private int[] stringCols;

		/**
		 * Constructor for this class
		 * Sets the default values
		 */
		public ImportInformation() {
			// Set the defaults
			firstDataRow = 2;
			headerRow = 1;
			idColumn = 0;
			codeColumn = 1;
		}

		/**
		 * {@link BufferedReader} to the text file, maintained while the wizard is open
		 */
		BufferedReader in;

		/**
		 * Get a {@link BufferedReader} to the text file containing the expression data
		 * Creates a new one 
		 * @return
		 */
		public BufferedReader getBufferedReader() {
			try {
				if (in == null) {
					in = new BufferedReader(new FileReader(txtFile));
					in.mark(10000);
				} else {
					in.reset();
				}
			} catch (Exception e) {
			} // TODO: handle exception
			return in;
		}

		/**
		 * Sets the private property stringCols
		 * @param cols	Column numbers (start with 0) of columns containing data that
		 * should not be treated as numeric
		 */
		public void setStringCols(int[] cols) {
			stringCols = cols;
		}

		/**
		 * Sets the private property stringCols
		 * @return	Column numbers (start with 0) of columns containing data that
		 * should not be treated as numeric, or an empty String[]
		 */
		public int[] getStringCols() {
			if (stringCols == null)
				stringCols = new int[] {};
			return stringCols;
		}

		/**
		 * Checks if the column for the given column index is marked as 'string column' and
		 * should not be treated as numeric
		 * @param colIndex	the index of the column to check (start with 0)
		 * @return true if the column is marked as 'string column', false if not
		 */
		public boolean isStringCol(int colIndex) {
			if (stringCols == null)
				return false;
			for (int col : stringCols)
				if (col == colIndex)
					return true;
			return false;
		}

		/**
		 * Reads the column names from the text file containing the expression data at the
		 * header row specified by the user
		 * @return the column names
		 */
		public String[] getColNames() {
			try {
				BufferedReader in = getBufferedReader();
				int i = 0;
				while (i < headerRow - 1 && in.readLine() != null)
					i++; // Go to headerline
				return in.readLine().split(ImportInformation.DELIMITER);
			} catch (IOException e) { // TODO: handle IOException
				GmmlVision.log.error("Unable to get column names for importing expression data: " + e.getMessage(), e);
				return new String[] {};
			}
		}

	}
}