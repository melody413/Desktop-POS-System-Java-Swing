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

package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializerRead;

/**
 *
 * @author  adrianromero
 */
public class AttributeValue implements IKeyed {

    private String id;
    private String attribute_id;
    private String value;
    private double price;

    /** Creates new CategoryInfo
     * @param id
     * @param attribute_id 
     * @param value */
    public AttributeValue(String id, String attribute_id, String value, double price) {
        this.id = id;
        this.attribute_id = attribute_id;
        this.value = value;
        this.price = price;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getKey() {
        return id;
    }
    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }
    public String getValue() {
        return value;
    }
    
    public double getPrice() {
        return price;
    }
    public static SerializerRead getSerializerRead() {
        return new SerializerRead() 
        {
            @Override
        public Object readValues(DataRead dr) throws BasicException {
            return new AttributeValue(
                    dr.getString(1), 
                    dr.getString(2),
                    dr.getString(3),
                    dr.getDouble(4));            
        }};
    }
}
