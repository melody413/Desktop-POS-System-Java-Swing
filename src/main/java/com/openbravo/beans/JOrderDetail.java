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
import java.awt.geom.AffineTransform;
import java.awt.font.FontRenderContext;
import java.time.LocalDateTime;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 *
 * @author JG uniCenta
 */
public class JOrderDetail extends javax.swing.JDialog {
    
    private String m_sPeopleID;
    private String m_sPeopleName;
    private String m_return = "";   
private WebSocketClient m_socketClient;
    
    /** Creates new form JTicketsBagSharedList */
    private JOrderDetail(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }
    /** Creates new form JTicketsBagSharedList */
    private JOrderDetail(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }

    /**
     *
     * @param atickets
     * @param dlReceipts
     * @return
     */
    public String showOrderDetail(java.util.List<Object[]> order, String ticketID, WebSocketClient socketClient, DataLogicSystem dlSystem) {
        m_socketClient = socketClient;
        //orderPanel - ticket Panel
        JPanel orderPanel;
        orderPanel = new JPanel();
        orderPanel.setLayout(new java.awt.FlowLayout(FlowLayout.CENTER));

        //panel2 - title Panel
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0,10));

        panel2.setPreferredSize(new Dimension(450, 30));
        Color tPColor = new Color(71, 67, 67);
        panel2.setBackground(tPColor);
        //textfield in title Panel
        JLabel title = new JLabel();
        title.setText("#" + ticketID);
        title.setFont(new Font("Serif", Font.BOLD, 16));
        //Change text font color
        title.setForeground(Color.WHITE);
        title.setBackground(tPColor);
        panel2.add(title, BorderLayout.WEST);

        //panel3 - ticket lines main panel, containing all ticketlines
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));

        int panel4Height = 0;
        //fetching out ticket lines of a particular ticket by its ticketid
        int count = 0;
        for(int i = 0; i < order.size(); i ++) {
            Object[] obj1 = order.get(i);
                //panel4 - containing panel5 and panel6
                JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
                JPanel panel5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));

                //contains the heading of dish
                JLabel detail = new JLabel();
                detail.setText((String) obj1[2]); //fetching detail from query
                detail.setFont(new Font("Serif", Font.BOLD, 14));
                int detailAreaHeight = 30;
                detail.setPreferredSize(new Dimension(430, detailAreaHeight));

                //fetching out attributes from object
                String attributes = "";
                if (obj1[3] != null) {
                    attributes = (String) obj1[3];
                }

                String[] att = null;
                String attribute = "";
                if (attributes != null) {
                    att = attributes.split("/");
                    attribute = att[0];
                    for (int j = 1; j < att.length; j ++) {
                        attribute += ", " + att[j];
                    }
                }

                String formatted = "<html><font size = '4'><div>" + attribute + "</div></font></html>";
                Font font = new Font("Serif", Font.PLAIN, 14);

                //textArea containing the attributes in form of String
                int textAreaHeight = 0;
                JLabel textArea = new JLabel();
                textArea.setText(formatted);
                textArea.setBorder(BorderFactory.createEmptyBorder());

                textArea.setFont(font);
                FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
                int attributeWidth = (int) (font.getStringBounds(attribute, frc).getWidth());
                int attributeHeight = (int) (font.getStringBounds(attribute, frc).getHeight());
                textAreaHeight = attributeHeight + 10;

                if (attributeWidth > 290) {
                    textAreaHeight = (attributeWidth / 290) * attributeHeight + attributeHeight + 30;
                }

                textArea.setPreferredSize(new Dimension(430, textAreaHeight));
                if (attribute.equals("")){
                    textArea.setVisible(false);
                    textAreaHeight =0;
                }

                //fetching out note from object
                String note = "";
                if (obj1[4] != null && !obj1[4].equals("") ) {
                    note = "NOTE: " + (String) obj1[4];
                }

                //textArea containing note
                int notesAreaHeight;
                JLabel notes = new JLabel();
                notes.setText(note);
                notes.setFont(font);
                int noteWidth = (int) (font.getStringBounds("NOTE: " + note, frc).getWidth());
                int noteHeight = (int) (font.getStringBounds("NOTE: " + note, frc).getHeight());
                notesAreaHeight = noteHeight + 10;
                System.out.println("noteWidth = " + noteWidth + " " + "noteHeight = " + noteHeight);
                if (noteWidth > 290) {
                    notesAreaHeight = (noteWidth / 290) * noteHeight + noteHeight + 10;
                }
                notes.setPreferredSize(new Dimension(430, notesAreaHeight));
                notes.setForeground(Color.red);

                if(note.equals("")){
                    notesAreaHeight = 0;
                    notes.setVisible(false);
                }

                int panel5Height = detailAreaHeight + textAreaHeight + notesAreaHeight + 10;
                System.out.println("panel5Height = " + panel5Height);
                panel5.setPreferredSize(new Dimension(370, panel5Height));

                //panel6 - containing done buton for particular ticket line
                JPanel panel6 = new JPanel();
                panel6.setPreferredSize(new Dimension(60, 100));

                panel5.add(detail);
                panel5.add(textArea);
                panel5.add(notes);

