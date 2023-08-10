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
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.ListQBFModelNumber;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.LocalRes;
import com.openbravo.data.loader.QBFCompareEnum;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerWrite;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.user.BrowsableEditableData;
import com.openbravo.format.Formats;
import com.openbravo.pos.catalog.JCatalog;
import com.openbravo.pos.inventory.ProductsPanel;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.reports.ReportEditorCreator;
import com.openbravo.pos.sales.TaxesLogic;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataListener;

/**
 *
 * @author JG uniCenta
 */
public class ProductFilter extends javax.swing.JPanel implements ReportEditorCreator {
    
    private SentenceList m_sentcat;
    private ComboBoxValModel m_CategoryModel;
    private DataLogicSales dlSales;
    private TaxesLogic taxeslogic;
    private String m_category = "";

    private BrowsableEditableData m_bd;
    
//    private SentenceList m_sentsup;
//    private ComboBoxValModel m_SupplierModel;

    /** Creates new form JQBFProduct */
    public ProductFilter() {

        initComponents();
    }
    
    /**
     *
     * @param app
     */
    @Override
    public void init(AppView app) {
         
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");

        m_sentcat = dlSales.getCategoriesList();
        m_CategoryModel = new ComboBoxValModel();
         
    }
    
    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {

        List catlist = m_sentcat.list();
//        m_CategoryModel = new ComboBoxValModel(catlist);
        loadCatalog(catlist);
    }

    public void setBrowsableEditableData(BrowsableEditableData bd) {
        this.m_bd = bd;
    }
    
    /**
     *
     * @return
     */
    @Override
    public SerializerWrite getSerializerWrite() {
        return new SerializerWriteBasic(
            new Datas[] {
                Datas.OBJECT, Datas.STRING, 
                Datas.OBJECT, Datas.DOUBLE, 
                Datas.OBJECT, Datas.DOUBLE, 
                Datas.OBJECT, Datas.STRING, 
                Datas.OBJECT, Datas.STRING});
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
   
    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        
//        if (m_jBarcode.getText() == null || m_jBarcode.getText().equals("")) {
//
//            return new Object[] {
//                m_jCboName.getSelectedItem(), m_jName.getText(),
//                m_jCboPriceBuy.getSelectedItem(), Formats.CURRENCY.parseValue(m_jPriceBuy.getText()),           
//                m_jCboPriceSell.getSelectedItem(), Formats.CURRENCY.parseValue(m_jPriceSell.getText()),
//                m_CategoryModel.getSelectedKey() == null ? QBFCompareEnum.COMP_NONE : QBFCompareEnum.COMP_EQUALS, m_CategoryModel.getSelectedKey(),
////                m_SupplierModel.getSelectedKey() == null ? QBFCompareEnum.COMP_NONE : QBFCompareEnum.COMP_EQUALS, m_SupplierModel.getSelectedKey(),                                
//                QBFCompareEnum.COMP_NONE, null         
//            };
//        } else {            
//            return new Object[] {
//                QBFCompareEnum.COMP_NONE, null,
//                QBFCompareEnum.COMP_NONE, null,
//                QBFCompareEnum.COMP_NONE, null,
//                QBFCompareEnum.COMP_NONE, null,
//                QBFCompareEnum.COMP_EQUALS, m_jBarcode.getText()
//            };
//        }
        return null;
    } 

    private void loadCatalog(List catetory) throws BasicException {
        taxeslogic = new TaxesLogic(dlSales.getTaxList().list());
        // Load all categories.
//        java.util.List<CategoryInfo> categories = dlSales.getRootCategories(); 
        loadTopCategories(catetory);
    }

    private void loadTopCategories(java.util.List<CategoryInfo> categories){
        m_filterCategories.removeAll();
        for (CategoryInfo cat : categories) {
            JButton btn = new JButton();
            btn.applyComponentOrientation(getComponentOrientation());
            btn.setFocusPainted(false);
            btn.setFocusable(false);
            btn.setRequestFocusEnabled(false);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.setMargin(new Insets(2, 2, 2, 2));
            btn.setText(cat.getName());
            btn.setPreferredSize(new Dimension(90, 35));
            //btn.setFont(new java.awt.Font("Tahoma", 0, 12));
            btn.addActionListener(new SetCategory(cat.getID(), cat.getName()));
            btn.setBackground(new Color(34,36,40));
            btn.setName("cat_" + cat.getID());
            
            m_filterCategories.add(btn);
        }
    }

    public class SetCategory implements ActionListener {
        private final String category;
        private final String name;
        public SetCategory(String categoryItem, String nameItem) {
            this.category = categoryItem;
            this.name = nameItem;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            m_category = category;
            try {
                List dataList = dlSales.getProductCatQBFByCategory(m_category).list();
                if(name.equals("Fast Bar"))
                    dataList = dlSales.getProductCatQBFFastBar().list();

                if(name.equals("Liquor")) {
                    dataList = dlSales.getProductCatQBFLiquor().list();
                }

                m_bd.loadList(dataList);
            } catch (BasicException eD) {}
        }
    }

    public String getCategory() {
        return m_category;
    }
 
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_filterCategories = new com.openbravo.beans.JFlowPanel();

        setPreferredSize(new java.awt.Dimension(976, 125));

        javax.swing.GroupLayout m_filterCategoriesLayout = new javax.swing.GroupLayout(m_filterCategories);
        m_filterCategories.setLayout(m_filterCategoriesLayout);
        m_filterCategoriesLayout.setHorizontalGroup(
            m_filterCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 966, Short.MAX_VALUE)
        );
        m_filterCategoriesLayout.setVerticalGroup(
            m_filterCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 125, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 976, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(m_filterCategories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 125, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(m_filterCategories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.openbravo.beans.JFlowPanel m_filterCategories;
    // End of variables declaration//GEN-END:variables
    
}
