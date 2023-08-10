//    Roxy Pos  - Touch Friendly Point Of Sale
//    Copyright © 2009-2020 uniCenta
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

package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JNumberDialog;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.TableRendererBasic;
import com.openbravo.data.loader.Session;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.sales.restaurant.JTicketsBagRestaurantMap;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author adrianromero
 */
public class JPanelTips extends JPanel implements JPanelView, BeanFactoryApp {
    
    private AppView m_App;
    private DataLogicSystem m_dlSystem;
    
    private PaymentTipsModel m_PaymentsToClose = null;   
    
    private TicketParser m_TTP;
    private final DateFormat df= new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");   
    
    private Session s;
    private Connection con;  
    private Statement stmt;
    private Integer result;
    private Integer dresult;    
    private String SQL;
    private ResultSet rs;
    
    private AppUser m_User;
    
    private final ComboBoxValModel m_ReasonModel;  
    List<PaymentTipsModel.PaymentsLine> paymentLines;

    private final JTicketsBagRestaurantMap m_restaurantmap;
    
    /** Creates new form JPanelCloseMoney */
    public JPanelTips(AppView app, JTicketsBagRestaurantMap restaurantmap) {
        m_App = app;
        m_restaurantmap = restaurantmap;
        initComponents();                   

        m_ReasonModel = new ComboBoxValModel();
        m_ReasonModel.add(AppLocal.getIntString("cboption.preview"));
        m_ReasonModel.add(AppLocal.getIntString("cboption.reprint"));               
    }
    
    /**
     *
     * @param app
     * @throws BeanFactoryException
     */
    @Override
    public void init(AppView app) throws BeanFactoryException {
        
        m_App = app;        
        m_dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        m_TTP = new TicketParser(m_App.getDeviceTicket(), m_dlSystem);

        m_jTicketTable.setDefaultRenderer(Object.class, new TableRendererBasic(
            new Formats[] {new FormatsPayment(), Formats.CURRENCY}));
        m_jTicketTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        m_jScrollTableTicket.getVerticalScrollBar().setPreferredSize(new Dimension(25, 25));       
        m_jTicketTable.getTableHeader().setReorderingAllowed(false);         
        m_jTicketTable.setRowHeight(30);
        m_jTicketTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    /**
     *
     * @return
     */
    @Override
    public Object getBean() {
        return this;
    }
    
    /**
     *
     * @return
     */
    @Override
    public JComponent getComponent() {
        return this;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.CloseTPV");
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {
        loadData();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {
        return true;
    }  
    
    private void loadData() throws BasicException {

        m_dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");

        m_jTicketTable.setModel(new DefaultTableModel());

        String openedTime = m_dlSystem.getOpenedStoreTime();
        String userid = m_App.getAppUserView().getUser().getId();
            
        // LoadData
        m_PaymentsToClose = PaymentTipsModel.loadInstance(m_App, openedTime, userid);
        paymentLines = m_PaymentsToClose.getPaymentLines();
        
        if (m_PaymentsToClose.getPayments() != 0 
                || m_PaymentsToClose.getSales() != 0) {
        }          
        
        m_jTicketTable.setModel(m_PaymentsToClose.getPaymentsModel());
                
        TableColumnModel jColumns = m_jTicketTable.getColumnModel();

        JTableHeader header = m_jTicketTable.getTableHeader();
        header.setPreferredSize(new Dimension(100, 30));
        m_jTicketTable.setRowHeight(32);
        
        jColumns.getColumn(0).setPreferredWidth(130);
        jColumns.getColumn(0).setResizable(false);
        jColumns.getColumn(1).setPreferredWidth(100);
        jColumns.getColumn(1).setResizable(false);

        m_jUpdateTip.setText("Update Tip");
        m_jUpdateTip.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jCancel.setText(" Cancel");
        m_jCancel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    }

    private class FormatsPayment extends Formats {
        @Override
        protected String formatValueInt(Object value) {
            return AppLocal.getIntString("transpayment." + (String) value);
        }   
        @Override
        protected Object parseValueInt(String value) throws ParseException {
            return value;
        }
        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.LEFT;
        }         
    }    
   
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        m_jScrollTableTicket = new javax.swing.JScrollPane();
        m_jTicketTable = new javax.swing.JTable();
        m_jCancel = new javax.swing.JButton();
        m_jUpdateTip = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        m_jScrollTableTicket.setBorder(null);
        m_jScrollTableTicket.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        m_jScrollTableTicket.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jScrollTableTicket.setMinimumSize(new java.awt.Dimension(350, 140));
        m_jScrollTableTicket.setPreferredSize(new java.awt.Dimension(325, 150));

        m_jTicketTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jTicketTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        m_jTicketTable.setFocusable(false);
        m_jTicketTable.setIntercellSpacing(new java.awt.Dimension(0, 1));
        m_jTicketTable.setRequestFocusEnabled(false);
        m_jTicketTable.setShowVerticalLines(false);
        m_jScrollTableTicket.setViewportView(m_jTicketTable);

        jPanel1.add(m_jScrollTableTicket, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 930, 630));

        m_jCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        m_jCancel.setText(AppLocal.getIntString("button.closecash")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        m_jCancel.setToolTipText(bundle.getString("tooltip.btn.closecash")); // NOI18N
        m_jCancel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jCancel.setIconTextGap(2);
        m_jCancel.setInheritsPopupMenu(true);
        m_jCancel.setMaximumSize(new java.awt.Dimension(85, 33));
        m_jCancel.setMinimumSize(new java.awt.Dimension(85, 33));
        m_jCancel.setPreferredSize(new java.awt.Dimension(150, 45));
        m_jCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jCancelActionPerformed(evt);
            }
        });
        jPanel1.add(m_jCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 660, 100, -1));

        m_jUpdateTip.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jUpdateTip.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/calculator.png"))); // NOI18N
        m_jUpdateTip.setText(AppLocal.getIntString("button.closecash")); // NOI18N
        m_jUpdateTip.setToolTipText(bundle.getString("tooltip.btn.closecash")); // NOI18N
        m_jUpdateTip.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jUpdateTip.setIconTextGap(2);
        m_jUpdateTip.setInheritsPopupMenu(true);
        m_jUpdateTip.setMaximumSize(new java.awt.Dimension(85, 33));
        m_jUpdateTip.setMinimumSize(new java.awt.Dimension(85, 33));
        m_jUpdateTip.setPreferredSize(new java.awt.Dimension(150, 45));
        m_jUpdateTip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jUpdateTipActionPerformed(evt);
            }
        });
        jPanel1.add(m_jUpdateTip, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 660, 130, -1));

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jCancelActionPerformed
        m_restaurantmap.viewTables();
    }//GEN-LAST:event_m_jCancelActionPerformed

    private void m_jUpdateTipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jUpdateTipActionPerformed
        // TODO add your handling code here:
        int selectedRow = m_jTicketTable.getSelectedRow();
        if(selectedRow >= 0) {
            String paymentID = paymentLines.get(selectedRow).getPaymentID();
            double tip = JNumberDialog.showEditNumber(this, 
                AppLocal.getIntString("label.scale"), 
                AppLocal.getIntString("label.scaleinput"),
                null);

            m_dlSystem.updateTip(paymentID, tip);
            try {
                loadData();
            } catch (BasicException e) {}
        }
    }//GEN-LAST:event_m_jUpdateTipActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton m_jCancel;
    private javax.swing.JScrollPane m_jScrollTableTicket;
    private javax.swing.JTable m_jTicketTable;
    private javax.swing.JButton m_jUpdateTip;
    // End of variables declaration//GEN-END:variables
    
}
