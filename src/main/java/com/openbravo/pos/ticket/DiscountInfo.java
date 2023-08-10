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
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceFind;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.pos.inventory.AttributeSetInfo;

/**
 *
 * @author  Jack
 * @version 
 */
public class DiscountInfo implements IKeyed {

    private static final long serialVersionUID = 8712449444103L;
    private String m_sID;
    private String m_sName;
    private int m_iType;
    private boolean m_bMon;
    private boolean m_bTue;
    private boolean m_bWed;
    private boolean m_bThu;
    private boolean m_bFri;
    private boolean m_bSat;
    private boolean m_bSun;
    private String m_iFromTime;
    private String m_iToTime;
    private int m_iRate;
    private String m_sCategoryId;
    private int m_iAmount;
    /** Creates new ProductInfo
        *@param      
    */
    public DiscountInfo(String id, String name, int type, boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean sat, boolean sun, String fromtime, String totime, int rate, String categoryid, int amount) {
        m_sID = id;
        m_sName = name;
        m_iType = type;
        m_bMon = mon;
        m_bTue = tue;
        m_bWed = wed;
        m_bThu = thu;
        m_bFri = fri;
        m_bSat = sat;
        m_bSun = sun;
        m_iFromTime = fromtime;
        m_iToTime = totime;
        m_iRate = rate;
        m_sCategoryId = categoryid;
        m_iAmount = amount;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getKey() {
        return m_sID;
    }

    /**
     *
     * @param sID
     */
    public void setID(String sID) {
        m_sID = sID;
    }
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
    public void setName(String sName) {
        m_sName = sName;
    }

    /**
     *
     * @return
     */
    public int getType() {
        return m_iType;
    }
    public void setType(int iType) {
        m_iType = iType;
    }
 
    /**
     *
     * @return
     */
    public boolean getMon() {
        return m_bMon;
    }
    public void setMon(boolean bMon) {
        m_bMon = bMon;
    }

/**
     *
     * @return
     */
    public boolean getTue() {
        return m_bTue;
    }
    public void setTue(boolean bTue) {
        m_bTue = bTue;
    }

/**
     *
     * @return
     */
    public boolean getWed() {
        return m_bWed;
    }
    public void setWed(boolean bWed) {
        m_bWed = bWed;
    }

/**
     *
     * @return
     */
    public boolean getThu() {
        return m_bThu;
    }
    public void setThu(boolean bThu) {
        m_bThu = bThu;
    }

/**
     *
     * @return
     */
    public boolean getFri() {
        return m_bFri;
    }
    public void setFri(boolean bFri) {
        m_bFri = bFri;
    }

    /**
     *
     * @return
     */
    public boolean getSat() {
        return m_bSat;
    }
    public void setSat(boolean bSat) {
        m_bSat = bSat;
    }

/**
     *
     * @return
     */
    public boolean getSun() {
        return m_bSun;
    }
    public void setSun(boolean bSun) {
        m_bSun = bSun;
    }

    /**
     *
     * @return
     */
    public String getFromTime() {
        return m_iFromTime;
    }
    public void setFromTime(String iFromTime) {
        m_iFromTime = iFromTime;
    }

    /**
     *
     * @return
     */
    public String getToTime() {
        return m_iToTime;
    }
    public void setToTime(String iToTime) {
        m_iToTime = iToTime;
    }

    /**
     *
     * @return
     */
    public int getRate() {
        return m_iRate;
    }
    public void setRate(int iRate) {
        m_iRate = iRate;
    }

    /**
     *
     * @return
     */
    public String getCategoryId() {
        return m_sCategoryId;
    }
    public void setCategoryId(String sCategoryId) {
        m_sCategoryId = sCategoryId;
    }

    @Override
    public String toString() {
        return m_sName;
    }

    /**
     *
     * @return
     */
    public int getAmount() {
        return m_iAmount;
    }
    public void setAmount(int iAmount) {
        m_iAmount = iAmount;
    }
    /**
     *
     * @return
     */
    public static SerializerRead getSerializerRead() {
        return new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
            return new DiscountInfo(dr.getString(1), dr.getString(2), 
                dr.getInt(3), dr.getBoolean(4), dr.getBoolean(5), dr.getBoolean(6), dr.getBoolean(7), dr.getBoolean(8), dr.getBoolean(9), dr.getBoolean(10), dr.getString(11), dr.getString(12), dr.getInt(13), dr.getString(14), dr.getInt(15));
        }};
    }
}