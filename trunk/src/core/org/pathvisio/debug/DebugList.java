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

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This is a List that helps in the search for ConcurrentModification problems.
 * Most List's in the collection interface are fail-fast, if you modify a list
 * while iterating over it, a ConcurrentModificationException might be thrown.
 * 
 * The problem is that this exception is thrown when this is detected,
 * which is in the iterator.next() method, but it does not tell where the modification
 * occurred.
 * 
 * DebugList saves a stack trace for each modification made to it. Then, when a
 * the exceptional situation arises, it also throws a ConcurrentModificationException
 * but adds the point of the last modification as the cause,
 * which should help greatly in debugging the problem.
 * 
 * This is intended to debug problems with single-threaded usage of lists,
 * I don't know how useful it will be for debugging multi-threading problems.
 * 
 * This class wraps around an ordinary ArrayList
 */
public class DebugList<E> implements List<E> 
{
	/**
	 * An iterator to go with the DebugList.
	 * This is the normal iterator for DebugLists,
	 * the next() method checks for concurrent modification
	 * and throws a ConcurrentModificationException with the last
	 * modification point as the cause.
	 */
	public static class DebugIterator<E> implements ListIterator<E>
	{
		final private DebugList<E> parent;
		final private ListIterator<E> delegate;
		final int expectedModCount;
		
		DebugIterator(DebugList<E> aParent)
		{
			parent = aParent;
			delegate = parent.delegate.listIterator();
			expectedModCount = parent.modCount;
		}

		DebugIterator(DebugList<E> aParent, int start)
		{
			parent = aParent;
			delegate = parent.delegate.listIterator(start);
			expectedModCount = parent.modCount;
		}
		
		public void add(E arg0) 
		{
			delegate.add(arg0);
		}

		public boolean hasNext() 
		{
			return delegate.hasNext();
		}

		public boolean hasPrevious() 
		{
			return delegate.hasPrevious();
		}

		public E next() 
		{
			if (parent.modCount != expectedModCount)
			{
				ConcurrentModificationException ex = new ConcurrentModificationException();
				ex.initCause(parent.cause);
				throw ex;
//				System.out.println ("Detected concurrent modification");
//				System.out.println ("Modification was done by");
//				parent.cause.printStackTrace();
			}
			return delegate.next();
		}

		public int nextIndex() 
		{
			return delegate.nextIndex();
		}

		public E previous() 
		{
			return delegate.previous();
		}

		public int previousIndex() 
		{
			return delegate.previousIndex();
		}

		public void remove() 
		{
			delegate.remove();
		}

		public void set(E arg0) 
		{
			delegate.set(arg0);
		}
	}
	
	// point of last modification
	private Throwable cause;
	
	// actual list
	private List<E> delegate = new ArrayList<E>();

	// #of structural modifications made to this list,
	// where each add, addAll, remove, clear, retainAll method call counts for one.
	// this is, if you will, the revision number of the list data structure
	int modCount = 0;
	
	public boolean add(E arg0) 
	{
		cause = new Throwable();
		modCount++;
		return delegate.add (arg0);
	}

	public void add(int arg0, E arg1) 
	{
		cause = new Throwable();
		modCount++;
		delegate.add (arg0, arg1);
	}

	public boolean addAll(Collection<? extends E> arg0) 
	{
		cause = new Throwable();
		modCount++;
		return delegate.addAll(arg0);
	}

	public boolean addAll(int arg0, Collection<? extends E> arg1) 
	{
		cause = new Throwable();
		modCount++;
		return delegate.addAll (arg0, arg1);
	}

	public void clear() 
	{
		cause = new Throwable();
		modCount++;
		delegate.clear();
	}

	public boolean contains(Object arg0) 
	{
		return delegate.contains (arg0);
	}

	public boolean containsAll(Collection<?> arg0) 
	{
		return delegate.containsAll(arg0);
	}

	public E get(int arg0) 
	{
		return delegate.get(arg0);
	}

	public int indexOf(Object arg0) 
	{
		return delegate.indexOf (arg0);
	}

	public boolean isEmpty() 
	{
		return delegate.isEmpty();
	}

	public Iterator<E> iterator() 
	{
		return new DebugIterator<E>(this);
	}

	public int lastIndexOf(Object arg0) 
	{
		return delegate.lastIndexOf(arg0);
	}

	public ListIterator<E> listIterator() 
	{
		return new DebugIterator<E>(this);
	}

	public ListIterator<E> listIterator(int arg0) 
	{
		return new DebugIterator<E>(this, arg0);
	}

	public boolean remove(Object arg0) 
	{
		cause = new Throwable();
		modCount++;
		return delegate.remove(arg0);
	}

	public E remove(int arg0) 
	{
		cause = new Throwable();
		modCount++;
		return delegate.remove(arg0);
	}

	public boolean removeAll(Collection<?> arg0) 
	{
		cause = new Throwable();
		modCount++;
		return delegate.removeAll(arg0);
	}

	public boolean retainAll(Collection<?> arg0) 
	{
		cause = new Throwable();
		modCount++;
		return delegate.retainAll(arg0);
	}

	public E set(int arg0, E arg1) 
	{
		return delegate.set(arg0, arg1);
	}

	public int size() 
	{
		return delegate.size();
	}

	public List<E> subList(int arg0, int arg1) 
	{
		return delegate.subList(arg0, arg1);
	}

	public Object[] toArray() 
	{
		return delegate.toArray();
	}

	public <T> T[] toArray(T[] arg0) 
	{
		return delegate.toArray(arg0);
	}

}
