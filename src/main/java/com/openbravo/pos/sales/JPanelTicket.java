//    Roxy Pos  - Touch Friendly Point Of Sale
//    Copyright © 2009-2020 uniCenta & previous Openbravo POS works
//    https://unicenta.com
//
//    This file is part of Roxy Pos
//
//    Roxy Pos is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   Roxy Pos is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Roxy Pos.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.sales;

import bsh.EvalError;
import bsh.Interpreter;
import com.alee.extended.time.ClockType;
import com.alee.extended.time.WebClock;
import com.alee.managers.notification.NotificationIcon;
import com.alee.managers.notification.NotificationManager;
import com.alee.managers.notification.WebNotification;
import com.openbravo.basic.BasicException;
import com.openbravo.beans.JClockedInUserDialog;
import com.openbravo.beans.JFlowPanel;
import com.openbravo.beans.JNumberPop;
import com.openbravo.beans.JSplitPop;
import com.openbravo.beans.JDiscountPop;
import com.openbravo.beans.JStringDialog;
import com.openbravo.beans.JModDialog;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.ListKeyed;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.format.Formats;
import com.openbravo.pos.catalog.JCatalog;
import com.openbravo.pos.catalog.JCatalog.UpdateTicket;
import com.openbravo.pos.customers.*;
import com.openbravo.pos.epm.EmployeeInfo;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.inventory.Modifier;
import com.openbravo.pos.inventory.ProductStock;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.panels.JProductFinder;
import static com.openbravo.pos.payment.JPaymentCredit.replaceWith;
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.pos.payment.JPaymentSelectReceipt;
import com.openbravo.pos.payment.JPaymentSelectRefund;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.sales.SharedTicketInfo;
import com.openbravo.pos.sales.restaurant.JTicketsBagRestaurantMap;
import com.openbravo.pos.sales.restaurant.Place;
import com.openbravo.pos.sales.restaurant.RestaurantDBUtils;
import com.openbravo.pos.sales.shared.JTicketsBagSharedList;
import com.openbravo.pos.scale.ScaleException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.*;
import com.openbravo.pos.util.AltEncrypter;
import com.openbravo.pos.util.InactivityListener;
import com.openbravo.pos.util.JRPrinterAWT300;
import com.openbravo.pos.util.ReportUtils;
import com.openbravo.pos.util.WebSocketClient;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import javax.print.PrintService;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.function.BiConsumer;

import static java.awt.Window.getWindows;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * @author JG uniCenta
 */
@Slf4j
public abstract class JPanelTicket extends JPanel implements JPanelView, BeanFactoryApp, TicketsEditor {

  private final static int NUMBERZERO = 0;
  private final static int NUMBERVALID = 1;

  private final static int NUMBER_INPUTZERO = 0;
  private final static int NUMBER_INPUTZERODEC = 1;
  private final static int NUMBER_INPUTINT = 2;
  private final static int NUMBER_INPUTDEC = 3;
  private final static int NUMBER_PORZERO = 4;
  private final static int NUMBER_PORZERODEC = 5;
  private final static int NUMBER_PORINT = 6;
  private final static int NUMBER_PORDEC = 7;
  private static final char PASTE_EVENT = '\u0016';
  private static final char ENTER_EVENT = '\n';
  private static final char UNKNOWN = '?';

  protected JTicketLines m_ticketlines;

  private TicketParser m_TTP;

  protected TicketInfo m_oTicket;
  protected Object m_oTicketExt;

  private int m_iNumberStatus;
  private int m_iNumberStatusInput;
  private int m_iNumberStatusPor;
  private StringBuffer m_sBarcode;

  private JTicketsBag m_ticketsbag;

  private SentenceList senttax;
  private ListKeyed taxcollection;

  private SentenceList senttaxcategories;
  private ListKeyed taxcategoriescollection;
  private ComboBoxValModel taxcategoriesmodel;

  private TaxesLogic taxeslogic;

  protected JPanelButtons m_jbtnconfig;
  protected AppView m_App;

  protected DataLogicSystem dlSystem;
  protected DataLogicSales dlSales;
  protected DataLogicCustomers dlCustomers;

  private JPaymentSelect paymentdialogreceipt;
  private JPaymentSelect paymentdialogrefund;

  private JRootApp root;
  private Object m_principalapp;
  private Boolean restaurant;
  private Boolean orderlistopen;

  private Action logout;
  private InactivityListener listener;
  private Integer delay = 0;
  private final String m_sCurrentTicket = null;

  protected TicketsEditor m_panelticket;
  private DataLogicReceipts dlReceipts = null;
  private Boolean priceWith00;
  private final String temp_jPrice = "";
  private String tableDetails;
  private RestaurantDBUtils restDB;
  private KitchenDisplay kitchenDisplay;
  private String ticketPrintType;

  private Boolean warrantyPrint = false;

  private TicketInfo m_ticket;
  private TicketInfo m_ticketCopy;
  private AppConfig m_config;

  private Integer count = 0;
  private Integer oCount = 0;
  private Boolean pinOK;

  private JCatalog m_jCatalog;
  private UpdateTicket m_updateTicket;

  private JPopupMenu popup;
  private JPopupMenu ticketPopup;
  private JMenuItem menuItemVoid;
  private JMenuItem menuItemComp;
  private JMenuItem menuItemUnComp;

    private boolean transferFlag = false;
    private UserInfo m_currentUser = null;
    private boolean isOrdered = false;
    private boolean isClosed = false;
    private String closedTicket = "";

    SimpleReceipt receiptone;
    SimpleReceipt receipttwo;
  /**
   * Creates new form JTicketView
   */
  public JPanelTicket() {

    initComponents();
  }

  /**
   * @param app
   * @throws BeanFactoryException
   */
  @Override
  public void init(AppView app) throws BeanFactoryException {

    m_config = new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
    m_config.load();

    m_App = app;
    restDB = new RestaurantDBUtils(m_App);

    dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
    dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
    dlCustomers = (DataLogicCustomers) m_App.getBean("com.openbravo.pos.customers.DataLogicCustomers");
    dlReceipts = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");


    /* uniCenta Feb 2018
     * Changed for 4.3
     * Set up main toolbar area with two rows to add cater for additional scripts
     * else over-crowding and some dynamic buttons off screen/not visible
     * m_jPanelScripts contains m_jButtonsExt panel for buttons Enabled/Disabled in
     * Resources>Menu.
     */

// Set Configuration>General>Tickets toolbar simple : standard : restaurant option
    m_ticketsbag = getJTicketsBag();
    m_jPanelBagTop.add(m_ticketsbag.getBagComponent(), BorderLayout.LINE_END); 
    add(m_ticketsbag.getNullComponent(), "null");

// Script event buttons
    m_jbtnconfig = new JPanelButtons("Ticket.Buttons", this);
    m_jButtonsExt.add(m_jbtnconfig);

// Configuration>Peripheral options
//    if (!m_App.getDeviceScale().existsScale()) {
//      m_jbtnScale.setVisible(false);
//    }
//    jbtnMooring.setVisible(Boolean.valueOf(m_App.getProperties().getProperty("till.marineoption")));
    m_jPanelScripts.setVisible(false);
    m_jButtonsExt.setVisible(true);
    m_jCombo.setText("v");
//    jTBtnShow.setSelected(false);

//    if (Boolean.valueOf(m_App.getProperties().getProperty("till.amountattop"))) {
//      m_jPanEntries.remove(jPanel9);
//      m_jPanEntries.remove(m_jNumberKeys);
//      m_jPanEntries.add(jPanel9);
//      m_jPanEntries.add(m_jNumberKeys);
//    }

//    priceWith00 = ("true".equals(m_App.getProperties().getProperty("till.pricewith00")));
//
//    if (priceWith00) {
//      m_jNumberKeys.dotIs00(true);
//    }

    m_ticketlines = new JTicketLines(dlSystem.getResourceAsXML("Ticket.Line"));
    m_jPanelCentral.add(m_ticketlines, java.awt.BorderLayout.CENTER);
    m_TTP = new TicketParser(m_App.getDeviceTicket(), dlSystem);
    catcontainer.add(getSouthComponent(), BorderLayout.CENTER);
    if (catcontainer.getComponent(0) instanceof JCatalog) {
        m_jCatalog = (JCatalog) catcontainer.getComponent(0);
    }
    senttax = dlSales.getTaxList();
    senttaxcategories = dlSales.getTaxCategoriesList();
    taxcategoriesmodel = new ComboBoxValModel();

    stateToZero();

    m_oTicket = null;
    m_oTicketExt = null;
    jCheckStock.setText(AppLocal.getIntString("message.title.checkstock"));
    m_jEnter.setVisible(false);
    if (this instanceof JPanelTicketSales) {
      // 2016-10-14 TJMChan - Sales screen Layout code starts here.
      if (AppConfig.getInstance().getProperty("machine.saleslayout") != null) {
        JSalesLayoutManager.createLayout();

        if (AppConfig.getInstance().getProperty("machine.saleslayout").equals("Layout1")) {
          Component southcomponent = getSouthComponent();
          catcontainer.add(southcomponent, BorderLayout.CENTER);
          JPanel southpanel = (JPanel) southcomponent;
          ((JPanel) southpanel.getComponent(0)).setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

          JPanel fpanel = new JPanel(new BorderLayout());
          fpanel.add(m_jPanEntries, BorderLayout.WEST);
          if (southcomponent instanceof JCatalog) {
            m_jCatalog = (JCatalog) southpanel;
            fpanel.add(m_jCatalog.getCatComponent(), BorderLayout.EAST);
          }

          m_jPanEntriesE.add(jPanel5, BorderLayout.WEST);
          m_jPanEntriesE.add(fpanel, BorderLayout.EAST);

          m_jTicketId.setPreferredSize(new Dimension(300, 16));
          m_jPanTicket.add(m_jTicketId, BorderLayout.BEFORE_FIRST_LINE);
//          jPanel2.setPreferredSize(new Dimension(90, 305));
          jPanel5.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
          jPanel4.add(m_jPanTotals, BorderLayout.CENTER);
          m_jContEntries.add(m_jPanEntriesE, BorderLayout.NORTH);
          if (southcomponent instanceof JCatalog) {
            JCatalog catpanel = (JCatalog) southpanel;
            m_jCatalog = catpanel;
            m_jContEntries.add(catpanel.getProductComponent(), BorderLayout.CENTER);
            catpanel.setControls("south");
            catpanel.getCatComponent().setPreferredSize(new Dimension(230, 10));
            catpanel.getProductComponent().setPreferredSize(new Dimension(230, 350));
          } else {
            m_jContEntries.add(southpanel, BorderLayout.CENTER);
          }
          southcomponent.setPreferredSize(new Dimension(0, 0));
        } else if (AppConfig.getInstance().getProperty("machine.saleslayout").equals("Layout2")) {
          if (catcontainer.getComponent(0) instanceof JCatalog) {
            JCatalog catpanel = (JCatalog) catcontainer.getComponent(0);
            m_jCatalog = catpanel;
            catpanel.setControls("south");
            catpanel.getCatComponent().setPreferredSize(new Dimension(230, 10));
          }
          jPanel4.add(m_jPanTotals, BorderLayout.CENTER);
          m_jPanContainer.add(m_jPanTicket, BorderLayout.EAST);
          m_jPanContainer.add(catcontainer, BorderLayout.CENTER);
          jPanel5.setPreferredSize(new Dimension(80, 0));
          m_jPanEntriesE.add(jPanel5, BorderLayout.CENTER);
          m_jPanEntriesE.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
          m_jContEntries.add(m_jPanEntries, BorderLayout.CENTER);
          m_jPanEntries.setPreferredSize(new Dimension(250, 316));
          m_jTicketId.setPreferredSize(new Dimension(300, 16));
          m_jPanTicket.add(m_jTicketId, BorderLayout.BEFORE_FIRST_LINE);
          m_jPanTicket.setPreferredSize(new Dimension(390, 316));
          m_jPanTicket.add(m_jContEntries, BorderLayout.SOUTH);
        } else if (AppConfig.getInstance().getProperty("machine.saleslayout").equals("Layout3")) {
          JPanel catPanel = null;
          JPanel jpanelA = new JPanel(new BorderLayout());
          if (catcontainer.getComponent(0) instanceof JCatalog) {
            JCatalog myCatpanel = (JCatalog) catcontainer.getComponent(0);
            m_jCatalog = myCatpanel;
            myCatpanel.setControls("south");
            myCatpanel.getCatComponent().setPreferredSize(new Dimension(230, 10));
            catPanel = (JPanel) myCatpanel.getCatComponent();
            catPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            m_jPanContainer.add(jpanelA, BorderLayout.EAST);
            jpanelA.add(catPanel, BorderLayout.CENTER);
          }
          jPanel4.add(m_jPanTotals, BorderLayout.CENTER);
          m_jPanContainer.add(m_jPanTicket, BorderLayout.EAST);
          m_jPanContainer.add(catcontainer, BorderLayout.CENTER);
          jPanel5.setPreferredSize(new Dimension(80, 0));
          m_jPanEntriesE.add(jPanel5, BorderLayout.CENTER);
          m_jPanEntriesE.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
          m_jContEntries.add(m_jPanEntries, BorderLayout.CENTER);
          m_jPanEntries.setPreferredSize(new Dimension(250, 316));
          m_jTicketId.setPreferredSize(new Dimension(300, 16));
          m_jPanTicket.add(m_jTicketId, BorderLayout.BEFORE_FIRST_LINE);
          if (catPanel != null) {
            m_jPanTicket.setPreferredSize(new Dimension(500, 316));
            jpanelA.add(m_jContEntries, BorderLayout.EAST);
            m_jPanTicket.add(jpanelA, BorderLayout.SOUTH);
          } else {
            m_jPanTicket.setPreferredSize(new Dimension(390, 316));
            m_jPanTicket.add(m_jContEntries, BorderLayout.SOUTH);
          }
        }
      }
    }

    jCheckStock.setVisible(false);
//    m_jList.setVisible(false);

    jBtnCustomer.setVisible(false);
    m_jContEntries.setVisible(false);
    btnReprint1.setVisible(false);
    m_jCatalog.setSession(m_App.getSession());
    if(m_App.getAppUserView().getUser().getLeftHand()){
        m_jPanContainer.add(m_jPanTicket, BorderLayout.AFTER_LINE_ENDS);
    }else{
        m_jPanContainer.add(m_jPanTicket, BorderLayout.BEFORE_LINE_BEGINS);
    }   
    //Create the popup menu.
    popup = new JPopupMenu();
    ticketPopup = new JPopupMenu();

    menuItemVoid = new JMenuItem(new AbstractAction("Void") {
        public void actionPerformed(ActionEvent e) {
            doVoid();    
        }
    });
    popup.add(menuItemVoid);
    menuItemComp = new JMenuItem(new AbstractAction("Comp") {
        public void actionPerformed(ActionEvent e) {
            doComp();
        }
    });
    popup.add(menuItemComp);
    menuItemUnComp = new JMenuItem(new AbstractAction("UnComp") {
        public void actionPerformed(ActionEvent e) {
            doUnComp();
        }
    });
    popup.add(menuItemUnComp);
    popup.add(new JMenuItem(new AbstractAction("Note") {
        public void actionPerformed(ActionEvent e) {
            doNote();
        }
    }));
    popup.add(new JMenuItem(new AbstractAction("Details") {
        public void actionPerformed(ActionEvent e) {
            doDetails();
        }
    }));
  }

  @Override
  public Object getBean() {
    return this;
  }

  @Override
  public JComponent getComponent() {
    return this;
  }

  private void jPanContainerFocusLost(FocusEvent evt) {
    System.out.println("PanelContainer : Focus Lost");
  }

  private class logout extends AbstractAction {

    public logout() {
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
      closeAllDialogs();
      switch (m_App.getProperties().getProperty("machine.ticketsbag")) {
        case "restaurant":
          if ("false".equals(m_App.getProperties().getProperty("till.autoLogoffrestaurant"))) {
            deactivate();
            ((JRootApp) m_App).closeAppView();
            break;
          }

          deactivate();
          setActiveTicket(null, null, false);
          break;

        default:
          deactivate();
          ((JRootApp) m_App).closeAppView();
      }
    }
  }

  private void closeAllDialogs() {
    Window[] windows = getWindows();

    for (Window window : windows) {
      if (window instanceof JDialog) {
        window.dispose();
      }
    }
  }

  private void saveCurrentTicket() {
    String currentTicket = (String) m_oTicketExt;
    if (currentTicket != null) {
        try {
            dlReceipts.updateSharedTicket(currentTicket, m_oTicket, m_oTicket.getPickupId());
        } catch (BasicException ex) {
            log.error(ex.getMessage());
        }
    }
  }

  /**
   * @throws BasicException
   */
  @Override
  public void activate() throws BasicException {

    Action logout = new logout();
    String autoLogoff = (m_App.getProperties().getProperty("till.autoLogoff"));

    if (autoLogoff != null) {
      if (autoLogoff.equals("true")) {
        try {
          delay = Integer.parseInt(m_App.getProperties().getProperty("till.autotimer"));
        } catch (NumberFormatException e) {
          delay = 0;
        }
        delay *= 1000;
      }
    }

    if (delay != 0) {
      listener = new InactivityListener(logout, delay);
      listener.start();
    }

    paymentdialogreceipt = JPaymentSelectReceipt.getDialog(this);
    paymentdialogreceipt.init(m_App);
    paymentdialogrefund = JPaymentSelectRefund.getDialog(this);
    paymentdialogrefund.init(m_App);

    m_jaddtax.setSelected("true".equals(m_jbtnconfig.getProperty("taxesincluded")));

    java.util.List<TaxInfo> taxlist = senttax.list();
    taxcollection = new ListKeyed<>(taxlist);
    java.util.List<TaxCategoryInfo> taxcategorieslist = senttaxcategories.list();
    taxcategoriescollection = new ListKeyed<>(taxcategorieslist);

    taxcategoriesmodel = new ComboBoxValModel(taxcategorieslist);
    m_jTax.setModel(taxcategoriesmodel);

    String taxesid = m_jbtnconfig.getProperty("taxcategoryid");

    if (taxesid == null) {
      if (m_jTax.getItemCount() > 0) {
        m_jTax.setSelectedIndex(0);
      }
    } else {
      taxcategoriesmodel.setSelectedKey(taxesid);
    }

    taxeslogic = new TaxesLogic(taxlist);
    m_jaddtax.setSelected((Boolean.parseBoolean(m_App.getProperties().getProperty("till.taxincluded"))));

    if (m_App.getAppUserView().getUser().hasPermission("sales.ChangeTaxOptions")) {
      m_jTax.setVisible(true);
      m_jaddtax.setVisible(true);
    } else {
      m_jTax.setVisible(false);
      m_jaddtax.setVisible(false);
    }

    m_jDelete.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
//    m_jNumberKeys.setMinusEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
//    m_jNumberKeys.setEqualsEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Total"));
    m_jbtnconfig.setPermissions(m_App.getAppUserView().getUser());

    m_ticketsbag.setEnabled(false);
    m_ticketsbag.activate();

    CustomerInfoGlobal customerInfoGlobal = CustomerInfoGlobal.getInstance();

    if (customerInfoGlobal.getCustomerInfoExt() != null) {

      if (m_oTicket != null) {
        m_oTicket.setCustomer(customerInfoGlobal.getCustomerInfoExt());
      }
    }

    refreshTicket();

    int role = Integer.parseInt(m_App.getAppUserView().getUser().getRole());

    if (role >= 3){
        m_jOptions.setVisible(false);
        m_jPanelBag.setVisible(false);
        jPanel7.setVisible(false);
        m_jCatalog.enabledButton(false);
        j_mDiscount.setVisible(false);
        m_jDelete.setEnabled(false);
        m_jEditLine.setEnabled(false);
        jOption.setEnabled(false);
        m_jOptions.setVisible(false);
    } else {
        m_jOptions.setVisible(true);
        m_jPanelBag.setVisible(true);
        jPanel7.setVisible(true);
        m_jCatalog.enabledButton(true);
        j_mDiscount.setVisible(true);
        m_jDelete.setEnabled(true);
        m_jEditLine.setEnabled(true);
        jOption.setEnabled(true);
        m_jOptions.setVisible(true);
    }

    if(role != 0){
        j_mComp.setEnabled(false);
        menuItemVoid.setVisible(false);
        menuItemComp.setVisible(false);
        menuItemUnComp.setVisible(false);
    } else {
        j_mComp.setEnabled(true);
        menuItemVoid.setVisible(true);
        menuItemComp.setVisible(true);
        menuItemUnComp.setVisible(true);
    }
  }

