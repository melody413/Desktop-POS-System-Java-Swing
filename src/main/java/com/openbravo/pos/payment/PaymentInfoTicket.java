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

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.format.Formats;

public class PaymentInfoTicket extends PaymentInfo implements SerializableRead  {
    
    private static final long serialVersionUID = 8865238639097L;
    private double m_dTicket;
    private String m_sName;
    private String m_transactionID;
    private double m_dTendered;
    private double m_change;
    private String m_dCardName =null;
    private double m_dTip;
    private boolean m_isProcessed;
    private String m_returnMessage;    
    private String m_sVoucher;
    private String sVoucher;
    private String m_sVoucherNumber;
    private String m_sCardNumber;
    private String m_dExpDate;
            
    /** Creates a new instance of PaymentInfoTicket
     * @param dTicket
     * @param sName */
    
// call by Cheque, Bank , Slip
    public PaymentInfoTicket(double dTicket, String sName) {
        m_sName = sName;
        m_dTicket = dTicket;
    }
    
    public PaymentInfoTicket(double dTicket, String sName, String transactionID, String sVoucher) {
        m_sName = sName;
        m_dTicket = dTicket;
        m_transactionID = transactionID;
        m_sVoucher = sVoucher;
    }
    
// call by Voucher    
    public PaymentInfoTicket(double dTicket, String sName, String sVoucher) {
        m_sName = sName;
        m_dTicket = dTicket;
        m_sVoucher = sVoucher;
        m_sVoucherNumber = sVoucher;
    }    
    
    public PaymentInfoTicket() {
        m_sName = null;
        m_dTicket = 0.0;
        m_transactionID = null;
        m_dTendered = 0.00;
     }


    @Override
    public String getExpDate() {
        return m_dExpDate;
    }
    
    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sName = dr.getString(1);
        m_dTicket = dr.getDouble(2);
        m_transactionID = dr.getString(3);
        if (dr.getDouble(4) != null) {
            m_dTendered = dr.getDouble(4);
        }
        m_dCardName = dr.getString(5);
    }
    
    @Override
    public PaymentInfo copyPayment(){
        return new PaymentInfoTicket(m_dTicket, m_sName);
    }
    @Override
    public String getName() {
        return m_sName;
    }   
    @Override
    public double getTotal() {
        return m_dTicket;
    }
    @Override
    public String getTransactionID(){
        return m_transactionID;
    }   

    @Override
    public double getPaid() {
        return (0.0); 
    }

    @Override
    public double getChange(){
       return m_dTendered - m_dTicket;
    }

    @Override
    public double getTip() {
        return m_dTip;
    }
    
    @Override
    public double getTendered() {
        return (0.0); 
    }

    @Override
    public String getCardName() {
       return m_dCardName;
    }

    @Override
    public String printCardNumber() {
        System.out.println(m_sCardNumber);
        if (m_sCardNumber.length() > 4) {
            return m_sCardNumber.substring(0, m_sCardNumber.length() - 4).replaceAll("\\.", "*")
                    + m_sCardNumber.substring(m_sCardNumber.length() - 4);
        } else {
            return "****";
        }
    }

    @Override
    public String getCardNumber() {
        return m_sCardNumber;
    }
   
    public String printPaid() {
        return Formats.CURRENCY.formatValue(m_dTicket);
    }
    

    public String printVoucherTotal() {
        return Formats.CURRENCY.formatValue(-m_dTicket);
    }

    public String printChange() {
        return Formats.CURRENCY.formatValue(m_dTendered - m_dTicket);
    }

    public String printTendered() {
        return Formats.CURRENCY.formatValue(m_dTendered);
    } 

    
    @Override
    public String getVoucher() {
       return m_sVoucher;
    } 
    public String printVoucher() {
        return m_sVoucher;
    }
    public String getVoucherNumber() {
       return m_sVoucherNumber;
    } 
    public String printVoucherNumber() {
        return m_sVoucherNumber;    
    }    
}