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
/*******************************************************************************
 * Copyright (c) 2007 SAS Institute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAS Institute - initial API and implementation
 *******************************************************************************/
package org.pathvisio.gui.swt.awt;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

class CleanResizeListener extends ControlAdapter {
    private Rectangle oldRect = null;
    public void controlResized(ControlEvent e) {
        assert e != null;
        assert Display.getCurrent() != null;     // On SWT event thread
        
        // Prevent garbage from Swing lags during resize. Fill exposed areas 
        // with background color. 
        Composite composite = (Composite)e.widget;
        //Rectangle newRect = composite.getBounds();
        //newRect = composite.getDisplay().map(composite.getParent(), composite, newRect);
        Rectangle newRect = composite.getClientArea();
        if (oldRect != null) {
            int heightDelta = newRect.height - oldRect.height;
            int widthDelta = newRect.width - oldRect.width;
            if ((heightDelta > 0) || (widthDelta > 0)) {
                GC gc = new GC(composite);
                try {
                    gc.fillRectangle(newRect.x, oldRect.height, newRect.width, heightDelta);
                    gc.fillRectangle(oldRect.width, newRect.y, widthDelta, newRect.height);
                } finally {
                    gc.dispose();
                }
            }
        }
        oldRect = newRect;
    }
}