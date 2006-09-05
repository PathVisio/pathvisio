package data;

import java.util.EventListener;

public interface GmmlListener extends EventListener {
	
	public void gmmlObjectModified(GmmlEvent e);
	
}
