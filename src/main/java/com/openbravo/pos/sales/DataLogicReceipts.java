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
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceFind;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.SerializerReadString;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.ticket.TicketInfo;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author adrianromero
 */
public class DataLogicReceipts extends BeanFactoryDataSingle {
    
    private Session s;
    
    /** Creates a new instance of DataLogicReceipts */
    public DataLogicReceipts() {
    }
    
    /**
     *
     * @param s
     */
    @Override
    public void init(Session s){
        this.s = s;
    }
     
    /**
     *
     * @param Id
     * @return
     * @throws BasicException
     */
    public final TicketInfo getSharedTicket(String Id) throws BasicException {
        
        if (Id == null) {
            return null; 
        } else {
            Object[]record = (Object[]) new StaticSentence(s
                    , "SELECT CONTENT, LOCKED FROM sharedtickets WHERE ID = ? ORDER BY ID"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadBasic(new Datas[] {
                        Datas.SERIALIZABLE})).find(Id);
            return record == null ? null : (record[0] == null ? new TicketInfo() : (TicketInfo) record[0]);
        }
    }

    public final TicketInfo getClosedTicket(String Id) throws BasicException {
        
        if (Id == null) {
            return null; 
        } else {
            Object[]record = (Object[]) new StaticSentence(s
                    , "SELECT CONTENT FROM closedtickets WHERE id = ? ORDER BY id"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadBasic(new Datas[] {
                        Datas.SERIALIZABLE})).find(Id);
            return record == null ? null : (record[0] == null ? new TicketInfo() : (TicketInfo) record[0]);
        }
    }

    public final String getNameSharedTicket(String Id) throws BasicException {
        
        if (Id == null) {
            return null; 
        } else {
            String record = (String) new StaticSentence(s
                    , "SELECT NAME FROM sharedtickets WHERE ID = ? ORDER BY ID"
                    , SerializerWriteString.INSTANCE
                    , SerializerReadString.INSTANCE).find(Id);
            return record;
        }
    }

