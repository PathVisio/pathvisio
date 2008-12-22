package samples.bidbuy ;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Vector;

public class rfq extends JPanel {
  private vInterface         vv = new v3();

  private String             regServerURL    = null ;
  private TitledBorder       regServerBorder = null ;
  private JComboBox          regServerList = null ;
  private JButton            removeButton  = null ;
  private JTable             serverTable   = null ;
  private DefaultTableModel  tableModel    = null ;
  private JPanel             regListPanel  = null ;
  private JButton            refreshButton = null ;
  private JButton            pingButton    = null ;
  private JButton            selectAllButton = null ;
  private JButton            deselectAllButton = null ;
  private JButton            requestButton = null ;
  private JButton            registerButton = null ;
  private JButton            addServerButton = null ;
  private JButton            removeServerButton = null ;
  private JButton            unregisterServerButton = null ;
  private JPanel             purchasePanel = null ;
  private JComboBox          buyList       = null ;
  private JTextField         tServer, tQuantity, tAddress ;
  private JComboBox          tNumItems ;

  public  boolean            doAxis = true ;

  private static int CHECK_COLUMN = 0 ;
  private static int NAME_COLUMN  = 1 ;
  private static int URL_COLUMN   = 2 ;
  private static int TYPE_COLUMN  = 3 ;
  private static int WSDL_COLUMN  = 4 ;
  private static int STATE_COLUMN = 5 ;
  private static int QUOTE_COLUMN = 6 ;
  private static int NUM_COLUMNS  = 7 ;

  class MyTableModel extends DefaultTableModel {
    public MyTableModel(Object[] obj, int x) { super( obj, x); }
    public Class getColumnClass(int col) {
      if ( col == CHECK_COLUMN ) return( Boolean.class );
      return( super.getColumnClass(col) );
    }
  };

