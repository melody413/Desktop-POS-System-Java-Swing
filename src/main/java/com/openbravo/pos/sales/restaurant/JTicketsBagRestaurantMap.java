//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright © 2009-2020 uniCenta & previous Openbravo POS works
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.sales.restaurant;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.openbravo.basic.BasicException;
import com.openbravo.beans.JCashOutDialog;
import com.openbravo.beans.JClockedInUserDialog;
import com.openbravo.beans.JClockedOutUserDialog;
import com.openbravo.beans.JCompletedOrderDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.NullIcon;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.JTicketsBag;
import com.openbravo.pos.sales.SharedTicketInfo;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.sales.shared.JTicketsBagSharedList;
import com.openbravo.pos.ticket.TicketInfo;
import lombok.extern.slf4j.Slf4j;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.format.Formats;
import com.openbravo.pos.panels.JPanelTips;
import com.openbravo.pos.panels.PaymentTipsModel;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.sales.ClosedTicketInfo;
import com.openbravo.pos.sales.JPanelTicket;
import com.openbravo.pos.sales.shared.JTicketsBagClosedList;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.List;
/**
 *
 * @author JG uniCenta
 */
@Slf4j
public class JTicketsBagRestaurantMap extends JTicketsBag {

    private static class ServerCurrent {
        public ServerCurrent() {
        }
    }

    private java.util.List<Place> m_aplaces;
    private java.util.List<Floor> m_afloors;
    
    private JTicketsBagRestaurant m_restaurantmap;
        
    private final JTicketsBagRestaurantRes m_jreservations;
    private final JPanelTips m_jPanelTips;
    private Place m_PlaceCurrent;
    private ServerCurrent m_ServerCurrent;
    private Place m_PlaceClipboard;  
    private CustomerInfo customer;

    private DataLogicReceipts dlReceipts = null;
    private DataLogicSales dlSales = null;
    private DataLogicSystem dlSystem = null;    
    private final RestaurantDBUtils restDB;
    private static final Icon ICO_OCU_SM = new ImageIcon(Place.class.getResource("/com/openbravo/images/edit_group_sm.png"));
    private static final Icon ICO_WAITER = new NullIcon(1, 1);  
    private static final Icon ICO_FRE = new NullIcon(22, 22);
    private String waiterDetails;
    private String customerDetails;
    private String tableName;
    private Boolean transBtns;
    private Boolean actionEnabled = true;    
    private int newX;
    private int newY;
    private AppView m_app_map;
    private Boolean showLayout = false;
    private String m_sCurrentTicket = null;
    private TicketsEditor m_panelticket;
    private boolean isOpenStore = false;
    private JPanel m_jPlaces = null;
    private TicketParser m_TTP;
        
    /** Creates new form JTicketsBagRestaurant
     * @param app
     * @param panelticket */

    public JTicketsBagRestaurantMap(AppView app, TicketsEditor panelticket) {
        
        super(app, panelticket);
        m_panelticket = panelticket;
        m_app_map = app;
        restDB = new  RestaurantDBUtils(app);        
        transBtns = AppConfig.getInstance().getBoolean("table.transbtn");
        
        dlReceipts = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");        
        
        m_restaurantmap = new JTicketsBagRestaurant(app, this);
        m_PlaceCurrent = null;
        m_PlaceClipboard = null;
        customer = null;

        m_TTP = new TicketParser(m_App.getDeviceTicket(), dlSystem);
            
        try {
            SentenceList sent = new StaticSentence(
                app.getSession(), 
                "SELECT ID, NAME, IMAGE FROM floors ORDER BY NAME", 
                null, 
                new SerializerReadClass(Floor.class));
            m_afloors = sent.list();
               
        } catch (BasicException eD) {
            m_afloors = new ArrayList<>();
        }
        try {
            SentenceList sent = new StaticSentence(
                app.getSession(), 
                "SELECT ID, NAME, SEATS, X, Y, FLOOR, CUSTOMER, WAITER, " 
                    + "TICKETID, TABLEMOVED, WIDTH, HEIGHT, GUESTS, OCCUPIED "
                    + "FROM places "
                    + "WHERE x<>0 AND y<>0 "
                    + "ORDER BY FLOOR ",
                null, 
                new SerializerReadClass(Place.class));
            m_aplaces = sent.list();
        } catch (BasicException eD) {
            m_aplaces = new ArrayList<>();
        } 
        
        initComponents(); 
                
        if (m_afloors.size() > 1) {
            JTabbedPane jTabFloors = new JTabbedPane();
            jTabFloors.applyComponentOrientation(getComponentOrientation());
            jTabFloors.setBorder(new javax.swing.border.EmptyBorder(new Insets(5, 5, 5, 5)));
            jTabFloors.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            jTabFloors.setFocusable(false);
            jTabFloors.setRequestFocusEnabled(false);
            m_jPanelMap.add(jTabFloors, BorderLayout.CENTER);
            
            m_afloors.stream().map((f) -> {
                f.getContainer().applyComponentOrientation(getComponentOrientation());
                return f;                
            }).forEach((f) -> {
                JScrollPane jScrCont = new JScrollPane();
                jScrCont.applyComponentOrientation(getComponentOrientation());
                JPanel jPanCont = new JPanel();  
                jPanCont.applyComponentOrientation(getComponentOrientation());
                
                jTabFloors.addTab(f.getName(), f.getIcon(), jScrCont);     
                jScrCont.setViewportView(jPanCont);
                jPanCont.add(f.getContainer());
            });
        } else if (m_afloors.size() == 1) {
            Floor f = m_afloors.get(0);
            f.getContainer().applyComponentOrientation(getComponentOrientation());
            
            JPanel jPlaces = new JPanel();
            jPlaces.applyComponentOrientation(getComponentOrientation());
            jPlaces.setLayout(new BorderLayout());
            jPlaces.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.EmptyBorder(new Insets(5, 5, 5, 5)),
                new javax.swing.border.TitledBorder(f.getName())));
            
            JScrollPane jScrCont = new JScrollPane();
            jScrCont.applyComponentOrientation(getComponentOrientation());
            JPanel jPanCont = new JPanel();
            jPanCont.applyComponentOrientation(getComponentOrientation());
            
            m_jPanelMap.add(jPlaces, BorderLayout.CENTER);
            jPlaces.add(jScrCont, BorderLayout.CENTER);
            jScrCont.setViewportView(jPanCont);            
            jPanCont.add(f.getContainer());

