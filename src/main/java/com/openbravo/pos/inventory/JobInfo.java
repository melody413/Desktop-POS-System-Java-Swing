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
public class JobInfo implements SerializableRead, IKeyed {
    
    private static final long serialVersionUID = 9032683595230L;
    private String m_sID;
    private String m_sJobName;
    private String m_sRoleId;
//    private double m_dSal;

    /** Creates a new instance of JobInfo */
    public JobInfo() {
        m_sID = null;
        m_sJobName = null;
        m_sRoleId = null;
//        m_dSal = 0;
    }
    
    public JobInfo(String sID, String sJobName, String sRoleId, double dSal) {
        m_sID = sID;
        m_sRoleId = sRoleId;
        m_sJobName = sJobName;      
//        m_dSal = dSal;
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
        m_sJobName = dr.getString(2);
        m_sRoleId = dr.getString(3);
//        m_dSal = dr.getDouble(4);
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
    public String getJobName() {
        return m_sJobName;
    }
    
    /**
     *
     * @param sName
     */
    public void setJobName(String sJobName) {
        m_sJobName = sJobName;
    }  

    /**
     *
     * @return
     */
    public String getRoleId() {
        return m_sRoleId;
    }
    
    /**
     *
     * @param sRoleId
     */
    public void setRoleId(String sRoleId) {
        m_sRoleId = sRoleId;
    } 

/**
     *
     * @return
     */
//    public double getSal() {
//        return m_dSal;
//    }
    
    /**
     *
     * @param dSal
     */
//    public void setSal(double dSal) {
//        m_dSal = dSal;
//    } 
    public String toString(){
        return m_sJobName;
    }    
}
