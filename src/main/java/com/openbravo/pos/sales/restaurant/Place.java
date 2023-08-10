//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright © 2009-2020 uniCenta & previous Openbravo POS works
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

package com.openbravo.pos.sales.restaurant;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.NullIcon;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import java.util.Date;

/**
 *
 * @author JG uniCenta
 */
public class Place implements SerializableRead, java.io.Serializable {
    
    private static final long serialVersionUID = 8652254694281L;
    private static final Icon ICO_OCU = new ImageIcon(Place.class.getResource("/com/openbravo/images/edit_group.png"));
    private static final Icon ICO_FRE = new NullIcon(22, 22);
    
    private String m_sId;
    private String m_sName;
    private String m_sSeats;
    private int m_ix;
    private int m_iy;
    private int m_diffx;
    private int m_diffy;
    private String m_sfloor;
    private String m_customer;
    private String m_waiter;
    private String m_ticketId;
    private Boolean m_tableMoved;
    private Boolean m_changed = false;    

    private boolean m_bPeople;
    private JButton m_btn;
    
    private int m_theight;
    private int m_twidth;
    private int m_guests;
    private Date m_occupied;
    
    
    /**
     * Creates a new instance of TablePlace
     */
    public Place() {
    }

    /**
     *
     * @param dr
     * @throws BasicException
     */
    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sId = dr.getString(1);
        m_sName = dr.getString(2);
        m_sSeats = dr.getString(3);        
        m_ix = dr.getInt(4);
        m_iy = dr.getInt(5);
        m_sfloor = dr.getString(6);
        m_customer = dr.getString(7);
        m_waiter = dr.getString(8);
        m_ticketId = dr.getString(9);
        m_tableMoved = dr.getBoolean(10);
        m_twidth = dr.getInt(11);
        m_theight = dr.getInt(12);
        m_guests = dr.getInt(13);
        m_occupied = dr.getTimestamp(14);
        
        m_bPeople = false;
        m_btn = new JButton();
        m_btn.setFocusPainted(false);
        m_btn.setFocusable(false);
        m_btn.setRequestFocusEnabled(false);
        m_btn.setHorizontalTextPosition(SwingConstants.CENTER);
        m_btn.setVerticalTextPosition(SwingConstants.BOTTOM);            
        m_btn.setIcon(ICO_FRE);
        m_btn.setText(m_sName);
        m_btn.setMargin(new Insets(2,5,2,5));
        m_diffx=0;
        m_diffy=0; 

    }
    
    public String getId() { 
        return m_sId; 
    }

    public void setId(String id) { 
        m_sId = id; 
    }
    
    public String getTicketID(){
        return m_ticketId;
    }

    public String getName() { 
        return m_sName; 
    }

    public void setName(String name) { 
        m_sName = name; 
    }
    
    public String getSeats() { 
        return m_sSeats; 
    }

    public int getX() { 
        return m_ix; 
    }

    public int getY() {
        return m_iy;
    }

    public void setX(int x) {
        this.m_ix = x;
    }

    public void setY(int y) {
        this.m_iy = y;
    }
    
    public int gettWidth() { 
        return m_twidth; 
    }

    public int gettHeight() {
        return m_theight;
    }

    public void settWidth(int twidth) {
        this.m_twidth = twidth;
    }

    public void settHeight(int theight) {
        this.m_theight = theight;
    }    

    public int getDiffX() {
        return m_diffx;
    }

    public int getDiffY() {
        return m_diffy;
    }

    public void setDiffX(int x) {
        this.m_diffx = x;
    }
    
    public void setDiffY(int y) {
        this.m_diffy = y;
    }
    
    public Boolean getChanged() {
        return m_changed;
    }

    public void setChanged(Boolean changed) {
        this.m_changed = changed;
    } 

    
    public String getFloor() { 
        return m_sfloor; 
    }
   
    public JButton getButton() { 
        return m_btn; 
    }
    
    public String getCustomer() {
        return m_customer;
    }

    public String getWaiter() {
        return m_waiter;
    }
    
    public boolean hasPeople() {
        return m_bPeople;
    }   

    public void setPeople(boolean bValue) {
        m_bPeople = bValue;
        m_btn.setIcon(bValue ? ICO_OCU : ICO_FRE);
    }     
    
    public int getGuests() {
        return m_guests;
    }

    public void setGuests(int guests) {
        this.m_guests = guests; 
    }
    
    public Date getOccupied() {
        return m_occupied;
    }

    public void setOccupied(Date occupied) {
        this.m_occupied = occupied; 
    }    

    public void setButtonBounds() {
        Dimension d = m_btn.getPreferredSize();
        m_btn.setPreferredSize(new Dimension(d.width + m_twidth - 35, d.height + m_theight - 10));        
        d = m_btn.getPreferredSize();
        m_btn.setBounds(m_ix - d.width / 2, m_iy - d.height / 2, d.width, d.height); 
    }
    
    public void setButtonText(String btnText) {
        m_btn.setText(btnText);         
    }    
}