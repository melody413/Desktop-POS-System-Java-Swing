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

package com.openbravo.pos.forms;

import java.util.HashMap;

/**
 *
 * @author Jack Gerrard
 */
public class Payments {
    private Double amount;
    private Double tendered;
    private final HashMap paymentPaid;
    private final HashMap paymentTendered;
    private final HashMap rtnMessage;
    private String name;
    private final HashMap paymentVoucher;
    private final HashMap paymentNote;
    private final HashMap paymentTip;
    private final HashMap paymentCardnum;
    private final HashMap paymentExpdate;

    /**
     *
     */
    public Payments() {
        paymentPaid =  new HashMap();
        paymentTendered =  new HashMap();
        rtnMessage = new HashMap();
        paymentVoucher = new HashMap();
        paymentNote = new HashMap();
        paymentTip = new HashMap();
        paymentCardnum = new HashMap();
        paymentExpdate = new HashMap();
    }

    /**
     *
     * @param pName
     * @param pAmountPaid
     * @param pTendered
     * @param rtnMsg
     * @param pVoucher
     * @param tip
     * @param cardName
     */
    public void addPayment (String pName, Double pAmountPaid, Double pTendered, String rtnMsg, String pVoucher, String pNote, Double tip, String pCardnum, String pExpdate){
        if (paymentPaid.containsKey(pName)){
            paymentPaid.put(pName,Double.parseDouble(paymentPaid.get(pName).toString()) + pAmountPaid);
            paymentTendered.put(pName,Double.parseDouble(paymentTendered.get(pName).toString()) + pTendered);
            rtnMessage.put(pName, rtnMsg);
            paymentVoucher.put(pName, pVoucher);
            paymentNote.put(pName, pNote);
            paymentTip.put(pName, Double.parseDouble(paymentTip.get(pName).toString()) + tip);
            paymentCardnum.put(pName, pCardnum);
            paymentExpdate.put(pName, pExpdate);
        }else {
            paymentPaid.put(pName, pAmountPaid);
            paymentTendered.put(pName,pTendered);
            rtnMessage.put(pName, rtnMsg);
            paymentNote.put(pName, pNote);
            if (pVoucher !=null) {
                paymentVoucher.put(pName, pVoucher);
            } else {
                pVoucher = "0";
                paymentVoucher.put(pName, pVoucher);
            }
            paymentTip.put(pName, tip);
            paymentCardnum.put(pName, pCardnum);
            paymentExpdate.put(pName, pExpdate);
        }
    }

    /**
     *
     * @param pName
     * @return
     */
    public Double getTendered (String pName){
        return(Double.parseDouble(paymentTendered.get(pName).toString()));
    }

    public String getCardnum (String pName){
        if(paymentCardnum.get(pName) != null )
            return paymentCardnum.get(pName).toString();
        return "";
    }

    public String getExpDate (String pName){
        if(paymentExpdate.get(pName) != null)
            return paymentExpdate.get(pName).toString();
        else return "";
    }

    public Double getTip (String pName){
        return(Double.parseDouble(paymentTip.get(pName).toString()));
    }

    /**
     *
     * @param pName
     * @return
     */
    public Double getPaidAmount (String pName){
        return (Double.parseDouble(paymentPaid.get(pName).toString()));
    }

    /**
     *
     * @return
     */
    public Integer getSize(){
        return (paymentPaid.size());
    }

    /**
     *
     * @param pName
     * @return
     */
    public String getRtnMessage(String pName){
        if(rtnMessage.get(pName) == null) return "";
        else return (rtnMessage.get(pName).toString());
    }

    public String getVoucher(String pName){
        return (paymentVoucher.get(pName).toString());
    }

    public String getNote(String pName) {
        String note = null;
        if (paymentNote.get(pName) != null) {
            note = paymentNote.get(pName).toString();

        }
        return note ;
    }

    public String getFirstElement(){
        String rtnKey= paymentPaid.keySet().iterator().next().toString();
        return(rtnKey);
    }

    /**
     *
     * @param pName
     */
    public void removeFirst (String pName){
        paymentPaid.remove(pName);
        paymentTendered.remove(pName);
        rtnMessage.remove(pName);
    }
}