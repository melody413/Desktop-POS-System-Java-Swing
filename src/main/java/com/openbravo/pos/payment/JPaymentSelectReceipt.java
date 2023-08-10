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

import java.awt.*;
import jpos.CashDrawer;
import jpos.CashDrawerConst;
import jpos.JposException;

/**
 *
 * @author adrianromero
 */
public class JPaymentSelectReceipt extends JPaymentSelect {
    
    /** Creates new form JPaymentSelect
     * @param parent
     * @param modal
     * @param o */
    protected JPaymentSelectReceipt(java.awt.Frame parent, boolean modal, ComponentOrientation o) {
        super(parent, modal, o);
    }
    /** Creates new form JPaymentSelect
     * @param parent
     * @param o
     * @param modal */
    protected JPaymentSelectReceipt(java.awt.Dialog parent, boolean modal, ComponentOrientation o) {
        super(parent, modal, o);
    }

    /**
     *
     * @param parent
     * @return
     */
    public static JPaymentSelect getDialog(Component parent) {
        Window window = getWindow(parent);
        
        if (window instanceof Frame) { 
            return new JPaymentSelectReceipt((Frame) window, true, parent.getComponentOrientation());
        } else {
            return new JPaymentSelectReceipt((Dialog) window, true, parent.getComponentOrientation());
        }
    }

    /**
     *
     */
    @Override
    protected void addTabs() {
//        boolean drawerStatus = false;
//        try{
//            CashDrawer drawer = new CashDrawer();
//            drawer.open("CashDrawer");
//            drawer.claim(1000);
//            drawer.setDeviceEnabled(true);
//
//            int status = drawer.getState();
//            if (status == CashDrawerConst.CASH_SUE_DRAWEROPEN) {
//                drawerStatus = true;
//            } else if (status == CashDrawerConst.CASH_SUE_DRAWERCLOSED) {
//                drawerStatus = true;
//            }
//
//            drawer.release();
//        } catch (JposException e) {
//            System.out.println("JposException: " + e.getMessage());
//        }

//        if(drawerStatus) addTabPayment(new JPaymentSelect.JPaymentCashCreator());
        addTabPayment(new JPaymentSelect.JPaymentCashCreator());
//        addTabPayment(new JPaymentSelect.JPaymentChequeCreator());
//        addTabPayment(new JPaymentSelect.JPaymentVoucherCreator());            
//        addTabPayment(new JPaymentSelect.JPaymentMagcardCreator());        
        addTabPayment(new JPaymentSelect.JPaymentMagcardRefundCreator());                
//        addTabPayment(new JPaymentSelect.JPaymentDebtCreator());
//        addTabPayment(new JPaymentSelect.JPaymentBankCreator());        
//        addTabPayment(new JPaymentSelect.JPaymentSlipCreator());  
    }
    
    /**
     *
     * @param isPositive
     * @param isComplete
     */
    @Override
    protected void setStatusPanel(boolean isPositive, boolean isComplete) {
        
        setAddEnabled(isPositive && !isComplete);
        setOKEnabled(isComplete);
    }
    
    /**
     *
     * @param total
     * @return
     */
    @Override
    protected PaymentInfo getDefaultPayment(double total) {
//        return new PaymentInfoCash_original(total, total);
        return new PaymentInfoCash(total, total);        
    }
}
