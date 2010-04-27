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
 * This class wraps around an ordinary ArrayList<E>
 *
 * @param <E> List element type
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
		int expectedModCount;

		DebugIterator(DebugList<E> aParent)
		{
			helper();
			parent = aParent;
			delegate = parent.delegate.listIterator();
			expectedModCount = parent.modCount;
		}

		DebugIterator(DebugList<E> aParent, int start)
		{
			helper();
			parent = aParent;
			delegate = parent.delegate.listIterator(start);
			expectedModCount = parent.modCount;
		}

		public void add(E arg0)
		{
			helper();
			delegate.add(arg0);
		}

		public boolean hasNext()
		{
			helper();
			return delegate.hasNext();
		}

		public boolean hasPrevious()
		{
			helper();
			return delegate.hasPrevious();
		}

		public E next()
		{
			helper();
			if (parent.modCount != expectedModCount)
			{
				ConcurrentModificationException ex = new ConcurrentModificationException();
				ex.initCause(parent.cause);
				System.out.println ("Detected concurrent modification");
				System.out.println ("Modification was done by");
				parent.cause.printStackTrace();
				throw ex;
			}
			return delegate.next();
		}

		public int nextIndex()
		{
			helper();
			return delegate.nextIndex();
		}

		public E previous()
		{
			helper();
			return delegate.previous();
		}

		public int previousIndex()
		{
			helper();
			return delegate.previousIndex();
		}

		public void remove()
		{
			helper();
			parent.cause = new Throwable();
			parent.modCount++;
			expectedModCount++;
			delegate.remove();
		}

		public void set(E arg0)
		{
			helper();
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
		helper();
		cause = new Throwable();
		modCount++;
		return delegate.add (arg0);
	}

	public void add(int arg0, E arg1)
	{
		helper();
		cause = new Throwable();
		modCount++;
		delegate.add (arg0, arg1);
	}

	public boolean addAll(Collection<? extends E> arg0)
	{
		helper();
		cause = new Throwable();
		modCount++;
		return delegate.addAll(arg0);
	}

	public boolean addAll(int arg0, Collection<? extends E> arg1)
	{
		helper();
		cause = new Throwable();
		modCount++;
		return delegate.addAll (arg0, arg1);
	}

	public void clear()
	{
		helper();
		cause = new Throwable();
		modCount++;
		delegate.clear();
	}

	public boolean contains(Object arg0)
	{
		helper();
		return delegate.contains (arg0);
	}

	public boolean containsAll(Collection<?> arg0)
	{
		helper();
		return delegate.containsAll(arg0);
	}

	public E get(int arg0)
	{
		helper();
		return delegate.get(arg0);
	}

	public int indexOf(Object arg0)
	{
		helper();
		return delegate.indexOf (arg0);
	}

	public boolean isEmpty()
	{
		helper();
		return delegate.isEmpty();
	}

	public Iterator<E> iterator()
	{
		helper();
		return new DebugIterator<E>(this);
	}

	public int lastIndexOf(Object arg0)
	{
		helper();
		return delegate.lastIndexOf(arg0);
	}

	public ListIterator<E> listIterator()
	{
		helper();
		return new DebugIterator<E>(this);
	}

	public ListIterator<E> listIterator(int arg0)
	{
		helper();
		return new DebugIterator<E>(this, arg0);
	}

	public boolean remove(Object arg0)
	{
		helper();
		cause = new Throwable();
		modCount++;
		return delegate.remove(arg0);
	}

	public E remove(int arg0)
	{
		helper();
		cause = new Throwable();
		modCount++;
		return delegate.remove(arg0);
	}

	public boolean removeAll(Collection<?> arg0)
	{
		helper();
		cause = new Throwable();
		modCount++;
		return delegate.removeAll(arg0);
	}

	public boolean retainAll(Collection<?> arg0)
	{
		helper();
		cause = new Throwable();
		modCount++;
		return delegate.retainAll(arg0);
	}

	public E set(int arg0, E arg1)
	{
		helper();
		return delegate.set(arg0, arg1);
	}

	public int size()
	{
		helper();
		return delegate.size();
	}

	public List<E> subList(int arg0, int arg1)
	{
		helper();
		return delegate.subList(arg0, arg1);
	}

	public Object[] toArray()
	{
		helper();
		return delegate.toArray();
	}

	public <T> T[] toArray(T[] arg0)
	{
		helper();
		return delegate.toArray(arg0);
	}

	private static void helper()
	{
//		Throwable x = new Throwable();
//		StackTraceElement[] elts = x.getStackTrace();
//		for (int i = 1; i < Math.min (elts.length, 4); ++i)
//		{
//			System.out.print (" -> " + elts[i].getClassName() + "." + elts[i].getMethodName() + ":" + elts[i].getLineNumber());
//		}
//		System.out.println ();
	}

}
