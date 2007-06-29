package org.pathvisio.view;

import java.awt.Point;

public abstract class MouseEvent extends InputEvent {

	//Buttons
	public static final int BUTTON_NONE = -1;
	public static final int BUTTON1 = 1;
	public static final int BUTTON2 = 2;
	public static final int BUTTON3 = 3;
	
	//Types
	public static final int MOUSE_DOWN = 10;
	public static final int MOUSE_UP = 11;
	public static final int MOUSE_MOVE = 12;
	public static final int MOUSE_EXIT = 13;
	public static final int MOUSE_ENTER = 14;
	public static final int MOUSE_HOVER = 15;
	public static final int MOUSE_CLICK = 16;
	
	private int type;
	private int button;
	private int clickCount;
	private int x; //x relative to source
	private int y; //y relative to source
	
	public MouseEvent(Object source, int type, int button, int x, int y, int clickCount, int modifier) {
		super(source, modifier);
		this.x = x;
		this.y = y;
		this.type = type;
		this.button = button;
		this.clickCount = clickCount;
	}
	
	//public abstract Point getLocationOnScreen();

	public int getButton() {
		return button;
	}

	public int getClickCount() {
		return clickCount;
	}

	public int getType() {
		return type;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public Point getLocation() {
		return new Point(x, y);
	}
	
	/*
	public int getXOnScreen() {
		return getLocationOnScreen().x;
	}
	
	public int getYOnScreen() {
		return getLocationOnScreen().y;
	}
	*/
}
