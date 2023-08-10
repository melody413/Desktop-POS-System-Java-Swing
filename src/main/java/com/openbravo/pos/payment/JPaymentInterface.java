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

import com.google.gson.JsonObject;
import com.openbravo.pos.customers.CustomerInfoExt;
import java.awt.Component;

/**
 *
 * @author Adrian
 */
public interface JPaymentInterface {
    
    public void activate(CustomerInfoExt customerext, double dTotal, String transactionID);    
    public PaymentInfo executePayment();
    public Component getComponent();
    public void setUserData(JsonObject m_userData);
}
