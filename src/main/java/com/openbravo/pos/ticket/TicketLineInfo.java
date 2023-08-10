//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright © 2009-2020 uniCenta
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializableWrite;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.inventory.AttributeValue;
import com.openbravo.pos.inventory.Modifier;
import com.openbravo.pos.util.StringUtils;
import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author adrianromero
 */
public class TicketLineInfo implements SerializableWrite, SerializableRead, Serializable {

    private static final long serialVersionUID = 6608012948284450199L;
    private String m_sTicket;
    private int m_iLine;
    private double multiply;
    private double price;
    private TaxInfo tax;
    private Properties attributes;
    private String productid;
    private String attsetinstid;
    private Boolean updated = false;
    private int seat;    
    private double newprice = 0.0;
    private double extraprice = 0.00000;
    private List<Modifier> modifiers;
    private String ordertime;
    private UserInfo m_User;
    private Boolean is_void = false;
    private Boolean is_comp = false;
    private int discountrate = 0;
    private String note = "";
    private String reason = "";
    private String ordereID = "";
    private int newMultiply = 0;

    /** Creates new TicketLineInfo
     * @param productid
     * @param dMultiply
     * @param dPrice
     * @param tax
     * @param props */
    public TicketLineInfo(String productid, double dMultiply, double dPrice, TaxInfo tax, Properties props) {
        init(productid, null, dMultiply, dPrice, tax, props, 0);
    }

    /**
     *
     * @param productid
     * @param dMultiply
     * @param dPrice
     * @param tax
     */
    public TicketLineInfo(String productid, double dMultiply, double dPrice, TaxInfo tax) {
        init(productid, null, dMultiply, dPrice, tax, new Properties(), 0);
    }

    /**
     * Example: Call from script.TotalDiscount event
     * @param productid
     * @param productname
     * @param producttaxcategory
     * @param productprinter
     * @param dMultiply
     * @param dPrice
     * @param tax
     */
    public TicketLineInfo(String productid, String productname, String producttaxcategory, String productprinter, double dMultiply, double dPrice, TaxInfo tax) {
        Properties props = new Properties();
        props.setProperty("product.name", productname);
        props.setProperty("product.taxcategoryid", producttaxcategory);
        props.setProperty("product.printer", productprinter);   //added to props as may introduce printer redirect
        
        init(productid, null, dMultiply, dPrice, tax, props, 0);
    }

    /**
     * Example: Call from script.LineDiscount event
     * @param productname
     * @param producttaxcategory
     * @param productprinter    //added to props as may introduce printer redirect
     * @param dMultiply
     * @param dPrice
     * @param tax
     */
    public TicketLineInfo(String productname, String producttaxcategory, String productprinter, double dMultiply, double dPrice, TaxInfo tax) {

        Properties props = new Properties();
        props.setProperty("product.name", productname);
        props.setProperty("product.taxcategoryid", producttaxcategory);
        props.setProperty("product.printer", productprinter);
        
        init(null, null, dMultiply, dPrice, tax, props,0);
    }

    public TicketLineInfo(int seat) {
        init(null, null, 0.0, 0.0, null, new Properties(), seat);
    }
    /**
     *
     */
    public TicketLineInfo() {
        init(null, null, 0.0, 0.0, null, new Properties(), 0);
    }

