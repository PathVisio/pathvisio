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
package data;

public class UndoAction 
{
	
	public UndoAction(String message, GmmlData parent)
	{
	
	}
	
	GmmlData parent;
	
	static final int UNDO_ADD = 1;
	static final int UNDO_REMOVE = 2;
	static final int UNDO_CHANGE = 3;
	
	int actionType;
	GmmlDataObject affectedObject;
	GmmlDataObject savedObject;
	
	public void undo()
	{
		switch (actionType)
		{
			case UNDO_ADD:
				parent.remove(affectedObject);
				break;
			case UNDO_REMOVE:
				parent.add(affectedObject);
				break;
			case UNDO_CHANGE:
				parent.remove(affectedObject);
				parent.add (savedObject);
				break;
		}
	}
}
