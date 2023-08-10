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
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

class TypeItem
{
    private String key;
    private String value;

    public TypeItem(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return value;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }
}
/**
 *
 * @author JG uniCenta
 */
public class DiscountEditor extends JPanel implements EditorRecord {
    
    private Object m_oId;
    
    private SentenceList catsent;
    private ComboBoxValModel catmodel;    
   
    /** Creates new form taxEditor
     * @param app
     * @param dirty */
    public DiscountEditor(AppView app, DirtyManager dirty) {
        
        DataLogicSales dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        
        initComponents();
        catsent = dlSales.getCategoriesList();
        catmodel = new ComboBoxValModel();        

        m_jName.getDocument().addDocumentListener(dirty);
        m_jType.addActionListener(dirty);
        m_jCategory.addActionListener(dirty);
        m_jMon.addActionListener(dirty);
        m_jTue.addActionListener(dirty);
        m_jWed.addActionListener(dirty);
        m_jThu.addActionListener(dirty);
        m_jFri.addActionListener(dirty);
        m_jSat.addActionListener(dirty);
        m_jSun.addActionListener(dirty);
        m_jFromTime.getDocument().addDocumentListener(dirty);
        m_jToTime.getDocument().addDocumentListener(dirty);
        m_jRate.getDocument().addDocumentListener(dirty);
        m_jAmount.getDocument().addDocumentListener(dirty);

        writeValueEOF();
    }
    
    /**
     *
     * @throws BasicException
     */
    public void activate() throws BasicException {
        
        List a = catsent.list();
        catmodel = new ComboBoxValModel(a);
        m_jCategory.setModel(catmodel);
        
        m_jType.addItem(new TypeItem("1", "Category"));
        m_jType.addItem(new TypeItem("2", "Time"));
        m_jType.addItem(new TypeItem("3", "Time & Category"));
       
    }

