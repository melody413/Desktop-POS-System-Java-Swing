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
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.util.RoundUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author JG uniCenta
 */
public class JPaymentCredit extends javax.swing.JPanel implements JPaymentInterface {
    
    private JPaymentNotifier m_notifier;
    private PaymentPanel m_cardpanel;

    private double m_dPaid;
    private double m_dTotal;

    private JsonObject userData;

    private String cc_number = "5431111111111111";
    private String cc_exp = "1224";
    private double cc_tip = 0.0;

    private AppView app;
    
    /** Creates new form JPaymentCash
     * @param notifier */
    public JPaymentCredit(AppView m_app, JPaymentNotifier notifier) {
        app = m_app;
        m_notifier = notifier;
        
        initComponents();
    }
    
    /**
     *
     * @param customerext
     * @param dTotal
     * @param transID
     */
    @Override
    public void activate(CustomerInfoExt customerext, double dTotal, String transID) {
        m_dTotal = dTotal;

        jLabel8.setText("Amount");
        m_jNumber.setText("Number");
        m_jExp.setText("Exp Date");

        m_jMoneyNumber.setText(replaceWith(cc_number, cc_number.length() - 4));
        m_jMoneyExp.setText(replaceWith(cc_exp, cc_exp.length()));
        
        printState();
        
    }

    public static String replaceWith(String s, int replace) {
        int length = s.length();
        String temp = s.substring(replace, length);
        for(int i = 0; i < length; i ++) temp = "*" + temp;
        return temp;
    }

    /**
     *
     * @return
     */
    @Override
    public PaymentInfo executePayment() {
        JsonObject observation = null;
//        try {
//            URL url = new URL((String) app.getProperties().getProperty("fortis.api_host") + "/transactions");
//            HttpURLConnection http = (HttpURLConnection)url.openConnection();
//
//            http.setRequestMethod("POST");
//            http.setRequestProperty("user-id", app.getProperties().getProperty("fortis.user_id"));
//            http.setRequestProperty("user-api-key", app.getProperties().getProperty("fortis.user_apikey"));
//            http.setRequestProperty("developer-id", app.getProperties().getProperty("fortis.developer_id"));
//            http.setRequestProperty("Accept", "application/json");
//            http.setRequestProperty("Content-Type", "application/json");
//            http.setDoOutput(true);
//	
////          // Set payload - Start
//            String postData = "{\n" +
//            "    \"transaction\": {\n" +
//            "        \"action\": \"authonly\",\n" +
//            "        \"payment_method\": \"cc\",\n" +
//            "        \"account_number\": \"" + cc_number + "\",\n" +
//            "        \"exp_date\":\"" + cc_exp + "\",\n" +
//            "        \"transaction_amount\": " + new BigDecimal(m_dPaid).setScale(8, RoundingMode.HALF_EVEN) + ",\n" +
//            "        \"tip_amount\": " + new BigDecimal(cc_tip).setScale(2, RoundingMode.HALF_EVEN) + ",\n" +
//            "        \"location_id\": " + userData.get("primary_location_id").toString() + "\n" +
//            "    }\n" +
//            "}";
//
//            DataOutputStream wr = new DataOutputStream(http.getOutputStream());
//            wr.writeBytes(postData);
//            wr.flush();
//            wr.close();
////          // Set payload - End
//
//            JsonParser jp = new JsonParser(); //from gson
//            JsonElement root = jp.parse(new InputStreamReader((InputStream) http.getContent()));
//            observation = root.getAsJsonObject().get("transaction").getAsJsonObject();
//
//            m_dPaid = Double.parseDouble(observation.get("transaction_amount").getAsString());
//
//            http.disconnect();
//            
//        } 
//        catch (MalformedURLException e) {
//            System.out.println("^^^^^^^^^^^^ mal " + e.getMessage());
//        }
//        catch (IOException exception) {
//            System.out.println("^^^^^^^^^^^^ IOEx " +  exception.getMessage());
//        }

        PaymentInfoMagcard temp = new PaymentInfoMagcard(null, cc_number, cc_exp, 
            null, null, null, null, null, null, m_dPaid + cc_tip);

        temp.setCardName(temp.getName());
        temp.setTip(cc_tip);
        temp.setExpDate(cc_exp);

        return temp;
    }

    /**
     *
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

    private void printState() {
        m_dPaid = m_dTotal;

        m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(new Double(m_dPaid)));
        
        int iCompare = RoundUtils.compare(m_dPaid, m_dTotal);
        
        // if iCompare > 0 then the payment is not valid
        m_notifier.setStatus(m_dPaid > 0.0 && iCompare <= 0, iCompare == 0);
    }
    
    private class RecalculateState implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            printState();
        }
    }     
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel3 = new javax.swing.JPanel();
        m_jTendered = new com.openbravo.editor.JEditorCurrencyPositive();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        m_jMoneyEuros = new javax.swing.JLabel();
        m_jNumber = new javax.swing.JLabel();
        m_jMoneyNumber = new javax.swing.JLabel();
        m_jExp = new javax.swing.JLabel();
        m_jMoneyExp = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        m_jKeys.setEnabled(false);
        jPanel1.add(m_jKeys);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());

        m_jTendered.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel3.add(m_jTendered, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3);

        jPanel2.add(jPanel1, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.EAST);

        jPanel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel4.setLayout(null);

        jLabel8.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel8.setText(AppLocal.getIntString("label.InputCash")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel4.add(jLabel8);
        jLabel8.setBounds(10, 10, 100, 30);

        m_jMoneyEuros.setBackground(new java.awt.Color(204, 255, 51));
        m_jMoneyEuros.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        m_jMoneyEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyEuros.setOpaque(true);
        m_jMoneyEuros.setPreferredSize(new java.awt.Dimension(180, 30));
        jPanel4.add(m_jMoneyEuros);
        m_jMoneyEuros.setBounds(120, 10, 180, 30);

        m_jNumber.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        m_jNumber.setText(AppLocal.getIntString("label.InputCash")); // NOI18N
        m_jNumber.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel4.add(m_jNumber);
        m_jNumber.setBounds(10, 50, 100, 30);

        m_jMoneyNumber.setBackground(new java.awt.Color(255, 255, 255));
        m_jMoneyNumber.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        m_jMoneyNumber.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyNumber.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyNumber.setOpaque(true);
        m_jMoneyNumber.setPreferredSize(new java.awt.Dimension(180, 30));
        jPanel4.add(m_jMoneyNumber);
        m_jMoneyNumber.setBounds(120, 50, 180, 30);

        m_jExp.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        m_jExp.setText(AppLocal.getIntString("label.InputCash")); // NOI18N
        m_jExp.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel4.add(m_jExp);
        m_jExp.setBounds(10, 90, 100, 30);

        m_jMoneyExp.setBackground(new java.awt.Color(255, 255, 255));
        m_jMoneyExp.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        m_jMoneyExp.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyExp.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyExp.setOpaque(true);
        m_jMoneyExp.setPreferredSize(new java.awt.Dimension(180, 30));
        jPanel4.add(m_jMoneyExp);
        m_jMoneyExp.setBounds(120, 90, 180, 30);

        add(jPanel4, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel m_jExp;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JLabel m_jMoneyEuros;
    private javax.swing.JLabel m_jMoneyExp;
    private javax.swing.JLabel m_jMoneyNumber;
    private javax.swing.JLabel m_jNumber;
    private com.openbravo.editor.JEditorCurrencyPositive m_jTendered;
    // End of variables declaration//GEN-END:variables
    
}
