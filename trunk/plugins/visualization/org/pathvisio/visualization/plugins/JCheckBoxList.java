package org.pathvisio.visualization.plugins;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
 
public class JCheckBoxList extends JList
{
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
 
    public JCheckBoxList()
    {
        setCellRenderer(new CheckBoxCellRenderer());
 
        addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                int index = locationToIndex(e.getPoint());
                if (index != -1)
                {
                    JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
                    checkbox.setSelected(!checkbox.isSelected());
                    repaint();
                }
            }
        });
 
        addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_SPACE)
                {
                    int index = getSelectedIndex();
                    if (index != -1)
                    {
                        JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
                        checkbox.setSelected(!checkbox.isSelected());
                        repaint();
                    }
                }
            }
        });
 
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
 
    protected class CheckBoxCellRenderer implements ListCellRenderer
    {
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus)
        {
            JCheckBox checkbox = (JCheckBox) value;
            checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
            checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
 
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusPainted(false);
 
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
 
            return checkbox;
        }
    }
}
