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

package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.editor.JEditorAbstract;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.UUID;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;


/**
 *
 * @author adrianromero
 */
public final class PaymentsEditor extends javax.swing.JPanel implements EditorRecord {
    
    private ComboBoxValModel m_ReasonModel;
    
    private String m_sId;
    private String m_sPaymentId;
    private Date datenew;
   
    private final AppView m_App;
    private String m_sNotes;
    private DataLogicSystem dlSystem; 
 
    PaymentReasonPositive cashInItem;
    PaymentReasonNegative cashOutItem;
    
    public class MyPropertyChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {}
    }

    /** Creates new form JPanelPayments
     * @param oApp
     * @param dirty */
    public PaymentsEditor(AppView oApp, DirtyManager dirty) {
        
        m_App = oApp;
        
        initComponents();
        m_ReasonModel = new ComboBoxValModel();
        
        jAmount1.addEditorKeys(m_jKeys);
        jAmount5.addEditorKeys(m_jKeys);
        jAmount10.addEditorKeys(m_jKeys);
        jAmount20.addEditorKeys(m_jKeys);
        jAmount50.addEditorKeys(m_jKeys);
        jAmount100.addEditorKeys(m_jKeys);

        jAmount1.addPropertyChangeListener("Text", dirty);
        jAmount5.addPropertyChangeListener("Text", dirty);
        jAmount10.addPropertyChangeListener("Text", dirty);
        jAmount20.addPropertyChangeListener("Text", dirty);
        jAmount50.addPropertyChangeListener("Text", dirty);
        jAmount100.addPropertyChangeListener("Text", dirty);

        jAmount1.addPropertyChangeListener(new MyPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateAllTotal();
            }
        });

        jAmount5.addPropertyChangeListener(new MyPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateAllTotal();
            }
        });

        jAmount10.addPropertyChangeListener(new MyPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateAllTotal();
            }
        });

        jAmount20.addPropertyChangeListener(new MyPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateAllTotal();
            }
        });

        jAmount50.addPropertyChangeListener(new MyPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateAllTotal();
            }
        });

        jAmount100.addPropertyChangeListener(new MyPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateAllTotal();
            }
        });

        m_jreason.addActionListener(dirty);

        dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");

        cashInItem = new PaymentReasonPositive("cashin", AppLocal.getIntString("transpayment.cashin"));
        cashOutItem = new PaymentReasonNegative("cashout", AppLocal.getIntString("transpayment.cashout"));

        jLabelStatic1.setText("X1");
        jLabelStatic5.setText("X5");
        jLabelStatic10.setText("X10");
        jLabelStatic20.setText("X20");
        jLabelStatic50.setText("X50");
        jLabelStatic100.setText("X100");

        jTotal1.setText("0");
        jTotal5.setText("0");
        jTotal10.setText("0");
        jTotal20.setText("0");
        jTotal50.setText("0");
        jTotal100.setText("0");

        jTotalTotal.setText("0");
        jLabelStaticTotal.setText("Total: ");

        writeValueEOF();
    }

    private void updateAllTotal() {
        jTotal1.setText(String.valueOf(Integer.parseInt("".equals(jAmount1.getText()) ? "0" : jAmount1.getText()) * 1));
        jTotal5.setText(String.valueOf(Integer.parseInt("".equals(jAmount5.getText()) ? "0" : jAmount5.getText()) * 5));
        jTotal10.setText(String.valueOf(Integer.parseInt("".equals(jAmount10.getText()) ? "0" : jAmount10.getText()) * 10));
        jTotal20.setText(String.valueOf(Integer.parseInt("".equals(jAmount20.getText()) ? "0" : jAmount20.getText()) * 20));
        jTotal50.setText(String.valueOf(Integer.parseInt("".equals(jAmount50.getText()) ? "0" : jAmount50.getText()) * 50));
        jTotal100.setText(String.valueOf(Integer.parseInt("".equals(jAmount100.getText()) ? "0" : jAmount100.getText()) * 100));

        jTotalTotal.setText(Formats.CURRENCY.formatValue(Integer.parseInt("".equals(jAmount1.getText()) ? "0" : jAmount1.getText()) * 1
                        + Integer.parseInt("".equals(jAmount5.getText()) ? "0" : jAmount5.getText()) * 5
                        + Integer.parseInt("".equals(jAmount10.getText()) ? "0" : jAmount10.getText()) * 10
                        + Integer.parseInt("".equals(jAmount20.getText()) ? "0" : jAmount20.getText()) * 20
                        + Integer.parseInt("".equals(jAmount50.getText()) ? "0" : jAmount50.getText()) * 50
                        + (Integer.parseInt("".equals(jAmount100.getText()) ? "0" : jAmount100.getText()) * 100 )));
    }
    
    /**
     *
     */
    @Override
    public void writeValueEOF() {
        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        setReasonTotal(null, null);
        m_jreason.setEnabled(false);

        jAmount1.setEnabled(false);
        jAmount5.setEnabled(false);
        jAmount10.setEnabled(false);
        jAmount20.setEnabled(false);
        jAmount50.setEnabled(false);
        jAmount100.setEnabled(false);

    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        m_ReasonModel = new ComboBoxValModel();

        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        m_jreason.setEnabled(true);

        jAmount1.setEnabled(true);
        jAmount1.setValueInteger(0);
        jAmount1.activate();

        jAmount5.setEnabled(true);
        jAmount5.setValueInteger(0);
        jAmount5.activate();

        jAmount10.setEnabled(true);
        jAmount10.setValueInteger(0);
        jAmount10.activate();

        jAmount20.setEnabled(true);
        jAmount20.setValueInteger(0);
        jAmount20.activate();

        jAmount50.setEnabled(true);
        jAmount50.setValueInteger(0);
        jAmount50.activate();

        jAmount100.setEnabled(true);
        jAmount100.setValueInteger(0);
        jAmount100.activate();

        try {
            String whichCash = dlSystem.getWhichCashFromOpen();
            
            if(whichCash == null || whichCash.equals("cashout")) {
                m_ReasonModel.add(cashInItem);
                m_jreason.setModel(m_ReasonModel);
                setReasonTotal("cashin", null);
            } else {    
                m_ReasonModel.add(cashOutItem);
                m_jreason.setModel(m_ReasonModel);
                setReasonTotal("cashout", null);
            }
        } catch (BasicException e) {}
    }
    
    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {
        Object[] payment = (Object[]) value;
        m_sId = (String) payment[0];
        datenew = (Date) payment[2];
        m_sPaymentId = (String) payment[3];
        setReasonTotal(payment[4], payment[5]);
        m_jreason.setEnabled(false);

        jAmount1.setEnabled(false);
        jAmount5.setEnabled(false);
        jAmount10.setEnabled(false);
        jAmount20.setEnabled(false);
        jAmount50.setEnabled(false);
        jAmount100.setEnabled(false);
    }
    
    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {
        Object[] payment = (Object[]) value;
        m_sId = (String) payment[0];
        datenew = (Date) payment[2];
        m_sPaymentId = (String) payment[3];
        setReasonTotal(payment[4], payment[5]);
        m_jreason.setEnabled(false);

        jAmount1.setEnabled(false);
        jAmount1.activate();
        jAmount5.setEnabled(false);
        jAmount5.activate();
        jAmount10.setEnabled(false);
        jAmount10.activate();
        jAmount20.setEnabled(false);
        jAmount20.activate();
        jAmount50.setEnabled(false);
        jAmount50.activate();
        jAmount100.setEnabled(false);
        jAmount100.activate();
    }
    
    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        Object[] payment = new Object[17];
        payment[0] = m_sId == null ? UUID.randomUUID().toString() : m_sId;
        payment[1] = m_App.getActiveCashIndex();
        payment[2] = datenew == null ? new Date() : datenew;
        payment[3] = m_sPaymentId == null ? UUID.randomUUID().toString() : m_sPaymentId;
        payment[4] = m_ReasonModel.getSelectedKey();

        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();
        String cashType = m_ReasonModel.getSelectedKey().toString();

        Integer dtotal1 = Integer.parseInt("".equals(jTotal1.getText()) ? "0" : jTotal1.getText());
        Integer dtotal5 = Integer.parseInt("".equals(jTotal5.getText()) ? "0" : jTotal5.getText());
        Integer dtotal10 = Integer.parseInt("".equals(jTotal10.getText()) ? "0" : jTotal10.getText());
        Integer dtotal20 = Integer.parseInt("".equals(jTotal20.getText()) ? "0" : jTotal20.getText());
        Integer dtotal50 = Integer.parseInt("".equals(jTotal50.getText()) ? "0" : jTotal50.getText());
        Integer dtotal100 = Integer.parseInt("".equals(jTotal100.getText()) ? "0" : jTotal100.getText());

        payment[5] = "cashin".equals(cashType) ? dtotal1 : reason.addSignum(dtotal1);
        payment[6] = "cashin".equals(cashType) ? dtotal5 : reason.addSignum(dtotal5);
        payment[7] = "cashin".equals(cashType) ? dtotal10 : reason.addSignum(dtotal10);
        payment[8] = "cashin".equals(cashType) ? dtotal20 : reason.addSignum(dtotal20);
        payment[9] = "cashin".equals(cashType) ? dtotal50 : reason.addSignum(dtotal50);
        payment[10] = "cashin".equals(cashType) ? dtotal100 : reason.addSignum(dtotal100);

        payment[11] = "1";
        payment[12] = "5";
        payment[13] = "10";
        payment[14] = "20";
        payment[15] = "50";
        payment[16] = "100";

        return payment;
    }
    
    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
    
    /**
     *
     */
    @Override
    public void refresh() {
//        m_ReasonModel = new ComboBoxValModel();
//
//        m_ReasonModel.add(cashInItem);
//        m_ReasonModel.add(cashOutItem);
//        m_jreason.setModel(m_ReasonModel);
    }  
    
    private void setReasonTotal(Object reasonfield, Object totalfield) {
        m_ReasonModel.setSelectedKey(reasonfield);
             
        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();
    }
    
    private static abstract class PaymentReason implements IKeyed {
        private String m_sKey;
        private String m_sText;
        
        public PaymentReason(String key, String text) {
            m_sKey = key;
            m_sText = text;
        }
        @Override
        public Object getKey() {
            return m_sKey;
        }
        public abstract Double positivize(Double d);
        public abstract Double addSignum(Double d);

        public abstract Integer addSignum(Integer d);
        
        @Override
        public String toString() {
            return m_sText;
        }
    }
    private static class PaymentReasonPositive extends PaymentReason {
        public PaymentReasonPositive(String key, String text) {
            super(key, text);
        }
        @Override
        public Double positivize(Double d) {
            return d;
        }
        @Override
        public Double addSignum(Double d) {
            if (d == null) {
                return null;
            } else if (d.doubleValue() < 0.0) {
                return new Double(-d.doubleValue());
            } else {
                return d;
            }
        }

        @Override
        public Integer addSignum(Integer d) {
            if (d == null) {
                return null;
            } else if (d.intValue() > 0) {
                return 0 - d.intValue();
            } else {
                return d;
            }
        }
    }
    private static class PaymentReasonNegative extends PaymentReason {
        public PaymentReasonNegative(String key, String text) {
            super(key, text);
        }
        @Override
        public Double positivize(Double d) {
            return d == null ? null : new Double(-d.doubleValue());
        }
        @Override
        public Double addSignum(Double d) {
            if (d == null) {
                return null;
            } else if (d.doubleValue() > 0.0) {
                return new Double(-d.doubleValue());
            } else {
                return d;
            }
        }

        @Override
        public Integer addSignum(Integer d) {
            if (d == null) {
                return null;
            } else if (d.intValue() > 0) {
                return 0 - d.intValue();
            } else {
                return d;
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

        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        m_jreason = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabelStatic1 = new javax.swing.JLabel();
        jTotal1 = new javax.swing.JLabel();
        m_jQtyPlus1 = new javax.swing.JButton();
        m_jQtyMinus1 = new javax.swing.JButton();
        m_jQtyPlus5 = new javax.swing.JButton();
        m_jQtyMinus5 = new javax.swing.JButton();
        jLabelStatic5 = new javax.swing.JLabel();
        jTotal5 = new javax.swing.JLabel();
        m_jQtyPlus10 = new javax.swing.JButton();
        m_jQtyMinus10 = new javax.swing.JButton();
        jLabelStatic10 = new javax.swing.JLabel();
        jTotal10 = new javax.swing.JLabel();
        m_jQtyPlus20 = new javax.swing.JButton();
        m_jQtyMinus20 = new javax.swing.JButton();
        jLabelStatic20 = new javax.swing.JLabel();
        jTotal20 = new javax.swing.JLabel();
        m_jQtyPlus50 = new javax.swing.JButton();
        m_jQtyMinus50 = new javax.swing.JButton();
        jLabelStatic50 = new javax.swing.JLabel();
        jTotal50 = new javax.swing.JLabel();
        m_jQtyPlus100 = new javax.swing.JButton();
        m_jQtyMinus100 = new javax.swing.JButton();
        jLabelStatic100 = new javax.swing.JLabel();
        jTotalTotal = new javax.swing.JLabel();
        jTotal100 = new javax.swing.JLabel();
        jLabelStaticTotal = new javax.swing.JLabel();
        jAmount1 = new com.openbravo.editor.JEditorIntegerPositive();
        jAmount5 = new com.openbravo.editor.JEditorIntegerPositive();
        jAmount10 = new com.openbravo.editor.JEditorIntegerPositive();
        jAmount20 = new com.openbravo.editor.JEditorIntegerPositive();
        jAmount50 = new com.openbravo.editor.JEditorIntegerPositive();
        jAmount100 = new com.openbravo.editor.JEditorIntegerPositive();
        jPanel2 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();

        setLayout(new java.awt.BorderLayout());

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.paymentreason")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jreason.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jreason.setFocusable(false);
        m_jreason.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(110, 30));

        jLabelStatic1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelStatic1.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabelStatic1.setPreferredSize(new java.awt.Dimension(110, 30));

        jTotal1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTotal1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jTotal1.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jTotal1.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jQtyPlus1.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyPlus1.setText("+");
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        m_jQtyPlus1.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyPlus1.setFocusPainted(false);
        m_jQtyPlus1.setFocusable(false);
        m_jQtyPlus1.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyPlus1.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus1.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus1.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyPlus1.setRequestFocusEnabled(false);
        m_jQtyPlus1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyPlus1ActionPerformed(evt);
            }
        });

        m_jQtyMinus1.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyMinus1.setText("-");
        m_jQtyMinus1.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyMinus1.setFocusPainted(false);
        m_jQtyMinus1.setFocusable(false);
        m_jQtyMinus1.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyMinus1.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus1.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus1.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyMinus1.setRequestFocusEnabled(false);
        m_jQtyMinus1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyMinus1ActionPerformed(evt);
            }
        });

        m_jQtyPlus5.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyPlus5.setText("+");
        m_jQtyPlus5.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyPlus5.setFocusPainted(false);
        m_jQtyPlus5.setFocusable(false);
        m_jQtyPlus5.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyPlus5.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus5.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus5.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyPlus5.setRequestFocusEnabled(false);
        m_jQtyPlus5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyPlus5ActionPerformed(evt);
            }
        });

        m_jQtyMinus5.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyMinus5.setText("-");
        m_jQtyMinus5.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyMinus5.setFocusPainted(false);
        m_jQtyMinus5.setFocusable(false);
        m_jQtyMinus5.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyMinus5.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus5.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus5.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyMinus5.setRequestFocusEnabled(false);
        m_jQtyMinus5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyMinus5ActionPerformed(evt);
            }
        });

        jLabelStatic5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelStatic5.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabelStatic5.setPreferredSize(new java.awt.Dimension(110, 30));

        jTotal5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTotal5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jTotal5.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jTotal5.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jQtyPlus10.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyPlus10.setText("+");
        m_jQtyPlus10.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyPlus10.setFocusPainted(false);
        m_jQtyPlus10.setFocusable(false);
        m_jQtyPlus10.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyPlus10.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus10.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus10.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyPlus10.setRequestFocusEnabled(false);
        m_jQtyPlus10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyPlus10ActionPerformed(evt);
            }
        });

        m_jQtyMinus10.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyMinus10.setText("-");
        m_jQtyMinus10.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyMinus10.setFocusPainted(false);
        m_jQtyMinus10.setFocusable(false);
        m_jQtyMinus10.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyMinus10.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus10.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus10.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyMinus10.setRequestFocusEnabled(false);
        m_jQtyMinus10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyMinus10ActionPerformed(evt);
            }
        });

        jLabelStatic10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelStatic10.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabelStatic10.setPreferredSize(new java.awt.Dimension(110, 30));

        jTotal10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTotal10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jTotal10.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jTotal10.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jQtyPlus20.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyPlus20.setText("+");
        m_jQtyPlus20.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyPlus20.setFocusPainted(false);
        m_jQtyPlus20.setFocusable(false);
        m_jQtyPlus20.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyPlus20.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus20.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus20.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyPlus20.setRequestFocusEnabled(false);
        m_jQtyPlus20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyPlus20ActionPerformed(evt);
            }
        });

        m_jQtyMinus20.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyMinus20.setText("-");
        m_jQtyMinus20.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyMinus20.setFocusPainted(false);
        m_jQtyMinus20.setFocusable(false);
        m_jQtyMinus20.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyMinus20.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus20.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus20.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyMinus20.setRequestFocusEnabled(false);
        m_jQtyMinus20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyMinus20ActionPerformed(evt);
            }
        });

        jLabelStatic20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelStatic20.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabelStatic20.setPreferredSize(new java.awt.Dimension(110, 30));

        jTotal20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTotal20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jTotal20.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jTotal20.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jQtyPlus50.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyPlus50.setText("+");
        m_jQtyPlus50.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyPlus50.setFocusPainted(false);
        m_jQtyPlus50.setFocusable(false);
        m_jQtyPlus50.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyPlus50.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus50.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus50.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyPlus50.setRequestFocusEnabled(false);
        m_jQtyPlus50.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyPlus50ActionPerformed(evt);
            }
        });

        m_jQtyMinus50.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyMinus50.setText("-");
        m_jQtyMinus50.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyMinus50.setFocusPainted(false);
        m_jQtyMinus50.setFocusable(false);
        m_jQtyMinus50.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyMinus50.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus50.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus50.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyMinus50.setRequestFocusEnabled(false);
        m_jQtyMinus50.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyMinus50ActionPerformed(evt);
            }
        });

        jLabelStatic50.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelStatic50.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabelStatic50.setPreferredSize(new java.awt.Dimension(110, 30));

        jTotal50.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTotal50.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jTotal50.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jTotal50.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jQtyPlus100.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyPlus100.setText("+");
        m_jQtyPlus100.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyPlus100.setFocusPainted(false);
        m_jQtyPlus100.setFocusable(false);
        m_jQtyPlus100.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyPlus100.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus100.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyPlus100.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyPlus100.setRequestFocusEnabled(false);
        m_jQtyPlus100.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyPlus100ActionPerformed(evt);
            }
        });

        m_jQtyMinus100.setBackground(new java.awt.Color(2, 2, 4));
        m_jQtyMinus100.setText("-");
        m_jQtyMinus100.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jQtyMinus100.setFocusPainted(false);
        m_jQtyMinus100.setFocusable(false);
        m_jQtyMinus100.setMargin(new java.awt.Insets(2, 6, 2, 6));
        m_jQtyMinus100.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus100.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jQtyMinus100.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jQtyMinus100.setRequestFocusEnabled(false);
        m_jQtyMinus100.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyMinus100ActionPerformed(evt);
            }
        });

        jLabelStatic100.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelStatic100.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabelStatic100.setPreferredSize(new java.awt.Dimension(110, 30));

        jTotalTotal.setFont(new java.awt.Font("Arial", 1, 15)); // NOI18N
        jTotalTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jTotalTotal.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jTotalTotal.setPreferredSize(new java.awt.Dimension(110, 30));

        jTotal100.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTotal100.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jTotal100.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jTotal100.setPreferredSize(new java.awt.Dimension(110, 30));

        jLabelStaticTotal.setFont(new java.awt.Font("Arial", 1, 15)); // NOI18N
        jLabelStaticTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelStaticTotal.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabelStaticTotal.setPreferredSize(new java.awt.Dimension(110, 30));

        jAmount1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jAmount1.setPreferredSize(new java.awt.Dimension(150, 30));

        jAmount5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jAmount5.setPreferredSize(new java.awt.Dimension(150, 30));

        jAmount10.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jAmount10.setPreferredSize(new java.awt.Dimension(150, 30));

        jAmount20.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jAmount20.setPreferredSize(new java.awt.Dimension(150, 30));

        jAmount50.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jAmount50.setPreferredSize(new java.awt.Dimension(150, 30));

        jAmount100.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jAmount100.setPreferredSize(new java.awt.Dimension(150, 30));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabelStaticTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTotalTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(m_jreason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jAmount50, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(m_jQtyPlus50, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(m_jQtyMinus50, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(97, 97, 97)
                                        .addComponent(jLabelStatic50, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jAmount100, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(m_jQtyPlus100, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(m_jQtyMinus100, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(97, 97, 97)
                                        .addComponent(jLabelStatic100, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(43, 43, 43)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTotal100, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTotal50, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jAmount1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addComponent(jAmount5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(m_jQtyPlus5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(m_jQtyMinus5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(97, 97, 97)
                                        .addComponent(jLabelStatic5, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(43, 43, 43)
                                        .addComponent(jTotal5, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(m_jQtyPlus1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(m_jQtyMinus1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(97, 97, 97)
                                        .addComponent(jLabelStatic1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(43, 43, 43)
                                        .addComponent(jTotal1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jAmount10, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addComponent(jAmount20, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(m_jQtyPlus20, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(m_jQtyMinus20, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(97, 97, 97)
                                        .addComponent(jLabelStatic20, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(43, 43, 43)
                                        .addComponent(jTotal20, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(m_jQtyPlus10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(m_jQtyMinus10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(97, 97, 97)
                                        .addComponent(jLabelStatic10, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(43, 43, 43)
                                        .addComponent(jTotal10, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(35, 35, 35)))
                .addContainerGap(66, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(m_jreason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                                .addGap(1, 1, 1)
                                                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(jLabelStatic1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jTotal1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(m_jQtyPlus1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(m_jQtyMinus1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addComponent(jAmount1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                            .addComponent(jLabelStatic5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(jTotal5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(m_jQtyPlus5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(m_jQtyMinus5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                    .addComponent(jAmount5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jLabelStatic10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jTotal10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(m_jQtyPlus10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(m_jQtyMinus10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addComponent(jAmount10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabelStatic20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jTotal20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(m_jQtyPlus20, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(m_jQtyMinus20, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jAmount20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabelStatic50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTotal50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(m_jQtyPlus50, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(m_jQtyMinus50, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jAmount50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelStatic100, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jQtyPlus100, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jQtyMinus100, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTotal100, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jAmount100, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTotalTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelStaticTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(83, Short.MAX_VALUE))
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        m_jKeys.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                m_jKeysPropertyChange(evt);
            }
        });
        jPanel2.add(m_jKeys, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jKeysPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_m_jKeysPropertyChange

    }//GEN-LAST:event_m_jKeysPropertyChange

    private void m_jQtyPlus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyPlus1ActionPerformed
        int temp = Integer.parseInt(jAmount1.getText());
        jAmount1.setValueInteger(++ temp);
    }//GEN-LAST:event_m_jQtyPlus1ActionPerformed

    private void m_jQtyMinus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyMinus1ActionPerformed
        // TODO add your handling code here:
        int temp = Integer.parseInt(jAmount1.getText());
        if((temp - 1) < 0) jAmount1.setValueInteger(0);
        else jAmount1.setValueInteger(-- temp);
    }//GEN-LAST:event_m_jQtyMinus1ActionPerformed

    private void m_jQtyPlus5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyPlus5ActionPerformed
        // TODO add your handling code here:
        int temp = Integer.parseInt(jAmount5.getText());
        jAmount5.setValueInteger(++ temp);
    }//GEN-LAST:event_m_jQtyPlus5ActionPerformed

    private void m_jQtyMinus5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyMinus5ActionPerformed
        // TODO add your handling code here:
        int temp = Integer.parseInt(jAmount5.getText());
        if((temp - 1) < 0) jAmount5.setValueInteger(0);
        else jAmount5.setValueInteger(-- temp);
    }//GEN-LAST:event_m_jQtyMinus5ActionPerformed

    private void m_jQtyPlus10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyPlus10ActionPerformed
        // TODO add your handling code here:
        int temp = Integer.parseInt(jAmount10.getText());
        jAmount10.setValueInteger(++ temp);
    }//GEN-LAST:event_m_jQtyPlus10ActionPerformed

    private void m_jQtyMinus10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyMinus10ActionPerformed
        // TODO add your handling code here:
        int temp = Integer.parseInt(jAmount10.getText());
        if((temp - 1) < 0) jAmount10.setValueInteger(0);
        else jAmount10.setValueInteger(-- temp);
    }//GEN-LAST:event_m_jQtyMinus10ActionPerformed

    private void m_jQtyPlus20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyPlus20ActionPerformed
        // TODO add your handling code here:
        int temp = Integer.parseInt(jAmount20.getText());
        jAmount20.setValueInteger(++ temp);
    }//GEN-LAST:event_m_jQtyPlus20ActionPerformed

    private void m_jQtyMinus20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyMinus20ActionPerformed
        // TODO add your handling code here:
        int temp = Integer.parseInt(jAmount20.getText());
        if((temp - 1) < 0) jAmount20.setValueInteger(0);
        else jAmount20.setValueInteger(-- temp);
    }//GEN-LAST:event_m_jQtyMinus20ActionPerformed

    private void m_jQtyPlus50ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyPlus50ActionPerformed
        // TODO add your handling code here:
        int temp = Integer.parseInt(jAmount50.getText());    
        jAmount50.setValueInteger(++ temp);
    }//GEN-LAST:event_m_jQtyPlus50ActionPerformed

    private void m_jQtyMinus50ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyMinus50ActionPerformed
        // TODO add your handling code here:
        int temp = Integer.parseInt(jAmount50.getText());
        if((temp - 1) < 0) jAmount50.setValueInteger(0);
        else jAmount50.setValueInteger(-- temp);
    }//GEN-LAST:event_m_jQtyMinus50ActionPerformed

    private void m_jQtyPlus100ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyPlus100ActionPerformed
        // TODO add your handling code here:
        int temp = Integer.parseInt(jAmount100.getText());
        jAmount100.setValueInteger(++ temp);
    }//GEN-LAST:event_m_jQtyPlus100ActionPerformed

    private void m_jQtyMinus100ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyMinus100ActionPerformed
        // TODO add your handling code here:
        int temp = Integer.parseInt(jAmount100.getText());
        if((temp - 1) < 0) jAmount100.setValueInteger(0);
        else jAmount100.setValueInteger(-- temp);
    }//GEN-LAST:event_m_jQtyMinus100ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.openbravo.editor.JEditorIntegerPositive jAmount1;
    private com.openbravo.editor.JEditorIntegerPositive jAmount10;
    private com.openbravo.editor.JEditorIntegerPositive jAmount100;
    private com.openbravo.editor.JEditorIntegerPositive jAmount20;
    private com.openbravo.editor.JEditorIntegerPositive jAmount5;
    private com.openbravo.editor.JEditorIntegerPositive jAmount50;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelStatic1;
    private javax.swing.JLabel jLabelStatic10;
    private javax.swing.JLabel jLabelStatic100;
    private javax.swing.JLabel jLabelStatic20;
    private javax.swing.JLabel jLabelStatic5;
    private javax.swing.JLabel jLabelStatic50;
    private javax.swing.JLabel jLabelStaticTotal;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel jTotal1;
    private javax.swing.JLabel jTotal10;
    private javax.swing.JLabel jTotal100;
    private javax.swing.JLabel jTotal20;
    private javax.swing.JLabel jTotal5;
    private javax.swing.JLabel jTotal50;
    private javax.swing.JLabel jTotalTotal;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JButton m_jQtyMinus1;
    private javax.swing.JButton m_jQtyMinus10;
    private javax.swing.JButton m_jQtyMinus100;
    private javax.swing.JButton m_jQtyMinus20;
    private javax.swing.JButton m_jQtyMinus5;
    private javax.swing.JButton m_jQtyMinus50;
    private javax.swing.JButton m_jQtyPlus1;
    private javax.swing.JButton m_jQtyPlus10;
    private javax.swing.JButton m_jQtyPlus100;
    private javax.swing.JButton m_jQtyPlus20;
    private javax.swing.JButton m_jQtyPlus5;
    private javax.swing.JButton m_jQtyPlus50;
    private javax.swing.JComboBox m_jreason;
    // End of variables declaration//GEN-END:variables
    
}
