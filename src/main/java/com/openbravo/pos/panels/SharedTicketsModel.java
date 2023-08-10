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

package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.util.StringUtils;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @authors
 * 
 */
public class SharedTicketsModel {

    private String m_sHost;
    private String m_sUser;
    private int m_iSeq;
    private Date m_dDateStart;
    private Date m_dDateEnd;       
    private Date rDate;
    private Date m_dPrintDate;
    
            
    private Integer m_iPayments;
    private Double m_dPaymentsTotal;
    private java.util.List<PaymentsLine> m_lpayments;
    
// JG 9 Nov 12 
    private Integer m_iCategorySalesRows;
    private Double m_dCategorySalesTotalUnits;
    private Double m_dCategorySalesTotal;
    private java.util.List<CategorySalesLine> m_lcategorysales;
// end     
    
    // by janar153 @ 01.12.2013
    private Integer m_iProductSalesRows;
    private Double m_dProductSalesTotalUnits;
    private Double m_dProductSalesTotal;
    private java.util.List<ProductSalesLine> m_lproductsales;
    // end
    
    // added by janar153 @ 29.12.2013
    private java.util.List<RemovedProductLines> m_lremovedlines;
    
    private java.util.List<DrawerOpenedLines> m_ldraweropenedlines;    
    
    private final static String[] TICKETHEADERS = {"label.tabname", "label.seat", "label.timeelapsed", "label.server", "label.total"};
    
    private Integer m_iSales;
    private Double m_dSalesBase;
    private Double m_dSalesTaxes;
    private Double m_dSalesTaxNet;
    private java.util.List<SalesLine> m_lsales;

    private AppView m_App; 
    private DataLogicReceipts dlReceipts = null;


    private SharedTicketsModel() {
    }

    /**
     *
     * @return
     */
    public static SharedTicketsModel emptyInstance() {
        
        SharedTicketsModel p = new SharedTicketsModel();
        
        p.m_iPayments = 0;
        p.m_dPaymentsTotal = 0.0;
// JG 16 May 2013 use diamond inference
        p.m_lpayments = new ArrayList<>();

// JG 9 Nov 12
        p.m_iCategorySalesRows = 0;
        p.m_dCategorySalesTotalUnits = 0.0;
        p.m_dCategorySalesTotal = 0.0;
        p.m_lcategorysales = new ArrayList<>();        
// end
        p.m_iSales = null;
        p.m_dSalesBase = null;
        p.m_dSalesTaxes = null;
        p.m_dSalesTaxNet = null;
        
// JG 16 May 2013 use diamond inference
        
        // by janar153 @ 01.12.2013 
        p.m_iProductSalesRows = 0;
        p.m_dProductSalesTotalUnits = 0.0;
        p.m_dProductSalesTotal = 0.0;
        p.m_lproductsales = new ArrayList<>();
        p.m_lremovedlines = new ArrayList<>();
        
        p.m_lsales = new ArrayList<>();

        return p;
    }
    
