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
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.ticket.UserInfo;
import com.openbravo.pos.util.WebSocketClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;


/**
 *
 * @author JG uniCenta
 */
public class JCompletedOrderDialog extends javax.swing.JDialog {
    
    private String m_sPeopleID;
    private String m_sPeopleName;
    
    /** Creates new form JTicketsBagSharedList */
    private JCompletedOrderDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }
    /** Creates new form JTicketsBagSharedList */
    private JCompletedOrderDialog(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }

    /**
     *
     * @param atickets
     * @param dlReceipts
     * @return
     */
    public void showTicketList(java.util.List<Object[]> completedTickets, DataLogicSystem dlSystem, WebSocketClient socketClient) {
        for (int i = 0; i < completedTickets.size(); i ++) {
            java.util.List<Object[]> order = null;
            m_jtickets.add(new JButtonTicket(completedTickets.get(i), dlSystem, socketClient));
        }
        
        int lsize = completedTickets.size();
        if (lsize > 0) {
            setVisible(true);
        }else{
            JOptionPane.showMessageDialog(this,
                AppLocal.getIntString("message.noCompletedTickets"), 
                AppLocal.getIntString("message.completedTicketTitle"), 
                JOptionPane.OK_OPTION);            
        }

        return;
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
//    public static JTicketsBagSharedList newJDialog(JTicketsBagShared ticketsbagshared) {
    public static JCompletedOrderDialog newJDialog(Window window) {
        
        JCompletedOrderDialog mydialog;
        if (window instanceof Frame) { 
            mydialog = new JCompletedOrderDialog((Frame) window, true);
        } else {
            mydialog = new JCompletedOrderDialog((Dialog) window, true);
        } 
 
        mydialog.initComponents();
        
        mydialog.jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        mydialog.jScrollPane1.getHorizontalScrollBar().setPreferredSize(new Dimension(25, 25));

        mydialog.setTitle("Completed Orders");
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
        private Object[] m_ticket = null;
        private DataLogicSystem m_dlSystem = null;
        private WebSocketClient m_socketClient = null;
                
        public JButtonTicket(Object[] ticket, DataLogicSystem dlSystem, WebSocketClient socketClient){
            
            super();

            m_ticket = ticket;
            m_dlSystem = dlSystem;
            m_socketClient = socketClient;
            
            setFocusPainted(false);
            setFocusable(false);
            setRequestFocusEnabled(false);
            setMargin(new Insets(8, 14, 8, 14));
            setFont(new java.awt.Font ("Dialog", 0, 14));
            setBackground(new java.awt.Color (220, 220, 220));
            addActionListener(new ActionListenerImpl());

            String buttonName = ticket[0] + " - ordered time: " + ticket[3];
            setText(buttonName);            

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy");
            LocalDateTime orderTime = LocalDateTime.parse(ticket[3].toString(), formatter);
            try {
                order = dlSystem.getOrderByTicketid(ticket[1].toString(), orderTime.toString());
            } catch (BasicException e) {
                throw new RuntimeException(e);
            }
        }

        private class ActionListenerImpl implements ActionListener {

            public ActionListenerImpl() {
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                        
                Window window = getWindow(JCompletedOrderDialog.this);
                JOrderDetail listDialog = JOrderDetail.newJDialog(window);

                String m_return = listDialog.showOrderDetail(order, m_ticket[0].toString(), m_socketClient, m_dlSystem);
                if(m_return.equals("ok")) {
                    m_socketClient.sendMessage("Re-Fire ticket");
                    for (int i=0; i<order.size(); i ++) {
                        try {
                            m_dlSystem.incompleteOrder(order.get(i)[0].toString());
                        } catch (BasicException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    dispose();
                }
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
