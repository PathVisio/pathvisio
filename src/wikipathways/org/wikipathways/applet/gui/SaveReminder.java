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
package org.wikipathways.applet.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.wikipathways.applet.WikiPathways;

/**
 * A timer that shows the user a save reminder using
 * the given time interval
 * @author thomas
 *
 */
public class SaveReminder extends Timer {
	private long interval;
	WikiPathways wiki;

	private static Map<WikiPathways, SaveReminder> reminders = new HashMap<WikiPathways, SaveReminder>();

	/**
	 * Start the timer that pops up a save reminder at the given interval.
	 * When a timer is already running, the interval will be updated to the
	 * given interval
	 * @param wiki	An instance of the WikiPathways class that contains the pathway and
	 * UserInterfaceHandler
	 * @param minutesInterval The interval to remind the user to save (in minutes)
	 */
	public static void startSaveReminder(WikiPathways wiki, double minutesInterval) {
		SaveReminder reminder = reminders.get(wiki);
		if(reminder == null) {
			reminder = new SaveReminder(wiki, minutesInterval);
			reminders.put(wiki, reminder);
		} else {
			reminder.setMinutesInterval(minutesInterval);
		}
	}

	/**
	 * Stops the save reminder for the given wiki
	 */
	public static void stopSaveReminder(WikiPathways wiki) {
		SaveReminder reminder = reminders.get(wiki);
		if(reminder != null) {
			reminder.cancel();
			reminders.remove(wiki);
		}
	}

	private SaveReminder(WikiPathways wiki, double minutesInterval) {
		super("WikiPathways save reminder", true);
		this.wiki = wiki;
		setMinutesInterval(minutesInterval);
		schedule(new SaveTask(), interval);
	}

	private void setMinutesInterval(double minutesInterval) {
		this.interval = (long)(minutesInterval * 1000 * 60);
	}

	private class SaveTask extends TimerTask {
		public void run() {
			askAutoSave();
		}

	}

	private void askAutoSave() {
		String msg =
			"It has been " + (interval / (1000 * 60)) +
			" minutes ago since you last saved the pathway.\n " +
			"Would you like to save now?";
		if(wiki.hasChanged()) {
			boolean answer = wiki.getUserInterfaceHandler().askQuestion("Reminder", msg);
			if(answer) {
				wiki.saveUI("Periodical save, work in progress");
			}
		}
		purge();
		schedule(new SaveTask(), interval);
	}
}