    /**
     *
     * @param app
     * @param mergeID
     * @return
     * @throws BasicException
     */
    public static SharedTicketsModel loadInstance(AppView app, String mergeID) throws BasicException {
        SharedTicketsModel p = new SharedTicketsModel();
        // Propiedades globales
        p.m_App = app;  
        p.m_sHost = app.getProperties().getHost();
        p.m_sUser = app.getAppUserView().getUser().getName();
        p.m_iSeq = app.getActiveCashSequence();
        p.m_dDateStart = app.getActiveCashDateStart();
        p.m_dDateEnd = null;
        p.dlReceipts = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        

// JG 9 Nov 12
        // Product category Sales
        Object[] valcategorysales = (Object []) new StaticSentence(app.getSession()
            , "SELECT COUNT(*), " + 
                "SUM(ticketlines.UNITS), " +
                "SUM((ticketlines.PRICE + ticketlines.PRICE * taxes.RATE ) * ticketlines.UNITS) " +
              "FROM ticketlines, tickets, receipts, taxes " +
              "WHERE ticketlines.TICKET = tickets.ID AND tickets.ID = receipts.ID " +
                    "AND ticketlines.TAXID = taxes.ID " +
                    "AND ticketlines.PRODUCT IS NOT NULL " + 
                    "AND receipts.MONEY = ? " +
              "GROUP BY receipts.MONEY"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE, Datas.DOUBLE}))
            .find(app.getActiveCashIndex());

        if (valcategorysales == null) {
            p.m_iCategorySalesRows = 0;
            p.m_dCategorySalesTotalUnits = 0.0;
            p.m_dCategorySalesTotal = 0.0;
        } else {
            p.m_iCategorySalesRows = (Integer) valcategorysales[0];
            p.m_dCategorySalesTotalUnits = (Double) valcategorysales[1];
            p.m_dCategorySalesTotal= (Double) valcategorysales[2];
        }

        List categorys = new StaticSentence(app.getSession()
            , "SELECT a.NAME, sum(c.UNITS), sum(c.UNITS * (c.PRICE + (c.PRICE * d.RATE))) " +
              "FROM categories as a " +
              "LEFT JOIN products as b on a.id = b.CATEGORY " +
              "LEFT JOIN ticketlines as c on b.id = c.PRODUCT " +
              "LEFT JOIN taxes as d on c.TAXID = d.ID " +
              "LEFT JOIN receipts as e on c.TICKET = e.ID " +
              "WHERE e.MONEY = ? " +
              "GROUP BY a.NAME"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(SharedTicketsModel.CategorySalesLine.class))
            .list(app.getActiveCashIndex());

        if (categorys == null) {
            p.m_lcategorysales = new ArrayList();
        } else {
            p.m_lcategorysales = categorys;
        }        