//////////////////////////////
                boolean isFire = true;
                try {
                    isFire = dlSystem.isRefireOrderItem(obj1[0].toString());
                } catch (BasicException e) {}

                JPanel itemPanel = new JPanel(null);
                itemPanel.setLayout(new BorderLayout(0, 2));
                itemPanel.add(panel5, java.awt.BorderLayout.WEST);
                JButton refireItem = new JButton("Re-Fire");
                refireItem.setEnabled(isFire);
                refireItem.setPreferredSize(new Dimension(80, 30));
                refireItem.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        try {
                            dlSystem.incompleteOrder(obj1[0].toString());
                            refireItem.setEnabled(false);
                            m_socketClient.sendMessage("Re-Fire ticket Item");
                        } catch (BasicException e) {}
                    }
                });

                itemPanel.add(refireItem, java.awt.BorderLayout.EAST);

                panel4.setPreferredSize(new Dimension(450, panel5Height + 10));
                panel4.add(itemPanel);
                panel4.add(panel6);

                panel3.add(panel4);
                panel4Height += panel5Height + 10;
                count++;
            }

            //setting height as required to contain all ticketlines of a ticket
            panel3.setPreferredSize(new Dimension(450, panel4Height + 10 * count));

            orderPanel.setPreferredSize(new Dimension(450, 50 + panel4Height + 10 * count + 10));
            orderPanel.add(panel2);
            orderPanel.add(panel3);
            jPanel2.add(orderPanel);

        setVisible(true);

        return m_return;
    }
    
    /**
     *
     * @param 
     * @return
     */
//    public static JTicketsBagSharedList newJDialog(JTicketsBagShared ticketsbagshared) {
    public static JOrderDetail newJDialog(Window window) {
        
        JOrderDetail mydialog;
        if (window instanceof Frame) { 
            mydialog = new JOrderDetail((Frame) window, true);
        } else {
            mydialog = new JOrderDetail((Dialog) window, true);
        } 
 
        mydialog.initComponents();
        
        mydialog.jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        mydialog.jScrollPane1.getHorizontalScrollBar().setPreferredSize(new Dimension(25, 25));

        mydialog.setTitle("Order detail");
        mydialog.m_jButtonCancel.setText("Cancel");
        mydialog.m_jButtonOK3.setText("Re-Fire Ticket");
        
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
        m_jButtonOK3 = new javax.swing.JButton();
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

        m_jButtonOK3.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonOK3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        m_jButtonOK3.setText(AppLocal.getIntString("button.OK")); // NOI18N
        m_jButtonOK3.setFocusPainted(false);
        m_jButtonOK3.setFocusable(false);
        m_jButtonOK3.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonOK3.setPreferredSize(new java.awt.Dimension(140, 45));
        m_jButtonOK3.setRequestFocusEnabled(false);
        m_jButtonOK3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonOK3ActionPerformed(evt);
            }
        });
        jPanel3.add(m_jButtonOK3);

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

    private void m_jButtonOK3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonOK3ActionPerformed
        m_return = "ok";
        dispose();
    }//GEN-LAST:event_m_jButtonOK3ActionPerformed
       
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JButton m_jButtonOK3;
    private javax.swing.JPanel m_jtickets;
    // End of variables declaration//GEN-END:variables
    
}
