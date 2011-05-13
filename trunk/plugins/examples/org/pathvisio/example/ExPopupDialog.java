// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package org.pathvisio.example;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.gui.dialogs.PathwayElementDialog;
import org.pathvisio.gui.dialogs.PopupDialogHandler.PopupDialogHook;
import org.pathvisio.gui.panels.PathwayElementPanel;

/**
 * Example of how to add tabs to the pathway element dialog, 
 * the dialog that appears when double-clicking an element in a pathway.
 * </p>
 * This plugin adds a "link" tab to the dialog if the pathway element is a label.
 * The value entered is set as the href property of the label.
 */
public class ExPopupDialog implements Plugin
{

	@Override
	public void done()
	{
		// nothing to be done
	}

	@Override
	public void init(PvDesktop desktop)
	{
		// define the new hook to be added
		desktop.getSwingEngine().getPopupDialogHandler().addHook(new PopupDialogHook()
		{
			@Override
			public void popupDialogHook(PathwayElement e, PathwayElementDialog dlg)
			{
				// check if it is a label first
				if (e.getObjectType() == ObjectType.LABEL)
					// add the LinkPanel, which is defined below.
					dlg.addPathwayElementPanel("Link", new LinkPanel());
			}
		});
		
		//TODO: possible improvement - invoke panel with link tab selected on Ctrl+K
	}

	private class LinkPanel extends PathwayElementPanel implements DocumentListener
	{
		// text field containing the link
		private JTextField txtRef;
		
		public LinkPanel()
		{
			txtRef = new JTextField(40);
			txtRef.getDocument().addDocumentListener(this);
			add(txtRef);
		}
		
		@Override
		public void refresh()
		{
			txtRef.setText (getInput().getHref());
		}

		@Override
		public void changedUpdate(DocumentEvent arg0)
		{
			getInput().setHref(txtRef.getText());
		}

		@Override
		public void insertUpdate(DocumentEvent arg0)
		{
			getInput().setHref(txtRef.getText());
		}

		@Override
		public void removeUpdate(DocumentEvent arg0)
		{
			getInput().setHref(txtRef.getText());
		}
	}
}