    /**
     *
     * @param product
     * @param dMultiply
     * @param dPrice
     * @param tax
     * @param attributes
     */
    public TicketLineInfo(ProductInfoExt product, double dMultiply, double dPrice, TaxInfo tax, Properties attributes) {

        String pid;

        if (product == null) {
            pid = null;
            tax = null;
        } else {
            pid = product.getID();
            attributes.setProperty("product.name", product.getName());
            attributes.setProperty("product.reference", product.getReference());
            attributes.setProperty("product.code", product.getCode());

            if (product.getMemoDate() == null) {
                attributes.setProperty("product.memodate", "1900-01-01 00:00:01");                
            } else {
                attributes.setProperty("product.memodate", product.getMemoDate());                
            }
         
            attributes.setProperty("product.com", product.isCom() ? "true" : "false");
            attributes.setProperty("product.constant", product.isConstant() ? "true" : "false");

            if (product.getStationId() != null)
                attributes.setProperty("product.stationid", product.getStationId());
            
            if (product.getPrinter() != null) {
                attributes.setProperty("product.printer", product.getPrinter());
            }    
            attributes.setProperty("product.service", product.isService() ? "true" : "false");
            attributes.setProperty("product.vprice", product.isVprice() ? "true" : "false");
            attributes.setProperty("product.verpatrib", product.isVerpatrib() ? "true" : "false");

            if (product.getTextTip() != null) {
                attributes.setProperty("product.texttip", product.getTextTip());
            }
 
            attributes.setProperty("product.warranty", product.getWarranty()? "true" : "false");        
       
            if (product.getAttributeSetID() != null) {
                attributes.setProperty("product.attsetid", product.getAttributeSetID());
            }
            
            attributes.setProperty("product.taxcategoryid", product.getTaxCategoryID());
        
            if (product.getCategoryID() != null) {
                attributes.setProperty("product.categoryid", product.getCategoryID());
            }

            if ("true".equals(attributes.getProperty("ticket.updated"))) {
                attributes.setProperty("ticket.updated", "false");                
            } else {
                attributes.setProperty("ticket.updated", "true");                
            }
        }

        init(pid, null, dMultiply, dPrice, tax, attributes,0);
    }
/**
     *
     * @param product
     * @param dMultiply
     * @param dPrice
     * @param tax
     * @param attributes
     * @param seat
     */
    public TicketLineInfo(ProductInfoExt product, double dMultiply, double dPrice, TaxInfo tax, Properties attributes, int seat) {

        String pid;

        if (product == null) {
            pid = null;
            tax = null;
        } else {
            pid = product.getID();
            attributes.setProperty("product.name", product.getName());
            attributes.setProperty("product.reference", product.getReference());
            attributes.setProperty("product.code", product.getCode());


            if (product.getMemoDate() == null) {
                attributes.setProperty("product.memodate", "1900-01-01 00:00:01");                
            } else {
                attributes.setProperty("product.memodate", product.getMemoDate());                
            }
         
            attributes.setProperty("product.com", product.isCom() ? "true" : "false");
            attributes.setProperty("product.constant", product.isConstant() ? "true" : "false");

            if (product.getStationId() != null)
                attributes.setProperty("product.stationid", product.getStationId());

            if (product.getPrinter() != null) {
                attributes.setProperty("product.printer", product.getPrinter());
            }   
            
            attributes.setProperty("product.service", product.isService() ? "true" : "false");
            attributes.setProperty("product.vprice", product.isVprice() ? "true" : "false");
            attributes.setProperty("product.verpatrib", product.isVerpatrib() ? "true" : "false");

            if (product.getTextTip() != null) {
                attributes.setProperty("product.texttip", product.getTextTip());
            }
 
            attributes.setProperty("product.warranty", product.getWarranty()? "true" : "false");        
       
            if (product.getAttributeSetID() != null) {
                attributes.setProperty("product.attsetid", product.getAttributeSetID());
            }
            
            attributes.setProperty("product.taxcategoryid", product.getTaxCategoryID());
        
            if (product.getCategoryID() != null) {
                attributes.setProperty("product.categoryid", product.getCategoryID());
            }

            if ("true".equals(attributes.getProperty("ticket.updated"))) {
                attributes.setProperty("ticket.updated", "false");                
            } else {
                attributes.setProperty("ticket.updated", "true");                
            }
        }
        init(pid, null, dMultiply, dPrice, tax, attributes, seat);
        setProductAttSetInstId(product.getAttSetInstId());
        setProductAttSetInstDesc(product.getAttSetInstDesc());
        setModifiers(product.getModifiers()); 
    }
    /**
     *
     * @param oProduct
     * @param dPrice
     * @param tax
     * @param attributes
     */
    public TicketLineInfo(ProductInfoExt oProduct, double dPrice, TaxInfo tax, Properties attributes) {
        this(oProduct, 1.0, dPrice, tax, attributes);
    }

    /**
     *
     * @param line
     */
    public TicketLineInfo(TicketLineInfo line) {
        init(line.productid, line.attsetinstid, line.multiply, line.price, 
            line.tax, (Properties) line.attributes.clone(),0);
    }

