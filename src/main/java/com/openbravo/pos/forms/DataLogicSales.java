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
import com.openbravo.data.model.Field;
import com.openbravo.data.model.Row;
import com.openbravo.format.DoubleUtils;
import com.openbravo.format.Formats;
import com.openbravo.pos.admin.RoleInfo;
import com.openbravo.pos.catalog.CategoryStock;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.CustomerTransaction;
import com.openbravo.pos.inventory.*;
import com.openbravo.pos.mant.FloorsInfo;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.pos.payment.PaymentInfoMagcard;
import com.openbravo.pos.payment.PaymentInfoTicket;
import com.openbravo.pos.sales.ReprintTicketInfo;
import com.openbravo.pos.suppliers.SupplierInfo;
import com.openbravo.pos.suppliers.SupplierInfoExt;
import com.openbravo.pos.ticket.*;
import com.openbravo.pos.voucher.VoucherInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 *
 * @author adrianromero
 * @author jackgerrard
 */
public class DataLogicSales extends BeanFactoryDataSingle {

    protected Session s;

    protected Datas[] auxiliarDatas;
    protected Datas[] stockdiaryDatas;
    protected Datas[] paymenttabledatas;
    protected Datas[] stockdatas;
    protected Datas[] stockAdjustDatas;

    protected Row productsRow;
    protected Row customersRow;

    private String pName;
    private Double getTotal;
    private Double getTendered;
    private String getRetMsg;
    private String getVoucher;
    private Double getTip;
    private String getCardnum;
    private String getExpDate;


    public static final String DEBT = "debt";
    public static final String DEBT_PAID = "debtpaid";
    protected static final String PREPAY = "prepay";
    private static final Logger logger = Logger.getLogger("com.openbravo.pos.forms.DataLogicSales");

    protected SentenceExec m_createCat;
    protected SentenceExec m_createSupp;

    protected AppView m_App;

    private AppConfig m_config;

