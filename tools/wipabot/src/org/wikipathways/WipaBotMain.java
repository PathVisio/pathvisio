package org.wikipathways;

import java.io.IOException;

import javax.xml.rpc.ServiceException;

import org.jibble.pircbot.IrcException;

public class WipaBotMain
{
	public static void main(String[] args) throws IOException, IrcException, ServiceException
	{
		WipaBot wb = new WipaBot();
		
		wb.setVerbose(true);
		
		wb.run();
	}
}
