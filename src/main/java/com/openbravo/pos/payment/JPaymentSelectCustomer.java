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

/**
 *
 * @author adrianromero
 */
public class JPaymentSelectCustomer extends JPaymentSelect {
    
    /** Creates new form JPaymentSelect
     * @param parent
     * @param modal
     * @param o */
    protected JPaymentSelectCustomer(java.awt.Frame parent, boolean modal, ComponentOrientation o) {
        super(parent, modal, o);
    }
    /** Creates new form JPaymentSelect
     * @param parent
     * @param modal
     * @param o */
    protected JPaymentSelectCustomer(java.awt.Dialog parent, boolean modal, ComponentOrientation o) {
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
            return new JPaymentSelectCustomer((Frame) window, true, parent.getComponentOrientation());
        } else {
            return new JPaymentSelectCustomer((Dialog) window, true, parent.getComponentOrientation());
        } 
    }

    /**
     *
     */
    @Override
    protected void addTabs() {
// Bank Payment Receipt - Thanks Steve Clough! August 2011
        addTabPayment(new JPaymentSelect.JPaymentCashCreator());
        addTabPayment(new JPaymentSelect.JPaymentChequeCreator());
        addTabPayment(new JPaymentSelect.JPaymentVoucherCreator());
        addTabPayment(new JPaymentSelect.JPaymentBankCreator());
        addTabPayment(new JPaymentSelect.JPaymentMagcardCreator());
        addTabPayment(new JPaymentSelect.JPaymentSlipCreator());
    }
    
    /**
     *
     * @param isPositive
     * @param isComplete
     */
    @Override
    protected void setStatusPanel(boolean isPositive, boolean isComplete) {
        
        setAddEnabled(isPositive && !isComplete);
        setOKEnabled(isPositive);
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
