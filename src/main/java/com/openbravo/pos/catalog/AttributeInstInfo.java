/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.openbravo.pos.catalog;

/**
 *
 * @author This PC
 */
public class AttributeInstInfo {
    
    private String attid;
    private String value;
    private double price;
    public AttributeInstInfo(String attid, String value, double price) {
        this.attid = attid;
        this.value = value;
        this.price = price;
    }
    /**
     * @return the attid
     */
    public String getAttid() {
      return attid;
    }
    /**
     * @return the value
     */
    public String getValue() {
      return value;
    }
    /**
     * @return the price
     */
    public double getPrice() {
      return price;
    }
}
