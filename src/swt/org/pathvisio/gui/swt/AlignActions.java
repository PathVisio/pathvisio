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
public class AlignActions {

		
	public static final char CENTERX = 'x';
	public static final char CENTERY = 'y';
	public static final char LEFT = 'l';
	public static final char RIGHT = 'r';
	public static final char TOP = 't';
	public static final char BOTTOM = 'b';
	public static final char WIDTH = 'w';
	public static final char HEIGHT = 'h';
	
	
		static class AlignCenterXAction extends Action 
		{
			MainWindowBase window;
			public AlignCenterXAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Align horizontal centers");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/aligncenterx.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().alignSelected(CENTERX);
				
			}
		}
		
		static class AlignCenterYAction extends Action 
		{
			MainWindowBase window;
			public AlignCenterYAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Align vertical centers");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/aligncentery.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().alignSelected(CENTERY);
				
			}
		}
		static class AlignLeftAction extends Action 
		{
			MainWindowBase window;
			public AlignLeftAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Align left edges");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/alignleft.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().alignSelected(LEFT);
				
			}
		}
		static class AlignRightAction extends Action 
		{
			MainWindowBase window;
			public AlignRightAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Align right edges");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/alignright.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().alignSelected(RIGHT);
				
			}
		}
		static class AlignTopAction extends Action 
		{
			MainWindowBase window;
			public AlignTopAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Align top edges");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/aligntop.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().alignSelected(TOP);
				
			}
		}
		static class AlignBottomAction extends Action 
		{
			MainWindowBase window;
			public AlignBottomAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Align bottom edges");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/alignbottom.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().alignSelected(BOTTOM);
				
			}
		}
		static class SetCommonHeightAction extends Action 
		{
			MainWindowBase window;
			public SetCommonHeightAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Set common height");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/sizeheight.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().scaleSelected(HEIGHT);
				
			}
		}
		static class SetCommonWidthAction extends Action 
		{
			MainWindowBase window;
			public SetCommonWidthAction (MainWindowBase w)
			{
				window = w;
				setToolTipText ("Set common width");
				setImageDescriptor(ImageDescriptor.createFromURL(Engine.getResourceURL("icons/sizewidth.gif")));
				
			}
			public void run () 
			{
				
				Engine.getActiveVPathway().scaleSelected(WIDTH);
				
			}
		}


	}


