package org.pathvisio.plugins.statistics;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.plugin.Plugin;

public class StatisticsPlugin implements Plugin 
{
	public void init() 
	{
		StatisticsAction statisticsAction = new StatisticsAction();

		Logger.log.info ("Initializing statistics plugin");
		SwingEngine.getCurrent().registerMenuAction ("Data", statisticsAction);
	}
	
	public static class StatisticsAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public StatisticsAction() 
		{
			super();
			putValue(NAME, "Statistics...");
			putValue(SHORT_DESCRIPTION, "Do simple pathway statistics");
		}

		public void actionPerformed(ActionEvent e) 
		{
			JOptionPane.showMessageDialog(SwingEngine.getCurrent().getFrame(), "Action not implemented");
		}
	}


}