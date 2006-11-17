package visualization.colorset;

import java.sql.Types;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import data.GmmlGex;

public class CriterionComposite extends Composite {
	ErrorArea errorArea;
	Criterion criterion;
	List symbolList;
	Text exprText;
	
	public CriterionComposite(Composite parent, Criterion criterion) {
		super(parent, SWT.NULL);
		this.criterion = criterion;
		createContents();
	}
	
	public void setAvailableSymbols(String[] symbols) {
		symbolList.setItems(symbols);
	}
	
	public void fetchSymbolsFromGex() {
		if(GmmlGex.isConnected()) {
			java.util.List<String> numSmp = GmmlGex.getSampleNames(Types.REAL);
			symbolList.setItems(numSmp.toArray(new String[numSmp.size()]));	
		} else {
			symbolList.setItems(new String[] {});
		}
	}
	
	void setExpression(String expression) {
		if(criterion != null) {
			criterion.setExpression(expression, symbolList.getItems());
			Exception e = criterion.getParseException();
			if(e == null) {
				errorArea.setErrorMessage(null);
			} else if(expression.equals("")) {
				errorArea.setWarningMessage("Please specify a boolean expression");
			} else {
				errorArea.setErrorMessage(
						"Invalid boolean expression: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public void setInput(Criterion input) {
		criterion = input;
		refresh();
	}
	
	public Criterion getCriterion() { return criterion; }
	
	public void refresh() {
		if(criterion == null) exprText.setText("");
		else exprText.setText(criterion.getExpression());
	}
	
	protected void createContents() {
		setLayout(new GridLayout());
		
		errorArea = new ErrorArea(this, SWT.NULL);
		
		Group criterionGroup = new Group(this, SWT.SHADOW_IN);
		criterionGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
	    criterionGroup.setLayout(new GridLayout(2, false));
	    
	    Label expressionLabel = new Label(criterionGroup, SWT.CENTER);
	    expressionLabel.setText("Boolean expression:");
	    exprText = new Text(criterionGroup, SWT.SINGLE | SWT.BORDER);
	    exprText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	    Label opsLabel = new Label(criterionGroup, SWT.CENTER);
	    opsLabel.setText("Operators:");
	    Label sampleLabel = new Label(criterionGroup, SWT.CENTER);
	    sampleLabel.setText("Samples:");

	    final List opsList = new List
		(criterionGroup, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    opsList.setLayoutData(new GridData(GridData.FILL_VERTICAL));
	    opsList.setItems(Criterion.tokens);
	    opsList.addMouseListener(new MouseAdapter() {
	    	public void mouseDoubleClick(MouseEvent e) {
	    		String[] selection = opsList.getSelection();
	    		if(selection != null && selection.length > 0) exprText.insert(" " + selection[0] + " ");
	    	}
	    });
	    
	    symbolList = new List
	    	(criterionGroup, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    symbolList.setLayoutData(new GridData(GridData.FILL_BOTH));
	    symbolList.addMouseListener(new MouseAdapter() {
	    	public void mouseDoubleClick(MouseEvent e) {
	    		String[] selection = symbolList.getSelection();
	    		if(selection != null && selection.length > 0)
	    			exprText.insert(" [" + selection[0] + "] ");
	    	}
	    });
	    	    
	    exprText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setExpression(exprText.getText());
			}
	    });
	}
	
	class ErrorArea extends Composite {
		Label errorImage;
		Label errorText;
		
		public ErrorArea(Composite parent, int style) {
			super(parent, style);
			setLayout(new RowLayout());
			errorImage = new Label(this, SWT.NULL);
			errorText = new Label(this, SWT.WRAP | SWT.CENTER);
		}
		
		public void setErrorMessage(String error) {
			if(error == null) {
				errorImage.setImage(null);
				errorText.setText("");
			} else {
				errorImage.setImage(getDisplay().getSystemImage(SWT.ICON_ERROR));
				errorText.setText(error);
			}
			getParent().layout(true, true);
		}
		
		public void setWarningMessage(String warning) {
			if(warning == null) {
				errorImage.setImage(null);
				errorText.setText("");
			} else {
				errorImage.setImage(getDisplay().getSystemImage(SWT.ICON_WARNING));
				errorText.setText(warning);
			}
			getParent().layout(true, true);
		}
	}
}
