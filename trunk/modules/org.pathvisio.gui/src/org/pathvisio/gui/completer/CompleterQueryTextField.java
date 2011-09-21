// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.gui.completer;

import com.jroller.completer.CompleterFilter;
import com.jroller.completer.CompleterTextField;

/**
 * Text field with auto completion that can be used if the completion
 * options need to be fetched from a database while the user is typing.
 * The options will be fetched in a seperate thread.
 * @author thomas
 *
 */
public class CompleterQueryTextField extends CompleterTextField {
	CompleterQueryFilter myFilter;

	public CompleterQueryTextField(OptionProvider optionProvider,
			boolean useWindow) {
		super(new Object[0]);
		myFilter.setOptionProvider(optionProvider);
	}

	protected CompleterFilter getFilter() {
		if(myFilter == null) {
			myFilter = new CompleterQueryFilter(this, null);
		}
		return myFilter;
	}
}
