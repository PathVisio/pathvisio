package util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class SwtUtils {

	public static Color changeColor(Color cOld, RGB rgbNew, Display display)
	{
		if(cOld != null && !cOld.isDisposed())
		{
			cOld.dispose();
			cOld = null;
		}
		return new Color(display, rgbNew);
	}
}