    public final String getClosedTicketID(String id) throws BasicException {
        Object[] ticketid = (Object[]) new StaticSentence(s
            , "SELECT ticketid FROM closedtickets WHERE id = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {
                Datas.STRING
            })).find(id);

        return ticketid[0].toString();
    }



    public final TicketInfo getSharedTicketUnlocked(String Id) throws BasicException {
        
        if (Id == null) {
            return null; 
        } else {
            Object[]record = (Object[]) new StaticSentence(s
                    , "SELECT CONTENT, LOCKED FROM sharedtickets WHERE ID = ? AND locked = '' ORDER BY ID"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadBasic(new Datas[] {
                        Datas.SERIALIZABLE})).find(Id);
            return record == null ? null : (record[0] == null ? new TicketInfo() : (TicketInfo) record[0]);
        }
    }


    
    /**
     *
     * @param pickupId
     * @return
     * @throws BasicException
     */
    public final TicketInfo getSharedTicketPickupId(String pickupId) throws BasicException {
        
        if (pickupId == null) {
            return null; 
        } else {
            Object[]record = (Object[]) new StaticSentence(s
                    , "SELECT CONTENT, LOCKED FROM sharedtickets WHERE PICKUPID = ?"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadBasic(new Datas[] {
                        Datas.SERIALIZABLE})).find(pickupId);
            return record == null ? null : (TicketInfo) record[0];
        }
    } 

    public final void updateSharedTicketBalance(String id, double balance) throws BasicException {
        
        Object[] values = new Object[] {
            id, 
            balance,
        };
        Datas[] datas = new Datas[] {
            Datas.STRING,
            Datas.DOUBLE,
        };

        new PreparedSentence(s
                , "UPDATE sharedtickets SET "
                + "BALANCE = ? "
                + "WHERE ID = ?"
                , new SerializerWriteBasicExt(datas, 
                        new int[] {
                            1, 0})).exec(values);
    }

    /**
     * JG Dec 14 Administrator and Manager Roles always have access to ALL SHAREDtickets
     * @return
     * @throws BasicException
     */
    public final List<SharedTicketInfo> getSharedTicketList() throws BasicException {
        
        return (List<SharedTicketInfo>) new StaticSentence(s
                , "SELECT ID, NAME, CONTENT, APPUSER, PICKUPID, LOCKED, BALANCE "
                        + "FROM sharedtickets ORDER BY PICKUPID" 
                , null
                , new SerializerReadClass(SharedTicketInfo.class)).list();
    }

    public final List<SharedTicketInfo> getSharedTicketListByUserID(final String userID) throws BasicException {
        
        return (List<SharedTicketInfo>) new StaticSentence(s
                , "SELECT sharedtickets.ID, sharedtickets.NAME, sharedtickets.CONTENT, sharedtickets.APPUSER, sharedtickets.PICKUPID, "
                    + "sharedtickets.LOCKED, sharedtickets.BALANCE FROM sharedtickets, people "
                    + "where sharedtickets.appuser = people.id and people.id = '" + userID + "' ORDER BY PICKUPID" 
                , null
                , new SerializerReadClass(SharedTicketInfo.class)).list();
    }

    public final SharedTicketInfo getSharedTicketById(final String id) throws BasicException {
        return (SharedTicketInfo) new StaticSentence(s
                , "SELECT ID, NAME, CONTENT, APPUSER, PICKUPID, LOCKED, BALANCE "
                        + "FROM sharedtickets where ID = '" + id + "' " 
                , null
                , new SerializerReadClass(SharedTicketInfo.class)).find();
    }

    public final List<SharedTicketInfo> getSharedTicketListWithoutItself(String id) throws BasicException {
        return (List<SharedTicketInfo>) new StaticSentence(s
                , "SELECT ID, NAME, CONTENT, APPUSER, PICKUPID, LOCKED, BALANCE "
                        + "FROM sharedtickets WHERE ID != '" + id + "'  ORDER BY PICKUPID" 
                , null
                , new SerializerReadClass(SharedTicketInfo.class)).list();
    }

    
    public final List<SharedTicketInfo> getSharedTicketListById(final String id) throws BasicException {

        String[] temp;
        String idString = "";
        String realId = "";
        if (id.contains("[")) {
            temp = id.split("\\[");
            idString = "%" + temp[0] + "[%";
            realId = temp[0];
        } else {
            idString = "%" + id + "[%";
            realId = id;
        }

        return (List<SharedTicketInfo>) new StaticSentence(s
                , "SELECT ID, NAME, CONTENT, APPUSER, PICKUPID, LOCKED, BALANCE "
                        + "FROM sharedtickets WHERE id =\""+ realId +"\" OR id like \""+ idString +"\" ORDER BY id"
                , null
                , new SerializerReadClass(SharedTicketInfo.class)).list();
    }

    public final int getSharedTicketCountById(final String id) throws BasicException {

        String[] temp;
        String idString = "";
        String realId = "";
        if (id.contains("[")) {
            temp = id.split("\\[");
            idString = "%" + temp[0] + "%";
            realId = temp[0];
        } else {
            idString = "%" + id + "%";
            realId = id;
        }

        List<SharedTicketInfo> list = new StaticSentence(s
                , "SELECT ID, NAME, CONTENT, APPUSER, PICKUPID, LOCKED, BALANCE "
                        + "FROM sharedtickets WHERE id like \""+ idString +"\" ORDER BY id"
                , null
                , new SerializerReadClass(SharedTicketInfo.class)).list();

        return list.size();
    }
    
    /**
     * Return only current APPUSER SHAREDtickets
     * @param appuser
     * @return
     * @throws BasicException
     */
    public final List<SharedTicketInfo> getUserSharedTicketList(String appuser) throws BasicException {
        String sql = "SELECT ID, NAME, CONTENT, APPUSER, PICKUPID, LOCKED, BALANCE " 
                + "FROM sharedtickets "
                + "WHERE APPUSER =\""+ appuser +"\" ORDER BY PICKUPID";
        
            return (List<SharedTicketInfo>) new StaticSentence(s
            , sql
            , null
            , new SerializerReadClass(SharedTicketInfo.class)).list();
    }
    
    /**
     * For Standard View
     * @param id
     * @param ticket
     * @param pickupid
     * @throws BasicException
     */
    public final void insertSharedTicket(final String id, final TicketInfo ticket, int pickupid) throws BasicException {
        LocalDateTime now = LocalDateTime.now();

        Object[] values = new Object[] {
            id, 
            ticket.getName(null), 
            ticket,
            ticket.getUser().getId(),
            pickupid,
            Timestamp.valueOf(now)
        };
        Datas[] datas;
        datas = new Datas[] {
            Datas.STRING, 
            Datas.STRING, 
            Datas.SERIALIZABLE, 
            Datas.STRING,            
            Datas.INT,
            Datas.TIMESTAMP

        };
        new PreparedSentence(s
            , "INSERT INTO sharedtickets ("
                + "ID, "
//                + "NAME, "
                + "CONTENT, "
                + "APPUSER, "                    
                + "PICKUPID, "
                + "OPENEDTIME) "
                + "VALUES (?, ?, ?, ?, ?)"        
            , new SerializerWriteBasicExt(datas, new int[] {0, 2, 3, 4, 5})).exec(values);                
    }
    
    /**
     *
     * @param id
     * @param ticket
     * @param pickupid
     * @throws BasicException
     */
    public final void updateSharedTicket(final String id, final TicketInfo ticket, int pickupid) throws BasicException {

        Object[] values = new Object[] {
            id, 
            ticket.getName(), 
            ticket,
            ticket.getUser().getId(),
            pickupid,
        };
        Datas[] datas = new Datas[] {
            Datas.STRING, 
            Datas.STRING, 
            Datas.SERIALIZABLE, 
            Datas.STRING,
            Datas.INT,
        };

        new PreparedSentence(s
                , "UPDATE sharedtickets SET "
                + "NAME = ?, "
                + "CONTENT = ?, "
//                + "APPUSER = ?, "                        
                + "PICKUPID = ? "
                + "WHERE ID = ?"
                , new SerializerWriteBasicExt(datas, 
                        new int[] {
                            1, 2, 4, 0})).exec(values);
    }

    public final void updateSharedTicketName(final String id, String name) throws BasicException {

        Object[] values = new Object[] {
            id, 
            name,
        };
        Datas[] datas = new Datas[] {
            Datas.STRING, 
            Datas.STRING,
        };

        new PreparedSentence(s
                , "UPDATE sharedtickets SET "
                + "NAME = ? "
                + "WHERE ID = ?"
                , new SerializerWriteBasicExt(datas, 
                        new int[] {
                            1, 0})).exec(values);
    }

    /**
     *
     * @param id
     * @param ticket
     * @param pickupid
     * @throws BasicException
     */
    public final void updateRSharedTicket(
            final String id, 
            final TicketInfo ticket, 
            int pickupid) throws BasicException {
            
        Object[] values = new Object[] {
            id, 
            ticket.getName(), 
            ticket, 
            pickupid
        };
        Datas[] datas = new Datas[] {
            Datas.STRING, 
            Datas.STRING, 
            Datas.SERIALIZABLE, 
            Datas.INT
        };
        new PreparedSentence(s
                , "UPDATE sharedtickets SET "
                + "NAME = ?, "
                + "CONTENT = ?, "
                + "PICKUPID = ? "
                + "WHERE ID = ?"
                , new SerializerWriteBasicExt(datas, new int[] {1, 2, 3, 0})).exec(values);                
    }
    
     /**
     * In place for multi-purposing like containing data from elsewhere and/or
     * using Place and User for Notifications
     * @param id
     * @param locked
     * @throws BasicException
     */
    public final void lockSharedTicket(final String id, final String locked) throws BasicException {

        Object[] values = new Object[] {
            id, 
            locked
        };
        Datas[] datas = new Datas[] {
            Datas.STRING, 
            Datas.STRING
        };
        new PreparedSentence(s
                , "UPDATE sharedtickets SET "
                + "LOCKED = ? "
                + "WHERE ID = ?"
                , new SerializerWriteBasicExt(datas, new int[] {1, 0})).exec(values);
    }

     /**
     * In place for multi-purposing like flushing locks from elsewhere and/or
     * using Place and User for Notifications
     * @param id
     * @param unlocked
     * @throws BasicException
     */
    public final void unlockSharedTicket(final String id, final String unlocked) throws BasicException {
            
        Object[] values = new Object[] {
            id, 
            unlocked
        };
        Datas[] datas = new Datas[] {
            Datas.STRING, 
            Datas.STRING
        };
        new PreparedSentence(s
                , "UPDATE sharedtickets SET "
                + "LOCKED = ? "
                + "WHERE ID = ?"
                , new SerializerWriteBasicExt(datas, new int[] {1, 0})).exec(values);
    }    

    /**
     * In place for multi-purposing like flushing locks from elsewhere and/or
     * using Place and User for Notifications
     * @param id
     * @param unlocked
     * @throws BasicException
     */
    public final void unlockSharedAllTicket(final String unlocked) throws BasicException {
            
        Object[] values = new Object[] {
            unlocked
        };
        Datas[] datas = new Datas[] {
            Datas.STRING, 
        };
        new PreparedSentence(s
                , "UPDATE sharedtickets SET "
                + "LOCKED = ? "
                + "WHERE 1 = 1"
                , new SerializerWriteBasicExt(datas, new int[] {0})).exec(values);
    }    
    
    /**
     * For Restaurant View
     * @param id
     * @param ticket
     * @param pickupid
     * @throws BasicException
     */
    public final void insertRSharedTicket(
            final String id, 
            final TicketInfo ticket, 
            int pickupid) throws BasicException {
        LocalDateTime now = LocalDateTime.now();

        Object[] values = new Object[] {
            id, 
            ticket.getName(null), 
            ticket,
            ticket.getUser(),
            ticket.getPickupId(),
            ticket.getHost(),
            Timestamp.valueOf(now)                         
        };

        Datas[] datas;
        datas = new Datas[] {
            Datas.STRING, 
            Datas.STRING, 
            Datas.SERIALIZABLE, 
            Datas.STRING,            
            Datas.INT,
            Datas.TIMESTAMP
        };
        new PreparedSentence(s
            , "INSERT INTO sharedtickets ("
                + "ID, "
//                + "NAME, "
                + "CONTENT, "
                + "APPUSER, "                    
                + "PICKUPID, "
                + "OPENEDTIME) "
                + "VALUES (?, ?, ?, ?, ?)"                
            , new SerializerWriteBasicExt(datas, new int[] {0, 2, 3, 4, 5})).exec(values);                
    }    

    /**
     * 
     * @param id
       @param balance
     * @throws BasicException
     */
    public final void deleteSharedTicket(final String id) throws BasicException {
        new StaticSentence(s
            , "DELETE FROM sharedtickets WHERE ID = ?"
            , SerializerWriteString.INSTANCE).exec(id);
    }

    public final void deleteClosedTicket(final String id) throws BasicException {
        new StaticSentence(s
            , "DELETE FROM closedtickets WHERE ID = ?"
            , SerializerWriteString.INSTANCE).exec(id);
    }

    public final void deletePayments(final String id) throws BasicException {
        Object[] temp = (Object[]) new StaticSentence(s
            , "SELECT id FROM tickets WHERE ticketid = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {
                Datas.STRING
            })).find(id);

        String receiptID = temp[0].toString();
        String ticketID = id;

        new StaticSentence(s
            , "DELETE FROM tickets WHERE ticketid = ?"
            , SerializerWriteString.INSTANCE).exec(ticketID);

        new StaticSentence(s
            , "DELETE FROM ticketlines WHERE ticket = ?"
            , SerializerWriteString.INSTANCE).exec(receiptID);

        new StaticSentence(s
            , "DELETE FROM receipts WHERE id = ?"
            , SerializerWriteString.INSTANCE).exec(receiptID);

        new StaticSentence(s
            , "DELETE FROM taxlines WHERE receipt = ?"
            , SerializerWriteString.INSTANCE).exec(receiptID);

        new StaticSentence(s
            , "DELETE FROM payments WHERE receipt = ?"
            , SerializerWriteString.INSTANCE).exec(receiptID);
    }

    public final void closeSharedTicket(final String id) throws BasicException {
        TicketInfo currentTicket = getSharedTicket(id);

        Datas[] datas;
        datas = new Datas[] {
            Datas.STRING, 
            Datas.STRING, 
            Datas.STRING, 
            Datas.TIMESTAMP,
            Datas.TIMESTAMP,
            Datas.SERIALIZABLE, 
            Datas.STRING,
        };

        LocalDateTime now = LocalDateTime.now();

        Timestamp openedTime = getOpenedTime(id);
        String ticketName = getTicketName(id); 
        String userID = getUserId(id);     

        Object[] values = new Object[] {
            id,
            ticketName,
            userID,
            openedTime,
            Timestamp.valueOf(now),
            getContent(id),
        };

        new PreparedSentence(s
            , "INSERT INTO closedtickets ("
                + "TICKETID, "
                + "TICKETNAME, "
                + "OPENEDBY, "                    
                + "OPENEDAT,"
                + "CLOSEDAT, "
                + "CONTENT) "
                + "VALUES (?, ?, ?, ?, ?, ?)"                
            , new SerializerWriteBasicExt(datas, new int[] {0, 1, 2, 3, 4, 5})).exec(values); 
    }

    public Timestamp getOpenedTime(String ticketid){
        try {
            SentenceFind m_getOpenedTime;
            SerializerRead openedTimeRead;

            openedTimeRead =(DataRead dr) -> (                       
                dr.getString(1));

            m_getOpenedTime = new StaticSentence(s
                , "SELECT openedtime FROM sharedtickets WHERE id = ?"
                , SerializerWriteString.INSTANCE
                , openedTimeRead
            );

            return Timestamp.valueOf(m_getOpenedTime.find(ticketid).toString());
        } catch(BasicException e) {
            return null;
        }
    }

    /**
     *
     * @param Id
     * @return
     * @throws BasicException
     */
    public final Integer getPickupId(String Id) throws BasicException {
        
        if (Id == null) {
            return null; 
        } else {
            Object[]record = (Object[]) new StaticSentence(s
                    , "SELECT PICKUPID FROM sharedtickets WHERE ID = ?"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadBasic(new Datas[] {Datas.INT})).find(Id);
            return record == null ? 0 : (Integer)record[0];
        }
    } 


    public final String getUserId(final String id) throws BasicException {
        Object[] userID = (Object []) new StaticSentence(s
            , "SELECT APPUSER FROM sharedtickets WHERE ID = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.STRING})).find(id);
            if( userID == null ) {
                return null;
            } else {
                return (String) userID[0];
        }
    }

    public final String getTicketName(final String id) throws BasicException {
        Object[] ticketName = (Object []) new StaticSentence(s
            , "SELECT NAME FROM sharedtickets WHERE ID = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.STRING})).find(id);
            if( ticketName == null ) {
                return null;
            } else {
                return (String) ticketName[0];
        }
    }

    public final Serializable getContent(final String id) throws BasicException {
        Object[] content = (Object []) new StaticSentence(s
            , "SELECT CONTENT FROM sharedtickets WHERE ID = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.SERIALIZABLE})).find(id);
            if( content == null ) {
                return null;
            } else {
                return (Serializable) content[0];
        }
    }
    
    public final String getLockState(final String id, String lockState) throws BasicException {
        Object[] state = (Object[]) new StaticSentence(s
            , "SELECT LOCKED FROM sharedtickets WHERE ID = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {
                Datas.STRING
            })).find(id);

            if (state != null)
                return (String) state[0];
            else {
                return null;
            }
    }

    public String getServer(final String id, String user) throws BasicException {
        Object[] server = (Object[]) new StaticSentence(s
            , "SELECT appuser FROM sharedtickets WHERE ID = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {
                Datas.STRING
            })).find(id);

        return (String) server[0];
    }

    public Object[] getClosedPayment(String id) throws BasicException {
        Object[] server = (Object[]) new StaticSentence(s
            , "SELECT payments.payment, payments.cardnum\n" +
                    "FROM closedtickets\n" +
                    "LEFT JOIN tickets ON closedtickets.ticketid = tickets.ticketid\n" +
                    "JOIN receipts ON tickets.id = receipts.id\n" +
                    "JOIN payments ON payments.receipt = receipts.id\n" +
                    "WHERE closedtickets.id = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {
                Datas.STRING,
                Datas.STRING
            })).find(id);

        return server;
    } 

    public Double getClosedTip(String id) throws BasicException {
        Object[] server = (Object[]) new StaticSentence(s
            , "SELECT tips FROM tickets WHERE ticketid = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {
                Datas.DOUBLE
            })).find(id);

        return Double.parseDouble(server[0].toString());
    } 
}
