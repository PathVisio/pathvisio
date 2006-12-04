// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package graphics;

import junit.framework.TestCase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import data.GmmlData;
import data.GmmlDataObject;
import data.ObjectType;

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
		
		data.add(new GmmlDataObject(ObjectType.GENEPRODUCT));
	}
}
