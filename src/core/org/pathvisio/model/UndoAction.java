// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 PathVisio contributors (for a complete list, see CONTRIBUTORS.txt)
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
package org.pathvisio.model;

public class UndoAction 
{
	
	public UndoAction(String _message, 
			int _actionType,
			PathwayElement _affectedObject)
	{
		parent = _affectedObject.getParent();
		message = _message;
		actionType = _actionType;
		affectedObject = _affectedObject;
		if (actionType == UNDO_CHANGE)
		{
			//TODO: doesn't work right now because
			//copy is made after change already took place!
			savedObject = affectedObject.copy(); 
		}
	}
	
	String message;
	Pathway parent;
	
	public static final int UNDO_ADD = 1;
	public static final int UNDO_REMOVE = 2;
	public static final int UNDO_CHANGE = 3;
	
	int actionType;
	
	/**
	 * affectedObject contains a reference to the actual object,
	 * used in all three event types.
	 * It acts like a pointer, its fields may be changed by following actions. 
	 */
	private PathwayElement affectedObject;
	/**
	 * savedObject contains a copy of the actual object,
	 * used only for the UNDO_CHANGE type.
	 */
	private PathwayElement savedObject = null;
	
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
				affectedObject.copyValuesFrom(savedObject);
				break;
		}
	}
}
