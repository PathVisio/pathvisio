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
package org.pathvisio.preferences;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class Test extends TestCase implements PreferenceListener {
	PreferenceEvent lastEvent = null;
	Set<PreferenceEvent> events = new HashSet<PreferenceEvent>();
	
	@Override
	protected void setUp() throws Exception {
		PreferenceManager.init();
		
		//Reset event trackers
		events.clear();
		lastEvent = null;
		
		PreferenceManager.getCurrent().addListener(this);
	}
	
	public void testListener() {
		//Trigger a preference event
		PreferenceManager.getCurrent().setBoolean(GlobalPreference.DATANODES_ROUNDED, true);
		assertNotNull(lastEvent);
		assertTrue(lastEvent.isModified(GlobalPreference.DATANODES_ROUNDED));
		
		//Setting the same as current value should NOT trigger an event!
		PreferenceManager.getCurrent().setColor(GlobalPreference.COLOR_HIGHLIGHTED, Color.GREEN);
		assertFalse(lastEvent.isModified(GlobalPreference.COLOR_HIGHLIGHTED));
		
		//Setting a different value should trigger an event
		PreferenceManager.getCurrent().setColor(GlobalPreference.COLOR_HIGHLIGHTED, Color.RED);
		assertTrue(lastEvent.isModified(GlobalPreference.COLOR_HIGHLIGHTED));
		
		//We modified 2 properties, test if 2 events were fired
		assertEquals(2, events.size());		
	}
	
	public void preferenceModified(PreferenceEvent event) {
		events.add(event);
		lastEvent = event;
	}
}
