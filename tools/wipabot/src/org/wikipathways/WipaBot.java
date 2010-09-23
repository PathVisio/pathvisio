package org.wikipathways;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.rpc.ServiceException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.webservice.WSHistoryRow;
import org.pathvisio.wikipathways.webservice.WSPathwayHistory;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

public class WipaBot extends PircBot
{
	Timer t;
	Date last;
	
	final static String channel = "#wikipathways";
	
	Set<WSHistoryRow> reported = new HashSet<WSHistoryRow>();
	
	class PollTask extends TimerTask
	{
		@Override
		public void run()
		{
			System.out.println ("TIMER FIRED");
			// TODO Auto-generated method stub

			System.out.println ("Changes since " + last);
			try
			{
				WSPathwayInfo infos[] = client.getRecentChanges(last);
				
				last = new Date();
				for (WSPathwayInfo info : infos)
				{
					WSHistoryRow history[] = client.getPathwayHistory(info.getId(), last).getHistory();
					
					for (WSHistoryRow hist : history)
					{
						String msg = info.getName() + " - " + info.getSpecies() + " updated by " + hist.getUser() + " with comment " + hist.getComment();
						System.out.println (msg);					
						
						if (reported.contains(hist))
						{
							System.out.println ("ERROR: already reported");
						}
						else
						{
							WipaBot.this.sendMessage(channel, msg);
							reported.add(hist);
						}
					}
				}
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	WikiPathwaysClient client;
	
	public WipaBot() throws ServiceException
	{
		this.setName("wipabot");
		client = new WikiPathwaysClient();
	}

	public void run() throws IOException, IrcException
    {
        this.connect("irc.freenode.net");
//        this.sendMessage("NickServ", "IDENTIFY password_here");
        this.joinChannel("#wikipathways");
        
        t = new Timer();
        last = new Date();
        t.schedule(new PollTask(), 0, 60000);
    }
}
