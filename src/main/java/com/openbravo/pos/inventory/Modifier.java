/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.openbravo.pos.inventory;

import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializableWrite;
import java.io.Serializable;

/**
 *
 * @author This PC
 */
public class Modifier implements Serializable {
    private static final long serialVersionUID = 1108012948284450199L;
    private String id;
    private String value;
    private double price;
    
    public void setId(String dId){
        id = dId;
    }
    public String getId(){
        return id;
    }
    public void setValue(String dValue){
        value = dValue;
    }
    public String getValue(){
        return value;
    }
    public void setPrice(double dPrice){
        price = dPrice;
    }
    public double getPrice(){
        return price;
    }
}
