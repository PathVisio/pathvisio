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

import com.jroller.completer.CompleterFilterWithWindow;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

/**
 * A completer filter that queries the completion options via
 * an {@link OptionProvider}.
 * @see CompleterFilterWithWindow
 * @author thomas
 *
 */
public class CompleterQueryFilter extends CompleterFilterWithWindow {
	OptionProvider optionProvider;
	SortedSet<Object> options = new TreeSet<Object>();
	Set<String> history = new HashSet<String>();

	QueryThread t = new QueryThread();

	public CompleterQueryFilter(JTextComponent textField) {
		super(new Object[0], textField);
	}
	public CompleterQueryFilter(JTextComponent textField, OptionProvider optionProvider) {
		this(textField);
		this.optionProvider = optionProvider;
	}

	public void setOptionProvider(OptionProvider optionProvider) {
		this.optionProvider = optionProvider;
	}

	public void replace(FilterBypass filterBypass, int offset, int length,
			String string, AttributeSet attributeSet)
	throws BadLocationException {
		super.replace(filterBypass, offset, length, string, attributeSet);
		if(!history.contains(_preText)) {
			if(t.isAlive()) {
				t.symbolChanged();
			} else {
				t = new QueryThread();
				t.start();
			}
		}
		history.add(_preText);
	}

	/**
	 * Thread to process the queries to provide the options
	 * for the auto completion
	 * @author thomas
	 *
	 */
	class QueryThread extends Thread {
		volatile boolean dirty = true;

		public void run() {
			while(dirty) {
				dirty = false;
				doRun();
			}
			dirty = true;
		}

		void doRun() {
			int size = options.size();
			for(Object o : optionProvider.provideOptions(_preText)) {
				options.add(o);
			}
			if(options.size() > size) {
				setCompleterMatches(options.toArray());
				if(_list != null) _list.validate();
			}
		}

		public void symbolChanged() {
			dirty = true;
		}
	}
}
