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


import javax.swing.JTextField;

/**
 * A filter that will attempt to autocomplete enties into a textfield with the string representations
 * of objects in a given array.
 * 
 * Add this filter class to the Document of the text field.
 * 
 * The first match in the array is the one used to autocomplete. So sort your array by most important
 * objects first.
 * @author neilcochrane
 */
public class CompleterFilter extends AbstractCompleterFilter
{
    /** Creates a new instance of CompleterFilter
     * @param completerObjs an array of objects used to attempt completion
     * @param textField the text component to receive the completion
     */
    public CompleterFilter(Object[] completerObjs, JTextField textField)
    {
        _objectList = completerObjs;
        _textField = textField;
    }   
    
    @Override
    public int getCompleterListSize()
    {
      return _objectList.length;
    }

    @Override
    public Object getCompleterObjectAt(int i)
    {
      return _objectList[i];
    }

    @Override
    public JTextField getTextField()
    {
      return _textField;
    }

    /**
     * Set the list of objects to match against.
     * @param objectsToMatch
     */
    public void setCompleterMatches(Object[] objectsToMatch)
    {
      _objectList = objectsToMatch;
      _firstSelectedIndex = -1;
    }
        
    protected JTextField _textField;
    protected Object[]   _objectList;
}