    public DataLogicSales() {
        AppView app = null;
        m_config = new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
        m_config.load();
        m_App = app;

        stockdiaryDatas = new Datas[] {
                Datas.STRING, Datas.TIMESTAMP, Datas.INT, Datas.STRING,
                Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE,
                Datas.STRING, Datas.STRING, Datas.STRING};

        paymenttabledatas = new Datas[] {
                Datas.STRING, Datas.STRING, Datas.TIMESTAMP,
                Datas.STRING, Datas.STRING, Datas.DOUBLE,
                Datas.STRING};

        stockdatas = new Datas[] {
                Datas.STRING, Datas.STRING, Datas.STRING,
                Datas.DOUBLE, Datas.DOUBLE, Datas.DOUBLE};

        stockAdjustDatas = new Datas[] {
                Datas.STRING,
                Datas.STRING,
                Datas.STRING,
                Datas.DOUBLE};

        auxiliarDatas = new Datas[] {
                Datas.STRING, Datas.STRING, Datas.STRING,
                Datas.STRING, Datas.STRING, Datas.STRING};

        productsRow = new Row(
                new Field("ID", Datas.STRING, Formats.STRING),
                new Field(AppLocal.getIntString("label.prodref"), Datas.STRING, Formats.STRING, true, true, true),
                new Field(AppLocal.getIntString("label.prodbarcode"), Datas.STRING, Formats.STRING, false, true, true),
                new Field(AppLocal.getIntString("label.prodbarcodetype"), Datas.STRING, Formats.STRING, false, true, true),
                new Field(AppLocal.getIntString("label.prodname"), Datas.STRING, Formats.STRING, true, true, true),
                new Field(AppLocal.getIntString("label.prodpricebuy"), Datas.DOUBLE, Formats.CURRENCY, false, true, true),
                new Field(AppLocal.getIntString("label.prodpricesell"), Datas.DOUBLE, Formats.CURRENCY, false, true, true),
                new Field(AppLocal.getIntString("label.prodcategory"), Datas.STRING, Formats.STRING, false, false, true),
                new Field(AppLocal.getIntString("label.taxcategory"), Datas.STRING, Formats.STRING, false, false, true),
                new Field(AppLocal.getIntString("label.attributeset"), Datas.STRING, Formats.STRING, false, false, true),
                new Field("STOCKCOST", Datas.DOUBLE, Formats.CURRENCY),
                new Field("STOCKVOLUME", Datas.DOUBLE, Formats.DOUBLE),
                new Field("IMAGE", Datas.IMAGE, Formats.NULL),
                new Field("ISCOM", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("ISSCALE", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("ISCONSTANT", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("PRINTKB", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("SENDSTATUS", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("ISSERVICE", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("PROPERTIES", Datas.BYTES, Formats.NULL),
                new Field(AppLocal.getIntString("label.display"), Datas.STRING, Formats.STRING, false, true, true),
                new Field("ISVPRICE", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("ISDISABLE_REG", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("ISFASTBAR", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("ISVERPATRIB", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("TEXTTIP", Datas.STRING, Formats.STRING),
                new Field("WARRANTY", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field(AppLocal.getIntString("label.stockunits"), Datas.DOUBLE, Formats.DOUBLE),
                new Field("PRINTTO", Datas.STRING, Formats.STRING),
                new Field(AppLocal.getIntString("label.prodsupplier"), Datas.STRING, Formats.STRING, false, false, true),
                new Field(AppLocal.getIntString("label.UOM"), Datas.STRING, Formats.STRING),
                new Field("MEMODATE", Datas.TIMESTAMP, Formats.DATE),
                new Field("PRICESELLH", Datas.DOUBLE, Formats.CURRENCY, false, true, true),
                new Field("STATION_ID", Datas.STRING, Formats.STRING),
                new Field("ISCATALOG", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("CATORDER", Datas.INT, Formats.INT)
                
        );

// creating customers object here for now for future global reuse
// LOYALTY, MEMBERSHIP & etc as will be more system centric than customer

        customersRow = new Row(
                new Field("ID", Datas.STRING, Formats.STRING),
                new Field("SEARCHKEY", Datas.STRING, Formats.STRING),
                new Field("TAXID", Datas.STRING, Formats.STRING),
                new Field("NAME", Datas.STRING, Formats.STRING),
                new Field("TAXCATEGORY", Datas.STRING, Formats.STRING),
                new Field("CARD", Datas.STRING, Formats.STRING),
                new Field("MAXDEBT", Datas.DOUBLE, Formats.CURRENCY),
                new Field("ADDRESS", Datas.STRING, Formats.STRING),
                new Field("ADDRESS2", Datas.STRING, Formats.STRING),
                new Field("POSTAL", Datas.STRING, Formats.STRING),
                new Field("CITY", Datas.STRING, Formats.STRING),
                new Field("REGION", Datas.STRING, Formats.STRING),
                new Field("COUNTRY", Datas.STRING, Formats.STRING),
                new Field("FIRSTNAME", Datas.STRING, Formats.STRING),
                new Field("LASTNAME", Datas.STRING, Formats.STRING),
                new Field("EMAIL", Datas.STRING, Formats.STRING),
                new Field("PHONE", Datas.STRING, Formats.STRING),
                new Field("PHONE2", Datas.STRING, Formats.STRING),
                new Field("FAX", Datas.STRING, Formats.STRING),
                new Field("NOTES", Datas.STRING, Formats.STRING),
                new Field("VISIBLE", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("CURDATE", Datas.STRING, Formats.TIMESTAMP),
                new Field("CURDEBT", Datas.DOUBLE, Formats.CURRENCY),
                new Field("IMAGE", Datas.BYTES, Formats.NULL),
                new Field("ISVIP", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("DISCOUNT", Datas.DOUBLE, Formats.CURRENCY),
                new Field("MEMODATE", Datas.STRING, Formats.TIMESTAMP)
        );

    }

    /**
     *
     * @param s
     */
    @Override
    public void init(Session s){
        this.s = s;

        m_createCat = new StaticSentence(s,
                "INSERT INTO categories ( ID, NAME, CATSHOWNAME ) "
                        + "VALUES (?, ?, ?)"
                , new SerializerWriteBasic(new Datas[]{
                Datas.STRING,
                Datas.STRING,
                Datas.BOOLEAN})
        );

        m_createSupp = new StaticSentence(s,
                "INSERT INTO suppliers ( ID, NAME, SEARCHKEY, VISIBLE ) "
                        + "VALUES (?, ?, ?, ?)"
                , new SerializerWriteBasic(new Datas[]{
                Datas.STRING,
                Datas.STRING,
                Datas.STRING,
                Datas.BOOLEAN})
        );
    }

    // Import Creates
    public final void createCategory(Object[] category) throws BasicException {
        m_createCat.exec(category);
    }

    public final void createSupplier(Object[] supplier) throws BasicException {
        m_createSupp.exec(supplier);
    }
// End Import Creates

    public final Row getProductsRow() {
        return productsRow;
    }

    public final Row getCustomersRow() {
        return customersRow;
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final ProductInfoExt getProductInfo(String id) throws BasicException {
        return (ProductInfoExt) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISDISABLE_REG, "
                + "ISFASTBAR, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, "
                + "PRICESELLH, "
                + "STATION_ID "
                + "FROM products WHERE ID = ?"
                , SerializerWriteString.INSTANCE
                , ProductInfoExt.getSerializerRead()).find(id);
    }

    public final ProductInfoExt getProductInfoByCode(String sCode) throws BasicException {
//        if (sCode.length() == 13 && (sCode.startsWith("2") || sCode.startsWith("02")))
//            return  getProductInfoByShortCode(sCode);
//        else {
        return (ProductInfoExt) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, "
                + "PRICESELLH, "
                + "STATION_ID "
                + "FROM products WHERE CODE = ?"
                , SerializerWriteString.INSTANCE
                , ProductInfoExt.getSerializerRead()).find(sCode);
//        }
    }

    public final ProductInfoExt getProductInfoByShortCode(String sCode) throws BasicException {

        return (ProductInfoExt) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, "
                + "PRICESELLH, "
                + "STATION_ID "
                + "FROM products "
                + "WHERE SUBSTRING( CODE, 3, 6 ) = ?"
                , SerializerWriteString.INSTANCE
                , ProductInfoExt.getSerializerRead()).find(sCode.substring(2, 8));
    }

    /*
     *  Important Note:
     *  Deliberately extracted from other code to force strict UPC-A (full 12 digits)
     *  Why? Because other manf' or in-store codes may exist and we just need a single
     *  record returned. Also, handling things this way will allow use (future) of a
     *  COUPON code (5 or 9 normally used) in-store
     *
     */
    public final ProductInfoExt getProductInfoByUShortCode(String sCode) throws BasicException {

        return (ProductInfoExt) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, "
                + "PRICESELLH, "
                + "STATION_ID "
                + "FROM products "
                + "WHERE LEFT( CODE, 7 ) = ? AND CODETYPE = 'UPC-A' "
//  selection of 7 digits ie: 2123456 specific to allow for other 12 digit
//  codes that may be in use at positions 234567
//  last digit (position 7) can be used to identify COUPON (5 or 9) - FUTURE
                , SerializerWriteString.INSTANCE
                , ProductInfoExt.getSerializerRead())
                .find(sCode.substring(0, 7));
    }

    /**
     *
     * @param sReference
     * @return
     * @throws BasicException
     */
    public final ProductInfoExt getProductInfoByReference(String sReference) throws BasicException {
        return (ProductInfoExt) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, "
                + "PRICESELLH, "
                + "STATION_ID "
                + "FROM products WHERE REFERENCE = ?"
                , SerializerWriteString.INSTANCE
                , ProductInfoExt.getSerializerRead()).find(sReference);
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    public final List<CategoryInfo> getRootCategories() throws BasicException {
        return new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER, "
                + "SENDKITCHEN "
                + "FROM categories "
                + "WHERE PARENTID IS NULL AND CATSHOWNAME = " + s.DB.TRUE() + " "
                + "ORDER BY ABS(CATORDER)"
                , null
                , CategoryInfo.getSerializerRead()).list();
    }

    /**
     *
     * @param category
     * @return
     * @throws BasicException
     */
    public final List<CategoryInfo> getSubcategories(String category) throws BasicException  {
        return new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER, "
                + "SENDKITCHEN "
                + "FROM categories WHERE PARENTID = ? "
                + "ORDER BY ABS(CATORDER)"
                , SerializerWriteString.INSTANCE
                , CategoryInfo.getSerializerRead()).list(category);
    }

    /**
     *
     * @param category
     * @return
     * @throws BasicException
     */
    public List getProductCatalog(String category) throws BasicException  {
        return new PreparedSentence(s
                , "SELECT "
                + "P.ID, "
                + "P.REFERENCE, "
                + "P.CODE, "
                + "P.CODETYPE, "
                + "P.NAME, "
                + "P.PRICEBUY, "
                + "P.PRICESELL, "
                + "P.CATEGORY, "
                + "P.TAXCAT, "
                + "P.ATTRIBUTESET_ID, "
                + "P.STOCKCOST, "
                + "P.STOCKVOLUME, "
                + "P.IMAGE, "
                + "P.ISCOM, "
                + "P.ISSCALE, "
                + "P.ISCONSTANT, "
                + "P.PRINTKB, "
                + "P.SENDSTATUS, "
                + "P.ISSERVICE, "
                + "P.ATTRIBUTES, "
                + "P.DISPLAY, "
                + "P.ISVPRICE, "
                + "P.ISDISABLE_REG, "
                + "P.ISFASTBAR, "
                + "P.ISVERPATRIB, "
                + "P.TEXTTIP, "
                + "P.WARRANTY, "
                + "P.STOCKUNITS, "
                + "P.PRINTTO, "
                + "P.SUPPLIER, "
                + "P.UOM, "
                + "MEMODATE, "
                + "PRICESELLH, "
                + "STATION_ID "
                + "FROM products P, products_cat O "
                + "WHERE P.ID = O.PRODUCT AND P.CATEGORY = ? "
                + "ORDER BY O.CATORDER, P.NAME "
                , SerializerWriteString.INSTANCE
                , ProductInfoExt.getSerializerRead()).list(category);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public List<ProductInfoExt> getProductComments(String id) throws BasicException {
        return new PreparedSentence(s
                , "SELECT "
                + "P.ID, "
                + "P.REFERENCE, "
                + "P.CODE, "
                + "P.CODETYPE, "
                + "P.NAME, "
                + "P.PRICEBUY, "
                + "P.PRICESELL, "
                + "P.CATEGORY, "
                + "P.TAXCAT, "
                + "P.ATTRIBUTESET_ID, "
                + "P.STOCKCOST, "
                + "P.STOCKVOLUME, "
                + "P.IMAGE, "
                + "P.ISCOM, "
                + "P.ISSCALE, "
                + "P.ISCONSTANT, "
                + "P.PRINTKB, "
                + "P.SENDSTATUS, "
                + "P.ISSERVICE, "
                + "P.ATTRIBUTES, "
                + "P.DISPLAY, "
                + "P.ISVPRICE, "
                + "P.ISVERPATRIB, "
                + "P.TEXTTIP, "
                + "P.WARRANTY, "
                + "P.STOCKUNITS, "
                + "P.PRINTTO, "
                + "P.SUPPLIER, "
                + "P.UOM, "
                + "MEMODATE, "
                + "PRICESELLH, "
                + "STATION_ID "
                + "FROM products P, "
                + "products_cat O, products_com M "
                + "WHERE P.ID = O.PRODUCT AND P.ID = M.PRODUCT2 AND M.PRODUCT = ? "
                + "AND P.ISCOM = " + s.DB.TRUE() + " " +
                "ORDER BY O.CATORDER, P.NAME"
                , SerializerWriteString.INSTANCE
                , ProductInfoExt.getSerializerRead()).list(id);
    }

    // JG uniCenta June 2014 includes StockUnits
    /**
     *
     * @return
     * @throws BasicException
     */
    public List<ProductInfoExt> getProductConstant() throws BasicException {
        return new PreparedSentence(s
                , "SELECT "
                + "products.ID, "
                + "products.REFERENCE, "
                + "products.CODE, "
                + "products.CODETYPE, "
                + "products.NAME, "
                + "products.PRICEBUY, "
                + "products.PRICESELL, "
                + "products.CATEGORY, "
                + "products.TAXCAT, "
                + "products.ATTRIBUTESET_ID, "
                + "products.STOCKCOST, "
                + "products.STOCKVOLUME, "
                + "products.IMAGE, "
                + "products.ISCOM, "
                + "products.ISSCALE, "
                + "products.ISCONSTANT, "
                + "products.PRINTKB, "
                + "products.SENDSTATUS, "
                + "products.ISSERVICE, "
                + "products.ATTRIBUTES, "
                + "products.DISPLAY, "
                + "products.ISVPRICE, "
                + "products.ISDISABLE_REG, "
                + "products.ISFASTBAR, "
                + "products.ISVERPATRIB, "
                + "products.TEXTTIP, "
                + "products.WARRANTY, "
                + "products.STOCKUNITS, "
                + "products.PRINTTO, "
                + "products.SUPPLIER, "
                + "products.UOM, "
                + "products.MEMODATE "
                + "FROM categories INNER JOIN products ON (products.CATEGORY = categories.ID) "
                + "WHERE products.ISCONSTANT = " +s.DB.TRUE()+ " "
                + "ORDER BY categories.NAME, products.NAME",
                null,
                ProductInfoExt.getSerializerRead()).list();


    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final CategoryInfo getCategoryInfo(String id) throws BasicException {
        return (CategoryInfo) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER, "
                + "SENDKITCHEN "
                + "FROM categories "
                + "WHERE ID = ? "
                + "ORDER BY CATORDER, NAME"
                , SerializerWriteString.INSTANCE
                , CategoryInfo.getSerializerRead()).find(id);
    }

    /**
     * JG Dec 2017
     * @param pId
     * @return
     * @throws BasicException
     */
    @SuppressWarnings("unchecked")
    public final List<CategoryStock> getCategorysProductList(String pId) throws BasicException {
        return new PreparedSentence(s,
                "SELECT products.ID, " +
                        "products.NAME AS Name, " +
                        "products.CODE AS Barcode, " +
                        "categories.ID AS Category " +
                        "FROM products products " +
                        "INNER JOIN categories categories ON (products.CATEGORY = categories.ID) " +
                        "WHERE products.category = ? " +
                        "ORDER BY products.NAME ASC",
                SerializerWriteString.INSTANCE,
                CategoryStock.getSerializerRead()).list(pId);
    }

    /**
     *
     * @return
     */

    public final SentenceList getProductList() {
        return new StaticSentence(s
                , new QBFBuilder(
                "SELECT "
                        + "ID, "
                        + "REFERENCE, "
                        + "CODE, "
                        + "CODETYPE, "
                        + "NAME, "
                        + "PRICEBUY, "
                        + "PRICESELL, "
                        + "CATEGORY, "
                        + "TAXCAT, "
                        + "ATTRIBUTESET_ID, "
                        + "STOCKCOST, "
                        + "STOCKVOLUME, "
                        + "IMAGE, "
                        + "ISCOM, "
                        + "ISSCALE, "
                        + "ISCONSTANT, "
                        + "PRINTKB, "
                        + "SENDSTATUS, "
                        + "ISSERVICE, "
                        + "ATTRIBUTES, "
                        + "DISPLAY, "
                        + "ISVPRICE, "
                        + "ISDISABLE_REG, "
                        + "ISFASTBAR, "
                        + "ISVERPATRIB, "
                        + "TEXTTIP, "
                        + "WARRANTY, "
                        + "STOCKUNITS, "
                        + "PRINTTO, "
                        + "SUPPLIER, "
                        + "UOM, "
                        + "MEMODATE, "
                        + "PRICESELLH, "
                        + "STATION_ID "
                        + "FROM products "
                        + "WHERE ?(QBF_FILTER) "
                        + "ORDER BY NAME ASC",
                new String[] {"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE"})
                , new SerializerWriteBasic(new Datas[] {
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING})
                , ProductInfoExt.getSerializerRead());
    }

    /**
     *
     * @return
     */
    public SentenceList getProductListNormal() {
        return new StaticSentence(s
                , new QBFBuilder(
                "SELECT "
                        + "ID, "
                        + "REFERENCE, "
                        + "CODE, "
                        + "CODETYPE, "
                        + "NAME, "
                        + "PRICEBUY, "
                        + "PRICESELL, "
                        + "CATEGORY, "
                        + "TAXCAT, "
                        + "ATTRIBUTESET_ID, "
                        + "STOCKCOST, "
                        + "STOCKVOLUME, "
                        + "IMAGE, "
                        + "ISCOM, "
                        + "ISSCALE, "
                        + "ISCONSTANT, "
                        + "PRINTKB, "
                        + "SENDSTATUS, "
                        + "ISSERVICE, "
                        + "ATTRIBUTES, "
                        + "DISPLAY, "
                        + "ISVPRICE, "
                        + "ISVERPATRIB, "
                        + "TEXTTIP, "
                        + "WARRANTY, "
                        + "STOCKUNITS, "
                        + "PRINTTO, "
                        + "SUPPLIER, "
                        + "UOM, "
                        + "MEMODATE, "
                        + "PRICESELLH, "
                        + "STATION_ID "
                        + "FROM products "
                        + "WHERE ISCOM = " + s.DB.FALSE() + " AND ?(QBF_FILTER) ORDER BY REFERENCE",
                new String[] {"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE"})
                , new SerializerWriteBasic(new Datas[] {
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING})
                , ProductInfoExt.getSerializerRead());
    }

    /**
     *
     * @return
     */
    public SentenceList getProductsList() {
        return new StaticSentence(s
                , "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, "
                + "PRICESELLH, "
                + "STATION_ID "
                + "FROM products "
                + "ORDER BY NAME"
                , null
                , ProductInfo.getSerializerRead());
    }

    public SentenceList getProductList2() {
        return new StaticSentence(s
                , new QBFBuilder(
                "SELECT "
                        + "products.id, "
                        + "products.name, "
                        + "stockcurrent.units, "
                        + "locations.name, "
                        + "products.pricesell, "
                        + "taxes.rate, "
                        + "products.pricesell + (products.pricesell * taxes.rate) AS SellIncTax "
                        + " FROM (((stockcurrent stockcurrent "
                        + "INNER JOIN locations locations "
                        + "ON (stockcurrent.location = locations.id)) "
                        + "INNER JOIN products products "
                        + "ON (stockcurrent.product = products.id)) "
                        + "INNER JOIN taxcategories taxcategories "
                        + "ON (products.taxcat = taxcategories.id)) "
                        + "INNER JOIN taxes taxes "
                        + "ON (taxes.category = taxcategories.id) "
                        + "WHERE ?(QBF_FILTER) "
                        + "GROUP BY products.name ",
                new String[] {"NAME", "UNITS", "SellIncTax", "LOCATION",})
                , new SerializerWriteBasic(new Datas[] {
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING})
                , ProductInfoExt.getSerializerRead());
    }

    /**
     *
     * @return
     */
    public SentenceList getProductListAuxiliar() {
        return new StaticSentence(s
                , new QBFBuilder(
                "SELECT "
                        + "ID, "
                        + "REFERENCE, "
                        + "CODE, "
                        + "CODETYPE, "
                        + "NAME, "
                        + "PRICEBUY, "
                        + "PRICESELL, "
                        + "CATEGORY, "
                        + "TAXCAT, "
                        + "ATTRIBUTESET_ID, "
                        + "STOCKCOST, "
                        + "STOCKVOLUME, "
                        + "IMAGE, "
                        + "ISCOM, "
                        + "ISSCALE, "
                        + "ISCONSTANT, "
                        + "PRINTKB, "
                        + "SENDSTATUS, "
                        + "ISSERVICE, "
                        + "ATTRIBUTES, "
                        + "DISPLAY, "
                        + "ISVPRICE, "
                        + "ISVERPATRIB, "
                        + "TEXTTIP, "
                        + "WARRANTY, "
                        + "STOCKUNITS, "
                        + "PRINTTO, "
                        + "SUPPLIER, "
                        + "UOM, "
                        + "MEMODATE, "
                        + "PRICESELLH, "
                        + "STATION_ID "
                        + "FROM products "
                        + "WHERE ISCOM = " + s.DB.TRUE() + " AND ?(QBF_FILTER) "
                        + "ORDER BY REFERENCE", new String[] {"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE"})
                , new SerializerWriteBasic(new Datas[] {
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING})
                , ProductInfoExt.getSerializerRead());
    }

    /**
     *
     * @param productId The product id to look for bundle
     * @return List of products part of the searched product
     * @throws BasicException
     */
    public final List<ProductsBundleInfo> getProductsBundle(String productId) throws BasicException {
        return new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "PRODUCT, "
                + "PRODUCT_BUNDLE, "
                + "QUANTITY "
                + "FROM products_bundle WHERE PRODUCT = ?"
                , SerializerWriteString.INSTANCE
                , ProductsBundleInfo.getSerializerRead()).list(productId);
    }

    /**
     * JG Oct 2016
     * Called from JPanelTicket
     * @param pId
     * @param location
     * @return
     * @throws BasicException
     */
    public final ProductStock getProductStockState(String pId, String location) throws BasicException {

        PreparedSentence preparedSentence = new PreparedSentence(s,
                "SELECT " +
                        "products.id, " +
                        "locations.id as Location, " +
                        "stockcurrent.units AS Current, " +
                        "stocklevel.stocksecurity AS Minimum, " +
                        "stocklevel.stockmaximum AS Maximum, " +
                        "products.pricebuy, " +
                        "products.pricesell, " +
                        "products.memodate " +
                        "FROM locations " +
                        "INNER JOIN ((products " +
                        "INNER JOIN stockcurrent " +
                        "ON products.id = stockcurrent.product) " +
                        "LEFT JOIN stocklevel ON products.id = stocklevel.product) " +
                        "ON locations.id = stockcurrent.location " +
                        "WHERE products.id = ? " +
                        "AND locations.id = ?"
                , SerializerWriteString.INSTANCE
                , ProductStock.getSerializerRead());

        ProductStock productStock = (ProductStock) preparedSentence.find(pId, location);

        return productStock;
    }

    /**
     * JG May 2016
     * Called from StockManagement
     * @param pId
     * @return
     * @throws BasicException
     */
    @SuppressWarnings("unchecked")
    public final List<ProductStock> getProductStockList(String pId) throws BasicException {
        return new PreparedSentence(s,
                "SELECT products.id, " +
                        "locations.name AS Location, " +
                        "stockcurrent.units AS Current, " +
                        "stocklevel.stocksecurity AS Minimum, " +
                        "stocklevel.stockmaximum AS Maximum, " +
                        "Round(products.pricebuy,2) AS PriceBuy, " +
                        "Round((products.pricesell * taxes.rate) + products.pricesell,2) AS PriceSell, " +
                        "products.memodate " +
                        "FROM ((((taxcategories TC " +
                        "INNER JOIN taxes taxes " +
                        "ON (TC.id = taxes.category)) " +
                        "RIGHT OUTER JOIN products products " +
                        "ON (products.TAXCAT = TC.id)) " +
                        "LEFT OUTER JOIN stocklevel stocklevel " +
                        "ON (stocklevel.product = products.ID)) " +
                        "LEFT OUTER JOIN stockcurrent stockcurrent " +
                        "ON (products.ID = stockcurrent.product)) " +
                        "INNER JOIN locations locations " +
                        "ON (stockcurrent.location = locations.id) " +
                        "WHERE products.id= ? " +
                        "GROUP BY locations.name",
                SerializerWriteString.INSTANCE,
                ProductStock.getSerializerRead()).list(pId);
    }

    /**
     * JG Sept 2017
     * @return
     * @throws BasicException
     */
    @SuppressWarnings("unchecked")
    public final List<ReprintTicketInfo> getReprintTicketList() throws BasicException {

        //payment queries
        List<ReprintTicketInfo> ticketInfoList = (List<ReprintTicketInfo>) new StaticSentence(s
                , "SELECT "
                + "T.TICKETID, "
                + "T.TICKETTYPE, "
                + "R.DATENEW, "
                + "P.NAME, "
                + "C.NAME, "
                + "SUM(PM.TOTAL), "
                + "T.STATUS "
                + "FROM receipts "
                + "R JOIN tickets T ON R.ID = T.ID LEFT OUTER JOIN payments PM "
                + "ON R.ID = PM.RECEIPT LEFT OUTER JOIN customers C "
                + "ON C.ID = T.CUSTOMER LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                + "GROUP BY "
                + "T.ID, "
                + "T.TICKETID, "
                + "T.TICKETTYPE, "
                + "R.DATENEW, "
                + "P.NAME, "
                + "C.NAME "
                + "ORDER BY R.DATENEW DESC, T.TICKETID "
                + "LIMIT 10 "
                , null
                , new SerializerReadClass(ReprintTicketInfo.class)).list();

        return ticketInfoList;
    }

    /**
     *
     * @param Id
     * @return
     * @throws BasicException
     */
    public final TicketInfo getReprintTicket(String Id) throws BasicException {

        //payment queries

        if (Id == null) {
            return null;
        } else {
            Object[]record = (Object[]) new StaticSentence(s
                    , "SELECT "
                    + "T.TICKETID, "
                    + "SUM(PM.TOTAL), "
                    + "R.DATENEW, "
                    + "P.NAME, "
                    + "T.TICKETTYPE, "
                    + "C.NAME, "
                    + "T.STATUS "
                    + "FROM receipts R "
                    + "JOIN tickets T ON R.ID = T.ID LEFT OUTER JOIN payments PM "
                    + "ON R.ID = PM.RECEIPT LEFT OUTER JOIN customers C "
                    + "ON C.ID = T.CUSTOMER LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                    + "WHERE T.TICKETID = ?"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadBasic(new Datas[] {Datas.SERIALIZABLE})).find(Id);
            return record == null ? null : (TicketInfo) record[0];
        }
    }

    //Tickets and Receipt list

    // Payment queries
    public SentenceList getTicketsList() {


        StaticSentence staticSentence = new StaticSentence(s
                , new QBFBuilder(
                "SELECT "
                        + "T.TICKETID, "
                        + "T.TICKETTYPE, "
                        + "R.DATENEW, "
                        + "P.NAME, "
                        + "C.NAME, "
                        + "SUM(PM.TOTAL), "
                        + "T.STATUS, "
                        + "PM.notes "
                        + "FROM receipts R "
                        + "JOIN tickets T ON R.ID = T.ID LEFT OUTER JOIN payments PM "
                        + "ON R.ID = PM.RECEIPT LEFT OUTER JOIN customers C "
                        + "ON C.ID = T.CUSTOMER LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                        + "WHERE ?(QBF_FILTER) "
                        + "GROUP BY T.ID, T.TICKETID, T.TICKETTYPE, R.DATENEW, P.NAME, C.NAME, PM.notes "
                        + "ORDER BY R.DATENEW DESC, T.TICKETID",
                new String[]{
                        "T.TICKETID", "T.TICKETTYPE", "PM.TOTAL", "R.DATENEW", "R.DATENEW", "P.NAME", "C.NAME", "PM.NOTES"})
                , new SerializerWriteBasic(new Datas[]{
                Datas.OBJECT, Datas.INT,
                Datas.OBJECT, Datas.INT,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.TIMESTAMP,
                Datas.OBJECT, Datas.TIMESTAMP,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING})
                , new SerializerReadClass(FindTicketsInfo.class));

        return staticSentence;
    }

    //User list

    /**
     *
     * @return
     */
    public final SentenceList getUserList() {
        return new StaticSentence(s
                , "SELECT "
                + "ID, "
                + "NAME "
                + "FROM people "
                + "ORDER BY NAME"
                , null
                , (DataRead dr) -> new TaxCategoryInfo(
                dr.getString(1),
                dr.getString(2)));
    }

    /**
     *
     * @return
     */
    public final SentenceList getTaxList() {
        return new StaticSentence(s
                , "SELECT "
                + "ID, "
                + "NAME, "
                + "CATEGORY, "
                + "CUSTCATEGORY, "
                + "PARENTID, "
                + "RATE, "
                + "RATECASCADE, "
                + "RATEORDER "
                + "FROM taxes "
                + "ORDER BY NAME"
                , null
                , (DataRead dr) -> new TaxInfo(
                dr.getString(1),
                dr.getString(2),
                dr.getString(3),
                dr.getString(4),
                dr.getString(5),
                dr.getDouble(6),
                dr.getBoolean(7),
                dr.getInt(8)));
    }

    /**
     *
     * @return
     */
    public final SentenceList getCategoriesList() {
        return new StaticSentence(s
                , "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER, "
                + "SENDKITCHEN "
                + "FROM categories "
                + "ORDER BY NAME"
                , null
                , CategoryInfo.getSerializerRead());
    }
    /**
     * JG Feb 2017
     * Returns all PARENT categories
     * @return
     */
    public final SentenceList getCategoriesList_1() {
        return new StaticSentence(s
                , "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER, "
                + "SENDKITCHEN "
                + "FROM categories "
                + "WHERE PARENTID IS NULL "
                + "ORDER BY NAME"
                , null
                , CategoryInfo.getSerializerRead());
    }
    /**
     *
     * @return
     */
    public final SentenceList getSuppList() {
        return new StaticSentence(s
                , "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "NAME "
                + "FROM suppliers "
                + "ORDER BY NAME"
                , null
                , (DataRead dr) -> new SupplierInfo(
                dr.getString(1),
                dr.getString(2),
                dr.getString(3)));
    }

    /**
     *
     * @return
     */
    public final SentenceList getTaxCustCategoriesList() {
        return new StaticSentence(s
                , "SELECT "
                + "ID, "
                + "NAME "
                + "FROM taxcustcategories "
                + "ORDER BY NAME"
                , null
                , (DataRead dr) -> new TaxCustCategoryInfo(
                dr.getString(1),
                dr.getString(2)));
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final CustomerInfoExt getCustomerInfo(String id) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "CARD, "
                + "MAXDEBT, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "NOTES, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "IMAGE, "
                + "ISVIP, "
                + "DISCOUNT, "
                + "MEMODATE "
                + "FROM customers WHERE ID = ?"
                , SerializerWriteString.INSTANCE
                , new CustomerExtRead()).find(id);
    }

    /**
     * JG Apr 2017 - Revised to return Customer Id - cId param
     * @param cId
     * @return
     * @throws BasicException
     */
    @SuppressWarnings("unchecked")
    public final List<CustomerTransaction> getCustomersTransactionList(String cId) throws BasicException {

        return new PreparedSentence(s,
                "SELECT tickets.TICKETID, " +
                        "products.NAME AS PNAME, " +
                        "SUM(ticketlines.UNITS) AS UNITS, " +
                        "SUM(ticketlines.UNITS * ticketlines.PRICE) AS AMOUNT, " +
                        "SUM(ticketlines.UNITS * ticketlines.PRICE * (1.0 + taxes.RATE)) AS TOTAL, " +
                        "receipts.DATENEW, " +
                        "customers.ID AS CID " +
                        "FROM ((((ticketlines ticketlines " +
                        "CROSS JOIN taxes taxes ON (ticketlines.TAXID = taxes.ID)) " +
                        "INNER JOIN tickets tickets ON (tickets.ID = ticketlines.TICKET)) " +
                        "INNER JOIN customers customers ON (customers.ID = tickets.CUSTOMER)) " +
                        "INNER JOIN receipts receipts ON (tickets.ID = receipts.ID)) " +
                        "LEFT OUTER JOIN products products ON (ticketlines.PRODUCT = products.ID) " +
                        "WHERE tickets.CUSTOMER = ? " +
                        "GROUP BY customers.ID, receipts.DATENEW, tickets.TICKETID, " +
                        "products.NAME, tickets.TICKETTYPE " +
                        "ORDER BY receipts.DATENEW DESC",
                SerializerWriteString.INSTANCE,
                CustomerTransaction.getSerializerRead()).list(cId);
    }

    /**
     *
     * @return
     */
    public final SentenceList getTaxCategoriesList() {
        return new StaticSentence(s
                , "SELECT "
                + "ID, "
                + "NAME "
                + "FROM taxcategories "
                + "ORDER BY NAME"
                , null
                , (DataRead dr) -> new TaxCategoryInfo(dr.getString(1), dr.getString(2)));
    }

    /**
     *
     * @return
     */
    public final SentenceList getStationsList() {
        return new StaticSentence(s
                , "SELECT "
                + "ID, "
                + "NAME "
                + "FROM stations "
                + "ORDER BY NAME"
                , null
                , (DataRead dr) -> new StationInfo(dr.getString(1), dr.getString(2)));
    }

    /**
     *
     * @return
     */
    public final SentenceList getRolesList() {
        return new StaticSentence(s
            , "SELECT ID, NAME FROM roles ORDER BY NAME"
            , null
            , new SerializerReadClass(RoleInfo.class));
    }

    /**
     *
     * @return
     */
    public final SentenceList getAttributeSetList() {
        return new StaticSentence(s
                , "SELECT "
                + "ID, "
                + "NAME "
                + "FROM attributeset "
                + "ORDER BY NAME"
                , null
                , (DataRead dr) -> new AttributeSetInfo(dr.getString(1), dr.getString(2)));
    }

    /**
     *
     * @return
     */
    public final SentenceList getLocationsList() {
        return new StaticSentence(s
                , "SELECT "
                + "ID, "
                + "NAME, "
                + "ADDRESS FROM locations "
                + "ORDER BY NAME"
                , null
                , new SerializerReadClass(LocationInfo.class));
    }

    /**
     *
     * @return
     */
    public final SentenceList getFloorsList() {
        return new StaticSentence(s
                , "SELECT ID, NAME FROM floors ORDER BY NAME"
                , null
                , new SerializerReadClass(FloorsInfo.class));
    }
    /**
     *
     * @return
     */
    public final SentenceList getFloorTablesList() {
        return new StaticSentence(s
                , "SELECT ID, NAME, SEATS, width, height FROM places ORDER BY NAME"
                , null
                , new SerializerReadClass(FloorsInfo.class));
    }


    /**
     *
     * @param card
     * @return
     * @throws BasicException
     */
    public CustomerInfoExt findCustomerExt(String card) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "TAXID, "
                + "SEARCHKEY, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "CARD, "
                + "MAXDEBT, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "NOTES, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "IMAGE, "
                + "ISVIP, "
                + "DISCOUNT, "
                + "MEMODATE "
                + "FROM customers "
                + "WHERE CARD = ? AND VISIBLE = " + s.DB.TRUE() + " "
                + "ORDER BY NAME"
                , SerializerWriteString.INSTANCE
                , new CustomerExtRead()).find(card);
    }
    /**
     *
     * @param name
     * @return
     * @throws BasicException
     */
    public CustomerInfoExt findCustomerName(String name) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "CARD, "
                + "MAXDEBT, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "NOTES, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "IMAGE, "
                + "ISVIP, "
                + "DISCOUNT, "
                + "MEMODATE "
                + "FROM customers "
                + "WHERE NAME = ? AND VISIBLE = " + s.DB.TRUE() + " "
                + "ORDER BY NAME"
                , SerializerWriteString.INSTANCE
                , new CustomerExtRead()).find(name);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public CustomerInfoExt loadCustomerExt(String id) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "CARD, "
                + "MAXDEBT, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "NOTES, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "IMAGE, "
                + "ISVIP, "
                + "DISCOUNT, "
                + "MEMODATE "
                + "FROM customers WHERE ID = ?"
                , SerializerWriteString.INSTANCE
                , new CustomerExtRead()).find(id);
    }

    /**
     * Quick Customer create
     * @param id
     * @return
     * @throws BasicException
     */
    public CustomerInfoExt qCustomerExt(String id) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "MAXDEBT, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "ISVIP, "
                + "DISCOUNT "
                + "FROM customers WHERE ID = ?"
                , SerializerWriteString.INSTANCE
                , new CustomerExtRead()).find(id);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final boolean isCashActive(String id) throws BasicException {

        return new PreparedSentence(s,
                "SELECT MONEY FROM closedcash WHERE DATEEND IS NULL AND MONEY = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadString.INSTANCE).find(id)
                != null;
    }

    /**
     *
     * @param tickettype
     * @param ticketid
     * @return
     * @throws BasicException
     */
    public final TicketInfo loadTicket(final int tickettype, final int ticketid) throws BasicException {
        TicketInfo ticket = (TicketInfo) new PreparedSentence(s
                , "SELECT "
                + "T.ID, "
                + "T.TICKETTYPE, "
                + "T.TICKETID, "
                + "R.DATENEW, "
                + "R.MONEY, "
                + "R.ATTRIBUTES, "
                + "P.ID, "
                + "P.NAME, "
                + "T.CUSTOMER, "
                + "T.STATUS, "
                + "T.TIPS "
                + "FROM receipts R "
                + "JOIN tickets T ON R.ID = T.ID "
                + "LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                + "WHERE T.TICKETTYPE = ? AND T.TICKETID = ? "
                + "ORDER BY R.DATENEW DESC"
                , SerializerWriteParams.INSTANCE
                , new SerializerReadClass(TicketInfo.class))
                .find(new DataParams() {
                    @Override
                    public void writeValues() throws BasicException {
                        setInt(1, tickettype);
                        setInt(2, ticketid);
                    }});

        if (ticket != null) {

            String customerid = ticket.getCustomerId();
            ticket.setCustomer(customerid == null
                    ? null
                    : loadCustomerExt(customerid));

            ticket.setLines(new PreparedSentence(s
                    , "SELECT L.TICKET, L.LINE, L.PRODUCT, L.ATTRIBUTESETINSTANCE_ID, " +
                    "L.UNITS, L.PRICE, T.ID, T.NAME, T.CATEGORY, T.CUSTCATEGORY, " +
                    "T.PARENTID, T.RATE, T.RATECASCADE, T.RATEORDER, L.ATTRIBUTES " +
                    "FROM ticketlines L, taxes T " +
                    "WHERE L.TAXID = T.ID AND L.TICKET = ? ORDER BY L.LINE"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadClass(TicketLineInfo.class)).list(ticket.getId()));

            ticket.setPayments(new PreparedSentence(s
                    , "SELECT PAYMENT, TOTAL, TRANSID, TENDERED, CARDNUM FROM payments WHERE RECEIPT = ?"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadClass(PaymentInfoTicket.class)).list(ticket.getId()));
        }
        return ticket;
    }

    /**
     *
     * @param ticket
     * @param location
     * @throws BasicException
     */
    public final void saveTicket(final TicketInfo ticket, final String location) throws BasicException {

        Transaction t;
        t = new Transaction(s) {
            @Override
            public Object transact() throws BasicException {

                // Set Receipt Id
                if (ticket.getTicketId() == 0) {
                    switch (ticket.getTicketType()) {
                        case TicketInfo.RECEIPT_NORMAL:
                            ticket.setTicketId(getNextTicketIndex());
                            break;
                        case TicketInfo.RECEIPT_REFUND:
                            ticket.setTicketId(getNextTicketRefundIndex());
                            break;
                        case TicketInfo.RECEIPT_PAYMENT:
                            ticket.setTicketId(getNextTicketPaymentIndex());
                            break;
                        case TicketInfo.RECEIPT_NOSALE:
                            ticket.setTicketId(getNextTicketPaymentIndex());
                            break;
                        default:
                            throw new BasicException();
                    }
                }

                new PreparedSentence(s
                        , "INSERT INTO receipts (ID, MONEY, DATENEW, ATTRIBUTES, PERSON) VALUES (?, ?, ?, ?, ?)"
                        , SerializerWriteParams.INSTANCE )
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, ticket.getId());
                                setString(2, ticket.getActiveCash());
                                setTimestamp(3, ticket.getDate());

                                try {
                                    ByteArrayOutputStream o = new ByteArrayOutputStream();
                                    ticket.getProperties().storeToXML(o, AppLocal.APP_NAME, "UTF-8");
                                    setBytes(4, o.toByteArray());
                                } catch (IOException e) {
                                    setBytes(4, null);
                                }
                                setString(5, ticket.getProperty("person"));
                            }
                        });

                // new ticket
                new PreparedSentence(s
                        , "INSERT INTO tickets (ID, TICKETTYPE, TICKETID, PERSON, CUSTOMER, STATUS, TIPS) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)"
                        , SerializerWriteParams.INSTANCE )
                        .exec(new DataParams() {
                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, ticket.getId());
                                setInt(2, ticket.getTicketType());
                                setInt(3, ticket.getTicketId());
                                setString(4, ticket.getUser().getId());
                                setString(5, ticket.getCustomerId());
                                setInt(6, ticket.getTicketStatus());
                                setDouble(7, ticket.getTipCash());
                            }
                        });


                LocalDateTime now = LocalDateTime.now();

                Object[] values = new Object[] {
                    ticket.getTicketId(),
                    ticket.getName(), 
                    ticket.getUser().getId(),
                    new Timestamp(ticket.getDate().getTime()),
                    Timestamp.valueOf(now),
                    ticket
                };

                Datas[] datas;
                datas = new Datas[] {
                    Datas.INT, 
                    Datas.STRING, 
                    Datas.STRING, 
                    Datas.TIMESTAMP,
                    Datas.TIMESTAMP,
                    Datas.SERIALIZABLE
                };

                new PreparedSentence(s, "update places set ticketid = NULL, occupied = NULL where id = ?", SerializerWriteString.INSTANCE).exec(ticket.getId());

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

                // update status of existing ticket
                new PreparedSentence(s
                        , "UPDATE tickets SET STATUS = ? "
                        + "WHERE TICKETTYPE = 0 AND TICKETID = ?"
                        , SerializerWriteParams.INSTANCE )
                        .exec(new DataParams() {
                            @Override
                            public void writeValues() throws BasicException {
                                setInt(1, ticket.getTicketId());
                                setInt(2, ticket.getTicketStatus());
                            }
                        });

                SentenceExec ticketlineinsert = new PreparedSentence(s
                        , "INSERT INTO ticketlines (TICKET, LINE, "
                        + "PRODUCT, ATTRIBUTESETINSTANCE_ID, "
                        + "UNITS, PRICE, TAXID, COMP, DISCOUNT, ATTRIBUTES, NOTES) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        , SerializerWriteBuilder.INSTANCE);

                for (TicketLineInfo l : ticket.getLines()) {
                    ticketlineinsert.exec(l);

                    if (l.getProductID() != null && l.isProductService() != true)  {
                        getStockDiaryInsert().exec(new Object[] {
                                UUID.randomUUID().toString(),
                                ticket.getDate(),
                                l.getMultiply() < 0.0
                                        ? MovementReason.IN_REFUND.getKey()
                                        : MovementReason.OUT_SALE.getKey(),
                                location,
                                l.getProductID(),
                                l.getProductAttSetInstId(), -l.getMultiply(), l.getPrice(),
                                ticket.getUser().getName()
                        });
                    }
                }

                final Payments payments = new Payments();
                SentenceExec paymentinsert = new PreparedSentence(s
                        , "INSERT INTO payments (ID, RECEIPT, PAYMENT, TOTAL, TRANSID, RETURNMSG, "
                        + "TENDERED, CARDNUM, VOUCHER, NOTES, TIP) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        , SerializerWriteParams.INSTANCE);

                ticket.getPayments().forEach((p) -> {
                    if (p instanceof PaymentInfoMagcard) {
                        payments.addPayment(p.getName(), p.getTotal(), p.getPaid(), ticket.getReturnMessage(), p.getVoucher(), ((PaymentInfoMagcard) p).getLastFourDigits(), 
                                p.getTip(), p.getCardNumber(), p.getExpDate());
                    } else {
                        payments.addPayment(p.getName(), p.getTotal(), p.getPaid(), ticket.getReturnMessage(), p.getVoucher(), null, 
                                p.getTip(), p.getCardNumber(), p.getExpDate());
                    }
                });

                while (payments.getSize() >= 1){
                    paymentinsert.exec(new DataParams() {
                        @Override
                        public void writeValues() throws BasicException {
                            pName = payments.getFirstElement();
                            getTotal = round(payments.getPaidAmount(pName), 2);
                            getTendered = round(payments.getTendered(pName), 2);
                            getRetMsg = payments.getRtnMessage(pName);
                            getTip = payments.getTip(pName);
                            getCardnum = payments.getCardnum(pName);
                            getExpDate = payments.getExpDate(pName);

                            if (pName.contains("voucher")) {
                                getVoucher = payments.getVoucher(pName);
                            }
                            payments.removeFirst(pName);

                            setString(1, UUID.randomUUID().toString());
                            setString(2, ticket.getId());
                            setString(3, pName);
                            setDouble(4, getTotal);
                            setString(5, ticket.getTransactionID());
                            setBytes(6, (byte[]) Formats.BYTEA.parseValue(getRetMsg));
                            setDouble(7, getTendered);
                            setString(8, getCardnum);
                            setString(9, getVoucher);
                            setString(10, getExpDate);
                            setDouble(11, getTip);
                            payments.removeFirst(pName);
                        }
                    });

                    if (pName.contains("voucher")) {
                        getVoucherNonActive().exec(payments.getVoucher(pName));
                    }

                    if ("debt".equals(pName) || "debtpaid".equals(pName)) {
                        ticket.getCustomer().updateCurDebt(getTotal, ticket.getDate());
                        getDebtUpdate().exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setDouble(1, ticket.getCustomer().getAccdebt());
                                setTimestamp(2, ticket.getCustomer().getCurdate());
                                setString(3, ticket.getCustomer().getId());
                            }
                        });
                    }
                }