  @Override
  public boolean deactivate() {
    if (listener != null) {
      listener.stop();
    }

    return m_ticketsbag.deactivate();
  }

  protected abstract JTicketsBag getJTicketsBag();

  protected abstract Component getSouthComponent();

  protected abstract void resetSouthComponent();

  /**
   * @param oTicket
   * @param oTicketExt
   */
  @SuppressWarnings("empty-statement")
  @Override
  public void setActiveTicket(TicketInfo oTicket, Object oTicketExt, boolean closed) {
    isClosed = closed;
    switch (m_App.getProperties().getProperty("machine.ticketsbag")) {
      case "restaurant":
        if ("true".equals(m_App.getProperties().getProperty("till.autoLogoffrestaurant"))) {
          if (listener != null) {
            listener.restart();
          }
        }
    }

    m_oTicket = oTicket;
    m_oTicketExt = oTicketExt;

    if (m_oTicket != null) {

        if (transferFlag) m_oTicket.setUser(m_currentUser);
        else m_oTicket.setUser(m_App.getAppUserView().getUser().getUserInfo());
        m_oTicket.setActiveCash(m_App.getActiveCashIndex());
        m_oTicket.setDate(new Date());
        try{
            dlReceipts.lockSharedTicket((String) m_oTicketExt, "locked");
        } catch (BasicException e) {
            new MessageInf(e).show(this);
        }

        if ("restaurant".equals(m_App.getProperties().getProperty("machine.ticketsbag"))
                && !oTicket.getOldTicket()) {
          if (restDB.getCustomerNameInTable(oTicketExt.toString()) == null) {
            if (m_oTicket.getCustomer() != null) {
              restDB.setCustomerNameInTable(m_oTicket.getCustomer().toString(),
                      oTicketExt.toString());
            }
          }
        if (restDB.getWaiterNameInTable(oTicketExt.toString()) == null
                || "".equals(restDB.getWaiterNameInTable(oTicketExt.toString()))) {
            restDB.setWaiterNameInTable(m_App.getAppUserView().getUser().getName(),
                  oTicketExt.toString());
        }

        restDB.setTicketIdInTable(m_oTicket.getId(), oTicketExt.toString());
        restDB.setGuestsInTable(restDB.getGuestsInTable(m_oTicket.getId()), m_oTicket.getId());
        restDB.setOccupied(m_oTicket.getId());
      }

        if(m_oTicketExt != null) {
            isOrdered = dlSystem.getSharedTicketOrdered(m_oTicketExt.toString());
        }
    }

    if ((m_oTicket != null) && (((Boolean.parseBoolean(m_App.getProperties()
            .getProperty("table.showwaiterdetails")))
            || (Boolean.valueOf(m_App.getProperties().getProperty(
            "table.showcustomerdetails")))))) {
    }

    if ((m_oTicket != null) && (((Boolean.valueOf(m_App.getProperties()
            .getProperty("table.showcustomerdetails"))) ||
            (Boolean.parseBoolean(m_App.getProperties().getProperty("table.showwaiterdetails")))))) {
      if (restDB.getTableMovedFlag(m_oTicket.getId())) {
        restDB.moveCustomer(oTicketExt.toString(), m_oTicket.getId());
      }
    }

    executeEvent(m_oTicket, m_oTicketExt, "ticket.show");

    if (m_App.getAppUserView().getUser().hasPermission("sales.PrintRemote")) {
      j_btnRemotePrt.setEnabled(true);

    } else {
      j_btnRemotePrt.setEnabled(false);
    }

    if (m_oTicketExt != null) {
        try {
            java.util.List<SharedTicketInfo> ticketLists = dlReceipts.getSharedTicketListById((String) m_oTicketExt);

            if (ticketLists.size() < 2) m_jCombo.setVisible(false);
            else m_jCombo.setVisible(true);
            ticketPopup.removeAll();
            for (int i=0; i<ticketLists.size(); i++) {
                ticketPopup.add(new JMenuItem(new AbstractAction(ticketLists.get(i).getId()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        changeTicket(e.getActionCommand());
                    }
                }));
            }
        } catch (BasicException ex) {
          log.error(ex.getMessage());
        }
    }

    refreshTicket();

    if(isClosed) {
        m_jOptions.setVisible(false);
        m_jPanelBag.setVisible(false);
        jPanel7.setVisible(false);
        m_jCatalog.enabledButton(false);
        j_mDiscount.setVisible(false);
        m_jDelete.setEnabled(false);
        m_jEditLine.setEnabled(false);
        jOption.setEnabled(false);
        m_jOptions.setVisible(false);
        m_jDelete.setVisible(false);
        jPanel8.setVisible(false);
        j_mCancel.setVisible(false);

        j_mVoid.setVisible(true);
        j_mBack.setVisible(true);
    } else {
        m_jOptions.setVisible(true);
        m_jPanelBag.setVisible(true);
        jPanel7.setVisible(true);
        m_jCatalog.enabledButton(true);
        j_mDiscount.setVisible(true);
        m_jDelete.setEnabled(true);
        m_jEditLine.setEnabled(true);
        jOption.setEnabled(true);
        m_jOptions.setVisible(true);
        m_jDelete.setVisible(true);
        jPanel8.setVisible(true);
        j_mCancel.setVisible(true);

        j_mVoid.setVisible(false);
        j_mBack.setVisible(false);
    }
  }

  /**
   * @return
   */
    @Override
    public TicketInfo getActiveTicket() {
        return m_oTicket;
    }

    @Override
    public void setClosedTicketID(String id) {
        closedTicket = id;
    }

   /**
   * @return
   */
    @Override
    public String getActivePlaceID() {
        return m_oTicketExt.toString();
    }

  private void refreshTicket() {
    CardLayout cl = (CardLayout) (getLayout());
    if (m_oTicket == null) {
      m_jTicketId.setText(null);
      m_ticketlines.clearTicketLines();

      m_jSubtotalEuros.setText(null);
      m_jTaxesEuros.setText(null);
      m_jTip.setText(null);
      m_jTotalEuros.setText(null);
      jCheckStock.setText(null);

      checkStock();
      stateToZero();
      repaint();

      cl.show(this, "null");

      if ((m_oTicket != null) && (m_oTicket.getLinesCount() == 0)) {
        resetSouthComponent();
      }
    } else {
      if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
        m_jEditLine.setVisible(false);
//        m_jList.setVisible(false);
      }

      m_oTicket.getLines().forEach((line) -> {
        line.setTaxInfo(taxeslogic.getTaxInfo(line
                .getProductTaxCategoryID(), m_oTicket.getCustomer()));
      });

    String ticketName = "";

    if(m_oTicketExt != null) {
        if (m_oTicketExt.toString().contains("[")) {
            String[] temp = m_oTicketExt.toString().split("\\[");
            String tempName = m_oTicket.getName();

            ticketName = temp[0] + " - " + tempName + "[" + temp[1];
        } else {
            String tempName = m_oTicket.getName();
            ticketName = m_oTicketExt.toString() + " - " + tempName;
        }
    }
    
    m_jTicketId.setText(ticketName); 


      m_ticketlines.clearTicketLines();
     
      for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
        m_ticketlines.addTicketLine(m_oTicket.getLine(i));
      }
// set line here?

      countArticles();
      printPartialTotals();
      stateToZero();

      cl.show(this, "ticket");

      if (m_oTicket.getLinesCount() == 0) {
        resetSouthComponent();
      }

      m_jKeyFactory.setText(null);
      java.awt.EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          m_jKeyFactory.requestFocus();
        }
      });
    }
    if (m_jCatalog != null){
      m_jCatalog.showSeatData();
    }
  }

  private void countArticles() {
    oCount = count;                             // existing line before change
    count = (int) m_oTicket.getArticlesCount(); //existing line after change

    if (m_oTicket != null) {
      for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
        if (m_App.getAppUserView().getUser().hasPermission("sales.Total")
                && m_oTicket.getArticlesCount() > 1) {
          btnSplit.setEnabled(true);
        } else {
          btnSplit.setEnabled(false);
        }
      }
    }
  }

  private boolean changeCount(boolean pinOK) {

    pinOK = false;

    if (m_oTicket != null) {

      if (m_App.getProperties().getProperty("override.check").equals("true")) {
        Integer secret = Integer.parseInt(m_App.getProperties().getProperty("override.pin"));
        Integer iValue = JDiscountPop.showEditNumber(this, AppLocal.getIntString("title.override.enterpin"));

        if (iValue == null ? secret == null : iValue.equals(secret)) {
          pinOK = true;
          return pinOK;

        } else {
          pinOK = false;
          JOptionPane.showMessageDialog(this, AppLocal.getIntString("message.override.badpin"));
          return pinOK;
        }
      }
    }
    return pinOK;
  }

  private void printPartialTotals() {

    if (m_oTicket.getLinesCount() == 0) {
      m_jSubtotalEuros.setText(null);
      m_jTaxesEuros.setText(null);
      m_jTip.setText(null);
      m_jTotalEuros.setText(null);
    } else {
      m_jSubtotalEuros.setText(m_oTicket.printSubTotal());
      m_jTaxesEuros.setText(m_oTicket.printTax());
      m_jTip.setText(m_oTicket.printTipCash());
      m_jTotalEuros.setText(m_oTicket.printTotal());
    }
    repaint();
  }

  private void paintTicketLine(int index, TicketLineInfo oLine) {
    if (executeEventAndRefresh("ticket.setline",
            new ScriptArg("index", index), new ScriptArg("line", oLine)) == null) {

      m_oTicket.setLine(index, oLine);
      m_ticketlines.setTicketLine(index, oLine);
      m_ticketlines.setSelectedIndex(index);

      oCount = count;     // pass line old multiplier value

      countArticles();
      visorTicketLine(oLine);
      printPartialTotals();
      stateToZero();

      executeEventAndRefresh("ticket.change");
    }
  }

  private void addTicketLine(ProductInfoExt oProduct, double dMul, double dPrice) {

//        if (oProduct.isVprice() || oProduct.getID().equals("xxx999_999xxx_x9x9x9")){
    if (oProduct.isVprice()) {
      TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(), m_oTicket.getCustomer());

      if (m_jaddtax.isSelected()) {
        dPrice /= (1 + tax.getRate());
      }

      addTicketLine(new TicketLineInfo(oProduct, dMul, dPrice, tax,
              (java.util.Properties) (oProduct.getProperties().clone())));

    } else if (oProduct.getID().equals("xxx998_998xxx_x8x8x8")) {

      if (m_App.getProperties().getProperty("till.SCOnOff").equals("true")) {
        TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(),
                m_oTicket.getCustomer());
        String SCRate = (m_App.getProperties().getProperty("till.SCRate"));

        double scharge;
        scharge = Double.parseDouble(SCRate);
        scharge = m_oTicket.getTotal() * (scharge / 100);

        addTicketLine(new TicketLineInfo(oProduct, 1, scharge, tax,
                (java.util.Properties) (oProduct.getProperties().clone())));

      } else {
        JOptionPane.showMessageDialog(this, "Service Charge Not Enabled");
      }

    } else {
// get the line product tax
      TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(), m_oTicket.getCustomer());
      addTicketLine(new TicketLineInfo(oProduct, dMul, dPrice, tax,
              (java.util.Properties) (oProduct.getProperties().clone()),TicketInfo.ticketlineSeat));