    private void init(String productid, String attsetinstid, double dMultiply, double dPrice, TaxInfo tax, Properties attributes, int seat) {

        this.productid = productid;
        this.attsetinstid = attsetinstid;
        multiply = dMultiply;
        price = dPrice;
        this.tax = tax;  
        this.attributes = attributes;
        this.seat = seat;
        m_sTicket = null;
        m_iLine = -1;
        this.ordertime = String.valueOf(System.currentTimeMillis());
        m_User = null;
    }

    void setTicket(String ticket, int line) {
        m_sTicket = ticket;
        m_iLine = line;
    }

    /**
     *
     * @param dp
     * @throws BasicException
     */
    @Override
    public void writeValues(DataWrite dp) throws BasicException {
        dp.setString(1, m_sTicket);
        dp.setInt(2, m_iLine);
        dp.setString(3, productid);
        dp.setString(4, attsetinstid);
        dp.setDouble(5, multiply);
        dp.setDouble(6, price);
        dp.setString(7, tax.getId());

        if(is_comp) dp.setInt(8, 1);
        else dp.setInt(8, 0);

        dp.setInt(9, discountrate);

        try {
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            attributes.storeToXML(o, AppLocal.APP_NAME, "UTF-8");
            dp.setBytes(10, o.toByteArray());
        } catch (IOException e) {
            dp.setBytes(10, null);
        }

        dp.setString(11, reason);
    }

    /**
     *
     * @param dr
     * @throws BasicException
     */
    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sTicket = dr.getString(1);
        m_iLine = dr.getInt(2);
        productid = dr.getString(3);
        attsetinstid = dr.getString(4);
        multiply = dr.getDouble(5);
        price = dr.getDouble(6);
        tax = new TaxInfo(
            dr.getString(7), 
            dr.getString(8), 
            dr.getString(9), 
            dr.getString(10), 
            dr.getString(11), 
            dr.getDouble(12), 
            dr.getBoolean(13), 
            dr.getInt(14));
        attributes = new Properties();

        try {
            byte[] img = dr.getBytes(15);
            if (img != null) {
                attributes.loadFromXML(new ByteArrayInputStream(img));
            }
        } catch (IOException e) {
        }
        m_User = new UserInfo(dr.getString(16), dr.getString(17));
    }

    /**
     *
     * @return
     */
    public TicketLineInfo copyTicketLine() {
        TicketLineInfo l = new TicketLineInfo();
        l.productid = productid;
        l.attsetinstid = attsetinstid;
        l.multiply = multiply;
        l.price = price;
        l.tax = tax; 
        l.attributes = (Properties) attributes.clone();
        l.modifiers = modifiers;
        l.seat = seat;
        return l;
    }

    /**
     *
     * @return
     */
    public int getTicketLine() {
        return m_iLine;
    }
// These are the Lookups   
    public String getProductID() {
        return productid;
    }
    public String getProductCategoryID() {
        return (attributes.getProperty("product.categoryid"));
    }
    public String getProductAttSetId() {
        return attributes.getProperty("product.attsetid");
    }
    public String getProductAttSetInstId() {
        return attsetinstid;
    }    
    public String getProductAttSetInstDesc() {
        return attributes.getProperty("product.attsetdesc", "");
    }
    public String getProductTaxCategoryID() {
        return (attributes.getProperty("product.taxcategoryid"));
    }
    public String getTicketUpdated() {
        return (attributes.getProperty("ticket.updated"));
    }
    public String getStationID() {
        return (attributes.getProperty("product.stationid"));
    }
    public TaxInfo getTaxInfo() {
        return tax;
    }    
    public void setTaxInfo(TaxInfo oTaxInfo) {
        tax = oTaxInfo;
    }     
        
// These appear on Printed TicketLine
    public String getProductName() {
        return attributes.getProperty("product.name");
    }
    public String getProductMemoDate() {
        return attributes.getProperty("product.memodate");
    }
    public double getPrice() {
        return price;
    }
    public double getMultiply() {
        return multiply;
    }
    public double getTaxRate() {
        return tax == null ? 0.0 : tax.getRate();
    }
    public double getNewPrice() {
        newprice = price * (1.0 + getTaxRate());
        return price;
    }
    public String getProductPrinter() {
        return attributes.getProperty("product.printer");
    }    