            m_jPlaces = jPlaces;
        }   
        
        Floor currfloor = null;
        
        for (Place pl : m_aplaces) {
            int iFloor = 0;
            
            if (currfloor == null || !currfloor.getID().equals(pl.getFloor())) {
                do {
                    currfloor = m_afloors.get(iFloor++);
                } while (!currfloor.getID().equals(pl.getFloor()));
            }

            currfloor.getContainer().add(pl.getButton());
            pl.setButtonBounds();

            if (transBtns) {
                pl.getButton().setOpaque(false);
                pl.getButton().setContentAreaFilled(false);
                pl.getButton().setBorderPainted(false);
            }
            
            pl.getButton().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent E) {
                if (!actionEnabled) {
                    if (pl.getDiffX() == 0) {
                        pl.setDiffX(pl.getButton().getX() - pl.getX());
                        pl.setDiffY(pl.getButton().getY() - pl.getY());
                    }
                    newX = E.getX() + pl.getButton().getX();
                    newY = E.getY() + pl.getButton().getY();
                    pl.getButton().setBounds(newX + pl.getDiffX(), newY + pl.getDiffY(),
                        pl.getButton().getWidth(), pl.getButton().getHeight());
                        pl.setX(newX);
                        pl.setY(newY);
                    }
                }
            });

            pl.getButton().addActionListener(new MyActionListener(pl));
        }
        
        m_jPanelTips = new JPanelTips(app, this);
        add(m_jPanelTips, "tips");

        m_jreservations = new JTicketsBagRestaurantRes(app, this);
        add(m_jreservations, "res");

        showLayout = m_App.getAppUserView().getUser().hasPermission("sales.Layout");
        if (showLayout) {
            m_jbtnLayout.setVisible(true);
        } else {
            m_jbtnLayout.setVisible(false);
        }        
        
        if (m_App.getProperties().getProperty("till.autoRefreshTableMap").equals("true")) {
            webLblautoRefresh.setText(java.util.ResourceBundle.getBundle("pos_messages")
                .getString("label.autoRefreshTableMapTimerON"));        
            
            Timer autoRefreshTimer = new Timer(Integer.parseInt(m_App.getProperties()
                .getProperty("till.autoRefreshTimer"))*1000, new tableMapRefresh());
    
            autoRefreshTimer.start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                autoRefreshTimer.stop();
            } 
        } else {
            webLblautoRefresh.setText(java.util.ResourceBundle.getBundle("pos_messages")
                .getString("label.autoRefreshTableMapTimerOFF"));        
        }

        
        updateCount();
    }


    class tableMapRefresh implements ActionListener {
       
        @Override
        public void actionPerformed(ActionEvent e) {
            loadTickets();
            printState(); 
        }
    }   
        
    /**
     *
     */
    @Override
    public void activate() {
        dlSystem.resetResourcesCache();

        showLayout = m_App.getAppUserView().getUser().hasPermission("sales.Layout");
        if (showLayout) {
            m_jbtnLayout.setVisible(true);
        } else {
            m_jbtnLayout.setVisible(false);
        }

        m_jbtnNoSale.setText("No Sale");
        m_PlaceClipboard = null;
        customer = null;
        loadTickets();        
        printState(); 
        
        m_panelticket.setActiveTicket(null, null, false); 
        m_restaurantmap.activate();
       
        showView("map");  
        //Inactive some feature for Guest
        if("3".equals(m_App.getAppUserView().getUser().getRole())){
            jPanel2.setVisible(false);
        }else{
            jPanel2.setVisible(true);
        }

        m_jbtnReservations.setVisible(false);
        m_jbtnLayout.setVisible(false);

        int role = Integer.parseInt(m_App.getAppUserView().getUser().getRole());

        if (role >= 2) j_BtnCashOut.setVisible(false);
        else j_BtnCashOut.setVisible(true);

////////////////////////////////          Open/Close STORE
        try {   
            boolean m_isOpenStore = dlSystem.isOpenStore();
            if(m_isOpenStore) {
                jToggleButton1.setSelected(true);
                jToggleButton1.setText("Close Store");
                isOpenStore = true;
            } else {
                jToggleButton1.setSelected(false);
                jToggleButton1.setText("Open Store");
                isOpenStore = false;
            }
        } catch (BasicException e) {}

        if(isOpenStore) {
            m_jPanelMap.remove(m_jPlaces);
            m_jPanelMap.add(m_jPlaces);
            Component[] components = jPanel2.getComponents();
            for (Component component : components) {
                component.setEnabled(true);
            }

            m_jbtnReservations.setEnabled(false);

            if(!"0".equals(m_App.getAppUserView().getUser().getRole())){
                jToggleButton1.setEnabled(false);
                jToggleButton1.setText("Store is opened");
            } else {
                jToggleButton1.setEnabled(true);
                jToggleButton1.setText("Close Store");
            }
        } else {
            m_jPanelMap.remove(m_jPlaces);
            Component[] components = jPanel2.getComponents();
            for (Component component : components) {
                component.setEnabled(false);
            }

            j_Close.setEnabled(true);
            jToggleButton1.setEnabled(true);

            if(!"0".equals(m_App.getAppUserView().getUser().getRole())){
                jToggleButton1.setEnabled(false);
                jToggleButton1.setText("Store is closed");
            } else {
                jToggleButton1.setEnabled(true);
                jToggleButton1.setText("Open Store");
            }
        }
////////////////////////////////
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {
        
        if (viewTables()) {
            m_PlaceClipboard = null;
            customer = null;

            if (m_PlaceCurrent != null) {
                if(m_panelticket.getActiveTicket() != null) {  
                    try {
                        dlReceipts.updateSharedTicket(m_PlaceCurrent.getId(), 
                            m_panelticket.getActiveTicket(),
                            m_panelticket.getActiveTicket().getPickupId());
                        if(m_panelticket.getActiveTicket().getTotal() <= 0)
                            dlReceipts.deleteSharedTicket(m_PlaceCurrent.getId());
                        else
                            dlReceipts.unlockSharedTicket(m_PlaceCurrent.getId(), null);
                    } catch (BasicException e) {
                        new MessageInf(e).show(this);
                    }                                  
                }
 
                m_PlaceCurrent = null;

            }
            printState();     
            m_panelticket.setActiveTicket(null, null, false); 

            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return
     */
    @Override
    protected JComponent getBagComponent() {
        return m_restaurantmap;
    }

    /**
     *
     * @return
     */
    @Override
    protected JComponent getNullComponent() {
        return this;
    }

    /**
     *
     * @return
     */
    public TicketInfo getActiveTicket() {
        return m_panelticket.getActiveTicket();
    }

    /**
     *
     */
    public void moveTicket() {
        if (m_PlaceCurrent != null) {
             try {
                dlReceipts.updateRSharedTicket(m_PlaceCurrent.getId(), 
                    m_panelticket.getActiveTicket(),m_panelticket.getActiveTicket().getPickupId());
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }      
            
            m_PlaceClipboard = m_PlaceCurrent;                                  // put FROM table to TO table
            
            customer = null;
            m_PlaceCurrent = null;                                            // Hang on we'll clear later after we're done
        }
        
        printState();
        m_panelticket.setActiveTicket(null, null, false);
    }
    
    /**
     *
     * @param c
     * @return
     */
    public boolean viewTables(CustomerInfo c) {
        if (m_jreservations.deactivate()) {
            showView("map");
            m_PlaceClipboard = null;    
            customer = c;     
            printState();
            return true;
        } else {
            return false;
        }        
    }
    
    /**
     *
     * @return
     */
    public boolean viewTables() {
        return viewTables(null);
    }
        
    /**
     *
     */
    public void newTicket() {

        if (m_PlaceCurrent != null) {

            try {
                String m_lockState = null;
                m_lockState = dlReceipts.getLockState(m_panelticket.getActivePlaceID(), m_lockState);

                if (m_lockState != null && ("override".equals(m_lockState)
                        || "locked".equals(m_lockState))) {
                    if (m_panelticket.getActiveTicket().getLinesCount() > 0)
                        dlReceipts.updateSharedTicket(
                            m_panelticket.getActivePlaceID(),
                            m_panelticket.getActiveTicket(),
                            m_panelticket.getActiveTicket().getPickupId()
                        ); 
                    else {
                        dlReceipts.deleteSharedTicket(m_panelticket.getActivePlaceID());
                    }

                    dlReceipts.unlockSharedAllTicket(null);
                    m_PlaceCurrent = null;                        
    
                } else {
                    JOptionPane.showMessageDialog(null
                        , AppLocal.getIntString("message.sharedticketlockoverriden")
                        , AppLocal.getIntString("title.editor")
                        , JOptionPane.INFORMATION_MESSAGE);                        
                }

                updateCount();
            } catch (BasicException ex) {
                log.error(ex.getMessage());
            }
        }
        
        printState();     
        m_panelticket.setActiveTicket(null, null, false);     
    }
    
    /**
     *
     * @return
     */
    public String getTable() {
        String id = null;
        if (m_PlaceCurrent != null) {
            id = m_PlaceCurrent.getId();
    }
        return(id);
    }
    
    /**
     *
     * @return
     */
    public String getTableName() {
        String stableName = null;
        if (m_PlaceCurrent != null) {
            stableName = m_PlaceCurrent.getName();
    }
        return(stableName);
    }

    /**
     *
     */
    @Override
    public void deleteTicket() {
        
        if (m_PlaceCurrent != null) {
            String id = m_panelticket.getActivePlaceID();
            try {
                dlReceipts.deleteSharedTicket(id);
                restDB.clearPlaceById(id);
                dlSystem.execTicketRemoved(
                new Object[] {
                    m_App.getAppUserView().getUser().getName(),
                    "Void",   
                    "Ticket Deleted",
                    0.0
                });

            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }       
            
            m_PlaceCurrent.setPeople(false);
            m_PlaceCurrent = null;
        }   

        printState();     
        m_panelticket.setActiveTicket(null, null, false); 
        updateCount();
    }

    /**
     *
     */
    public void changeServer() {

        if (m_ServerCurrent != null) {
        }
    }
    
    /**
     *
     */
    public void loadTickets() {
       
        Set<String> atickets = new HashSet<>();
        
        try {
            java.util.List<SharedTicketInfo> l = dlReceipts.getSharedTicketList();
            l.stream().forEach((ticket) -> {
                try {
                    TicketInfo ticket2 = dlReceipts.getSharedTicketUnlocked(ticket.getId());
                    if(ticket2 == null)
                        atickets.add(ticket.getId());
                    else if(ticket2.getTotal() > 0)
                        atickets.add(ticket.getId());
                    else
                        dlReceipts.deleteSharedTicket(ticket.getId());
                } catch (BasicException e) {}
            });
        } catch (BasicException e) {
            new MessageInf(e).show(this);
        }            
            
        m_aplaces.stream().forEach((table) -> {
            table.setPeople(atickets.contains(table.getId()));
        });

        updateCount();
    }
    
/*
 *  Populate the floor plans and tables    
*/
    private void printState() {
        String sDB;
        sDB = m_App.getProperties().getProperty("db.engine"); 
        
        if (m_PlaceClipboard == null) {
            if (customer == null) {
                m_jText.setText(null);

                m_aplaces.stream()
                        .map((place) -> {
                            place.getButton().setEnabled(true);
                            Integer guests = restDB.getGuestsInTable(place.getId());
                            place.setGuests(guests);
                            Date occupied = restDB.getOccupied(place.getId());
                            place.setOccupied(occupied);
                            return place;
                        })

                        .map((place) -> {
                            if (m_App.getProperties().getProperty("table.tablecolour")== null){
                                tableName="<style=font-size:9px;font-weight:bold;><font color = black>"
                                + place.getName()+"</font></style>";  
                            }else{
                                if (place.getOccupied() != null) {
                                    Date date = new java.util.Date();
                                    Timestamp t1 = new Timestamp(date.getTime());
                                    Timestamp t2 = new Timestamp(place.getOccupied().getTime());    
 
                                    long milliseconds = t1.getTime() - t2.getTime();
                                    int seconds = (int) milliseconds / 1000;
                                    int hours = seconds / 3600;
                                    int minutes = (seconds % 3600) / 60;

Integer count = restDB.getGuestsInTable(place.getId());
        
                                    tableName="<style=font-size:9px;font-weight:bold;><font color ="
                                        + m_App.getProperties().getProperty("table.tablecolour")+ ">"
                                        + place.getName() + " / " + place.getSeats() + "<br>"
//                                        + AppLocal.getIntString("button.guest") + place.getGuests() + "<br>"
                                        + AppLocal.getIntString("button.guest") + count + "<br>"                                            
                                        + AppLocal.getIntString("button.occupied") 
                                            + hours + "h" + ":" + minutes + "m" 
                                            + "<br>"
                                        + "</font></style>";      
                                } else {
                                    tableName="<style=font-size:9px;font-weight:bold;><font color ="
                                    + m_App.getProperties().getProperty("table.tablecolour")+ ">"
                                    + place.getName() + " / " + place.getSeats() + "<br>"
                                    + "</font></style>";
                                }
                            }
                            return place;            
                        })

                        .map((place) -> {
                            if (Boolean.parseBoolean(m_App.getProperties().getProperty("table.showwaiterdetails"))){
                                if (m_App.getProperties().getProperty("table.waitercolour")== null){
                                    waiterDetails = (restDB.getWaiterNameInTable(place.getName()) ==null)? ""
                                    :"<style=font-size:9px;font-weight:bold;><font color = red>"
                                    + restDB.getWaiterNameInTableById(place.getId())+"</font></style><br>";
                                }else{
                                    waiterDetails = (restDB.getWaiterNameInTable(place.getName()) ==null)? ""
                                    :"<style=font-size:9px;font-weight:bold;><font color ="
                                    + m_App.getProperties().getProperty("table.waitercolour")+ ">"
                                    + restDB.getWaiterNameInTableById(place.getId())+"</font></style><br>";
                                }
                                place.getButton().setIcon(ICO_OCU_SM);
                            } else {
                                waiterDetails = ""; 
                            }
                            return place;           
                        })
                        
                        .map((place) -> {
                            if (Boolean.parseBoolean(
                                    m_App.getProperties().getProperty("table.showcustomerdetails"))){
                                place.getButton().setIcon((Boolean.parseBoolean(
                                        m_App.getProperties().getProperty("table.showwaiterdetails"))
                                        && (restDB.getCustomerNameInTable(place.getName()) !=null))
                                        ? ICO_WAITER:ICO_OCU_SM);
                                if (m_App.getProperties().getProperty("table.customercolour")== null){                
                                    customerDetails = (restDB.getCustomerNameInTable(place.getName()) ==null)? ""
                                    :"<style=font-size:9px;font-weight:bold;><font color = blue>"
                                    + restDB.getCustomerNameInTableById(place.getId())+"</font></style><br>";
                                }else{
                                    customerDetails = (restDB.getCustomerNameInTable(place.getName()) ==null)? ""
                                            :"<style=font-size:9px;font-weight:bold;><font color ="
                                        + m_App.getProperties().getProperty("table.customercolour")+ ">"
                                        + restDB.getCustomerNameInTableById(place.getId())+"</font></style><br>";
                                }
                            } else {
                                customerDetails = ""; 
                            }
                            return place;
                        })
                        
                        .map((place) -> { 
                            if ((Boolean.parseBoolean(
                                    m_App.getProperties().getProperty("table.showwaiterdetails")))
                                    || (Boolean.parseBoolean(
                                            m_App.getProperties().getProperty("table.showcustomerdetails")))) {
                                place.getButton().setText("<html><center>"
                                + customerDetails + waiterDetails  +tableName+"</html>" );
                            }else{
                                if (m_App.getProperties().getProperty("table.tablecolour")== null){
                                    tableName="<style=font-size:10px;font-weight:bold;><font color = black>"
                                    + place.getName()+"</font></style>";  
                                }else{
                                    tableName="<style=font-size:10px;font-weight:bold;><font color ="
                                    + m_App.getProperties().getProperty("table.tablecolour")+ ">"
                                    + place.getName()+"</font></style>";     
                                }
                                place.getButton().setText("<html><center>"+tableName+"</html>");
                            }
                            return place;
                        })
                        
                        .filter((place) -> (!place.hasPeople()))

                        .forEach((place) -> {
                            place.getButton().setIcon(ICO_FRE);
                        });

                        m_jbtnReservations.setEnabled(false);
// places here

            } else {
                m_jText.setText(AppLocal.getIntString("label.restaurantcustomer"
                        , new Object[] {
                            customer.getName()
                        }
                )
                );

                m_aplaces.stream()
                        .forEach((place) -> {
                            place.getButton().setEnabled(!place.hasPeople());
                        });
                m_jbtnReservations.setEnabled(false);
            }
        } else {
            m_jText.setText(AppLocal.getIntString("label.restaurantmove"
                    , new Object[] {
                    m_PlaceClipboard.getName()
                }
            ));

            m_aplaces.stream()
                .forEach((place) -> {
                    Integer guests = restDB.getGuestsInTable(place.getId());
                    place.setGuests(guests);
                    Date occupied = restDB.getOccupied(place.getId());
                    place.setOccupied(occupied);                
                    place.getButton().setEnabled(true);
                });  

            m_jbtnReservations.setEnabled(false);
        }       
    }

    private TicketInfo getTicketInfo(Place place) {

        try {
            return dlReceipts.getSharedTicket(place.getId());
        } catch (BasicException e) {
            new MessageInf(e).show(JTicketsBagRestaurantMap.this);
            return null;
        }
    }
    
    private void setActivePlace(Place place, TicketInfo ticket, boolean closed) {
        m_PlaceCurrent = place;
        m_panelticket.setActiveTicket(ticket, m_PlaceCurrent.getId(), closed);
        m_restaurantmap.updateGuestCount();
        updateCount();
        try {
            dlReceipts.lockSharedTicket(m_PlaceCurrent.getId(),"locked");
        } catch (BasicException ex) {
            log.error(ex.getMessage());
        }
    } 

    private void showView(String view) {
        CardLayout cl = (CardLayout)(getLayout());
        cl.show(this, view);  
    }

    private class MyActionListener implements ActionListener {

        private final Place m_place;
        public MyActionListener(Place place) {
            m_place = place;
        }
        @Override
        public void actionPerformed(ActionEvent evt) {    
            TicketInfo.tableSeatCount = Integer.valueOf(m_place.getSeats());
            m_App.getAppUserView().getUser();    
        
            if (!actionEnabled) {
                m_place.setDiffX(0);
            } else {
                if (m_PlaceClipboard == null) {  
                    TicketInfo ticket = getTicketInfo(m_place);
                        if (ticket == null) {                                   // It's an empty table            
                            ticket = new TicketInfo();
                            ticket.setUser(m_App.getAppUserView().getUser().getUserInfo());
                            try {
//                                ticket.setName(ticket.getName(null));
                                dlReceipts.insertSharedTicket(m_place.getId(), ticket, ticket.getPickupId());
                                String ticketName = dlReceipts.getNameSharedTicket(m_place.getId());
                                ticket.setName(ticketName);
                            } catch (BasicException e) {
                                new MessageInf(e).show(JTicketsBagRestaurantMap.this);
                            }
                            m_place.setPeople(true);
                            m_place.setGuests(restDB.updateGuestsInTable(m_place.getId()));
                            setActivePlace(m_place, ticket, false);
                        } else {                                                // Table not empty            
                            String m_lockState = null;
                            try {
                                m_lockState = dlReceipts.getLockState(m_place.getId(), m_lockState); //check lockstate

                                if ("locked".equals(m_lockState)) {             // It's locked
                                    JOptionPane.showMessageDialog(null, 
                                        AppLocal.getIntString("message.sharedticketlock")); 

                                    if (m_App.getAppUserView().getUser().hasPermission("sales.Override")) {       // Override it             
                                        int res = JOptionPane.showConfirmDialog(null
                                            , AppLocal.getIntString("message.sharedticketlockoverride")
                                            , AppLocal.getIntString("title.editor")
                                            , JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                                        if (res == JOptionPane.YES_OPTION) {                
                                            m_place.setPeople(true);                                             
                                            m_PlaceClipboard = null;
                                            setActivePlace(m_place, ticket, false);
                                            dlReceipts.lockSharedTicket(m_PlaceCurrent.getId(),"locked");                                        
                                        }                        
                                    }
                                } else {   // It's not locked
                                    String m_user = m_App.getAppUserView().getUser().getId(); 
                                    String ticketuser = dlReceipts.getServer(m_place.getId(),m_user);                                        

                                    if (m_App.getAppUserView().getUser().hasPermission("sales.Override") //Check User permission
                                            || m_user.equals(ticketuser)) {
                                        m_place.setPeople(true);                                             
                                        
                                        m_PlaceClipboard = null;
                                        m_lockState = "locked";
                                        setActivePlace(m_place, ticket, false);
                                    } else {
                                        JOptionPane.showMessageDialog(null
                                            , AppLocal.getIntString("message.sharedticket")
                                            , AppLocal.getIntString("title.editor")
                                            , JOptionPane.OK_OPTION);
                                    }
                                }
                            } catch (BasicException ex) {
                                log.error(ex.getMessage());
                            }
//                            printState();                // show table map. Why here?
                        }    
                    }
// This block handles Merge
// at this stage m_PlaceClipboard is FROM table
// at this point m_place is TO table

                    if (m_PlaceClipboard != null) {                                         // Anything in the Clipboard?
                        TicketInfo ticketclip = getTicketInfo(m_PlaceClipboard);            // add ticket object from clipboard

                        if (ticketclip != null) {

                            if (m_PlaceClipboard == m_place) {                              // check if FROM same as TO
                                Place placeclip = m_PlaceClipboard;                       
                                m_PlaceClipboard = null;
                                customer = null;
                                printState();
                                setActivePlace(placeclip, ticketclip, false);
                            } 

                            if (m_place.hasPeople()) {                                      // check if TO table already occupied
                                TicketInfo ticket = getTicketInfo(m_place);                 // add TO ticket object

                                if (ticket != null) {                                       // It does, so...

                                    if (JOptionPane.showConfirmDialog(JTicketsBagRestaurantMap.this,
                                            AppLocal.getIntString("message.mergetablequestion"), 
                                            AppLocal.getIntString("message.mergetable"), 
                                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {                                 
                                        try {
                                            m_PlaceClipboard.setPeople(false);

                                            if (ticket.getCustomer() == null) {
                                                ticket.setCustomer(ticketclip.getCustomer());
                                            }
                                            ticketclip.getLines().stream().forEach((line) -> {
                                                ticket.addLine(line);
                                            });
                                            dlReceipts.updateRSharedTicket(m_place.getId(), 
                                                    ticket, ticket.getPickupId());
                                            dlReceipts.deleteSharedTicket(m_PlaceClipboard.getId());   
                                            m_place.setGuests(m_PlaceClipboard.getGuests());    // Add Guests from Clipboard        
                           
                                        } catch (BasicException e) {
                                            new MessageInf(e).show(JTicketsBagRestaurantMap.this);
                                        }
                                        m_PlaceClipboard = null;
                                        customer = null;

                                        restDB.clearCustomerNameInTable(restDB.getTableDetails(ticketclip.getId()));
                                        restDB.clearWaiterNameInTable(restDB.getTableDetails(ticketclip.getId()));
                                        restDB.clearTableMovedFlag(restDB.getTableDetails(ticketclip.getId()));
                                        restDB.clearTicketIdInTable(restDB.getTableDetails(ticketclip.getId()));                 
                                        restDB.clearOccupiedTable(restDB.getTableDetails(ticketclip.getId()));

                                        printState();
                                        setActivePlace(m_place, ticket, false);
                                    } else { 
                                        Place placeclip = m_PlaceClipboard;                 // don't want to merge so clear clipboard      
                                        m_PlaceClipboard = null;
                                        customer = null;
                                        printState();
                                        setActivePlace(placeclip, ticketclip, false);                                   
                                    }
                                } else {                                                    
                                    new MessageInf(MessageInf.SGN_WARNING, 
                                            AppLocal.getIntString("message.tableempty"))
                                            .show(JTicketsBagRestaurantMap.this);
                                    m_place.setPeople(false);                            
                                }                                
                            } else {                                                        // The TO table is empty
                                TicketInfo ticket = getTicketInfo(m_place);                 // fill ticket object with TO table

                                if (ticket == null) {
                                    try {
                                        dlReceipts.insertRSharedTicket(m_place.getId(), ticketclip, ticketclip.getPickupId());
                                        m_place.setPeople(true);                        
                                        m_place.setGuests(m_PlaceClipboard.getGuests());    // Add Guests from Clipboard
                                        dlReceipts.deleteSharedTicket(m_PlaceClipboard.getId());                            
                                        m_PlaceClipboard.setPeople(false);
                                    } catch (BasicException e) {
                                        new MessageInf(e).show(JTicketsBagRestaurantMap.this);
                                    }
                                    printState();
                                    setActivePlace(m_place, ticketclip, false);                            
                                    m_PlaceClipboard = null;
                                    customer = null;                        
                                } else {
                                    new MessageInf(MessageInf.SGN_WARNING, 
                                            AppLocal.getIntString("message.tablefull"))
                                            .show(JTicketsBagRestaurantMap.this);
                                    m_PlaceClipboard.setPeople(true);
                                    printState();
                                }
                            }
                        } else { // table empty! Do we need it here?
                            new MessageInf(MessageInf.SGN_WARNING, 
                                    AppLocal.getIntString("message.tableempty")).show(JTicketsBagRestaurantMap.this);
                            m_PlaceClipboard.setPeople(false);
                            m_PlaceClipboard = null;
                            customer = null;
                            printState();
                        }
                    } // end of Merge
            } // end of !actionEnabled 
        } // end of actionPerformed
    } // end of Action Listener

    private void printFinalCloseStore(TicketInfo ticket) {
        String sresource = dlSystem.getResourceAsXML("Printer.FinalCloseStore");

        try {
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            double totalCashIn = dlSystem.getTotalCashIn();
            double totalCashOut = dlSystem.getTotalCashOut();
            double totalCash = dlSystem.getTotalCash();
            double totalCCSales = dlSystem.getTotalCC()[0];
            double totalCCTips = dlSystem.getTotalCC()[1];
            double ccGratuity = 0;

            double cashInDrawer = totalCash;
            double shortage = totalCashIn + totalCashOut;
            double cashDeposit = totalCash - shortage;

            script.put("ticket", ticket);
            script.put("openDate", Formats.TIMESTAMP.formatValue(formatter.parse(dlSystem.getOpenedStoreTime())));
            script.put("closeDate", Formats.TIMESTAMP.formatValue(new Date()));

            script.put("cashDrawer", new BigDecimal(cashInDrawer).setScale(2, RoundingMode.HALF_EVEN));
            script.put("shortage", new BigDecimal(shortage).setScale(2, RoundingMode.HALF_EVEN));
            script.put("cashActivity", new BigDecimal(totalCash).setScale(2, RoundingMode.HALF_EVEN));
            script.put("cashDeposit", new BigDecimal(cashDeposit).setScale(2, RoundingMode.HALF_EVEN));

            script.put("ccSales", new BigDecimal(totalCCSales).setScale(2, RoundingMode.HALF_EVEN));
            script.put("ccGratuity", new BigDecimal(ccGratuity).setScale(2, RoundingMode.HALF_EVEN));
            script.put("ccTips", new BigDecimal(totalCCTips).setScale(2, RoundingMode.HALF_EVEN));
            script.put("ccGT", new BigDecimal(totalCCTips + ccGratuity).setScale(2, RoundingMode.HALF_EVEN));
            script.put("ccTotal", new BigDecimal(totalCCTips + ccGratuity + totalCCSales).setScale(2, RoundingMode.HALF_EVEN));

            m_TTP.printTicket(script.eval(sresource).toString(), ticket);
        } catch (ScriptException | TicketPrinterException | BasicException | ParseException e) {}
    }

    private void printCloseStore(TicketInfo ticket) {
        String sresource = dlSystem.getResourceAsXML("Printer.CloseStore");

        try {
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

            List<Object[]> paymentSummary = dlSystem.getPaymentSummary();
            double totalNet = dlSystem.getTotalNet();
            double totalTax = dlSystem.getTotalTax();
            double totalPayment = 0, totalTip = 0;
            double orderCount = dlSystem.getCountOfOrdersFromOpen();

            for (int i = 0; i < paymentSummary.size(); i ++) {
                totalPayment += Double.parseDouble(paymentSummary.get(i)[1].toString());
                totalTip += Double.parseDouble(paymentSummary.get(i)[2].toString());
            }

            script.put("ticket", ticket);
            script.put("openDate", Formats.TIMESTAMP.formatValue(formatter.parse(dlSystem.getOpenedStoreTime())));
            script.put("closeDate", Formats.TIMESTAMP.formatValue(new Date()));

            script.put("paymentSummary", paymentSummary);
            script.put("totalPayment", new BigDecimal(totalPayment).setScale(2, RoundingMode.HALF_EVEN));
            script.put("totalTip", new BigDecimal(totalTip).setScale(2, RoundingMode.HALF_EVEN));

            script.put("totalNet", new BigDecimal(totalPayment - totalTax).setScale(2, RoundingMode.HALF_EVEN));
            script.put("totalTax", new BigDecimal(totalTax).setScale(2, RoundingMode.HALF_EVEN));

            script.put("orderCount", orderCount);

            if(orderCount == 0) {
                script.put("avgOrderNet", 0.00);
                script.put("avgOrderTax", 0.00);
            } else {
                script.put("avgOrderNet", new BigDecimal(totalPayment / orderCount).setScale(2, RoundingMode.HALF_EVEN));
                script.put("avgOrderTax", new BigDecimal(totalNet / orderCount).setScale(2, RoundingMode.HALF_EVEN));
            }

            m_TTP.printTicket(script.eval(sresource).toString(), ticket);
        } catch (ScriptException | TicketPrinterException | BasicException | ParseException e) {}
    }

    /**
     *
     * @param btnText
     */
    public void setButtonTextBags(String btnText){
      m_PlaceClipboard.setButtonText(btnText);
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jPanelMap = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        m_jbtnReservations = new javax.swing.JButton();
        m_jbtnRefresh = new javax.swing.JButton();
        m_jbtnLayout = new javax.swing.JButton();
        m_jbtnTips = new javax.swing.JButton();
        m_jbtnNoSale = new javax.swing.JButton();
        m_jText = new javax.swing.JLabel();
        m_jbtnOpenTicket = new javax.swing.JButton();
        m_jListTickets = new javax.swing.JButton();
        j_BtnReOpen = new javax.swing.JButton();
        j_BtnCashOut = new javax.swing.JButton();
        j_Close = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        webLblautoRefresh = new com.alee.laf.label.WebLabel();

        setLayout(new java.awt.CardLayout());

        m_jPanelMap.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPanelMap.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        m_jbtnReservations.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jbtnReservations.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/date.png"))); // NOI18N
        m_jbtnReservations.setText(AppLocal.getIntString("button.reservations")); // NOI18N
        m_jbtnReservations.setToolTipText("Open Reservations screen");
        m_jbtnReservations.setEnabled(false);
        m_jbtnReservations.setFocusPainted(false);
        m_jbtnReservations.setFocusable(false);
        m_jbtnReservations.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnReservations.setMaximumSize(new java.awt.Dimension(133, 40));
        m_jbtnReservations.setMinimumSize(new java.awt.Dimension(133, 40));
        m_jbtnReservations.setPreferredSize(new java.awt.Dimension(133, 45));
        m_jbtnReservations.setRequestFocusEnabled(false);
        m_jbtnReservations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnReservationsActionPerformed(evt);
            }
        });
        jPanel2.add(m_jbtnReservations);

        m_jbtnRefresh.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jbtnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/reload.png"))); // NOI18N
        m_jbtnRefresh.setText(AppLocal.getIntString("button.reloadticket")); // NOI18N
        m_jbtnRefresh.setToolTipText("Reload table information");
        m_jbtnRefresh.setFocusPainted(false);
        m_jbtnRefresh.setFocusable(false);
        m_jbtnRefresh.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnRefresh.setMaximumSize(new java.awt.Dimension(100, 40));
        m_jbtnRefresh.setMinimumSize(new java.awt.Dimension(100, 40));
        m_jbtnRefresh.setPreferredSize(new java.awt.Dimension(100, 45));
        m_jbtnRefresh.setRequestFocusEnabled(false);
        m_jbtnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnRefreshActionPerformed(evt);
            }
        });
        jPanel2.add(m_jbtnRefresh);

        m_jbtnLayout.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jbtnLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/movetable.png"))); // NOI18N
        m_jbtnLayout.setText(AppLocal.getIntString("button.layout")); // NOI18N
        m_jbtnLayout.setToolTipText("");
        m_jbtnLayout.setFocusPainted(false);
        m_jbtnLayout.setFocusable(false);
        m_jbtnLayout.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnLayout.setMaximumSize(new java.awt.Dimension(100, 40));
        m_jbtnLayout.setMinimumSize(new java.awt.Dimension(100, 40));
        m_jbtnLayout.setPreferredSize(new java.awt.Dimension(90, 45));
        m_jbtnLayout.setRequestFocusEnabled(false);
        m_jbtnLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnLayoutActionPerformed(evt);
            }
        });
        jPanel2.add(m_jbtnLayout);

        m_jbtnTips.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jbtnTips.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/wallet.png"))); // NOI18N
        m_jbtnTips.setText(AppLocal.getIntString("button.layout")); // NOI18N
        m_jbtnTips.setToolTipText("");
        m_jbtnTips.setFocusPainted(false);
        m_jbtnTips.setFocusable(false);
        m_jbtnTips.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnTips.setMaximumSize(new java.awt.Dimension(100, 40));
        m_jbtnTips.setMinimumSize(new java.awt.Dimension(100, 40));
        m_jbtnTips.setPreferredSize(new java.awt.Dimension(90, 45));
        m_jbtnTips.setRequestFocusEnabled(false);
        m_jbtnTips.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnTipsActionPerformed(evt);
            }
        });
        jPanel2.add(m_jbtnTips);

        m_jbtnNoSale.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jbtnNoSale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cashdrawer.png"))); // NOI18N
        m_jbtnNoSale.setText(AppLocal.getIntString("button.save")); // NOI18N
        m_jbtnNoSale.setToolTipText("");
        m_jbtnNoSale.setFocusPainted(false);
        m_jbtnNoSale.setFocusable(false);
        m_jbtnNoSale.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnNoSale.setMaximumSize(new java.awt.Dimension(100, 40));
        m_jbtnNoSale.setMinimumSize(new java.awt.Dimension(100, 40));
        m_jbtnNoSale.setPreferredSize(new java.awt.Dimension(110, 45));
        m_jbtnNoSale.setRequestFocusEnabled(false);
        m_jbtnNoSale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnNoSaleActionPerformed(evt);
            }
        });
        jPanel2.add(m_jbtnNoSale);

        m_jText.setBackground(new java.awt.Color(255, 255, 255));
        m_jText.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jText.setForeground(new java.awt.Color(0, 153, 255));
        m_jText.setOpaque(true);
        jPanel2.add(m_jText);

        m_jbtnOpenTicket.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jbtnOpenTicket.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileopen.png"))); // NOI18N
        m_jbtnOpenTicket.setText("New Ticket");
        m_jbtnOpenTicket.setToolTipText("");
        m_jbtnOpenTicket.setFocusPainted(false);
        m_jbtnOpenTicket.setFocusable(false);
        m_jbtnOpenTicket.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnOpenTicket.setMaximumSize(new java.awt.Dimension(100, 40));
        m_jbtnOpenTicket.setMinimumSize(new java.awt.Dimension(100, 40));
        m_jbtnOpenTicket.setPreferredSize(new java.awt.Dimension(130, 45));
        m_jbtnOpenTicket.setRequestFocusEnabled(false);
        m_jbtnOpenTicket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnOpenTicketActionPerformed(evt);
            }
        });
        jPanel2.add(m_jbtnOpenTicket);

        m_jListTickets.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jListTickets.setForeground(new java.awt.Color(255, 0, 153));
        m_jListTickets.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_pending.png"))); // NOI18N
        m_jListTickets.setText("Tickets");
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        m_jListTickets.setToolTipText(bundle.getString("tooltip.layaway")); // NOI18N
        m_jListTickets.setFocusPainted(false);
        m_jListTickets.setFocusable(false);
        m_jListTickets.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jListTickets.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        m_jListTickets.setIconTextGap(1);
        m_jListTickets.setMargin(new java.awt.Insets(0, 2, 0, 2));
        m_jListTickets.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jListTickets.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jListTickets.setPreferredSize(new java.awt.Dimension(120, 45));
        m_jListTickets.setRequestFocusEnabled(false);
        m_jListTickets.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        m_jListTickets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jListTicketsActionPerformed(evt);
            }
        });
        jPanel2.add(m_jListTickets);

        j_BtnReOpen.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        j_BtnReOpen.setText("Re-Open");
        j_BtnReOpen.setPreferredSize(new java.awt.Dimension(90, 45));
        j_BtnReOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_BtnReOpenActionPerformed(evt);
            }
        });
        jPanel2.add(j_BtnReOpen);

        j_BtnCashOut.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        j_BtnCashOut.setText("Cash Out");
        j_BtnCashOut.setPreferredSize(new java.awt.Dimension(90, 45));
        j_BtnCashOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_BtnCashOutActionPerformed(evt);
            }
        });
        jPanel2.add(j_BtnCashOut);

        j_Close.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        j_Close.setText("Logout");
        j_Close.setPreferredSize(new java.awt.Dimension(80, 45));
        j_Close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_CloseActionPerformed(evt);
            }
        });
        jPanel2.add(j_Close);

        jToggleButton1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jToggleButton1.setText("Open Store");
        jToggleButton1.setPreferredSize(new java.awt.Dimension(100, 45));
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jToggleButton1);

        jPanel1.add(jPanel2, java.awt.BorderLayout.LINE_START);

        webLblautoRefresh.setBackground(new java.awt.Color(255, 51, 51));
        webLblautoRefresh.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        webLblautoRefresh.setText(bundle.getString("label.autoRefreshTableMapTimerON")); // NOI18N
        webLblautoRefresh.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel1.add(webLblautoRefresh, java.awt.BorderLayout.CENTER);

        m_jPanelMap.add(jPanel1, java.awt.BorderLayout.NORTH);

        add(m_jPanelMap, "map");
    }// </editor-fold>//GEN-END:initComponents

    private void m_jbtnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnRefreshActionPerformed
        loadTickets();     
        printState();   
    }//GEN-LAST:event_m_jbtnRefreshActionPerformed

    private void m_jbtnReservationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnReservationsActionPerformed
        showView("res");
        m_jreservations.activate();
    }//GEN-LAST:event_m_jbtnReservationsActionPerformed

    private void m_jbtnLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnLayoutActionPerformed
        if (java.util.ResourceBundle.getBundle("pos_messages")
                .getString("button.layout").equals(m_jbtnLayout.getText())) {
            actionEnabled = false;
            m_jbtnLayout.setText(java.util.ResourceBundle
                    .getBundle("pos_messages").getString("button.disablelayout"));
            
            m_aplaces.stream().filter((pl) -> (transBtns)).map((pl) -> {
                pl.getButton().setOpaque(true);
                return pl;
            }).map((pl) -> {
                pl.getButton().setContentAreaFilled(true);
                return pl;
            }).forEachOrdered((pl) -> {
                pl.getButton().setBorderPainted(true);
            });
        } else {
            actionEnabled = true;
            m_jbtnLayout.setText(java.util.ResourceBundle
                    .getBundle("pos_messages").getString("button.layout"));

            m_aplaces.stream().filter((pl) -> (transBtns)).map((pl) -> {
                pl.getButton().setOpaque(false);
                return pl;
            }).map((pl) -> {
                pl.getButton().setContentAreaFilled(false);
                return pl;
            }).forEachOrdered((pl) -> {
                pl.getButton().setBorderPainted(false);
            });
        }
    }//GEN-LAST:event_m_jbtnLayoutActionPerformed

    private void m_jbtnNoSaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnNoSaleActionPerformed
        String sresource = dlSystem.getResourceAsXML("Printer.OpenDrawer");

        try {
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);

            m_TTP.printTicket(script.eval(sresource).toString());
        } catch (ScriptException | TicketPrinterException e) {}
    }//GEN-LAST:event_m_jbtnNoSaleActionPerformed

    private void m_jbtnOpenTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnOpenTicketActionPerformed
        java.util.List<Place> m_aplaces;
        Place m_place;
        try{
            SentenceList sent = new StaticSentence(
                m_app_map.getSession(), 
                "SELECT ID, NAME, SEATS, X, Y, FLOOR, CUSTOMER, WAITER, " 
                    + "TICKETID, TABLEMOVED, WIDTH, HEIGHT, GUESTS, OCCUPIED "
                    + "FROM places "
                    + "WHERE X=0 AND Y=0 and TICKETID is NULL ",
                null, 
                new SerializerReadClass(Place.class));
            m_aplaces = sent.list();
        } catch (BasicException eD) {
            m_aplaces = new ArrayList<>();
        }
        int i=0;
        while (true) {
            try {
                m_place = m_aplaces.get(i);
                TicketInfo existTicket = dlReceipts.getSharedTicket(m_place.getId());
                if(existTicket == null) break;
                else if(i >= m_aplaces.size()) break;
                else i ++;
            } catch (BasicException e) {}
        }

        TicketInfo ticket = new TicketInfo();
        ticket.setUser(m_App.getAppUserView().getUser().getUserInfo());
