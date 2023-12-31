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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.util.WebSocketClient;
import javax.swing.JOptionPane;


/**
 *
 * @author JG uniCenta
 */
public class JClockedOutUserDialog extends javax.swing.JDialog {
    
    private String m_sPeopleID;
    private String m_sPeopleName;
    private String returnMSG;
    
    /** Creates new form JTicketsBagSharedList */
    private JClockedOutUserDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }
    /** Creates new form JTicketsBagSharedList */
    private JClockedOutUserDialog(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }

    /**
     *
     * @param peoples
     * @param dlSystem
     * @param app
     * @param dlReceipts
     */
    public String showUsersList(java.util.List peoples, DataLogicReceipts dlReceipts, DataLogicSystem dlSystem, AppView app) {
        
        for (Object people : peoples) {
            AppUser user = (AppUser) people;
            if (!"0".equals(user.getRole())) m_jtickets.add(new JButtonTicket(user, dlReceipts, dlSystem, app));
        }  
     
        m_sPeopleID = null;        
        m_sPeopleName = null;

        int lsize = peoples.size();
        if (lsize > 0) {
            setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                "There is no Clockin User", 
                "Warning",
                JOptionPane.OK_OPTION);            
        }

        return returnMSG;
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
    public static JClockedOutUserDialog newJDialog(Window window) {
        
        JClockedOutUserDialog mydialog;
        if (window instanceof Frame) { 
            mydialog = new JClockedOutUserDialog((Frame) window, true);
        } else {
            mydialog = new JClockedOutUserDialog((Dialog) window, true);
        } 
 
        mydialog.initComponents();
        
        mydialog.jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        mydialog.jScrollPane1.getHorizontalScrollBar().setPreferredSize(new Dimension(25, 25));

        mydialog.setTitle("Clock in Users");
        
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
        
        private final AppUser m_People;
        private final DataLogicSystem m_dlSystem;
        private final AppView m_App;
        private WebSocketClient m_socketClient;

        public JButtonTicket(AppUser people, DataLogicReceipts dlReceipts, DataLogicSystem dlSystem, AppView app){
            
            super();
            
            m_People = people;
            m_dlSystem = dlSystem;
            m_App = app;
            m_socketClient = ((JRootApp) m_App).getSocketClient();

            setFocusPainted(false);
            setFocusable(false);
            setRequestFocusEnabled(false);
            setMargin(new Insets(8, 14, 8, 14));
            setFont(new java.awt.Font ("Dialog", 0, 14));
            setBackground(new java.awt.Color (220, 220, 220));
            addActionListener(new ActionListenerImpl());

            String buttonName = people.getName();

            setText(buttonName);            
        }

        private class ActionListenerImpl implements ActionListener {

            public ActionListenerImpl() {
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                String peopleId = m_People.getId();
                if(peopleId != null){
                    m_dlSystem.execCheckout(peopleId);
                    ((JRootApp) m_App).printClockOut(peopleId);
                    m_socketClient.sendMessage("UserClockOut");
                    setVisible(false);
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
        m_jButtonOK = new javax.swing.JButton();

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
        m_jButtonCancel.setOpaque(false);
        m_jButtonCancel.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jButtonCancel.setRequestFocusEnabled(false);
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel3.add(m_jButtonCancel);

        m_jButtonOK.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        m_jButtonOK.setText(AppLocal.getIntString("button.close")); // NOI18N
        m_jButtonOK.setFocusPainted(false);
        m_jButtonOK.setFocusable(false);
        m_jButtonOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonOK.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jButtonOK.setRequestFocusEnabled(false);
        m_jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonOKActionPerformed(evt);
            }
        });
        jPanel3.add(m_jButtonOK);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        setSize(new java.awt.Dimension(518, 533));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed
        returnMSG = "cancel";
        dispose();
        
    }//GEN-LAST:event_m_jButtonCancelActionPerformed

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonOKActionPerformed
        // TODO add your handling code here:
        returnMSG = "ok";
        dispose();
    }//GEN-LAST:event_m_jButtonOKActionPerformed
       
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JButton m_jButtonOK;
    private javax.swing.JPanel m_jtickets;
    // End of variables declaration//GEN-END:variables
    
}
