package util;


import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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

import sun.awt.FocusingTextField;
import util.SuggestCombo.SuggestionProvider.SuggestThread;

public class SuggestCombo extends Composite {
	protected boolean ignoreModify;
	protected boolean ignoreFocusOut;

	private SuggestThread currThread;
	
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
	
	public SuggestionProvider getSuggestionProvider() { 
		return suggestionProvider; 
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
					if(!ignoreModify) {
						startSuggesting();
						currThread.setText(text.getText());
					} else {
//						hideSuggestions();
						ignoreModify = false;
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
				case SWT.FocusOut:
					if(!ignoreFocusOut) {
						//Check if focus is on suggestShell/suggestList
						if(!isSuggestFocus()) stopSuggesting();
					}
					break;
				}
			}
		};
		
		text.addListener(SWT.Modify, textListener);
		text.addListener(SWT.KeyDown, textListener);
		text.addListener(SWT.FocusOut, textListener);

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
					break;
				case SWT.FocusIn:
					System.out.println("Focus on list");
					//try { throw new Exception(); } catch(Exception ex) { ex.printStackTrace(); }
					break;
				case SWT.FocusOut:
					if(!ignoreFocusOut) {
						stopSuggesting();
					}
				}
			}
		};
		suggestList.addListener(SWT.KeyDown, listListener);
		suggestList.addListener(SWT.DefaultSelection, listListener);
		suggestList.addListener(SWT.MouseDown, listListener);
		suggestList.addListener(SWT.FocusOut, listListener);
		suggestList.addListener(SWT.FocusIn, listListener);
		
		getShell().addShellListener(new ShellAdapter() {
			public void shellDeactivated(ShellEvent e) {
				stopSuggesting();
			}
		});
		suggestShell.addShellListener(new ShellAdapter() {
			public void shellActivated(ShellEvent arg0) {
				showSuggestions();
			}
		});
		// DisposeListener, in case user closes PathVisio while suggestShell is still active
		suggestShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				stopSuggesting();
			}
		});
	}
	
	public void setVisible(boolean visible) {
		stopSuggesting();
		super.setVisible(visible);
	}
	
	public void dispose() {
		super.dispose();
		suggestShell.dispose();
	}
	
	void startSuggesting() {		
		if(!suggestShell.isVisible()) {
			initSuggestShell();
		}
		if(!isSuggesting()) {
			currThread = new SuggestThread(text.getText(), this);
			currThread.start();
		}
	}
	
	void stopSuggesting() {
		doHideSuggestions();
		if(currThread != null) {
			System.out.println("> SuggestThread interrupt requested");
			currThread.interrupt();
		}
		currThread = null;
	}
	
	boolean isSuggesting() {
		return currThread != null && currThread.getState() != Thread.State.TERMINATED;
	}
	
	public void addSuggestion(final String suggestion) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				doAddSuggestion(suggestion);
			}
		});
}
	
	void doAddSuggestion(String suggestion) {
			suggestList.add(suggestion);
			showSuggestions();
			resizeSuggestShell();
	}
	

	
	void initSuggestShell() {
		Point location = text.getLocation();
		location.y += text.getSize().y;
		suggestShell.setLocation(text.toDisplay(location));
		resizeSuggestShell();
	}
	
	void resizeSuggestShell() {
		Point listSize = suggestList.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		suggestShell.setSize(listSize.x, listSize.y < maxHeight ? listSize.y : maxHeight);
	}
	
	public void setSuggestions(final String[] suggestions) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				doSetSuggestions(suggestions);
			}
		});
	}
	
	void doSetSuggestions(String[] suggestions) {
		suggestList.setItems(suggestions);
		resizeSuggestShell();
		showSuggestions();
	}
	
	public void hideSuggestions() {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				doHideSuggestions();
			}
		});
	}
	
	void doHideSuggestions() {
		if(!suggestShell.isDisposed()) 
			suggestShell.setVisible(false);
	}
	
	void showSuggestions() {
		boolean restoreFocus = text.isFocusControl();
		if(isFocusControl() && suggestList.getItemCount() > 0) {
			suggestShell.setVisible(true);
			
			if(restoreFocus) {
				ignoreFocusOut = true;
				text.setFocus();
				ignoreFocusOut = false;
			}
			
		} else {
			doHideSuggestions();
		}
	}
	
	void suggestionSelected(String suggestion) {
		ignoreModify = true;
		text.setText(suggestion);
		doHideSuggestions();
		for(SuggestionListener l : listeners) {
			ignoreModify = true;
			l.suggestionSelected(suggestion);
		}
	}
	
	public void addSuggetsionListener(SuggestionListener l) {
		listeners.add(l);
	}
	
	public interface SuggestionProvider {		
		public String[] getSuggestions(String text, SuggestCombo suggestCombo);
		
		public class SuggestThread extends Thread {
			protected SuggestCombo suggestCombo;
			volatile protected String text;
			volatile protected boolean textChange;
			
			private Thread doSuggestThread;
			
			public SuggestCombo getSuggestCombo() { return suggestCombo; }
			public String getText() { return text; }
			
			public void setText(String newText) { 
				text = newText;
				textChange = true;
			}
			
			public SuggestThread(String _text, SuggestCombo _suggestCombo) {
				text = _text;
				suggestCombo = _suggestCombo;
			}
			
			public void start() {
				if(suggestCombo == null || suggestCombo.isDisposed()) return;
				super.start();
			}

			void setSuggestions(String[] suggestions) {
				if(Thread.currentThread() == doSuggestThread) {
					suggestCombo.setSuggestions(suggestions);
				}
			}
			
			public void run() {
				while(!isInterrupted()) {
					if(textChange) {
						if(doSuggestThread != null) doSuggestThread.interrupt();
						suggestCombo.hideSuggestions();
						if(!text.equals("")) {
							doSuggestThread = new Thread() {
								public void run() {
									SuggestionProvider sp = suggestCombo.getSuggestionProvider();
									setSuggestions(sp.getSuggestions(text, suggestCombo));
								}
							};
							doSuggestThread.start();
						} else {
							suggestCombo.hideSuggestions();
						}
						textChange = false;
					} else {						
						try {
							Thread.sleep(300); //Wait for a while, in case user continues typing
						} catch (InterruptedException e) {
							return;
						}
					}
				}
			}
		}
	}
		
	public interface SuggestionListener {
		public void suggestionSelected(String suggestion);
	}
}
