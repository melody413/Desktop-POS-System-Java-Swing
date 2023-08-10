//    Roxy Pos  - Touch Friendly Point Of Sale
//    Copyright © 2009-2020 uniCenta and part works Openbravo, S.L.
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

package com.openbravo.pos.payment;

import com.google.gson.JsonObject;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.util.RoundUtils;
import com.openbravo.pos.voucher.VoucherInfo;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * @author JG uniCenta
 */
@Slf4j
public class JPaymentVoucher extends javax.swing.JPanel implements JPaymentInterface {

  private final JPaymentNotifier m_notifier;

  private DataLogicSales dlSales;
  private DataLogicCustomers dlCustomers;
  private ComboBoxValModel m_VoucherModel;
  private SentenceList m_sentvouch;
  private VoucherInfo m_voucherInfo;

    private JsonObject userData;

  private double m_dTicket;
  private double m_dTotal;

  private final String m_sVoucher; // "voucherin", "voucherout"
  private String m_sVoucher1;

  /**
   * Creates new form JPaymentTicket
   *
   * @param app
   * @param notifier
   * @param sVoucher
   */
  public JPaymentVoucher(AppView app, JPaymentNotifier notifier, String sVoucher) {

    m_notifier = notifier;
    m_sVoucher = sVoucher;
    m_dTotal = 0.0;

    init(app);

    m_jTendered.addPropertyChangeListener("Edition", new RecalculateState());

  }

  private void init(AppView app) {

    dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
    dlCustomers = (DataLogicCustomers) app.getBean("com.openbravo.pos.customers.DataLogicCustomers");
    initComponents();

    webLblcustomerName.setText(null);
  }

  /**
   * @param customerext
   * @param dTotal
   * @param transID
   */
  @Override
  public void activate(CustomerInfoExt customerext, double dTotal, String transID) {

    m_dTotal = dTotal;

    m_jTendered.reset();

    m_jKeys.setEnabled(false);
    m_jTendered.setEnabled(false);
    m_VoucherModel = new ComboBoxValModel();
    m_sentvouch = dlSales.getVoucherList();
    List a = null;
    try {
      a = m_sentvouch.list();
    } catch (BasicException ex) {
      log.error(ex.getMessage());
    }
    removeAlreadyAddedVoucher(this.m_sVoucher1, a);
    m_VoucherModel = new ComboBoxValModel(a);
    m_jVoucher.setModel(m_VoucherModel);

    printState();
    this.m_sVoucher1 = null;
  }

  private void removeAlreadyAddedVoucher(String existingVoucherNumber, List<VoucherInfo> vouchers) {
    if (vouchers != null && existingVoucherNumber != null) {
      vouchers.removeIf(voucherInfo -> voucherInfo.getVoucherNumber().equals(existingVoucherNumber));
    }
  }

  /**
   * @return
   */
  @Override
  public Component getComponent() {
    return this;
  }

    @Override
    public void setUserData(JsonObject m_userData) {
        userData = m_userData;
    }

  /**
   * @return
   */
  @Override
  public PaymentInfo executePayment() {

    try {
      String id = m_VoucherModel.getSelectedKey().toString();
      VoucherInfo m_voucherInfo1 = dlCustomers.getVoucherInfo(id);
      m_sVoucher1 = m_voucherInfo1.getVoucherNumber();
    } catch (BasicException ex) {
      log.error(ex.getMessage());
    }
    return new PaymentInfoTicket(m_dTicket, m_sVoucher, m_sVoucher1);

  }

  public void confirmVoucher() {
    List a = null;
    try {
      a = m_sentvouch.list();

// a.forEach(System.out::println); 
      a.remove(m_jVoucher.getSelectedIndex());
    } catch (BasicException ex) {
      log.error(ex.getMessage());
    }

    m_VoucherModel = new ComboBoxValModel(a);
    m_jVoucher.setModel(m_VoucherModel);
  }

  private void printState() {

    Double value = m_jTendered.getDoubleValue();
    if (value == null) {
      m_dTicket = 0.0;
    } else {
      m_dTicket = value;
    }

    m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(m_dTicket));

    int iCompare = RoundUtils.compare(m_dTicket, m_dTotal);

