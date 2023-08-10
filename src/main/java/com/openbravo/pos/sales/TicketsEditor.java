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

package com.openbravo.pos.sales;

import com.openbravo.pos.ticket.TicketInfo;

/**
 *
 * @author JG uniCenta
 */
public interface TicketsEditor {
    
    /**
     *
     * @param oTicket
     * @param oTicketExt
     * @param closed
     */
    public void setActiveTicket(TicketInfo oTicket, Object oTicketExt, boolean closed); // el ticket mas informacion extra...

    /**
     *
     * @return
     */
    public TicketInfo getActiveTicket(); 

    /** 
     *
     * @return
     */
    public String getActivePlaceID();
    public void setClosedTicketID(String id);

}
