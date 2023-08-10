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

package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JNumberDialog;
import com.openbravo.beans.JStringDialog;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import java.awt.Component;
import java.awt.datatransfer.StringSelection;
import java.util.UUID;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author adrianromero
 */
public final class JobsView extends javax.swing.JPanel implements EditorRecord {
    
    // private DirtyManager m_Dirty = new DirtyManager();    
    private String m_sID;
    private SentenceList rolesent;
    private ComboBoxValModel rolemodel;
    private DataLogicSales dlSales;
    /** Creates new form JobsEditor
     * @param dirty */
    public JobsView(AppView app, DirtyManager dirty) {
        try{
            dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
            initComponents();
            rolesent = dlSales.getRolesList();
            rolemodel = new ComboBoxValModel();

            m_jJobName.getDocument().addDocumentListener(dirty);
            m_jRole.addActionListener(dirty);
            rolemodel = new ComboBoxValModel(rolesent.list());
            m_jRole.setModel(rolemodel);
            m_jSal.getDocument().addDocumentListener(dirty);
            writeValueEOF();    
        } catch(BasicException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
   *
   */
    @Override
    public void refresh() {

      List a;

      try {
        a = rolesent.list();
      } catch (BasicException eD) {
        MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotloadlists"), eD);
        msg.show(this);
        a = new ArrayList();
      }

      a.add(0, null);
      rolemodel = new ComboBoxValModel(a);
      m_jRole.setModel(rolemodel);
    }
    /**
     *
     */
    @Override
    public void writeValueEOF() {
        
        m_sID = null;
        rolemodel.setSelectedKey(null);
        m_jJobName.setText(null);
        m_jSal.setText(null);

        m_jJobName.setEnabled(false);
        m_jRole.setEnabled(false);
        m_jSal.setEnabled(false);
    }    

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        
        m_sID = UUID.randomUUID().toString(); 
        rolemodel.setSelectedKey(null);
        m_jJobName.setText(null);
        m_jSal.setText(null);

        m_jJobName.setEnabled(true);
        m_jRole.setEnabled(true);
        m_jSal.setEnabled(true);

    }    

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {
        
        Object[] job = (Object[]) value;
        m_sID = Formats.STRING.formatValue(job[0]);
        m_jJobName.setText(Formats.STRING.formatValue(job[1]));
        rolemodel.setSelectedKey(job[2]);
        m_jSal.setText(Formats.DOUBLE.formatValue(job[3]));

        m_jJobName.setEnabled(false);
        m_jRole.setEnabled(false);
        m_jSal.setEnabled(false);
    }    

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {
        
        Object[] job = (Object[]) value;
        m_sID = Formats.STRING.formatValue(job[0]);
        m_jJobName.setText(Formats.STRING.formatValue(job[1]));
        rolemodel.setSelectedKey(job[2]);
        m_jSal.setText(Formats.DOUBLE.formatValue(job[3]));

        m_jJobName.setEnabled(true);
        m_jRole.setEnabled(true);
        m_jSal.setEnabled(true);

    }    

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        Object[] job = new Object[4];
        job[0] = m_sID;
        job[1] = m_jJobName.getText();
        job[2] = rolemodel.getSelectedKey();

        if(m_jSal.getText().isEmpty()) job[3] = 0.00;
        else job[3] = Double.valueOf(m_jSal.getText());
        return job;
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        m_jJobName = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        m_jRole = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        m_jSal = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/info.png"))); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.locationname")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(110, 30));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        m_jJobName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jJobName.setPreferredSize(new java.awt.Dimension(220, 30));
        m_jJobName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                m_jJobNameMouseClicked(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel27.setText("Role");
        jLabel27.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jRole.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jRole.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.sal")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(110, 30));
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });

        m_jSal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSal.setPreferredSize(new java.awt.Dimension(220, 30));
        m_jSal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                m_jSalMouseClicked(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("/Hour");
        jLabel4.setPreferredSize(new java.awt.Dimension(110, 30));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(m_jSal, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_jRole, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jJobName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jJobName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jSal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked

        if (evt.getClickCount() == 2) {
            String uuidString = m_sID;
            StringSelection stringSelection = new StringSelection(uuidString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        
            JOptionPane.showMessageDialog(null, 
                AppLocal.getIntString("message.uuidcopy"));
        }
    }//GEN-LAST:event_jLabel2MouseClicked

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel3MouseClicked

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel4MouseClicked

    private void m_jJobNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_jJobNameMouseClicked
        // TODO add your handling code here:
        String jobName = JStringDialog.showEditString(this, "Name");
        if(jobName != null) m_jJobName.setText(jobName);
    }//GEN-LAST:event_m_jJobNameMouseClicked

    private void m_jSalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_jSalMouseClicked
        // TODO add your handling code here:
        Double salary = JNumberDialog.showEditNumber(this, "Salary", "Salary", null);
        if(salary != null) {
            m_jSal.setText("" + salary);
        }
    }//GEN-LAST:event_m_jSalMouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField m_jJobName;
    private javax.swing.JComboBox m_jRole;
    private javax.swing.JTextField m_jSal;
    // End of variables declaration//GEN-END:variables
    
}
