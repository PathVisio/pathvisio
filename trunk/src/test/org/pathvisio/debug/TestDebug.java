package org.pathvisio.debug;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;

import junit.framework.TestCase;

public class TestDebug extends TestCase
{
	DebugList<String> l;
	
	public void setUp()
	{
		l = new DebugList<String>();
		l.add ("boom");
		l.add ("roos");
		l.add ("vis");
		l.add ("vuur");
	}
	
	public void test1()
	{			
		try
		{
			ListIterator<String> i = l.listIterator();
			while (i.hasNext())
			{
				System.out.println (i.next());
				i.add("Hello");
				l.remove(3);
			}
			fail ("Expected concurrentModificationException");
		}
		catch (ConcurrentModificationException ex)
		{
			// success!
		}
		
		try
		{
			for (String s : l)
			{
				System.out.println (s);
				l.add("Bye");
			}
			fail ("Expected concurrentModificationException");
		}
		catch (ConcurrentModificationException ex)
		{
			// success!
		}

	}
}
