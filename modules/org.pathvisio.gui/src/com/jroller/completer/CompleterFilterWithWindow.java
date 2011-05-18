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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;


public class CompleterFilterWithWindow extends CompleterFilter
{

	public CompleterFilterWithWindow(Object[] completerObjs, JTextComponent textField)
	{
		super(completerObjs, textField);
		_init();
	}

	@Override
	public void insertString(FilterBypass filterBypass, int offset, String string, AttributeSet attributeSet) throws BadLocationException
	{
		setFilterWindowVisible(false);
		super.insertString(filterBypass, offset, string, attributeSet);
	}

	@Override
	public void remove(FilterBypass filterBypass, int offset, int length) throws BadLocationException
	{
		setFilterWindowVisible(false);
		super.remove(filterBypass, offset, length);
	}

	@Override
	public void replace(FilterBypass filterBypass, int offset, int length, String string, AttributeSet attributeSet) throws BadLocationException
	{
		if(!isPerformCompletion()) {
			super.replace(filterBypass, offset, length, string, attributeSet);
			return;
		}

		if (_isAdjusting)
		{
			filterBypass.replace(offset, length, string, attributeSet);
			return;
		}

		super.replace(filterBypass, offset, length, string, attributeSet);

		if (getLeadingSelectedIndex() == -1)
		{
			if (isFilterWindowVisible())
				setFilterWindowVisible(false);

			return;
		}

		_lm.setFilter(_preText);

		if (!isFilterWindowVisible())
			setFilterWindowVisible(true);
		else
			_setWindowHeight();

		_list.setSelectedValue(_textField.getText(), true);
	}

