package org.pathvisio.gui.wikipathways;

import java.util.Timer;
import java.util.TimerTask;

import org.pathvisio.wikipathways.WikiPathways;

/**
 * A timer that shows the user a save reminder using
 * the given time interval
 * @author thomas
 *
 */
public class SaveReminder extends Timer {
	private long interval;
	WikiPathways wiki;
	
	private static SaveReminder reminder;
	
	/**
	 * Start the timer that pops up a save reminder at the given interval.
	 * When a timer is already running, the interval will be updated to the 
	 * given interval
	 * @param wiki	An instance of the WikiPathways class that contains the pathway and
	 * UserInterfaceHandler
	 * @param minutesInterval The interval to remind the user to save (in minutes)
	 */
	public static void startSaveReminder(WikiPathways wiki, double minutesInterval) {
		if(reminder == null) {
			reminder = new SaveReminder(wiki, minutesInterval);
		} else {
			reminder.setMinutesInterval(minutesInterval);
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
