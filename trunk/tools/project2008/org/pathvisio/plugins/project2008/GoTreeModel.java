package org.pathvisio.plugins.project2008;

import java.util.*;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class GoTreeModel implements TreeModel 
{
	
	List<GoTerm> roots;
	
	String top = "Gene Ontology";
	
	public GoTreeModel (List<GoTerm> roots)
	{
		this.roots = roots;
	}

	private static final long serialVersionUID = 1L;

	
	public void addTreeModelListener(TreeModelListener arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	public Object getChild(Object o, int pos) 
	{
		if (o == top)
		{
			return roots.get(pos);
		}
		else
		{
			GoTerm term = (GoTerm)o;
			return term.getChildren().get(pos);
		}
	}

	public int getChildCount(Object o) 
	{
		if (o == top)
		{
			return roots.size();
		}
		else
		{
			GoTerm term = (GoTerm)o;
			return term.getChildren().size();
		}
	}

	public int getIndexOfChild(Object o, Object p) 
	{
		if (o == top)
		{
			return roots.indexOf(p);
		}
		else
		{
			GoTerm term = (GoTerm)o;
			return term.getChildren().indexOf(p);
		}
	}

	public Object getRoot() 
	{
		return top;
	}

	public boolean isLeaf(Object o) 
	{
		if (o == top)
		{
			return false;
		}
		else
		{
			GoTerm term = (GoTerm)o;
			return (term.getChildren().size() == 0);
		}
	}

	public void removeTreeModelListener(TreeModelListener arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	public void valueForPathChanged(TreePath arg0, Object arg1) 
	{
		// TODO Auto-generated method stub
		
	}

}