  public rfq() {
    setLayout( new BorderLayout() );

    // Do the Registration Server list area
    //////////////////////////////////////////////////////////////////////////
    JPanel regSelectPanel = new JPanel();
    regSelectPanel.setLayout( new BoxLayout(regSelectPanel, BoxLayout.X_AXIS) );
    regSelectPanel.setBorder( new EmptyBorder(5,5,5,5) );
    regSelectPanel.add( new JLabel( "Registration Server: " ) );
    regSelectPanel.add( regServerList = new JComboBox() );
    regSelectPanel.add( Box.createRigidArea(new Dimension(5,0)) );
    regSelectPanel.add( removeButton = new JButton("Remove") );

    loadRegList();

    regServerList.setEditable( true );
    regServerList.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          String act = event.getActionCommand();
          if ( act.equals( "comboBoxChanged" ) ) {
            String name = (String) regServerList.getSelectedItem();
            if ( name != null && !name.equals("") ) {
              chooseRegServer( name );
              addRegistrationServer( name );
            }
            else
              clearTable();
          }
        };
      });

    removeButton.setEnabled( true );
    removeButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Remove".equals(event.getActionCommand()) ) {
            String name = (String) regServerList.getSelectedItem();
            regServerList.removeItem( name );
            saveRegList();
          }
        };
      });

    add( regSelectPanel, BorderLayout.NORTH );

    // Do the List Of Registration Servers Table
    //////////////////////////////////////////////////////////////////////////
    regListPanel = new JPanel();
    regListPanel.setLayout( new BorderLayout() );
    regServerBorder = new TitledBorder(" Product Servers ");
    regListPanel.setBorder( regServerBorder );
    regListPanel.add( new JLabel( "Select the servers you want to request " +
                                  "a price from:"), BorderLayout.NORTH );
                
    tableModel = new MyTableModel( new Object[] {"", "Name", "URL", "Type", 
                                                 "WSDL", "State", "Quote", 
                                                 ""}, 0 );
    serverTable = new JTable( 0, NUM_COLUMNS );
    serverTable.setModel( tableModel );
    serverTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    TableColumn col = serverTable.getColumnModel().getColumn(CHECK_COLUMN);
    col.setMaxWidth( 10 );
    // col = serverTable.getColumnModel().getColumn(STATE_COLUMN);
    // col.setMaxWidth( col.getPreferredWidth()/2 );
    // col = serverTable.getColumnModel().getColumn(QUOTE_COLUMN);
    // col.setMaxWidth( col.getPreferredWidth()/2 );
    col = serverTable.getColumnModel().getColumn(TYPE_COLUMN);
    col.setMaxWidth( col.getPreferredWidth()/2 );

    tableModel.addTableModelListener( new TableModelListener() {
        public void tableChanged(TableModelEvent event) {
          int type = event.getType();
          if ( type == TableModelEvent.UPDATE && event.getColumn() == 0 )
            enableButtons();
        };
      });

    regListPanel.add( new JScrollPane(serverTable), BorderLayout.CENTER );

    JPanel  btns = new JPanel();
    // btns.setLayout( new BoxLayout( btns, BoxLayout.X_AXIS ) );
    btns.setLayout( new GridBagLayout() );

    GridBagConstraints  c = new GridBagConstraints();

    c.anchor    = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    c.fill = GridBagConstraints.HORIZONTAL ;

    // row 1
    btns.add( refreshButton = new JButton( "Refresh List" ), c );
    btns.add( Box.createRigidArea(new Dimension(5,0)), c );
    btns.add( selectAllButton = new JButton( "Select All" ), c );
    btns.add( Box.createRigidArea(new Dimension(5,0)), c );
    btns.add( requestButton = new JButton( "Request RFQs" ), c );
    c.weightx = 1.0 ;
    btns.add( Box.createHorizontalGlue(), c );
    c.weightx = 0.0 ;
    btns.add( registerButton = new JButton( "Register Server" ), c );
    btns.add( Box.createRigidArea(new Dimension(5,0)), c );
    c.gridwidth = GridBagConstraints.REMAINDER ;
    btns.add( addServerButton = new JButton( "Add Bid Server" ), c );

    // row 2
    c.gridwidth = 1 ;
    c.gridx = 2 ;
    btns.add( deselectAllButton = new JButton( "Deselect All" ), c );
    c.gridx = GridBagConstraints.RELATIVE ;
    btns.add( Box.createRigidArea(new Dimension(5,0)), c );
    btns.add( pingButton = new JButton( "Ping" ), c );
    c.weightx = 1.0 ;
    btns.add( Box.createRigidArea(new Dimension(5,0)), c );
    c.weightx = 0.0 ;
    btns.add( unregisterServerButton = new JButton( "Unregister Server" ), c );
    btns.add( Box.createRigidArea(new Dimension(5,0)), c );
    // btns.add( Box.createRigidArea(new Dimension(5,0)), c );
    c.gridwidth = GridBagConstraints.REMAINDER ;
    btns.add( removeServerButton = new JButton( "Remove Server" ), c );

    regListPanel.add( btns, BorderLayout.SOUTH );

    refreshButton.setEnabled( false );
    refreshButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Refresh List".equals(event.getActionCommand()) ) {
            refreshList();
          }
        };
      });

    selectAllButton.setEnabled( false );
    selectAllButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Select All".equals(event.getActionCommand()) ) {
            for ( int i = 0 ; i < tableModel.getRowCount() ; i++ ){
              tableModel.setValueAt(new Boolean(true),i,CHECK_COLUMN);
            }
          }
        };
      });

    deselectAllButton.setEnabled( false );
    deselectAllButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Deselect All".equals(event.getActionCommand()) ) {
            for ( int i = 0 ; i < tableModel.getRowCount() ; i++ ){
              tableModel.setValueAt(new Boolean(false),i,CHECK_COLUMN);
            }
          }
        };
      });

    pingButton.setEnabled( false );
    pingButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Ping".equals(event.getActionCommand()) ) {
            ping();
          }
        };
      });

    requestButton.setEnabled( false );
    requestButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Request RFQs".equals(event.getActionCommand()) ) {
            requestRFQs();
          }
        };
      });

    registerButton.setEnabled( false );
    registerButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Register Server".equals(event.getActionCommand()) ) 
            registerNewServer();
        };
      });

    addServerButton.setEnabled( true );
    addServerButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Add Bid Server".equals(event.getActionCommand()) )
            promptForServer();
        };
      });

    removeServerButton.setEnabled( false );
    removeServerButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Remove Server".equals(event.getActionCommand()) )
            removeServers();
        };
      });

    unregisterServerButton.setEnabled( false );
    unregisterServerButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Unregister Server".equals(event.getActionCommand()) )
            unregisterServer();
        };
      });


    // Purchase data
    //////////////////////////////////////////////////////////////////////////
    GridBagLayout       layout        = new GridBagLayout();
    // GridBagConstraints  c             = new GridBagConstraints();
    c             = new GridBagConstraints();

    purchasePanel = new JPanel(layout);
    purchasePanel.setBorder( new TitledBorder("Purchase") );

    JButton    tSimpleBuy ;
    JButton    tPOBuy ;

    c.anchor    = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    purchasePanel.add( new JLabel("Select the purchase server from the " +
                                  "combo box" ),c );

    c.anchor    = GridBagConstraints.EAST ;
    c.gridwidth = 1 ;
    purchasePanel.add( new JLabel("Server:"), c );

    c.anchor    = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    purchasePanel.add( buyList = new JComboBox(), c );

    c.gridwidth = 1 ;
    c.anchor    = GridBagConstraints.EAST ;
    purchasePanel.add( new JLabel("Quantity:"),c );

    c.anchor    = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    purchasePanel.add( tQuantity = new JTextField(6), c );
    tQuantity.setText("1");

    c.anchor    = GridBagConstraints.EAST ;
    c.gridwidth = 1 ;
    purchasePanel.add( new JLabel("# Line Items:"),c );

    c.anchor    = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    purchasePanel.add( tNumItems = new JComboBox(), c );

    c.anchor    = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    purchasePanel.add( tSimpleBuy = new JButton( "Simple Buy" ) );

    c.anchor    = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    purchasePanel.add( tPOBuy = new JButton( "PO Buy" ) );

    for ( int j = 1 ; j < 20 ; j++ )
      tNumItems.addItem( ""+j );

    tSimpleBuy.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Simple Buy".equals(event.getActionCommand()) ) {
            simpleBuy();
          }
        };
      });

    tPOBuy.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "PO Buy".equals(event.getActionCommand()) ) {
            poBuy();
          }
        };
      });

    JSplitPane  splitPane = new JSplitPane( 0, regListPanel, 
                                               new JScrollPane(purchasePanel));
    add( splitPane, BorderLayout.CENTER );
    setSize(getPreferredSize());
    splitPane.setDividerLocation( 200 );
    purchasePanel.setEnabled( false );
  }

  public void addRegistrationServer( String name ) {
    int     count, i ;

    if ( name == null || "".equals(name) ) return ;
    count = regServerList.getItemCount();
    for ( i = 0 ; i < count ; i++ ) 
      if ( name.equals( regServerList.getItemAt(i) ) ) return ;
    regServerList.addItem( name );
    saveRegList();
  }

  public void chooseRegServer(String name) {
    regServerURL = name ;
    regServerBorder.setTitle( " Product Servers at ' " + name + " ' " );
    regListPanel.repaint();
    refreshList();
  }

  public void enableButtons() {
    boolean flag ;
    int i ;
    int total = tableModel.getRowCount();
    int count = 0 ;
    for ( i = 0 ; i < total ; i++ ) {
      flag = ((Boolean)tableModel.getValueAt(i,CHECK_COLUMN)).booleanValue();
      if ( flag ) count++ ;
    }
    selectAllButton.setEnabled( total > 0 && count != total );
    deselectAllButton.setEnabled( total > 0 && count > 0 );
    pingButton.setEnabled( count > 0 );
    requestButton.setEnabled( count > 0 );
    removeServerButton.setEnabled( count > 0 );
    unregisterServerButton.setEnabled( count > 0 );
  }

  public void clearTable() {
    while ( tableModel.getRowCount() > 0 )
      tableModel.removeRow(0);
    enableButtons();
  }

  public void refreshList() {
    clearTable();

    Vector services = null ;

    try {
      services = vv.lookupAsString(regServerURL);
    }
    catch( Exception e ) {
      System.err.println("---------------------------------------------");
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, e.toString(), "Error",
                                    JOptionPane.INFORMATION_MESSAGE );
    }

    for ( int i = 0 ; services != null && i < services.size() ; i++ ) {
      Service s = (Service) services.elementAt(i);
      addServer( s );
    }

    buyList.removeAllItems();
    purchasePanel.setEnabled( false );
    refreshButton.setEnabled( true );
    registerButton.setEnabled( true );
    enableButtons();
  }

  public void ping() {
    Boolean  flag ;
    int      i;

    for ( i = 0 ; i < tableModel.getRowCount() ; i++ ) {
      flag = (Boolean) tableModel.getValueAt( i, CHECK_COLUMN );
      if ( flag.booleanValue() ) {
        String url = (String) tableModel.getValueAt( i, URL_COLUMN );
        Boolean value = new Boolean(false);

        try {
          value = vv.ping( url );

          tableModel.setValueAt( value.booleanValue() ? "Alive" : "Down", 
                                 i, STATE_COLUMN );
          serverTable.repaint();
        }
        catch( Exception e ) {
          JOptionPane.showMessageDialog(this, e.toString(), "Error",
                                        JOptionPane.INFORMATION_MESSAGE );
        }

      }
    }
  }

  public void unregisterServer() {
    Boolean  flag ;
    int      i;

    for ( i = 0 ; i < tableModel.getRowCount() ; i++ ) {
      flag = (Boolean) tableModel.getValueAt( i, CHECK_COLUMN );
      if ( flag.booleanValue() ) {
        String name = (String) tableModel.getValueAt( i, NAME_COLUMN );
        String regServer =  (String) regServerList.getSelectedItem() ;
        Boolean value = new Boolean(false);

        try {
          vv.unregister( regServer, name);
        }
        catch( Exception e ) {
          JOptionPane.showMessageDialog(this, e.toString(), "Error",
                                        JOptionPane.INFORMATION_MESSAGE );
        }

      }
    }
    refreshList();
  }

  public void requestRFQs() {
    Boolean  flag ;
    int      i, j;

    // buyList.removeAllItems();

    for ( i = 0 ; i < tableModel.getRowCount() ; i++ ) {
      flag = (Boolean) tableModel.getValueAt( i, CHECK_COLUMN );
      if ( flag.booleanValue() ) {
        String url = (String) tableModel.getValueAt( i, URL_COLUMN );
        double value = 0.0 ;

        try {
          value = vv.requestForQuote( url );

          tableModel.setValueAt( new Double(value), i, QUOTE_COLUMN );
          serverTable.repaint();

          String str = (String) tableModel.getValueAt(i, NAME_COLUMN);
          for ( j = 0 ; j < buyList.getItemCount(); j++ ) 
            if ( ((String)buyList.getItemAt(j)).equals(str) ) break ;
          if ( j == buyList.getItemCount() )
            buyList.addItem( str );
        }
        catch( Exception e ) {
          JOptionPane.showMessageDialog(this, e.toString(), "Error",
                                        JOptionPane.INFORMATION_MESSAGE );
        }

      }
    }
    // buyList.setSelectedIndex(-1);
    purchasePanel.setEnabled( true );
  }

  public void removeServers() {
    Boolean  flag ;
    int      i, j;

    for ( i = tableModel.getRowCount()-1 ; i >= 0 ; i-- ) {
      flag = (Boolean) tableModel.getValueAt( i, CHECK_COLUMN );
      if ( flag.booleanValue() )
        tableModel.removeRow( i );
    }
    enableButtons();
  }

  public void simpleBuy() {
    try {
      String url = null ;
      int total = tableModel.getRowCount();
      String name = (String) buyList.getSelectedItem();
      for ( int i = 0 ; i < total ; i++ ) {
        String val = (String) tableModel.getValueAt(i, NAME_COLUMN) ;
        if ( val.equals(name) ) {
          url = (String) tableModel.getValueAt(i, URL_COLUMN);
          break ;
        }
      }
      String address = "123 Main Street, Any Town, USA" ;
      String product = "soap" ;
      int quantity = Integer.parseInt((String) tQuantity.getText());
      String value = null ;

      value = vv.simpleBuy( url, quantity );

      JOptionPane.showMessageDialog(this, value, "Receipt",
                                    JOptionPane.INFORMATION_MESSAGE );
    }
    catch( Exception e ) {
      JOptionPane.showMessageDialog(this, e.toString(), "Error",
                                    JOptionPane.INFORMATION_MESSAGE );
    }
  }

  public void poBuy() {
    try {
      String url = null ;
      int total = tableModel.getRowCount();
      String name = (String) buyList.getSelectedItem();
      double price = 0 ;
      for ( int i = 0 ; i < total ; i++ ) {
        String val = (String) tableModel.getValueAt(i, NAME_COLUMN) ;
        Double dval ;
        if ( val.equals(name) ) {
          url = (String) tableModel.getValueAt(i, URL_COLUMN);
          dval = (Double) tableModel.getValueAt(i, QUOTE_COLUMN);
          price = dval.doubleValue();
          break ;
        }
      }
      // String address = (String) tAddress.getText();
      String product = "soap" ;
      int quantity = Integer.parseInt((String) tQuantity.getText());
      int numItems = Integer.parseInt((String) tNumItems.getSelectedItem());
      String value = null ;

      value = vv.buy( url, quantity, numItems, price );

      JOptionPane.showMessageDialog(this, value, "Receipt",
                                    JOptionPane.INFORMATION_MESSAGE );
    }
    catch( Exception e ) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, e.toString(), "Error",
                                    JOptionPane.INFORMATION_MESSAGE );
    }
  }

  public void registerNewServer() {
    Component parent = this ;
    while ( parent != null && !(parent instanceof JFrame) )
      parent = parent.getParent();
    final JDialog j = new JDialog((JFrame)parent, "Register Server", true );
    Container  pane = j.getContentPane();
    final JTextField  fName = new JTextField(20), 
                      fURL  = new JTextField(20), 
                      fType = new JTextField(20), 
                      fWsdl = new JTextField(20);
    JButton     regButton, cancelButton ; 

    pane.setLayout( new GridBagLayout() );
    GridBagConstraints c = new GridBagConstraints();

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( new JLabel( "Service Name" ), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    pane.add( fName, c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( new JLabel( "Service URL" ), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    pane.add( fURL, c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( new JLabel( "Service Type" ), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    pane.add( fType, c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( new JLabel( "WSDL URL" ), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    pane.add( fWsdl, c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( regButton = new JButton( "Register" ), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( Box.createHorizontalStrut(3), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( cancelButton = new JButton( "Cancel" ), c );

    fType.setText( "Bid" );

    regButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Register".equals(event.getActionCommand()) ) {
            Service s = new Service();
            s.setServiceName( fName.getText() );
            s.setServiceUrl( fURL.getText() );
            s.setServiceType( fType.getText() );
            s.setServiceWsdl( fWsdl.getText() );
            register( s );
            j.dispose();
          }
        };
      });

    cancelButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Cancel".equals(event.getActionCommand()) ) {
            j.dispose();
          }
        };
      });

    j.pack();
    Point p = new Point( parent.getLocation() );
    Dimension d = parent.getSize();
    p.setLocation( (int)(p.getX() + d.getWidth()/2), 
                   (int)(p.getY() + d.getHeight()/2) );
    d = j.getSize();
    j.setLocation( (int)(p.getX() - d.getWidth()/2), 
                   (int)(p.getY() - d.getHeight()/2) );
    j.show();
  }


  public void promptForServer() {
    Component parent = this ;
    while ( parent != null && !(parent instanceof JFrame) )
      parent = parent.getParent();
    final JDialog j = new JDialog((JFrame)parent, "Add Bid Server", true );
    Container  pane = j.getContentPane();
    final JTextField  fName = new JTextField(20), 
                      fURL  = new JTextField(20), 
                      fType = new JTextField(20), 
                      fWsdl = new JTextField(20);
    JButton     addButton, cancelButton ; 

    pane.setLayout( new GridBagLayout() );
    GridBagConstraints c = new GridBagConstraints();

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( new JLabel( "Service Name" ), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    pane.add( fName, c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( new JLabel( "Service URL" ), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    pane.add( fURL, c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( new JLabel( "Service Type" ), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    pane.add( fType, c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( new JLabel( "WSDL URL" ), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = GridBagConstraints.REMAINDER ;
    pane.add( fWsdl, c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( addButton = new JButton( "Add" ), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( Box.createHorizontalStrut(3), c );

    c.anchor = GridBagConstraints.WEST ;
    c.gridwidth = 1 ;
    pane.add( cancelButton = new JButton( "Cancel" ), c );

    fType.setText( "Bid" );

    addButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Add".equals(event.getActionCommand()) ) {
            Service s = new Service();
            s.setServiceName( fName.getText() );
            s.setServiceUrl( fURL.getText() );
            s.setServiceType( fType.getText() );
            s.setServiceWsdl( fWsdl.getText() );
            addServer( s );
            j.dispose();
          }
        };
      });

    cancelButton.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if ( "Cancel".equals(event.getActionCommand()) ) {
            j.dispose();
          }
        };
      });

    j.pack();
    Point p = new Point( parent.getLocation() );
    Dimension d = parent.getSize();
    p.setLocation( (int)(p.getX() + d.getWidth()/2), 
                   (int)(p.getY() + d.getHeight()/2) );
    d = j.getSize();
    j.setLocation( (int)(p.getX() - d.getWidth()/2), 
                   (int)(p.getY() - d.getHeight()/2) );
    j.show();
  }

  public void addServer(Service s) {
    Object[]  objs = new Object[NUM_COLUMNS] ;
    objs[0] = new Boolean(false);
    objs[1] = s.getServiceName();
    objs[2] = s.getServiceUrl();
    objs[3] = s.getServiceType();
    objs[4] = s.getServiceWsdl();
    objs[5] = null ;
    objs[6] = null ;

    tableModel.addRow( objs );
  }

  public void register(Service s) {
    try {
      vv.register( (String) regServerList.getSelectedItem(), s );
      refreshList();
    }
    catch( Exception e ) {
      JOptionPane.showMessageDialog(this, e.toString(), "Error",
                                    JOptionPane.INFORMATION_MESSAGE );
    }
  }

  public void loadRegList() {
    try {
      FileReader fr = new FileReader( "reg.lst" );
      LineNumberReader lnr = new LineNumberReader( fr );
      String  line = null ;

      while ( (line = lnr.readLine()) != null )
        addRegistrationServer( line );
      fr.close();
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
  }

  public void saveRegList() {
    try {
      FileOutputStream fos = new FileOutputStream( "reg.lst" );
      PrintWriter      pw  = new PrintWriter( fos );
      int              count = regServerList.getItemCount();
      int              i ;

      for ( i = 0 ; i < count ; i++ ) 
        pw.println( (String) regServerList.getItemAt(i) );
      pw.close();

      fos.close();
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      JFrame  window = new JFrame("Request For Quote Client") {
          protected void processWindowEvent(WindowEvent event) {
            switch( event.getID() ) {
              case WindowEvent.WINDOW_CLOSING: exit();
                                               break ;
              default: super.processWindowEvent(event);
                       break ;
            }
          }
          private void exit() {
            System.exit(0);
          }
        };

      window.getContentPane().add( new rfq() );
      window.pack();
      window.setSize( new Dimension(800, 500) );
      window.setVisible( true );
    }
    catch( Throwable exp ) {
      exp.printStackTrace();
    }
  }
}
