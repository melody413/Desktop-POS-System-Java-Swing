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

package com.openbravo.pos.payment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openbravo.beans.JSplitPop;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 *
 * @author adrianromero
 */
@Slf4j
public abstract class JPaymentSelect extends javax.swing.JDialog implements JPaymentNotifier {

    private PaymentInfoList m_aPaymentInfo;
    private boolean printselected;

    private int accepted;

    private AppView app;
    private double m_dTotal;
    private CustomerInfoExt customerext;
    private DataLogicSystem dlSystem;
    private DataLogicCustomers dlCustomers;
    private DataLogicSales dlSales;

private JsonObject userData;

    // JG 16 May 12 use diamond inference
    private Map<String, JPaymentInterface> payments = new HashMap<>();
    private String m_sTransactionID;
    private static PaymentInfo returnPayment = null;

    private JPaymentSelect getInstance(){
        return this;
    }

    public static PaymentInfo getReturnPayment() {
        return returnPayment;
    }

    public static void setReturnPayment(PaymentInfo returnPayment) {
        JPaymentSelect.returnPayment = returnPayment;
    }


    /**
     * Creates new form JPaymentSelect
     *
     * @param parent
     * @param modal
     * @param o
     */
    protected JPaymentSelect(java.awt.Frame parent, boolean modal, ComponentOrientation o) {
        super(parent, modal);
        initComponents();
        this.applyComponentOrientation(o);
        getRootPane().setDefaultButton(m_jButtonOK);

    }

    /** Creates new form JPaymentSelect
     * @param parent
     * @param modal
     * @param o */
    protected JPaymentSelect(java.awt.Dialog parent, boolean modal, ComponentOrientation o) {
        super(parent, modal);
        initComponents();

        m_jButtonPrint.setVisible(true);
        this.applyComponentOrientation(o);
        if (printselected) {
            jlblPrinterStatus.setText("Printer ON");
        } else {
            jlblPrinterStatus.setText("Printer OFF");
        }
    }

    public void init(AppView app) {
        this.app = app;
        dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dlCustomers= (DataLogicCustomers) app.getBean("com.openbravo.pos.customers.DataLogicCustomers");
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");

//        try {
//            URL url = new URL((String) app.getProperties().getProperty("fortis.api_host") + "/users/" + app.getProperties().getProperty("fortis.user_id"));
//            HttpURLConnection http = (HttpURLConnection)url.openConnection();
//
//            http.setRequestProperty("user-id", app.getProperties().getProperty("fortis.user_id"));
//            http.setRequestProperty("user-api-key", app.getProperties().getProperty("fortis.user_apikey"));
//            http.setRequestProperty("developer-id", app.getProperties().getProperty("fortis.developer_id"));
//            http.setRequestProperty("Accept", "application/json");
//
//            JsonParser jp = new JsonParser();
//            JsonElement root = jp.parse(new InputStreamReader((InputStream) http.getContent()));
//
//            JsonObject observation = root.getAsJsonObject().get("user").getAsJsonObject();
//            userData = observation;
//
//            http.disconnect();
//        } 
//        catch (MalformedURLException e) {}
//        catch (IOException exception) {}

        m_jButtonRemove.setVisible(false);
        m_jButtonAdd.setVisible(false);
        m_jRemaininglEuros.setVisible(false);
        m_jLblRemainingEuros.setVisible(false);

        printselected = false;
        if (printselected) {
            jlblPrinterStatus.setText("Printer ON");
        } else {
            jlblPrinterStatus.setText("Printer OFF");
        }

        m_jButtonSplit.setText("Split");

    }

    public void setPrintSelected(boolean value) {
        printselected = value;
    }

    public boolean isPrintSelected() {
        return printselected;
    }

    public List<PaymentInfo> getSelectedPayments() {
        return m_aPaymentInfo.getPayments();
    }

    public int showDialog(double total, CustomerInfoExt customerext,double deposit) {
        m_aPaymentInfo = new PaymentInfoList();
        accepted = 0;
        total -= deposit;
        m_dTotal = total;

        this.customerext = customerext;
        m_jButtonPrint.setVisible(true);
        m_jButtonPrint.setSelected(printselected);
        m_jTotalEuros.setText(Formats.CURRENCY.formatValue(m_dTotal));

        addTabs();

        // gets the print button state
        printselected = m_jButtonPrint.isSelected();

        // remove all tabs
        m_jTabPayment.removeAll();

        return accepted;
    }

