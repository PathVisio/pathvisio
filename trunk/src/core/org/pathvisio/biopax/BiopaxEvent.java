package org.pathvisio.biopax;

import java.util.EventObject;

public class BiopaxEvent extends EventObject {
	public static final int BIOPAX_MODIFIED = 0;
	
	public BiopaxEvent(BiopaxReferenceManager source) {
		super(source);
	}
}
