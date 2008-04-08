package RowHeaderTable;
//URL: http://www.chka.de/swing/table/row-headers/

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.*;

import java.util.Vector;


public class RowHeaderTable{

    public static void main(String[] args){
    
        DefaultTableModel headerData = new DefaultTableModel(0, 1);
        DefaultTableModel data = new DefaultTableModel(0, 6);

        for (int i = 0; i < 30; i++)
        {
            headerData.addRow(new Object[] { "Line: "+i } );

            Vector v = new Vector();

            for (int k = 0; k < 6; k++)
                v.add(new Float(k / (float)i));

            data.addRow(v);
        }

        JTable table = new JTable(data);

        JTable rowHeader = new JTable(headerData);

        LookAndFeel.installColorsAndFont
            (rowHeader, "TableHeader.background", 
            "TableHeader.foreground", "TableHeader.font");

        
        rowHeader.setIntercellSpacing(new Dimension(0, 0));
        Dimension d = rowHeader.getPreferredScrollableViewportSize();
        d.width = rowHeader.getPreferredSize().width;
        rowHeader.setPreferredScrollableViewportSize(d);
        rowHeader.setRowHeight(table.getRowHeight());
        rowHeader.setDefaultRenderer(Object.class, new RowHeaderRenderer());
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        scrollPane.setRowHeaderView(rowHeader);

        JTableHeader corner = rowHeader.getTableHeader();
        corner.setReorderingAllowed(false);
        corner.setResizingAllowed(false);

        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner);

        new JScrollPaneAdjuster(scrollPane);

        new JTableRowHeaderResizer(scrollPane).setEnabled(true); 

        JFrame f = new JFrame("Row Header Test");

        f.getContentPane().add(scrollPane, BorderLayout.CENTER);

        f.pack();
        f.setLocation(200, 100);
        f.setVisible(true);
    }
}