    m_notifier.setStatus(m_dTicket > 0.0, iCompare >= 0);

  }

  private class RecalculateState implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      printState();
    }
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        m_jVoucher = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        m_jMoneyEuros = new javax.swing.JLabel();
        webLblCustomer = new com.alee.laf.label.WebLabel();
        webLblcustomerName = new com.alee.laf.label.WebLabel();
        voucherStatus = new com.alee.laf.label.WebLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel1 = new javax.swing.JPanel();
        m_jTendered = new com.openbravo.editor.JEditorCurrencyPositive();

        setLayout(new java.awt.BorderLayout());

        jLabel5.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel5.setLabelFor(m_jVoucher);
        jLabel5.setText(AppLocal.getIntString("label.voucher")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jVoucher.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jVoucher.setPreferredSize(new java.awt.Dimension(180, 30));
        m_jVoucher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jVoucherActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.voucherValue")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jMoneyEuros.setBackground(new java.awt.Color(204, 255, 51));
        m_jMoneyEuros.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        m_jMoneyEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyEuros.setOpaque(true);
        m_jMoneyEuros.setPreferredSize(new java.awt.Dimension(180, 30));

        webLblCustomer.setText(AppLocal.getIntString("label.customer")); // NOI18N
        webLblCustomer.setToolTipText("");
        webLblCustomer.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        webLblCustomer.setPreferredSize(new java.awt.Dimension(100, 30));

        webLblcustomerName.setText(AppLocal.getIntString("label.customer")); // NOI18N
        webLblcustomerName.setToolTipText("");
        webLblcustomerName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        webLblcustomerName.setPreferredSize(new java.awt.Dimension(100, 30));

        voucherStatus.setText(AppLocal.getIntString("label.voucherStatus")); // NOI18N
        voucherStatus.setToolTipText("");
        voucherStatus.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        voucherStatus.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel6.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel6.setLabelFor(m_jVoucher);
        jLabel6.setText(AppLocal.getIntString("label.voucherStatus")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 30));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_jMoneyEuros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_jVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(webLblCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(webLblcustomerName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(voucherStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(voucherStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jMoneyEuros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(webLblCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(webLblcustomerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(240, 240, 240))
        );

        add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel11.setLayout(new java.awt.BorderLayout());

        jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.Y_AXIS));
        jPanel12.add(m_jKeys);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());

        m_jTendered.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jTendered.setPreferredSize(new java.awt.Dimension(130, 30));
        jPanel1.add(m_jTendered, java.awt.BorderLayout.CENTER);

        jPanel12.add(jPanel1);

        jPanel11.add(jPanel12, java.awt.BorderLayout.NORTH);

        add(jPanel11, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

  private void m_jVoucherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jVoucherActionPerformed

    m_jMoneyEuros.setText(null);

    if (m_VoucherModel.getSelectedKey() != null) {
      try {
        String id = m_VoucherModel.getSelectedKey().toString();
        VoucherInfo m_voucherInfo = dlCustomers.getVoucherInfo(id);

        if (m_voucherInfo != null) {
          m_jTendered.setDoubleValue(m_voucherInfo.getAmount());
          m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(m_voucherInfo.getAmount()));
          webLblcustomerName.setText(m_voucherInfo.getCustomerName());

          if ("A".equals(m_voucherInfo.getStatus())) {
            voucherStatus.setText("Available");
          } else if ("D".equals(m_voucherInfo.getStatus())) {
            voucherStatus.setText("Redeemed");
          }

          printState();

        } else {
          voucherStatus.setText("Error");
          webLblcustomerName.setText("");
        }
      } catch (BasicException ex) {
        log.error(ex.getMessage());
      }

    }
  }//GEN-LAST:event_m_jVoucherActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel4;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JLabel m_jMoneyEuros;
    private com.openbravo.editor.JEditorCurrencyPositive m_jTendered;
    private javax.swing.JComboBox m_jVoucher;
    private com.alee.laf.label.WebLabel voucherStatus;
    private com.alee.laf.label.WebLabel webLblCustomer;
    private com.alee.laf.label.WebLabel webLblcustomerName;
    // End of variables declaration//GEN-END:variables

}
