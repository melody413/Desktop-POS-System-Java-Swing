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


public class VoucherPaymentInfo extends PaymentInfo implements SerializableRead  {
    
    private static final long serialVersionUID = 8865238639097L;
    private double m_dTicket;
    private String m_sName;
    private String m_sVoucher;
    private double m_dTip;
    private String m_sCardNumber;
    private String m_dExpDate;
   
    public VoucherPaymentInfo(double dTicket, String sName, String sVoucher) {
        m_dTicket = dTicket;
        m_sName = sName;
        m_sVoucher = sVoucher;
    }
    
    public VoucherPaymentInfo() {
        m_dTicket = 0.0;
        m_sName = null;
        m_sVoucher = null;
     }
    
    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sName = dr.getString(1);
        m_dTicket = dr.getDouble(2);
        m_sVoucher = dr.getString(3);
    }
    
    @Override
    public PaymentInfo copyPayment(){
        return new VoucherPaymentInfo(m_dTicket, m_sName, m_sVoucher);
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
    public String getExpDate() {
        return m_dExpDate;
    }
    
    @Override
    public String getTransactionID(){
        return null;
    }
    
    public String printPaid() {
        return Formats.CURRENCY.formatValue(m_dTicket);
    }

    public String printVoucherTotal() {
        return Formats.CURRENCY.formatValue(-m_dTicket);
    }


    @Override
    public double getPaid() {
        return m_dTicket;
    }

    @Override
    public double getChange() {
        return 0;
    }

    @Override
    public double getTip() {
        return m_dTip;
    }


    @Override
    public String getVoucher() {
       return m_sVoucher;
    }

    public String getCardType() {
       return null;
    }

    @Override
    public double getTendered() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCardName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCardNumber() {
        return m_sCardNumber;
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
}
