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
package org.pathvisio.view;

import java.util.ArrayList;
import java.util.List;
import org.pathvisio.Engine;
import org.pathvisio.model.Pathway;

public class UndoManager 
{
	private List<UndoAction> undoList = new ArrayList<UndoAction>();

	void newAction (String desc)
	{
		Pathway pwy = Engine.getCurrent().getActivePathway();
		UndoAction x = new UndoAction (desc, (Pathway)pwy.clone());
		undoList.add (x);
	}
		
	void undo()
	{
		if (undoList.size() > 0)
		{
			UndoAction a = undoList.get(undoList.size()-1);
			System.out.println ("Undoing " + a.getMessage());
			a.undo();
			undoList.remove(a);
			System.out.println (undoList.size() + " remaining");
		}
	}
}