   @Override
    public void refresh() {
    }
    /**
     *
     */
    @Override
    public void writeValueEOF() {
        m_oId = null;
        m_jName.setText(null);
        catmodel.setSelectedKey(null);
        m_jType.setSelectedIndex(-1);
        m_jCategory.setEnabled(false);
        m_jMon.setSelected(false);
        m_jTue.setSelected(false);
        m_jWed.setSelected(false);
        m_jThu.setSelected(false);
        m_jFri.setSelected(false);
        m_jSat.setSelected(false);
        m_jSun.setSelected(false);
        m_jToTime.setText(null);
        m_jFromTime.setText(null);
        m_jRate.setText("0");
        m_jAmount.setText("0");
        m_jName.setEnabled(false);
        m_jType.setEnabled(false);
        m_jMon.setEnabled(false);
        m_jTue.setEnabled(false);
        m_jWed.setEnabled(false);
        m_jThu.setEnabled(false);
        m_jFri.setEnabled(false);
        m_jSat.setEnabled(false);
        m_jSun.setEnabled(false);
        m_jFromTime.setEnabled(false);
        m_jToTime.setEnabled(false);
        m_jRate.setEnabled(false);
        m_jAmount.setEnabled(false);
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        m_oId = UUID.randomUUID().toString();
        m_jName.setText(null);
        catmodel.setSelectedKey(null);
        m_jType.setSelectedIndex(-1);
        m_jMon.setSelected(false);
        m_jTue.setSelected(false);
        m_jWed.setSelected(false);
        m_jThu.setSelected(false);
        m_jFri.setSelected(false);
        m_jSat.setSelected(false);
        m_jSun.setSelected(false);
        m_jFromTime.setText(null);
        m_jToTime.setText(null);
        m_jRate.setText("0");
        m_jAmount.setText("0");
        m_jName.setEnabled(true);
        m_jType.setEnabled(true);
        m_jCategory.setEnabled(true);
        m_jMon.setEnabled(true);    
        m_jTue.setEnabled(true);    
        m_jWed.setEnabled(true);    
        m_jThu.setEnabled(true);    
        m_jFri.setEnabled(true);    
        m_jSat.setEnabled(true);    
        m_jSun.setEnabled(true);    
        m_jFromTime.setEnabled(true);
        m_jToTime.setEnabled(true);
        m_jRate.setEnabled(true);
        m_jAmount.setEnabled(true);
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {

        Object[] discount = (Object[]) value;
        m_oId = discount[0];
        m_jName.setText(Formats.STRING.formatValue(discount[1]));
        m_jType.setSelectedIndex(((int) discount[2])-1);
        m_jMon.setSelected((Boolean) discount[3]);
        m_jTue.setSelected((Boolean) discount[4]);
        m_jWed.setSelected((Boolean) discount[5]);
        m_jThu.setSelected((Boolean) discount[6]);
        m_jFri.setSelected((Boolean) discount[7]);
        m_jSat.setSelected((Boolean) discount[8]);
        m_jSun.setSelected((Boolean) discount[9]);
        m_jFromTime.setText(Formats.STRING.formatValue(discount[10]));
        m_jToTime.setText(Formats.STRING.formatValue(discount[11]));
        m_jRate.setText(Formats.INT.formatValue(discount[12]));
        catmodel.setSelectedKey(discount[13]);        
        m_jAmount.setText(Formats.INT.formatValue(discount[14]));
        m_jName.setEnabled(false);
        m_jType.setEnabled(false);
        m_jCategory.setEnabled(false);
        m_jMon.setEnabled(false);    
        m_jTue.setEnabled(false);    
        m_jWed.setEnabled(false);    
        m_jThu.setEnabled(false);    
        m_jFri.setEnabled(false);    
        m_jSat.setEnabled(false);    
        m_jSun.setEnabled(false);    
        m_jFromTime.setEnabled(false);
        m_jToTime.setEnabled(false);
        m_jRate.setEnabled(false);
        m_jAmount.setEnabled(false);
    }    

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {

        Object[] discount = (Object[]) value;
        m_oId = discount[0];
        m_jName.setText(Formats.STRING.formatValue(discount[1]));
        m_jType.setSelectedIndex(((int) discount[2])-1);
        m_jMon.setSelected((Boolean) discount[3]);
        m_jTue.setSelected((Boolean) discount[4]);
        m_jWed.setSelected((Boolean) discount[5]);
        m_jThu.setSelected((Boolean) discount[6]);
        m_jFri.setSelected((Boolean) discount[7]);
        m_jSat.setSelected((Boolean) discount[8]);
        m_jSun.setSelected((Boolean) discount[9]);
        m_jFromTime.setText(Formats.STRING.formatValue(discount[10]));
        m_jToTime.setText(Formats.STRING.formatValue(discount[11]));
        m_jRate.setText(Formats.INT.formatValue(discount[12]));
        catmodel.setSelectedKey(discount[13]);
        m_jAmount.setText(Formats.INT.formatValue(discount[14]));
        m_jName.setEnabled(true);
        m_jType.setEnabled(true);
        m_jCategory.setEnabled(true);
        m_jMon.setEnabled(true);    
        m_jTue.setEnabled(true);    
        m_jWed.setEnabled(true);    
        m_jThu.setEnabled(true);    
        m_jFri.setEnabled(true);    
        m_jSat.setEnabled(true);    
        m_jSun.setEnabled(true);    
        m_jFromTime.setEnabled(true);
        m_jToTime.setEnabled(true);
        m_jRate.setEnabled(true);
        m_jAmount.setEnabled(true);
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        
        Object[] discount = new Object[15];

        discount[0] = m_oId;
        discount[1] = m_jName.getText();
        Object item = m_jType.getSelectedItem();
        String value = ((TypeItem)item).getKey();
        discount[2] = Formats.INT.parseValue(value);
        discount[3] = Boolean.valueOf(m_jMon.isSelected());
        discount[4] = Boolean.valueOf(m_jTue.isSelected());
        discount[5] = Boolean.valueOf(m_jWed.isSelected());
        discount[6] = Boolean.valueOf(m_jThu.isSelected());
        discount[7] = Boolean.valueOf(m_jFri.isSelected());
        discount[8] = Boolean.valueOf(m_jSat.isSelected());
        discount[9] = Boolean.valueOf(m_jSun.isSelected());
        discount[10] = m_jFromTime.getText();
        discount[11] = m_jToTime.getText();
        discount[12] = Formats.INT.parseValue(m_jRate.getText());
        discount[13] = catmodel.getSelectedKey(); 
        discount[14] = Formats.INT.parseValue(m_jAmount.getText());

        return discount;
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        m_jFromTime = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        m_jTue = new javax.swing.JCheckBox();
        m_jType = new javax.swing.JComboBox();
        m_jCategory = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        m_jToTime = new javax.swing.JTextField();
        m_jMon = new javax.swing.JCheckBox();
        m_jWed = new javax.swing.JCheckBox();
        m_jThu = new javax.swing.JCheckBox();
        m_jFri = new javax.swing.JCheckBox();
        m_jSat = new javax.swing.JCheckBox();
        m_jSun = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        m_jRate = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        m_jAmount = new javax.swing.JTextField();

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/info.png"))); // NOI18N
        jLabel2.setText("Name");
        jLabel2.setPreferredSize(new java.awt.Dimension(170, 30));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("From Time");
        jLabel3.setPreferredSize(new java.awt.Dimension(170, 30));

        m_jFromTime.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jFromTime.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText("Time");
        jLabel1.setPreferredSize(new java.awt.Dimension(170, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("Category");
        jLabel4.setPreferredSize(new java.awt.Dimension(170, 30));

        m_jTue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTue.setText("Tue");
        m_jTue.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jType.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jType.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCategory.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText("To Time");
        jLabel6.setPreferredSize(new java.awt.Dimension(170, 30));

        m_jToTime.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jToTime.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jMon.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jMon.setText("Mon");
        m_jMon.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jWed.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jWed.setText("Wed");
        m_jWed.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jThu.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jThu.setText("Thu");
        m_jThu.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jFri.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jFri.setText("Fri");
        m_jFri.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jSat.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSat.setText("Sat");
        m_jSat.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jSun.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSun.setText("Sun");
        m_jSun.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText("Rate");
        jLabel7.setPreferredSize(new java.awt.Dimension(170, 30));

        m_jRate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jRate.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText("Amount");
        jLabel8.setPreferredSize(new java.awt.Dimension(170, 30));

        m_jAmount.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jAmount.setPreferredSize(new java.awt.Dimension(0, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_jFri, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jSat, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(m_jSun, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jType, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jToTime, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jFromTime, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_jMon, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(m_jTue, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jWed, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(m_jThu, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jRate, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jTue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jWed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jThu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jMon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jFri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jSat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jSun, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jFromTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jToTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        
        if (evt.getClickCount() == 2) {
            String uuidString = m_oId.toString();
            StringSelection stringSelection = new StringSelection(uuidString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        
            JOptionPane.showMessageDialog(null, 
                AppLocal.getIntString("message.uuidcopy"));
        }
    }//GEN-LAST:event_jLabel2MouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JTextField m_jAmount;
    private javax.swing.JComboBox m_jCategory;
    private javax.swing.JCheckBox m_jFri;
    private javax.swing.JTextField m_jFromTime;
    private javax.swing.JCheckBox m_jMon;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextField m_jRate;
    private javax.swing.JCheckBox m_jSat;
    private javax.swing.JCheckBox m_jSun;
    private javax.swing.JCheckBox m_jThu;
    private javax.swing.JTextField m_jToTime;
    private javax.swing.JCheckBox m_jTue;
    private javax.swing.JComboBox m_jType;
    private javax.swing.JCheckBox m_jWed;
    // End of variables declaration//GEN-END:variables
    
}
