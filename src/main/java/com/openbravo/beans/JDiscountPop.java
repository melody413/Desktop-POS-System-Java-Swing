//    Roxy Pos  - Touch Friendly Point Of Sale
//    Copyright © 2009-2020 uniCenta - joint with Jacinto Rodriguez
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

package com.openbravo.beans;

import com.openbravo.basic.BasicException;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * Dec 2017
 * @author  Jack Gerarrd uniCenta
 */
@Slf4j
public class JDiscountPop extends javax.swing.JDialog {
    
    private static LocaleResources m_resources;
    
    private Integer m_value;
    
    /** Creates new form JNumberDialog
     * @param parent
     * @param modal */
    public JDiscountPop(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        init();
    }
    
    /** Creates new form JNumberDialog
     * @param parent
     * @param modal */
    public JDiscountPop(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        init();
    }
    
    private void init() {
        
        if (m_resources == null) {
            m_resources = new LocaleResources();
            m_resources.addBundleName("beans_messages");
        }
        
        initComponents();        
        //m_jnumber.setVisible(false);
        
        m_jPanelTitle.setBorder(RoundedBorder.createGradientBorder());

        m_value = null;
    }
    
    private void setTitle(String title, String message, Icon icon) {
        setTitle(title);
        m_lblMessage.setText(message);
        m_lblMessage.setIcon(icon);
    }
    
    public static Integer showEditNumber(Component parent, String title) {
        return showEditNumber(parent, title, null, null);
    }
    public static Integer showEditNumber(Component parent, String title, String role) {
        return showEditNumber(parent, title, role, null);
    }
    public static Integer showEditNumber(Component parent, String title, String role, Icon icon) {
        
        Window window = SwingUtilities.windowForComponent(parent);
        
        JDiscountPop myMsg;
        if (window instanceof Frame) { 
            myMsg = new JDiscountPop((Frame) window, true);
        } else {
            myMsg = new JDiscountPop((Dialog) window, true);
        }
        
        if(!role.equals("0"))
            myMsg.j_mManager.setEnabled(false);
        myMsg.setTitle(title, role, icon);
        myMsg.setVisible(true);
        return myMsg.m_value;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanelGrid = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        j_mNone = new javax.swing.JButton();
        j_mEmployee = new javax.swing.JButton();
        j_mPolice = new javax.swing.JButton();
        j_mITB = new javax.swing.JButton();
        j_mManager = new javax.swing.JButton();
        m_jPanelTitle = new javax.swing.JPanel();
        m_lblMessage = new javax.swing.JLabel();

        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setLayout(new java.awt.BorderLayout());
        jPanel3.add(jPanel4);

        jPanelGrid.add(jPanel3);

        j_mNone.setText("None (0%)");
        j_mNone.setMaximumSize(new java.awt.Dimension(32767, 32767));
        j_mNone.setMinimumSize(new java.awt.Dimension(84, 23));
        j_mNone.setPreferredSize(new java.awt.Dimension(300, 40));
        j_mNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mNoneActionPerformed(evt);
            }
        });
        jPanelGrid.add(j_mNone);

        j_mEmployee.setText("Employee (30% of the entire tab)");
        j_mEmployee.setMaximumSize(new java.awt.Dimension(32767, 32767));
        j_mEmployee.setMinimumSize(new java.awt.Dimension(84, 23));
        j_mEmployee.setPreferredSize(new java.awt.Dimension(300, 40));
        j_mEmployee.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mEmployeeActionPerformed(evt);
            }
        });
        jPanelGrid.add(j_mEmployee);

        j_mPolice.setText("Police (30% of items in food categories.)");
        j_mPolice.setMaximumSize(new java.awt.Dimension(32767, 32767));
        j_mPolice.setMinimumSize(new java.awt.Dimension(84, 23));
        j_mPolice.setPreferredSize(new java.awt.Dimension(300, 40));
        j_mPolice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mPoliceActionPerformed(evt);
            }
        });
        jPanelGrid.add(j_mPolice);

        j_mITB.setText("ITB (20% of alcohol categories.)");
        j_mITB.setMaximumSize(new java.awt.Dimension(32767, 32767));
        j_mITB.setMinimumSize(new java.awt.Dimension(84, 23));
        j_mITB.setPreferredSize(new java.awt.Dimension(300, 40));
        j_mITB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mITBActionPerformed(evt);
            }
        });
        jPanelGrid.add(j_mITB);

        j_mManager.setText("Manager (100% of the entire tab)");
        j_mManager.setMaximumSize(new java.awt.Dimension(32767, 32767));
        j_mManager.setMinimumSize(new java.awt.Dimension(84, 23));
        j_mManager.setPreferredSize(new java.awt.Dimension(300, 40));
        j_mManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_mManagerActionPerformed(evt);
            }
        });
        jPanelGrid.add(j_mManager);

        jPanel2.add(jPanelGrid, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        m_jPanelTitle.setLayout(new java.awt.BorderLayout());

        m_lblMessage.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPanelTitle.add(m_lblMessage, java.awt.BorderLayout.CENTER);

        getContentPane().add(m_jPanelTitle, java.awt.BorderLayout.NORTH);

        setSize(new java.awt.Dimension(328, 313));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        setVisible(false);
        dispose();
        
    }//GEN-LAST:event_formWindowClosing

    private void j_mEmployeeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mEmployeeActionPerformed
        m_value = 1;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_j_mEmployeeActionPerformed

    private void j_mPoliceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mPoliceActionPerformed
        // TODO add your handling code here:
        m_value = 2;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_j_mPoliceActionPerformed

    private void j_mITBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mITBActionPerformed
        // TODO add your handling code here:
        m_value = 3;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_j_mITBActionPerformed

    private void j_mManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mManagerActionPerformed
        // TODO add your handling code here:
        m_value = 4;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_j_mManagerActionPerformed

    private void j_mNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_mNoneActionPerformed
        // TODO add your handling code here:
        m_value = 0;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_j_mNoneActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelGrid;
    private javax.swing.JButton j_mEmployee;
    private javax.swing.JButton j_mITB;
    private javax.swing.JButton j_mManager;
    private javax.swing.JButton j_mNone;
    private javax.swing.JButton j_mPolice;
    private javax.swing.JPanel m_jPanelTitle;
    private javax.swing.JLabel m_lblMessage;
    // End of variables declaration//GEN-END:variables
    
}
