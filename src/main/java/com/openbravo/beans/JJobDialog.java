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

package com.openbravo.beans;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

/**
 *
 * @author  adrian
 */
public class JJobDialog extends javax.swing.JDialog {
    
    private static LocaleResources m_resources;
    private SentenceList jobsent;
    private ComboBoxValModel jobmodel;
    private DataLogicSales dlSales;
    private String m_value;
    private DataLogicSystem m_dlSystem;
    private String m_peopleId;
    /** Creates new form JNumberDialog
     * @param parent
     * @param modal */
    public JJobDialog(java.awt.Frame parent, boolean modal, DataLogicSystem dlSystem, String peopleId) {
        super(parent, modal);
        m_dlSystem = dlSystem;
        m_peopleId = peopleId;
        init();
    }
    
    /** Creates new form JNumberDialog
     * @param parent
     * @param modal */
    public JJobDialog(java.awt.Dialog parent, boolean modal, DataLogicSystem dlSystem, String peopleId) {
        super(parent, modal);
        m_dlSystem = dlSystem;
        m_peopleId = peopleId;
        init();
    }

    public JJobDialog() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void init() {
        if (m_resources == null) {
            m_resources = new LocaleResources();
            m_resources.addBundleName("beans_messages");
        }
        
        initComponents();        
        jobsent = m_dlSystem.getJobsList();
        jobmodel = new ComboBoxValModel();
        try{
            jobmodel = new ComboBoxValModel(jobsent.list(m_peopleId));
        } catch(BasicException ex) {
            System.out.println(ex.getMessage());
        }
        m_jJob.setModel(jobmodel);
        getRootPane().setDefaultButton(jcmdOK);   

        jobmodel.setSelectedFirst();
        
        m_jPanelTitle.setBorder(RoundedBorder.createGradientBorder());

        m_value = null;
    }
    
    private void setTitle(String title, String message, Icon icon) {
        setTitle(title);
        m_lblMessage.setText(message);
        m_lblMessage.setIcon(icon);
    }
    private void setValue(String sValue){
       jobmodel.setSelectedKey(sValue);
    }
    public static String showSelectJob(DataLogicSystem dlSystem, String peopleId, Component parent, String title) {
        return showSelectJob(dlSystem, peopleId, parent, title, null, null);
    }
    /**
     *
     * @param parent
     * @param title
     * @param message
     * @return
     */
    public static String showSelectJob(DataLogicSystem dlSystem, String peopleId, Component parent, String title, String message) {
        return showSelectJob(dlSystem, peopleId, parent, title, message, null);
    }

    /**
     *
     * @param parent
     * @param title
     * @param message
     * @param icon
     * @return
     */
    public static String showSelectJob(DataLogicSystem dlSystem, String peopleId, Component parent, String title, String message, Icon icon) {
        
        return showSelectJob(dlSystem, peopleId, parent, title, message, icon, null);
    }

    public static String showSelectJob(DataLogicSystem dlSystem, String peopleId, Component parent, String title, String message, Icon icon, String sValue) {

            Window window = SwingUtilities.windowForComponent(parent);

            JJobDialog myMsg;
            if (window instanceof Frame) { 
                myMsg = new JJobDialog((Frame) window, true, dlSystem, peopleId);
            } else {
                myMsg = new JJobDialog((Dialog) window, true, dlSystem, peopleId);
            }
            myMsg.setTitle(title, message, icon);
            myMsg.setValue(sValue);
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

        jPanel1 = new javax.swing.JPanel();
        jcmdCancel = new javax.swing.JButton();
        jcmdOK = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanelGrid = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jJob = new javax.swing.JComboBox();
        m_jPanelTitle = new javax.swing.JPanel();
        m_lblMessage = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(900, 429));
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jcmdCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        jcmdCancel.setText(m_resources.getString("button.cancel")); // NOI18N
        jcmdCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdCancel);

        jcmdOK.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jcmdOK.setText(m_resources.getString("button.ok")); // NOI18N
        jcmdOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdOK);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanelGrid.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanelGrid.setPreferredSize(new java.awt.Dimension(300, 320));
        jPanelGrid.setLayout(new java.awt.BorderLayout());

        jPanel3.setPreferredSize(new java.awt.Dimension(300, 350));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(110, 30));
        jPanel4.setPreferredSize(new java.awt.Dimension(142, 30));
        jPanel4.setLayout(new java.awt.BorderLayout());

        m_jJob.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jJob.setPreferredSize(new java.awt.Dimension(200, 30));
        jPanel4.add(m_jJob, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel4, java.awt.BorderLayout.PAGE_START);

        jPanelGrid.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        jPanel2.add(jPanelGrid, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        m_jPanelTitle.setLayout(new java.awt.BorderLayout());

        m_lblMessage.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_lblMessage.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.darkGray), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        m_jPanelTitle.add(m_lblMessage, java.awt.BorderLayout.CENTER);

        getContentPane().add(m_jPanelTitle, java.awt.BorderLayout.PAGE_START);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed


        m_value = String.valueOf(jobmodel.getSelectedKey());
        setVisible(false);
        dispose();

        
    }//GEN-LAST:event_jcmdOKActionPerformed

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed

        setVisible(false);
        dispose();
        
    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        setVisible(false);
        dispose();
        
    }//GEN-LAST:event_formWindowClosing
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelGrid;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    private javax.swing.JComboBox m_jJob;
    private javax.swing.JPanel m_jPanelTitle;
    private javax.swing.JLabel m_lblMessage;
    // End of variables declaration//GEN-END:variables
    
}
