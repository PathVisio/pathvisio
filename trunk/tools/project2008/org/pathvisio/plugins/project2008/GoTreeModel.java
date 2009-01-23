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
package org.pathvisio.plugins.project2008;

import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A simple static TreeModel for displaying the Gene Ontology as a tree.
 */
public class GoTreeModel implements TreeModel 
{
	
	List<GoTerm> roots;
	
	String top = "Gene Ontology";
	
	public GoTreeModel (List<GoTerm> roots)
	{
		this.roots = roots;
	}
	
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