// These are the Summaries    
    public double getPriceTax() {
        return price * (1.0 + getTaxRate());
    }

    public Properties getProperties() {
        return attributes;
    }
    public String getProperty(String key) {
        return attributes.getProperty(key);
    }
    public String getProperty(String key, String defaultvalue) {
        return attributes.getProperty(key, defaultvalue);
    }

// These are Ticket Totals    
    public double getTax() {
        return price * multiply * getTaxRate();
    }
    public double getValue() {
        return price * multiply * (1.0 + getTaxRate());
    }
    public double getSubValue() {
        return price * multiply;
    }
// These are Ticket Totals    
    public double getTaxE() {
        double subValue = getSubValueE();
        double rate =  discountrate / 100.0;
        return (subValue - (double)Math.abs(subValue * rate * 100) /100) * getTaxRate();
    }
    public double getValueE() {
        double subValue = getSubValueE();
        double rate =  discountrate / 100.0;
        return (subValue - (double)Math.abs(subValue * rate * 100) /100) * (1.0 + getTaxRate());
    }
    public double getSubValueE() {
        return (price + extraprice) * multiply;
    }
    public double getTotal(){
        double extraP = 0;
        if(modifiers!=null){
            for(Modifier modifier:modifiers){
                extraP = extraP + modifier.getPrice();
            }
        }
        return getValue()+extraP;
    }
