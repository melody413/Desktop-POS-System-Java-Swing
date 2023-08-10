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

package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.format.Formats;
import com.openbravo.pos.inventory.JobInfo;
import com.openbravo.pos.sales.ClosedTicketInfo;
import com.openbravo.pos.util.ThumbNailBuilder;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.bouncycastle.util.Times;

import javax.imageio.ImageIO;
import javax.swing.*;
/**
 *
 * @author JG uniCenta
 */
public class DataLogicSystem extends BeanFactoryDataSingle {
    
    protected String m_sInitScript;
    private SentenceFind m_version;       
    private SentenceExec m_dummy;
    private String m_dbVersion;
    protected SentenceList m_peoplevisible;  
    protected SentenceList m_clockinVisible;
    protected SentenceFind m_peoplebycard;  
    protected SentenceFind m_peoplebyip;
    protected SerializerRead peopleread;
    protected SentenceList m_permissionlist;
    protected SerializerRead productIdRead;
    protected SerializerRead openCloseRead;
    protected SerializerRead customerIdRead;    
    protected SerializerRead peopleIdRead;
    protected SerializerRead lastOrderTimeRead;
    
    private SentenceFind m_rolepermissions; 
    private SentenceExec m_changepassword;    
    private SentenceFind m_locationfind;
    private SentenceExec m_insertCSVEntry;
    private SentenceExec m_insertStockUpdateEntry;    

    private SentenceFind m_getProductAllFields;
    private SentenceFind m_getProductRefAndCode;    
    private SentenceFind m_getProductRefAndName; 
    private SentenceFind m_getProductCodeAndName;  
    private SentenceFind m_getProductByReference;
    private SentenceFind m_getProductByCode;    
    private SentenceFind m_getProductByName;    

    private SentenceExec m_insertCustomerCSVEntry;    
    private SentenceFind m_getCustomerAllFields;
    private SentenceFind m_getCustomerSearchKeyAndName;    
    private SentenceFind m_getCustomerBySearchKey;
    private SentenceFind m_getCustomerByName;      
    private SentenceFind m_getLastOrderTime;
    private SentenceFind m_getPeopleId;
    private SentenceFind m_resourcebytes;
    private SentenceExec m_resourcebytesinsert;
    private SentenceExec m_resourcebytesupdate;
    private SentenceExec m_closeStore;

    protected SentenceFind m_sequencecash;
    protected SentenceFind m_activecash;
    protected SentenceFind m_closedcash;    
    protected SentenceExec m_insertcash;
    protected SentenceExec m_draweropened;
    protected SentenceExec m_updatepermissions;
    protected SentenceExec m_lineremoved;
    protected SentenceExec m_ticketremoved;  
    protected SentenceExec m_checkin;  
    protected SentenceExec m_checkout;
    protected SentenceExec m_openStore;
    
    private String SQL;    
    private Map<String, byte[]> resourcescache;
    
    private SentenceList m_voucherlist;

    protected SentenceFind m_order;    
    protected SentenceExec m_addOrder;
    protected SentenceExec m_updateOrder;
    protected SentenceExec m_deleteOrder;
    protected SentenceExec m_incompleteOrder;
    
    private SentenceExec m_resetPickup;    
    
    private SentenceExec m_updatePlaces;    
    
    protected SentenceList m_getOrders;

    protected SentenceList m_getOrderByticketid;

    protected SentenceList m_getCompletedticketid;

    protected SentenceList m_getCashOutUserid;

    protected SentenceExec m_updateCompleteTime;

    protected SentenceExec m_updateCompleteTimeForAll;

    protected SentenceFind m_getCountOfOrder;

    protected SentenceFind m_getCountOfOrderFromOpen;

    private SentenceList m_filterNotification;

    private SentenceList m_isOpenStore;

    private SentenceList m_isWhichCash;

    private SentenceList m_totalOrderCash;

    private SentenceList m_totalCash;

    private SentenceList m_totalCashIn;

    private SentenceList m_totalCashOut;

    private SentenceList m_lastCashIn;

    private SentenceList m_totalCC;

    private SentenceList m_paymentSummary;

    private SentenceList m_totalNet;

    private SentenceList m_totalTax;

    private SentenceList m_isPeopleClockIn;

    private SentenceList m_getRemainingTicketLine;

    private SentenceExec m_notificationPeopleData;

    private SentenceExec m_sendNotification;

    protected SentenceList m_getDataOfChangeInfo;

    protected SentenceFind m_getDataOfTable;

    protected SentenceExec m_updateDoneOfChangeInfo;

    protected SentenceList m_getDataOfAttributeSet;

    protected SentenceList m_getDataOfAttributeSetInstance;

    protected SentenceList m_getDataOfCategories;

    protected SentenceList m_getDataOfClosedcash;

    protected SentenceList m_getDataOfTaxcustcategories;

    protected SentenceList m_getDataOfCustomers;

    protected SentenceList m_getDataOfLocations;

    protected SentenceList m_getDataOfReceipts;

    protected SentenceList m_getDataOfPayments;
	
    protected SentenceList m_getEightOrders;

    protected SentenceList m_getNewOrders;
    protected SentenceList m_getUpdatedOrder;
    
    protected SentenceFind m_getCountOfOrderBlock;

    protected Session s;
    /** Creates a new instance of DataLogicSystem */
    public DataLogicSystem() {            
    }
    
    /**
     *
     * @param s
     */
    @Override
    public void init(Session s){
        this.s = s;
        m_sInitScript = "/com/openbravo/pos/scripts/" + s.DB.getName();
        m_dbVersion = s.DB.getName();

        m_version = new PreparedSentence(s, "SELECT VERSION FROM applications WHERE ID = ?"
            , SerializerWriteString.INSTANCE, SerializerReadString.INSTANCE);

        m_dummy = new StaticSentence(s, "SELECT * FROM people WHERE 1 = 0");
         
        final ThumbNailBuilder tnb = new ThumbNailBuilder(32, 32, "com/openbravo/images/user.png");        

        peopleread = (DataRead dr) -> new AppUser(
                dr.getString(1),
                dr.getString(2),
                dr.getString(3),
                dr.getString(4),
                dr.getString(5),
                new ImageIcon(tnb.getThumbNail(ImageUtils.readImage(dr.getBytes(6)))),
                dr.getBoolean(7),
                dr.getString(8));
   
// START OF PRODUCT ************************************************************      
        productIdRead =(DataRead dr) -> (                       
                dr.getString(1)
                );

        openCloseRead =(DataRead dr) -> (                       
                dr.getString(1)
                );

 	m_getProductAllFields = new PreparedSentence(s
		, "SELECT ID FROM products WHERE REFERENCE=? AND CODE=? AND NAME=? "
		, new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING, Datas.STRING})
		, productIdRead
                );
      
       m_getProductRefAndCode  = new PreparedSentence(s
		, "SELECT ID FROM products WHERE REFERENCE=? AND CODE=?"
		, new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING})
		, productIdRead
                );
    
       m_getProductRefAndName  = new PreparedSentence(s
		, "SELECT ID FROM products WHERE REFERENCE=? AND NAME=? "
		, new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING})
		, productIdRead
                );
    
       m_getProductCodeAndName  = new PreparedSentence(s
		, "SELECT ID FROM products WHERE CODE=? AND NAME=? "
		, new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING})
		, productIdRead
                );       

      m_getProductByReference  = new PreparedSentence(s
		, "SELECT ID FROM products WHERE REFERENCE=? "
		, SerializerWriteString.INSTANCE //(Datas.STRING)
		, productIdRead
                ); 
       
      m_getProductByCode  = new PreparedSentence(s
		, "SELECT ID FROM products WHERE CODE=? "
              , SerializerWriteString.INSTANCE //(Datas.STRING)
		//, new SerializerWriteBasic(Datas.STRING)
		, productIdRead
                );

      m_getProductByName  = new PreparedSentence(s
		, "SELECT ID FROM products WHERE NAME=? "
		, SerializerWriteString.INSTANCE //(Datas.STRING)
              //, new SerializerWriteBasic(Datas.STRING)
		, productIdRead
                );     
      
 // END OF PRODUCT *************************************************************

// START OF CUSTOMER ***********************************************************        
        customerIdRead =(DataRead dr) -> (                       
                dr.getString(1)
                );
