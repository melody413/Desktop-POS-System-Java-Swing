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

/**
 *
 * @author adrianromero
 */
public class PaymentPanelFac {
    
    /** Creates a new instance of PaymentPanelFac */
    private PaymentPanelFac() {
    }
    
    public static PaymentPanel getPaymentPanel(String sReader, JPaymentNotifier notifier) {
// JG 16 May 12 use switch
        switch (sReader) {
            case "Intelligent":
                return new PaymentPanelMagCard(new MagCardReaderIntelligent(), notifier);
            case "Generic":
                return new PaymentPanelMagCard(new MagCardReaderGeneric(), notifier);
            case "Keyboard":
                return new PaymentPanelType(notifier);
            case "EMV":
                return new PaymentPanelEMV(notifier);
            default:
           return new PaymentPanelBasic(notifier);
        }
    }      
}
