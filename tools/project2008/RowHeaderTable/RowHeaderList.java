package RowHeaderTable;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.*;

import java.util.Vector;


public class RowHeaderList
{
    public static void main(String[] args)
    {
        DefaultListModel list = new DefaultListModel();
        DefaultTableModel data = new DefaultTableModel(0, 6);

        for (int i = 0; i < 30; i++)
        {
            list.addElement("Line: "+i);

            Vector v = new Vector();

            for (int k = 0; k < 6; k++)
                v.add(new Float(k / (float)i));

            data.addRow(v);
        }

        JTable table = new JTable(data);

        JList rowHeader = new JList(list);

        LookAndFeel.installColorsAndFont
            (rowHeader, "TableHeader.background", 
            "TableHeader.foreground", "TableHeader.font");


        rowHeader.setFixedCellHeight(table.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        
        scrollPane.setRowHeaderView(rowHeader);


        new JScrollPaneAdjuster(scrollPane);


        JFrame f = new JFrame("Row Header Test");

        f.getContentPane().add(scrollPane, BorderLayout.CENTER);

        f.pack();
        f.setLocation(200, 100);
        f.setVisible(true);
    }
}