// duplicate this for now as will extend in future release 
 	m_getCustomerAllFields = new PreparedSentence(s
		, "SELECT ID FROM customers WHERE SEARCHKEY=? AND NAME=? "
		, new SerializerWriteBasic(new Datas[] {
                    Datas.STRING, Datas.STRING})
		, customerIdRead
                );
    
       m_getCustomerSearchKeyAndName  = new PreparedSentence(s
		, "SELECT ID FROM customers WHERE SEARCHKEY=? AND NAME=? "
		, new SerializerWriteBasic(new Datas[] {
                    Datas.STRING, Datas.STRING})
		, customerIdRead
                );      

      m_getCustomerBySearchKey  = new PreparedSentence(s
		, "SELECT ID FROM customers WHERE SEARCHKEY=? "
		, SerializerWriteString.INSTANCE
		, customerIdRead
                ); 

      m_getCustomerByName  = new PreparedSentence(s
		, "SELECT ID FROM customers WHERE NAME=? "
		, SerializerWriteString.INSTANCE
		, customerIdRead
                );     
      
 // END OF CUSTOMER ******************************************************************      
       
        m_peoplevisible = new StaticSentence(s
            , "SELECT ID, NAME, APPPASSWORD, CARD, ROLE, IMAGE, LEFTHAND, STATION_ID FROM people WHERE VISIBLE = " + s.DB.TRUE() + " AND (ROLE = '0' OR EXISTS(SELECT ID FROM leaves WHERE PPLID = people.ID AND STARTDATE IS NOT NULL AND ENDDATE IS NULL )) ORDER BY NAME"
            , null
            , peopleread);

        m_clockinVisible = new StaticSentence(s
            , "SELECT ID, NAME, APPPASSWORD, CARD, ROLE, IMAGE, LEFTHAND, STATION_ID FROM people WHERE VISIBLE = " + s.DB.TRUE() + " AND (EXISTS(SELECT ID FROM leaves WHERE PPLID = people.ID AND STARTDATE IS NOT NULL AND ENDDATE IS NULL )) ORDER BY NAME"
            , null
            , peopleread);

        m_peoplebycard = new PreparedSentence(s
            , "SELECT ID, NAME, APPPASSWORD, CARD, ROLE, IMAGE, LEFTHAND, STATION_ID FROM people WHERE CARD = ? AND VISIBLE = " + s.DB.TRUE()
            , SerializerWriteString.INSTANCE
            , peopleread);

        m_peoplebyip = new PreparedSentence(s
            , "SELECT people.ID, people.NAME, people.APPPASSWORD, people.CARD, people.ROLE, people.IMAGE, people.LEFTHAND, people.STATION_ID FROM people LEFT JOIN stations ON people.STATION_ID = stations.ID WHERE stations.IP = ? AND people.VISIBLE = " + s.DB.TRUE()
            , SerializerWriteString.INSTANCE
            , peopleread);
        m_resourcebytes = new PreparedSentence(s
            , "SELECT CONTENT FROM resources WHERE NAME = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadBytes.INSTANCE);
        
            Datas[] resourcedata = new Datas[] {
            Datas.STRING, Datas.STRING, 
            Datas.INT, Datas.BYTES};
        
        m_resourcebytesinsert = new PreparedSentence(s
                , "INSERT INTO resources(ID, NAME, RESTYPE, CONTENT) VALUES (?, ?, ?, ?)"
                , new SerializerWriteBasic(resourcedata));
        
        m_resourcebytesupdate = new PreparedSentence(s
                , "UPDATE resources SET NAME = ?, RESTYPE = ?, CONTENT = ? WHERE NAME = ?"
                , new SerializerWriteBasicExt(resourcedata, new int[] {1, 2, 3, 1}));
        
        m_rolepermissions = new PreparedSentence(s
                , "SELECT PERMISSIONS FROM roles WHERE ID = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadBytes.INSTANCE);     
        
        m_changepassword = new StaticSentence(s
                , "UPDATE people SET APPPASSWORD = ? WHERE ID = ?"
                ,new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING}));

        m_sequencecash = new StaticSentence(s,
                "SELECT MAX(HOSTSEQUENCE) FROM closedcash WHERE HOST = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadInteger.INSTANCE);
        
        m_activecash = new StaticSentence(s
            , "SELECT HOST, HOSTSEQUENCE, DATESTART, DATEEND, NOSALES FROM closedcash WHERE MONEY = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {
                Datas.STRING, 
                Datas.INT, 
                Datas.TIMESTAMP, 
                Datas.TIMESTAMP,
                Datas.INT}));

        m_closedcash = new StaticSentence(s
            , "SELECT HOST, HOSTSEQUENCE, DATESTART, DATEEND, NOSALES " +
                    "FROM closedcash WHERE HOSTSEQUENCE = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {
                Datas.STRING, 
                Datas.INT, 
                Datas.TIMESTAMP, 
                Datas.TIMESTAMP,
                Datas.INT}));          
        
        m_insertcash = new StaticSentence(s
                , "INSERT INTO closedcash(MONEY, HOST, HOSTSEQUENCE, DATESTART, DATEEND) " +
                  "VALUES (?, ?, ?, ?, ?)"
                , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING, 
                    Datas.STRING, 
                    Datas.INT, 
                    Datas.TIMESTAMP, 
                    Datas.TIMESTAMP}));

        m_draweropened = new StaticSentence(s
                , "INSERT INTO draweropened ( NAME, TICKETID) " +
                  "VALUES (?, ?)"
                , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING, 
                    Datas.STRING}));       
        
        m_lineremoved = new StaticSentence(s,
                "INSERT INTO lineremoved (NAME, TICKETID, PRODUCTID, PRODUCTNAME, UNITS) " +
                "VALUES (?, ?, ?, ?, ?)",
                new SerializerWriteBasic(new Datas[] {
                    Datas.STRING, Datas.STRING, 
                    Datas.STRING, Datas.STRING, 
                    Datas.DOUBLE
                }));

        m_ticketremoved = new StaticSentence(s,
                "INSERT INTO lineremoved (NAME, TICKETID, PRODUCTNAME, UNITS) " +
                "VALUES (?, ?, ?, ?)",
                new SerializerWriteBasic(new Datas[] {
                    Datas.STRING, Datas.STRING,
                    Datas.STRING, Datas.DOUBLE                    
                }));        
        
        m_locationfind = new StaticSentence(s
                , "SELECT NAME FROM locations WHERE ID = ?"
                , SerializerWriteString.INSTANCE
                , SerializerReadString.INSTANCE);   
        
        m_permissionlist = new StaticSentence(s
                , "SELECT PERMISSIONS FROM permissions WHERE ID = ?"
                , SerializerWriteString.INSTANCE
                , new SerializerReadBasic(new Datas[] {
                    Datas.STRING
                }));         
         
        m_updatepermissions = new StaticSentence(s
                , "INSERT INTO permissions (ID, PERMISSIONS) " +
                  "VALUES (?, ?)"
                , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING, 
                    Datas.STRING})); 
        
        
//  Push Products into CSVImport table      
        m_insertCSVEntry = new StaticSentence(s
                , "INSERT INTO csvimport ( "
                        + "ID, ROWNUMBER, CSVERROR, REFERENCE, "
                        + "CODE, NAME, PRICEBUY, PRICESELL, "
                        + "PREVIOUSBUY, PREVIOUSSELL, CATEGORY, TAX) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING,
                    Datas.DOUBLE,
                    Datas.DOUBLE,
                    Datas.DOUBLE,
                    Datas.DOUBLE,
                    Datas.STRING,                    
                    Datas.STRING
                }));

//  Push Product Quantity Update into CSVImport table      
        m_insertStockUpdateEntry = new StaticSentence(s
                , "INSERT INTO csvimport ( "
                        + "ID, ROWNUMBER, CSVERROR, REFERENCE, "
                        + "CODE, PRICEBUY ) " 
                        + "VALUES (?, ?, ?, ?, ?, ?)"
                , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING,                    
                    Datas.DOUBLE
                }));
        
