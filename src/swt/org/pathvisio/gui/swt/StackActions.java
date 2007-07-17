//	 PathVisio,
//	 a tool for data visualization and analysis using Biological Pathways
//	 Copyright 2006-2007 BiGCaT Bioinformatics
	//
//	 Licensed under the Apache License, Version 2.0 (the "License"); 
//	 you may not use this file except in compliance with the License. 
//	 You may obtain a copy of the License at 
//	 
//	 http://www.apache.org/licenses/LICENSE-2.0 
//	  
//	 Unless required by applicable law or agreed to in writing, software 
//	 distributed under the License is distributed on an "AS IS" BASIS, 
//	 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//	 See the License for the specific language governing permissions and 
//	 limitations under the License.
	//

package org.pathvisio.gui.swt;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.pathvisio.Engine;
public class StackActions {

		
	public static final char CENTERX = 'x';
	public static final char CENTERY = 'y';
	public static final char LEFT = 'l';
	public static final char RIGHT = 'r';
	public static final char TOP = 't';
	public static final char BOTTOM = 'b';
	public static final char WIDTH = 'w';
	public static final char HEIGHT = 'h';
	
	
		static class StackCenterXAction extends Action 
		{
			MainWindowBase window;
			public StackCenterXAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Stack vertical center");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/stackverticalcenter.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().stackSelected(CENTERX);
				
			}
		}
		
		static class StackCenterYAction extends Action 
		{
			MainWindowBase window;
			public StackCenterYAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Stack horizontal center");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/stackhorizontalcenter.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().stackSelected(CENTERY);
				
			}
		}
		static class StackLeftAction extends Action 
		{
			MainWindowBase window;
			public StackLeftAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Stack vertical left");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/stackverticalleft.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().stackSelected(LEFT);
				
			}
		}
		static class StackRightAction extends Action 
		{
			MainWindowBase window;
			public StackRightAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Stack veritcal right");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/stackverticalright.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().stackSelected(RIGHT);
				
			}
		}
		static class StackTopAction extends Action 
		{
			MainWindowBase window;
			public StackTopAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Stack horizontal top");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/stackhorizontaltop.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().stackSelected(TOP);
				
			}
		}
		static class StackBottomAction extends Action 
		{
			MainWindowBase window;
			public StackBottomAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Stack horizontal bottom");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/stackhorizontalbottom.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().stackSelected(BOTTOM);
				
			}
		}

	}