// end
        
        // Payments
        Object[] valtickets = (Object []) new StaticSentence(app.getSession()
            , "SELECT COUNT(*), SUM(payments.TOTAL) " +
              "FROM payments, receipts " +
              "WHERE payments.RECEIPT = receipts.ID AND receipts.MONEY = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
            .find(app.getActiveCashIndex());
            
        if (valtickets == null) {
            p.m_iPayments = 0;
            p.m_dPaymentsTotal = 0.0;
        } else {
            p.m_iPayments = (Integer) valtickets[0];
            p.m_dPaymentsTotal = (Double) valtickets[1];
        }
        
        String query = "select shared.name, shared.id, shared.openedtime, people.name "
                + "from sharedtickets shared left join people on shared.appuser = people.id where 1=1";

        if(!"".equals(mergeID)) {
            query += " AND shared.id != '" + mergeID + "'";
        }
        List l = new StaticSentence(app.getSession(),            
            query
            , null
            , new SerializerReadClass(SharedTicketsModel.PaymentsLine.class))
            .list();

        
        if (l == null) {
            p.m_lpayments = new ArrayList();
        } else {
            p.m_lpayments = l;
        }
        
        // Sales
        Object[] recsales = (Object []) new StaticSentence(app.getSession(),
            "SELECT COUNT(DISTINCT receipts.ID), SUM(ticketlines.UNITS * ticketlines.PRICE) " +
              "FROM receipts, ticketlines " +
              "WHERE receipts.ID = ticketlines.TICKET AND receipts.MONEY = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
                .find(app.getActiveCashIndex());

        if (recsales == null) {
            p.m_iSales = null;
            p.m_dSalesBase = null;
        } else {
            p.m_iSales = (Integer) recsales[0];
            p.m_dSalesBase = (Double) recsales[1];
        }             
        
        // Taxes
        Object[] rectaxes = (Object []) new StaticSentence(app.getSession(),
            "SELECT SUM(taxlines.AMOUNT), SUM(taxlines.BASE) " +
                "FROM receipts, taxlines " +
                "WHERE receipts.ID = taxlines.RECEIPT AND receipts.MONEY = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.DOUBLE, Datas.DOUBLE})) 
            .find(app.getActiveCashIndex());            

        if (rectaxes == null) {
            p.m_dSalesTaxes = null;
            p.m_dSalesTaxNet = null;
        } else {
            p.m_dSalesTaxes = (Double) rectaxes[0];
            p.m_dSalesTaxNet = (Double) rectaxes[1];
        } 
                
        // JG June 2014 Added .BASE for array
        List<SalesLine> asales = new StaticSentence(app.getSession(),
                "SELECT taxcategories.NAME, SUM(taxlines.AMOUNT), SUM(taxlines.BASE), SUM(taxlines.BASE + taxlines.AMOUNT) "
                    + "FROM receipts, taxlines, taxes, taxcategories "
                    + "WHERE receipts.ID = taxlines.RECEIPT AND taxlines.TAXID = taxes.ID AND taxes.CATEGORY = taxcategories.ID " +
                        "AND receipts.MONEY = ?"
                    + "GROUP BY taxcategories.NAME"
                , SerializerWriteString.INSTANCE
                , new SerializerReadClass(SharedTicketsModel.SalesLine.class))
                .list(app.getActiveCashIndex());

        if (asales == null) {
            p.m_lsales = new ArrayList<>();
        } else {
            p.m_lsales = asales;
        }
         
         // added by janar153 @ 29.12.2013
        // removed lines list
        SimpleDateFormat ndf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startDateFormatted = ndf.format(app.getActiveCashDateStart());
        List removedLines = new StaticSentence(app.getSession()
            , "SELECT lineremoved.NAME, lineremoved.TICKETID, lineremoved.PRODUCTNAME, SUM(lineremoved.UNITS) AS TOTAL_UNITS  "
              + "FROM lineremoved "
              + "WHERE lineremoved.REMOVEDDATE > ? "
              + "GROUP BY lineremoved.NAME, lineremoved.TICKETID, lineremoved.PRODUCTNAME"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(SharedTicketsModel.RemovedProductLines.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
            .list(startDateFormatted);
        
        if (removedLines == null) {
            p.m_lremovedlines = new ArrayList();
        } else {
            p.m_lremovedlines = removedLines;
        }
 
/** 
 * JG Dec 14
 * Open Drawer List
*/

        List drawerOpenedLines = new StaticSentence(app.getSession()
            , "SELECT OPENDATE, NAME, TICKETID  " +
              "FROM draweropened " +
              "WHERE TICKETID = 'No Sale' AND OPENDATE > ? " +
              "GROUP BY NAME, OPENDATE, TICKETID"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(SharedTicketsModel.DrawerOpenedLines.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
            .list(startDateFormatted);
        
        if (drawerOpenedLines == null) {
            p.m_ldraweropenedlines = new ArrayList();
        } else {
            p.m_ldraweropenedlines = drawerOpenedLines;
        }        
                
        // by janar153 @ 01.12.2013
        // Product Sales
        Object[] valproductsales = (Object []) new StaticSentence(app.getSession()
            , "SELECT COUNT(*), SUM(ticketlines.UNITS), "
                + "SUM((ticketlines.PRICE + ticketlines.PRICE * taxes.RATE ) * ticketlines.UNITS) "
            + "FROM ticketlines, tickets, receipts, taxes "
            + "WHERE ticketlines.TICKET = tickets.ID " +
                "AND tickets.ID = receipts.ID " +
                "AND ticketlines.TAXID = taxes.ID " +
                "AND ticketlines.PRODUCT IS NOT NULL " +
                "AND receipts.MONEY = ? "
            + "GROUP BY receipts.MONEY"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE, Datas.DOUBLE}))
            .find(app.getActiveCashIndex());
 
        if (valproductsales == null) {
            p.m_iProductSalesRows = 0;
            p.m_dProductSalesTotalUnits = 0.0;
            p.m_dProductSalesTotal = 0.0;
        } else {
            p.m_iProductSalesRows = (Integer) valproductsales[0];
            p.m_dProductSalesTotalUnits = (Double) valproductsales[1];
            p.m_dProductSalesTotal= (Double) valproductsales[2];
        }
 
        List products = new StaticSentence(app.getSession()
            , "SELECT products.NAME, SUM(ticketlines.UNITS), ticketlines.PRICE, taxes.RATE "
            + "FROM ticketlines, tickets, receipts, products, taxes "
            + "WHERE ticketlines.PRODUCT = products.ID " +
                "AND ticketlines.TICKET = tickets.ID " +
                "AND tickets.ID = receipts.ID " + 
                "AND ticketlines.TAXID = taxes.ID " +
                "AND receipts.MONEY = ? "
            + "GROUP BY products.NAME, ticketlines.PRICE, taxes.RATE"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(SharedTicketsModel.ProductSalesLine.class))
            .list(app.getActiveCashIndex());
 
        if (products == null) {
            p.m_lproductsales = new ArrayList();
        } else {
            p.m_lproductsales = products;
        }
      
        return p;
    }

    /**
     *
     * @return
     */
    public int getPayments() {
        return m_iPayments;
    }

    /**
     *
     * @return
     */
    public double getTotal() {
        return m_dPaymentsTotal;
    }

    /**
     *
     * @return
     */
    public String getHost() {
        return m_sHost;
    }
    /**
     *
     * @return
     */
    public String getUser() {
        return m_sUser;
    }

    /**
     *
     * @return
     */
    public int getSequence() {
        return m_iSeq;
    }
    
    public String getPrintDate() {
        Date m_dPrintDate = new Date();
        return Formats.TIMESTAMP.formatValue(m_dPrintDate);
    }
    /**
     *
     * @return
     */
    public Date getDateStart() {
        return m_dDateStart;
    }

    /**
     *
     * @param dValue
     */
    public void setDateEnd(Date dValue) {
        m_dDateEnd = dValue;
    }

    /**
     *
     * @return
     */
    public Date getDateEnd() {
        return m_dDateEnd;
    }
    
    /**
     *
     * @return
     */
    public String getDateStartDerby(){
        SimpleDateFormat ndf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return ndf.format(m_dDateStart);
    }
    
    /**
     *
     * @return
     */
    public String printHost() {
//        return m_sHost;
        return StringUtils.encodeXML(m_sHost);
    }
    
    /**
     *
     * @return
     */
    public String printUser() {
        return StringUtils.encodeXML(m_sUser);
    }
    
    /**
     *
     * @return
     */
    public String printSequence() {
        return Formats.INT.formatValue(m_iSeq);
    }
    
    public String printDate() {
        Date m_dPrintDate = new Date();
        return Formats.TIMESTAMP.formatValue(m_dPrintDate);
    }


    /**
     *
     * @return
     */
    public String printDateStart() {
        return Formats.TIMESTAMP.formatValue(m_dDateStart);
    }

    /**
     *
     * @return
     */
    public String printDateEnd() {
        return Formats.TIMESTAMP.formatValue(m_dDateEnd);
    }

    /**
     *
     * @return
     */
    public String printPayments() {
        return Formats.INT.formatValue(m_iPayments);
    }

    /**
     *
     * @return
     */
    public String printPaymentsTotal() {
        return Formats.CURRENCY.formatValue(m_dPaymentsTotal);
    }

    /**
     *
     * @return
     */
    public List<PaymentsLine> getPaymentLines() {
        return m_lpayments;
    }
    
    /**
     *
     * @return
     */
    public int getSales() {
        return m_iSales == null ? 0 : m_iSales;
    }    

    /**
     *
     * @return
     */
    public String printSales() {
        return Formats.INT.formatValue(m_iSales);
    }

    /**
     *
     * @return
     */
    public String printSalesBase() {
        return Formats.CURRENCY.formatValue(m_dSalesBase);
    }     

    /**
     *
     * @return
     */
    public String printSalesTaxes() {
        return Formats.CURRENCY.formatValue(m_dSalesTaxes);
    }     

    /**
     *
     * @return
     */
    public String printSalesTotal() {            
        return Formats.CURRENCY.formatValue((m_dSalesBase == null || m_dSalesTaxes == null)
                ? null
                : m_dSalesBase + m_dSalesTaxes);
    }     

    /**
     *
     * @return
     */
    public List<SalesLine> getSaleLines() {
        return m_lsales;
    }

// JG 9 Nov 12

    /**
     *
     * @return
     */
        public double getCategorySalesRows() {
        return m_iCategorySalesRows;
    }

    /**
     *
     * @return
     */
    public String printCategorySalesRows() {
        return Formats.INT.formatValue(m_iCategorySalesRows);
    }

    /**
     *
     * @return
     */
    public double getCategorySalesTotalUnits() {
        return m_dCategorySalesTotalUnits;
    }

    /**
     *
     * @return
     */
    public String printCategorySalesTotalUnits() {
        return Formats.DOUBLE.formatValue(m_dCategorySalesTotalUnits);
    }

    /**
     *
     * @return
     */
    public double getCategorySalesTotal() {
        return m_dCategorySalesTotal;
    }

    /**
     *
     * @return
     */
    public String printCategorySalesTotal() {
        return Formats.CURRENCY.formatValue(m_dCategorySalesTotal);
    }

    /**
     *
     * @return
     */
    public List<CategorySalesLine> getCategorySalesLines() {
        return m_lcategorysales;
    }    
// end
    
    // by janar153 @ 01.12.2013

    /**
     *
     * @return
     */
        public double getProductSalesRows() {
        return m_iProductSalesRows;
    }
    
    /**
     *
     * @return
     */
    public String printProductSalesRows() {
        return Formats.INT.formatValue(m_iProductSalesRows);
    }
 
    /**
     *
     * @return
     */
    public double getProductSalesTotalUnits() {
        return m_dProductSalesTotalUnits;
    }
 
    /**
     *
     * @return
     */
    public String printProductSalesTotalUnits() {
        return Formats.DOUBLE.formatValue(m_dProductSalesTotalUnits);
    }
 
    /**
     *
     * @return
     */
    public double getProductSalesTotal() {
        return m_dProductSalesTotal;
    }
 
    /**
     *
     * @return
     */
    public String printProductSalesTotal() {
        return Formats.CURRENCY.formatValue(m_dProductSalesTotal);
    }
 
    /**
     *
     * @return
     */
    public List<ProductSalesLine> getProductSalesLines() {
        return m_lproductsales;
    }
    // end
    
    /**
     * janar153 @ 29.12.2013
     * @return
     */
        public List<RemovedProductLines> getRemovedProductLines() {
        return m_lremovedlines;
    }

    /**
     * JG Dec 14
     * @return
     */
        public List<DrawerOpenedLines> getDrawerOpenedLines() {
        return m_ldraweropenedlines;
    }
    
    /**
     *
     * @return
     */
    public AbstractTableModel getPaymentsModel() {
        return new AbstractTableModel() {
            @Override
            public String getColumnName(int column) {
                return AppLocal.getIntString(TICKETHEADERS[column]);
            }
            @Override
            public int getRowCount() {
                return m_lpayments.size();
            }
            @Override
            public int getColumnCount() {
                return TICKETHEADERS.length;
            }
            @Override
            public Object getValueAt(int row, int column) {
                PaymentsLine l = m_lpayments.get(row);
                switch (column) {
                    case 0: return l.getTabName();
                    case 1: return l.getSeat();
                    case 2: return l.getTimeElapsed();
                    case 3: return l.getServer();
                    case 4: 
                        TicketInfo ticket2 = null;
                        try {
                            ticket2 = dlReceipts.getSharedTicket(l.getRealSeat());
                        } catch (BasicException e) {}

                        return ticket2.printTotal();
                    default: return null;
                }
            }
        };
    }

    public void sortTableByColumn(int columnIndex, boolean ascending) {
        Comparator<PaymentsLine> comparator = new Comparator<PaymentsLine>() {
            @Override
            public int compare(PaymentsLine p1, PaymentsLine p2) {
                int returnValue = 0;
                switch (columnIndex) {
                    case 0: 
                        returnValue = p1.getTabName().compareTo(p2.getTabName());
                        break;
                    case 1: 
                        returnValue = p1.getSeat().compareTo(p2.getSeat());
                        break;
                    case 2: 
                        if (p1.getElapsed() > p2.getElapsed()) returnValue = 1;
                        else returnValue = -1;
                        break;
                    case 3: 
                        returnValue = p1.getServer().compareTo(p2.getServer());
                        break;
                    case 4:
                        TicketInfo ticket1 = null, ticket2 = null;
                        try {
                            ticket1 = dlReceipts.getSharedTicket(p1.getSeat());
                            ticket2 = dlReceipts.getSharedTicket(p2.getSeat());
                        } catch (BasicException e) {}

                        if (Double.parseDouble(ticket1.printTotal().substring(1)) > Double.parseDouble(ticket2.printTotal().substring(1))) returnValue = 1;
                        else returnValue = -1;
                        break;
                    default: break;
                }

                return ascending ? returnValue : 0 - returnValue;
            }
        };

        Collections.sort(m_lpayments, comparator);
    }

// JG 9 Nov 12
    // Products category sales class

    /**
     *
     */
        public static class CategorySalesLine implements SerializableRead {

        private String m_CategoryName;
        private Double m_CategoryUnits;
        private Double m_CategorySum;

        /**
         *
         * @param dr
         * @throws BasicException
         */
        @Override
        public void readValues(DataRead dr) throws BasicException {
            m_CategoryName = dr.getString(1);
            m_CategoryUnits = dr.getDouble(2);
            m_CategorySum = dr.getDouble(3);
        }

        /**
         *
         * @return
         */
        public String printCategoryName() {
            return m_CategoryName;
        }

        /**
         *
         * @return
         */
        public String printCategoryUnits() {
            return Formats.DOUBLE.formatValue(m_CategoryUnits);
        }

        /**
         *
         * @return
         */
        public Double getCategoryUnits() {
            return m_CategoryUnits;
        }

        /**
         *
         * @return
         */
        public String printCategorySum() {
            return Formats.CURRENCY.formatValue(m_CategorySum);
        }

        /**
         *
         * @return
         */
        public Double getCategorySum() {
            return m_CategorySum;
        }
    }    
// end
    
    /**
     * janar153 @ 29.12.2013
     */
        public static class RemovedProductLines implements SerializableRead {

            private String m_Name;
            private String m_TicketId;
            private String m_ProductName;
            private Double m_TotalUnits;
        
        @Override
        public void readValues(DataRead dr) throws BasicException {
            m_Name = dr.getString(1);
            m_TicketId = dr.getString(2);
            m_ProductName = dr.getString(3);
            m_TotalUnits = dr.getDouble(4);
        }
          
        public String printWorkerName() {
            return StringUtils.encodeXML(m_Name);
        }
        
        public String printTicketId() {
            return StringUtils.encodeXML(m_TicketId);
        }
        
        public String printProductName() {
            return StringUtils.encodeXML(m_ProductName);
        }
 
        public String printTotalUnits() {
            return Formats.DOUBLE.formatValue(m_TotalUnits);
        }
 
    }
        
    /**
     * JG Dec 14
     */
        public static class DrawerOpenedLines implements SerializableRead {

            private String m_DrawerOpened;
            private String m_Name;
            private String m_TicketId;
        
            @Override
            public void readValues(DataRead dr) throws BasicException {
                m_DrawerOpened = dr.getString(1);
                m_Name = dr.getString(2);
                m_TicketId = dr.getString(3);
            }
          
            public String printDrawerOpened() {
                return StringUtils.encodeXML(m_DrawerOpened);
            }
            
            public String printUserName() {
                return StringUtils.encodeXML(m_Name);
            }
        
            public String printTicketId() {
                return StringUtils.encodeXML(m_TicketId);
            }
        }        

        public static class ProductSalesLine implements SerializableRead {
 
        private String m_ProductName;
        private Double m_ProductUnits;
        private Double m_ProductPrice;
        private Double m_TaxRate;
        private Double m_ProductPriceTax;
        private Double m_ProductPriceNet;  //JG 7 June 2014
 
        @Override
        public void readValues(DataRead dr) throws BasicException {
            m_ProductName = dr.getString(1);
            m_ProductUnits = dr.getDouble(2);
            m_ProductPrice = dr.getDouble(3);
            m_TaxRate = dr.getDouble(4);
 
            m_ProductPriceTax = m_ProductPrice + m_ProductPrice*m_TaxRate;
            m_ProductPriceNet = m_ProductPrice * m_TaxRate;
        }
 
        public String printProductName() {
            return StringUtils.encodeXML(m_ProductName);
        }
 
        public String printProductUnits() {
            return Formats.DOUBLE.formatValue(m_ProductUnits);
        }
 
        public Double getProductUnits() {
            return m_ProductUnits;
        }
 
        public String printProductPrice() {
            return Formats.CURRENCY.formatValue(m_ProductPrice);
        }
 
        public Double getProductPrice() {
            return m_ProductPrice;
        }
 
        public String printTaxRate() {
            return Formats.PERCENT.formatValue(m_TaxRate);
        }
 
        public Double getTaxRate() {
            return m_TaxRate;
        }
 
        public String printProductPriceTax() {
            return Formats.CURRENCY.formatValue(m_ProductPriceTax);
        }
        
        public String printProductSubValue() {
            return Formats.CURRENCY.formatValue(m_ProductPriceTax*m_ProductUnits);
        }
        
        /**
         * JG 4 Jun 2014
         * @return
         */
        public String printProductPriceNet() {
            return Formats.CURRENCY.formatValue(m_ProductPrice*m_ProductUnits);
    }
        
    }

    public static class SalesLine implements SerializableRead {
        
        private String m_SalesTaxName;
        private Double m_SalesTaxes;
        private Double m_SalesTaxNet;           //JG June 2014
        private Double m_SalesTaxGross;          //JG June 2014        
        /**
         *
         * @param dr
         * @throws BasicException
         */
        @Override
        public void readValues(DataRead dr) throws BasicException {
            m_SalesTaxName = dr.getString(1);
            m_SalesTaxes = dr.getDouble(2);
            m_SalesTaxNet = dr.getDouble(3);    //JG June 2014
            m_SalesTaxGross = dr.getDouble(4);    //JG June 2014                     
        }

        /**
         *
         * @return
         */
        public String printTaxName() {
            return m_SalesTaxName;
        }      

        /**
         *
         * @return
         */
        public String printTaxes() {
            return Formats.CURRENCY.formatValue(m_SalesTaxes);
        }

        /**
         * JG June 2014
         * @return
         */
        public String printTaxNet() {
            return Formats.CURRENCY.formatValue(m_SalesTaxNet);
        }

        /**
         * JG June 2014
         * @return
         */
        public String printTaxGross() {
            return Formats.CURRENCY.formatValue(m_SalesTaxes + m_SalesTaxNet);
        }
        
        
        /**
         *
         * @return
         */
        public String getTaxName() {
            return m_SalesTaxName;
        }

        /**
         *
         * @return
         */
        public Double getTaxes() {
            return m_SalesTaxes;
        }        
        
        /**
         * JG June 2014
         * @return
         */
        public Double getTaxNet() {
            return m_SalesTaxNet;
    }

    /**
         * JG June 2014
         * @return
         */
        public Double getTaxGross() {
            return m_SalesTaxGross;
        }
    }
    
    /**
     *
     */
    public static class PaymentsLine implements SerializableRead {
        
//TAB NAME | SEAT # | TIME ELAPSED OR TIME STARTED | SERVER/USER | TOTAL
        private String tabname;
        private String seat;
        private String timeelapsed;
        private String server;
        private Long current = System.currentTimeMillis();
        /**
         *
         * @param dr
         * @throws BasicException
         */
        @Override
        public void readValues(DataRead dr) throws BasicException {
            tabname = dr.getString(1);
            seat = dr.getString(2);
            timeelapsed = dr.getString(3);
            server = dr.getString(4);
        }

        public double round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();

            long factor = (long) Math.pow(10, places);
            value = value * factor;
            long tmp = Math.round(value);
            return (double) tmp / factor;
        }

        /**
         *
         * @return
         */
        public String getTabName() {
//            if (seat.contains("[")) {
//                String[] temp = seat.split("\\[");
//                String[] tempName = tabname.split(" - ");
//
//                return tempName[0] + "[" + temp[1];
//            } else {
//                String[] tempName = tabname.split(" - ");
//
//                return tempName[0];
//            }
            return tabname;
        }

        public String getSeat() {
//            if (seat.contains("[")) {
//                String[] temp = seat.split("\\[");
//                return temp[0];
//            } else {
                return seat;
//            }
        }

        public String getRealSeat() {
            return seat;
        }

        public String getTimeElapsed() {
            JLabel timerLabel = new JLabel(String.format("%02d:%02d:%02d",0 , 0, 0));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
            LocalDateTime orderTime = LocalDateTime.parse(timeelapsed, formatter);
            Long lastTickTime = orderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            long runningTime = current - lastTickTime;
            Duration duration = Duration.ofMillis(runningTime);
            long hours = duration.toHours();
            duration = duration.minusHours(hours);
            long minutes = duration.toMinutes();
            duration = duration.minusMinutes(minutes);
            long millis = duration.toMillis();
            long seconds = millis / 1000;
            millis -= (seconds * 1000);

            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        public String getServer() {
            return server;
        }

        public String getTicketID() {
            return seat;
        }

        public Long getElapsed() {
            JLabel timerLabel = new JLabel(String.format("%02d:%02d:%02d",0 , 0, 0));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
            LocalDateTime orderTime = LocalDateTime.parse(timeelapsed, formatter);
            Long lastTickTime = orderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            long runningTime = current - lastTickTime;
            return runningTime;
        }
    }
}