//  Push Customers into CSVImport table      
        m_insertCustomerCSVEntry = new StaticSentence(s
                , "INSERT INTO csvimport ( "
                        + "ID, ROWNUMBER, CSVERROR, SEARCHKEY, NAME) " +
                  "VALUES (?, ?, ?, ?, ?)"
                , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING
                }));

        m_order = new StaticSentence(s,
                "SELECT LINEKEY FROM orders WHERE TICKETID = ? AND LINEKEY = ?",
                new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING}),
                SerializerReadString.INSTANCE);   
  
        m_addOrder =  new StaticSentence(s
                , "INSERT INTO orders (ORDERID, QTY, DETAILS, ATTRIBUTES, "
                        + "NOTES, TICKETID, ORDERTIME, DISPLAYID, AUXILIARY, "
                        + "COMPLETETIME, LINEKEY, STATION_ID, UPDATEDTIME) " +
                  "VALUES (?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, ?, ? ) "
                , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING,   // OrderId
                    Datas.INT,      // Qty
                    Datas.STRING,   // Details
                    Datas.STRING,   // Attributes
                    Datas.STRING,   // Notes
                    Datas.STRING,   // TicketId
                    Datas.TIMESTAMP,   // OrderTime
                    Datas.INT,      // DisplayId
                    Datas.STRING,      // Auxiliary
                    Datas.STRING,    // CompleteTime
                    Datas.STRING,    // LineKey
                    Datas.STRING,    // StationId
                    Datas.STRING    // UpdatedTime
                })); 

        m_updateOrder =  new StaticSentence(s
                , "UPDATE orders SET "
                    + "ORDERID = ?, "
                    + "QTY = ?, "
                    + "DETAILS = ?, "
                    + "ATTRIBUTES = ?, "
                    + "NOTES = ?, "
                    + "TICKETID = ?, "
                    + "ORDERTIME = ?, "
                    + "DISPLAYID = ?, "
                    + "AUXILIARY = ?, "
                    + "COMPLETETIME = ? "
                    + "UPDATEDTIME = ? "
                    + "WHERE ORDERID = ? "
                , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING,   // OrderId
                    Datas.INT,      // Qty
                    Datas.STRING,   // Details
                    Datas.STRING,   // Attributes
                    Datas.STRING,   // Notes
                    Datas.STRING,   // TicketId
                    Datas.STRING,   // OrderTime
                    Datas.INT,      // DisplayId
                    Datas.INT,      // Auxiliary
                    Datas.STRING,    // CompleteTime
                    Datas.STRING    // UpdatedTime
                }));

        m_deleteOrder =  new StaticSentence(s
                    , "DELETE FROM orders WHERE ORDERID = ?"
                    , SerializerWriteString.INSTANCE);

        m_incompleteOrder =  new StaticSentence(s
                    , "UPDATE orders SET COMPLETETIME = null, DISPLAYID = 1, UPDATEDTIME = ?, REFIREDTIME = ? WHERE ID = ?"
                    , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING,    // UpdatedTime
                    Datas.STRING,    // RefiredTime
                    Datas.STRING,    // ID
                }));

        m_getUpdatedOrder = new StaticSentence(s
                ,"SELECT TICKETID, ORDERTIME FROM orders, stations WHERE UPDATEDTIME > ? AND orders.station_id = stations.id"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.TIMESTAMP,
                        Datas.STRING
                })
                ,new SerializerReadBasic(new Datas[]{
                        Datas.STRING,
                        Datas.TIMESTAMP
        }));
        
        m_updatePlaces = new StaticSentence(s
                , "UPDATE places SET X = ?, Y = ? "
                + "WHERE ID = ? "
                , new SerializerWriteBasic(new Datas[]{
            Datas.INT,
            Datas.INT,
            Datas.STRING
        })); 
        
        m_resetPickup =  new StaticSentence(s
                , "UPDATE pickup_number SET id=1"
                , SerializerWriteInteger.INSTANCE);         
		
	m_getOrders = new PreparedSentence(s
                , "SELECT TICKETID FROM orders WHERE DISPLAYID = " + s.DB.TRUE() + " WHERE STATION_ID = ? GROUP BY TICKETID"
                , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING
                    })
                , new SerializerReadBasic(new Datas[] {
                    Datas.STRING
                    })
                );

        m_getOrderByticketid = new StaticSentence(s
                , "SELECT ID, ORDERID, DETAILS, ATTRIBUTES, NOTES, ORDERTIME, COMPLETETIME, QTY FROM orders WHERE orderid = ? and ordertime = Timestamp(?)"
                , new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING
                    })
                , new SerializerReadBasic(new Datas[] {
                    Datas.INT,
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING,
                    Datas.STRING,
                    Datas.TIMESTAMP,
                    Datas.TIMESTAMP,
                    Datas.STRING}));

        m_getCompletedticketid = new StaticSentence(s
                , "SELECT DISTINCT TICKETID, ORDERID, NOTES, ORDERTIME FROM orders WHERE COMPLETETIME IS NOT NULL AND DISPLAYID = 0 and ordertime >= Timestamp(?) "
                , SerializerWriteString.INSTANCE
                , new SerializerReadBasic(new Datas[] {
                Datas.STRING,
                Datas.STRING,
                Datas.STRING,
                Datas.TIMESTAMP}));

        m_getCashOutUserid = new StaticSentence(s
                , "SELECT DISTINCT TICKETID, ORDERID, NOTES, ORDERTIME FROM orders WHERE COMPLETETIME IS NOT NULL AND DISPLAYID = 0 "
                , new SerializerWriteBasic()
                , new SerializerReadBasic(new Datas[] {
                Datas.STRING,
                Datas.STRING,
                Datas.STRING,
                Datas.TIMESTAMP}));

        m_updateCompleteTime = new StaticSentence(s,
                "UPDATE orders SET " +
                        "COMPLETETIME = ?, " +
                        "UPDATEDTIME = ? " +
                        "WHERE ID = ? "
                , new SerializerWriteBasic(new Datas[] {
                Datas.TIMESTAMP,    // CompleteTime
                Datas.TIMESTAMP,    // UpdatedTime
                Datas.STRING
        }));

        m_updateCompleteTimeForAll = new StaticSentence(s,
                "UPDATE orders SET " +
                        "DISPLAYID = 0, " +
                        "UPDATEDTIME = ?, " +
                        "REFIREDTIME = ? " +
                        "WHERE ORDERID = ? AND ORDERTIME = ? "
                , new SerializerWriteBasic(new Datas[]{
                Datas.TIMESTAMP,
                Datas.TIMESTAMP,
                Datas.STRING,
                Datas.TIMESTAMP
        }));

        m_getCountOfOrder = new StaticSentence(s,
                "SELECT COUNT(*) FROM orders WHERE TICKETID = ? AND COMPLETETIME IS NULL"
                , SerializerWriteString.INSTANCE
                , SerializerReadInteger.INSTANCE);

        m_getCountOfOrderFromOpen = new StaticSentence(s,
                "SELECT COUNT(DISTINCT(orderid)) FROM orders WHERE ORDERTIME >= TIMESTAMP(?)"
                , SerializerWriteString.INSTANCE
                , SerializerReadInteger.INSTANCE);



        m_getRemainingTicketLine = new StaticSentence(s,
                "SELECT details FROM orders WHERE TICKETID = ? AND COMPLETETIME IS NULL",
                SerializerWriteString.INSTANCE,
                SerializerReadString.INSTANCE);


        m_notificationPeopleData = new StaticSentence(s
                , "INSERT INTO notification_peoples (notification_id, people_id) " +
                "VALUES (?, ?)"
                , new SerializerWriteBasic(new Datas[] {
                Datas.STRING,
                Datas.STRING}));

        m_filterNotification = new PreparedSentence(s
                ,"SELECT * FROM notifications WHERE (people_id is null or people_id = ?) and id Not in ( Select" +
                " notification_id from notification_peoples WHERE people_id = ?)"
                ,new SerializerWriteBasic(new Datas[]{
                        Datas.STRING,
                        Datas.STRING
                })
                ,new SerializerReadBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.INT,
                        Datas.STRING
                }));

        m_isOpenStore = new PreparedSentence(s
                ,"SELECT * FROM open_close ORDER BY id DESC LIMIT 1;"
                ,new SerializerWriteBasic(new Datas[]{})
                ,new SerializerReadBasic(new Datas[] {
                        Datas.INT,
                        Datas.STRING,
                        Datas.STRING
                }));

        m_isWhichCash = new PreparedSentence(s
                ,"select payments.payment from receipts, payments where payments.receipt = receipts.id and (payments.payment = 'cashin' or payments.payment = 'cashout') and receipts.datenew >= TIMESTAMP(?) ORDER BY receipts.datenew DESC LIMIT 1;"
                ,SerializerWriteString.INSTANCE
                ,SerializerReadString.INSTANCE);

        m_totalOrderCash = new PreparedSentence(s
                ,"select sum(payments.total) from receipts, payments where payments.receipt = receipts.id and payments.payment = 'cash' and receipts.datenew >= TIMESTAMP(?);"
                ,SerializerWriteString.INSTANCE
                ,SerializerReadString.INSTANCE);

        m_totalCash = new PreparedSentence(s
                ,"select sum(payments.total) from receipts, payments where payments.receipt = receipts.id and payments.payment = 'cash' and receipts.datenew >= TIMESTAMP(?);"
                ,SerializerWriteString.INSTANCE
                ,SerializerReadString.INSTANCE);

        m_totalCashIn = new PreparedSentence(s
                ,"select sum(payments.total) from receipts, payments where payments.receipt = receipts.id and (payments.payment = 'cashin' or payments.payment = 'cash') and receipts.datenew >= TIMESTAMP(?);"
                ,SerializerWriteString.INSTANCE
                ,SerializerReadString.INSTANCE);

        m_totalCashOut = new PreparedSentence(s
                ,"select sum(payments.total) from receipts, payments where payments.receipt = receipts.id and payments.payment = 'cashout' and receipts.datenew >= TIMESTAMP(?);"
                ,SerializerWriteString.INSTANCE
                ,SerializerReadString.INSTANCE);

        m_totalNet = new PreparedSentence(s
                ,"SELECT SUM(taxlines.AMOUNT), SUM(taxlines.BASE) FROM receipts, taxlines WHERE receipts.ID = taxlines.RECEIPT and receipts.datenew >= TIMESTAMP(?);"
                ,SerializerWriteString.INSTANCE
                ,new SerializerReadBasic(new Datas[] {
                        Datas.DOUBLE,
                        Datas.DOUBLE
                }));

        m_totalTax = new PreparedSentence(s
                ,"SELECT SUM(ticketlines.UNITS * ticketlines.PRICE) FROM receipts, ticketlines WHERE receipts.ID = ticketlines.TICKET and receipts.datenew >= TIMESTAMP(?);"
                ,SerializerWriteString.INSTANCE
                ,SerializerReadString.INSTANCE);

        m_lastCashIn = new PreparedSentence(s
                    ,"SELECT payments.notes, payments.total / payments.notes, payments.total, receipts.datenew FROM receipts, payments "
                        + "WHERE payments.receipt = receipts.id AND payments.payment = 'cashin' AND receipts.datenew = (SELECT MAX(receipts.datenew) "
                        + "FROM receipts, payments WHERE payments.receipt = receipts.id AND payments.payment = 'cashin') "
                        + "AND receipts.datenew >= TIMESTAMP(?) ORDER BY receipts.datenew;"
                ,SerializerWriteString.INSTANCE
                ,new SerializerReadBasic(new Datas[] {
                        Datas.INT,
                        Datas.INT,
                        Datas.INT,
                        Datas.STRING
                }));

        m_totalCC = new PreparedSentence(s
                ,"select sum(payments.total), sum(payments.tip) from receipts, payments where payments.receipt = receipts.id and payments.payment not like '%cash%' and receipts.datenew >= TIMESTAMP(?);"
                ,SerializerWriteString.INSTANCE
                ,new SerializerReadBasic(new Datas[] {
                        Datas.DOUBLE,
                        Datas.DOUBLE
                }));

        m_paymentSummary = new PreparedSentence(s
                ,"select payments.payment, ROUND(sum(payments.total), 2) as total, sum(payments.tip) as tip from receipts, payments where payments.receipt = receipts.id and payments.payment != 'cashin' and payments.payment != 'cashout' and receipts.datenew >= TIMESTAMP(?) GROUP BY payments.payment;"
                ,SerializerWriteString.INSTANCE
                ,new SerializerReadBasic(new Datas[] {
                        Datas.STRING,
                        Datas.DOUBLE,
                        Datas.DOUBLE
                }));

        m_isPeopleClockIn = new PreparedSentence(s
                ,"SELECT ID, STARTDATE, ENDDATE FROM leaves WHERE pplid = ? ORDER BY STARTDATE DESC LIMIT 1;"
                ,SerializerWriteString.INSTANCE
                ,new SerializerReadBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING
                }));

        m_sendNotification = new PreparedSentence(s,
                "INSERT INTO notifications VALUES (?,?,?,?,?,?)",
                new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.INT,
                        Datas.STRING,
                        Datas.TIMESTAMP
                }));

        m_getDataOfChangeInfo = new StaticSentence(s,
                "SELECT * FROM change_info WHERE DONE = FALSE ORDER BY TIME",
                null,
                new SerializerReadBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING,
                        Datas.TIMESTAMP,
                        Datas.STRING
                }));

        m_updateDoneOfChangeInfo = new PreparedSentence(s,
                "UPDATE change_info SET DONE = TRUE WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                null);



        m_getDataOfCustomers = new StaticSentence(s
                ,"SELECT * FROM customers WHERE ID = ?"
                , SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.DOUBLE,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.BOOLEAN,
                        Datas.TIMESTAMP,
                        Datas.DOUBLE,
                        Datas.IMAGE,
                        Datas.BOOLEAN,
                        Datas.DOUBLE,
                        Datas.TIMESTAMP
                }));

        m_getDataOfAttributeSet = new StaticSentence(s,
                "SELECT * FROM attributeset WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING
                }));

        m_getDataOfAttributeSetInstance = new StaticSentence(s,
                "SELECT * FROM attributesetinstance WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING
                }));

        m_getDataOfCategories = new StaticSentence(s,
                "SELECT * FROM categories WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.IMAGE,
                        Datas.STRING,
                        Datas.INT,
                        Datas.STRING,
                        Datas.INT
                }));

        m_getDataOfClosedcash = new StaticSentence(s,
                "SELECT * FROM closedcash WHERE MONEY = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[]{
                        Datas.STRING,
                        Datas.STRING,
                        Datas.INT,
                        Datas.TIMESTAMP,
                        Datas.TIMESTAMP,
                        Datas.INT
                }));

        m_getDataOfTaxcustcategories = new StaticSentence(s,
                "SELECT * FROM taxcustcategories WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[]{
                        Datas.STRING,
                        Datas.STRING
                }));

        m_getDataOfLocations = new StaticSentence(s,
                "SELECT * FROM locations WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[]{
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING
                }));

        m_getDataOfReceipts = new StaticSentence(s,
                "SELECT * FROM receipts WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[]{
                        Datas.STRING,
                        Datas.STRING,
                        Datas.TIMESTAMP,
                        Datas.IMAGE,
                        Datas.STRING
                }));

        m_getDataOfPayments = new StaticSentence(s,
                "SELECT * FROM payments WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[]{
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.DOUBLE,
                        Datas.DOUBLE,
                        Datas.STRING,
                        Datas.BOOLEAN,
                        Datas.IMAGE,
                        Datas.STRING,
                        Datas.DOUBLE,
                        Datas.STRING,
                        Datas.STRING
                }));

        m_getEightOrders = new StaticSentence(s
                ,"SELECT TICKETID, ORDERTIME FROM orders WHERE DISPLAYID ="+ s.DB.TRUE() + " AND STATION_ID = ? GROUP BY TICKETID, ORDERTIME ORDER BY " +
                " ORDERTIME, TICKETID LIMIT ?,8"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.INT
                })
                ,new SerializerReadBasic(new Datas[]{
                        Datas.STRING,
                        Datas.TIMESTAMP
        }));

        m_getNewOrders = new StaticSentence(s
                ,"SELECT TICKETID, ORDERTIME, ORDERID FROM orders, stations, sharedtickets WHERE DISPLAYID ="+ s.DB.TRUE() + " AND stations.NAME = 'Kitchen Out' AND ORDERTIME > ? "
                + "AND orders.station_id = stations.id and sharedtickets.id = orders.ticketid GROUP BY TICKETID, ORDERID, ORDERTIME ORDER BY REFIREDTIME desc, "
                + "ORDERTIME"
                , new SerializerWriteBasic(new Datas[] {
                        Datas.TIMESTAMP,
                        Datas.STRING,
                })
                , new SerializerReadBasic(new Datas[]{
                        Datas.STRING,
                        Datas.TIMESTAMP,
                        Datas.STRING
        }));
        lastOrderTimeRead =(DataRead dr) -> (                       
                dr.getString(1)
                );
        m_getLastOrderTime = new StaticSentence(s
                ,"SELECT IFNULL(MAX(ORDERTIME), NOW()) FROM orders WHERE STATION_ID LIKE ?"
                ,SerializerWriteString.INSTANCE
                ,lastOrderTimeRead
                );

        m_getCountOfOrderBlock = new StaticSentence(s
                ,"SELECT COUNT(DISTINCT(ticketid)) FROM orders WHERE DISPLAYID ="+ s.DB.TRUE()
                ,null
                ,SerializerReadInteger.INSTANCE);

        peopleIdRead =(DataRead dr) -> (                       
                dr.getString(1)
                );
 	m_getPeopleId = new PreparedSentence(s
		, "SELECT ID FROM people WHERE EMPLOYEE_NO=?"
		, SerializerWriteString.INSTANCE
		, peopleIdRead
                );
        m_checkin = new StaticSentence(s
                , "INSERT INTO leaves(ID, PPLID, NAME, STARTDATE, JOB_ID) " +
                  "VALUES (?, ?, ?, ?, ?)"
                , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING, 
                    Datas.STRING, 
                    Datas.STRING, 
                    Datas.TIMESTAMP,
                    Datas.STRING}));
        m_checkout = new StaticSentence(s
                , "UPDATE leaves SET ENDDATE = ? WHERE id = ? AND STARTDATE IS NOT NULL"
                , new SerializerWriteBasic(new Datas[] {
                    Datas.TIMESTAMP,
                    Datas.STRING}));

        m_openStore = new StaticSentence(s
                , "INSERT INTO open_close(OPEN) " +
                  "VALUES (?)"
                , SerializerWriteString.INSTANCE);

        m_closeStore = new PreparedSentence(s
                , "UPDATE open_close SET CLOSE = ? where ID = ?"
                , new SerializerWriteBasic(new Datas[] {
                    Datas.STRING,
                    Datas.INT}));

	resetResourcesCache();      
    }

    /**
     *
     * @return
     */
    public String getInitScript() {
        return m_sInitScript;
    }
    
    /**
     *
     * @return
     */
    public String getDBVersion(){
        return m_dbVersion;        
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    public final String findVersion() throws BasicException {
        return (String) m_version.find(AppLocal.APP_ID);
    }
    
    /**
     *
     * @return
     * @throws BasicException
     */
    public final String getUser() throws BasicException {
        return ("");
        
    }

    /**
     *
     * @throws BasicException
     */
    public final void execDummy() throws BasicException {
        m_dummy.exec();
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    public final List listPeopleVisible() throws BasicException {
        return m_peoplevisible.list();
    }

    public final List listClockinVisible() throws BasicException {
        return m_clockinVisible.list();
    }

    /**
     *
     * @param role
     * @return
     * @throws BasicException
     */
    public final List<String> getPermissions(String role)throws BasicException {         
        return m_permissionlist.list(role);
    }
    
    /**
     *
     * @param card
     * @return
     * @throws BasicException
     */
    public final AppUser findPeopleByCard(String card) throws BasicException {
        return (AppUser) m_peoplebycard.find(card);
    }

    /**
     *
     * @param card
     * @return
     * @throws BasicException
     */
    public final AppUser findPeopleByIp(String ip) throws BasicException {
        return (AppUser) m_peoplebyip.find(ip);
    }
    /**
     *
     * @param sRole
     * @return
     */
    public final String findRolePermissions(String sRole) {
        
        try {
            return Formats.BYTEA.formatValue(m_rolepermissions.find(sRole));        
        } catch (BasicException e) {
            return null;                    
        }             
    }
    
    /**
     *
     * @param userdata
     * @throws BasicException
     */
    public final void execChangePassword(Object[] userdata) throws BasicException {
        m_changepassword.exec(userdata);
    }
    
    /**
     *
     */
    public final void resetResourcesCache() {
        resourcescache = new HashMap<>();      
    }
    
    private byte[] getResource(String name) {

        byte[] resource;
        
        resource = resourcescache.get(name);
        
        if (resource == null) {       
            try {
                resource = (byte[]) m_resourcebytes.find(name);
                resourcescache.put(name, resource);
            } catch (BasicException e) {
                resource = null;
            }
        }
        
        return resource;
    }
    
    /**
     *
     * @param name
     * @param type
     * @param data
     */
    public final void setResource(String name, int type, byte[] data) {
        
        Object[] value = new Object[] {
            UUID.randomUUID().toString(), 
            name, 
            type, 
            data
        };
        try {
            if (m_resourcebytesupdate.exec(value) == 0) {
                m_resourcebytesinsert.exec(value);
            }
            resourcescache.put(name, data);
        } catch (BasicException e) {
        }
    }
    
    /**
     *
     * @param sName
     * @param data
     */
    public final void setResourceAsBinary(String sName, byte[] data) {
        setResource(sName, 0, data);
    }
    
    /**
     *
     * @param sName
     * @return
     */
    public final byte[] getResourceAsBinary(String sName) {
        return getResource(sName);
    }
    
    /**
     *
     * @param sName
     * @return
     */
    public final String getResourceAsText(String sName) {
        return Formats.BYTEA.formatValue(getResource(sName));
    }
    
    /**
     *
     * @param sName
     * @return
     */
    public final String getResourceAsXML(String sName) {
        return Formats.BYTEA.formatValue(getResource(sName));
    }

    /**
     *
     * @param sName
     * @return
     */
    public final BufferedImage getResourceAsImage(String sName) {
        try {
            byte[] img = getResource(sName); // , ".png"
            return img == null ? null : ImageIO.read(new ByteArrayInputStream(img));
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     *
     * @param sName
     * @param p
     */
    public final void setResourceAsProperties(String sName, Properties p) {
        if (p == null) {
            setResource(sName, 0, null); // texto
        } else {
            try {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                p.storeToXML(o, AppLocal.APP_NAME, "UTF8");
                setResource(sName, 0, o.toByteArray()); // El texto de las propiedades   
            } catch (IOException e) { // no deberia pasar nunca
            }            
        }
    }
    
    /**
     *
     * @param sName
     * @return
     */
    public final Properties getResourceAsProperties(String sName) {
        
        Properties p = new Properties();
        try {
            byte[] img = getResourceAsBinary(sName);
            if (img != null) {
                p.loadFromXML(new ByteArrayInputStream(img));
            }
        } catch (IOException e) {
        }
        return p;
    }

    /**
     *
     * @param host
     * @return
     * @throws BasicException
     */
    public final int getSequenceCash(String host) throws BasicException {
        Integer i = (Integer) m_sequencecash.find(host);
        return (i == null) ? 1 : i;
    }

    /**
     *
     * @param sActiveCashIndex
     * @return
     * @throws BasicException
     */
    public final Object[] findActiveCash(String sActiveCashIndex) throws BasicException {
        return (Object[]) m_activecash.find(sActiveCashIndex);
    }
    /**
     *
     * @param sClosedCashIndex
     * @return
     * @throws BasicException
     */
    public final Object[] findClosedCash(String sClosedCashIndex) throws BasicException {
        return (Object[]) m_activecash.find(sClosedCashIndex);
    }
    
    
    /**
     *
     * @param cash
     * @throws BasicException
     */
    public final void execInsertCash(Object[] cash) throws BasicException {
        m_insertcash.exec(cash);
    }

    /**
     *
     * @param drawer
     * @throws BasicException
     */
    public final void execDrawerOpened(Object[] drawer) throws BasicException {
        m_draweropened.exec(drawer);
    }

    /**
     *
     * @param permissions
     * @throws BasicException
     */
    public final void execUpdatePermissions(Object[] permissions) throws BasicException {
        m_updatepermissions.exec(permissions);
    }

    /**
     *
     * @param line
     */
    public final void execLineRemoved(Object[] line) {
        try {
            m_lineremoved.exec(line);
        } catch (BasicException e) {
        }
    }
    
    /**
     *
     * @param ticket
     */
    public final void execTicketRemoved(Object[] ticket) {
        try {
            m_ticketremoved.exec(ticket);
        } catch (BasicException e) {
        }
    }    
    
    /**
     *
     * @param iLocation
     * @return
     * @throws BasicException
     */
    public final String findLocationName(String iLocation) throws BasicException {
        return (String) m_locationfind.find(iLocation);
    }

    /**
     *
     * @param csv
     * @throws BasicException
     */
    public final void execCSVStockUpdate(Object[] csv) throws BasicException {
        m_insertStockUpdateEntry.exec(csv);
        
    }
    /**
     *
     * @param csv
     * @throws BasicException
     */
    public final void execAddCSVEntry(Object[] csv) throws BasicException {
        m_insertCSVEntry.exec(csv);
        
    }    
    
    /**
     *
     * @param csv
     * @throws BasicException
     */
    public final void execCustomerAddCSVEntry(Object[] csv) throws BasicException {
        m_insertCustomerCSVEntry.exec(csv);
        
    }    
    
   
// This is used by CSVimport to detect what type of product insert we are looking at, or what error occured

    /**
     *
     * @param myProduct
     * @return
     * @throws BasicException
     */
        public final String getProductRecordType(Object[] myProduct) throws BasicException {
        // check if the product exist with all the details, if so return product ID
        if (m_getProductAllFields.find(myProduct) != null){
            return m_getProductAllFields.find(myProduct).toString();
        } 
        // check if the product exists with matching reference and code, but a different name
        if (m_getProductRefAndCode.find(myProduct[0],myProduct[1]) != null){
            return "Name change";
        }
        
       if (m_getProductRefAndName.find(myProduct[0],myProduct[2]) != null){
            return "Barcode change";
        }

       if (m_getProductCodeAndName.find(myProduct[1],myProduct[2]) != null){
            return "Reference change";
        }
        
       if (m_getProductByReference.find(myProduct[0]) != null){
            return "Duplicate Reference found.";
        }       

       if (m_getProductByCode.find(myProduct[1]) != null){
            return "Duplicate Barcode found.";
        }
       
       if (m_getProductByName.find(myProduct[2]) != null){
            return "Duplicate Description found.";
        }       
       
       return "new";
    } 

    /**
     *
     * @param myCustomer
     * @return
     * @throws BasicException
    */
    public final String getCustomerRecordType(Object[] myCustomer) throws BasicException {

        if (m_getCustomerAllFields.find(myCustomer) != null){
            return m_getCustomerAllFields.find(myCustomer).toString();
        } 

        if (m_getCustomerSearchKeyAndName.find(myCustomer[0],myCustomer[1]) != null){
            return "reference error";
        }
        
        if (m_getCustomerBySearchKey.find(myCustomer[0]) != null){
            return "Duplicate Search Key found.";
        }       
       
        if (m_getCustomerByName.find(myCustomer[1]) != null){
            return "Duplicate Name found.";
        }       
       
        return "new";
    } 

    public final void updatePlaces(int x, int y, String id) throws BasicException {
        m_updatePlaces.exec(x, y, id);
    }

    public final void resetPickup(int x) throws BasicException {
        m_resetPickup.exec(x);
    }    
        
    public final SentenceList getVouchersActiveList() {
        return m_voucherlist;
    } 

    public final void addOrder(String orderId, Integer qty, 
            String details, String attributes, String notes, String ticketId, 
            Timestamp ordertime, Integer displayId, String auxiliary, String completetime, String linekey, String stationId
        ) throws BasicException {
//        if (m_order.find(ticketId, linekey) == null) {
            LocalDateTime now = LocalDateTime.now();
            m_addOrder.exec(orderId, qty, details, attributes, notes, ticketId, 
                ordertime, displayId, auxiliary, completetime, linekey, stationId, Timestamp.valueOf(now).toString());    

            updateSharedTicketByID(ticketId);
//        }
    }     
   
    public final void updateOrder(String orderId, Integer qty, 
            String details, String attributes, String notes, String ticketId, 
            String ordertime, Integer displayId, String auxiliary, String completetime
        ) throws BasicException {
        LocalDateTime now = LocalDateTime.now();
        m_updateOrder.exec(orderId, qty, details, attributes, notes, ticketId, 
                ordertime, displayId, auxiliary, completetime, Timestamp.valueOf(now).toString());    
    }    

    public void deleteOrder(String orderId) throws BasicException {
        m_deleteOrder.exec(orderId);
    }

    public void incompleteOrder(String orderId) throws BasicException {
        LocalDateTime now = LocalDateTime.now();
        m_incompleteOrder.exec(Timestamp.valueOf(now).toString(), Timestamp.valueOf(now).toString(), orderId);
    }
    
    public List<Object[]> getOrders(String station_id) throws BasicException{
        return m_getOrders.list(station_id);
    }

    public List<Object[]> getOrderByTicketid(String orderid, String ordertime) throws BasicException{
        return m_getOrderByticketid.list(orderid, ordertime);
    }

    public boolean isRefireOrderItem(String itemID) throws BasicException {
        List<Object> ordered = new StaticSentence(s
                , "select COMPLETETIME from orders where id = ?"
                , SerializerWriteString.INSTANCE
                , SerializerReadString.INSTANCE).list(itemID);

        if (!ordered.isEmpty()) {
            if (ordered.get(0) == null) return false;
        }

        return true;
    }

    public boolean isRefiredOrder(String ticketID) throws BasicException {
        List<Object> ordered = new StaticSentence(s
                , "select refiredtime from orders where ticketid = ? and (completetime is null OR displayid = 1)"
                , SerializerWriteString.INSTANCE
                , SerializerReadString.INSTANCE).list(ticketID);

        if (!ordered.isEmpty()) {
            for (int i = 0; i < ordered.size(); i ++) {
                if (ordered.get(i) != null) return true;
            }
        }
        return false;
    }

    public List<Object[]> getCompletedTicketid() throws BasicException{
        String openedTime = getOpenedStoreTime();
        return m_getCompletedticketid.list(openedTime);
    }

    public List<ClosedTicketInfo> getClosedTicketList() throws BasicException {
        String openedTime = getOpenedStoreTime();
        return (List<ClosedTicketInfo>) new StaticSentence(s
                , "SELECT id, ticketid, ticketname, openedby, openedat, closedat, content FROM closedtickets where openedat >= TIMESTAMP(?)"
                , SerializerWriteString.INSTANCE
                , new SerializerReadClass(ClosedTicketInfo.class)).list(openedTime);
    }

    public List<Object[]> getCashOutUserid() throws BasicException{
        String openedTime = getOpenedStoreTime();
        List<Object[]> list = (new PreparedSentence(s
                ,"select DISTINCT people.id, people.name from receipts, tickets left join people on people.id = tickets.person"
                    + " where tickets.id = receipts.id and receipts.datenew >= TIMESTAMP(?) "
                    + "and tickets.cashout = 0"
                ,SerializerWriteString.INSTANCE
                ,new SerializerReadBasic(new Datas[] {
                Datas.STRING,
                Datas.STRING}))).list(openedTime);
        return list;
    }

    public void updateCompleteTime(Timestamp completeTime, String id) throws BasicException{
        LocalDateTime now = LocalDateTime.now();
        m_updateCompleteTime.exec(completeTime, Timestamp.valueOf(now), id);
    }

    public void updateCompleteTimeForAll(String orderId, Timestamp orderTime) throws BasicException{
        LocalDateTime now = LocalDateTime.now();
        m_updateCompleteTimeForAll.exec(Timestamp.valueOf(now), Timestamp.valueOf(now), orderId, orderTime);
    }

    public Object getCountOfOrders(String ticketId) throws BasicException{
        return m_getCountOfOrder.find(ticketId);
    }

    public double getCountOfOrdersFromOpen() throws BasicException{
        String openedTime = getOpenedStoreTime();
        return Double.parseDouble(m_getCountOfOrderFromOpen.find(openedTime).toString());
    }

    public void insertNotificationPeople(String n_id, String p_id) throws BasicException {
        m_notificationPeopleData.exec(n_id, p_id);
    }

    public List<Object[]> filteredNotification() throws BasicException {
        return m_filterNotification.list();
    }

    public void sendNotification(String randomUUID, String title, String msg, int type, String people_id,
                                 Timestamp timestamp) throws BasicException {
        m_sendNotification.exec(randomUUID, title, msg, type, people_id, timestamp);
    }

    public List<String> getRemainingTicketLine(String s) throws BasicException {
        return m_getRemainingTicketLine.list(s);
    }

    public List<Object[]> getDataOfChangeInfo() throws BasicException{
        return m_getDataOfChangeInfo.list();
    }

    public void updateDoneOfChangeInfo(String id) throws BasicException{
        m_updateDoneOfChangeInfo.exec(id);
    }

    public List<Object[]> getDataOfCustomers(String id) throws BasicException{
        return m_getDataOfCustomers.list(id);
    }

    public List<Object[]> getDataOfAttributeset(String id) throws BasicException{
        return m_getDataOfAttributeSet.list(id);
    }

    public List<Object[]> getDataOfAttributesetinstance(String id) throws BasicException{
        return m_getDataOfAttributeSetInstance.list(id);
    }

    public List<Object[]> getDataOfCategories(String id) throws BasicException{
        return m_getDataOfCategories.list(id);
    }

    public List<Object[]> getDataOfClosedcash(String id) throws BasicException{
        return m_getDataOfClosedcash.list(id);
    }

    public List<Object[]> getDataOfTaxcustcategories(String id) throws BasicException{
        return m_getDataOfTaxcustcategories.list(id);
    }

    public List<Object[]> getDataOfLocations(String id) throws BasicException{
        return m_getDataOfLocations.list(id);
    }

    public List<Object[]> getDataOfReceipts(String id) throws BasicException{
        return m_getDataOfReceipts.list(id);
    }

    public List<Object[]> getDataOfPayments(String id) throws BasicException{
        return m_getDataOfPayments.list(id);
    }

    public List<Object[]> getEightOrders(String station_id, Integer n) throws BasicException{

        return m_getEightOrders.list(station_id, (n-1)*8);
    }

    public List<Object[]> getNewOrders(String station_id, Timestamp timestamp) throws BasicException{

        return m_getNewOrders.list(timestamp, station_id);
    }

    public List<Object[]> getUpdatedOrders(String station_id, Timestamp timestamp) throws BasicException{

        return m_getUpdatedOrder.list(timestamp, station_id);
    }

    public int getCountOfOrderBlock() throws BasicException{
        return (Integer) m_getCountOfOrderBlock.find();
    }
    
    public Timestamp getLastOrderTime(String station_id){
        try{
            return Timestamp.valueOf(m_getLastOrderTime.find("%" + station_id + "%").toString());
        }
        catch(BasicException e)
        {
            return null;
        }
    }

    /**
     *
     * @return
     */
    public final SentenceList getJobsList() {
        return new StaticSentence(s
            , "SELECT jobs.ID, jobs.JOB_NAME, jobs.ROLE_ID FROM people INNER JOIN jobs ON people.ROLE = jobs.ROlE_ID WHERE people.ID = ? ORDER BY jobs.JOB_NAME"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(JobInfo.class));
    }

    public String getPeopleId(String employee_no){
        try{
            return m_getPeopleId.find(employee_no).toString();
        }
        catch(BasicException e)
        {
            return null;
        }
        catch(NullPointerException e)
        {
            return null;
        }
    }
    
    /**
     *
     * @param checkin
     */
    public final void execCheckin(Object[] checkin) {
        try {
            m_checkin.exec(checkin);
        } catch (BasicException e) {
        }
    }
    /**
     *
     * @param checkin
     */
    public final void execCheckout(String peopleId) {
        try {
            List<Object[]> list = m_isPeopleClockIn.list(peopleId);
            if(!list.isEmpty()) {
                if(list.get(0)[2] == null) {
                    LocalDateTime now = LocalDateTime.now();
                    m_checkout.exec(Timestamp.valueOf(now), list.get(0)[0]);
                }
            }
        } catch (BasicException e) {
        }
    }

    public String getClockInByPeopleID(String peopleId) {
        try {
            List<Object[]> list = m_isPeopleClockIn.list(peopleId);
            if(list.size() > 0) {
                if(list.get(0)[1] != null) {
                    return list.get(0)[1].toString();
                } else {
                    return (new Date()).toString();
                }
            }
        } catch (BasicException e) {}
        return (new Date()).toString();
    }

    public boolean getClockOutByPeopleID(String peopleId) {
        try {
            List<Object[]> list = m_isPeopleClockIn.list(peopleId);
            if(!list.isEmpty()) {
                if (list.get(0)[2] != null)
                    return true;
                else return false;
            } else {
                return true;
            }
        } catch (BasicException e) {}
        return true;
    }

    public final void openStore() {
        try {
            LocalDateTime now = LocalDateTime.now();
            m_openStore.exec(Timestamp.valueOf(now).toString());
        } catch (BasicException e) {}
    }

    public boolean isOpenStore() throws BasicException {
        List<Object[]> list = m_isOpenStore.list();

        if(list.size() > 0) {
            if(list.get(0)[2] == null) {
                return true;
            } else {
                return false;
            }    
        } else {
            return false;
        }
    }

    public String getOpenedStoreTime() throws BasicException {
        List<Object[]> list = m_isOpenStore.list();

        if(!list.isEmpty()) {
            return (String) list.get(0)[1];  
        } else {
            return null;
        }
    }

    public String getWhichCashFromOpen() throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object> list = m_isWhichCash.list(openedTime);

        if(!list.isEmpty()) {
            return list.get(0).toString();
        } else {
            return null;
        }
    }

    public List<Object[]> getLastCashIn() throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object[]> list = m_lastCashIn.list(openedTime);

        return list;
    }

    public double[] getTotalCC() throws BasicException {
        String openedTime = getOpenedStoreTime();
        double[] returnValue = new double[2];
        List<Object[]> list = m_totalCC.list(openedTime);

        if(!list.isEmpty()) {
            if(list.get(0)[0] == null) {
                returnValue[0] = 0.0;
                returnValue[1] = 0.0;
                return returnValue;
            }
            returnValue[0] = Double.valueOf(list.get(0)[0].toString());
            returnValue[1] = Double.valueOf(list.get(0)[1].toString());
            return returnValue;
        } else {
            returnValue[0] = 0.0;
            returnValue[1] = 0.0;
            return returnValue;
        }
    }

    public double getTotalCash() throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object> list = m_totalCash.list(openedTime);

        if(!list.isEmpty()) {
            if(list.get(0) == null) return 0.0;
            return Double.valueOf(list.get(0).toString());
        } else {
            return 0.0;
        }
    }

    public double getTotalCashIn() throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object> list = m_totalCashIn.list(openedTime);

        if(!list.isEmpty()) {
            if(list.get(0) == null) return 0.0;
            return Double.valueOf(list.get(0).toString());
        } else {
            return 0.0;
        }
    }

    public double getTotalCashOut() throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object> list = m_totalCashOut.list(openedTime);

        if(!list.isEmpty()) {
            if(list.get(0) == null) return 0.0;
            return Double.valueOf(list.get(0).toString());
        } else {
            return 0.0;
        }
    }

    public double getTotalOrderCash() throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object[]> list = m_lastCashIn.list(openedTime);
        if(!list.isEmpty()) {
            String date = list.get(0)[3].toString();
            List<Object> total = m_totalOrderCash.list(date);

            if(total.get(0) != null) {
                return Double.valueOf(total.get(0).toString());
            } else {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }

    public List<Object[]> getPaymentSummary() throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object[]> list = m_paymentSummary.list(openedTime);
        return list;
    }

    public double getTotalNet() throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object[]> list = m_totalNet.list(openedTime);

        if(!list.isEmpty()) {
            if(list.get(0)[0] == null) return 0.0;
            return Double.valueOf(list.get(0)[1].toString());
        } else {
            return 0.0;
        }
    }

    public double getTotalTax() throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object[]> list = m_totalNet.list(openedTime);

        if(!list.isEmpty()) {
            if(list.get(0)[0] == null) return 0.0;
            return Double.valueOf(list.get(0)[0].toString());
        } else {
            return 0.0;
        }
    }

    public boolean isAllUserCashOut() throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object> list = (new PreparedSentence(s
                ,"select count(tickets.id) from receipts, tickets where tickets.id = receipts.id and receipts.datenew >= TIMESTAMP(?) "
                    + "and tickets.cashout = 0"
                ,SerializerWriteString.INSTANCE
                ,SerializerReadString.INSTANCE)).list(openedTime);

        if(!list.isEmpty()) {
            if(list.get(0) == null) return true;
            if(Integer.parseInt(list.get(0).toString()) > 0) return false;
            else return true;
        } else {
            return true;
        }
    }

    public boolean isUserCashOut(String userid) throws BasicException {
        String openedTime = getOpenedStoreTime();

        List<Object> list = (new PreparedSentence(s
                ,"select count(tickets.id) from receipts, tickets where tickets.id = receipts.id and receipts.datenew >= TIMESTAMP(?) "
                    + "and tickets.cashout = 0 and tickets.person = ?"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING,
                })
                ,SerializerReadString.INSTANCE)).list(openedTime, userid);

        if(!list.isEmpty()) {
            if(list.get(0) == null) return true;
            if(Integer.parseInt(list.get(0).toString()) > 0) return false;
            else return true;
        } else {
            return true;
        }
    }

    public void updateUserCashOut(String userid) throws BasicException {
        String openedTime = getOpenedStoreTime();
        (new PreparedSentence(s
                ,"UPDATE tickets INNER JOIN receipts ON tickets.id = receipts.id SET tickets.cashout = 1 WHERE receipts.datenew >= TIMESTAMP(?) AND tickets.person = ?;"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING,
                })
                ,SerializerReadString.INSTANCE)).exec(openedTime, userid);
    }

    public int getUserOrderCount(String userid) throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object> list = (new PreparedSentence(s
                ,"select count(DISTINCT ticketid) from receipts, ticketlines, tickets where receipts.id = ticketlines.ticket and ticketlines.ticket = tickets.id and receipts.datenew >= TIMESTAMP(?) and tickets.person = ?;"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING,
                })
                ,SerializerReadString.INSTANCE)).list(openedTime, userid);

        if(!list.isEmpty()) {
            if(list.get(0) == null) return 0;
            return Integer.parseInt(list.get(0).toString());
        } else {
            return 0;
        }
    }

    public double[] getUserOrderAmount(String userid) throws BasicException {
        String openedTime = getOpenedStoreTime();
        double[] returnValue = new double[2];
        List<Object[]> list = (new PreparedSentence(s
                    ,"SELECT ROUND(SUM(TL.PRICE * TL.units), 2), ROUND(SUM((TL.PRICE * TX.RATE) * TL.units), 2) "
                        + "FROM ((((((products P CROSS JOIN categories C ON (P.category = C.id)) INNER JOIN ticketlines TL ON (TL.product = P.id)) "
                        + "INNER JOIN taxes TX ON (TL.taxid = TX.id)) INNER JOIN tickets T ON (TL.ticket = T.id)) INNER JOIN receipts R ON (T.id = R.id)) "
                        + "INNER JOIN taxlines TXL ON (TXL.receipt = R.id) AND (TXL.taxid = TX.id), receipts) left join categories as doubleCate on (doubleCate.id = C.parentid) "
                        + "WHERE receipts.id = TL.ticket and receipts.datenew >= TIMESTAMP(?) and T.person = ?"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING
                })
                ,new SerializerReadBasic(new Datas[] {
                        Datas.DOUBLE,
                        Datas.DOUBLE
                }))).list(openedTime, userid);

        List<Object> attendance = (new PreparedSentence(s
                    ,"SELECT ROUND(SUM(B.price), 2) FROM ((((((products P CROSS JOIN categories C ON (P.category = C.id)) "
                        + "INNER JOIN ticketlines TL ON (TL.product = P.id)) INNER JOIN taxes TX ON (TL.taxid = TX.id)) INNER JOIN tickets T ON (TL.ticket = T.id)) "
                        + "INNER JOIN receipts R ON (T.id = R.id)) INNER JOIN taxlines TXL ON (TXL.receipt = R.id) AND (TXL.taxid = TX.id), receipts) "
                        + "left join categories as doubleCate on (doubleCate.id = C.parentid) left join attributeinstance A on TL.attributesetinstance_id = A.attributesetinstance_id "
                        + "LEFT JOIN attributevalue B ON A.ATTRIBUTE_ID = B.ATTRIBUTE_ID AND A.VALUE = B.VALUE WHERE receipts.id = TL.ticket "
                        + "and receipts.datenew >= TIMESTAMP(?) and T.person = ?"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING
                })
                ,SerializerReadString.INSTANCE)).list(openedTime, userid);

        if(!list.isEmpty()) {
            if(list.get(0)[0] == null) {
                returnValue[0] = 0.0;
                returnValue[1] = 0.0;
                return returnValue;
            }

            else if(attendance.get(0) == null) {
                returnValue[0] = Double.valueOf(list.get(0)[0].toString());
                returnValue[1] = Double.valueOf(list.get(0)[1].toString());
                return returnValue;
            }

            returnValue[0] = Double.valueOf(list.get(0)[0].toString()) + Double.valueOf(attendance.get(0).toString());
            returnValue[1] = Double.valueOf(list.get(0)[1].toString());
            return returnValue;
        } else {
            returnValue[0] = 0.0;
            returnValue[1] = 0.0;
            return returnValue;
        }
    }

    public double getUserComp(String userid) throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object> list = (new PreparedSentence(s
                ,"SELECT ROUND(SUM(TL.PRICE *(1 + TX.RATE) * TL.units), 2) FROM ((((((products P CROSS JOIN categories C ON (P.category = C.id)) "
                    + "INNER JOIN ticketlines TL ON (TL.product = P.id)) INNER JOIN taxes TX ON (TL.taxid = TX.id)) "
                    + "INNER JOIN tickets T ON (TL.ticket = T.id)) INNER JOIN receipts R ON (T.id = R.id)) INNER JOIN taxlines TXL ON (TXL.receipt = R.id) "
                    + "AND (TXL.taxid = TX.id), receipts) left join categories as doubleCate on (doubleCate.id = C.parentid) "
                    + "WHERE receipts.id = TL.ticket "
                    + "and TL.comp = 1 and receipts.datenew >= TIMESTAMP(?) and T.person = ?"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING
                })
                ,SerializerReadString.INSTANCE)).list(openedTime, userid);

        List<Object> attendance = (new PreparedSentence(s
                ,"SELECT ROUND(SUM(B.price), 2) FROM ((((((products P CROSS JOIN categories C ON (P.category = C.id)) "
                    + "INNER JOIN ticketlines TL ON (TL.product = P.id)) INNER JOIN taxes TX ON (TL.taxid = TX.id)) INNER JOIN tickets T ON (TL.ticket = T.id)) "
                    + "INNER JOIN receipts R ON (T.id = R.id)) INNER JOIN taxlines TXL ON (TXL.receipt = R.id) AND (TXL.taxid = TX.id), receipts) "
                    + "left join categories as doubleCate on (doubleCate.id = C.parentid) left join attributeinstance A on TL.attributesetinstance_id = A.attributesetinstance_id "
                    + "LEFT JOIN attributevalue B ON A.ATTRIBUTE_ID = B.ATTRIBUTE_ID AND A.VALUE = B.VALUE WHERE receipts.id = TL.ticket and TL.comp = 1 and "
                    + "receipts.datenew >= TIMESTAMP(?) and T.person = ?"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING
                })
                ,SerializerReadString.INSTANCE)).list(openedTime, userid);

        if(!list.isEmpty()) {
            if(list.get(0) == null) return 0.0;
            return Double.parseDouble(list.get(0).toString()) + Double.parseDouble(attendance.get(0).toString());
        } else {
            return 0.0;
        }
    }

    public double getUserDiscount(String userid) throws BasicException {
        String openedTime = getOpenedStoreTime();
        List<Object> list = (new PreparedSentence(s
                ,"SELECT ROUND(SUM(TL.PRICE *(1 + TX.RATE) * TL.units * TL.discount / 100), 2) "
                    + "FROM ((((((products P CROSS JOIN categories C ON (P.category = C.id)) INNER JOIN ticketlines TL ON (TL.product = P.id)) "
                    + "INNER JOIN taxes TX ON (TL.taxid = TX.id)) INNER JOIN tickets T ON (TL.ticket = T.id)) INNER JOIN receipts R ON (T.id = R.id)) "
                    + "INNER JOIN taxlines TXL ON (TXL.receipt = R.id) AND (TXL.taxid = TX.id), receipts) "
                    + "WHERE receipts.id = TL.ticket and receipts.datenew >= TIMESTAMP(?) and T.person = ?"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING
                })
                ,SerializerReadString.INSTANCE)).list(openedTime, userid);

        List<Object> attendance = (new PreparedSentence(s
                ,"SELECT ROUND(SUM(B.price), 2) "
                    + "FROM ((((((products P CROSS JOIN categories C ON (P.category = C.id)) INNER JOIN ticketlines TL ON (TL.product = P.id)) "
                    + "INNER JOIN taxes TX ON (TL.taxid = TX.id)) INNER JOIN tickets T ON (TL.ticket = T.id)) INNER JOIN receipts R ON (T.id = R.id)) "
                    + "INNER JOIN taxlines TXL ON (TXL.receipt = R.id) AND (TXL.taxid = TX.id), receipts) left join categories as doubleCate on (doubleCate.id = C.parentid) "
                    + "left join attributeinstance A on TL.attributesetinstance_id = A.attributesetinstance_id LEFT JOIN attributevalue B ON A.ATTRIBUTE_ID = B.ATTRIBUTE_ID "
                    + "AND A.VALUE = B.VALUE "
                    + "WHERE receipts.id = TL.ticket and receipts.datenew >= TIMESTAMP(?) and T.person = ?"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING
                })
                ,SerializerReadString.INSTANCE)).list(openedTime, userid);

        if(!list.isEmpty()) {
            if(list.get(0) == null) return 0.0;
            return Double.parseDouble(list.get(0).toString());
        } else {
            return 0.0;
        }
    }

    public double[] getUserSubTotal(String userid) throws BasicException {
        String openedTime = getOpenedStoreTime();
        double[] returnValue = new double[2];
        List<Object[]> list = (new PreparedSentence(s
                ,"select sum(payments.total), sum(payments.tip) from receipts left join tickets on (receipts.id = tickets.id), "
                    + "payments where payments.receipt = receipts.id and payments.payment not like '%cash%' and receipts.datenew >= TIMESTAMP(?) "
                    + "and tickets.person = ?;"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING
                })
                ,new SerializerReadBasic(new Datas[] {
                        Datas.DOUBLE,
                        Datas.DOUBLE
                }))).list(openedTime, userid);

        if(!list.isEmpty()) {
            if(list.get(0)[0] == null) {
                returnValue[0] = 0.0;
                returnValue[1] = 0.0;
                return returnValue;
            }
            returnValue[0] = Double.valueOf(list.get(0)[0].toString());
            returnValue[1] = Double.valueOf(list.get(0)[1].toString());
            return returnValue;
        } else {
            returnValue[0] = 0.0;
            returnValue[1] = 0.0;
            return returnValue;
        }
    }

    public List<Object[]> getUserCategorySummary(String userid) throws BasicException {
        String openedTime = getOpenedStoreTime();
        Double tempSum = 0.0;
        String[] temp = new String[2];
        List<Object[]> nonParentList = (new PreparedSentence(s
            ,"SELECT C.name, round(SUM(TL.PRICE *(1 + TX.RATE)*TL.units), 2) FROM ((((((products P CROSS JOIN categories C ON (P.category = C.id)) "
                + "INNER JOIN ticketlines TL ON (TL.product = P.id)) INNER JOIN taxes TX ON (TL.taxid = TX.id)) INNER JOIN tickets T ON (TL.ticket = T.id)) "
                + "INNER JOIN receipts R ON (T.id = R.id)) INNER JOIN taxlines TXL ON (TXL.receipt = R.id) AND (TXL.taxid = TX.id), receipts) "
                + "left join categories as doubleCate on (doubleCate.id = C.parentid) WHERE receipts.id = TL.ticket and C.texttip != 'food' and "
                + "receipts.datenew >= TIMESTAMP(?) and T.person = ? and C.parentid is null GROUP BY C.id"
            ,new SerializerWriteBasic(new Datas[] {
                    Datas.STRING,
                    Datas.STRING
            })
            ,new SerializerReadBasic(new Datas[] {
                    Datas.STRING,
                    Datas.STRING
            }))).list(openedTime, userid);

        List<Double> nonParentAttendance = (new PreparedSentence(s
            ,"SELECT round(SUM(B.price), 2) FROM ((((((products P CROSS JOIN categories C ON (P.category = C.id)) "
                + "INNER JOIN ticketlines TL ON (TL.product = P.id)) INNER JOIN taxes TX ON (TL.taxid = TX.id)) INNER JOIN tickets T ON (TL.ticket = T.id)) "
                + "INNER JOIN receipts R ON (T.id = R.id)) INNER JOIN taxlines TXL ON (TXL.receipt = R.id) AND (TXL.taxid = TX.id), receipts) "
                + "left join categories as doubleCate on (doubleCate.id = C.parentid)  left join attributeinstance A on TL.attributesetinstance_id = A.attributesetinstance_id "
                + "LEFT JOIN attributevalue B ON A.ATTRIBUTE_ID = B.ATTRIBUTE_ID AND A.VALUE = B.VALUE WHERE receipts.id = TL.ticket and C.texttip != 'food' and "
                + "receipts.datenew >= TIMESTAMP(?) and T.person = ? and C.parentid is null GROUP BY C.id"
            ,new SerializerWriteBasic(new Datas[] {
                    Datas.STRING,
                    Datas.STRING
            })
            ,SerializerReadDouble.INSTANCE)).list(openedTime, userid);

            if(nonParentList.size() > 0 && nonParentAttendance.size() > 0 && nonParentAttendance.get(0) != null) {
                tempSum = Double.parseDouble(nonParentList.get(0)[1].toString()) + nonParentAttendance.get(0);

                temp[0] = nonParentList.get(0)[0].toString();
                temp[1] = tempSum.toString();

                nonParentList.set(0, temp);
            }

        List<Object[]> parentList = (new PreparedSentence(s
            ,"SELECT doubleCate.name, round(SUM(TL.PRICE *(1 + TX.RATE)*TL.units), 2) FROM ((((((products P CROSS JOIN categories C ON (P.category = C.id)) "
                + "INNER JOIN ticketlines TL ON (TL.product = P.id)) INNER JOIN taxes TX ON (TL.taxid = TX.id)) INNER JOIN tickets T ON (TL.ticket = T.id)) "
                + "INNER JOIN receipts R ON (T.id = R.id)) INNER JOIN taxlines TXL ON (TXL.receipt = R.id) AND (TXL.taxid = TX.id), receipts) "
                + "left join categories as doubleCate on (doubleCate.id = C.parentid) WHERE receipts.id = TL.ticket and C.texttip != 'food' and receipts.datenew >= TIMESTAMP(?) "
                + "and T.person = ? and C.parentid is not null GROUP BY C.parentid"
            ,new SerializerWriteBasic(new Datas[] {
                    Datas.STRING,
                    Datas.STRING
            })
            ,new SerializerReadBasic(new Datas[] {
                    Datas.STRING,
                    Datas.STRING
            }))).list(openedTime, userid);

        List<Double> parentAttendance = (new PreparedSentence(s
            ,"SELECT round(sum(B.price), 2) FROM ((((((products P CROSS JOIN categories C ON (P.category = C.id)) "
                + "INNER JOIN ticketlines TL ON (TL.product = P.id)) INNER JOIN taxes TX ON (TL.taxid = TX.id)) INNER JOIN tickets T ON (TL.ticket = T.id)) "
                + "INNER JOIN receipts R ON (T.id = R.id)) INNER JOIN taxlines TXL ON (TXL.receipt = R.id) AND (TXL.taxid = TX.id), receipts) "
                + "left join categories as doubleCate on (doubleCate.id = C.parentid)  left join attributeinstance A on TL.attributesetinstance_id = A.attributesetinstance_id "
                + "LEFT JOIN attributevalue B ON A.ATTRIBUTE_ID = B.ATTRIBUTE_ID AND A.VALUE = B.VALUE WHERE receipts.id = TL.ticket and C.texttip != 'food' and receipts.datenew >= TIMESTAMP(?) "
                + "and T.person = ? and C.parentid is not null GROUP BY C.parentid"
            ,new SerializerWriteBasic(new Datas[] {
                    Datas.STRING,
                    Datas.STRING
            })
            ,SerializerReadDouble.INSTANCE)).list(openedTime, userid);

        if(parentList.size() > 0 && parentAttendance.size() > 0 && parentAttendance.get(0) != null) {
            tempSum = Double.parseDouble(parentList.get(0)[1].toString()) + parentAttendance.get(0);

            temp = new String[2];
            temp[0] = parentList.get(0)[0].toString();
            temp[1] = tempSum.toString();

            parentList.set(0, temp);
        }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        List<Object[]> foodList = (new PreparedSentence(s
                ,"SELECT C.texttip, round(SUM(TL.PRICE *(1 + TX.RATE)*TL.units), 2) FROM (((((products P CROSS JOIN categories C ON (P.category = C.id)) "
                    + "INNER JOIN ticketlines TL ON (TL.product = P.id)) INNER JOIN taxes TX ON (TL.taxid = TX.id)) INNER JOIN tickets T ON (TL.ticket = T.id)) "
                    + "INNER JOIN receipts R ON (T.id = R.id)) INNER JOIN taxlines TXL ON (TXL.receipt = R.id) AND (TXL.taxid = TX.id), receipts "
                    + "WHERE receipts.id = TL.ticket and C.texttip = 'food' and receipts.datenew >= TIMESTAMP(?) and T.person = ? GROUP BY C.texttip"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING
                })
                ,new SerializerReadBasic(new Datas[] {
                    Datas.STRING,
                    Datas.STRING
            }))).list(openedTime, userid);

        List<Double> foodAttendace = (new PreparedSentence(s
                ,"SELECT round(SUM(B.price), 2) FROM (((((products P CROSS JOIN categories C ON (P.category = C.id)) "
                    + "INNER JOIN ticketlines TL ON (TL.product = P.id)) INNER JOIN taxes TX ON (TL.taxid = TX.id)) INNER JOIN tickets T ON (TL.ticket = T.id)) "
                    + "INNER JOIN receipts R ON (T.id = R.id)) INNER JOIN taxlines TXL ON (TXL.receipt = R.id) AND (TXL.taxid = TX.id) "
                    + "left join attributeinstance A on TL.attributesetinstance_id = A.attributesetinstance_id LEFT JOIN attributevalue B ON A.ATTRIBUTE_ID = B.ATTRIBUTE_ID AND A.VALUE = B.VALUE, receipts "
                    + "WHERE receipts.id = TL.ticket and C.texttip = 'food' and receipts.datenew >= TIMESTAMP(?) and T.person = ? GROUP BY C.texttip"
                ,new SerializerWriteBasic(new Datas[] {
                        Datas.STRING,
                        Datas.STRING
                })
                ,SerializerReadDouble.INSTANCE)).list(openedTime, userid);

        if(foodList.size() > 0 && foodAttendace.size() > 0 && foodAttendace.get(0) != null) {
            tempSum = Double.parseDouble(foodList.get(0)[1].toString()) + foodAttendace.get(0);

            temp = new String[2];
            temp[0] = foodList.get(0)[0].toString();
            temp[1] = tempSum.toString();

            foodList.set(0, temp);
        }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        List<Object[]> allList = new ArrayList<Object[]>();
        allList.addAll(nonParentList);
        allList.addAll(parentList);
        allList.addAll(foodList);

        return allList;
    }

    public final void closeStore() {
        try {
            LocalDateTime now = LocalDateTime.now();
            String id = null;
            List<Object[]> list = m_isOpenStore.list();

            if(!list.isEmpty()) {
                if(list.get(0)[2] == null) {
                    m_closeStore.exec(Timestamp.valueOf(now).toString(), (Integer) list.get(0)[0]);
                }  
            }
        } catch (BasicException e) {}
    }

    public final void updateTip(String id, double tip) {
        try {
            new StaticSentence(s
                , "UPDATE payments SET TIP = ? WHERE ID = ?"
                ,new SerializerWriteBasic(new Datas[] {Datas.DOUBLE, Datas.STRING})).exec(tip, id);
        } catch (BasicException e) {}
    }

    public final void updateSharedTicketByID(String id) {
        try {
            new StaticSentence(s
                , "UPDATE sharedtickets SET ordered = 1 WHERE ID = ?"
                , SerializerWriteString.INSTANCE).exec(id);
        } catch (BasicException e) {}
    }

    public boolean getSharedTicketOrdered(String id) {
        try {
            List<Object> ordered = new StaticSentence(s
                , "select ordered from sharedtickets WHERE ID = ?"
                , SerializerWriteString.INSTANCE
                , SerializerReadString.INSTANCE).list(id);

            if (!ordered.isEmpty())
                if (Integer.parseInt(ordered.get(0).toString()) > 0) return true;
            else return false;
        } catch (BasicException e) {}

        return false;
    }

    public void plusItemOrdered(String ticketid, String productName) {
        try {
            new StaticSentence(s
                , "UPDATE orders LEFT JOIN products ON orders.details = products.name SET orders.qty = orders.qty + 1 "
                        + "WHERE orders.ticketid = ? and orders.details = ? "
                        + "and orders.completetime is null and orders.displayid = 1;"
                , new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING})).exec(ticketid, productName);
        } catch (BasicException e) {}
    }
}
