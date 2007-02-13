package util;


import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SuggestCombo extends Composite {
	protected boolean noModifyListen;
	
	private java.util.List<SuggestionListener> listeners;
	private SuggestionProvider suggestionProvider;
	
	private int maxHeight = 100;
	
	private Text text;
	private Shell suggestShell;
	private List suggestList;
	
	public SuggestCombo(Composite parent, SuggestionProvider s) {
		super(parent, SWT.NONE);
		listeners = new ArrayList<SuggestionListener>();
		suggestionProvider = s;
		init(parent);
	}
	
	public Control getControl() { 
		return text;
	}
	
	public boolean isFocusControl() {		
		return 	text.isVisible() && text.isFocusControl() ||
				isSuggestFocus();
	}
	
	public boolean isSuggestFocus() {
		return 	suggestShell.isVisible() && suggestShell.isFocusControl() ||
				suggestList.isVisible() && suggestList.isFocusControl();
	}
	
	public String getText() {
		return text.getText();
	}
	
	public void setText(String s) {
		System.out.println("Setting text: " + s);
		text.setText(s);
	}
	
	public void setMaxHeight(int height) {
		maxHeight = height;
	}
	
	public int getMaxHeight() { return maxHeight; }
	
	void init(Composite parent) {
		setLayout(new FillLayout());
		
		text = new Text(this, SWT.NONE);
		Listener textListener = new Listener() {
			public void handleEvent(Event e) {
				switch(e.type) {
				case SWT.Modify:
					String[] s = suggestionProvider.getSuggestions(text.getText());
					if(s.length > 1 && !noModifyListen) {
						displaySuggestions(s);
						text.setFocus();
					} else {
						hideSuggestions();
						noModifyListen = false;
					}
					break;
				case SWT.KeyDown:
					if(e.keyCode == SWT.ARROW_DOWN) {
						if(suggestShell.isVisible()) {
							suggestList.select(0);
							suggestList.setFocus();
						}
					}
					break;
				}
			}
		};
		
		text.addListener(SWT.Modify, textListener);
		text.addListener(SWT.KeyDown, textListener);

		suggestShell = new Shell(getShell(), SWT.TOOL | SWT.ON_TOP);
		suggestShell.setLayout(new FillLayout());
		suggestList = new List(suggestShell, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		
		Listener listListener = new Listener() {
			public void handleEvent(Event e) {
				switch(e.type) {
				case SWT.KeyDown:
					if(e.keyCode != 13) break; //TODO:find proper SWT constant for return key
				case SWT.DefaultSelection:
				case SWT.MouseDown:
					String[] selection = suggestList.getSelection();
					if(selection.length > 0) 
						suggestionSelected(suggestList.getSelection()[0]);
				}
			}
		};
		suggestList.addListener(SWT.KeyDown, listListener);
		suggestList.addListener(SWT.DefaultSelection, listListener);
		suggestList.addListener(SWT.MouseDown, listListener);
		
		getShell().addShellListener(new ShellAdapter() {
			public void shellDeactivated(ShellEvent e) {
				hideSuggestions();
			}
		});
		suggestShell.addShellListener(new ShellAdapter() {
			public void shellActivated(ShellEvent arg0) {
				unhideSuggestions();
			}
		});
	}
	
	public void setVisible(boolean visible) {
		hideSuggestions();
		super.setVisible(visible);
	}
	
	public void dispose() {
		super.dispose();
		suggestShell.dispose();
	}
	
	void displaySuggestions(String[] suggestions) {
		suggestList.setItems(suggestions);
		Point listSize = suggestList.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point location = text.getLocation();
		location.y += text.getSize().y;
		suggestShell.setLocation(text.toDisplay(location));
		suggestShell.setSize(listSize.x, listSize.y < maxHeight ? listSize.y : maxHeight);
		suggestShell.setVisible(true);
	}
	
	void hideSuggestions() {
		suggestShell.setVisible(false);
	}
	
	void unhideSuggestions() {
		suggestShell.setVisible(true);
	}
	
	void suggestionSelected(String suggestion) {
		noModifyListen = true;
		text.setText(suggestion);
		hideSuggestions();
		for(SuggestionListener l : listeners) {
			noModifyListen = true;
			l.suggestionSelected(suggestion);
		}
	}
	
	public void addSuggetsionListener(SuggestionListener l) {
		listeners.add(l);
	}
	
	public interface SuggestionProvider {
		public String[] getSuggestions(String text);
	}
	
	public interface SuggestionListener {
		public void suggestionSelected(String suggestion);
	}
}