                SentenceExec taxlinesinsert = new PreparedSentence(s
                        , "INSERT INTO taxlines (ID, RECEIPT, TAXID, BASE, AMOUNT)  "
                        + "VALUES (?, ?, ?, ?, ?)"
                        , SerializerWriteParams.INSTANCE);

                if (ticket.getTaxes() != null) {
                    for (final TicketTaxInfo tickettax: ticket.getTaxes()) {
                        taxlinesinsert.exec(new DataParams() {
                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, UUID.randomUUID().toString());
                                setString(2, ticket.getId());
                                setString(3, tickettax.getTaxInfo().getId());
                                setDouble(4, tickettax.getSubTotal());
                                setDouble(5, DoubleUtils.fixDecimals(tickettax.getTax()));
                            }
                        });
                    }
                }
                return null;
            }
        };

        t.execute();
    }

    /**
     *
     * @param ticket
     * @param location
     * @throws BasicException
     */
    public final void deleteTicket(final TicketInfo ticket, final String location) throws BasicException {

        Transaction t;
        t = new Transaction(s) {
            @Override
            public Object transact() throws BasicException {

                // update the inventory
                Date d = new Date();
                for (int i = 0; i < ticket.getLinesCount(); i++) {
                    if (ticket.getLine(i).getProductID() != null)  {
                        // Hay que actualizar el stock si el hay producto
                        getStockDiaryInsert().exec( new Object[] {
                                UUID.randomUUID().toString(),
                                d,
                                ticket.getLine(i).getMultiply() >= 0.0
                                        ? MovementReason.IN_REFUND.getKey()
                                        : MovementReason.OUT_SALE.getKey(),
                                location,
                                ticket.getLine(i).getProductID(),
                                ticket.getLine(i).getProductAttSetInstId(), ticket.getLine(i).getMultiply(), ticket.getLine(i).getPrice(),
                                ticket.getUser().getName()
                        });
                    }
// For productBundle
                    List<ProductsBundleInfo> bundle = getProductsBundle((String)ticket.getLine(i).getProductID());

                    if (bundle.size() > 0) {
                        for (ProductsBundleInfo bundleComponent : bundle) {
                            ProductInfoExt bundleProduct = getProductInfo(bundleComponent.getProductBundleId());

                            getStockDiaryInsert().exec(new Object[]{
                                    UUID.randomUUID().toString(),
                                    d,
                                    ticket.getLine(i).getMultiply() * bundleComponent.getQuantity()  >= 0.0
                                            ? MovementReason.IN_REFUND.getKey()
                                            : MovementReason.OUT_SALE.getKey(),
                                    location,
                                    bundleComponent.getProductBundleId(),
                                    null, ticket.getLine(i).getMultiply() * bundleComponent.getQuantity()
                                    , bundleProduct.getPriceSell(),
                                    ticket.getUser().getName()});
                        }
                    }
                }

                // update customer debts
                for (PaymentInfo paymentInfo : ticket.getPayments()) {
                    if ("debt".equals(paymentInfo.getName()) || "debtpaid".equals(paymentInfo.getName())) {

                        // update customer fields...
                        ticket.getCustomer().updateCurDebt(-paymentInfo.getTotal(), ticket.getDate());

                        // save customer fields...
                        getDebtUpdate().exec(new DataParams() {
                            @Override
                            public void writeValues() throws BasicException {
                                setDouble(1, ticket.getCustomer().getAccdebt());
                                setTimestamp(2, ticket.getCustomer().getCurdate());
                                setString(3, ticket.getCustomer().getId());
                            }});
                    }
                }

                // and delete the receipt
                new StaticSentence(s
                        , "DELETE FROM taxlines WHERE RECEIPT = ?"
                        , SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s
                        , "DELETE FROM payments WHERE RECEIPT = ?"
                        , SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s
                        , "DELETE FROM ticketlines WHERE TICKET = ?"
                        , SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s
                        , "DELETE FROM tickets WHERE ID = ?"
                        , SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s
                        , "DELETE FROM receipts WHERE ID = ?"
                        , SerializerWriteString.INSTANCE).exec(ticket.getId());
                return null;
            }
        };
        t.execute();
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    public final Integer getNextPickupIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "pickup_number").find();
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    public final Integer getNextTicketIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "ticketsnum").find();
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    public final Integer getNextTicketRefundIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "ticketsnum_refund").find();
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    public final Integer getNextTicketPaymentIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "ticketsnum_payment").find();
    }

    // JG 3 Feb 16 - Product load speedup
    public final SentenceFind getProductImage() {
        return new PreparedSentence(s,
                "SELECT IMAGE FROM products WHERE ID = ?",
                SerializerWriteString.INSTANCE
                , (DataRead dr) -> ImageUtils.readImage(dr.getBytes(1)));
    }

    /**
     * Loads on ProductsEditor
     * @return
     */
    public final SentenceList getProductCatQBF() {

        return new StaticSentence(s
                , new QBFBuilder(
                "SELECT "
                        + "P.ID, "
                        + "P.REFERENCE, "
                        + "P.CODE, "
                        + "P.CODETYPE, "
                        + "P.NAME, "
                        + "P.PRICEBUY, "
                        + "P.PRICESELL, "
                        + "P.CATEGORY, "
                        + "P.TAXCAT, "
                        + "P.ATTRIBUTESET_ID, "
                        + "P.STOCKCOST, "
                        + "P.STOCKVOLUME, "
                        + s.DB.CHAR_NULL() + ","
                        + "P.ISCOM, "
                        + "P.ISSCALE, "
                        + "P.ISCONSTANT, "
                        + "P.PRINTKB, "
                        + "P.SENDSTATUS, "
                        + "P.ISSERVICE, "
                        + "P.ATTRIBUTES, "
                        + "P.DISPLAY, "
                        + "P.ISVPRICE, "
                        + "P.ISDISABLE_REG, "
                        + "P.ISFASTBAR, "
                        + "P.ISVERPATRIB, "
                        + "P.TEXTTIP, "
                        + "P.WARRANTY, "
                        + "P.STOCKUNITS, "
                        + "P.PRINTTO, "
                        + "P.SUPPLIER, "
                        + "P.UOM, "
                        + "MEMODATE, "
                        + "PRICESELLH, "
                        + "STATION_ID, "
                        + "CASE WHEN "
                        + "C.PRODUCT IS NULL "
                        + "THEN " + s.DB.FALSE()
                        + " ELSE " + s.DB.TRUE()
                        + " END, "
                        + "C.CATORDER "
                        + "FROM products P LEFT OUTER JOIN products_cat C "
                        + "ON P.ID = C.PRODUCT "
                        + "WHERE ?(QBF_FILTER) "
                        + "ORDER BY P.REFERENCE",
                new String[] {
                        "P.NAME", "P.PRICEBUY", "P.PRICESELL", "P.CATEGORY", "P.CODE"})
                , new SerializerWriteBasic(new Datas[] {
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING})
                , productsRow.getSerializerRead());
    }

    public final SentenceList getProductCatQBFByCategory(String category) {

        String temp = "";
        if(!category.isEmpty()) temp = "AND P.category = '" + category + "' ";

        return new StaticSentence(s
                , new QBFBuilder(
                "SELECT "
                        + "P.ID, "
                        + "P.REFERENCE, "
                        + "P.CODE, "
                        + "P.CODETYPE, "
                        + "P.NAME, "
                        + "P.PRICEBUY, "
                        + "P.PRICESELL, "
                        + "P.CATEGORY, "
                        + "P.TAXCAT, "
                        + "P.ATTRIBUTESET_ID, "
                        + "P.STOCKCOST, "
                        + "P.STOCKVOLUME, "
                        + s.DB.CHAR_NULL() + ","
                        + "P.ISCOM, "
                        + "P.ISSCALE, "
                        + "P.ISCONSTANT, "
                        + "P.PRINTKB, "
                        + "P.SENDSTATUS, "
                        + "P.ISSERVICE, "
                        + "P.ATTRIBUTES, "
                        + "P.DISPLAY, "
                        + "P.ISVPRICE, "
                        + "P.ISDISABLE_REG, "
                        + "P.ISFASTBAR, "
                        + "P.ISVERPATRIB, "
                        + "P.TEXTTIP, "
                        + "P.WARRANTY, "
                        + "P.STOCKUNITS, "
                        + "P.PRINTTO, "
                        + "P.SUPPLIER, "
                        + "P.UOM, "
                        + "MEMODATE, "
                        + "PRICESELLH, "
                        + "STATION_ID, "
                        + "CASE WHEN "
                        + "C.PRODUCT IS NULL "
                        + "THEN " + s.DB.FALSE()
                        + " ELSE " + s.DB.TRUE()
                        + " END, "
                        + "C.CATORDER "
                        + "FROM products P LEFT OUTER JOIN products_cat C "
                        + "ON P.ID = C.PRODUCT "
                        + "WHERE ?(QBF_FILTER) " + temp
                        + "ORDER BY P.REFERENCE",
                new String[] {
                        "P.NAME", "P.PRICEBUY", "P.PRICESELL", "P.CATEGORY", "P.CODE"})
                , new SerializerWriteBasic(new Datas[] {
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING})
                , productsRow.getSerializerRead());
    }

    public final SentenceList getProductCatQBFFastBar() {

        return new StaticSentence(s
                , new QBFBuilder(
                "SELECT "
                        + "P.ID, "
                        + "P.REFERENCE, "
                        + "P.CODE, "
                        + "P.CODETYPE, "
                        + "P.NAME, "
                        + "P.PRICEBUY, "
                        + "P.PRICESELL, "
                        + "P.CATEGORY, "
                        + "P.TAXCAT, "
                        + "P.ATTRIBUTESET_ID, "
                        + "P.STOCKCOST, "
                        + "P.STOCKVOLUME, "
                        + s.DB.CHAR_NULL() + ","
                        + "P.ISCOM, "
                        + "P.ISSCALE, "
                        + "P.ISCONSTANT, "
                        + "P.PRINTKB, "
                        + "P.SENDSTATUS, "
                        + "P.ISSERVICE, "
                        + "P.ATTRIBUTES, "
                        + "P.DISPLAY, "
                        + "P.ISVPRICE, "
                        + "P.ISDISABLE_REG, "
                        + "P.ISFASTBAR, "
                        + "P.ISVERPATRIB, "
                        + "P.TEXTTIP, "
                        + "P.WARRANTY, "
                        + "P.STOCKUNITS, "
                        + "P.PRINTTO, "
                        + "P.SUPPLIER, "
                        + "P.UOM, "
                        + "MEMODATE, "
                        + "PRICESELLH, "
                        + "STATION_ID, "
                        + "CASE WHEN "
                        + "C.PRODUCT IS NULL "
                        + "THEN " + s.DB.FALSE()
                        + " ELSE " + s.DB.TRUE()
                        + " END, "
                        + "C.CATORDER "
                        + "FROM products P LEFT OUTER JOIN products_cat C "
                        + "ON P.ID = C.PRODUCT "
                        + "WHERE ?(QBF_FILTER) AND P.ISFASTBAR = true "
                        + "ORDER BY P.REFERENCE",
                new String[] {
                        "P.NAME", "P.PRICEBUY", "P.PRICESELL", "P.CATEGORY", "P.CODE"})
                , new SerializerWriteBasic(new Datas[] {
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING})
                , productsRow.getSerializerRead());
    }

    public final SentenceList getProductCatQBFLiquor() {

        return new StaticSentence(s
                , new QBFBuilder(
                "SELECT "
                        + "P.ID, "
                        + "P.REFERENCE, "
                        + "P.CODE, "
                        + "P.CODETYPE, "
                        + "P.NAME, "
                        + "P.PRICEBUY, "
                        + "P.PRICESELL, "
                        + "P.CATEGORY, "
                        + "P.TAXCAT, "
                        + "P.ATTRIBUTESET_ID, "
                        + "P.STOCKCOST, "
                        + "P.STOCKVOLUME, "
                        + s.DB.CHAR_NULL() + ","
                        + "P.ISCOM, "
                        + "P.ISSCALE, "
                        + "P.ISCONSTANT, "
                        + "P.PRINTKB, "
                        + "P.SENDSTATUS, "
                        + "P.ISSERVICE, "
                        + "P.ATTRIBUTES, "
                        + "P.DISPLAY, "
                        + "P.ISVPRICE, "
                        + "P.ISDISABLE_REG, "
                        + "P.ISFASTBAR, "
                        + "P.ISVERPATRIB, "
                        + "P.TEXTTIP, "
                        + "P.WARRANTY, "
                        + "P.STOCKUNITS, "
                        + "P.PRINTTO, "
                        + "P.SUPPLIER, "
                        + "P.UOM, "
                        + "MEMODATE, "
                        + "PRICESELLH, "
                        + "STATION_ID, "
                        + "CASE WHEN "
                        + "C.PRODUCT IS NULL "
                        + "THEN " + s.DB.FALSE()
                        + " ELSE " + s.DB.TRUE()
                        + " END, "
                        + "C.CATORDER "
                        + "FROM products P LEFT OUTER JOIN products_cat C ON P.ID = C.PRODUCT LEFT OUTER JOIN categories Cat ON P.CATEGORY = Cat.ID "
                        + "WHERE ?(QBF_FILTER) AND Cat.PARENTID = '000' "
                        + "ORDER BY P.REFERENCE",
                new String[] {
                        "P.NAME", "P.PRICEBUY", "P.PRICESELL", "P.CATEGORY", "P.CODE"})
                , new SerializerWriteBasic(new Datas[] {
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING})
                , productsRow.getSerializerRead());
    }

    /**
     *
     * @return
     */

    public final SentenceExec getProductCatInsert() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] values = (Object[]) params;
                int i = new PreparedSentence(s
                        , "INSERT INTO products ("
                        + "ID, "
                        + "REFERENCE, "
                        + "CODE, "
                        + "CODETYPE, "
                        + "NAME, "
                        + "PRICEBUY, "
                        + "PRICESELL, "
                        + "CATEGORY, "
                        + "TAXCAT, "
                        + "ATTRIBUTESET_ID, "
                        + "STOCKCOST, "
                        + "STOCKVOLUME, "
                        + "IMAGE, "
                        + "ISCOM, "
                        + "ISSCALE, "
                        + "ISCONSTANT, "
                        + "PRINTKB, "
                        + "SENDSTATUS, "
                        + "ISSERVICE, "
                        + "ATTRIBUTES, "
                        + "DISPLAY, "
                        + "ISVPRICE, "
                        + "ISDISABLE_REG, "
                        + "ISFASTBAR, "
                        + "ISVERPATRIB, "
                        + "TEXTTIP, "
                        + "WARRANTY, "
                        + "STOCKUNITS, "
                        + "PRINTTO, "
                        + "SUPPLIER, "
                        + "UOM, "
                        + "MEMODATE, "
                        + "PRICESELLH,"
                        + "STATION_ID ) "
                        + "VALUES ("
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        , new SerializerWriteBasicExt(productsRow.getDatas(),
                        new int[]{0,
                                1, 2, 3, 4, 5, 6,
                                7, 8, 9, 10, 11, 12,
                                13, 14, 15, 16, 17, 18,
                                19, 20, 21, 22, 23, 24,
                                25, 26, 27, 28, 29, 30, 31, 32, 33}))

                        .exec(params);

                if (i > 0 && ((Boolean)values[34])) {
                    return new PreparedSentence(s
                            , "INSERT INTO products_cat (PRODUCT, CATORDER) VALUES (?, ?)"
                            , new SerializerWriteBasicExt(productsRow.getDatas(), new int[] {0, 35}))
                            .exec(params);
                } else {
                    return i;
                }
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getProductCatUpdate() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] values = (Object[]) params;
                int i = new PreparedSentence(s
                        , "UPDATE products SET "
                        + "ID = ?, "
                        + "REFERENCE = ?, "
                        + "CODE = ?, "
                        + "CODETYPE = ?, "
                        + "NAME = ?, "
                        + "PRICEBUY = ?, "
                        + "PRICESELL = ?, "
                        + "CATEGORY = ?, "
                        + "TAXCAT = ?, "
                        + "ATTRIBUTESET_ID = ?, "
                        + "STOCKCOST = ?, "
                        + "STOCKVOLUME = ?, "
                        + "IMAGE = ?, "
                        + "ISCOM = ?, "
                        + "ISSCALE = ?, "
                        + "ISCONSTANT = ?, "
                        + "PRINTKB = ?, "
                        + "SENDSTATUS = ?, "
                        + "ISSERVICE = ?,  "
                        + "ATTRIBUTES = ?,"
                        + "DISPLAY = ?, "
                        + "ISVPRICE = ?, "
                        + "ISDISABLE_REG = ?, "
                        + "ISFASTBAR= ?, "
                        + "ISVERPATRIB = ?, "
                        + "TEXTTIP = ?, "
                        + "WARRANTY = ?, "
                        + "STOCKUNITS = ?, "
                        + "PRINTTO = ?, "
                        + "SUPPLIER = ?, "
                        + "UOM = ?, "
                        + "MEMODATE = ?, "
                        + "PRICESELLH = ?, "
                        + "STATION_ID = ? "
                        + "WHERE ID = ?"
                        , new SerializerWriteBasicExt(productsRow.getDatas(),
                        new int[]{0,
                                1, 2, 3, 4, 5,
                                6, 7, 8, 9, 10,
                                11, 12, 13, 14, 15,
                                16, 17, 18, 19, 20,
                                21, 22, 23, 24, 25,
                                26, 27, 28, 29, 30, 31, 32, 33, 0}))
                        .exec(params);
                if (i > 0) {
                    if (((Boolean)values[34])) {
                        if (new PreparedSentence(s
                                , "UPDATE products_cat SET CATORDER = ? WHERE PRODUCT = ?"
                                , new SerializerWriteBasicExt(productsRow.getDatas()
                                , new int[] {35, 0})).exec(params) == 0) {
                            new PreparedSentence(s
                                    , "INSERT INTO products_cat (PRODUCT, CATORDER) VALUES (?, ?)"
                                    , new SerializerWriteBasicExt(productsRow.getDatas(), new int[] {0, 35})).exec(params);
                        }
                    } else {
                        new PreparedSentence(s
                                , "DELETE FROM products_cat WHERE PRODUCT = ?"
                                , new SerializerWriteBasicExt(productsRow.getDatas(), new int[] {0})).exec(params);
                    }
                }
                return i;
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getProductCatDelete() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                new PreparedSentence(s
                        , "DELETE FROM products_cat WHERE PRODUCT = ?"
                        , new SerializerWriteBasicExt(productsRow.getDatas(), new int[] {0})).exec(params);
                return new PreparedSentence(s
                        , "DELETE FROM products WHERE ID = ?"
                        , new SerializerWriteBasicExt(productsRow.getDatas(), new int[] {0})).exec(params);
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getDebtUpdate() {

        return new PreparedSentence(s
                , "UPDATE customers SET CURDEBT = ?, CURDATE = ? WHERE ID = ?"
                , SerializerWriteParams.INSTANCE);
    }

    /**
     * ProductBundle version
     * @return
     */
    public final SentenceExec getStockDiaryInsert() {
        return new SentenceExecTransaction(s) {
            @Override
            /**
             * @param params[0] String     STOCKDIARY.ID
             * @param params[1] Date       Timestamp
             * @param params[2] Integer    Reason
             * @param params[3] String     Location
             * @param params[4] String     Product ID
             * @param params[5] String     Attribute instance ID
             * @param params[6] Double     Units
             * @param params[7] Double     Price
             * @param params[8] String     Application User
             */
            public int execInTransaction(Object params) throws BasicException {

                Object[] adjustParams = new Object[4];
                Object[] paramsArray = (Object[]) params;
                adjustParams[0] = paramsArray[4];                               //product ->Location
                adjustParams[1] = paramsArray[3];                               //location -> Product
                adjustParams[2] = paramsArray[5];                               //attributesetinstance
                adjustParams[3] = paramsArray[6];                               //units
                adjustStock(adjustParams);

                return new PreparedSentence(s
                        , "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, "
                        + "PRODUCT, ATTRIBUTESETINSTANCE_ID, "
                        + "UNITS, PRICE, AppUser) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        , new SerializerWriteBasicExt(stockdiaryDatas
                        , new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8}))
                        .exec(params);
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getStockDiaryInsert1() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {

                int updateresult = ((Object[]) params)[5] == null
                        ? new PreparedSentence(s
                        , "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                        + "AND ATTRIBUTESETINSTANCE_ID IS NULL"
                        , new SerializerWriteBasicExt(stockdiaryDatas, new int[] {6, 3, 4})).exec(params)
                        : new PreparedSentence(s
                        , "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                        + "AND ATTRIBUTESETINSTANCE_ID = ?"
                        , new SerializerWriteBasicExt(stockdiaryDatas, new int[] {6, 3, 4, 5})).exec(params);

                if (updateresult == 0) {
                    new PreparedSentence(s
                            , "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                            + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                            + "VALUES (?, ?, ?, ?)"
                            , new SerializerWriteBasicExt(stockdiaryDatas, new int[] {3, 4, 5, 6})).exec(params);
                }
                return new PreparedSentence(s
                        , "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, PRODUCT, "
                        + "ATTRIBUTESETINSTANCE_ID, UNITS, PRICE, AppUser, "
                        + "SUPPLIER, SUPPLIERDOC) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        , new SerializerWriteBasicExt(stockdiaryDatas,
                        new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10})).exec(params);

            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getStockDiaryDelete() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                int updateresult = ((Object[]) params)[5] == null // if ATTRIBUTESETINSTANCE_ID is null
                        ? new PreparedSentence(s
                        , "UPDATE stockcurrent SET UNITS = (UNITS - ?) "
                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                        + "AND ATTRIBUTESETINSTANCE_ID IS NULL"
                        , new SerializerWriteBasicExt(stockdiaryDatas, new int[] {6, 3, 4})).exec(params)
                        : new PreparedSentence(s
                        , "UPDATE stockcurrent SET UNITS = (UNITS - ?) "
                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                        + "AND ATTRIBUTESETINSTANCE_ID = ?"
                        , new SerializerWriteBasicExt(stockdiaryDatas, new int[] {6, 3, 4, 5})).exec(params);

                if (updateresult == 0) {
                    new PreparedSentence(s
                            , "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                            + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                            + "VALUES (?, ?, ?, -(?))"
                            , new SerializerWriteBasicExt(stockdiaryDatas, new int[] {3, 4, 5, 6})).exec(params);
                }
                return new PreparedSentence(s
                        , "DELETE FROM stockdiary WHERE ID = ?"
                        , new SerializerWriteBasicExt(stockdiaryDatas, new int[] {0})).exec(params);
            }
        };
    }


    private void adjustStock(Object params) throws BasicException {

        List<ProductsBundleInfo> bundle = getProductsBundle((String) ((Object[])params)[0]);

        if (bundle.size() > 0) {

            for (ProductsBundleInfo component : bundle) {
                Object[] adjustParams = new Object[4];
                adjustParams[0] = component.getProductBundleId();
                adjustParams[1] = ((Object[])params)[1];
                adjustParams[2] = ((Object[])params)[2];
                adjustParams[3] = ((Double)((Object[])params)[3]) * component.getQuantity();
                adjustStock(adjustParams);
            }
        } else {

            int updateresult = ((Object[]) params)[2] == null
                    ? new PreparedSentence(s
                    , "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                    + "WHERE LOCATION = ? AND PRODUCT = ? "
                    + "AND ATTRIBUTESETINSTANCE_ID IS NULL"
                    , new SerializerWriteBasicExt(stockAdjustDatas
                    , new int[] {3, 1, 0}))
                    .exec(params)
                    : new PreparedSentence(s
                    , "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                    + "WHERE LOCATION = ? AND PRODUCT = ? "
                    + "AND ATTRIBUTESETINSTANCE_ID = ?"
                    , new SerializerWriteBasicExt(stockAdjustDatas
                    , new int[] {3, 1, 0, 2}))
                    .exec(params);

            if (updateresult == 0) {

                new PreparedSentence(s
                        , "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                        + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                        + "VALUES (?, ?, ?, ?)"
                        , new SerializerWriteBasicExt(stockAdjustDatas
                        , new int[] {1, 0, 2, 3}))
                        .exec(params);
            }
        }
    }

    /**
     *
     * @return
     */
    public final SentenceExec getPaymentMovementInsert() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] arrayParam = (Object[]) params;
                new PreparedSentence(s
                        , "INSERT INTO receipts (ID, MONEY, DATENEW) "
                        + "VALUES (?, ?, ?)"
                        , new SerializerWriteBasicExt(paymenttabledatas,
                        new int[] {0, 1, 2})).exec(params);
                for(int i = 0; i < 6; i ++) {
                    if(Double.parseDouble(arrayParam[i+5].toString()) != 0) {
                        new PreparedSentence(s
                            , "INSERT INTO payments (ID, RECEIPT, PAYMENT, TOTAL, NOTES) "
                            + "VALUES (?, ?, ?, ?, ?)"
                            , new SerializerWriteBasic(new Datas[] {
                                Datas.STRING, Datas.STRING,
                                Datas.STRING, Datas.INT,
                                Datas.STRING })).exec(arrayParam[3], arrayParam[0], arrayParam[4], arrayParam[i+5], arrayParam[i+11]);
                    }
                }

                return 1;
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getPaymentMovementDelete() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                new PreparedSentence(s
                        , "DELETE FROM payments WHERE ID = ?"
                        , new SerializerWriteBasicExt(paymenttabledatas, new int[] {3})).exec(params);
                return new PreparedSentence(s
                        , "DELETE FROM receipts WHERE ID = ?"
                        , new SerializerWriteBasicExt(paymenttabledatas, new int[] {0})).exec(params);
            }
        };
    }

    /**
     *
     * @param warehouse
     * @param id
     * @param attsetinstid
     * @return
     * @throws BasicException
     */
    public final double findProductStock(String warehouse, String id, String attsetinstid) throws BasicException {

        PreparedSentence p = attsetinstid == null
                ? new PreparedSentence(s, "SELECT UNITS FROM stockcurrent "
                + "WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID IS NULL"
                , new SerializerWriteBasic(Datas.STRING, Datas.STRING)
                , SerializerReadDouble.INSTANCE)
                : new PreparedSentence(s, "SELECT UNITS FROM stockcurrent "
                + "WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID = ?"
                , new SerializerWriteBasic(Datas.STRING, Datas.STRING, Datas.STRING)
                , SerializerReadDouble.INSTANCE);

        Double d = (Double) p.find(warehouse, id, attsetinstid);
        return d == null ? 0.0 : d;
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCatalogCategoryAdd() {
        return new StaticSentence(s
                , "INSERT INTO products_cat(PRODUCT, CATORDER) SELECT ID, " + s.DB.INTEGER_NULL() + " FROM products WHERE CATEGORY = ?"
                , SerializerWriteString.INSTANCE);
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCatalogCategoryDel() {
        return new StaticSentence(s
                , "DELETE FROM products_cat WHERE PRODUCT = ANY (SELECT ID "
                + "FROM products WHERE CATEGORY = ?)"
                , SerializerWriteString.INSTANCE);
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableCategories() {
        return new TableDefinition(s,
                "categories"
                , new String[] {"ID", "NAME", "PARENTID", "IMAGE", "TEXTTIP", "CATSHOWNAME", "CATORDER", "SENDKITCHEN"}
                , new String[] {"ID", AppLocal.getIntString("label.name"), "", AppLocal.getIntString("label.image")}
                , new Datas[] {Datas.STRING, Datas.STRING, Datas.STRING,
                Datas.IMAGE, Datas.STRING, Datas.BOOLEAN, Datas.STRING, Datas.BOOLEAN}
                , new Formats[] {Formats.STRING, Formats.STRING, Formats.STRING,
                Formats.NULL, Formats.STRING, Formats.BOOLEAN, Formats.STRING, Formats.BOOLEAN}
                , new int[] {0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableTaxes() {
        return new TableDefinition(s,
                "taxes"
                , new String[] {"ID", "NAME", "CATEGORY", "CUSTCATEGORY", "PARENTID", "RATE", "RATECASCADE", "RATEORDER"}
                , new String[] {"ID", AppLocal.getIntString("label.name"), AppLocal.getIntString("label.taxcategory"), AppLocal.getIntString("label.custtaxcategory"), AppLocal.getIntString("label.taxparent"), AppLocal.getIntString("label.dutyrate"), AppLocal.getIntString("label.cascade"), AppLocal.getIntString("label.order")}
                , new Datas[] {Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.BOOLEAN, Datas.INT}
                , new Formats[] {Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.PERCENT, Formats.BOOLEAN, Formats.INT}
                , new int[] {0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableTaxCustCategories() {
        return new TableDefinition(s,
                "taxcustcategories"
                , new String[] {"ID", "NAME"}
                , new String[] {"ID", AppLocal.getIntString("label.name")}
                , new Datas[] {Datas.STRING, Datas.STRING}
                , new Formats[] {Formats.STRING, Formats.STRING}
                , new int[] {0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableTaxCategories() {
        return new TableDefinition(s,
                "taxcategories"
                , new String[] {"ID", "NAME"}
                , new String[] {"ID", AppLocal.getIntString("label.name")}
                , new Datas[] {Datas.STRING, Datas.STRING}
                , new Formats[] {Formats.STRING, Formats.STRING}
                , new int[] {0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableLocations() {
        return new TableDefinition(s,
                "locations"
                , new String[] {"ID", "NAME", "ADDRESS"}
                , new String[] {"ID", AppLocal.getIntString("label.locationname"),
                AppLocal.getIntString("label.locationaddress")}
                , new Datas[] {Datas.STRING, Datas.STRING, Datas.STRING}
                , new Formats[] {Formats.STRING, Formats.STRING, Formats.STRING}
                , new int[] {0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableStations() {
        return new TableDefinition(s,
                "stations"
                , new String[] {"ID", "NAME", "PRINTER", "IP"}
                , new String[] {"ID", AppLocal.getIntString("label.stationname"),
                AppLocal.getIntString("label.stationaddress")}
                , new Datas[] {Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING}
                , new Formats[] {Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING}
                , new int[] {0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableJobs() {
        return new TableDefinition(s,
                "jobs"
                , new String[] {"ID", "JOB_NAME", "ROLE_ID", "SAL"}
                , new String[] {"ID", AppLocal.getIntString("label.jobname")
                    , AppLocal.getIntString("label.role"), AppLocal.getIntString("label.role")}
                , new Datas[] {Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE}
                , new Formats[] {Formats.STRING, Formats.STRING, Formats.STRING, Formats.CURRENCY}
                , new int[] {0}
        );
    }
    /**
     *
     * @return
     */
    public final TableDefinition getTableDiscounts() {
        return new TableDefinition(s,
                "discounts"
                , new String[] {"ID", "NAME", "TYPE", "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN", "FROM_TIME", "TO_TIME", "RATE", "CATEGORY_ID", "AMOUNT"}
                , new String[] {"ID", "NAME", "TYPE", "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN", "FROM_TIME", "TO_TIME", "RATE", "CATEGORY_ID", "AMOUNT"}
                , new Datas[] {Datas.STRING, Datas.STRING, Datas.INT, Datas.BOOLEAN, Datas.BOOLEAN, Datas.BOOLEAN, Datas.BOOLEAN, Datas.BOOLEAN, Datas.BOOLEAN, Datas.BOOLEAN, Datas.STRING, Datas.STRING, Datas.INT, Datas.STRING, Datas.INT}
                , new Formats[] {Formats.STRING, Formats.STRING, Formats.INT, Formats.BOOLEAN, Formats.BOOLEAN, Formats.BOOLEAN, Formats.BOOLEAN, Formats.BOOLEAN, Formats.BOOLEAN, Formats.BOOLEAN, Formats.STRING, Formats.STRING, Formats.INT, Formats.STRING, Formats.INT}
                , new int[] {0}
        );
    }

    /**
     *
     */
    protected static class CustomerExtRead implements SerializerRead {

        /**
         *
         * @param dr
         * @return
         * @throws BasicException
         */
        @Override
        public Object readValues(DataRead dr) throws BasicException {
            CustomerInfoExt c = new CustomerInfoExt(dr.getString(1));
            c.setSearchkey(dr.getString(2));
            c.setTaxid(dr.getString(3));
            c.setTaxCustomerID(dr.getString(3));
            c.setName(dr.getString(4));
            c.setTaxCustCategoryID(dr.getString(5));
            c.setCard(dr.getString(6));
            c.setMaxdebt(dr.getDouble(7));
            c.setAddress(dr.getString(8));
            c.setAddress2(dr.getString(9));
            c.setPcode(dr.getString(10));
            c.setCity(dr.getString(11));
            c.setRegion(dr.getString(12));
            c.setCountry(dr.getString(13));
            c.setFirstname(dr.getString(14));
            c.setLastname(dr.getString(15));
            c.setCemail(dr.getString(16));
            c.setPhone1(dr.getString(17));
            c.setPhone2(dr.getString(18));
            c.setFax(dr.getString(19));
            c.setNotes(dr.getString(20));
            c.setVisible(dr.getBoolean(21));
            c.setCurdate(dr.getTimestamp(22));
            c.setAccdebt(dr.getDouble(23));
            c.setImage(ImageUtils.readImage(dr.getString(24)));
            c.setisVIP(dr.getBoolean(25));
            c.setDiscount(dr.getDouble(26));
            c.setMemoDate(dr.getString(27));

            return c;
        }
    }

    public final UomInfo getUomInfoById(String id) throws BasicException {
        return (UomInfo) new PreparedSentence(s,
                "SELECT " +
                        "id, name " +
                        "FROM uom " +
                        "WHERE id = ?"
                , SerializerWriteString.INSTANCE, UomInfo.getSerializerRead()).find(id);
    }

    public final TableDefinition getTableUom() {
        return new TableDefinition(s,
                "uom",
                new String[]{"id", "name"},
                new String[]{"id",
                        AppLocal.getIntString("Label.Name")},
                new Datas[]{
                        Datas.STRING, Datas.STRING},
                new Formats[]{
                        Formats.STRING, Formats.STRING},
                new int[]{0}
        );
    }

    public final SentenceList getUomList() {
        return new StaticSentence(s, "SELECT ID, NAME  FROM uom ORDER BY NAME", null, UomInfo.getSerializerRead());
    }       
    
/*    public final SentenceList getVoucherList() {
        return new StaticSentence(s,
            "SELECT " +
                "vouchers.ID,vouchers.VOUCHER_NUMBER,vouchers.CUSTOMER, " +
                "customers.NAME,AMOUNT, STATUS " +
              "FROM vouchers   " +
                "JOIN customers ON customers.id = vouchers.CUSTOMER  " +
              "WHERE STATUS='A' " +
              "ORDER BY vouchers.VOUCHER_NUMBER ASC"
            , null, VoucherInfo.getSerializerRead());      
    }
*/

    public final SentenceList getVoucherList() {
        return new StaticSentence(s,
                "SELECT " +
                        "vouchers.ID,vouchers.VOUCHER_NUMBER,vouchers.CUSTOMER, " +
                        "vouchers.AMOUNT, vouchers.STATUS " +
                        "FROM vouchers   " +
                        "WHERE STATUS='A' " +
                        "ORDER BY vouchers.VOUCHER_NUMBER ASC"
                , null, VoucherInfo.getSerializerRead());
    }

    public final SentenceExec getVoucherNonActive() {
        return new PreparedSentence(s,
                "UPDATE vouchers SET STATUS = 'D' " +
                        "WHERE VOUCHER_NUMBER = ?"
                , SerializerWriteString.INSTANCE);
    }
    public final SentenceExec setVoucherPending() {
        return new PreparedSentence(s,
                "UPDATE vouchers SET STATUS = 'P' " +
                        "WHERE VOUCHER_NUMBER = ?"
                , SerializerWriteString.INSTANCE);
    }

    public final SentenceExec resetPickupId() {

        return new PreparedSentence(s,
                "UPDATE pickup_number SET ID=1 "
                , SerializerWriteString.INSTANCE);

    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public SupplierInfoExt loadSupplierExt(String id) throws BasicException {
        return (SupplierInfoExt) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "MAXDEBT, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "NOTES, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "VATID "
                + "FROM suppliers WHERE ID = ?"
                , SerializerWriteString.INSTANCE
                , new SupplierExtRead()).find(id);
    }

    /**
     *
     */
    protected static class SupplierExtRead implements SerializerRead {

        /**
         *
         * @param dr
         * @return
         * @throws BasicException
         */
        @Override
        public Object readValues(DataRead dr) throws BasicException {
            SupplierInfoExt s = new SupplierInfoExt(dr.getString(1));
            s.setSearchkey(dr.getString(2));
            s.setTaxid(dr.getString(3));
            s.setName(dr.getString(4));
            s.setMaxdebt(dr.getDouble(5));
            s.setAddress(dr.getString(6));
            s.setAddress2(dr.getString(7));
            s.setPostal(dr.getString(8));
            s.setCity(dr.getString(9));
            s.setRegion(dr.getString(10));
            s.setCountry(dr.getString(11));
            s.setFirstname(dr.getString(12));
            s.setLastname(dr.getString(13));
            s.setEmail(dr.getString(14));
            s.setPhone(dr.getString(15));
            s.setPhone2(dr.getString(16));
            s.setFax(dr.getString(17));
            s.setNotes(dr.getString(18));
            s.setVisible(dr.getBoolean(19));
            s.setCurdate(dr.getTimestamp(20));
            s.setCurdebt(dr.getDouble(21));
            s.setSupplierVATID(dr.getString(22));

            return s;
        }
    }

    /**
     *
     * @return
     */

    public final SentenceExec getCustomerInsert() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] values = (Object[]) params;
                int i = new PreparedSentence(s
                        , "INSERT INTO customers ("
                        + "ID, "
                        + "SEARCHKEY, "
                        + "TAXID, "
                        + "NAME, "
                        + "TAXCATEGORY, "
                        + "CARD, "
                        + "MAXDEBT, "
                        + "ADDRESS, "
                        + "ADDRESS2, "
                        + "POSTAL, "
                        + "CITY, "
                        + "REGION, "
                        + "COUNTRY, "
                        + "FIRSTNAME, "
                        + "LASTNAME, "
                        + "EMAIL, "
                        + "PHONE, "
                        + "PHONE2, "
                        + "FAX, "
                        + "NOTES, "
                        + "VISIBLE, "
                        + "CURDATE, "
                        + "CURDEBT, "
                        + "IMAGE, "
                        + "ISVIP, "
                        + "DISCOUNT, "
                        + "MEMODATE ) "
                        + "VALUES ("
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?)"
                        , new SerializerWriteBasicExt(customersRow.getDatas(),
                        new int[]{0,
                                1, 2, 3, 4, 5, 6,
                                7, 8, 9, 10, 11, 12,
                                13, 14, 15, 16, 17, 18,
                                19, 20, 21, 22, 23, 24,
                                25, 26}))

                        .exec(params);
                return i;
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCustomerUpdate() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] values = (Object[]) params;
                int i = new PreparedSentence(s
                        , "UPDATE customers SET "
                        + "ID = ?, "
                        + "SEARCHKEY = ?, "
                        + "TAXID = ?, "
                        + "NAME = ?, "
                        + "TAXCATEGORY = ?, "
                        + "CARD = ?, "
                        + "MAXDEBT = ?, "
                        + "ADDRESS = ?, "
                        + "ADDRESS2 = ?, "
                        + "POSTAL = ?, "
                        + "CITY = ?, "
                        + "REGION = ?, "
                        + "COUNTRY = ?, "
                        + "FIRSTNAME = ?, "
                        + "LASTNAME = ?, "
                        + "EMAIL = ?, "
                        + "PHONE = ?, "
                        + "PHONE2 = ?, "
                        + "FAX = ?,  "
                        + "NOTES = ?,"
                        + "VISIBLE = ?, "
                        + "CURDATE = ?, "
                        + "CURDEBT = ?, "
                        + "IMAGE = ?, "
                        + "ISVIP = ?, "
                        + "DISCOUNT = ?, "
                        + "MEMODATE = ? "
                        + "WHERE ID = ?"
                        , new SerializerWriteBasicExt(customersRow.getDatas(),
                        new int[]{0,
                                1, 2, 3, 4, 5,
                                6, 7, 8, 9, 10,
                                11, 12, 13, 14, 15,
                                16, 17, 18, 19, 20,
                                21, 22, 23, 24, 25,
                                26, 0}))
                        .exec(params);

/*                
 * Use this block workflow as template to pump LOYALTY, MEMBERSHIP & etc
 * updates to internal or external DB table               
                if (i > 0) {
                    if (((Boolean)values[n0])) {
			if (new PreparedSentence(s
                                , "UPDATE tablename SET FIELD = ? WHERE CUSTOMER = ?"
                                , new SerializerWriteBasicExt(customersRow.getDatas()
                                , new int[] {n1, 0})).exec(params) == 0) {
                            new PreparedSentence(s
				, "INSERT INTO other_tablename (CUSTOMER, FIELD) VALUES (?, ?)"
                                , new SerializerWriteBasicExt(productsRow.getDatas(), new int[] {0, n1})).exec(params);
                            }
			} else {
                            new PreparedSentence(s
				, "DELETE FROM FIELD WHERE CUSTOMER = ?"
				, new SerializerWriteBasicExt(customersRow.getDatas(), new int[] {0})).exec(params);
			}
		}
*/
                return i;
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCustomerDelete() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                return new PreparedSentence(s
                        , "DELETE FROM customers WHERE ID = ?"
                        , new SerializerWriteBasicExt(customersRow.getDatas(),
                        new int[] {0}
                )).exec(params);
            }
        };
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    public final List<AttributeInfo> getProductModifiers(String attributeset_id) throws BasicException {
        return new PreparedSentence(s
                , "SELECT "
                + "att.ID, "
                + "att.NAME "
                + "FROM attributeuse attu "
                + "INNER JOIN attribute att ON attu.ATTRIBUTE_ID = att.ID "
                + "WHERE att.MODIFIER = 1 AND attu.ATTRIBUTESET_ID = ? "
                + "ORDER BY attu.LINENO"
                , SerializerWriteString.INSTANCE
                , AttributeInfo.getSerializerRead()).list(attributeset_id);
    }
    /**
     *
     * @return
     * @throws BasicException
     */
    public final List<AttributeValue> getAttributeValues(String attribute_id) throws BasicException {
        return new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "ATTRIBUTE_ID, "
                + "VALUE, "
                + "PRICE "
                + "FROM attributevalue "
                + "WHERE ATTRIBUTE_ID = ? "
                + "ORDER BY VALUE"
                , SerializerWriteString.INSTANCE
                , AttributeValue.getSerializerRead()).list(attribute_id);
    }
    public AppView getApp(){
        return m_App;
    }
    //Nam Ho added
/**
     *
     * @return
     * @throws BasicException
     */
    public List<DiscountInfo> getDiscounts() throws BasicException {
        return new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "NAME, "
                + "TYPE, "
                + "MON, "
                + "TUE, "
                + "WED, "
                + "THU, "
                + "FRI, "
                + "SAT, "
                + "SUN, "
                + "FROM_TIME, "
                + "TO_TIME, "
                + "RATE, "
                + "CATEGORY_ID, "
                + "AMOUNT "
                + "FROM discounts ",

                null,
                DiscountInfo.getSerializerRead()).list();


    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
