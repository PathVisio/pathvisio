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

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class CompleterTextField extends JTextField
{
	/**
	 * default constructor shows the completer window when offering matches.
	 * @param completeMatches
	 */
	public CompleterTextField(Object[] completeMatches)
	{
		super();
		_filter = getFilter();

		_init();
	}

	protected CompleterFilter getFilter() {
		if(_filter == null) {
			_filter = new CompleterFilter(new Object[0], this);
		}
		return _filter;
	}

	/**
	 * useWindow - true will popup the completer window to help with matches,
	 * false will just complete in the textfield with no window.
	 */
	public CompleterTextField(Object[] completeMatches, boolean useWindow)
	{
		super();
		if (useWindow)
			_filter = new CompleterFilterWithWindow(completeMatches, this);
		else
			_filter = new CompleterFilter(completeMatches, this);
	}

	private void _init()
	{
		PlainDocument pd = new PlainDocument();
		pd.setDocumentFilter(getFilter());
		setDocument(pd);
	}

	@Override
	/**
	 * Warning: Calling setDocument on a completerTextField will remove the completion
	 * mecanhism for this text field if the document is not derived from AbstractDocument.
	 *
	 *  Only AbstractDocuments support the required DocumentFilter API for completion.
	 */
	public void setDocument(Document doc)
	{
		super.setDocument(doc);

		if (doc instanceof AbstractDocument)
			((AbstractDocument)doc).setDocumentFilter(_filter);
	}

	public boolean isCaseSensitive()
	{
		return _filter.isCaseSensitive();
	}

	public boolean isCorrectingCase()
	{
		return _filter.isCorrectingCase();
	}

	public void setCaseSensitive(boolean caseSensitive)
	{
		_filter.setCaseSensitive(caseSensitive);
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
		_filter.setCorrectCase(correctCase);
	}

	/**
	 * Set the list of objects to match against.
	 * @param completeMatches
	 */
	public void setCompleterMatches(Object[] completeMatches)
	{
		_filter.setCompleterMatches(completeMatches);
	}

	public void setText(String t) {
		//Don't auto-complete on setText
		_filter.setPerformCompletion(false);
		super.setText(t);
		_filter.setPerformCompletion(true);
	}

	private CompleterFilter _filter;

}
