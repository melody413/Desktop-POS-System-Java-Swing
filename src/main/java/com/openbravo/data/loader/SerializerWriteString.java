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

package com.openbravo.data.loader;

import com.openbravo.basic.BasicException;

import java.lang.reflect.Array;

/**
 *
 * @author JG uniCenta
 */
public class SerializerWriteString implements SerializerWrite<Object> {
    
    /**
     *
     */
    public static final SerializerWrite INSTANCE = new SerializerWriteString();
    
    /** Creates a new instance of SerializerWriteString */
    private SerializerWriteString() {
    }
    
    /**
     *
     * @param dp
     * @param obj
     * @throws BasicException
     */
    public void writeValues(DataWrite dp, Object obj) throws BasicException {
        if (obj instanceof Object[]){
            for (int i = 0 ; i < ((Object[]) obj).length ; i++) {
                Datas.STRING.setValue(dp, i+1, ((Object[]) obj)[i]);
            }
        }
        else {
            Datas.STRING.setValue(dp, 1, obj);
        }

    }
}
