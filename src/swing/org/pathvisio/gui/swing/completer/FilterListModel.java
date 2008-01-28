// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
/**
 * Copyright Neil Cochrane 2006
 * @author neilcochrane
 */
package org.pathvisio.gui.swing.completer;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractListModel;

/**
 *
 * Class to hold the remaining objects that still match the users input.
 * @author ncochran
 *
 */
public class FilterListModel extends AbstractListModel
{
  public FilterListModel(Object[] unfilteredList)
  {
    _fullList = unfilteredList;
    _filteredList = new ArrayList<Object>(Arrays.asList(unfilteredList));
  }

  public int getSize()
  {
    return _filteredList.size();
  }

  public Object getElementAt(int index)
  {
    return _filteredList.get(index);
  }
  
  public String getFilter()
  {
    return _filter;
  }
  
  public void setFilter(String filter)
  {
    _filteredList.clear();
    for(Object obj : _fullList)
    {
      if (obj.toString().length() < filter.length())
        continue;
      
      if (_caseSensitive)
      {
        if (obj.toString().startsWith(filter))
          _filteredList.add(obj);
      }
      else
      {
        if (obj.toString().substring(0, filter.length()).compareToIgnoreCase(filter) == 0)
          _filteredList.add(obj);
      }      
    }
    fireContentsChanged(this, 0, _filteredList.size());
  }
  
  public void clearFilter()
  {
    _filter = null;
    _filteredList = new ArrayList<Object>(Arrays.asList(_fullList));
  }
  
  public boolean getCaseSensitive()
  {
    return _caseSensitive;
  }

  public void setCaseSensitive(boolean caseSensitive)
  {
    _caseSensitive = caseSensitive;
    clearFilter();
  }
  
  public void setCompleterMatches(Object[] objectsToMatch)
  {
    _fullList = objectsToMatch;
    clearFilter();
  }

  private Object[] _fullList;
  private ArrayList<Object> _filteredList;
  private String _filter;
  private boolean _caseSensitive = false;
}