    public int showDialog(double total, CustomerInfoExt customerext, int current, int split) {

        jlblPaymentStep.setText("Payment " + current + " of " + split);
        if (current > 1) { 
            m_jButtonCancel.setEnabled(false);
        } else {
            m_jButtonCancel.setEnabled(true);
        }

        if (split > 1) { 
            m_jButtonSplit.setEnabled(false);
        } else {
            jlblPaymentStep.setText("");
            m_jButtonSplit.setEnabled(true);       
        }


        m_aPaymentInfo = new PaymentInfoList();
        accepted = 0;

        m_dTotal = total;

        this.customerext = customerext;

        setPrintSelected(!Boolean.parseBoolean(app.getProperties().getProperty("till.receiptprintoff")));
        m_jButtonPrint.setSelected(printselected);
        m_jTotalEuros.setText(Formats.CURRENCY.formatValue(m_dTotal));

        if (printselected) {
            jlblPrinterStatus.setText("Printer ON");
        } else {
            jlblPrinterStatus.setText("Printer OFF");
        }

// N. Deppe 08/11/2018
// Fix issue where dialog keeps moving lower and lower on the screen
// Get the size of the screen, and center the dialog in the window
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension thisDim = this.getSize();
        int x = (screenDim.width - thisDim.width) / 2;
        int y = (screenDim.height - thisDim.height) / 2;
        this.setLocation(x, y);

        addTabs();

        if (m_jTabPayment.getTabCount() == 0) {
            // No payment panels available
            m_aPaymentInfo.add(getDefaultPayment(total));
            accepted = 1;
        } else {
            getRootPane().setDefaultButton(m_jButtonOK);
            printState();
            setVisible(true);
        }

        // gets the print button state
        printselected = m_jButtonPrint.isSelected();

        // remove all tabs
        m_jTabPayment.removeAll();

        return accepted;
    }

    protected abstract void addTabs();
    protected abstract void setStatusPanel(boolean isPositive, boolean isComplete);
    protected abstract PaymentInfo getDefaultPayment(double total);

    protected void setOKEnabled(boolean value) {
        m_jButtonOK.setEnabled(value);
    }

    protected void setAddEnabled(boolean value) {
        m_jButtonAdd.setEnabled(value);
    }

    protected void addTabPayment(JPaymentCreator jpay) {
        JPaymentInterface jpayinterface = payments.get(jpay.getKey());
        if (jpayinterface == null) {
            jpayinterface = jpay.createJPayment();
            payments.put(jpay.getKey(), jpayinterface);
        }

        jpayinterface.getComponent().applyComponentOrientation(getComponentOrientation());

        m_jTabPayment.addTab(
                AppLocal.getIntString(jpay.getLabelKey()),
                new javax.swing.ImageIcon(getClass().getResource(jpay.getIconKey())),
                jpayinterface.getComponent());
    }


    public interface JPaymentCreator {
        public JPaymentInterface createJPayment();
        public String getKey();
        public String getLabelKey();
        public String getIconKey();
    }