	private void _init()
	{
		_fwl = new FilterWindowListener();
		_lm = new FilterListModel(_objectList);
		_tfkl = new TextFieldKeyListener();
		_textField.addKeyListener(_tfkl);

		EscapeAction escape = new EscapeAction();
		_textField.registerKeyboardAction(escape, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	public boolean isFilterWindowVisible()
	{
		return ((_win != null) && (_win.isVisible()));
	}

	public void setCaseSensitive(boolean caseSensitive)
	{
		super.setCaseSensitive(caseSensitive);
		_lm.setCaseSensitive(caseSensitive);
	}

	public void setFilterWindowVisible(boolean visible)
	{
		if (visible)
		{
			_initWindow();
			_list.setModel(_lm);
			_win.setVisible(true);
			_textField.requestFocus();
			_textField.addFocusListener(_fwl);
		}
		else
		{
			if (_win == null)
				return;

			_win.setVisible(false);
			_win.removeFocusListener(_fwl);
			Window ancestor = SwingUtilities.getWindowAncestor(_textField);
			ancestor.removeMouseListener(_fwl);
			_textField.removeFocusListener(_fwl);
			_textField.removeAncestorListener(_fwl);
			_list.removeMouseListener(_lml);
			_list.removeListSelectionListener(_lsl);
			_lsl = null;
			_lml = null;
			_win.dispose();
			_win = null;
			_list = null;
		}
	}

	private void _initWindow()
	{
		Window ancestor = SwingUtilities.getWindowAncestor(_textField);
		_win = new JWindow(ancestor);
		_win.addWindowFocusListener(_fwl);
		_textField.addAncestorListener(_fwl);
		ancestor.addMouseListener(_fwl);
		_lsl = new ListSelListener();
		_lml = new ListMouseListener();

		_list = new JList(_lm);
		_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_list.setFocusable(false);
		_list.setPrototypeCellValue("Prototype");
		_list.addListSelectionListener(_lsl);
		_list.addMouseListener(_lml);

		_sp = new JScrollPane(_list,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		_sp.setFocusable(false);
		_sp.getVerticalScrollBar().setFocusable(false);

		_setWindowHeight();
		_win.setLocation(_textField.getLocationOnScreen().x, _textField.getLocationOnScreen().y + _textField.getHeight());
		_win.getContentPane().add(_sp);
	}

	private void _setWindowHeight()
	{
		int height = _list.getFixedCellHeight() * Math.min(MAX_VISIBLE_ROWS, _lm.getSize());
		height += _list.getInsets().top + _list.getInsets().bottom;
		height += _sp.getInsets().top + _sp.getInsets().bottom;

		_win.setSize(_textField.getWidth(), height);
		_sp.setSize(_textField.getWidth(), height); // bottom border fails to draw without this
	}

	public void setCompleterMatches(Object[] objectsToMatch)
	{
		boolean visible = isFilterWindowVisible();
		if (visible)
			setFilterWindowVisible(false);

		super.setCompleterMatches(objectsToMatch);
		_lm.setCompleterMatches(objectsToMatch);
		_lm.setFilter(_preText);
		if(visible)
			setFilterWindowVisible(true);

	}

	class EscapeAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			if (isFilterWindowVisible())
				setFilterWindowVisible(false);
		}
	}

	private class FilterWindowListener extends MouseAdapter
	implements AncestorListener, FocusListener, WindowFocusListener
	{
		public void ancestorMoved(AncestorEvent event)
		{
			setFilterWindowVisible(false);
		}
		public void ancestorAdded(AncestorEvent event)
		{
			setFilterWindowVisible(false);
		}
		public void ancestorRemoved(AncestorEvent event)
		{
			setFilterWindowVisible(false);
		}

		public void focusLost(FocusEvent e)
		{
			if (e.getOppositeComponent() != _win)
				setFilterWindowVisible(false);
		}

		public void focusGained(FocusEvent e){}

		public void windowLostFocus(WindowEvent e)
		{
			Window w = e.getOppositeWindow();

			if (w.getFocusOwner() != _textField)
				setFilterWindowVisible(false);
		}
		public void windowGainedFocus(WindowEvent e) {}

		@Override
		public void mousePressed(MouseEvent e)
		{
			setFilterWindowVisible(false);
		}
	}

	private class TextFieldKeyListener extends KeyAdapter
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			if (!((e.getKeyCode() == KeyEvent.VK_DOWN) ||
					(e.getKeyCode() == KeyEvent.VK_UP) ||
					((e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) && (isFilterWindowVisible())) ||
					((e.getKeyCode() == KeyEvent.VK_PAGE_UP) && (isFilterWindowVisible())) ||
					(e.getKeyCode() == KeyEvent.VK_ENTER)))
				return;

			if ((e.getKeyCode() == KeyEvent.VK_DOWN) && !isFilterWindowVisible())
			{
				_preText = _textField.getText();
				_lm.setFilter(_preText);

				if (_lm.getSize() > 0)
					setFilterWindowVisible(true);
				else
					return;
			}

			if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				if (isFilterWindowVisible())
					setFilterWindowVisible(false);

				_textField.setCaretPosition(_textField.getText().length());
				return;
			}

			int index = -1;

			if (e.getKeyCode() == KeyEvent.VK_DOWN)
				index = Math.min(_list.getSelectedIndex() + 1, _list.getModel().getSize()-1);
			else if (e.getKeyCode() == KeyEvent.VK_UP)
				index = Math.max(_list.getSelectedIndex() - 1, 0);
			else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP)
				index = Math.max(_list.getSelectedIndex() - MAX_VISIBLE_ROWS, 0);
			else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
				index = Math.min(_list.getSelectedIndex() + MAX_VISIBLE_ROWS, _list.getModel().getSize()-1);

			if (index == -1)
				return;

			_list.setSelectedIndex(index);
			_list.scrollRectToVisible(_list.getCellBounds(index, index));
		}
	}

	private class ListSelListener implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent e)
		{
			_isAdjusting = true;
			_textField.setText(_list.getSelectedValue().toString());
			_isAdjusting = false;
			_textField.select(_preText.length(), _textField.getText().length());
		}
	}

	private class ListMouseListener extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2)
				setFilterWindowVisible(false);
		}
	}

	protected FilterWindowListener _fwl;
	protected JWindow _win;
	protected TextFieldKeyListener _tfkl;
	protected ListSelListener _lsl;
	protected ListMouseListener _lml;
	protected JList _list;
	protected JScrollPane _sp;
	protected FilterListModel _lm;

	protected boolean _isAdjusting = false;

	public static int MAX_VISIBLE_ROWS = 8;
}
