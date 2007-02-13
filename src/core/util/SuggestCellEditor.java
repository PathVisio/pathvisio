package util;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import util.SuggestCombo.SuggestionProvider;


public abstract class SuggestCellEditor extends CellEditor {
	protected SuggestCombo suggestCombo;
	
	public SuggestCellEditor(Composite parent) {
		super(parent);
	}
		
	protected Control createControl(Composite parent) {
		suggestCombo = new SuggestCombo(parent, getSuggestionProvider());

		setKeyListeners();
		setFocusListeners();

		return suggestCombo;
	}

	protected void setKeyListeners() {
		suggestCombo.getControl().addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                keyReleaseOccured(e);
            }
		});
	}
	
	protected void setFocusListeners() {
        suggestCombo.getControl().addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
            	if(!suggestCombo.isSuggestFocus()) {
            		SuggestCellEditor.this.focusLost();
            	}
            }
        });
	}
	public abstract SuggestionProvider getSuggestionProvider();
		
	protected Object doGetValue() {
		return suggestCombo.getText();
	}
	
	protected void doSetValue(Object value) {
		suggestCombo.setText(value == null ? "" : value.toString());
	}

	protected void doSetFocus() {}


}