//        ticket.setName(ticket.getName(null));
        try {
            dlReceipts.insertSharedTicket(m_place.getId(), ticket, ticket.getPickupId());
            String ticketName = dlReceipts.getNameSharedTicket(m_place.getId());
            ticket.setName(ticketName);
        } catch (BasicException e) {
            new MessageInf(e).show(JTicketsBagRestaurantMap.this);
        }
        m_place.setPeople(true);
        m_place.setGuests(restDB.updateGuestsInTable(m_place.getId()));
        setActivePlace(m_place, ticket, false);
        updateCount();
    }//GEN-LAST:event_m_jbtnOpenTicketActionPerformed

    private void m_jListTicketsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jListTicketsActionPerformed

        SwingUtilities.invokeLater(() -> {
            try {
                if (!m_App.getAppUserView().getUser().hasPermission("sales.ViewSharedTicket")){
                    JOptionPane.showMessageDialog(null,
                        AppLocal.getIntString("message.sharedticket"),
                        AppLocal.getIntString("message.sharedtickettitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                } else {

                    if("0".equals(m_App.getAppUserView().getUser().getRole())
                        || "1".equals(m_App.getAppUserView().getUser().getRole())
                        || m_App.getAppUserView().getUser().hasPermission("sales.ViewSharedTicket")
                        || m_App.getAppUserView().getUser().hasPermission("sales.Override"))
                    {
                        List<SharedTicketInfo> l = dlReceipts.getSharedTicketList();
                        Window window = getWindow(JTicketsBagRestaurantMap.this);
                        JTicketsBagSharedList listDialog = JTicketsBagSharedList.newJDialog(window);

                        String id = listDialog.showTicketsList(l, dlReceipts, m_App, "");

                        if (id != null) {
                            //saveCurrentTicket();
                            setActiveTicket(id, false);
                        }
                    } else {

                        String appuser = m_App.getAppUserView().getUser().getId();
                        List<SharedTicketInfo> l = dlReceipts.getUserSharedTicketList(appuser);
                        Window window = getWindow(JTicketsBagRestaurantMap.this);
                        JTicketsBagSharedList listDialog = JTicketsBagSharedList.newJDialog(window);

                        String id = listDialog.showTicketsList(l, dlReceipts, m_App, "");

                        if (id != null) {
                            //saveCurrentTicket();
                            setActiveTicket(id ,false);
                        }
                    }
                }
            }catch (BasicException e) {
                new MessageInf(e).show(JTicketsBagRestaurantMap.this);
                newTicket();
            }
        });

    }//GEN-LAST:event_m_jListTicketsActionPerformed

    private void j_CloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_CloseActionPerformed
        ((JRootApp)m_App).closeAppView();
    }//GEN-LAST:event_j_CloseActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        // TODO add your handling code here:
        if("Open Store".equals(jToggleButton1.getText())) {
            jToggleButton1.setText("Close Store");
            dlSystem.openStore();
        } else {
            int count = 0;
            boolean isAllCashOut = false;
            try {
                java.util.List<SharedTicketInfo> l = dlReceipts.getSharedTicketList();
                isAllCashOut = dlSystem.isAllUserCashOut();
                count = l.size();
            } catch (BasicException e) {}

            if(count > 0) {
                JFrame frame = new JFrame("Swing Dialog");
                JOptionPane.showMessageDialog(frame, "There are still Open Tickets!");
                return;
            } else {
                if(isAllCashOut) {
                    boolean flag = true;

                    if (JOptionPane.showConfirmDialog(jPanel2, 
                        "Would you like to clock out users?", 
                        "Confirm", 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) flag = false;

                    if (flag == true) {
                        try {
                            java.util.List people = dlSystem.listClockinVisible();
                            Window window = getWindow(this);
                            JClockedOutUserDialog listDialog = JClockedOutUserDialog.newJDialog(window);

                            String returnMSG = listDialog.showUsersList(people, dlReceipts, dlSystem, m_app_map);

                            if ("cancel".equals(returnMSG)) {
                                return;
                            }
                        } catch (BasicException ee) {}
                    }

                    jToggleButton1.setText("Open Store");
                    dlSystem.closeStore();

                    TicketInfo printTicket = new TicketInfo();
                    printCloseStore(printTicket);
                    printFinalCloseStore(printTicket);
                } else {
                    JFrame frame = new JFrame("Swing Dialog");
                    JOptionPane.showMessageDialog(frame, "All user must cash out!");
                    return;
                }
            }
        }

        activate();
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void j_BtnCashOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_BtnCashOutActionPerformed
        // TODO add your handling code here:
        List<Object[]> cashOutUsers = null;
        try {
            cashOutUsers = dlSystem.getCashOutUserid();
        } catch (BasicException e) {
            throw new RuntimeException(e);
        }
        Window window = getWindow(this);
        JCashOutDialog listDialog = JCashOutDialog.newJDialog(window);

        listDialog.showTicketList(cashOutUsers, dlSystem, m_App, dlReceipts);
    }//GEN-LAST:event_j_BtnCashOutActionPerformed

    private void m_jbtnTipsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnTipsActionPerformed
        // TODO add your handling code here:
        try {
            showView("tips");
            m_jPanelTips.activate();
        } catch (BasicException e) {
            System.out.println("Open Tips Windows Error: " + e.getMessage());
        }
    }//GEN-LAST:event_m_jbtnTipsActionPerformed

    private void j_BtnReOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_BtnReOpenActionPerformed
        // TODO add your handling code here:
        try {
            String appuser = m_App.getAppUserView().getUser().getId();
            List<ClosedTicketInfo> l = dlSystem.getClosedTicketList();
            Window window = getWindow(JTicketsBagRestaurantMap.this);
            JTicketsBagClosedList listDialog = JTicketsBagClosedList.newJDialog(window);

            String id = listDialog.showTicketsList(l, dlReceipts, m_App);

            if (id != null) 
                setActiveTicket(id, true);
        } catch (BasicException e) {}
    }//GEN-LAST:event_j_BtnReOpenActionPerformed
    
    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window) parent;
        } else {
            return getWindow(parent.getParent());
        }
    }  
    
    private void saveCurrentTicket() {
        if (m_sCurrentTicket != null) {
            try {
                dlReceipts.insertSharedTicket(m_sCurrentTicket, 
                    m_panelticket.getActiveTicket(),
                    m_panelticket.getActiveTicket().getPickupId());
                    m_jListTickets.setText("*");
                TicketInfo l = dlReceipts.getSharedTicket(m_sCurrentTicket);
                    if(l.getLinesCount() == 0) {
                        dlReceipts.deleteSharedTicket(m_sCurrentTicket);
                    }             
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }  
        }    

        updateCount();
    }
    
    private void setActiveTicket(String id, boolean closed) throws BasicException{
        TicketInfo ticket = dlReceipts.getSharedTicket(id);
        if (closed)
            ticket = dlReceipts.getClosedTicket(id);

        if (ticket == null)  {
            m_jListTickets.setText("");
            throw new BasicException(AppLocal.getIntString("message.noticket"));
        } else {
            ticket.setUser(m_App.getAppUserView().getUser().getUserInfo());
            Integer pickUp = dlReceipts.getPickupId(id);
            m_sCurrentTicket = id;
            java.util.List<Place> m_aplaces;
            Place m_place;

            String temp = id;
            if(temp.contains("[")) id = temp.split("\\[")[0];
// ////////////////////////////////////// Closed
            if (closed) {
                try{
                    SentenceList sent = new StaticSentence(
                        m_app_map.getSession(), 
                        "SELECT ID, NAME, SEATS, X, Y, FLOOR, CUSTOMER, WAITER, " 
                            + "TICKETID, TABLEMOVED, WIDTH, HEIGHT, GUESTS, OCCUPIED "
                            + "FROM places "
                            + "WHERE X=0 AND Y=0 and TICKETID is NULL ",
                        null, 
                        new SerializerReadClass(Place.class));
                    m_aplaces = sent.list();
                } catch (BasicException eD) {
                    m_aplaces = new ArrayList<>();
                }
                int i=0;
                while (true) {
                    try {
                        m_place = m_aplaces.get(i);
                        TicketInfo existTicket = dlReceipts.getSharedTicket(m_place.getId());
                        if(existTicket == null) break;
                        else if(i >= m_aplaces.size()) break;
                        else i ++;
                    } catch (BasicException e) {}
                }

                m_place.setPeople(true);
                m_place.setGuests(restDB.updateGuestsInTable(m_place.getId()));
// ////////////////////////////////////// Closed
            } else {
                try{
                    SentenceList sent = new StaticSentence(
                        m_app_map.getSession(), 
                        "SELECT ID, NAME, SEATS, X, Y, FLOOR, CUSTOMER, WAITER, " 
                            + "TICKETID, TABLEMOVED, WIDTH, HEIGHT, GUESTS, OCCUPIED "
                            + "FROM places "
                            + "WHERE ID = '" + id +"'",
                        null, 
                        new SerializerReadClass(Place.class));
                    m_aplaces = sent.list();
                } catch (BasicException eD) {
                    m_aplaces = new ArrayList<>();
                } 
                m_place = m_aplaces.get(0);

                if(temp.contains("[")) {
                    m_place.setId(temp);
                    m_place.setName(temp);
                }
            }

            m_panelticket.setActiveTicket(ticket, m_place.getId(), closed);
            m_panelticket.setClosedTicketID(id);
            setActivePlace(m_place, ticket, closed);      
        } 

        updateCount();
    }
    public void updateCount() {
        try {
            java.util.List<SharedTicketInfo> l = dlReceipts.getSharedTicketList();
            int count = l.size();
            
            if (count > 0) {
                m_jListTickets.setText(Integer.toString(count)+ " Tickets");
            } else {
                m_jListTickets.setText("0 Ticket");
            }
        } catch (BasicException ex) {
            new MessageInf(ex).show(this);
            m_jListTickets.setText("");
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JButton j_BtnCashOut;
    private javax.swing.JButton j_BtnReOpen;
    private javax.swing.JButton j_Close;
    private javax.swing.JButton m_jListTickets;
    private javax.swing.JPanel m_jPanelMap;
    private javax.swing.JLabel m_jText;
    private javax.swing.JButton m_jbtnLayout;
    private javax.swing.JButton m_jbtnNoSale;
    private javax.swing.JButton m_jbtnOpenTicket;
    private javax.swing.JButton m_jbtnRefresh;
    private javax.swing.JButton m_jbtnReservations;
    private javax.swing.JButton m_jbtnTips;
    private com.alee.laf.label.WebLabel webLblautoRefresh;
    // End of variables declaration//GEN-END:variables
    
}
