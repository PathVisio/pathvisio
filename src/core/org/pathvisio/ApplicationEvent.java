package org.pathvisio;

import java.util.EventObject;

public class ApplicationEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	public static final int PATHWAY_OPENED = 1;
	public static final int PATHWAY_NEW = 2;
	public static final int APPLICATION_CLOSE = 3;
	public static final int VPATHWAY_CREATED = 4;
	public static final int VPATHWAY_OPENED = 5;
	public static final int VPATHWAY_NEW = 6;

	public Object source;
	public int type;
	
	public ApplicationEvent(Object source, int type) {
		super(source);
		this.source = source;
		this.type = type;
	}
}
