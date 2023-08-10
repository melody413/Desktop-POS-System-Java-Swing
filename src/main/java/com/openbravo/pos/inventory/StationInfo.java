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
import com.openbravo.data.loader.SerializableRead;

/**
 *
 * @author adrianromero
 * Created on February 13, 2007, 10:13 AM
 *
 */
public class StationInfo implements SerializableRead, IKeyed {
    
    private static final long serialVersionUID = 9032683595230L;
    private String m_sID;
    private String m_sName;
    private String m_sPrinter;
    private String m_sIp;

    /** Creates a new instance of StationInfo */
    public StationInfo() {
        m_sID = null;
        m_sName = null;
        m_sPrinter = null;
        m_sIp = null;
    }
    
    public StationInfo(String sID, String sName) {
        m_sID = sID;
        m_sName = sName;      
    }
    /**
     *
     * @return
     */
    public Object getKey() {
        return m_sID;
    }

    /**
     *
     * @param dr
     * @throws BasicException
     */
    public void readValues(DataRead dr) throws BasicException {
        m_sID = dr.getString(1);
        m_sName = dr.getString(2);
        m_sPrinter = dr.getString(3);
    }

    /**
     *
     * @param sID
     */
    public void setID(String sID) {
        m_sID = sID;
    }
    
    /**
     *
     * @return
     */
    public String getID() {
        return m_sID;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return m_sName;
    }
    
    /**
     *
     * @param sName
     */
    public void setName(String sName) {
        m_sName = sName;
    }  

    /**
     *
     * @return
     */
    public String getPrinter() {
        return m_sPrinter;
    }
    
    /**
     *
     * @param sPrinter
     */
    public void setPrinter(String sPrinter) {
        m_sPrinter = sPrinter;
    } 
    
/**
     *
     * @return
     */
    public String getIp() {
        return m_sIp;
    }
    
    /**
     *
     * @param sIp
     */
    public void setIp(String sIp) {
        m_sIp = sIp;
    } 
    public String toString(){
        return m_sName;
    }    
}