// SETTERS
    public void setPrice(double dValue) {
        price = dValue;
    }
    public void setPriceTax(double dValue) {
        price = dValue / (1.0 + getTaxRate());               
    }
    public void setMultiply(double dValue) {
        multiply = dValue;
    }
    public void setProperty(String key, String value) {
        attributes.setProperty(key, value);
    }    
    public void setProductTaxCategoryID(String taxID){
        attributes.setProperty("product.taxcategoryid",taxID);
    }
    public void setProductAttSetInstId(String value) {
        attsetinstid = value;
    }
    public void setProductAttSetInstDesc(String value) {
        if (value == null) {
            attributes.remove("product.attsetdesc");
        } else {
            attributes.setProperty("product.attsetdesc", value);
        }
    }
    public void setTicketUpdated(String key, String value){
        attributes.setProperty("ticket.updated",value);
    }
    public void setProductPrinter(String value) {
        if (value == null) {
            attributes.remove(value);
        } else {
            attributes.setProperty("product.printer", value);
        }
    }    
    public void setModifiers(List<Modifier> dModifers) {
        modifiers = dModifers;
        //Caculate extra price
        double extraP = 0.0;
        if(modifiers!=null){
            for(Modifier modifier:modifiers){
                extraP = extraP + modifier.getPrice();
            }
        }
        extraprice = extraP/(1+tax.getRate());
    }
    public List<Modifier> getModifiers() {
        return modifiers;
    }
    public void setExtraprice(double dExtraprice){
        extraprice = dExtraprice;
    }
    public double getExtraprice(){
        return extraprice;
    }
    /**
     *
     * @return
     */
    // Print to actual ${ticketline
    // Nam Ho Add
    public String printModifiers() {
        String modStr = "";
        if(modifiers!=null){
            for(Modifier modifier:modifiers){
                modStr = modStr + "<br>&nbsp;&nbsp;&nbsp;<i>" + modifier.getValue();
                if(modifier.getPrice()>0){
                    modStr = modStr + "($" + modifier.getPrice() + ")";
                }
                modStr = modStr + "</i>";
            }
        }
        if(!note.isEmpty()){
            modStr = modStr + "<br>(" + note +")";
        }
        return modStr;
    }
    // Nam Ho Add
    public String printReference() {
        return StringUtils.encodeXML(attributes.getProperty("product.reference"));
    }
    public String printCode() {
        return StringUtils.encodeXML(attributes.getProperty("product.code"));
    }
    public String printName() {
        if (productid==null)
            return StringUtils.encodeXML("Seat #" + seat);
        else{
            if(discountrate>0){
                double rate = discountrate / 100.0;
                String sdiscount = Formats.PERCENT.formatValue((rate));
                return StringUtils.encodeXML(attributes.getProperty("product.name") + " OFF " + sdiscount);
            }
            return StringUtils.encodeXML(attributes.getProperty("product.name"));
        }
    }
    public String printProductMemoDate() {
        return StringUtils.encodeXML(attributes.getProperty("product.memodate"));
    }         
    public String printPrice() {
        return Formats.CURRENCY.formatValue(getPrice());
    }
    public String printPriceTax() {
        return Formats.CURRENCY.formatValue(getPriceTax());
    }
    public String printMultiply() {
        return Formats.DOUBLE.formatValue(multiply);
    }
    public String printValue() {
        return Formats.CURRENCY.formatValue(getValue());
    }
    public String printValueEx() {
        double extraP = 0;
        if(modifiers!=null){
            for(Modifier modifier:modifiers){
                extraP = extraP + modifier.getPrice();
            }
        }
        double subValue = getValue() + extraP * multiply;
        double rate =  discountrate / 100.0;
        subValue = (subValue - (double)Math.abs(subValue * rate * 100) /100);
        return Formats.CURRENCY.formatValue(subValue);
    }
    public String printPriceTaxEx() {
        double extraP = 0;
        if(modifiers!=null){
            for(Modifier modifier:modifiers){
                extraP = extraP + modifier.getPrice();
            }
        }

        double subValue = getPriceTax() + extraP;
        return Formats.CURRENCY.formatValue(subValue);
    }

    public String printTaxRate() {
        return Formats.PERCENT.formatValue(getTaxRate());
    }
    public String printSubValue() {
        return Formats.CURRENCY.formatValue(getSubValue());
    }
    public String printTax() {
        return Formats.CURRENCY.formatValue(getTax());
    }
    public String printTextTip() {
	return attributes.getProperty("product.texttip");
    }
    public String printPrinter() {
        return StringUtils.encodeXML(attributes.getProperty("product.printer"));
    }      
    public boolean isProductCom() {
       return "true".equals(attributes.getProperty("product.com"));
    }
    public boolean isProductService() {
	return "true".equals(attributes.getProperty("product.service"));
    }
    public boolean isProductVprice() {
	return "true".equals(attributes.getProperty("product.vprice"));
    }
    public boolean isProductVerpatrib() {
	return "true".equals(attributes.getProperty("product.verpatrib"));
    }
    public boolean isProductWarranty() {
	return "true".equals(attributes.getProperty("product.warranty"));
    }    
    public boolean getUpdated() {
        return "true".equals(attributes.getProperty("ticket.updated"));
    }
    public void setUpdated(Boolean value) {
        updated = value;
    }
    public void setSeat(int vSeat){
        seat = vSeat;
    }
    public int getSeat(){
        return seat;
    }
    public boolean isSeatLine(){
        return (productid==null)? true:false;
    }
    public void setOrdertime(String vOrdertime){
        ordertime = vOrdertime;
    }
    public String getOrdertime(){
        return ordertime;
    }
    public String printElapsedTime(){
        return String.valueOf((System.currentTimeMillis() - Long.valueOf(ordertime))/60000) + "'";
    }

    public UserInfo getUser() {
        return m_User;
    }

    public void setUser(UserInfo value) {
        m_User = value;
    }

    public boolean getIsVoid() {
        return is_void;
    }
    public void setIsVoid(Boolean value) {
        is_void = value;

    }

    public boolean getIsComp() {
        return is_comp;
    }
    public void setIsComp(Boolean value) {
        is_comp = value;

    }
    
    public int getDiscountRate() {
        return discountrate;
    }
    public void setDiscountRate(int value) {
        discountrate = value;

    }

    public String getNote() {
        return note;
    }

    public void setNote(String value) {
        note = value;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String m_reason) {
        reason = m_reason;
    }

    public String getIsOrdered() {
        return ordereID;
    }

    public void setIsOrdered(String m_ordered) {
        ordereID = m_ordered;
    }

    public int getNewMultiply() {
        return newMultiply;
    }

    public void setNewMultiply(int multiply) {
        newMultiply = multiply;
    }
}
