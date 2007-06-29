package org.pathvisio.view;


public class KeyEvent extends InputEvent {
	//Types
	public static final int KEY_PRESSED = 0;
	public static final int KEY_RELEASED = 1;
	
	//Keys
	public static final int MIN_VALUE = Character.MAX_VALUE;
	public static final int NONE = 0;
	public static final int CTRL = MIN_VALUE + 1;
	public static final int ALT = MIN_VALUE + 2;
	public static final int SHIFT = MIN_VALUE + 3;
	public static final int DEL = MIN_VALUE + 4;
	public static final int INSERT = MIN_VALUE + 5;
	
	private static int twoPower(int p) { return (int)Math.pow(2, p); }

	private int keyCode;
	private int type;
	
	public KeyEvent(Object source, int keyCode, int type, int modifier) {
		super(source, modifier);
		this.keyCode = keyCode;
		this.type = type;
	}
	
	public KeyEvent(Object source, char keyCode, int type, int modifier) {
		this(source, Character.getNumericValue(keyCode), type, modifier);
	}
	
	public int getKeyCode() {
		return keyCode;
	}	
	
	public int getType() {
		return type;
	}
	
	public boolean isKey(char c) {
		System.out.println("ask: " + (int)c);
		System.out.println("have: " + keyCode);
		return (int)c == keyCode;
	}
	
	public boolean isKey(int i) {
		return keyCode == i;
	}
}
