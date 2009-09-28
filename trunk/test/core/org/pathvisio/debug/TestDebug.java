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
