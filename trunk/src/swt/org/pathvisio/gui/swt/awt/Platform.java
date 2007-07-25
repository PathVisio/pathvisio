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

import org.eclipse.swt.SWT;

class Platform {
    private static String platformString = SWT.getPlatform();

    // prevent instantiation
    private Platform() {
    }
    
    public static boolean isWin32() {
        return "win32".equals(platformString); //$NON-NLS-1$
    }
    
    public static boolean isGtk() {
        return "gtk".equals(platformString); //$NON-NLS-1$
    }
    
}
