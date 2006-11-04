package graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import data.*;

import junit.framework.TestCase;

public class Test extends TestCase {
	
	GmmlDrawing drawing;
    private Shell shell;
	
	public void setUp()
	{
		shell = new Shell(Display.getDefault());
        shell.setLayout(new FillLayout());
        drawing = new GmmlDrawing(shell, SWT.NO_BACKGROUND);
	}

    protected void tearDown() throws Exception {
        shell.dispose();
    }
    
	public void testInit()
	{
		GmmlData data = new GmmlData();
		drawing.fromGmmlData(data);
		assertEquals (drawing.getGmmlData(), data);
		
		data.addDataObject(new GmmlDataObject(ObjectType.GENEPRODUCT));
	}
}