    public class JPaymentCashCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentCashPos(JPaymentSelect.this, dlSystem);
        }
        @Override
        public String getKey() {
            return "payment.cash"; }
        @Override
        public String getLabelKey() {
            return "tab.cash"; }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/cash.png"; }
    }

    public class JPaymentChequeCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentCheque(JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "payment.cheque"; }
        @Override
        public String getLabelKey() {
            return "tab.cheque"; }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/cheque.png"; }
    }

    public class JPaymentVoucherCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentVoucher(app, JPaymentSelect.this, "voucherin");
        }
        @Override
        public String getKey() {
            return "payment.voucher"; }
        @Override
        public String getLabelKey() {
            return "tab.voucher"; }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/voucher.png"; }
    }

    public class JPaymentMagcardCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            JPaymentCredit temp = new JPaymentCredit(app, JPaymentSelect.this);
            temp.setUserData(userData);
            return temp;
        }
        @Override
        public String getKey() {
            return "payment.magcard"; }
        @Override
        public String getLabelKey() {
            return "tab.magcard"; }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/ccard.png"; }
    }



    public class JPaymentFreeCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentFree(JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "payment.free";
        }
        @Override
        public String getLabelKey() {
            return "tab.free";
        }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/wallet.png";
        }
    }

    public class JPaymentDebtCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentDebt(JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "payment.debt";
        }
        @Override
        public String getLabelKey() {
            return "tab.debt";
        }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/customer.png";
        }
    }

    public class JPaymentCashRefundCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "cashrefund");
        }
        @Override
        public String getKey() {
            return "refund.cash";
        }
        @Override
        public String getLabelKey() {
            return "tab.cashrefund";
        }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/cash.png";
        }
    }

    public class JPaymentChequeRefundCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "chequerefund");
        }
        @Override
        public String getKey() {
            return "refund.cheque";
        }
        @Override
        public String getLabelKey() {
            return "tab.chequerefund";
        }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/cheque.png";
        }
    }

    public class JPaymentVoucherRefundCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "voucherout");
        }
        @Override
        public String getKey() {
            return "refund.voucher";
        }
        @Override
        public String getLabelKey() {
            return "tab.voucher";
        }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/voucher.png";
        }
    }

    public class JPaymentMagcardRefundCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            JPaymentMagcard temp = new JPaymentMagcard(app, JPaymentSelect.this);
            temp.setUserData(userData);
            return temp;
        }
        @Override
        public String getKey() {
            return "refund.magcard";
        }
        @Override
        public String getLabelKey() {
            return "tab.magcard";
        }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/ccard.png";
        }
    }

    public class JPaymentDebtRefundCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentDebt(JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "refund.debt";
        }
        @Override
        public String getLabelKey() {
            return "tab.debt";
        }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/customer.png";
        }
    }
    public class JPaymentBankCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentBank(JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "payment.bank";
        }
        @Override
        public String getLabelKey() {
            return "tab.bank";
        }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/bank.png";
        }
    }

    public class JPaymentSlipCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentSlip(JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "payment.slip";
        }
        @Override
        public String getLabelKey() {
            return "tab.slip";
        }
        @Override
        public String getIconKey() {
            return "/com/openbravo/images/slip.png";
        }
    }

    private void printState() {

        m_jRemaininglEuros.setText(Formats.CURRENCY.formatValue(
                m_dTotal - m_aPaymentInfo.getTotal()));
        m_jButtonRemove.setEnabled(!m_aPaymentInfo.isEmpty());
        m_jTabPayment.setSelectedIndex(0);
        ((JPaymentInterface) m_jTabPayment.getSelectedComponent())
                .activate(customerext,
                        m_dTotal - m_aPaymentInfo.getTotal()
                        , m_sTransactionID);
    }

    protected static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window)parent;
        } else {
            return getWindow(parent.getParent());
        }
    }

    @Override
    public void setStatus(boolean isPositive, boolean isComplete) {

        setStatusPanel(isPositive, isComplete);
    }

    public void setTransactionID(String tID){
        this.m_sTransactionID = tID;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        m_jLblTotalEuros1 = new javax.swing.JLabel();
        m_jTotalEuros = new javax.swing.JLabel();
        m_jLblRemainingEuros = new javax.swing.JLabel();
        m_jRemaininglEuros = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        m_jButtonRemove = new javax.swing.JButton();
        m_jButtonAdd = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        m_jTabPayment = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jlblPaymentStep = new javax.swing.JLabel();
        m_jButtonCancel = new javax.swing.JButton();
        m_jButtonSplit = new javax.swing.JButton();
        m_jButtonOK = new javax.swing.JButton();
        m_jButtonPrint = new javax.swing.JToggleButton();
        jlblPrinterStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("payment.title")); // NOI18N
        setResizable(false);

        jPanel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        m_jLblTotalEuros1.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        m_jLblTotalEuros1.setText(AppLocal.getIntString("label.totalcash")); // NOI18N
        m_jLblTotalEuros1.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jTotalEuros.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        m_jTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jTotalEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTotalEuros.setOpaque(true);
        m_jTotalEuros.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jTotalEuros.setRequestFocusEnabled(false);

        m_jLblRemainingEuros.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        m_jLblRemainingEuros.setText(AppLocal.getIntString("label.remainingcash")); // NOI18N
        m_jLblRemainingEuros.setPreferredSize(new java.awt.Dimension(120, 30));

        m_jRemaininglEuros.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        m_jRemaininglEuros.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jRemaininglEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jRemaininglEuros.setOpaque(true);
        m_jRemaininglEuros.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jRemaininglEuros.setRequestFocusEnabled(false);

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        m_jButtonRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/btnminus.png"))); // NOI18N
        m_jButtonRemove.setToolTipText("Delete Part Payment");
        m_jButtonRemove.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonRemoveActionPerformed(evt);
            }
        });

        m_jButtonAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/btnplus.png"))); // NOI18N
        m_jButtonAdd.setToolTipText("Add Part Payment");
        m_jButtonAdd.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonAddActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(51, 51, 51)
                .add(m_jLblTotalEuros1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(m_jTotalEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(m_jLblRemainingEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(m_jRemaininglEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(18, 18, 18)
                .add(m_jButtonAdd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_jButtonRemove, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .add(0, 0, Short.MAX_VALUE)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, m_jButtonRemove, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, m_jButtonAdd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(m_jLblTotalEuros1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(m_jRemaininglEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(m_jLblRemainingEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(m_jTotalEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        getContentPane().add(jPanel4, java.awt.BorderLayout.NORTH);

        jPanel3.setNextFocusableComponent(m_jTabPayment);
        jPanel3.setLayout(new java.awt.BorderLayout());

        m_jTabPayment.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jTabPayment.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        m_jTabPayment.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTabPayment.setRequestFocusEnabled(false);
        m_jTabPayment.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                m_jTabPaymentStateChanged(evt);
            }
        });
        m_jTabPayment.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                m_jTabPaymentKeyPressed(evt);
            }
        });
        jPanel3.add(m_jTabPayment, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel2.setPreferredSize(new java.awt.Dimension(500, 55));

        jlblPaymentStep.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jlblPaymentStep.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlblPaymentStep.setMaximumSize(new java.awt.Dimension(32767, 29));
        jlblPaymentStep.setMinimumSize(new java.awt.Dimension(50, 29));
        jlblPaymentStep.setPreferredSize(new java.awt.Dimension(180, 29));
        jPanel2.add(jlblPaymentStep);

        m_jButtonCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        m_jButtonCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setFocusable(false);
        m_jButtonCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonCancel.setPreferredSize(new java.awt.Dimension(105, 45));
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonCancel);

        m_jButtonSplit.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonSplit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_split_sml.png"))); // NOI18N
        m_jButtonSplit.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        m_jButtonSplit.setFocusPainted(false);
        m_jButtonSplit.setFocusable(false);
        m_jButtonSplit.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonSplit.setPreferredSize(new java.awt.Dimension(90, 45));
        m_jButtonSplit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonSplitActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonSplit);

        m_jButtonOK.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jButtonOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        m_jButtonOK.setText(AppLocal.getIntString("button.OK")); // NOI18N
        m_jButtonOK.setFocusPainted(false);
        m_jButtonOK.setFocusable(false);
        m_jButtonOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonOK.setMaximumSize(new java.awt.Dimension(100, 44));
        m_jButtonOK.setPreferredSize(new java.awt.Dimension(90, 45));
        m_jButtonOK.setRequestFocusEnabled(false);
        m_jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonOKActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonOK);

        jPanel5.add(jPanel2, java.awt.BorderLayout.LINE_END);
        jPanel2.getAccessibleContext().setAccessibleDescription("");

        m_jButtonPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer24_off.png"))); // NOI18N
        m_jButtonPrint.setSelected(true);
        m_jButtonPrint.setToolTipText("Print Receipt");
        m_jButtonPrint.setFocusPainted(false);
        m_jButtonPrint.setFocusable(false);
        m_jButtonPrint.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonPrint.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jButtonPrint.setRequestFocusEnabled(false);
        m_jButtonPrint.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer24.png"))); // NOI18N
        m_jButtonPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonPrintActionPerformed(evt);
            }
        });
        jPanel5.add(m_jButtonPrint, java.awt.BorderLayout.LINE_START);

        jlblPrinterStatus.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jlblPrinterStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jlblPrinterStatus.setText(bundle.getString("label.printerstatusOn")); // NOI18N
        jlblPrinterStatus.setMaximumSize(new java.awt.Dimension(30, 29));
        jlblPrinterStatus.setMinimumSize(new java.awt.Dimension(50, 29));
        jlblPrinterStatus.setPreferredSize(new java.awt.Dimension(80, 29));
        jPanel5.add(jlblPrinterStatus, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel5, java.awt.BorderLayout.SOUTH);

        setSize(new java.awt.Dimension(758, 497));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonRemoveActionPerformed

        m_aPaymentInfo.removeLast();
        printState();

    }//GEN-LAST:event_m_jButtonRemoveActionPerformed

    private void m_jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonAddActionPerformed

        JPaymentInterface selectedComponent = (JPaymentInterface) m_jTabPayment.getSelectedComponent();
        PaymentInfo returnPayment = selectedComponent.executePayment();
        if (returnPayment != null) {
            m_aPaymentInfo.add(returnPayment);
            printState();
        }

    }//GEN-LAST:event_m_jButtonAddActionPerformed

    private void m_jTabPaymentStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_jTabPaymentStateChanged

        if (m_jTabPayment.getSelectedComponent() != null) {
            ((JPaymentInterface) m_jTabPayment.getSelectedComponent())
                    .activate(customerext
                            , m_dTotal - m_aPaymentInfo.getTotal()
                            , m_sTransactionID);
            m_jRemaininglEuros.setText(
                    Formats.CURRENCY.formatValue(
                            m_dTotal - m_aPaymentInfo.getTotal()));
        }

    }//GEN-LAST:event_m_jTabPaymentStateChanged

    private void m_jTabPaymentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_jTabPaymentKeyPressed

        if( evt.getKeyCode() == KeyEvent.VK_F1 ) {

        } else if ( evt.getKeyCode() == KeyEvent.VK_F2 ) {

        }
    }//GEN-LAST:event_m_jTabPaymentKeyPressed

    private void m_jButtonPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonPrintActionPerformed
        if (!m_jButtonPrint.isSelected()) {
            jlblPrinterStatus.setText("Printer OFF");
        } else {
            jlblPrinterStatus.setText("Printer ON");
        }
    }//GEN-LAST:event_m_jButtonPrintActionPerformed

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonOKActionPerformed

        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                m_jButtonOK.setEnabled(false);
                PaymentInfo paymentInfo = ((JPaymentInterface) m_jTabPayment.getSelectedComponent()).executePayment();
                setReturnPayment(paymentInfo);
                return null;
            }

            @Override
            public void done() {
                m_jButtonOK.setEnabled(true);
                m_jButtonCancel.setEnabled(true);
                if (returnPayment != null) {
                    m_aPaymentInfo.add(returnPayment);
                    accepted = 1;
                    dispose();
                }
            }
        };

        worker.execute();
    }//GEN-LAST:event_m_jButtonOKActionPerformed

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed

        dispose();
    }//GEN-LAST:event_m_jButtonCancelActionPerformed

    private void m_jButtonSplitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonSplitActionPerformed
        // TODO add your handling code here:
        Integer splitValue = JSplitPop.showEditNumber(this, "Split");
        accepted = (splitValue == null) ? 0 : splitValue.intValue();
        dispose();
    }//GEN-LAST:event_m_jButtonSplitActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel jlblPaymentStep;
    private javax.swing.JLabel jlblPrinterStatus;
    private javax.swing.JButton m_jButtonAdd;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JButton m_jButtonOK;
    private javax.swing.JToggleButton m_jButtonPrint;
    private javax.swing.JButton m_jButtonRemove;
    private javax.swing.JButton m_jButtonSplit;
    private javax.swing.JLabel m_jLblRemainingEuros;
    private javax.swing.JLabel m_jLblTotalEuros1;
    private javax.swing.JLabel m_jRemaininglEuros;
    private javax.swing.JTabbedPane m_jTabPayment;
    private javax.swing.JLabel m_jTotalEuros;
    // End of variables declaration//GEN-END:variables
}
