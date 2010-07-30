/*
 * Copyright Neil Cochrane 2006
 * @author neilcochrane
 *
 * Feel free to use the code without restriction
 * in open source projects.
 * There is no cost or warranty attached and the code is
 * provided as is.
 *
 * http://www.jroller.com/swinguistuff/entry/text_completion
 */
package com.jroller.completer;


import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

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
    public CompleterFilter(Object[] completerObjs, JTextComponent textField)
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
    public JTextComponent getTextComponent()
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

    protected JTextComponent _textField;
    protected Object[]   _objectList;
}
