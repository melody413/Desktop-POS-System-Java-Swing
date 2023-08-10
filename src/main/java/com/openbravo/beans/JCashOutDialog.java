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

package com.openbravo.beans;


import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.openbravo.pos.sales.shared.*;
import com.openbravo.pos.sales.SharedTicketInfo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import com.openbravo.basic.BasicException;
import com.openbravo.pos.epm.EmployeeInfo;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.panels.PaymentTipsModel;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.UserInfo;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;


/**
 *
 * @author JG uniCenta
 */
public class JCashOutDialog extends javax.swing.JDialog {
    
    private String m_sPeopleID;
    private String m_sPeopleName;
    public AppView m_app;
    public TicketParser m_TTP;
    
    /** Creates new form JTicketsBagSharedList */
    private JCashOutDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }
    /** Creates new form JTicketsBagSharedList */
    private JCashOutDialog(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }

    /**
     *
     * @param atickets
     * @param dlReceipts
     * @return
     */
    public void showTicketList(java.util.List<Object[]> userList, DataLogicSystem dlSystem, AppView app, DataLogicReceipts dlReceipts) {
        m_app = app;
        m_TTP = new TicketParser(m_app.getDeviceTicket(), dlSystem);

        for (int i = 0; i < userList.size(); i ++) {
            java.util.List<Object[]> order = null;

            JButtonTicket btn = new JButtonTicket(userList.get(i), dlSystem);

            try {
                int count = 0;
                boolean isAllCashOut = false;
                boolean m_isUserCashOut = dlSystem.isUserCashOut(userList.get(i)[0].toString());

                java.util.List<SharedTicketInfo> l = dlReceipts.getSharedTicketListByUserID(userList.get(i)[0].toString());
                isAllCashOut = dlSystem.isAllUserCashOut();
                count = l.size();

                if(count > 0 || m_isUserCashOut == true) { 
                    btn.setEnabled(false);
                } else {
                    btn.setEnabled(true);
                }
            } catch (BasicException e) {}

            m_jtickets.add(btn);
        }

        setVisible(true);
    }

    public String getPeopleId() {
        return m_sPeopleID;
    }

    public String getPeopleName() {
        return m_sPeopleName;
    }
    
    /**
     *
     * @param 
     * @return
     */
    public static JCashOutDialog newJDialog(Window window) {
        
        JCashOutDialog mydialog;
        if (window instanceof Frame) { 
            mydialog = new JCashOutDialog((Frame) window, true);
        } else {
            mydialog = new JCashOutDialog((Dialog) window, true);
        } 
 
        mydialog.initComponents();
//        
        mydialog.jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        mydialog.jScrollPane1.getHorizontalScrollBar().setPreferredSize(new Dimension(25, 25));

        mydialog.setTitle("Cash Users Out");
        mydialog.m_jButtonCancel.setText("Cancel");
        
        return mydialog;
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

    private class JButtonTicket extends JButton {
        private java.util.List<Object[]> order = null;
        private Object[] m_user = null;
        private DataLogicSystem m_dlSystem = null;
                
        public JButtonTicket(Object[] user, DataLogicSystem dlSystem){
            
            super();

            m_user = user;
            m_dlSystem = dlSystem;
            
            setFocusPainted(false);
            setFocusable(false);
            setRequestFocusEnabled(false);
            setMargin(new Insets(8, 14, 8, 14));
            setFont(new java.awt.Font ("Dialog", 0, 14));
            setBackground(new java.awt.Color (220, 220, 220));
            addActionListener(new ActionListenerImpl());

            setText(user[1].toString());            
        }

        private class ActionListenerImpl implements ActionListener {

            public ActionListenerImpl() {
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                String sresource = m_dlSystem.getResourceAsXML("Printer.CashOut");

                try {
                    ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                    TicketInfo ticket = new TicketInfo();
                    ticket.setUser(new UserInfo(m_user[0].toString(), m_user[1].toString()));
                    String userid = m_user[0].toString();

                    boolean flag = true;
                    try {
                        String openedTime = m_dlSystem.getOpenedStoreTime();
                        PaymentTipsModel m_PaymentsToClose = PaymentTipsModel.loadInstance(m_app, openedTime, userid);
                        java.util.List<PaymentTipsModel.PaymentsLine> paymentLines = m_PaymentsToClose.getPaymentLines();
                        int noTipCount = 0;
                        for (int i=0; i<paymentLines.size(); i++) {
                            if(paymentLines.get(i).getTip() == 0) noTipCount ++;
                        }

                        if(noTipCount > 0) {
                            if (JOptionPane.showConfirmDialog(jPanel2, 
                                "You have " + noTipCount + " unentered tips, Would you like to continue ?", 
                                "Confirm", 
                                JOptionPane.YES_NO_OPTION, 
                                JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) flag = false;
                        }

                        if (flag == true) {
                            for (int i=0; i<paymentLines.size(); i++) {
                                try {
//                                    URL url = new URL("https://api.zeamster.com/v2/transactions");
//                                    HttpURLConnection http = (HttpURLConnection)url.openConnection();
//
//                                    http.setRequestMethod("POST");
//                                    http.setRequestProperty("user-id", "11ee0b9c54d50f16a441d36d");
//                                    http.setRequestProperty("user-api-key", "11ee24b6f1f17be4b1034fd1");
//                                    http.setRequestProperty("developer-id", "vJPDVw71");
//                                    http.setRequestProperty("Accept", "application/json");
//                                    http.setRequestProperty("Content-Type", "application/json");
//                                    http.setDoOutput(true);

                                    URL url = new URL("https://api.sandbox.zeamster.com/v2/transactions");
                                    HttpURLConnection http = (HttpURLConnection)url.openConnection();

                                    http.setRequestMethod("POST");
                                    http.setRequestProperty("user-id", "11edfee1b0fcd41c9276920a");
                                    http.setRequestProperty("user-api-key", "11edff78c1f0714c826f3ef2");
                                    http.setRequestProperty("developer-id", "vJPDVw71");
                                    http.setRequestProperty("Accept", "application/json");
                                    http.setRequestProperty("Content-Type", "application/json");
                                    http.setDoOutput(true);

                        //          // Set payload - Start
                                    String postData = "{\n" +
                                    "    \"transaction\": {\n" +
                                    "        \"action\": \"sale\",\n" +
                                    "        \"payment_method\": \"cc\",\n" +
                                    "        \"account_number\": \"" + paymentLines.get(i).getCardNum() + "\",\n" +
                                    "        \"exp_date\":\"" + paymentLines.get(i).getCardExpDate() + "\",\n" +
                                    "        \"transaction_amount\": " + new BigDecimal(paymentLines.get(i).getAmount()).setScale(8, RoundingMode.HALF_EVEN) + ",\n" +
                                    "        \"tip_amount\": " + new BigDecimal(paymentLines.get(i).getTip()).setScale(2, RoundingMode.HALF_EVEN) + "\n" +
                                    "    }\n" +
                                    "}";

                                    DataOutputStream wr = new DataOutputStream(http.getOutputStream());
                                    wr.writeBytes(postData);
                                    wr.flush();
                                    wr.close();
                        //          // Set payload - End

                                    JsonParser jp = new JsonParser(); //from gson
                                    JsonElement root = jp.parse(new InputStreamReader((InputStream) http.getContent()));

                                    http.disconnect();
                                } 
                                catch (MalformedURLException e) {
                                    System.out.println("^^^^^^^^^^^^ mal " + e.getMessage());
                                }
                                catch (IOException exception) {
                                    System.out.println("^^^^^^^^^^^^ IOEx " +  exception.getMessage());
                                }
                            }
                        }   
                    } catch (BasicException e) {}
        /////////////////////////////////////////////
                    if (flag == true) {
                        int totalOrders = m_dlSystem.getUserOrderCount(userid);
                        double totalTax = m_dlSystem.getUserOrderAmount(userid)[1];
                        double totalNet = m_dlSystem.getUserOrderAmount(userid)[0];
                        double grossSales = totalTax + totalNet;
                        double totalComps = m_dlSystem.getUserComp(userid);
                        double totalDiscounts = m_dlSystem.getUserDiscount(userid);
                        double totalCategorySummary = 0;

                        double subTotal = m_dlSystem.getUserSubTotal(userid)[0];
                        double tip = m_dlSystem.getUserSubTotal(userid)[1];
                        double totalGratutiyTip = tip;
                        double totalTaxNonCash = subTotal + totalGratutiyTip;
                        double totalGratutiyTip3 = totalGratutiyTip * 0.03;

                        double netGratutiyTip = totalGratutiyTip - totalGratutiyTip3;

                        java.util.List<Object[]> categorySummary = m_dlSystem.getUserCategorySummary(userid);
                        for(int i=0; i<categorySummary.size(); i++) {
                            totalCategorySummary += Double.parseDouble(categorySummary.get(i)[1].toString());
                        }

                        script.put("ticket", ticket);

                        script.put("totalOrders", totalOrders);
                        script.put("totalNet", new BigDecimal(totalNet).setScale(2, RoundingMode.HALF_EVEN));
                        script.put("totalTax", new BigDecimal(totalTax).setScale(2, RoundingMode.HALF_EVEN));
                        script.put("grossSales", new BigDecimal(grossSales).setScale(2, RoundingMode.HALF_EVEN));
                        script.put("totalComps", new BigDecimal(0 - totalComps).setScale(2, RoundingMode.HALF_EVEN));
                        script.put("totalDiscounts", new BigDecimal(0 - totalDiscounts).setScale(2, RoundingMode.HALF_EVEN));
                        script.put("totalOrdersAmount", new BigDecimal(totalNet).setScale(2, RoundingMode.HALF_EVEN));

                        script.put("categorySummary", categorySummary);
                        script.put("totalCategorySummary", new BigDecimal(totalCategorySummary).setScale(2, RoundingMode.HALF_EVEN));

                        script.put("subTotal", new BigDecimal(subTotal).setScale(2, RoundingMode.HALF_EVEN));
                        script.put("tip", new BigDecimal(tip).setScale(2, RoundingMode.HALF_EVEN));
                        script.put("totalGratutiyTip", new BigDecimal(totalGratutiyTip).setScale(2, RoundingMode.HALF_EVEN));
                        script.put("totalTaxNonCash", new BigDecimal(totalTaxNonCash).setScale(2, RoundingMode.HALF_EVEN));

                        script.put("totalGratutiyTip3", new BigDecimal(totalGratutiyTip3).setScale(2, RoundingMode.HALF_EVEN));
                        script.put("netGratutiyTip", new BigDecimal(netGratutiyTip).setScale(2, RoundingMode.HALF_EVEN));

                        m_TTP.printTicket(script.eval(sresource).toString(), ticket);
                        m_dlSystem.updateUserCashOut(userid);
                    }
                } catch (ScriptException | TicketPrinterException | BasicException e) {
                    System.out.println("********* printException: " + e.getMessage());
                }
                dispose();
            }
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        m_jtickets = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jButtonCancel = new javax.swing.JButton();

        setTitle(AppLocal.getIntString("caption.tickets")); // NOI18N
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel2.setLayout(new java.awt.BorderLayout());

        m_jtickets.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jtickets.setLayout(new java.awt.GridLayout(0, 1, 5, 5));
        jPanel2.add(m_jtickets, java.awt.BorderLayout.NORTH);

        jScrollPane1.setViewportView(jPanel2);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        jPanel3.add(jPanel4);

        m_jButtonCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        m_jButtonCancel.setText(AppLocal.getIntString("button.close")); // NOI18N
        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setFocusable(false);
        m_jButtonCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonCancel.setPreferredSize(new java.awt.Dimension(100, 45));
        m_jButtonCancel.setRequestFocusEnabled(false);
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel3.add(m_jButtonCancel);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        setSize(new java.awt.Dimension(518, 533));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed
        dispose();        
    }//GEN-LAST:event_m_jButtonCancelActionPerformed
       
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JPanel m_jtickets;
    // End of variables declaration//GEN-END:variables
}
