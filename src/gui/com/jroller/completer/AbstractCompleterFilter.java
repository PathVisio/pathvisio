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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

abstract public class AbstractCompleterFilter extends DocumentFilter
{
  abstract public int getCompleterListSize();

  abstract public Object getCompleterObjectAt(int i);

  abstract public JTextComponent getTextComponent();

  private boolean performCompletion = false;
  public void setPerformCompletion(boolean perform) {
	  performCompletion = perform;
  }

  public boolean isPerformCompletion() {
	  return performCompletion;
  }

  public void replace(FilterBypass filterBypass, int offset, int length, String string, AttributeSet attributeSet)
    throws BadLocationException
  {
      super.replace(filterBypass, offset, length, string, attributeSet);

      if(!isPerformCompletion()) return; //Skip completion flag

      Document doc = filterBypass.getDocument();
      _preText = doc.getText(0,doc.getLength());
      _firstSelectedIndex = -1;

      for (int i=0; i< getCompleterListSize(); i++)
      {
          String objString = getCompleterObjectAt(i).toString();

          if ((_case)
            ? objString.equals(_preText)
            : objString.equalsIgnoreCase(_preText))
          {
            _firstSelectedIndex = i;

            if (_corrective)
              filterBypass.replace(0, _preText.length(), objString, attributeSet);
            break;
          }

          if (objString.length() <= _preText.length())
            continue;

          String objStringStart = objString.substring(0, _preText.length());

          if ((_case)
            ? objStringStart.equals(_preText)
            : objStringStart.equalsIgnoreCase(_preText))
          {
              String objStringEnd = objString.substring(_preText.length());
              if (_corrective)
                  filterBypass.replace(0, _preText.length(), objString, attributeSet);
              else
                  filterBypass.insertString(_preText.length(), objStringEnd, attributeSet);

              getTextComponent().select(_preText.length(), doc.getLength());
              _firstSelectedIndex = i;
              break;
          }
      }
  }

  public void insertString(FilterBypass filterBypass, int offset, String string, AttributeSet attributeSet)
    throws BadLocationException
  {
      super.insertString(filterBypass, offset, string, attributeSet);
  }

  public void remove(FilterBypass filterBypass, int offset, int length)
    throws BadLocationException
  {
      super.remove(filterBypass, offset, length);
  }

  public void setCaseSensitive(boolean caseSensitive)
  {
      _case = caseSensitive;
  }

  public boolean isCaseSensitive()
  {
      return _case;
  }

  /**
   * Will change the user entered part of the string to match the case of the matched item.
   *
   * e.g.
   * "europe/lONdon" would be corrected to "Europe/London"
   *
   * This option only makes sense if case sensitive is turned off
   */
  public void setCorrectCase(boolean correctCase)
  {
    _corrective = correctCase;
  }

  public boolean isCorrectingCase()
  {
      return _corrective;
  }

  /**
   *
   * @return the index of the first object in the object array that can match
   * the user entered string (-1 if no object is currently being used as a match)
   */
  public int getLeadingSelectedIndex()
  {
    return _firstSelectedIndex;
  }

  protected String     _preText; // The text in the input field before we started last looking for matches.

  protected boolean _case       = false;
  protected boolean _corrective = true;
  protected int _firstSelectedIndex = -1;
}