//            if (oProduct.getID().equals("xxx999_999xxx_x9x9x9")){
//                m_jEditLine.doClick();
//            }
      refreshTicket();

    }

    j_btnRemotePrt.setEnabled(true);

  }

  /**
   * @param oLine
   */
  protected void addTicketLine(TicketLineInfo oLine) {
    oLine.setUser(m_App.getAppUserView().getUser().getUserInfo());
    if (executeEventAndRefresh("ticket.addline", new ScriptArg("line", oLine)) == null) {
      if (oLine.isProductCom()) {
        int i = m_ticketlines.getSelectedIndex();

        if (i >= 0 && !m_oTicket.getLine(i).isProductCom()) {
          i++;
        }

        while (i >= 0 && i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
          i++;
        }

        if (i >= 0) {
          m_oTicket.insertLine(i, oLine);
          m_ticketlines.insertTicketLine(i, oLine);
        } else {
          Toolkit.getDefaultToolkit().beep();
        }
      } else {
        m_oTicket.addLine(oLine);
        m_ticketlines.addTicketLine(oLine);

//        try {
//          int i = m_ticketlines.getSelectedIndex();
//          TicketLineInfo line = m_oTicket.getLine(i);
//
//          if (line.isProductVerpatrib()) {
//            JProductAttEdit2 attedit = JProductAttEdit2.getAttributesEditor(this, m_App.getSession());
//            attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
//            attedit.setVisible(true);
//
//            if (attedit.isOK()) {
//              line.setProductAttSetInstId(attedit.getAttributeSetInst());
//              line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
//              paintTicketLine(i, line);
//            }
//          }
//
//        } catch (BasicException ex) {
//          MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
//                  AppLocal.getIntString("message.cannotfindattributes"), ex);
//          msg.show(this);
//        }
      }

      visorTicketLine(oLine);
      printPartialTotals();
      stateToZero();
      checkStock();
      countArticles();

      executeEvent(m_oTicket, m_oTicketExt, "ticket.change");
    }
  }

  private void removeTicketLine(int i) {

//        if (m_App.getProperties().getProperty("override.check").equals("true")) {
    if (!m_oTicket.getLine(i).getUpdated()) {
      JOptionPane.showMessageDialog(this
              , AppLocal.getIntString("message.deletelinesent")
              , AppLocal.getIntString("label.deleteline")
              , JOptionPane.WARNING_MESSAGE);
    } else {
      if (executeEventAndRefresh("ticket.removeline", new ScriptArg("index", i)) == null) {
        String ticketID = Integer.toString(m_oTicket.getTicketId());
        if (m_oTicket.getTicketId() == 0) {
          ticketID = "Void";
        }
        dlSystem.execLineRemoved(
                new Object[]{
                        m_App.getAppUserView().getUser().getName(),
                        ticketID,
                        m_oTicket.getLine(i).getProductID(),
                        m_oTicket.getLine(i).getProductName(),
                        m_oTicket.getLine(i).getMultiply()
                }
        );

        if (m_oTicket.getLine(i).isProductCom()) {
          m_oTicket.removeLine(i);
          m_ticketlines.removeTicketLine(i);
        } else {
          if (i < 1) {

            if (m_App.getAppUserView().getUser().hasPermission("sales.DeleteLines")) {
              int input = JOptionPane.showConfirmDialog(this,
                      AppLocal.getIntString("message.deletelineyes")
                      , AppLocal.getIntString("label.deleteline"), JOptionPane.YES_NO_OPTION);

              if (input == 0) {
                m_oTicket.removeLine(i);
                m_ticketlines.removeTicketLine(i);
              }
            } else {
              JOptionPane.showMessageDialog(this,
                      AppLocal.getIntString("message.deletelineno")
                      , AppLocal.getIntString("label.deleteline"), JOptionPane.WARNING_MESSAGE);
            }
          } else {
            m_oTicket.removeLine(i);
            m_ticketlines.removeTicketLine(i);

            while (i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
              m_oTicket.removeLine(i);
              m_ticketlines.removeTicketLine(i);
            }
          }
        }

        visorTicketLine(null);
        printPartialTotals();
        stateToZero();
        checkStock();
        countArticles();

        executeEventAndRefresh("ticket.change");
      }
    }
//        }
  }

    private void removeTicketLineForUpdate(int i) {

    if (executeEventAndRefresh("ticket.removeline", new ScriptArg("index", i)) == null) {
        String ticketID = Integer.toString(m_oTicket.getTicketId());
        if (m_oTicket.getTicketId() == 0) {

          ticketID = "Void";
        }
        dlSystem.execLineRemoved(
            new Object[]{
                m_App.getAppUserView().getUser().getName(),
                ticketID,
                m_oTicket.getLine(i).getProductID(),
                m_oTicket.getLine(i).getProductName(),
                m_oTicket.getLine(i).getMultiply()
            }
        );

        if (m_oTicket.getLine(i).isProductCom()) {
            m_oTicket.removeLine(i);
            m_ticketlines.removeTicketLine(i);
        } else {
            if (m_App.getAppUserView().getUser().hasPermission("sales.DeleteLines")) {
                m_oTicket.removeLine(i);
                m_ticketlines.removeTicketLine(i);
            } else {
                m_oTicket.removeLine(i);
                m_ticketlines.removeTicketLine(i);

                while (i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                  m_oTicket.removeLine(i);
                  m_ticketlines.removeTicketLine(i);
                }
            }

            visorTicketLine(null);
            printPartialTotals();
            stateToZero();
            checkStock();
            countArticles();

            executeEventAndRefresh("ticket.change");
        }
    }
 }
  private ProductInfoExt getInputProduct() {
    ProductInfoExt oProduct = new ProductInfoExt();
// Always add Default Prod ID + Add Name to Misc. if empty
    oProduct.setID("xxx999_999xxx_x9x9x9");
    oProduct.setReference("xxx999");
    oProduct.setCode("xxx999");
    oProduct.setName("***");
    oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel
            .getSelectedItem()).getID());
    oProduct.setPriceSell(includeTaxes(oProduct.getTaxCategoryID(), getInputValue()));

    return oProduct;
  }

  private double includeTaxes(String tcid, double dValue) {
    if (m_jaddtax.isSelected()) {
      TaxInfo tax = taxeslogic.getTaxInfo(tcid, m_oTicket.getCustomer());
      double dTaxRate = tax == null ? 0.0 : tax.getRate();
      return dValue / (1.0 + dTaxRate);
    } else {
      return dValue;
    }
  }

  private double excludeTaxes(String tcid, double dValue) {
    TaxInfo tax = taxeslogic.getTaxInfo(tcid, m_oTicket.getCustomer());
    double dTaxRate = tax == null ? 0.0 : tax.getRate();
    return dValue / (1.0 + dTaxRate);
  }


  private double getInputValue() {
    try {
      return Double.parseDouble(m_jPrice.getText());
    } catch (NumberFormatException e) {
      return 0.0;
    }
  }

  private double getPorValue() {
    try {
      return Double.parseDouble(m_jPor.getText().substring(1));
    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
      return 1.0;
    }
  }

  private void stateToZero() {
    m_jPor.setText("");
    m_jPrice.setText("");
    m_sBarcode = new StringBuffer();

    m_iNumberStatus = NUMBER_INPUTZERO;
    m_iNumberStatusInput = NUMBERZERO;
    m_iNumberStatusPor = NUMBERZERO;
    repaint();
  }

  private void incProductByCode(String sCode) {

    try {
      ProductInfoExt oProduct = dlSales.getProductInfoByCode(sCode);

      if (oProduct == null) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                sCode + " - " + AppLocal.getIntString("message.noproduct"),
                "Check", JOptionPane.WARNING_MESSAGE);
        stateToZero();
      } else {
        incProduct(oProduct);
      }
    } catch (BasicException eData) {
      stateToZero();
      new MessageInf(eData).show(this);
    }
  }

  private void incProductByCodePrice(String sCode, double dPriceSell) {
    try {
      ProductInfoExt oProduct = dlSales.getProductInfoByCode(sCode);
      if (oProduct == null) {
        Toolkit.getDefaultToolkit().beep();
        new MessageInf(MessageInf.SGN_WARNING, AppLocal
                .getIntString("message.noproduct")).show(this);
        stateToZero();
      } else {
        if (m_jaddtax.isSelected()) {
          TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(), m_oTicket.getCustomer());
          addTicketLine(oProduct, 1.0, dPriceSell / (1.0 + tax.getRate()));
        } else {
          addTicketLine(oProduct, 1.0, dPriceSell);
        }
      }
    } catch (BasicException eData) {
      stateToZero();
      new MessageInf(eData).show(this);
    }
  }

  private void incProduct(ProductInfoExt prod) {

    if (prod.isScale() && m_App.getDeviceScale().existsScale()) {
      try {
        Double value = m_App.getDeviceScale().readWeight();
        if (value != null) {
          incProduct(value, prod);
        }
      } catch (ScaleException e) {
        Toolkit.getDefaultToolkit().beep();
        new MessageInf(MessageInf.SGN_WARNING, AppLocal
                .getIntString("message.noweight"), e).show(this);
        stateToZero();
      }
    } else {
      if (!prod.isVprice()) {
        incProduct(TicketInfo.ticketlineQuantity, prod);
      } else {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                AppLocal.getIntString("message.novprice"));
      }
    }
  }

  private void incProduct(double dPor, ProductInfoExt prod) {

    if (prod.isVprice()) {
      addTicketLine(prod, getPorValue(), getInputValue());
    } else {
      addTicketLine(prod, dPor, prod.getPriceSell()); // Nam Ho Added
    }
  }

  /**
   * @param prod
   */
  protected void buttonTransition(ProductInfoExt prod) {
    if (m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
      incProduct(prod);
    } else if (m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
      incProduct(getInputValue(), prod);
    } else if (prod.isVprice()) {
      addTicketLine(prod, getPorValue(), getInputValue());
    } else {
      Toolkit.getDefaultToolkit().beep();
    }
  }

  @SuppressWarnings("empty-statement")
  private void stateTransition(char cTrans) {

    if ((cTrans == ENTER_EVENT) || (cTrans == UNKNOWN) || (cTrans == PASTE_EVENT)) {

      if (m_sBarcode.length() > 0) {

        String sCode = m_sBarcode.toString();
        String sCodetype = "EAN";                                           // Declare EAN. It's default

        if ("true".equals(m_App.getProperties().getProperty("machine.barcodetype"))) {
          sCodetype = "UPC";
        } else {
          sCodetype = "EAN";                                              // Ensure not null
        }

        if (sCode.startsWith("C") || sCode.startsWith("c")) {
          try {
            String card = sCode;
            CustomerInfoExt newcustomer = dlSales.findCustomerExt(card);

            if (newcustomer == null) {
              Toolkit.getDefaultToolkit().beep();
              new MessageInf(MessageInf.SGN_WARNING, AppLocal
                      .getIntString("message.nocustomer")).show(this);
            } else {
              m_oTicket.setCustomer(newcustomer);
              m_jTicketId.setText(m_oTicket.getName());
            }
          } catch (BasicException e) {
            Toolkit.getDefaultToolkit().beep();
            new MessageInf(MessageInf.SGN_WARNING, AppLocal
                    .getIntString("message.nocustomer"), e).show(this);
          }
          stateToZero();

        } else if (sCode.startsWith(";")) {
          stateToZero();


          // START OF BARCODE PARSING
          /*  This block is deliberately verbose and is base for future scanner handling
           *  Some scanners inject a CR+LF... some don't...
           *  stateTransition() must allow for this as these add characters to .length()
           *  First 3 digits are GS1 CountryCode OR Retailer internal use
           *
           *  Prefix   ManCodeProdCode    CheckCode
           *  PPP      MMMMMCCCCC         K
           *  012      3456789012         K
           *  Barcode CCCCC must be unique
           *  Notes:
           *      ManufacturerCode and ProductCode must be exactly 10 digits
           *      If code begins with 0 then is actually a UPC-A with prepended 0
           *
           *  Roxy Pos Retailer instore uses these RULES
           *  Prefixes 020 to 029 are set aside for Retailer internal use
           *  This means that CCCC becomes price/weight values
           *  Prefixes 978 and 979 are set aside for ISBN - Future use
           *
           *  Prefix   ManCode    ProdCode   CheckCode
           *  PPP      MMMMM      CCCCC       K           Format
           *  012      34567      89012       K           Human
           *
           */

        } else if ("EAN".equals(sCodetype)
                && ((sCode.startsWith("2")) || (sCode.startsWith("02"))) // check code prefix
                && ((sCode.length() == 13) || (sCode.length() == 12))) { // check code length variances

          try {
            ProductInfoExt oProduct = dlSales.getProductInfoByShortCode(sCode);// get product(s) with PMMMMM

            if (oProduct == null) {                                      // nothing returned so display message to user
              Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(null,
                      sCode + " - "
                              + AppLocal.getIntString("message.noproduct"),
                      "Check", JOptionPane.WARNING_MESSAGE);
              stateToZero();                                          // clear the user input

            } else if ("EAN-13".equals(oProduct.getCodetype())) {        // have a valid barcode
              oProduct.setProperty("product.barcode", sCode);         // set the screen's barcode from input
              double dPriceSell = oProduct.getPriceSell();            // default price for product
              double weight = 0;                                      // used if barcode includes weight of product
              double dUnits = 0;                                      // used for pro-rata unit
              String sVariableTypePrefix = sCode.substring(0, 2);     // get first two PPP digits
              String sVariableNum;                                    // CCCCC variable value of barcode

              if (sCode.length() == 13) {                             // full barcode from scanner
                sVariableNum = sCode.substring(8, 12);              // get the 5 CCCCC digits
              } else {                                                // barcode can be any length
                sVariableNum = sCode.substring(7, 11);              // get the 5 CCCCC digits
              }                                                       // scanner has dropped 1st digit so shift get to left

//  PRICE - SET value decimals
              switch (sVariableTypePrefix) {                          // Use CCCCC value of 01049 as example
                case "02":                                          // first 2 PPP digits determine decimal position
                  dUnits = (Double.parseDouble(sVariableNum)      // position decimal in CCC.CC
                          / 100) / oProduct.getPriceSell();       // 2 decimal = 010.49
                  break;
                case "20":
                  dUnits = (Double.parseDouble(sVariableNum)      // position decimal in CCC.CC
                          / 100) / oProduct.getPriceSell();       // 2 decimal = 010.49
                  break;
                case "21":
                  dUnits = (Double.parseDouble(sVariableNum)      // position decimal in CC.CCC
                          / 10) / oProduct.getPriceSell();        // 2 decimal = 0104.9
                  break;
                case "22":
                  dUnits = Double.parseDouble(sVariableNum)       // position decimal in CCCC.C
                          / oProduct.getPriceSell();              // Price = 01049.
                  break;

//  WEIGHT - SET value decimals
                case "23":                                          // Use CCCCC 01049kg as example
                  weight = Double.parseDouble(sVariableNum)
                          / 1000;                                 // Weight = 01.049
                  dUnits = weight;                                // set Units for price calculation
                  break;
                case "24":
                  weight = Double.parseDouble(sVariableNum)
                          / 100;                                  // Weight = 010.49
                  dUnits = weight;                                // set Units for price calculation
                  break;
                case "25":
                  weight = Double.parseDouble(sVariableNum)
                          / 10;                                   // Weight = 0104.9
                  dUnits = weight;                                // set Units for price calculation
                  break;
                default:
                  break;
              }

              TaxInfo tax = taxeslogic                                // get the TaxRate for the product
                      .getTaxInfo(oProduct.getTaxCategoryID()
                              , m_oTicket.getCustomer());                         // calculate if ticket has a Customer

              switch (sVariableTypePrefix) {
//  PRICE - Assign var's
                case "02":                                          // now we need to calculate some values
                  dPriceSell = oProduct.getPriceSellTax(tax)
                          / (1.0 + tax.getRate());                    // selling price with tax
                  dUnits = (Double.parseDouble(sVariableNum)
                          / 100) / oProduct.getPriceSellTax(tax);     // Units as proportion of selling price
                  oProduct.setProperty("product.price"
                          , Double.toString(oProduct.getPriceSell())); // push to screen
                  break;
                case "20":                                          // as above
                  dPriceSell = oProduct.getPriceSellTax(tax)
                          / (1.0 + tax.getRate());
                  dUnits = (Double.parseDouble(sVariableNum)
                          / 100) / oProduct.getPriceSellTax(tax);
                  oProduct.setProperty("product.price"
                          , Double.toString(oProduct.getPriceSellTax(tax)));
                  break;
                case "21":
                  dPriceSell = oProduct.getPriceSellTax(tax)
                          / (1.0 + tax.getRate());
                  dUnits = (Double.parseDouble(sVariableNum)
                          / 10) / oProduct.getPriceSellTax(tax);
                  oProduct.setProperty("product.price"
                          , Double.toString(oProduct.getPriceSell()));
                  break;
                case "22":
                  dPriceSell = oProduct.getPriceSellTax(tax)
                          / (1.0 + tax.getRate());
                  dUnits = (Double.parseDouble(sVariableNum)
                          / 1) / oProduct.getPriceSellTax(tax);
                  oProduct.setProperty("product.price"
                          , Double.toString(oProduct.getPriceSell()));
                  break;

// WEIGHT - Assign variable to Unit
                case "23":
                  weight = Double.parseDouble(sVariableNum)
                          / 1000;                                     // 3 decimals = 01.049 kg
                  dUnits = weight;                                // which represents 1gramme Units
                  oProduct.setProperty("product.weight"
                          , Double.toString(weight));
                  oProduct.setProperty("product.price"
                          , Double.toString(dPriceSell));
                  break;
                case "24":
                  weight = Double.parseDouble(sVariableNum)
                          / 100;                                      // 2 decimals = 010.49 kg
                  dUnits = weight;                                // which represents 10gramme Units
                  oProduct.setProperty("product.weight"
                          , Double.toString(weight));
                  oProduct.setProperty("product.price"
                          , Double.toString(dPriceSell));
                  break;
                case "25":
                  weight = Double.parseDouble(sVariableNum)
                          / 10;                                       // 1 decimal = 0104.9 kg
                  dUnits = weight;                                // which represents 100gramme Units
                  oProduct.setProperty("product.weight"
                          , Double.toString(weight));
                  oProduct.setProperty("product.price"
                          , Double.toString(dPriceSell));
                  break;

/*
 *  Some countries use different barcode prefix 26-29 or 250 etc.
 *  Use this section to add more case statements but these are not mandatory
 *  If you have your own internal or other barcode schema then...
 *  Example:
        case "28":
        {
        // price has tax. Remove it from sPriceSell
            TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(), m_oTicket.getCustomer());
            dPriceSell /= (1.0 + tax.getRate());
            oProduct.setProperty("product.price", Double.toString(dPriceSell));
            weight = -1.0;
        break;
*/
                default:
                  break;
              }

              if (m_jaddtax.isSelected()) {
                addTicketLine(oProduct
                        , dUnits //weight
                        , dPriceSell = oProduct.getPriceSellTax(tax));
              } else {
                addTicketLine(oProduct
                        , dUnits
                        , dPriceSell);
              }
            }
          } catch (BasicException eData) {
            stateToZero();
            new MessageInf(eData).show(this);
          }

// UPC-A
/* Note: if begins 02 then its a standard
// UPC-A max value limitation is 4 digit price
// UPC-A Extended uses State digit to give 5 digit price
// Roxy Pos does not support UPC-A Extended at this time
// Identifier   Prod    State   Cost    CheckCode
// I            PPPPP   S       CCCC    K
// 1            23456   7       8901    2

 *    0 = Standard UPC number (must have a zero to do zero-suppressed numbers)
 *    1 = Reserved
 *    2 = Random-weight items (fruits, vegetables, meats, etc.)
 *    3 = Pharmaceuticals
 *    4 = In-store marketing for retailers (Other stores will not understand)
 *    5 = Coupons
 *    6 = Standard UPC number
 *    7 = Standard UPC number
 *    8 = Reserved
 *    9 = Reserved
*/

        } else if ("UPC".equals(sCodetype)
                && (sCode.startsWith("2"))
                && (sCode.length() == 12)) {

          try {
            ProductInfoExt oProduct = dlSales.getProductInfoByUShortCode(sCode);// Return only UPC product

            if (oProduct == null) {
              Toolkit.getDefaultToolkit().beep();
              JOptionPane.showMessageDialog(null,
                      sCode + " - "
                              + AppLocal.getIntString("message.noproduct"),
                      "Check", JOptionPane.WARNING_MESSAGE);
              stateToZero();
            } else if ("Upc-A".equals(oProduct.getCodetype())) {
              oProduct.setProperty("product.barcode", sCode);
              double dPriceSell = oProduct.getPriceSell();            // default price for product
              double weight = 0;                                      // used if barcode includes weight of product
              double dUnits = 0;                                      // used for pro-rata unit
              String sVariableNum = sCode.substring(7, 11);           // grab the value from the code only using 4 digit price

              TaxInfo tax = taxeslogic                                // get the TaxRate for the product
                      .getTaxInfo(oProduct.getTaxCategoryID()
                              , m_oTicket.getCustomer());

              if (oProduct.getPriceSell() != 0.0) {                       // we have a weight barcode
                weight = Double.parseDouble(sVariableNum) / 100;        // 2 decimals (e.g. 10.49 kg)
                dUnits = weight;                                        // Units is now transformed to weight

                oProduct.setProperty("product.weight"                   // catch-all for weight
                        , Double.toString(weight));
                oProduct.setProperty("product.price"                    // get the prod sellprice
                        , Double.toString(oProduct.getPriceSell()));
                dPriceSell = oProduct.getPriceSellTax(tax);             // calculate the tax on sellprice
                dUnits = (Double.parseDouble(sVariableNum)              // calculate Units in sellprice with Tax
                        / 100)
                        / oProduct.getPriceSellTax(tax);

              } else {                                                    // no sellprice so we have a price barcode
                dPriceSell = (Double.parseDouble(sVariableNum)          // calculate Units in sellprice with Tax
                        / 100);
                dUnits = 1;                                             // no sellprice to calculate so must be 1 Unit
              }

              if (m_jaddtax.isSelected()) {
                addTicketLine(oProduct
                        , dUnits
                        , dPriceSell);
              } else {
                addTicketLine(oProduct
                        , dUnits
                        , dPriceSell / (1.0 + tax.getRate()));
              }
            }
          } catch (BasicException eData) {
            stateToZero();
            new MessageInf(eData).show(this);
          }

        } else {
          incProductByCode(sCode);                                        // returned is standard so go get it
        }
// END OF BARCODE

      } else {
        Toolkit.getDefaultToolkit().beep();
      }

    } else {

      m_sBarcode.append(cTrans);

      if (cTrans == '\u007f') {
        stateToZero();

      } else if ((cTrans == '0') && (m_iNumberStatus == NUMBER_INPUTZERO)) {
        m_jPrice.setText(Character.toString('0'));

      } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3'
              || cTrans == '4' || cTrans == '5' || cTrans == '6'
              || cTrans == '7' || cTrans == '8' || cTrans == '9')
              && (m_iNumberStatus == NUMBER_INPUTZERO)) {

        if (!priceWith00) {
          m_jPrice.setText(m_jPrice.getText() + cTrans);
        } else {
          m_jPrice.setText(setTempjPrice(m_jPrice.getText() + cTrans));
        }

        m_iNumberStatus = NUMBER_INPUTINT;
        m_iNumberStatusInput = NUMBERVALID;

      } else if ((cTrans == '0' || cTrans == '1' || cTrans == '2'
              || cTrans == '3' || cTrans == '4' || cTrans == '5'
              || cTrans == '6' || cTrans == '7' || cTrans == '8'
              || cTrans == '9')
              && (m_iNumberStatus == NUMBER_INPUTINT)) {

        if (!priceWith00) {
          m_jPrice.setText(m_jPrice.getText() + cTrans);
        } else {
          m_jPrice.setText(setTempjPrice(m_jPrice.getText() + cTrans));
        }


      } else if (cTrans == '.'
              && m_iNumberStatus == NUMBER_INPUTZERO && !priceWith00) {
        m_jPrice.setText("0.");
        m_iNumberStatus = NUMBER_INPUTZERODEC;
      } else if (cTrans == '.'
              && m_iNumberStatus == NUMBER_INPUTZERO) {
        m_jPrice.setText("");
        m_iNumberStatus = NUMBER_INPUTZERO;
      } else if (cTrans == '.'
              && m_iNumberStatus == NUMBER_INPUTINT && !priceWith00) {
        m_jPrice.setText(m_jPrice.getText() + ".");
        m_iNumberStatus = NUMBER_INPUTDEC;
      } else if (cTrans == '.'
              && m_iNumberStatus == NUMBER_INPUTINT) {

        if (!priceWith00) {
          m_jPrice.setText(m_jPrice.getText() + "00");
        } else {
          m_jPrice.setText(setTempjPrice(m_jPrice.getText() + "00"));
        }

        m_iNumberStatus = NUMBER_INPUTINT;

      } else if ((cTrans == '0')
              && (m_iNumberStatus == NUMBER_INPUTZERODEC
              || m_iNumberStatus == NUMBER_INPUTDEC)) {

        if (!priceWith00) {
          m_jPrice.setText(m_jPrice.getText() + cTrans);
        } else {
          m_jPrice.setText(setTempjPrice(m_jPrice.getText() + cTrans));
        }

      } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3'
              || cTrans == '4' || cTrans == '5' || cTrans == '6'
              || cTrans == '7' || cTrans == '8' || cTrans == '9')
              && (m_iNumberStatus == NUMBER_INPUTZERODEC
              || m_iNumberStatus == NUMBER_INPUTDEC)) {

        m_jPrice.setText(m_jPrice.getText() + cTrans);
        m_iNumberStatus = NUMBER_INPUTDEC;
        m_iNumberStatusInput = NUMBERVALID;

      } else if (cTrans == '*'
              && (m_iNumberStatus == NUMBER_INPUTINT
              || m_iNumberStatus == NUMBER_INPUTDEC)) {
        m_jPor.setText("x");
        m_iNumberStatus = NUMBER_PORZERO;
      } else if (cTrans == '*'
              && (m_iNumberStatus == NUMBER_INPUTZERO
              || m_iNumberStatus == NUMBER_INPUTZERODEC)) {
        m_jPrice.setText("0");
        m_jPor.setText("x");
        m_iNumberStatus = NUMBER_PORZERO;

      } else if ((cTrans == '0')
              && (m_iNumberStatus == NUMBER_PORZERO)) {
        m_jPor.setText("x0");
      } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3'
              || cTrans == '4' || cTrans == '5' || cTrans == '6'
              || cTrans == '7' || cTrans == '8' || cTrans == '9')
              && (m_iNumberStatus == NUMBER_PORZERO)) {

        m_jPor.setText("x" + Character.toString(cTrans));
        m_iNumberStatus = NUMBER_PORINT;
        m_iNumberStatusPor = NUMBERVALID;
      } else if ((cTrans == '0' || cTrans == '1' || cTrans == '2'
              || cTrans == '3' || cTrans == '4' || cTrans == '5'
              || cTrans == '6' || cTrans == '7' || cTrans == '8'
              || cTrans == '9') && (m_iNumberStatus == NUMBER_PORINT)) {

        m_jPor.setText(m_jPor.getText() + cTrans);

      } else if (cTrans == '.'
              && m_iNumberStatus == NUMBER_PORZERO && !priceWith00) {
        m_jPor.setText("x0.");
        m_iNumberStatus = NUMBER_PORZERODEC;
      } else if (cTrans == '.'
              && m_iNumberStatus == NUMBER_PORZERO) {
        m_jPor.setText("x");
        m_iNumberStatus = NUMBERVALID;
      } else if (cTrans == '.'
              && m_iNumberStatus == NUMBER_PORINT && !priceWith00) {
        m_jPor.setText(m_jPor.getText() + ".");
        m_iNumberStatus = NUMBER_PORDEC;
      } else if (cTrans == '.'
              && m_iNumberStatus == NUMBER_PORINT) {
        m_jPor.setText(m_jPor.getText() + "00");
        m_iNumberStatus = NUMBERVALID;

      } else if ((cTrans == '0')
              && (m_iNumberStatus == NUMBER_PORZERODEC
              || m_iNumberStatus == NUMBER_PORDEC)) {
        m_jPor.setText(m_jPor.getText() + cTrans);
      } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3'
              || cTrans == '4' || cTrans == '5' || cTrans == '6'
              || cTrans == '7' || cTrans == '8' || cTrans == '9')
              && (m_iNumberStatus == NUMBER_PORZERODEC || m_iNumberStatus == NUMBER_PORDEC)) {

        m_jPor.setText(m_jPor.getText() + cTrans);
        m_iNumberStatus = NUMBER_PORDEC;
        m_iNumberStatusPor = NUMBERVALID;

      } else if (cTrans == '\u00a7'
              && m_iNumberStatusInput == NUMBERVALID
              && m_iNumberStatusPor == NUMBERZERO) {

        if (m_App.getDeviceScale().existsScale()
                && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
          try {
            Double value = m_App.getDeviceScale().readWeight();
            if (value != null) {
              ProductInfoExt product = getInputProduct();
              addTicketLine(product, value, product.getPriceSell());
            }
          } catch (ScaleException e) {
            Toolkit.getDefaultToolkit().beep();
            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);
            stateToZero();
          }
        } else {

          Toolkit.getDefaultToolkit().beep();
        }
      } else if (cTrans == '\u00a7'
              && m_iNumberStatusInput == NUMBERZERO
              && m_iNumberStatusPor == NUMBERZERO) {

        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
          Toolkit.getDefaultToolkit().beep();
        } else if (m_App.getDeviceScale().existsScale()) {
          try {
            Double value = m_App.getDeviceScale().readWeight();
            if (value != null) {
              TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
              newline.setMultiply(value);
              newline.setPrice(Math.abs(newline.getPrice()));
              paintTicketLine(i, newline);
            }
          } catch (ScaleException e) {
            Toolkit.getDefaultToolkit().beep();
            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);
            stateToZero();
          }
        } else {

          Toolkit.getDefaultToolkit().beep();
        }

      } else if (cTrans == '+'
              && m_iNumberStatusInput == NUMBERZERO
              && m_iNumberStatusPor == NUMBERZERO) {
        int i = m_ticketlines.getSelectedIndex();

        if (i < 0) {
          Toolkit.getDefaultToolkit().beep();
        } else {
          TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
          //If it's a refund + button means one unit less
          if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
            if (m_App.getProperties().getProperty("override.check").equals("true")) {
              oCount = count - 1;  //increment existing line
              pinOK = false;
              changeCount(pinOK);
              newline.setMultiply(newline.getMultiply() - 1.0);
              newline.setProperty("ticket.updated", "true");
              paintTicketLine(i, newline);
            } else {
              newline.setMultiply(newline.getMultiply() - 1.0);
              newline.setProperty("ticket.updated", "true");
              paintTicketLine(i, newline);
            }
          } else {
            if (m_App.getProperties().getProperty("override.check").equals("true")) {
              oCount = count + 1;  //increment existing line
              pinOK = false;
              if (changeCount(pinOK)) {
                newline.setMultiply(newline.getMultiply() + 1.0);
                newline.setProperty("ticket.updated", "true");
                paintTicketLine(i, newline);
              }
            } else {
              newline.setMultiply(newline.getMultiply() + 1.0);
              newline.setProperty("ticket.updated", "true");
              paintTicketLine(i, newline);
            }
          }
        }
      } else if (cTrans == '-'
              && m_iNumberStatusInput == NUMBERZERO
              && m_iNumberStatusPor == NUMBERZERO
              && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {

        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
          Toolkit.getDefaultToolkit().beep();
        } else {
          TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));

          if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
            if (m_App.getProperties().getProperty("override.check").equals("true")) {
              oCount = count - 1;  //increment existing line
              pinOK = false;
              changeCount(pinOK);
              newline.setMultiply(newline.getMultiply() - 1.0);
              newline.setProperty("ticket.updated", "true");
              paintTicketLine(i, newline);
            } else {
              newline.setMultiply(newline.getMultiply() - 1.0);
              newline.setProperty("ticket.updated", "true");
              paintTicketLine(i, newline);
            }

            if (newline.getMultiply() >= 0) {
              removeTicketLine(i);
            } else {
              paintTicketLine(i, newline);
            }
          } else {
            if (m_App.getProperties().getProperty("override.check").equals("true")) {
              oCount = count - 1;  //increment existing line
              pinOK = false;
              if (changeCount(pinOK)) {
                newline.setMultiply(newline.getMultiply() - 1.0);
                newline.setProperty("ticket.updated", "true");
                paintTicketLine(i, newline);
              }
            } else {
              newline.setMultiply(newline.getMultiply() - 1.0);
              newline.setProperty("ticket.updated", "true");
              paintTicketLine(i, newline);
            }

            if (newline.getMultiply() <= 0.0) {
              removeTicketLine(i);
            } else {
              paintTicketLine(i, newline);
            }
          }
        }

      } else if (cTrans == '+'
              && m_iNumberStatusInput == NUMBERZERO
              && m_iNumberStatusPor == NUMBERVALID) {
        int i = m_ticketlines.getSelectedIndex();

        if (i < 0) {
          Toolkit.getDefaultToolkit().beep();
        } else {
          double dPor = getPorValue();
          TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));

          if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
            if (m_App.getProperties().getProperty("override.check").equals("true")) {
              oCount = count - 1;  //increment existing line
              pinOK = false;
              changeCount(pinOK);
              newline.setMultiply(-dPor);
              newline.setProperty("ticket.updated", "true");
              newline.setPrice(Math.abs(newline.getPrice()));
              paintTicketLine(i, newline);
            } else {
              newline.setMultiply(-dPor);
              newline.setProperty("ticket.updated", "true");
              newline.setPrice(Math.abs(newline.getPrice()));
              paintTicketLine(i, newline);
            }
          } else {
            if (m_App.getProperties().getProperty("override.check").equals("true")) {
              oCount = count + 1;  //increment existing line
              pinOK = false;
              if (changeCount(pinOK)) {
                newline.setMultiply(dPor);
                newline.setProperty("ticket.updated", "true");
                newline.setPrice(Math.abs(newline.getPrice()));
                paintTicketLine(i, newline);
              }
            } else {
              newline.setMultiply(dPor);
              newline.setProperty("ticket.updated", "true");
              newline.setPrice(Math.abs(newline.getPrice()));
              paintTicketLine(i, newline);
            }
          }
        }
      } else if (cTrans == '-'
              && m_iNumberStatusInput == NUMBERZERO
              && m_iNumberStatusPor == NUMBERVALID
              && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
        int i = m_ticketlines.getSelectedIndex();

        if (i < 0) {
          Toolkit.getDefaultToolkit().beep();
        } else {
          double dPor = getPorValue();
          TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));

          if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
            if (m_App.getProperties().getProperty("override.check").equals("true")) {
              oCount = count - 1;  //increment existing line
              pinOK = false;
              changeCount(pinOK);
              newline.setMultiply(-dPor);
              newline.setProperty("ticket.updated", "true");
              newline.setPrice(Math.abs(newline.getPrice()));
              paintTicketLine(i, newline);
            } else {
              newline.setMultiply(-dPor);
              newline.setProperty("ticket.updated", "true");
              newline.setPrice(Math.abs(newline.getPrice()));
              paintTicketLine(i, newline);
            }
          } else {
            if (m_App.getProperties().getProperty("override.check").equals("true")) {
              oCount = count - 1;  //increment existing line
              pinOK = false;
              if (changeCount(pinOK)) {
                newline.setMultiply(dPor);
                newline.setProperty("ticket.updated", "true");
                newline.setPrice(Math.abs(newline.getPrice()));
                paintTicketLine(i, newline);
              }
            } else {
              newline.setMultiply(dPor);
              newline.setProperty("ticket.updated", "true");
              newline.setPrice(Math.abs(newline.getPrice()));
              paintTicketLine(i, newline);
            }
          }
        }
      } else if (cTrans == '+'
              && m_iNumberStatusInput == NUMBERVALID
              && m_iNumberStatusPor == NUMBERZERO
              && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
        ProductInfoExt product = getInputProduct();
        addTicketLine(product, 1.0, product.getPriceSell());
        m_jEditLine.doClick();

      } else if (cTrans == '-'
              && m_iNumberStatusInput == NUMBERVALID
              && m_iNumberStatusPor == NUMBERZERO
              && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
        ProductInfoExt product = getInputProduct();
        addTicketLine(product, 1.0, -product.getPriceSell());
        m_jEditLine.doClick();

      } else if (cTrans == '+'
              && m_iNumberStatusInput == NUMBERVALID
              && m_iNumberStatusPor == NUMBERVALID
              && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
        ProductInfoExt product = getInputProduct();
        addTicketLine(product, getPorValue(), product.getPriceSell());

      } else if (cTrans == '-'
              && m_iNumberStatusInput == NUMBERVALID
              && m_iNumberStatusPor == NUMBERVALID
              && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
        ProductInfoExt product = getInputProduct();
        addTicketLine(product, getPorValue(), -product.getPriceSell());

      } else if (cTrans == ' ' || cTrans == '=') {
        if (m_oTicket.getLinesCount() > 0) {
          if (closeTicket(m_oTicket, m_oTicketExt)) {
            m_ticketsbag.deleteTicket();
            String autoLogoff = (m_App.getProperties().getProperty("till.autoLogoff"));
            if (autoLogoff != null) {
              if (autoLogoff.equals("true")) {
                if ("restaurant".equals(
                        m_App.getProperties().getProperty("machine.ticketsbag"))
                        && ("true".equals(m_App.getProperties().getProperty("till.autoLogoffrestaurant")))) {
                  deactivate();
                  setActiveTicket(null, null, false);
                } else {
                  ((JRootApp) m_App).closeAppView();
                }
              }
            }
            ;
          } else {
            refreshTicket();
          }
        } else {
          Toolkit.getDefaultToolkit().beep();
        }
      } else if (cTrans == 't'
              && m_iNumberStatusInput == NUMBERVALID
              && m_iNumberStatusPor == NUMBERZERO){
            if (m_oTicket.getLinesCount() > 0) {
                m_oTicket.setTipCash(getInputValue());
                printPartialTotals();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
            stateToZero();
      }
    }
  }


  private boolean closeTicket(TicketInfo ticket, Object ticketext) {
    if (listener != null) {
      listener.stop();
    }
    boolean resultok = false;

    if (m_App.getAppUserView().getUser().hasPermission("sales.Total")) {

      warrantyCheck(ticket);

      try {

        taxeslogic.calculateTaxes(ticket);
        if (ticket.getTotal() >= 0.0) {
          ticket.resetPayments();
        }

        if (executeEvent(ticket, ticketext, "ticket.total") == null) {
            if (listener != null) {
              listener.stop();
            }

            JPaymentSelect paymentdialog = ticket.getTicketType() == TicketInfo.RECEIPT_NORMAL
                ? paymentdialogreceipt
                : paymentdialogrefund;
            paymentdialog.setPrintSelected("true".equals(m_jbtnconfig.getProperty("printselected", "true")));
            paymentdialog.setTransactionID(ticket.getTransactionID());

            SharedTicketInfo item = null;
            double total = 0;

            try{
                item = dlReceipts.getSharedTicketById((String) m_oTicketExt);
            } catch (BasicException eData) {}

            if (item.getBalance() == 0) {
                total = ticket.getTotal();
            } else {
                total = item.getBalance();
            }

            int splitValue = paymentdialog.showDialog(total, ticket.getCustomer(), 1, 1);

            if (splitValue > 0) {

                int current = 2;

                if (splitValue == 1) {
                    ticket.setPayments(paymentdialog.getSelectedPayments());

                    ticket.setUser(m_App.getAppUserView().getUser().getUserInfo());
                    ticket.setActiveCash(m_App.getActiveCashIndex());
                    ticket.setDate(new Date());

                    if (executeEvent(ticket, ticketext, "ticket.save") == null) {

                        try {
                            dlSales.saveTicket(ticket, m_App.getInventoryLocation());
                            m_config.setProperty("lastticket.number", Integer.toString(ticket.getTicketId()));
                            m_config.setProperty("lastticket.type", Integer.toString(ticket.getTicketType()));
                            m_config.save();

                        } catch (BasicException eData) {
                            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"), eData);
                            msg.show(this);
                        } catch (IOException ex) {
                            log.error(ex.getMessage());
                        }

                        printTicket("Printer.TicketReceipt", ticket, ticketext, "0");
//                        Notify(AppLocal.getIntString("notify.printing"));
                    }
                } else {

                    for (current = 1; current <= splitValue; current ++) {

                        try{
                            item = dlReceipts.getSharedTicketById((String) m_oTicketExt);
                        } catch (BasicException eData) {}

                        if (paymentdialog.showDialog(total / splitValue, ticket.getCustomer(), current, splitValue) > 0) {

                            ticket.setPayments(paymentdialog.getSelectedPayments());

                            ticket.setUser(m_App.getAppUserView().getUser().getUserInfo());
                            ticket.setActiveCash(m_App.getActiveCashIndex());
                            ticket.setDate(new Date());

                            if (executeEvent(ticket, ticketext, "ticket.save") == null) {

                                try {
                                    if (item.getBalance() == 0) {
                                        dlReceipts.updateSharedTicketBalance(m_oTicketExt.toString(), ticket.getTotal() - total / splitValue);
                                    } else {
                                        dlReceipts.updateSharedTicketBalance(m_oTicketExt.toString(), item.getBalance() - total / splitValue);
                                    }
                                    dlSales.saveTicket(ticket, m_App.getInventoryLocation());
                                    m_config.setProperty("lastticket.number", Integer.toString(ticket.getTicketId()));
                                    m_config.setProperty("lastticket.type", Integer.toString(ticket.getTicketType()));
                                    m_config.save();

                                } catch (BasicException eData) {
                                    MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"), eData);
                                    msg.show(this);
                                } catch (IOException ex) {
                                    log.error(ex.getMessage());
                                }

                                printTicket("Printer.TicketReceipt", ticket, ticketext, "0");
//                                Notify(AppLocal.getIntString("notify.printing"));
                            }
                        } else {
                            break;
                        }
                    }
                }

                if (current > splitValue) {
                    resultok = true;

                    if ("restaurant".equals(m_App.getProperties()
                        .getProperty("machine.ticketsbag")) && !ticket.getOldTicket()) {
                      /* Deliberately Explicit to allow for reassignments - future
                       * even though we could do a single SQL statment sweep for reset
                       */
                        restDB.clearCustomerNameInTable(ticketext.toString());
                        restDB.clearWaiterNameInTable(ticketext.toString());
                        restDB.clearTicketIdInTable(ticketext.toString());
                    }
                }
            } else {
                resultok = false;
            }
        }
    } catch (TaxesException e) {
      MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
              AppLocal.getIntString("message.cannotcalculatetaxes"));
      msg.show(this);
      resultok = false;
    }

      m_oTicket.resetTaxes();
      m_oTicket.resetPayments();

      jCheckStock.setText("");

    }

    return resultok;
  }

  private boolean warrantyCheck(TicketInfo ticket) {
    warrantyPrint = false;
    int lines = 0;
    while (lines < ticket.getLinesCount()) {
      if (!warrantyPrint) {
        warrantyPrint = ticket.getLine(lines).isProductWarranty();
        return (true);
      }
      lines++;
    }
    return false;
  }

  /**
   * @param pTicket
   * @return
   */
    public String getPickupString(TicketInfo pTicket) {
        if (pTicket == null) {
            return ("0");
        }
        String tmpPickupId = Integer.toString(pTicket.getPickupId());
        String pickupSize = (m_App.getProperties().getProperty("till.pickupsize"));
        if (pickupSize != null && (Integer.parseInt(pickupSize) >= tmpPickupId.length())) {
            while (tmpPickupId.length() < (Integer.parseInt(pickupSize))) {
                tmpPickupId = "0" + tmpPickupId;
            }
        }
        return (tmpPickupId);
    }


  private void printTicket(String sresourcename, TicketInfo ticket, Object ticketext, String isVoid) {

    String sresource = dlSystem.getResourceAsXML(sresourcename);
    Double closedtip = 0.0;
    Object[] closedPayment = null;

    if (sresource == null) {
      MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
      msg.show(JPanelTicket.this);
    } else {
      if (ticket.getPickupId() == 0) {
        try {
            ticket.setPickupId(dlSales.getNextPickupIndex());
        } catch (BasicException e) {
            ticket.setPickupId(0);
        }
      }
      try {
        ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);

        if (Boolean.parseBoolean(m_App.getProperties().getProperty("receipt.newlayout"))) {
          script.put("taxes", ticket.getTaxLines());
        } else {
          script.put("taxes", taxcollection);
        }

        java.util.List<PaymentInfo> temp = ticket.getPayments();

        double cashTip = 0.0;
        for(PaymentInfo item: temp) {
            cashTip += item.getTip();
        }

        ticket.setTipCash(cashTip);

        if(temp.size() > 0) {
            PaymentInfo payment = temp.get(0);
            script.put("payment", payment);
        }

        String tabname = "";

        if(ticketext != null) {
            if (ticketext.toString().contains("[")) {

                String[] temptext = ticketext.toString().split("\\[");
                String tempName = ticket.getName();

                tabname = temptext[0] + " - " + tempName + "[" + temptext[1];
            } else {
                String tempName = ticket.getName();
                tabname = ticketext.toString() + " - " + tempName;
            }
        }

        script.put("taxeslogic", taxeslogic);
        script.put("ticket", ticket);
        script.put("place", tabname);
        script.put("table", (String) ticketext);
        script.put("warranty", warrantyPrint);
        script.put("pickupid", getPickupString(ticket));
        script.put("void", isVoid);
        script.put("closedTip", closedtip);
        script.put("closedTotal", Formats.CURRENCY.formatValue(closedtip + ticket.getTotal()));


        try {
            if(isClosed) {
                String ticketid = dlReceipts.getClosedTicketID(closedTicket);
                closedPayment = dlReceipts.getClosedPayment(closedTicket);

                if (ticketid != "")
                    closedtip = dlReceipts.getClosedTip(ticketid);
            }
        } catch (BasicException e) {}
        String prompt = "", paymentType = "";

        if(isClosed) {
            paymentType = closedPayment[0].toString().toUpperCase();
            script.put("cardType", paymentType);

            if(!"CASH".equals(paymentType)) {
                String paymentNumber = replaceWith(closedPayment[1].toString(), closedPayment[1].toString().length() - 4);
                script.put("cardNumber", paymentNumber);
            }
        }

        refreshTicket();

        m_TTP.printTicket(script.eval(sresource).toString(), ticket);

      } catch (ScriptException | TicketPrinterException e) {
        MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
        msg.show(JPanelTicket.this);
      }
    }
  }

  public void printTicket(String resource) {
// this method is intended to be called only from JPanelButtons.

    if (resource == null) {
      MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"));
      msg.show(this);
    } else {
// JG 5 Jul 17 calculate taxes disabled as causing incorrect tax recalc
//            taxeslogic.calculateTaxes(m_oTicket);
      printTicket(resource, m_oTicket, m_oTicketExt, "0");
    }

    Notify(AppLocal.getIntString("notify.printed"));
    j_btnRemotePrt.setEnabled(false);
  }

  public void customerAdd(String resource) {
    Notify(AppLocal.getIntString("notify.customeradd"));
  }

  public void customerRemove(String resource) {
    Notify(AppLocal.getIntString("notify.customerremove"));
  }

  public void customerChange(String resource) {
    Notify(AppLocal.getIntString("notify.customerchange"));
  }

  public void Notify(String msg) {
    final WebNotification notification = new WebNotification();
    notification.setIcon(NotificationIcon.information);
    notification.setDisplayTime(4000);

    final WebClock clock = new WebClock();
    clock.setClockType(ClockType.timer);
    clock.setTimeLeft(5000);
    notification.setContent(msg);

    NotificationManager.showNotification(notification);
    clock.start();
  }

  private void printReport(String resourcefile, TicketInfo ticket, Object ticketext) {

    try {

      JasperReport jr;

      InputStream in = getClass().getResourceAsStream(resourcefile + ".ser");
      if (in == null) {
        // read and compile the report
        JasperDesign jd = JRXmlLoader.load(getClass().getResourceAsStream(resourcefile + ".jrxml"));
        jr = JasperCompileManager.compileReport(jd);
      } else {
        try (ObjectInputStream oin = new ObjectInputStream(in)) {
          jr = (JasperReport) oin.readObject();
        }
      }


      Map reportparams = new HashMap();

      try {
        reportparams.put("REPORT_RESOURCE_BUNDLE", ResourceBundle.getBundle(resourcefile + ".properties"));
      } catch (MissingResourceException e) {
      }
      reportparams.put("TAXESLOGIC", taxeslogic);

      Map reportfields = new HashMap();
      reportfields.put("TICKET", ticket);
      reportfields.put("PLACE", ticketext);

      JasperPrint jp = JasperFillManager.fillReport(jr, reportparams, new JRMapArrayDataSource(new Object[]{reportfields}));

      PrintService service = ReportUtils.getPrintService(m_App.getProperties().getProperty("machine.printername"));

      JRPrinterAWT300.printPages(jp, 0, jp.getPages().size() - 1, service);

    } catch (JRException | IOException | ClassNotFoundException e) {
      MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadreport"), e);
      msg.show(this);
    }
  }

  private void visorTicketLine(TicketLineInfo oLine) {
    if (oLine == null) {
      m_App.getDeviceTicket().getDeviceDisplay().clearVisor();
    } else {
      try {
        ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
        script.put("ticketline", oLine);
        m_TTP.printTicket(script.eval(dlSystem.getResourceAsXML("Printer.TicketLine")).toString());

      } catch (ScriptException | TicketPrinterException e) {
        MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintline"), e);
        msg.show(JPanelTicket.this);
      }
    }
  }

  private Object evalScript(ScriptObject scr, String resource, ScriptArg... args) {

    // resource here is guaranteed to be not null
    try {
      scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
      return scr.evalScript(dlSystem.getResourceAsXML(resource), args);
    } catch (ScriptException e) {
      MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"), e);
      msg.show(this);
      return msg;
    }
  }

  /**
   * @param resource
   * @param args
   */
  public void evalScriptAndRefresh(String resource, ScriptArg... args) {

    if (resource == null) {
      MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"));
      msg.show(this);
    } else {
      ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
      scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
      evalScript(scr, resource, args);
      refreshTicket();

      setSelectedIndex(scr.getSelectedIndex());
    }
  }

  /**
   *
   */
  private Object executeEventAndRefresh(String eventkey, ScriptArg... args) {

    String resource = m_jbtnconfig.getEvent(eventkey);

    if (resource == null) {
      return null;
    } else {
      ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
      scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
      Object result = evalScript(scr, resource, args);
      refreshTicket();
      setSelectedIndex(scr.getSelectedIndex());
      return result;
    }
  }


  private Object executeEvent(TicketInfo ticket, Object ticketext, String eventkey, ScriptArg... args) {

    String resource = m_jbtnconfig.getEvent(eventkey);
    if (resource == null) {
      return null;
    } else {
      ScriptObject scr = new ScriptObject(ticket, ticketext);
      return evalScript(scr, resource, args);
    }
  }

  /**
   * @param sresourcename
   * @return
   */
  public String getResourceAsXML(String sresourcename) {
    return dlSystem.getResourceAsXML(sresourcename);
  }

  /**
   * @param sresourcename
   * @return
   */
  public BufferedImage getResourceAsImage(String sresourcename) {
    return dlSystem.getResourceAsImage(sresourcename);
  }

  private void setSelectedIndex(int i) {

    if (i >= 0 && i < m_oTicket.getLinesCount()) {
      m_ticketlines.setSelectedIndex(i);
    } else if (m_oTicket.getLinesCount() > 0) {
      m_ticketlines.setSelectedIndex(m_oTicket.getLinesCount() - 1);
    }
  }

  /**
   *
   */
  public static class ScriptArg {
    private final String key;
    private final Object value;

    /**
     * @param key
     * @param value
     */
    public ScriptArg(String key, Object value) {
      this.key = key;
      this.value = value;
    }

    /**
     * @return
     */
    public String getKey() {
      return key;
    }

    /**
     * @return
     */
    public Object getValue() {
      return value;
    }
  }

  private String setTempjPrice(String jPrice) {
    jPrice = jPrice.replace(".", "");
// remove all leading zeros from the string
    long tempL = Long.parseLong(jPrice);
    jPrice = Long.toString(tempL);

    while (jPrice.length() < 3) {
      jPrice = "0" + jPrice;
    }
    return (jPrice.length() <= 2) ? jPrice : (new StringBuffer(jPrice).insert(jPrice.length() - 2, ".").toString());
  }

  public void checkStock() {
    int i = m_ticketlines.getSelectedIndex();

    if (i < 0) {
    } else {
      try {
        TicketLineInfo line = m_oTicket.getLine(i);
        String pId = line.getProductID();

        String lName = (m_App.getProperties().getProperty("machine.department"));
        lName = "'" + lName + "'";
        ProductStock checkProduct;
        String location = m_App.getInventoryLocation();
        checkProduct = dlSales.getProductStockState(pId, location);

        if (checkProduct != null) {

          if (checkProduct.getUnits() <= 0) {
            jCheckStock.setForeground(Color.magenta);
          } else {
            jCheckStock.setForeground(Color.darkGray);
          }

          double dUnits = checkProduct.getUnits();
          int iUnits;
          iUnits = (int) dUnits;
          jCheckStock.setText(Integer.toString(iUnits));
        } else {
          jCheckStock.setText(null);
        }
      } catch (BasicException ex) {
        log.error(ex.getMessage());
      }
    }
  }

  public void checkCustomer() {
    if (m_oTicket.getCustomer().isVIP() == true) {

      String content;
      String vip;
      String discount;

      if (m_oTicket.getCustomer().isVIP() == true) {
        vip = AppLocal.getIntString("message.vipyes");
      } else {
        vip = AppLocal.getIntString("message.vipno");
      }
      if (m_oTicket.getCustomer().getDiscount() > 0) {
        discount = AppLocal.getIntString("message.discyes") + m_oTicket.getCustomer().getDiscount() + "%";
      } else {
        discount = AppLocal.getIntString("message.discno");
      }

      content = "<html>" +
              "<b>" + AppLocal.getIntString("label.vip") + " : " + "</b>" + vip + "<br>" +
              "<b>" + AppLocal.getIntString("label.discount") + " : " + "</b>" + discount + "<br>";


      JFrame frame = new JFrame();
      JOptionPane.showMessageDialog(frame,
              content,
              "Info",
              JOptionPane.WARNING_MESSAGE);
    }
  }

  /**
   *
   */
  public class ScriptObject {

    private final TicketInfo ticket;
    private final Object ticketext;

    private int selectedindex;

    private ScriptObject(TicketInfo ticket, Object ticketext) {
      this.ticket = ticket;
      this.ticketext = ticketext;
    }

    /**
     * @return
     */
    public double getInputValue() {
      if (m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
        return JPanelTicket.this.getInputValue();
      } else {
        return 0.0;
      }
    }

    /**
     * @return
     */
    public int getSelectedIndex() {
      return selectedindex;
    }

    /**
     * @param i
     */
    public void setSelectedIndex(int i) {
      selectedindex = i;
    }

    /**
     * @param resourcefile
     */
    public void printReport(String resourcefile) {
      JPanelTicket.this.printReport(resourcefile, ticket, ticketext);
    }

    /**
     * @param sresourcename
     */
    public void printTicket(String sresourcename) {
      JPanelTicket.this.printTicket(sresourcename, ticket, ticketext, "0");
      j_btnRemotePrt.setEnabled(false);
    }

    /**
     * @param code
     * @param args
     * @return Script object and valuef
     * @throws ScriptException
     */
    public Object evalScript(String code, ScriptArg... args) throws ScriptException {

      ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);

      String sDBUser = m_App.getProperties().getProperty("db.user");
      String sDBPassword = m_App.getProperties().getProperty("db.password");

      if (sDBUser != null && sDBPassword != null && sDBPassword.startsWith("crypt:")) {
        AltEncrypter cypher = new AltEncrypter("cypherkey" + sDBUser);
        sDBPassword = cypher.decrypt(sDBPassword.substring(6));
      }
      script.put("hostname", m_App.getProperties().getProperty("machine.hostname"));
      script.put("dbURL", m_App.getProperties().getProperty("db.URL") + m_App.getProperties().getProperty("db.schema"));
      script.put("dbUser", sDBUser);
      script.put("dbPassword", sDBPassword);

      script.put("ticket", ticket);
      script.put("place", ticketext);
      script.put("taxes", taxcollection);
      script.put("taxeslogic", taxeslogic);
      script.put("user", m_App.getAppUserView().getUser());
      script.put("sales", this);
      script.put("taxesinc", m_jaddtax.isSelected());
      script.put("warranty", warrantyPrint);
      script.put("pickupid", getPickupString(ticket));

      for (ScriptArg arg : args) {
        script.put(arg.getKey(), arg.getValue());
      }

      return script.eval(code);
    }
  }


  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckStock = new javax.swing.JButton();
        m_jPanContainer = new javax.swing.JPanel();
        m_jOptions = new javax.swing.JPanel();
        m_jPanelScripts = new javax.swing.JPanel();
        m_jButtonsExt = new javax.swing.JPanel();
        m_jPanTicket = new javax.swing.JPanel();
        m_jPanelBag = new javax.swing.JPanel();
        m_jPanelBagTop = new javax.swing.JPanel();
        m_jButtons = new javax.swing.JPanel();
        btnReprint1 = new javax.swing.JButton();
        j_btnRemotePrt = new javax.swing.JButton();
        jBtnCustomer = new javax.swing.JButton();
        jBtnNewTicket = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        m_jPanTotals = new javax.swing.JPanel();
        m_jLblTotalEuros3 = new javax.swing.JLabel();
        m_jSubtotalEuros = new javax.swing.JLabel();
        m_jLblTotalEuros2 = new javax.swing.JLabel();
        m_jTaxesEuros = new javax.swing.JLabel();
        m_jPanTotals1 = new javax.swing.JPanel();
        m_jLblTip = new javax.swing.JLabel();
        m_jTip = new javax.swing.JLabel();
        m_jLblTotalEuros1 = new javax.swing.JLabel();
        m_jTotalEuros = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        m_jQtyPlus = new javax.swing.JButton();
        m_jQtyMinus = new javax.swing.JButton();
        m_jDelete = new javax.swing.JButton();
        m_jEditLine = new javax.swing.JButton();
        jEditAttributes = new javax.swing.JButton();
        jOption = new javax.swing.JButton();
        m_jPanelCentral = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        m_jTicketId = new javax.swing.JLabel();
        m_jCombo = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        m_jDollarBtn = new javax.swing.JButton();
        btnTransfer = new javax.swing.JButton();
        btnMerge = new javax.swing.JButton();
        btnSplit = new javax.swing.JButton();
        m_jPay = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        j_mVoid = new javax.swing.JButton();
        j_mCancel = new javax.swing.JButton();
        j_mPrint = new javax.swing.JButton();
        j_mLogout = new javax.swing.JButton();
        j_mDiscount = new javax.swing.JButton();
        j_mComp = new javax.swing.JButton();
        j_mBack = new javax.swing.JButton();
        m_jContEntries = new javax.swing.JPanel();
        m_jPanEntries = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        m_jaddtax = new com.alee.extended.button.WebSwitch();
        m_jPrice = new javax.swing.JLabel();
        m_jPor = new javax.swing.JLabel();
        m_jEnter = new javax.swing.JButton();
        m_jTax = new javax.swing.JComboBox();
        m_jKeyFactory = new javax.swing.JTextField();
        m_jPanEntriesE = new javax.swing.JPanel();
        catcontainer = new javax.swing.JPanel();

        jCheckStock.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jCheckStock.setForeground(new java.awt.Color(76, 197, 237));
        jCheckStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/info.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jCheckStock.setToolTipText(bundle.getString("tooltip.salecheckstock")); // NOI18N
        jCheckStock.setFocusPainted(false);
        jCheckStock.setFocusable(false);
        jCheckStock.setHideActionText(true);
        jCheckStock.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jCheckStock.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jCheckStock.setMargin(new java.awt.Insets(8, 4, 8, 4));
        jCheckStock.setMaximumSize(new java.awt.Dimension(42, 36));
        jCheckStock.setMinimumSize(new java.awt.Dimension(42, 36));
        jCheckStock.setPreferredSize(new java.awt.Dimension(40, 40));
        jCheckStock.setRequestFocusEnabled(false);
        jCheckStock.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jCheckStock.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckStockMouseClicked(evt);
            }
        });
        jCheckStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckStockActionPerformed(evt);
            }
        });

        setBackground(new java.awt.Color(255, 204, 153));
        setOpaque(false);
        setLayout(new java.awt.CardLayout());

        m_jPanContainer.setBackground(new java.awt.Color(24, 26, 30));
        m_jPanContainer.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_jPanContainerFocusLost(evt);
            }
        });
        m_jPanContainer.setLayout(new java.awt.BorderLayout());

        m_jOptions.setLayout(new java.awt.BorderLayout());

        m_jPanelScripts.setPreferredSize(new java.awt.Dimension(200, 60));
        m_jPanelScripts.setLayout(new java.awt.BorderLayout());

        m_jButtonsExt.setBackground(new java.awt.Color(47, 49, 53));
        m_jButtonsExt.setPreferredSize(new java.awt.Dimension(20, 60));
        m_jPanelScripts.add(m_jButtonsExt, java.awt.BorderLayout.CENTER);

        m_jOptions.add(m_jPanelScripts, java.awt.BorderLayout.CENTER);
        m_jPanelScripts.getAccessibleContext().setAccessibleDescription("");

        m_jPanContainer.add(m_jOptions, java.awt.BorderLayout.NORTH);

        m_jPanTicket.setBackground(new java.awt.Color(102, 102, 102));
        m_jPanTicket.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));
        m_jPanTicket.setPreferredSize(new java.awt.Dimension(350, 730));
        m_jPanTicket.setLayout(new java.awt.BorderLayout());

        m_jPanelBag.setBackground(new java.awt.Color(71, 72, 75));
        m_jPanelBag.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(95, 95, 95)));
        m_jPanelBag.setAutoscrolls(true);
        m_jPanelBag.setMaximumSize(new java.awt.Dimension(10, 10));
        m_jPanelBag.setPreferredSize(new java.awt.Dimension(600, 52));
        m_jPanelBag.setLayout(new java.awt.BorderLayout(0, 5));

        m_jPanelBagTop.setBackground(new java.awt.Color(71, 72, 75));
        m_jPanelBagTop.setPreferredSize(new java.awt.Dimension(200, 50));
        m_jPanelBagTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 5));
        m_jPanelBag.add(m_jPanelBagTop, java.awt.BorderLayout.LINE_END);

        m_jButtons.setBackground(new java.awt.Color(71, 72, 75));
        m_jButtons.setPreferredSize(new java.awt.Dimension(270, 50));
        m_jButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnReprint1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnReprint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/reprint24.png"))); // NOI18N
        btnReprint1.setToolTipText(bundle.getString("tooltip.reprintLastTicket")); // NOI18N
        btnReprint1.setFocusPainted(false);
        btnReprint1.setFocusable(false);
        btnReprint1.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnReprint1.setMaximumSize(new java.awt.Dimension(50, 40));
        btnReprint1.setMinimumSize(new java.awt.Dimension(50, 40));
        btnReprint1.setPreferredSize(new java.awt.Dimension(60, 40));
        btnReprint1.setRequestFocusEnabled(false);
        btnReprint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReprint1ActionPerformed(evt);
            }
        });
        m_jButtons.add(btnReprint1);

        j_btnRemotePrt.setBackground(new java.awt.Color(2, 2, 4));
        j_btnRemotePrt.setText(bundle.getString("button.sendorder")); // NOI18N
        j_btnRemotePrt.setToolTipText(bundle.getString("tooltip.printtoremote")); // NOI18N
        j_btnRemotePrt.setMargin(new java.awt.Insets(0, 4, 0, 4));
        j_btnRemotePrt.setMaximumSize(new java.awt.Dimension(50, 40));
        j_btnRemotePrt.setMinimumSize(new java.awt.Dimension(50, 40));
        j_btnRemotePrt.setPreferredSize(new java.awt.Dimension(60, 40));
        j_btnRemotePrt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_btnRemotePrtActionPerformed(evt);
            }
        });
        m_jButtons.add(j_btnRemotePrt);

        jBtnCustomer.setBackground(new java.awt.Color(2, 2, 4));
        jBtnCustomer.setText("Guest");
        jBtnCustomer.setToolTipText(bundle.getString("tooltip.salescustomer")); // NOI18N
        jBtnCustomer.setPreferredSize(new java.awt.Dimension(70, 40));
        jBtnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCustomerActionPerformed(evt);
            }
        });
        m_jButtons.add(jBtnCustomer);

        jBtnNewTicket.setBackground(new java.awt.Color(2, 2, 4));
        jBtnNewTicket.setText("New Ticket");
        jBtnNewTicket.setToolTipText(bundle.getString("tooltip.salescustomer")); // NOI18N
        jBtnNewTicket.setPreferredSize(new java.awt.Dimension(70, 40));
        jBtnNewTicket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNewTicketActionPerformed(evt);
            }
        });
        m_jButtons.add(jBtnNewTicket);

        m_jPanelBag.add(m_jButtons, java.awt.BorderLayout.LINE_START);

        m_jPanTicket.add(m_jPanelBag, java.awt.BorderLayout.PAGE_START);

        jPanel11.setLayout(new java.awt.BorderLayout());

        jPanel5.setBackground(new java.awt.Color(47, 49, 53));
        jPanel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(75, 160));
        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel4.setBackground(new java.awt.Color(47, 49, 53));
        jPanel4.setLayout(new java.awt.BorderLayout(10, 10));
        jPanel4.add(filler2, java.awt.BorderLayout.LINE_START);

        m_jPanTotals.setBackground(new java.awt.Color(47, 49, 53));
        m_jPanTotals.setMinimumSize(new java.awt.Dimension(100, 50));
        m_jPanTotals.setPreferredSize(new java.awt.Dimension(200, 60));
        m_jPanTotals.setLayout(new java.awt.GridLayout(2, 2, 4, 5));

        m_jLblTotalEuros3.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jLblTotalEuros3.setForeground(new java.awt.Color(255, 255, 255));
        m_jLblTotalEuros3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jLblTotalEuros3.setLabelFor(m_jSubtotalEuros);
        m_jLblTotalEuros3.setText(AppLocal.getIntString("label.subtotalcash")); // NOI18N
        m_jPanTotals.add(m_jLblTotalEuros3);

        m_jSubtotalEuros.setBackground(m_jEditLine.getBackground());
        m_jSubtotalEuros.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        m_jSubtotalEuros.setForeground(new java.awt.Color(255, 255, 255));
        m_jSubtotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jSubtotalEuros.setLabelFor(m_jSubtotalEuros);
        m_jSubtotalEuros.setToolTipText(bundle.getString("tooltip.salesubtotal")); // NOI18N
        m_jSubtotalEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jSubtotalEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jSubtotalEuros.setPreferredSize(new java.awt.Dimension(80, 25));
        m_jSubtotalEuros.setRequestFocusEnabled(false);
        m_jPanTotals.add(m_jSubtotalEuros);

        m_jLblTotalEuros2.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jLblTotalEuros2.setForeground(new java.awt.Color(255, 255, 255));
        m_jLblTotalEuros2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jLblTotalEuros2.setLabelFor(m_jSubtotalEuros);
        m_jLblTotalEuros2.setText(AppLocal.getIntString("label.taxcash")); // NOI18N
        m_jPanTotals.add(m_jLblTotalEuros2);

        m_jTaxesEuros.setBackground(m_jEditLine.getBackground());
        m_jTaxesEuros.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        m_jTaxesEuros.setForeground(new java.awt.Color(255, 255, 255));
        m_jTaxesEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jTaxesEuros.setLabelFor(m_jTaxesEuros);
        m_jTaxesEuros.setToolTipText(bundle.getString("tooltip.saletax")); // NOI18N
        m_jTaxesEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jTaxesEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jTaxesEuros.setPreferredSize(new java.awt.Dimension(80, 25));
        m_jTaxesEuros.setRequestFocusEnabled(false);
        m_jPanTotals.add(m_jTaxesEuros);

        jPanel4.add(m_jPanTotals, java.awt.BorderLayout.CENTER);

        m_jPanTotals1.setBackground(new java.awt.Color(47, 49, 53));
        m_jPanTotals1.setPreferredSize(new java.awt.Dimension(170, 60));
        m_jPanTotals1.setLayout(new java.awt.GridLayout(2, 2, 5, 5));

        m_jLblTip.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jLblTip.setForeground(new java.awt.Color(255, 255, 255));
        m_jLblTip.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jLblTip.setLabelFor(m_jTip);
        m_jLblTip.setText("Discount");
        m_jPanTotals1.add(m_jLblTip);

        m_jTip.setBackground(m_jEditLine.getBackground());
        m_jTip.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        m_jTip.setForeground(new java.awt.Color(255, 255, 255));
        m_jTip.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jTip.setLabelFor(m_jTotalEuros);
        m_jTip.setToolTipText(bundle.getString("tooltip.saletotal")); // NOI18N
        m_jTip.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jTip.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jTip.setPreferredSize(new java.awt.Dimension(100, 25));
        m_jTip.setRequestFocusEnabled(false);
        m_jPanTotals1.add(m_jTip);

        m_jLblTotalEuros1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jLblTotalEuros1.setForeground(new java.awt.Color(255, 255, 255));
        m_jLblTotalEuros1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jLblTotalEuros1.setLabelFor(m_jTotalEuros);
        m_jLblTotalEuros1.setText(AppLocal.getIntString("label.totalcash")); // NOI18N
        m_jPanTotals1.add(m_jLblTotalEuros1);

        m_jTotalEuros.setBackground(m_jEditLine.getBackground());
        m_jTotalEuros.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        m_jTotalEuros.setForeground(new java.awt.Color(255, 255, 255));
        m_jTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jTotalEuros.setLabelFor(m_jTotalEuros);
        m_jTotalEuros.setToolTipText(bundle.getString("tooltip.saletotal")); // NOI18N
        m_jTotalEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jTotalEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jTotalEuros.setPreferredSize(new java.awt.Dimension(100, 25));
        m_jTotalEuros.setRequestFocusEnabled(false);
        m_jPanTotals1.add(m_jTotalEuros);

        jPanel4.add(m_jPanTotals1, java.awt.BorderLayout.LINE_END);

        jPanel5.add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel7.setBackground(new java.awt.Color(47, 49, 53));
        jPanel7.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 2, 0, new java.awt.Color(95, 95, 95)));
        jPanel7.setMinimumSize(new java.awt.Dimension(240, 54));
        jPanel7.setPreferredSize(new java.awt.Dimension(338, 54));
        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        m_jQtyPlus.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyPlus.setText("+");
        m_jQtyPlus.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyPlus.setFocusPainted(false);
        m_jQtyPlus.setFocusable(false);
        m_jQtyPlus.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyPlus.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyPlus.setRequestFocusEnabled(false);
        m_jQtyPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyPlusActionPerformed(evt);
            }
        });
        jPanel7.add(m_jQtyPlus);

        m_jQtyMinus.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyMinus.setText("-");
        m_jQtyMinus.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyMinus.setFocusPainted(false);
        m_jQtyMinus.setFocusable(false);
        m_jQtyMinus.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyMinus.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyMinus.setRequestFocusEnabled(false);
        m_jQtyMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyMinusActionPerformed(evt);
            }
        });
        jPanel7.add(m_jQtyMinus);

        m_jDelete.setBackground(new java.awt.Color(2, 2, 4));
        m_jDelete.setText("Delete");
        m_jDelete.setToolTipText(bundle.getString("tooltip.saleremoveline")); // NOI18N
        m_jDelete.setFocusPainted(false);
        m_jDelete.setFocusable(false);
        m_jDelete.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jDelete.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jDelete.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jDelete.setPreferredSize(new java.awt.Dimension(60, 40));
        m_jDelete.setRequestFocusEnabled(false);
        m_jDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDeleteActionPerformed(evt);
            }
        });
        jPanel7.add(m_jDelete);

        m_jEditLine.setBackground(new java.awt.Color(2, 2, 4));
        m_jEditLine.setText("Edit");
        m_jEditLine.setToolTipText(bundle.getString("tooltip.saleeditline")); // NOI18N
        m_jEditLine.setFocusPainted(false);
        m_jEditLine.setFocusable(false);
        m_jEditLine.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jEditLine.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jEditLine.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jEditLine.setPreferredSize(new java.awt.Dimension(50, 40));
        m_jEditLine.setRequestFocusEnabled(false);
        m_jEditLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEditLineActionPerformed(evt);
            }
        });
        jPanel7.add(m_jEditLine);

        jEditAttributes.setBackground(new java.awt.Color(2, 2, 4));
        jEditAttributes.setText("Mod");
        jEditAttributes.setToolTipText(bundle.getString("tooltip.saleattributes")); // NOI18N
        jEditAttributes.setActionCommand("Options");
        jEditAttributes.setFocusPainted(false);
        jEditAttributes.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jEditAttributes.setMaximumSize(new java.awt.Dimension(42, 36));
        jEditAttributes.setMinimumSize(new java.awt.Dimension(42, 36));
        jEditAttributes.setPreferredSize(new java.awt.Dimension(60, 40));
        jEditAttributes.setRequestFocusEnabled(false);
        jEditAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEditAttributesActionPerformed(evt);
            }
        });
        jPanel7.add(jEditAttributes);

        jOption.setBackground(new java.awt.Color(2, 2, 4));
        jOption.setText("Options");
        jOption.setToolTipText(bundle.getString("tooltip.saleattributes")); // NOI18N
        jOption.setFocusPainted(false);
        jOption.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jOption.setMaximumSize(new java.awt.Dimension(42, 36));
        jOption.setMinimumSize(new java.awt.Dimension(42, 36));
        jOption.setPreferredSize(new java.awt.Dimension(60, 40));
        jOption.setRequestFocusEnabled(false);
        jOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOptionActionPerformed(evt);
            }
        });
        jPanel7.add(jOption);

        jPanel5.add(jPanel7, java.awt.BorderLayout.PAGE_START);

        jPanel11.add(jPanel5, java.awt.BorderLayout.PAGE_END);

        m_jPanelCentral.setBackground(new java.awt.Color(47, 49, 53));
        m_jPanelCentral.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPanelCentral.setName(""); // NOI18N
        m_jPanelCentral.setPreferredSize(new java.awt.Dimension(450, 450));
        m_jPanelCentral.setLayout(new java.awt.BorderLayout());

        jPanel3.setBackground(new java.awt.Color(47, 49, 53));
        jPanel3.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 2, 0, new java.awt.Color(95, 95, 95)));
        jPanel3.setPreferredSize(new java.awt.Dimension(338, 35));
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 4));

        m_jTicketId.setBackground(new java.awt.Color(47, 49, 53));
        m_jTicketId.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        m_jTicketId.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jTicketId.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        m_jTicketId.setOpaque(true);
        m_jTicketId.setPreferredSize(new java.awt.Dimension(295, 20));
        m_jTicketId.setRequestFocusEnabled(false);
        m_jTicketId.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        m_jTicketId.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                m_jTicketIdMouseClicked(evt);
            }
        });
        jPanel3.add(m_jTicketId);

        m_jCombo.setPreferredSize(new java.awt.Dimension(30, 23));
        m_jCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jComboActionPerformed(evt);
            }
        });
        jPanel3.add(m_jCombo);

        m_jPanelCentral.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        jPanel11.add(m_jPanelCentral, java.awt.BorderLayout.CENTER);

        m_jPanTicket.add(jPanel11, java.awt.BorderLayout.CENTER);

        jPanel6.setBackground(new java.awt.Color(47, 49, 53));
        jPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 2, 0, new java.awt.Color(95, 95, 95)));
        jPanel6.setPreferredSize(new java.awt.Dimension(120, 104));
        jPanel6.setLayout(new java.awt.BorderLayout());

        jPanel8.setBackground(new java.awt.Color(47, 49, 53));
        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        m_jDollarBtn.setBackground(new java.awt.Color(2, 2, 4));
        m_jDollarBtn.setText("$");
        m_jDollarBtn.setToolTipText(bundle.getString("tooltip.saleremoveline")); // NOI18N
        m_jDollarBtn.setFocusPainted(false);
        m_jDollarBtn.setFocusable(false);
        m_jDollarBtn.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jDollarBtn.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jDollarBtn.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jDollarBtn.setPreferredSize(new java.awt.Dimension(50, 40));
        m_jDollarBtn.setRequestFocusEnabled(false);
        m_jDollarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDollarBtnActionPerformed(evt);
            }
        });
        jPanel8.add(m_jDollarBtn);

        btnTransfer.setBackground(new java.awt.Color(2, 2, 4));
        btnTransfer.setText("Transfer");
        btnTransfer.setToolTipText(bundle.getString("tooltip.salesplit")); // NOI18N
        btnTransfer.setFocusPainted(false);
        btnTransfer.setFocusable(false);
        btnTransfer.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnTransfer.setMaximumSize(new java.awt.Dimension(50, 40));
        btnTransfer.setMinimumSize(new java.awt.Dimension(50, 40));
        btnTransfer.setPreferredSize(new java.awt.Dimension(60, 40));
        btnTransfer.setRequestFocusEnabled(false);
        btnTransfer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransferActionPerformed(evt);
            }
        });
        jPanel8.add(btnTransfer);

        btnMerge.setBackground(new java.awt.Color(2, 2, 4));
        btnMerge.setText("Merge");
        btnMerge.setToolTipText(bundle.getString("tooltip.salesplit")); // NOI18N
        btnMerge.setFocusPainted(false);
        btnMerge.setFocusable(false);
        btnMerge.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnMerge.setMaximumSize(new java.awt.Dimension(50, 40));
        btnMerge.setMinimumSize(new java.awt.Dimension(50, 40));
        btnMerge.setPreferredSize(new java.awt.Dimension(60, 40));
        btnMerge.setRequestFocusEnabled(false);
        btnMerge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMergeActionPerformed(evt);
            }
        });
        jPanel8.add(btnMerge);

        btnSplit.setBackground(new java.awt.Color(2, 2, 4));
        btnSplit.setText("Split");
        btnSplit.setToolTipText(bundle.getString("tooltip.salesplit")); // NOI18N
        btnSplit.setEnabled(false);
        btnSplit.setFocusPainted(false);
        btnSplit.setFocusable(false);
        btnSplit.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnSplit.setMaximumSize(new java.awt.Dimension(50, 40));
        btnSplit.setMinimumSize(new java.awt.Dimension(50, 40));
        btnSplit.setPreferredSize(new java.awt.Dimension(60, 40));
        btnSplit.setRequestFocusEnabled(false);
        btnSplit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSplitActionPerformed(evt);
            }
        });
        jPanel8.add(btnSplit);

        m_jPay.setBackground(new java.awt.Color(2, 2, 4));
        m_jPay.setToolTipText(bundle.getString("tooltip.saleremoveline")); // NOI18N
        m_jPay.setFocusPainted(false);
        m_jPay.setFocusable(false);
        m_jPay.setLabel("Pay");
        m_jPay.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jPay.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jPay.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jPay.setPreferredSize(new java.awt.Dimension(50, 40));
        m_jPay.setRequestFocusEnabled(false);
        m_jPay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jPayActionPerformed(evt);
            }
        });
        jPanel8.add(m_jPay);

        jPanel6.add(jPanel8, java.awt.BorderLayout.SOUTH);

        jPanel2.setBackground(new java.awt.Color(47, 49, 53));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        j_mVoid.setText("Void");
        j_mVoid.setPreferredSize(new java.awt.Dimension(55, 40));
        j_mVoid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mVoidActionPerformed(evt);
            }
        });
        jPanel2.add(j_mVoid);

        j_mCancel.setText("Cancel");
        j_mCancel.setPreferredSize(new java.awt.Dimension(60, 40));
        j_mCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mCancelActionPerformed(evt);
            }
        });
        jPanel2.add(j_mCancel);

        j_mPrint.setText("Print");
        j_mPrint.setPreferredSize(new java.awt.Dimension(60, 40));
        j_mPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mPrintActionPerformed(evt);
            }
        });
        jPanel2.add(j_mPrint);

        j_mLogout.setText("Logout");
        j_mLogout.setPreferredSize(new java.awt.Dimension(60, 40));
        j_mLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mLogoutActionPerformed(evt);
            }
        });
        jPanel2.add(j_mLogout);

        j_mDiscount.setText("Discount");
        j_mDiscount.setPreferredSize(new java.awt.Dimension(65, 40));
        j_mDiscount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mDiscountActionPerformed(evt);
            }
        });
        jPanel2.add(j_mDiscount);

        j_mComp.setText("Comp");
        j_mComp.setPreferredSize(new java.awt.Dimension(55, 40));
        j_mComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mCompActionPerformed(evt);
            }
        });
        jPanel2.add(j_mComp);

        j_mBack.setText("Back");
        j_mBack.setPreferredSize(new java.awt.Dimension(55, 40));
        j_mBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mBackActionPerformed(evt);
            }
        });
        jPanel2.add(j_mBack);

        jPanel6.add(jPanel2, java.awt.BorderLayout.CENTER);

        m_jPanTicket.add(jPanel6, java.awt.BorderLayout.PAGE_END);

        m_jPanContainer.add(m_jPanTicket, java.awt.BorderLayout.LINE_END);

        m_jContEntries.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jContEntries.setMinimumSize(new java.awt.Dimension(300, 350));
        m_jContEntries.setLayout(new java.awt.BorderLayout());

        m_jPanEntries.setEnabled(false);
        m_jPanEntries.setFocusable(false);
        m_jPanEntries.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPanEntries.setMinimumSize(new java.awt.Dimension(310, 310));
        m_jPanEntries.setPreferredSize(new java.awt.Dimension(310, 350));
        m_jPanEntries.setLayout(new javax.swing.BoxLayout(m_jPanEntries, javax.swing.BoxLayout.Y_AXIS));

        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel9.setAlignmentY(0.0F);

        m_jaddtax.setBorder(null);
        m_jaddtax.setToolTipText(bundle.getString("tooltip.switchtax")); // NOI18N
        m_jaddtax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jaddtax.setPreferredSize(new java.awt.Dimension(60, 30));
        m_jaddtax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jaddtaxActionPerformed(evt);
            }
        });

        m_jPrice.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        m_jPrice.setForeground(new java.awt.Color(76, 197, 237));
        m_jPrice.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jPrice.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(76, 197, 237)), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jPrice.setOpaque(true);
        m_jPrice.setPreferredSize(new java.awt.Dimension(100, 20));
        m_jPrice.setRequestFocusEnabled(false);

        m_jPor.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jPor.setRequestFocusEnabled(false);

        m_jEnter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/barcode.png"))); // NOI18N
        m_jEnter.setToolTipText(bundle.getString("tooltip.salebarcode")); // NOI18N
        m_jEnter.setFocusPainted(false);
        m_jEnter.setFocusable(false);
        m_jEnter.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jEnter.setRequestFocusEnabled(false);
        m_jEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnterActionPerformed(evt);
            }
        });

        m_jTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTax.setToolTipText(bundle.getString("tooltip.salestaxswitch")); // NOI18N
        m_jTax.setFocusable(false);
        m_jTax.setPreferredSize(new java.awt.Dimension(28, 25));
        m_jTax.setRequestFocusEnabled(false);

        m_jKeyFactory.setEditable(false);
        m_jKeyFactory.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        m_jKeyFactory.setForeground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setAutoscrolls(false);
        m_jKeyFactory.setBorder(null);
        m_jKeyFactory.setCaretColor(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setMinimumSize(new java.awt.Dimension(0, 0));
        m_jKeyFactory.setPreferredSize(new java.awt.Dimension(1, 1));
        m_jKeyFactory.setRequestFocusEnabled(false);
        m_jKeyFactory.setVerifyInputWhenFocusTarget(false);
        m_jKeyFactory.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                m_jKeyFactoryKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(m_jaddtax, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_jTax, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13)
                        .addComponent(m_jKeyFactory, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                        .addGap(186, 186, 186))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(m_jPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(m_jPor)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(m_jEnter, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(m_jEnter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(m_jPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jPor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jKeyFactory, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jTax, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jaddtax, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        m_jPanEntries.add(jPanel9);

        m_jContEntries.add(m_jPanEntries, java.awt.BorderLayout.NORTH);

        m_jPanEntriesE.setLayout(new java.awt.BorderLayout());
        m_jContEntries.add(m_jPanEntriesE, java.awt.BorderLayout.LINE_END);

        m_jPanContainer.add(m_jContEntries, java.awt.BorderLayout.PAGE_END);

        catcontainer.setBackground(new java.awt.Color(24, 26, 30));
        catcontainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));
        catcontainer.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        catcontainer.setPreferredSize(new java.awt.Dimension(500, 450));
        catcontainer.setLayout(new java.awt.BorderLayout());
        m_jPanContainer.add(catcontainer, java.awt.BorderLayout.CENTER);

        add(m_jPanContainer, "ticket");
    }// </editor-fold>//GEN-END:initComponents

  private void m_jEditLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEditLineActionPerformed

    count = (int) m_oTicket.getArticlesCount();     // get existing line value

    int i = m_ticketlines.getSelectedIndex();

    if (i < 0) {
      Toolkit.getDefaultToolkit().beep(); // no line selected
    } else {
      try {
        TicketLineInfo currentline = m_oTicket.getLine(i);
        TicketLineInfo newline = JProductLineEdit.showMessage(this, m_App, m_oTicket.getLine(i));
        if (newline != null) {
          currentline.setMultiply(newline.getMultiply());
          paintTicketLine(i, currentline);
        }

      } catch (BasicException e) {
        new MessageInf(e).show(this);
      }
    }

  }//GEN-LAST:event_m_jEditLineActionPerformed

  private void m_jDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDeleteActionPerformed
    int i = m_ticketlines.getSelectedIndex();
    TicketLineInfo currentline = m_oTicket.getLine(i);

    if (!"".equals(currentline.getIsOrdered())) {
        JOptionPane.showMessageDialog(this,
            "The item has already been sent. You can not delete!", 
            "Warning", 
            JOptionPane.OK_OPTION);    
        return;  
    }

    if (m_App.getProperties().getProperty("override.check").equals("true")) {
      pinOK = false;
      if (changeCount(pinOK)) {
        if (i < 0) {
          Toolkit.getDefaultToolkit().beep();
        } else {
          removeTicketLine(i);
          jCheckStock.setText("");
        }
      }
    } else {
      if (i < 0) {
        Toolkit.getDefaultToolkit().beep();
      } else {
        removeTicketLine(i);
        jCheckStock.setText("");
      }
    }
  }//GEN-LAST:event_m_jDeleteActionPerformed
    public class UpdateMod implements ActionListener {
        public UpdateMod() {
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            int i = m_ticketlines.getSelectedIndex();
            TicketLineInfo line = m_oTicket.getLine(i);
            if(line.getProductAttSetId()!=null){
                m_jCatalog.processModifier(line.getProductAttSetId());
                line.setProductAttSetInstId(m_jCatalog.getAttInstanceId());
                line.setProductAttSetInstDesc(m_jCatalog.getAttInstanceDescription());
                java.util.List<String> m_modifier = m_jCatalog.getModifier();
                java.util.List<Modifier> modifiers = new ArrayList();
                for(String modifier:m_modifier){
                    String[] values = modifier.split("_");
                    Modifier item = new Modifier();
                    item.setId(values[0]);
                    item.setValue(values[1]);
                    item.setPrice(Double.valueOf(values[2]));
                    modifiers.add(item);
                }
                line.setModifiers(modifiers);
                paintTicketLine(i, line);
            }
        }
    }
  private void j_btnRemotePrtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_btnRemotePrtActionPerformed

    remoteOrderDisplay();

    saveCurrentTicket();
    try{
        dlReceipts.lockSharedTicket((String) m_oTicketExt, "");
    } catch (BasicException e) {
        new MessageInf(e).show(this);
    }

    WebSocketClient socketClient = ((JRootApp) m_App).getSocketClient();
    socketClient.sendMessage("New Order");

    setActiveTicket(null, null, false);
  }//GEN-LAST:event_j_btnRemotePrtActionPerformed

  private void btnReprint1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReprint1ActionPerformed
    if (m_config.getProperty("lastticket.number") != null) {
      try {
        TicketInfo ticket = dlSales.loadTicket(
                Integer.parseInt((m_config.getProperty("lastticket.type"))),
                Integer.parseInt((m_config.getProperty("lastticket.number"))));
        if (ticket == null) {
          JFrame frame = new JFrame();
          JOptionPane.showMessageDialog(frame,
                  AppLocal.getIntString("message.notexiststicket"),
                  AppLocal.getIntString("message.notexiststickettitle"),
                  JOptionPane.WARNING_MESSAGE);
        } else {
          m_ticket = ticket;
          m_ticketCopy = null;
          try {
            taxeslogic.calculateTaxes(m_ticket);
            TicketTaxInfo[] taxlist = m_ticket.getTaxLines();
          } catch (TaxesException ex) {
          }
          printTicket("Printer.ReprintTicket", m_ticket, null, "0");
          Notify("'Printed'");
        }
      } catch (BasicException e) {
        MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadticket"), e);
        msg.show(this);
      }
    }
  }//GEN-LAST:event_btnReprint1ActionPerformed

  private void btnSplitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSplitActionPerformed

    if (m_oTicket.getLinesCount() > 0) {
         ReceiptSplit splitdialog = ReceiptSplit.getDialog(this,
              dlSystem.getResourceAsXML("Ticket.Line"), dlSales, dlCustomers, taxeslogic);

        java.util.List<SharedTicketInfo> ticketList = null;
        try {
            dlReceipts.updateSharedTicket((String) m_oTicketExt, 
                m_oTicket,
                m_oTicket.getPickupId());

            int length = 0;
            ticketList = dlReceipts.getSharedTicketList();

            String m_oTicketExtString = (String) m_oTicketExt;
            String realId = "";
            if (m_oTicketExtString.contains("[")) {
                realId = m_oTicketExtString.split("\\[")[0];
            } else {
                realId = m_oTicketExtString;
            }

            for (int i=0; i<ticketList.size(); i++) {
                SharedTicketInfo temp = ticketList.get(i);
                if (temp.getId().equals(realId) || temp.getId().contains(realId + "[")) {
                    length ++;
                }
            }

            SimpleReceipt[] receipts = splitdialog.showDialog(m_oTicket, m_oTicketExtString);

            if (receipts != null)  {
                dlReceipts.deleteSharedTicket(m_oTicketExtString);
                TicketInfo ticket = receipts[0].getTicket();
                ticket.setUser(m_oTicket.getUser());
                ticket.setName(ticket.getName());
                try {
                    dlReceipts.insertSharedTicket(m_oTicketExtString, ticket, ticket.getPickupId());
                    dlReceipts.lockSharedTicket(m_oTicketExtString, "");
                } catch (BasicException e) {
                    new MessageInf(e).show(this);
                }
                
                for (int i = 1; i < receipts.length; i++) {
                    TicketInfo tempTicket = receipts[i].getTicket();
                    
                    if (tempTicket.getLinesCount() > 0) {
                        tempTicket.setCustomer(m_oTicket.getCustomer());
                        tempTicket.setUser(m_oTicket.getUser());
//                        tempTicket.setTName(ticket.getName(null));

                        try {
                            dlReceipts.insertSharedTicket(realId + "[" + length + "]" , tempTicket, tempTicket.getPickupId());
                            dlReceipts.lockSharedTicket(realId + "[" + length + "]", "");

                            length ++;
                        } catch (BasicException e) {
                            new MessageInf(e).show(this);
                        }
                    }
                }

                setActiveTicket(ticket, m_oTicketExtString, false);
            }
        } catch (BasicException ex) {
          log.error(ex.getMessage());
        }
    }

  }//GEN-LAST:event_btnSplitActionPerformed

  private void jCheckStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckStockActionPerformed

    if (listener != null) {
      listener.stop();
    }

    int i = m_ticketlines.getSelectedIndex();
    if (i < 0) {
      Toolkit.getDefaultToolkit().beep();
    } else {
      try {
        TicketLineInfo line = m_oTicket.getLine(i);
        String pId = line.getProductID();
        String location = m_App.getInventoryLocation();
        ProductStock checkProduct;
        checkProduct = dlSales.getProductStockState(pId, location);

        if (checkProduct != null) {

          if (checkProduct.getUnits() <= 0) {
            jCheckStock.setForeground(Color.magenta);
          } else {
            jCheckStock.setForeground(Color.darkGray);
          }

          String content;

          if (!location.equals(checkProduct.getLocation())) {
            content = AppLocal.getIntString("message.location.current");
            JFrame frame = new JFrame();
            JOptionPane.showMessageDialog(frame,
                    content,
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
          } else {
            double dUnits = checkProduct.getUnits();
            int iUnits;
            iUnits = (int) dUnits;

            jCheckStock.setText(Integer.toString(iUnits));
          }

        } else {
          jCheckStock.setText(null);
        }
      } catch (BasicException ex) {
        log.error(ex.getMessage());
      }
    }

    if (listener != null) {
      listener.restart();
    }
  }//GEN-LAST:event_jCheckStockActionPerformed

  private void jCheckStockMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckStockMouseClicked
    if (evt.getClickCount() == 2) {
      if (listener != null) {
        listener.stop();
      }

      int i = m_ticketlines.getSelectedIndex();
      if (i < 0) {
        Toolkit.getDefaultToolkit().beep();
      } else {
        try {
          TicketLineInfo line = m_oTicket.getLine(i);
          String pId = line.getProductID();
          String location = m_App.getInventoryLocation();
          ProductStock checkProduct;
          checkProduct = dlSales.getProductStockState(pId, location);

          Double pMin;
          Double pMax;
          Double pUnits;
          Date pMemoDate;
          String content;

          if (!location.equals(checkProduct.getLocation())) {
            content = AppLocal.getIntString("message.location.current");
            JFrame frame = new JFrame();
            JOptionPane.showMessageDialog(frame,
                    content,
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
          } else {
            if (checkProduct.getMinimum() != null) {
              pMin = checkProduct.getMinimum();
            } else {
              pMin = 0.;
            }
            if (checkProduct.getMaximum() != null) {
              pMax = checkProduct.getMaximum();
            } else {
              pMax = 0.;
            }
            if (checkProduct.getUnits() != null) {
              pUnits = checkProduct.getUnits();
            } else {
              pUnits = 0.;
            }
            if (checkProduct.getMemoDate() != null) {
              pMemoDate = checkProduct.getMemoDate();
            } else {
              pMemoDate = null;
            }

            content = "<html>" +
                    "<b>" + AppLocal.getIntString("label.currentstock") +
                    " : " + "</b>" + checkProduct.getUnits() + "<br>" +
                    "<b>" + AppLocal.getIntString("label.maximum") +
                    " : " + "</b>" + pMax + "<br>" +
                    "<b>" + AppLocal.getIntString("label.minimum") +
                    " : " + "</b>" + pMin + "<br>" +
                    "<b>" + AppLocal.getIntString("label.proddate") +
                    " : " + "</b>" + pMemoDate + "<br>";

            JFrame frame = new JFrame();
            JOptionPane.showMessageDialog(frame,
                    content,
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
          }
        } catch (BasicException ex) {
          log.error(ex.getMessage());
        }
      }

      if (listener != null) {
        listener.restart();
      }
    }
  }//GEN-LAST:event_jCheckStockMouseClicked

  private void jBtnCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCustomerActionPerformed
    if (listener != null) {
      listener.stop();
    }
    Object[] options = {"Create", "Find", "Cancel"};

    int n = JOptionPane.showOptionDialog(null,
            AppLocal.getIntString("message.customeradd"),
            AppLocal.getIntString("label.customer"),
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[2]);

    if (n == 0) {
      JDialogNewCustomer dialog = JDialogNewCustomer.getDialog(this, m_App);
      dialog.setVisible(true);

      CustomerInfoExt m_customerInfo = dialog.getSelectedCustomer();
      if (dialog.getSelectedCustomer() != null) {
        try {
          m_oTicket.setCustomer(dlSales.loadCustomerExt
                  (dialog.getSelectedCustomer().getId()));
        } catch (BasicException ex) {
          log.error(ex.getMessage());
        }
      }
    }

    if (n == 1) {
      JCustomerFinder finder = JCustomerFinder.getCustomerFinder(this, dlCustomers);

      if (m_oTicket.getCustomerId() == null) {
        finder.setAppView(m_App);
        finder.search(m_oTicket.getCustomer());
        finder.executeSearch();
        finder.setVisible(true);

        if (finder.getSelectedCustomer() != null) {
          try {
            m_oTicket.setCustomer(dlSales.loadCustomerExt
                    (finder.getSelectedCustomer().getId()));
            if ("restaurant".equals(m_App.getProperties().getProperty("machine.ticketsbag"))) {
              restDB.setCustomerNameInTableByTicketId(dlSales.loadCustomerExt
                      (finder.getSelectedCustomer().getId()).toString(), m_oTicket.getId());
            }

            checkCustomer();

            m_jTicketId.setText(m_oTicket.getName());

          } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    AppLocal.getIntString("message.cannotfindcustomer"), e);
            msg.show(this);
          }
        } else {
          restDB.setCustomerNameInTableByTicketId(null, m_oTicket.getId());
          m_oTicket.setCustomer(null);
          Notify("notify.customerremove");
        }

      } else {
        if (JOptionPane.showConfirmDialog(this,
                AppLocal.getIntString("message.customerchange"),
                AppLocal.getIntString("title.editor"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

          finder.setAppView(m_App);
          finder.search(m_oTicket.getCustomer());
          finder.executeSearch();
          finder.setVisible(true);

          if (finder.getSelectedCustomer() != null) {
            try {
              m_oTicket.setCustomer(dlSales.loadCustomerExt
                      (finder.getSelectedCustomer().getId()));
              if ("restaurant".equals(m_App.getProperties().getProperty("machine.ticketsbag"))) {
                restDB.setCustomerNameInTableByTicketId(dlSales.loadCustomerExt
                        (finder.getSelectedCustomer().getId()).toString(), m_oTicket.getId());
              }

              checkCustomer();

              m_jTicketId.setText(m_oTicket.getName());

            } catch (BasicException e) {
              MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                      AppLocal.getIntString("message.cannotfindcustomer"), e);
              msg.show(this);
            }
          } else {
            restDB.setCustomerNameInTableByTicketId(null, m_oTicket.getId());
            m_oTicket.setCustomer(null);
          }
        }
      }
    }

    refreshTicket();

  }//GEN-LAST:event_jBtnCustomerActionPerformed

  private void m_jPanContainerFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_jPanContainerFocusLost
    jPanContainerFocusLost(evt);
  }//GEN-LAST:event_m_jPanContainerFocusLost

    private void m_jKeyFactoryKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_jKeyFactoryKeyTyped

        m_jKeyFactory.setText(null);

        stateTransition(evt.getKeyChar());
    }//GEN-LAST:event_m_jKeyFactoryKeyTyped

    private void m_jEnterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEnterActionPerformed

        stateTransition('\n');
    }//GEN-LAST:event_m_jEnterActionPerformed

    private void m_jaddtaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jaddtaxActionPerformed
        m_jKeyFactory.requestFocus();
    }//GEN-LAST:event_m_jaddtaxActionPerformed

    private void m_jNumberKeysKeyPerformed(com.openbravo.beans.JNumberEvent evt) {//GEN-FIRST:event_m_jNumberKeysKeyPerformed

        stateTransition(evt.getKey());

        j_btnRemotePrt.setEnabled(true);
        j_btnRemotePrt.revalidate();
    }//GEN-LAST:event_m_jNumberKeysKeyPerformed

    private void m_jDollarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDollarBtnActionPerformed
        // TODO add your handling code here:
        String sValue = JStringDialog.showEditString(this, "Product Name");
        if(sValue !=null){
            Integer iValue = JNumberPop.showEditNumber(this, "Enter $");
            if(iValue !=null){
                m_jPrice.setText(String.valueOf(iValue));
                ProductInfoExt product = getInputProduct();
                product.setName(sValue);
                addTicketLine(product, 1.0, product.getPriceSell());
            }
        }
    }//GEN-LAST:event_m_jDollarBtnActionPerformed

    private void m_jPayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jPayActionPerformed
        // TODO add your handling code here:
        if (m_oTicket.getLinesCount() > 0) {
          if (closeTicket(m_oTicket, m_oTicketExt)) {
            m_ticketsbag.deleteTicket();
            String autoLogoff = (m_App.getProperties().getProperty("till.autoLogoff"));
            if (autoLogoff != null) {
              if (autoLogoff.equals("true")) {
                if ("restaurant".equals(
                        m_App.getProperties().getProperty("machine.ticketsbag"))
                        && ("true".equals(m_App.getProperties().getProperty("till.autoLogoffrestaurant")))) {
                  deactivate();
                  setActiveTicket(null, null, false);
                } else {
                  ((JRootApp) m_App).closeAppView();
                }
              }
            };
          } else {
            refreshTicket();
          }
        } else {
          Toolkit.getDefaultToolkit().beep();
        }
    }//GEN-LAST:event_m_jPayActionPerformed

    private void m_jTicketIdMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_jTicketIdMouseClicked
        // TODO add your handling code here:
        DateFormat m_dateformat = new SimpleDateFormat("hh:mm");
        java.util.Date m_dDate = new Date();
        String sValue = JStringDialog.showEditString(this, "Change ticket name"); 
        if (sValue!=null){
            m_jTicketId.setText(sValue);
            m_oTicket.setTName(sValue + " - (" + m_dateformat.format(m_dDate) + " " 
                        + Long.toString(m_dDate.getTime() % 1000) + ")");
            String currentTicket = (String) m_oTicketExt;
            if (currentTicket != null) {
                try {
                    dlReceipts.updateSharedTicket(currentTicket, m_oTicket, m_oTicket.getPickupId());
                } catch (BasicException ex) {
                    log.error(ex.getMessage());
                }
            }
            refreshTicket();
        }
    }//GEN-LAST:event_m_jTicketIdMouseClicked

    private void j_mLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mLogoutActionPerformed
        ((JRootApp)m_App).closeAppView();
    }//GEN-LAST:event_j_mLogoutActionPerformed

    private void j_mDiscountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mDiscountActionPerformed
        doDiscount();
    }//GEN-LAST:event_j_mDiscountActionPerformed

    private void m_jQtyPlusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyPlusActionPerformed
        int i = m_ticketlines.getSelectedIndex();
        TicketLineInfo line = m_oTicket.getLine(i);        

        if (!"".equals(line.getIsOrdered())) {
            line.setNewMultiply(line.getNewMultiply() + 1);
        }

        line.setMultiply(line.getMultiply() + 1);
        m_oTicket.removeLine(i);
        m_oTicket.addLine(line);
        
        setActiveTicket(m_oTicket, m_oTicketExt, false);
    }//GEN-LAST:event_m_jQtyPlusActionPerformed

    private void jBtnNewTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNewTicketActionPerformed
        // TODO add your handling code here:
        java.util.List<Place> m_aplaces;
        Place m_place;
        try{
            if(m_oTicket.getLinesCount() <= 0) {
                dlReceipts.deleteSharedTicket((String) m_oTicketExt);

//                restDB.clearPlaceById((String) m_oTicketExt);
            } else {
                dlReceipts.updateSharedTicket(
                    (String) m_oTicketExt,
                    m_oTicket,
                    m_oTicket.getPickupId()
                );
            }   

            SentenceList sent = new StaticSentence(
                m_App.getSession(),
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
        while(true) {
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
            new MessageInf(e).show(this);
        }
        m_place.setPeople(true);
        m_place.setGuests(restDB.updateGuestsInTable(m_place.getId()));
        setActiveTicket(ticket, m_place.getId(), false);
    }//GEN-LAST:event_jBtnNewTicketActionPerformed

    private void jEditAttributesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEditAttributesActionPerformed
       
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
          Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            try{
                TicketLineInfo line = m_oTicket.getLine(i);
                if(line.getProductAttSetId()!=null){
                    JFlowPanel m_flowPanel = m_jCatalog.showModifiertoMod(line.getProductAttSetId(), line.getProductAttSetInstId());
                    Window window = getWindow(JPanelTicket.this);
                    JModDialog modDialog = JModDialog.newJDialog(window);
                    ProductInfoExt product = dlSales.getProductInfo(line.getProductID());
                    m_updateTicket = m_jCatalog.getUpdateActionListener(product);
                    modDialog.setUpdateAction(m_updateTicket);
                    String m_resModDialog = modDialog.showModDialog(m_flowPanel, product);
                    if(m_resModDialog == "ok") {
                        removeTicketLineForUpdate(i);
                        m_jCatalog.loadCatalog();
                        m_ticketlines.setSelectedIndex(m_oTicket.getLinesCount() - 1);
                    }
                }
            } catch (BasicException ex) {
                System.out.print(ex);
            }
        }
    }//GEN-LAST:event_jEditAttributesActionPerformed

    private void j_mCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mCompActionPerformed
        String sValue = JStringDialog.showEditString(this, "Comp");
        if (m_oTicket.getLinesCount() > 0) {
            for (int number= 0; number < m_oTicket.getLinesCount(); number++) {
                TicketLineInfo line = m_oTicket.getLine(number);
                if(!line.getIsVoid())
                    line.setIsComp(true);
                line.setReason(sValue);
                paintTicketLine(number, line);
            }
            refreshTicket();
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }//GEN-LAST:event_j_mCompActionPerformed

    private void jOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOptionActionPerformed
        // TODO add your handling code here:
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
          Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            TicketLineInfo line = m_oTicket.getLine(i);
            if(line.getIsVoid()){
                menuItemVoid.setEnabled(false);
                menuItemComp.setEnabled(false);
                menuItemUnComp.setEnabled(false);
            }else{
                menuItemVoid.setEnabled(true);
                if(line.getIsComp()){
                    menuItemComp.setEnabled(false);
                    menuItemUnComp.setEnabled(true);
                }else{
                    menuItemComp.setEnabled(true);
                    menuItemUnComp.setEnabled(false);
                }
            }
            Dimension popupSize = popup.getPreferredSize();
            popup.show(jPanel7, jOption.getX(), jOption.getY()-popupSize.height);
        }
    }//GEN-LAST:event_jOptionActionPerformed

    private void j_mPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mPrintActionPerformed
        if (isClosed)
            printTicket("Printer.TicketReceipt", m_oTicket, m_oTicketExt, "0");
        else
            printTicket("Printer.TicketPreview", m_oTicket, m_oTicketExt, "0");
    }//GEN-LAST:event_j_mPrintActionPerformed

    private void m_jComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jComboActionPerformed
        // TODO add your handling code here:
        Dimension popupSize = ticketPopup.getPreferredSize();
        Dimension buttonSize = m_jCombo.getPreferredSize();
        ticketPopup.show(jPanel3, jOption.getX(), jOption.getY() + buttonSize.height);
    }//GEN-LAST:event_m_jComboActionPerformed

    private void btnMergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMergeActionPerformed
        // TODO add your handling code here:
        try {

            dlReceipts.updateSharedTicket((String) m_oTicketExt, 
                m_oTicket,
                m_oTicket.getPickupId());

            receiptone = new SimpleReceipt(dlSystem.getResourceAsXML("Ticket.Line"), dlSales, dlCustomers, taxeslogic);
            receipttwo = new SimpleReceipt(dlSystem.getResourceAsXML("Ticket.Line"), dlSales, dlCustomers, taxeslogic);

            String m_oTicketExtString = (String) m_oTicketExt;
            java.util.List<SharedTicketInfo> l = dlReceipts.getSharedTicketListWithoutItself(m_oTicketExtString);
            Window window = getWindow(this);
            JTicketsBagSharedList listDialog = JTicketsBagSharedList.newJDialog(window);
            String id = listDialog.showTicketsList(l, dlReceipts, m_App, m_oTicketExtString);

            if (id != null && !id.equals(m_oTicketExtString)){
                TicketInfo ticket1 = dlReceipts.getSharedTicket(m_oTicketExtString);
                TicketInfo ticket2 = dlReceipts.getSharedTicket(id);

                receiptone.setTicket(ticket1, dlSystem.getResourceAsXML("Ticket.Line"));
                receipttwo.setTicket(ticket2, dlSystem.getResourceAsXML("Ticket.Line"));

                int lineSize = receiptone.getTicket().getLinesCount();

                if(lineSize > 0)
                    for (int i = 0; i < lineSize; i ++) {
                        TicketLineInfo[] lines = receiptone.getLinesByIndex(0);
                        receipttwo.addSelectedLines(lines);
                    }

                dlReceipts.deleteSharedTicket(m_oTicketExtString);
                dlReceipts.deleteSharedTicket(id);

                ticket2.setUser(m_App.getAppUserView().getUser().getUserInfo());
                dlReceipts.insertSharedTicket(id, ticket2, ticket2.getPickupId());

                setActiveTicket(ticket2, id, false); 
            }
        } catch (BasicException ex) {
            System.out.print(ex);
        }

    }//GEN-LAST:event_btnMergeActionPerformed

    private void btnTransferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransferActionPerformed
        // TODO add your handling code here:
        try {
            java.util.List people = dlSystem.listPeopleVisible();
            Window window = getWindow(this);
            JClockedInUserDialog listDialog = JClockedInUserDialog.newJDialog(window);
            String userId = m_App.getAppUserView().getUser().getId(); 

            listDialog.showUsersList(people, dlReceipts, userId);
            
            String m_sPeopleID = listDialog.getPeopleId();
            String m_sPeopleName = listDialog.getPeopleName();

            m_oTicket.setUser(m_App.getAppUserView().getUser().getUserInfoWith(m_sPeopleID, m_sPeopleName));

            transferFlag = true;
            m_currentUser = m_App.getAppUserView().getUser().getUserInfoWith(m_sPeopleID, m_sPeopleName);

            changeTicket(m_oTicketExt.toString());
            refreshTicket();
        } catch (BasicException ee) {
        }
    }//GEN-LAST:event_btnTransferActionPerformed

    private void m_jQtyMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyMinusActionPerformed
        // TODO add your handling code here:
        int i = m_ticketlines.getSelectedIndex();
        TicketLineInfo line = m_oTicket.getLine(i);

        if(line.getNewMultiply() <= 0 && !"".equals(line.getIsOrdered())) {
            JOptionPane.showMessageDialog(this,
                "Can't remove sent items",
                "Warning",
                JOptionPane.OK_OPTION);    
            return;
        }

        if(line.getMultiply() > 1)
            line.setMultiply(line.getMultiply() - 1);
        line.setNewMultiply(line.getNewMultiply() - 1);
        m_oTicket.removeLine(i);
        m_oTicket.addLine(line);
        setActiveTicket(m_oTicket, m_oTicketExt, false);
    }//GEN-LAST:event_m_jQtyMinusActionPerformed

    private void j_mCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mCancelActionPerformed
        // TODO add your handling code here:
        if (!isOrdered) {
            for (int i=0; i<m_oTicket.getLinesCount(); i++) {
                TicketLineInfo line = m_oTicket.getLine(i);

                line.setDiscountRate(0);
                line.setIsComp(false);
                line.setIsVoid(false);
                line.setReason("");

                paintTicketLine(i, line);
            }

            try {
                dlReceipts.updateSharedTicket(
                    m_oTicketExt.toString(),
                    m_oTicket,
                    m_oTicket.getPickupId()
                );

                dlReceipts.unlockSharedAllTicket(null);
            } catch (BasicException e) {}
        }

        setActiveTicket(null, null, false);
    }//GEN-LAST:event_j_mCancelActionPerformed

    private void j_mBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mBackActionPerformed
        // TODO add your handling code here:
        setActiveTicket(null, null, false);
    }//GEN-LAST:event_j_mBackActionPerformed

    private void j_mVoidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mVoidActionPerformed
        // TODO add your handling code here:
        try {
            Object[] payment = dlReceipts.getClosedPayment(closedTicket);
            String prompt = "", paymentType = "";

            paymentType = payment[0].toString().toUpperCase();
            if (!"CASH".equals(paymentType)) {
                prompt = "Do you want to remove the payment (" + paymentType + ": " + replaceWith(payment[1].toString(), payment[1].toString().length() - 4) +  ")";
            } else {
                prompt = "Do you want to remove the payment (" + paymentType + ")";
            }

            int input = JOptionPane.showConfirmDialog(this
                , prompt
                , "Confirm", JOptionPane.YES_NO_OPTION);

            if (input == 0) {
                if (!"CASH".equals(paymentType)) {
                    printTicket("Printer.TicketReceipt", m_oTicket, m_oTicketExt, "1");
                } else {
                    String sresource = dlSystem.getResourceAsXML("Printer.OpenDrawer");
                    try {
                        ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                        m_TTP.printTicket(script.eval(sresource).toString());
                    } catch (ScriptException | TicketPrinterException e) {}
                }

                String ticketid = dlReceipts.getClosedTicketID(closedTicket);
                if(!"".equals(ticketid)) {
                    dlReceipts.deletePayments(ticketid);
                    dlReceipts.deleteClosedTicket(closedTicket);
                }

                dlReceipts.insertSharedTicket(m_oTicketExt.toString(), m_oTicket, m_oTicket.getPickupId());
                isClosed = false;

                setActiveTicket(m_oTicket, m_oTicketExt, false);
            }
        } catch (BasicException e) {}
    }//GEN-LAST:event_j_mVoidActionPerformed
    

    public static String replaceWith(String s, int replace) {
        int length = s.length();
        String temp = s.substring(replace, length);
        for(int i = 0; i < length; i ++) temp = "*" + temp;
        return temp;
    }

    private void doTotalDiscount(Integer rate){
        
        if (m_oTicket.getLinesCount() > 0) {
            if (m_oTicket.getTotal() > 0.0 && rate > 0.0) {
                for (int number= 0; number < m_oTicket.getLinesCount(); number++) {
                    TicketLineInfo line = m_oTicket.getLine(number);
                    line.setDiscountRate(rate);
                    paintTicketLine(number, line);
                }
                refreshTicket();
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMerge;
    private javax.swing.JButton btnReprint1;
    private javax.swing.JButton btnSplit;
    private javax.swing.JButton btnTransfer;
    private javax.swing.JPanel catcontainer;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JButton jBtnCustomer;
    private javax.swing.JButton jBtnNewTicket;
    private javax.swing.JButton jCheckStock;
    private javax.swing.JButton jEditAttributes;
    private javax.swing.JButton jOption;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JButton j_btnRemotePrt;
    private javax.swing.JButton j_mBack;
    private javax.swing.JButton j_mCancel;
    private javax.swing.JButton j_mComp;
    private javax.swing.JButton j_mDiscount;
    private javax.swing.JButton j_mLogout;
    private javax.swing.JButton j_mPrint;
    private javax.swing.JButton j_mVoid;
    private javax.swing.JPanel m_jButtons;
    private javax.swing.JPanel m_jButtonsExt;
    private javax.swing.JButton m_jCombo;
    private javax.swing.JPanel m_jContEntries;
    private javax.swing.JButton m_jDelete;
    private javax.swing.JButton m_jDollarBtn;
    private javax.swing.JButton m_jEditLine;
    private javax.swing.JButton m_jEnter;
    private javax.swing.JTextField m_jKeyFactory;
    private javax.swing.JLabel m_jLblTip;
    private javax.swing.JLabel m_jLblTotalEuros1;
    private javax.swing.JLabel m_jLblTotalEuros2;
    private javax.swing.JLabel m_jLblTotalEuros3;
    private javax.swing.JPanel m_jOptions;
    private javax.swing.JPanel m_jPanContainer;
    private javax.swing.JPanel m_jPanEntries;
    private javax.swing.JPanel m_jPanEntriesE;
    private javax.swing.JPanel m_jPanTicket;
    private javax.swing.JPanel m_jPanTotals;
    private javax.swing.JPanel m_jPanTotals1;
    private javax.swing.JPanel m_jPanelBag;
    private javax.swing.JPanel m_jPanelBagTop;
    private javax.swing.JPanel m_jPanelCentral;
    private javax.swing.JPanel m_jPanelScripts;
    private javax.swing.JButton m_jPay;
    private javax.swing.JLabel m_jPor;
    private javax.swing.JLabel m_jPrice;
    private javax.swing.JButton m_jQtyMinus;
    private javax.swing.JButton m_jQtyPlus;
    private javax.swing.JLabel m_jSubtotalEuros;
    private javax.swing.JComboBox m_jTax;
    private javax.swing.JLabel m_jTaxesEuros;
    private javax.swing.JLabel m_jTicketId;
    private javax.swing.JLabel m_jTip;
    private javax.swing.JLabel m_jTotalEuros;
    private com.alee.extended.button.WebSwitch m_jaddtax;
    // End of variables declaration//GEN-END:variables

/* Remote Orders Display
    We only know about Roxy Pos orders and won't try and handle any
    that are injected from an external source
*/

  public void remoteOrderDisplay() {
    remoteOrderDisplay(remoteOrderId(), 1, true);
  }

  public void remoteOrderDisplay(String id) {
    remoteOrderDisplay(id, 1, true);
  }

  public void remoteOrderDisplay(Integer display) {
    remoteOrderDisplay(remoteOrderId(), display, false);
  }

    public String remoteOrderId() {

        String id = "";

        if ((m_oTicket.getCustomer() != null)) {
            return m_oTicket.getCustomer().getName();
        } else if (m_oTicketExt != null) {
            return m_oTicketExt.toString();
        } else {
            if (m_oTicket.getPickupId() == 0) {
                try { 
                    m_oTicket.setPickupId(dlSales.getNextPickupIndex());
                } catch (BasicException e) {
                    m_oTicket.setPickupId(0);
                }
            }

            return getPickupString(m_oTicket);
        }
    }

  public void remoteOrderDisplay(String id, Integer display, boolean primary) {
    try {
      // may use later if/when external order injection
      // String orderUUID = UUID.randomUUID().toString();
      dlSystem.deleteOrder(id);
    } catch (BasicException ex) {
      log.error(ex.getMessage());
    }

    Timestamp orderTime = new Timestamp(System.currentTimeMillis());

    String orderID = "";

    for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
        if (!"".equals(m_oTicket.getLine(i).getIsOrdered()) ) {
            orderID = m_oTicket.getLine(i).getIsOrdered();
            break;
        }
    }

    if ("".equals(orderID)) {
        try {
            orderID = String.valueOf(dlSales.getNextPickupIndex());
        } catch (BasicException e) {}
    }

    for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
      try {
        if (primary) {
          if ((m_oTicket.getLine(i).getProperty("display") == null)
                  || ("".equals(m_oTicket.getLine(i).getProperty("display")))) {
            display = 1;
          } else {
            display = Integer.parseInt(m_oTicket.getLine(i).getProperty("display"));
          }
        }

        if (m_oTicket.getLine(i).getStationID() != null) {
            if (!"".equals(m_oTicket.getLine(i).getIsOrdered())) {
                if (m_oTicket.getLine(i).getNewMultiply() > 0) {
                    dlSystem.addOrder(
                        orderID
                        , (int) m_oTicket.getLine(i).getNewMultiply()
                        , m_oTicket.getLine(i).getProductName()
                        , m_oTicket.getLine(i).getProductAttSetInstDesc()
                        , m_oTicket.getLine(i).getNote()
                        , m_oTicketExt.toString()
                        , orderTime
                        , display
                        , String.valueOf(System.currentTimeMillis())
                        , null
                        , m_oTicket.getLine(i).getOrdertime()
                        , m_oTicket.getLine(i).getStationID());
                }
            } else {
                dlSystem.addOrder(
                    orderID
                    , (int) m_oTicket.getLine(i).getMultiply()
                    , m_oTicket.getLine(i).getProductName()
                    , m_oTicket.getLine(i).getProductAttSetInstDesc()
                    , m_oTicket.getLine(i).getNote()
                    , m_oTicketExt.toString()
                    , orderTime
                    , display
                    , String.valueOf(System.currentTimeMillis())
                    , null
                    , m_oTicket.getLine(i).getOrdertime()
                    , m_oTicket.getLine(i).getStationID());
            }
        }

        m_oTicket.getLine(i).setIsOrdered(orderID);
        m_oTicket.getLine(i).setNewMultiply(0);

      } catch (BasicException ex) {
        log.error(ex.getMessage());
      }
    }
  }

  public static void addCutCopyPasteInterceptor(JTextComponent tc, BiConsumer<String, String> listener) {
    tc.registerKeyboardAction(ae -> handlePaste(tc, listener),
            KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);

  }

  private static void handlePaste(JTextComponent tc, BiConsumer<String, String> listener) {
    try {
      String s = (String) Toolkit.getDefaultToolkit()
              .getSystemClipboard()
              .getData(DataFlavor.stringFlavor);
      tc.paste();
      listener.accept("PASTE", s);

    } catch (Exception e) {
      System.err.println(e);
    }
  }
    private void doComp(){
        String sValue = JStringDialog.showEditString(this, "Discount");
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
          Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            TicketLineInfo line = m_oTicket.getLine(i);
            line.setIsComp(true);
            line.setIsVoid(false);
            line.setReason(sValue);
            paintTicketLine(i, line);
        }
    }
    private void doUnComp(){
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
          Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            TicketLineInfo line = m_oTicket.getLine(i);
            line.setReason("");
            line.setIsComp(false);
            paintTicketLine(i, line);
        }
    }
    private void doVoid(){
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
          Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            TicketLineInfo line = m_oTicket.getLine(i);
            line.setIsComp(false);
            line.setIsVoid(true);
            paintTicketLine(i, line);
        }
    }
    private void doDetails(){
        count = (int) m_oTicket.getArticlesCount();     // get existing line value

        int i = m_ticketlines.getSelectedIndex();

        if (i < 0) {
          Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
          try {
            TicketLineInfo newline = JProductLineView.showMessage(this, m_App, m_oTicket.getLine(i));
            if (newline != null) {
              paintTicketLine(i, newline);
            }

          } catch (BasicException e) {
            new MessageInf(e).show(this);
          }
        }
    }

    private void doDiscount(){
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
          Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            String role = m_App.getAppUserView().getUser().getRole();
            Integer iValue = JDiscountPop.showEditNumber(this, "Choose Discount Mode", role);
            String sValue = "";

            if(iValue !=null){
                switch(iValue) {
                    case 0:
                        doDiscountNone();
                        break;
                    case 1:
                        sValue = JStringDialog.showEditString(this, "Discount");
                        if(!"cancel".equals(sValue))
                            doDiscountEmployee(sValue);
                        break;
                    case 2: 
                        sValue = JStringDialog.showEditString(this, "Discount");
                        if(!"cancel".equals(sValue))
                            doDiscountPolice(sValue);
                        break;
                    case 3: 
                        sValue = JStringDialog.showEditString(this, "Discount");
                        if(!"cancel".equals(sValue))
                            doDiscountITB(sValue);
                        break;
                    case 4: 
                        sValue = JStringDialog.showEditString(this, "Discount");
                        if(!"cancel".equals(sValue))
                            doDiscountManager(sValue);
                        break;
                }
            }
        }
    }

    private void doDiscountNone(){
        for (int i=0; i<m_oTicket.getLinesCount(); i++) {
            TicketLineInfo line = m_oTicket.getLine(i);
            line.setDiscountRate(0);
            line.setReason("");
            paintTicketLine(i, line);
        }
    }

    private void doDiscountEmployee(String reason){
        for (int i=0; i<m_oTicket.getLinesCount(); i++) {
            TicketLineInfo line = m_oTicket.getLine(i);
            line.setDiscountRate(30);
            line.setReason(reason);
            paintTicketLine(i, line);
        }
    }

    private void doDiscountPolice(String reason){
        for (int i=0; i<m_oTicket.getLinesCount(); i++) {
            TicketLineInfo line = m_oTicket.getLine(i);
            try {
                CategoryInfo categoryInfo;
                categoryInfo = dlSales.getCategoryInfo(line.getProductCategoryID());
                String category = categoryInfo.getTextTip();
                if(category.compareTo("food") == 0){
                    line.setDiscountRate(30);
                    line.setReason(reason);
                    paintTicketLine(i, line);
                } else {
                    line.setDiscountRate(0);
                    paintTicketLine(i, line);
                }
            } catch (BasicException eData) {
                new MessageInf(eData).show(this);
            }
        }
    }

    private void doDiscountITB(String reason){
        for (int i=0; i<m_oTicket.getLinesCount(); i++) {
            TicketLineInfo line = m_oTicket.getLine(i);
            try {
                CategoryInfo categoryInfo;
                categoryInfo = dlSales.getCategoryInfo(line.getProductCategoryID());
                String category = categoryInfo.getTextTip();
                if(category.compareTo("alcohol") == 0){
                    line.setDiscountRate(20);
                    line.setReason(reason);
                    paintTicketLine(i, line);
                } else {
                    line.setDiscountRate(0);
                    paintTicketLine(i, line);
                }
            } catch (BasicException eData) {
                new MessageInf(eData).show(this);
            }
        }
    }

    private void doDiscountManager(String reason){
        for (int i=0; i<m_oTicket.getLinesCount(); i++) {
            TicketLineInfo line = m_oTicket.getLine(i);
            line.setReason(reason);
            line.setDiscountRate(100);
            paintTicketLine(i, line);
        }
    }

    public void changeTicket(String id) {
        try {
            dlReceipts.updateSharedTicket((String) m_oTicketExt, 
                m_oTicket,
                m_oTicket.getPickupId());

            TicketInfo ticket = dlReceipts.getSharedTicket(id);
            dlReceipts.lockSharedTicket((String) m_oTicketExt, null);
            dlReceipts.lockSharedTicket(id, "locked");

            setActiveTicket(ticket, id, false);
        } catch (BasicException eData) {
            new MessageInf(eData).show(this);
        }
    }

    private void doNote(){
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
          Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            TicketLineInfo line = m_oTicket.getLine(i);
            String sValue = JStringDialog.showEditString(this, "Note", null, null, line.getNote());
            if(sValue !=null){
                line.setNote(sValue);
                paintTicketLine(i, line);
            }
        }
    }

    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window) parent;
        } else {
            return getWindow(parent.getParent());
        }
    }  
}

