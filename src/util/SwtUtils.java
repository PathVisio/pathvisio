package util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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
		if(rgbNew == null) rgbNew = new RGB(0,0,0);
		return new Color(display, rgbNew);
	}
	
	public static Font changeFont(Font fOld, FontData fd, Display display)
	{
		if(fOld != null && !fOld.isDisposed())
		{
			fOld.dispose();
			fOld = null;
		}
		return new Font(display, fd);
	}
}
