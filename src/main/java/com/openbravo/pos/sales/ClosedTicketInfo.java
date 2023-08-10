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

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializableWrite;
import com.openbravo.pos.ticket.TicketInfo;

public class ClosedTicketInfo implements SerializableRead, SerializableWrite {

    private String id;
    private String ticketId;
    private String ticketName;
    private String userId;
    private String openedAt;
    private String closedAt;
    private TicketInfo content;
    
    /** Creates a new instance of SharedTicketInfo */
    public ClosedTicketInfo() {
    }
    
    /**
     *
     * @param dr
     * @throws BasicException
     */
    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getString(1);
        ticketId = dr.getString(2);
        ticketName = dr.getString(3);
        userId = dr.getString(4);
        openedAt = dr.getString(5);
        closedAt = dr.getString(6);
        content = (TicketInfo) dr.getObject(7);
    }   

    /**
     *
     * @param dp
     * @throws BasicException
     */
    @Override
    public void writeValues(DataWrite dp) throws BasicException {
       
    }
    
    public String getId() {
        return id;
    }
    
    public String getTicketID() {
        return ticketId;
    }

    public String getTicketName() {
        return ticketName;
    }

    public String getUserID() {
        return userId;
    }

    public String getOpenedAt() {
        return openedAt;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public TicketInfo getContent() {
        return content;
    }
}
