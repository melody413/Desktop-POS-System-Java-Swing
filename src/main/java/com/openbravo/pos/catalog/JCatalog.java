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

package com.openbravo.pos.catalog;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JFlowPanel;
import com.openbravo.beans.JDiscountPop;
import com.openbravo.beans.JModDialog;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceFind;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerReadString;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.ticket.CategoryInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.util.ThumbNailBuilder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.openbravo.pos.inventory.AttributeInfo;
import com.openbravo.pos.inventory.AttributeValue;
import com.openbravo.pos.inventory.Modifier;
import com.openbravo.pos.sales.JPanelTicket;
import com.openbravo.pos.ticket.DiscountInfo;
import com.openbravo.pos.util.StringUtils;
import com.sleepycat.persist.model.ModelInternal;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import org.joda.time.DateTime;

/**
 *
 * @author adrianromero
 */
public class JCatalog extends JPanel implements ListSelectionListener, CatalogSelector {
    
    /**
     *
     */
    protected EventListenerList listeners = new EventListenerList();
    private DataLogicSales m_dlSales;   
    private TaxesLogic taxeslogic;
    
    private boolean pricevisible;
    private boolean taxesincluded;
    
    // Set of Products panels
    private final Map<String, ProductInfoExt> m_productsset = new HashMap<>();
    
    // Set of Categoriespanels
     private final Set<String> m_categoriesset = new HashSet<>();
        
    private ThumbNailBuilder tnbbutton;
    private ThumbNailBuilder tnbcat;
    private ThumbNailBuilder tnbsubcat;
    
    private CategoryInfo showingcategory = null;
    private JCatalogTab m_jcurrTab;
    private SentenceList m_sentprods;

    JFlowPanel m_flowPanel;
    private List<String> m_modifier = new ArrayList();
    private List<String> m_modifierForMod = new ArrayList();
    private String attInstanceId;
    private String attInstanceDescription;
    private Session s;
    private String currentCatId="";
    private String currentSubCatId="";
    protected AppView m_App;
    /** Creates new form JCatalog
     * @param dlSales */
    public JCatalog(DataLogicSales dlSales) {
        this(dlSales, false, false, 90, 60);
    }

    /**
     *
     * @param dlSales
     * @param pricevisible
     * @param taxesincluded
     * @param width
     * @param height
     */
    public JCatalog(DataLogicSales dlSales, boolean pricevisible, 
            boolean taxesincluded, int width, int height) {
        
        m_dlSales = dlSales;
        this.pricevisible = pricevisible;
        this.taxesincluded = taxesincluded;
        
        initComponents();
        
        m_jListCategories.addListSelectionListener(this);
        
        m_jscrollcat.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        
        tnbcat = new ThumbNailBuilder(48, 48, "com/openbravo/images/category.png");  
        tnbsubcat = new ThumbNailBuilder(width, height, "com/openbravo/images/subcategory.png"); 
        tnbbutton = new ThumbNailBuilder(width, height, "com/openbravo/images/null.png");        
        this.m_App = m_App;
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
     * @param id
     */
    @Override
    public void showCatalogPanel(String id) {
           
        if (id == null) {
            showRootCategoriesPanel();
        } else {       
            showProductPanel(id);
        }
    }
    
    public Component getCatComponent() {
        return m_jCategories;
    }

    public Component getProductComponent() {
        return m_jProducts;
    }

    // Nam Ho ADD
    public String getAttInstanceId(){
        return attInstanceId;
    }
    public String getAttInstanceDescription(){
        return attInstanceDescription;
    }
    public List<String> getModifier(){
        return m_modifier;
    }
    public List<String> getModifierForMod(){
        return m_modifierForMod;
    }
    // Nam Ho ADD
    public boolean setControls(String position) {
        if (position.equals("south")) {
            m_jRootCategories.add(jPanel2, BorderLayout.SOUTH);
            m_jSubCategories.add(jPanel1, BorderLayout.SOUTH);
            ((GridLayout) jPanel3.getLayout()).setRows(1);
            ((GridLayout) jPanel5.getLayout()).setRows(1);
            return true;
        }
        return false;
    }    
    
    
    /**
     *
     * @throws BasicException
     */
    @Override
    public void loadCatalog() throws BasicException {
        
        // delete all categories panel
        m_jProducts.removeAll();
        
        m_productsset.clear();        
        m_categoriesset.clear();
        
        showingcategory = null;
                
        // Load the taxes logic
        taxeslogic = new TaxesLogic(m_dlSales.getTaxList().list());

        // Load all categories.
        java.util.List<CategoryInfo> categories = m_dlSales.getRootCategories(); 
        
        // Select the first category
        m_jListCategories.setCellRenderer(new SmallCategoryRenderer());
        m_jListCategories.setModel(new CategoriesListModel(categories)); // aCatList
        m_jRootCategories.setVisible(false);
        loadTopCategories(categories);
        if (m_jListCategories.getModel().getSize() == 0) {
            m_jscrollcat.setVisible(false);
            jPanel2.setVisible(false);
        } else {
            m_jscrollcat.setVisible(true);
            jPanel2.setVisible(true);
            m_jListCategories.setSelectedIndex(0);
        }
            
        showRootCategoriesPanel();
    }
    
    /**
     *
     * @param value
     */
    @Override
    public void setComponentEnabled(boolean value) {
        
        m_jListCategories.setEnabled(value);
        m_jscrollcat.setEnabled(value);
        m_lblIndicator.setEnabled(value);
        m_btnBack1.setEnabled(value);
        m_jProducts.setEnabled(value); 

        synchronized (m_jProducts.getTreeLock()) {
            int compCount = m_jProducts.getComponentCount();
            for (int i = 0 ; i < compCount ; i++) {
                m_jProducts.getComponent(i).setEnabled(value);
            }
        }
     
        this.setEnabled(value);
    }
    
    /**
     *
     * @param l
     */
    @Override
    public void addActionListener(ActionListener l) {
        listeners.add(ActionListener.class, l);
    }

    /**
     *
     * @param l
     */
    @Override
    public void removeActionListener(ActionListener l) {
        listeners.remove(ActionListener.class, l);
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        
        if (!evt.getValueIsAdjusting()) {
            int i = m_jListCategories.getSelectedIndex();
            if (i >= 0) {
                // Lo hago visible...
                Rectangle oRect = m_jListCategories.getCellBounds(i, i);
                m_jListCategories.scrollRectToVisible(oRect);       
            }
        }
    }

    public void showSeatData(){
        int tableSeats = TicketInfo.tableSeatCount;
        Dimension catPanelSize = m_jSCategories.getPreferredSize();
        if(tableSeats>1){
            if(tableSeats>1){
                m_jSeat1.setVisible(true);
                m_jSeat2.setVisible(true);
            }else{
                m_jSeat1.setVisible(false);
                m_jSeat2.setVisible(false);
            }   
            if(tableSeats>2){
                m_jSeat3.setVisible(true);
            }else{
                m_jSeat3.setVisible(false);
            }
            if(tableSeats>3){
                m_jSeat4.setVisible(true);
            }else{
                m_jSeat4.setVisible(false);
            }
            seatPanel.setVisible(true);
            if(tableSeats<TicketInfo.ticketlineSeat){
                TicketInfo.ticketlineSeat = 1;
                m_jLblSeat.setText("Seat #1");
            }
            jPanel6.setPreferredSize(new Dimension(500,250+catPanelSize.height));
        }else{
          TicketInfo.ticketlineSeat = 1;
          m_jLblSeat.setText("Seat #1");
          seatPanel.setVisible(false); 
          jPanel6.setPreferredSize(new Dimension(500,200+catPanelSize.height));
        }
    }
    /**
     *
     * @param prod
     */
    protected void fireSelectedProduct(ProductInfoExt prod) {
        String catId = currentCatId;
        String subCatId = currentSubCatId;
        EventListener[] l = listeners.getListeners(ActionListener.class);
        ActionEvent e = null;
        for (EventListener l1 : l) {
            if (e == null) {
                e = new ActionEvent(prod, ActionEvent.ACTION_PERFORMED, prod.getID());
            }
            ((ActionListener) l1).actionPerformed(e);	       
        }
        selectCategoryPanel(catId, false);
        if (!"".equals(currentSubCatId)){
            selectCategoryPanel(subCatId, true);             
        }      
    }   

    /**
     *
     * @param prod
     */
    protected void updateSelectedProduct(ProductInfoExt prod) {
        String catId = currentCatId;
        String subCatId = currentSubCatId;
        EventListener[] l = listeners.getListeners(ActionListener.class);
        ActionEvent e = null;
        for (EventListener l1 : l) {
            if (e == null) {
                e = new ActionEvent(prod, ActionEvent.ACTION_PERFORMED, prod.getID());
            }
            ((ActionListener) l1).actionPerformed(e);	       
        }
        selectCategoryPanel(catId, false);
        if (!"".equals(currentSubCatId)){
            selectCategoryPanel(subCatId, true);             
        }      
    }  
    
    private void selectCategoryPanel(String catid, boolean isSubCat) {
        try {
               if(isSubCat)
                    setFocusSubCat(catid);
               else
                    setFocusCat(catid);
               java.util.List<CategoryInfo> categories = m_dlSales.getSubcategories(catid);
                if(!categories.isEmpty() || !isSubCat){
                    m_jSCategories.removeAll();
                }
                for (CategoryInfo cat : categories) {
                    JButton btn = new JButton();
                    btn.applyComponentOrientation(getComponentOrientation());
                    String btnText = cat.getName();
                    btn.setText(btnText);
                    btn.setFocusPainted(false);
                    btn.setFocusable(false);
                    btn.setRequestFocusEnabled(false);
                    btn.setHorizontalTextPosition(SwingConstants.CENTER);
                    btn.setVerticalTextPosition(SwingConstants.BOTTOM);
                    btn.setMargin(new Insets(2, 2, 2, 2));
                    btn.setPreferredSize(new Dimension(116, 60));
                    btn.addActionListener(new SelectedCategory(cat));
                    btn.setOpaque(true);
                    btn.setBackground(new Color(3, 23, 67));
                    btn.setForeground(Color.WHITE);
                    btn.setName("cat_" + cat.getID());
                    m_jSCategories.add(btn);
                }
                m_jSCategories.revalidate();
                m_jSCategories.repaint();
                if(!categories.isEmpty()){
                    CategoryInfo cat = categories.get(0);
                    showSubcategoryPanel(cat);
                }else{
                    //Set size of Cat Panel
                    Dimension catPanelSize = m_jSCategories.getPreferredSize();
                    if(TicketInfo.tableSeatCount>1){
                        jPanel6.setPreferredSize(new Dimension(500,250 + catPanelSize.height));
                    }else{
                      jPanel6.setPreferredSize(new Dimension(500,200 + catPanelSize.height));
                    }
                    if (!m_categoriesset.contains(catid)) {
                        m_jcurrTab = new JCatalogTab();
                        m_jcurrTab.applyComponentOrientation(getComponentOrientation());
                        m_jProducts.add(m_jcurrTab, catid);
                        m_categoriesset.add(catid);
                        java.util.List<ProductInfoExt> prods = m_dlSales.getProductConstant();
                        for (ProductInfoExt prod : prods) {
                            JButton btn = new JButton();
                            btn.applyComponentOrientation(getComponentOrientation());
                            String btnText = getProductLabel(prod);
                            btn.setText(btnText);
                            btn.setFocusPainted(false);
                            btn.setFocusable(false);
                            btn.setRequestFocusEnabled(false);
                            btn.setHorizontalTextPosition(SwingConstants.CENTER);
                            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
                            btn.setMargin(new Insets(2, 2, 2, 2));
                            btn.setPreferredSize(new Dimension(116, 60));
                            btn.addActionListener(new SelectedAction(prod, m_jcurrTab));
                            btn.setOpaque(true);
                            btn.setBackground(new Color(3, 23, 67));
                            btn.setForeground(Color.WHITE);
                            m_jcurrTab.flowpanel.add(btn);
                        }            

                        String category = "";
                        try {
                            CategoryInfo categoryInfo;
                            categoryInfo = m_dlSales.getCategoryInfo(catid);
                            category = categoryInfo.getName();
                        } catch (BasicException eData) {
                            new MessageInf(eData).show(this);
                        }   

                        java.util.List<ProductInfoExt> products;

                        if(category.equals("Fast Bar")){
                            m_sentprods = m_dlSales.getProductList();
                            products = m_sentprods.list();
                        } else {
                            products = m_dlSales.getProductCatalog(catid);
                        } 

                        java.util.List<DiscountInfo> discounts = m_dlSales.getDiscounts();
                        for (ProductInfoExt prod : products) {
                            //Get discount rate Start
                            String categoryid = prod.getCategoryID();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
                            Calendar calendar = Calendar.getInstance();
                            int currentTime = Integer.valueOf(dateFormat.format(calendar.getTime()));
                            int day = calendar.get(Calendar.DAY_OF_WEEK);
                            int b_disc = 0;
                            int b_amount = 0;
                            for (DiscountInfo discount : discounts) {
                                if(discount.getType()==1 && discount.getCategoryId().equals(categoryid)){
                                    b_disc=discount.getRate();
                                    b_amount = discount.getAmount();
                                    break;
                                }else if (discount.getType()==2){ //Discount by time range
                                    if((day==2 && discount.getMon())||(day==3 && discount.getTue())||(day==4 && discount.getWed())||(day==5 && discount.getThu())||(day==6 && discount.getFri())||(day==7 && discount.getSat())||(day==1 && discount.getSun())){
                                        if((StringUtils.isEmpty(discount.getFromTime()) && StringUtils.isEmpty(discount.getToTime()))||(currentTime>=Integer.valueOf(discount.getFromTime()) && currentTime<=Integer.valueOf(discount.getToTime()))){
                                            b_disc=discount.getRate();
                                            b_amount = discount.getAmount();
                                            break;
                                        }
                                    }
                                }else{
                                    if(discount.getCategoryId().equals(categoryid) && ((day==2 && discount.getMon())||(day==3 && discount.getTue())||(day==4 && discount.getWed())||(day==5 && discount.getThu())||(day==6 && discount.getFri())||(day==7 && discount.getSat())||(day==1 && discount.getSun()))){
                                        if((StringUtils.isEmpty(discount.getFromTime()) && StringUtils.isEmpty(discount.getToTime()))||(currentTime>=Integer.valueOf(discount.getFromTime()) && currentTime<=Integer.valueOf(discount.getToTime()))){
                                            b_disc=discount.getRate();
                                            b_amount = discount.getAmount();
                                            break;
                                        }
                                    }
                                }
                            }
                            if(b_disc>0){
                                double discountrate = 1 - (b_disc)/100d;
                                double price = prod.getPriceSell();
                                price = (double)Math.rint(price * discountrate *100) /100d;
                                prod.setPriceSell(price);
                            }else if(b_amount>0){
                                double price = prod.getPriceSell();
                                price = price - b_amount;
                                prod.setPriceSell(price);
                            }
                            //Get discount rate End                            
                            // Happy Hour Price Start
                            if(day>1 && day<7 && currentTime>=150000 && currentTime<=181000 && prod.getPriceSellH()>0.0){
                                prod.setPriceSell(prod.getPriceSellH());
                            }
                            // Happy Hour Price End
                            JButton btn = new JButton();
                            btn.applyComponentOrientation(getComponentOrientation());
                            String btnText = getProductLabel(prod);
                            btn.setText(btnText);
                            btn.setFocusPainted(false);
                            btn.setFocusable(false);
                            btn.setRequestFocusEnabled(false);
                            btn.setHorizontalTextPosition(SwingConstants.CENTER);
                            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
                            btn.setMargin(new Insets(2, 2, 2, 2));
                            btn.setPreferredSize(new Dimension(116, 60));

                            if (prod.isVerpatrib()) btn.addActionListener(new SelectedAction(prod, m_jcurrTab));
                            else btn.addActionListener(new AddToTicket(prod, m_jcurrTab));

                            btn.setOpaque(true);
                            btn.setBackground(new Color(3, 23, 67));
                            btn.setForeground(Color.WHITE);

                            int flag = 1;

                            //Fast Bar
                            if (category.equals("Fast Bar")){
                                if(!prod.isFastBar()) flag = 0;
                            }

                            //Happy Hour Price
                            if(prod.isDisableRegPrice()) {
                                DateTime dt = new DateTime();
                                int hour = dt.getHourOfDay();
                                if ((hour < 15) || (hour > 18)) {
                                    flag = 0;
                                }
                            }

                            if (flag == 1) m_jcurrTab.flowpanel.add(btn);
                        }
                    }

                    CardLayout cl = (CardLayout)(m_jProducts.getLayout());
                    cl.show(m_jProducts, catid);  
                }
            Dimension catPanelSize = m_jSCategories.getPreferredSize();
            int tableSeats = TicketInfo.tableSeatCount;
            if(tableSeats>1){
                jPanel6.setPreferredSize(new Dimension(500,250+catPanelSize.height));
            }else{
                jPanel6.setPreferredSize(new Dimension(500,200+catPanelSize.height));
            }
            jPanel6.revalidate();
            jPanel6.repaint();
        } catch (BasicException e) {
            JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_WARNING, 
                AppLocal.getIntString("message.notactive"), e));            
        }
    }
    
    private String getProductLabel(ProductInfoExt product) {

        if (pricevisible) {
            if (taxesincluded) {
                TaxInfo tax = taxeslogic.getTaxInfo(product.getTaxCategoryID());
                if(!"".equals(product.getDisplay())){
                    return "<html><center>" + product.getDisplay() + "<br>" + product.printPriceSell();                
                } else {
                    return "<html><center>" + product.getName() + "<br>" + product.printPriceSell();                                    
                }
            } else {
                if(!"".equals(product.getDisplay())){
                    return "<html><center>" + product.getDisplay() + "<br>" + product.printPriceSell();                
                } else {
                    return "<html><center>" + product.getName() + "<br>" + product.printPriceSell();                
                }                
            }
        } else {

            if (!"".equals(product.getDisplay())) {
                return product.getDisplay();                
            } else {
                return product.getName();
            }
        }
    }
    
    private void selectIndicatorPanel(Icon icon, String label, String texttip) {
        
        m_lblIndicator.setText(label);
        m_lblIndicator.setIcon(icon);
        
        // Show subcategories panel
        CardLayout cl = (CardLayout)(m_jCategories.getLayout());
        cl.show(m_jCategories, "subcategories");
    }
    
    private void selectIndicatorCategories() {
        // Show root categories panel
        CardLayout cl = (CardLayout)(m_jCategories.getLayout());
        cl.show(m_jCategories, "rootcategories");
    }
    
    private void showRootCategoriesPanel() {
        
        selectIndicatorCategories();
        // Show selected root category
        CategoryInfo cat = (CategoryInfo) m_jListCategories.getSelectedValue();
        
        if (cat != null) {
            selectCategoryPanel(cat.getID(), false);
        }
        showingcategory = null;
    }
    
    private void showSubcategoryPanel(CategoryInfo category) {
// Modified JDL 13.04.13
// this is the new panel that displays when a sub catergory is selected mouse does not work here        
//        selectIndicatorPanel(new ImageIcon(tnbsubcat.getThumbNail(
//            category.getImage())),category.getName(), category.getTextTip());
        selectCategoryPanel(category.getID(), true);
        showingcategory = category;
    }
   
    private void showProductPanel(String id) {
        
        ProductInfoExt product = m_productsset.get(id);

        if (product == null) {
            if (m_productsset.containsKey(id)) {
                // It is an empty panel
                if (showingcategory == null) {
                    showRootCategoriesPanel();                         
                } else {
                    showSubcategoryPanel(showingcategory);
                }
            } else {
                try {
                    // Create  products panel
                    java.util.List<ProductInfoExt> products = m_dlSales.getProductComments(id);

                    if (products.isEmpty()) {                    
                        m_productsset.put(id, null);

                        if (showingcategory == null) {
                            showRootCategoriesPanel();                         
                        } else {
                            showSubcategoryPanel(showingcategory);
                        }
                    } else {

                        product = m_dlSales.getProductInfo(id);
                        m_productsset.put(id, product);

                        JCatalogTab jcurrTab = new JCatalogTab();      
                        jcurrTab.applyComponentOrientation(getComponentOrientation());
                        m_jProducts.add(jcurrTab, "PRODUCT." + id);                        

                        // Add products
                        for (ProductInfoExt prod : products) {
                            jcurrTab.addButton(new ImageIcon(tnbbutton.getThumbNailText(prod.getImage(), 
                                getProductLabel(prod))), new SelectedAction(prod, m_jcurrTab),prod.getTextTip());                            
                        }                       
                        selectIndicatorPanel(new ImageIcon(tnbbutton.getThumbNail(product.getImage())),
                            product.getDisplay(), product.getTextTip());                        
                        
                        CardLayout cl = (CardLayout)(m_jProducts.getLayout());
                        cl.show(m_jProducts, "PRODUCT." + id); 
                    }
                } catch (BasicException eb) {
                    m_productsset.put(id, null);
                    if (showingcategory == null) {
                        showRootCategoriesPanel();                         
                    } else {
                        showSubcategoryPanel(showingcategory);
                    }
                }
            }
        } else {
            selectIndicatorPanel(new ImageIcon(tnbbutton.getThumbNail(
                product.getImage())), product.getName(), product.getTextTip());            
            
            CardLayout cl = (CardLayout)(m_jProducts.getLayout());
            cl.show(m_jProducts, "PRODUCT." + id); 
        }
    }

    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window) parent;
        } else {
            return getWindow(parent.getParent());
        }
    }
    
    private class SelectedAction implements ActionListener {
        private final ProductInfoExt prod;
        private final JCatalogTab jTab;
        public SelectedAction(ProductInfoExt prod, JCatalogTab jTab) {
            this.prod = prod;
            this.jTab = jTab;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            try{
                JFlowPanel panel = showModifiertoMod(prod.getAttributeSetID(), null); 
                Window window = getWindow(JCatalog.this);
                JModDialog modDialog = JModDialog.newJDialog(window);

                modDialog.setUpdateAction(new UpdateTicket(prod));
                String m_resModDialog = modDialog.showModDialog(panel, prod);
                if(m_resModDialog == "ok") {
//                    addToTicket(prod, jTab);
                }
            }catch(BasicException ex){}
        }
    }
    
    private class SelectedCategory implements ActionListener {
        private final CategoryInfo category;
        public SelectedCategory(CategoryInfo category) {
            this.category = category;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            showSubcategoryPanel(category);
        }
    }
    
    private class BackCategory implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            
        }
    }

    private class SelectedTopCategory implements ActionListener {
        private final CategoryInfo category;
        public SelectedTopCategory(CategoryInfo category) {
            this.category = category;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            selectCategoryPanel(category.getID(), false);
        }
    }
    
    private class SelectedModifier implements ActionListener {
        private final String modifier;
        public SelectedModifier(String modifier) {
            this.modifier = modifier;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            //Remove all modifier
            Component[] components = m_jcurrTab.flowpanel.getComponents();
            String[] values = modifier.split("_");
            if(m_modifier != null && m_modifier.contains(modifier)) {
                for (Component component : components){
                    if(component.getName()!= null && component.getName().equals("modifier_d_" + values[0] + "_" + values[1])){
                        component.setBackground(component.getForeground());
                        component.setForeground(Color.WHITE);                        
                        break;
                    }
                }
                m_modifier.remove(modifier);
            }else{
                for (Component component : components){
                    if(component.getName()!= null && component.getName().equals("modifier_d_" + values[0] + "_" + values[1])){
                        component.setForeground(component.getBackground());
                        component.setBackground(Color.ORANGE);
                        break;
                    }
                }
                m_modifier.add(modifier);
            }
        }
    }

    private class SelectedModifierForMod implements ActionListener {
        private final String modifier;
        public SelectedModifierForMod(String modifier) {
            this.modifier = modifier;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            //Remove all modifier
            Component[] components = m_flowPanel.getComponents();
            String[] values = modifier.split("_");
            if(m_modifierForMod != null && m_modifierForMod.contains(modifier)) {
                for (Component component : components){
                    if(component.getName()!= null && component.getName().equals("modifier_d_mod_" + values[0] + "_" + values[1])){
                        component.setForeground(Color.WHITE);
                        component.setBackground(component.getForeground());                       
                        break;
                    }
                }
                m_modifierForMod.remove(modifier);
            } else {
                for (Component component : components){
                    if(component.getName()!= null && component.getName().equals("modifier_d_mod_" + values[0] + "_" + values[1])){
                        component.setForeground(Color.WHITE);
                        component.setBackground(Color.BLUE); 
                        break;
                    }
                }
                m_modifierForMod.add(modifier);
            }
        }
    }
    private class AddToTicket implements ActionListener {
        private final ProductInfoExt prod;
        private final JCatalogTab jTab;
        public AddToTicket(ProductInfoExt prod, JCatalogTab jTab) {
            this.prod = prod;
            this.jTab = jTab;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            addToTicket(prod, jTab);
        }
    }

    public class UpdateTicket implements ActionListener {
        private final ProductInfoExt prod;
        public UpdateTicket(ProductInfoExt prod) {
            this.prod = prod;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            updateTicket(prod);
        }
    }

    private class CategoriesListModel extends AbstractListModel {
        private final java.util.List m_aCategories;
        public CategoriesListModel(java.util.List aCategories) {
            m_aCategories = aCategories;
        }
        @Override
        public int getSize() { 
            return m_aCategories.size(); 
        }
        @Override
        public Object getElementAt(int i) {
            return m_aCategories.get(i);
        }    
    }
    
    private class SmallCategoryRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, 
          int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
            CategoryInfo cat = (CategoryInfo) value;
            setText(cat.getName());
            setIcon(new ImageIcon(tnbcat.getThumbNail(cat.getImage())));

            return this;
        }      
    }            
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        seatPanel = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        m_jSeat1 = new javax.swing.JButton();
        m_jSeat2 = new javax.swing.JButton();
        m_jSeat3 = new javax.swing.JButton();
        m_jSeat4 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        m_jLblSeat = new javax.swing.JLabel();
        m_jRootCategories = new javax.swing.JPanel();
        m_jscrollcat = new javax.swing.JScrollPane();
        m_jListCategories = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        m_jCategories = new javax.swing.JPanel();
        m_jListTopCategories = new com.openbravo.beans.JFlowPanel();
        m_jSubCategories = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_lblIndicator = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        m_btnBack1 = new javax.swing.JButton();
        m_jSCategories = new com.openbravo.beans.JFlowPanel();
        jPanel9 = new javax.swing.JPanel();
        m_jProducts = new javax.swing.JPanel();

        setBackground(new java.awt.Color(24, 26, 30));
        setLayout(new java.awt.BorderLayout());

        jPanel6.setBackground(new java.awt.Color(24, 26, 30));
        jPanel6.setPreferredSize(new java.awt.Dimension(500, 390));
        jPanel6.setLayout(new java.awt.BorderLayout());

        seatPanel.setBackground(new java.awt.Color(47, 49, 53));
        seatPanel.setPreferredSize(new java.awt.Dimension(100, 50));
        seatPanel.setLayout(new java.awt.BorderLayout());

        jPanel7.setBackground(new java.awt.Color(47, 49, 53));

        m_jSeat1.setBackground(new java.awt.Color(2, 2, 4));
        m_jSeat1.setText("Seat #1");
        m_jSeat1.setPreferredSize(new java.awt.Dimension(80, 40));
        m_jSeat1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jSeat1ActionPerformed(evt);
            }
        });
        jPanel7.add(m_jSeat1);

        m_jSeat2.setBackground(new java.awt.Color(2, 2, 4));
        m_jSeat2.setText("Seat #2");
        m_jSeat2.setPreferredSize(new java.awt.Dimension(80, 40));
        m_jSeat2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jSeat2ActionPerformed(evt);
            }
        });
        jPanel7.add(m_jSeat2);

        m_jSeat3.setBackground(new java.awt.Color(2, 2, 4));
        m_jSeat3.setText("Seat #3");
        m_jSeat3.setPreferredSize(new java.awt.Dimension(80, 40));
        m_jSeat3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jSeat3ActionPerformed(evt);
            }
        });
        jPanel7.add(m_jSeat3);

        m_jSeat4.setBackground(new java.awt.Color(2, 2, 4));
        m_jSeat4.setText("Seat #4");
        m_jSeat4.setPreferredSize(new java.awt.Dimension(80, 40));
        m_jSeat4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jSeat4ActionPerformed(evt);
            }
        });
        jPanel7.add(m_jSeat4);

        seatPanel.add(jPanel7, java.awt.BorderLayout.LINE_START);

        jPanel8.setBackground(new java.awt.Color(47, 49, 53));
        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 10));

        m_jLblSeat.setText("Seat #1");
        jPanel8.add(m_jLblSeat);

        seatPanel.add(jPanel8, java.awt.BorderLayout.LINE_END);

        jPanel6.add(seatPanel, java.awt.BorderLayout.PAGE_START);

        m_jRootCategories.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        m_jRootCategories.setMinimumSize(new java.awt.Dimension(200, 100));
        m_jRootCategories.setPreferredSize(new java.awt.Dimension(275, 130));
        m_jRootCategories.setLayout(new java.awt.BorderLayout());

        m_jscrollcat.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 0, new java.awt.Color(0, 0, 0)));
        m_jscrollcat.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        m_jscrollcat.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jscrollcat.setPreferredSize(new java.awt.Dimension(265, 130));

        m_jListCategories.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jListCategories.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        m_jListCategories.setFocusable(false);
        m_jListCategories.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                m_jListCategoriesValueChanged(evt);
            }
        });
        m_jscrollcat.setViewportView(m_jListCategories);

        m_jRootCategories.add(m_jscrollcat, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanel3.setLayout(new java.awt.GridLayout(0, 1, 0, 5));
        jPanel2.add(jPanel3, java.awt.BorderLayout.NORTH);

        m_jRootCategories.add(jPanel2, java.awt.BorderLayout.PAGE_END);

        jPanel6.add(m_jRootCategories, java.awt.BorderLayout.CENTER);

        m_jCategories.setBackground(new java.awt.Color(24, 26, 30));
        m_jCategories.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jCategories.setMaximumSize(new java.awt.Dimension(275, 600));
        m_jCategories.setPreferredSize(new java.awt.Dimension(265, 160));
        m_jCategories.setLayout(new java.awt.CardLayout());

        m_jListTopCategories.setBackground(new java.awt.Color(24, 26, 30));
        m_jCategories.add(m_jListTopCategories, "card4");

        m_jSubCategories.setBackground(new java.awt.Color(24, 26, 30));
        m_jSubCategories.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.BorderLayout());

        m_lblIndicator.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_lblIndicator.setText("jLabel1");
        jPanel4.add(m_lblIndicator, java.awt.BorderLayout.NORTH);

        m_jSubCategories.add(jPanel4, java.awt.BorderLayout.WEST);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanel5.setLayout(new java.awt.GridLayout(0, 1, 0, 5));

        m_btnBack1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/2uparrow.png"))); // NOI18N
        m_btnBack1.setFocusPainted(false);
        m_btnBack1.setFocusable(false);
        m_btnBack1.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_btnBack1.setPreferredSize(new java.awt.Dimension(60, 45));
        m_btnBack1.setRequestFocusEnabled(false);
        m_btnBack1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnBack1ActionPerformed(evt);
            }
        });
        jPanel5.add(m_btnBack1);

        jPanel1.add(jPanel5, java.awt.BorderLayout.NORTH);

        m_jSubCategories.add(jPanel1, java.awt.BorderLayout.LINE_END);

        m_jCategories.add(m_jSubCategories, "subcategories");

        jPanel6.add(m_jCategories, java.awt.BorderLayout.CENTER);

        m_jSCategories.setBackground(new java.awt.Color(24, 26, 30));
        jPanel6.add(m_jSCategories, java.awt.BorderLayout.SOUTH);

        add(jPanel6, java.awt.BorderLayout.PAGE_START);

        jPanel9.setBackground(new java.awt.Color(24, 26, 30));
        jPanel9.setLayout(new java.awt.BorderLayout());

        m_jProducts.setBackground(new java.awt.Color(24, 26, 30));
        m_jProducts.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jProducts.setPreferredSize(new java.awt.Dimension(500, 300));
        m_jProducts.setLayout(new java.awt.CardLayout());
        jPanel9.add(m_jProducts, java.awt.BorderLayout.CENTER);

        add(jPanel9, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jListCategoriesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_m_jListCategoriesValueChanged

        if (!evt.getValueIsAdjusting()) {
            CategoryInfo cat = (CategoryInfo) m_jListCategories.getSelectedValue();
            if (cat != null) {
                selectCategoryPanel(cat.getID(), false);
            }
        }
        
    }//GEN-LAST:event_m_jListCategoriesValueChanged

    private void m_btnBack1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnBack1ActionPerformed

        showRootCategoriesPanel();

    }//GEN-LAST:event_m_btnBack1ActionPerformed

    private void m_jSeat1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jSeat1ActionPerformed
        TicketInfo.ticketlineSeat = 1;
        m_jLblSeat.setText("Seat #1");
    }//GEN-LAST:event_m_jSeat1ActionPerformed

    private void m_jSeat2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jSeat2ActionPerformed
        TicketInfo.ticketlineSeat = 2;
        m_jLblSeat.setText("Seat #2");
    }//GEN-LAST:event_m_jSeat2ActionPerformed

    private void m_jSeat3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jSeat3ActionPerformed
        TicketInfo.ticketlineSeat = 3;
        m_jLblSeat.setText("Seat #3");
    }//GEN-LAST:event_m_jSeat3ActionPerformed

    private void m_jSeat4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jSeat4ActionPerformed
        TicketInfo.ticketlineSeat = 4;
        m_jLblSeat.setText("Seat #4");
    }//GEN-LAST:event_m_jSeat4ActionPerformed

    private void loadTopCategories(java.util.List<CategoryInfo> categories){
        m_jListTopCategories.removeAll();
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
            btn.setPreferredSize(new Dimension(116,60));
            //btn.setFont(new java.awt.Font("Tahoma", 0, 12));
            btn.addActionListener(new SelectedTopCategory(cat));
            btn.setBackground(new Color(34,36,40));
            btn.setName("cat_" + cat.getID());
            //btn.addActionListener(al);
            m_jListTopCategories.add(btn);
        }
    }
    private boolean hasModifier(ProductInfoExt prod) throws BasicException{
        List<AttributeInfo> attributes = m_dlSales.getProductModifiers(prod.getAttributeSetID());
        return !attributes.isEmpty();
    }

    public void showAttribute(JCatalogTab jTab, String attributeId, boolean show){
        Component[] components = jTab.flowpanel.getComponents();
        for (Component component : components){
            if(component.getName()!= null && component.getName().contains("modifier")){
                String[] names = component.getName().split("_");
                if (names[1].equals("d") && names[2].equals(attributeId)){
                    component.setVisible(show);
                }
            }
        }
    }
    public void showEditModifier(String attSetId, String attInstanceId, ActionListener ol) throws BasicException{
        removeModifier(m_jcurrTab);
        SentenceList attsetinstSent = new PreparedSentence(s,
            "SELECT A.ATTRIBUTE_ID, A.VALUE, B.PRICE FROM attributeinstance A LEFT JOIN attributevalue B ON A.ATTRIBUTE_ID = B.ATTRIBUTE_ID AND A.VALUE = B.VALUE WHERE A.ATTRIBUTESETINSTANCE_ID = ?",
            SerializerWriteString.INSTANCE,
            new SerializerRead() {
              @Override
              public Object readValues(DataRead dr) throws BasicException {
                return new AttributeInstInfo(dr.getString(1), dr.getString(2), dr.getDouble(3));
              }
            });
        List<AttributeInstInfo> attinstinfo = attsetinstSent.list(attInstanceId);
        m_modifier = new ArrayList();
        for(AttributeInstInfo att:attinstinfo){
            String modifier = att.getAttid() + "_" + att.getValue() + "_" + att.getPrice();
            m_modifier.add(modifier);
        }
        List<AttributeInfo> attributes = m_dlSales.getProductModifiers(attSetId);
        for (AttributeInfo attribute : attributes) {
            // Write Section Header
            JLabel headerLbl = new JLabel();
            headerLbl.setText(attribute.toString());
            JPanel header = new JPanel();
            header.add(headerLbl);
            header.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10));
            header.setPreferredSize(new Dimension(600,30));
            header.setName( "modifier_h_" + attribute.getId());
            m_jcurrTab.flowpanel.add(header);
            List<AttributeValue> attributevalues = m_dlSales.getAttributeValues(attribute.getId());
            for (AttributeValue attributevalue : attributevalues) {
                JButton btn = new JButton();
                btn.applyComponentOrientation(getComponentOrientation());
                String btnText = "";
                if(attributevalue.getPrice()>0){
                    btnText = "<html><center>" + attributevalue.getValue() + "<br>$" + attributevalue.getPrice();
                }else{
                    btnText = "<html><center>" + attributevalue.getValue();
                }
                btn.setText(btnText);
                btn.setFocusPainted(false);
                btn.setFocusable(false);
                btn.setRequestFocusEnabled(false);
                btn.setHorizontalTextPosition(SwingConstants.CENTER);
                btn.setVerticalTextPosition(SwingConstants.BOTTOM);
                btn.setMargin(new Insets(2, 2, 2, 2));
                btn.setPreferredSize(new Dimension(80, 50));
                btn.setName("modifier_d_" + attribute.getId() + "_" + attributevalue.getValue());
                btn.addActionListener(new SelectedModifier(attribute.getId() + "_" + attributevalue.getValue() + "_" + attributevalue.getPrice()));
                for(AttributeInstInfo att:attinstinfo){
                    if(att.getAttid().equals(attribute.getId())&& att.getValue().equals(attributevalue.getValue())){
                        btn.setForeground(Color.WHITE);
                        btn.setBackground(Color.BLUE);
                    }else{
                        btn.setBackground(new Color(167,115,255));
                    }
                }

                m_jcurrTab.flowpanel.add(btn);
            }
        }
        // Add Update button
        JButton btn = new JButton();
        btn.applyComponentOrientation(getComponentOrientation());
        btn.setText("Update");
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setRequestFocusEnabled(false);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setPreferredSize(new Dimension(116, 60));
        btn.setName("modifier_d_add");
        btn.addActionListener(ol);
        JPanel footer = new JPanel();
        footer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 10));
        footer.setPreferredSize(new Dimension(600,80));
        footer.setName( "modifier_f_");
        footer.add(btn);
        m_jcurrTab.flowpanel.add(footer);
    }
    public JFlowPanel showModifiertoMod(String attSetId, String attInstanceId) throws BasicException{
        m_flowPanel = new JFlowPanel();
        SentenceList attsetinstSent = new PreparedSentence(s,
            "SELECT A.ATTRIBUTE_ID, A.VALUE, B.PRICE FROM attributeinstance A LEFT JOIN attributevalue B ON A.ATTRIBUTE_ID = B.ATTRIBUTE_ID AND A.VALUE = B.VALUE WHERE A.ATTRIBUTESETINSTANCE_ID = ?",
            SerializerWriteString.INSTANCE,
            new SerializerRead() {
              @Override
              public Object readValues(DataRead dr) throws BasicException {
                return new AttributeInstInfo(dr.getString(1), dr.getString(2), dr.getDouble(3));
              }
            });

        List<AttributeInstInfo> attinstinfo = attsetinstSent.list(attInstanceId);
        m_modifierForMod = new ArrayList();
        for(AttributeInstInfo att:attinstinfo){
            String modifier = att.getAttid() + "_" + att.getValue() + "_" + att.getPrice();
            m_modifierForMod.add(modifier);
        }
        List<AttributeInfo> attributes = m_dlSales.getProductModifiers(attSetId);
        for (AttributeInfo attribute : attributes) {
            // Write Section Header
            JLabel headerLbl = new JLabel();
            headerLbl.setText(attribute.toString());
            JPanel header = new JPanel();
            header.add(headerLbl);
            header.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10));
            header.setPreferredSize(new Dimension(870,30));
            header.setName( "modifier_h_" + attribute.getId());
            m_flowPanel.add(header);
            List<AttributeValue> attributevalues = m_dlSales.getAttributeValues(attribute.getId());
            for (AttributeValue attributevalue : attributevalues) {
                JButton btn = new JButton();
                btn.applyComponentOrientation(getComponentOrientation());
                String btnText = "";
                if(attributevalue.getPrice()>0){
                    btnText = "<html><center>" + attributevalue.getValue() + "<br>$" + attributevalue.getPrice();
                }else{
                    btnText = "<html><center>" + attributevalue.getValue();
                }
                btn.setText(btnText);
                btn.setFocusPainted(false);
                btn.setFocusable(false);
                btn.setRequestFocusEnabled(false);
                btn.setHorizontalTextPosition(SwingConstants.CENTER);
                btn.setVerticalTextPosition(SwingConstants.BOTTOM);
                btn.setMargin(new Insets(2, 2, 2, 2));
                btn.setPreferredSize(new Dimension(80, 50));
                btn.setName("modifier_d_mod_" + attribute.getId() + "_" + attributevalue.getValue());
                btn.addActionListener(new SelectedModifierForMod(attribute.getId() + "_" + attributevalue.getValue() + "_" + attributevalue.getPrice()));
                for(AttributeInstInfo att:attinstinfo){
                    btn.setForeground(Color.WHITE);
                    btn.setBackground(btn.getForeground()); 
                }
                for(AttributeInstInfo att:attinstinfo){
                    if(att.getAttid().equals(attribute.getId()) && att.getValue().equals(attributevalue.getValue())){
                        btn.setForeground(Color.WHITE);
                        btn.setBackground(Color.BLUE);
                    }
                }

                m_flowPanel.add(btn);
            }
        }

        JPanel footer = new JPanel();
        footer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 10));
        footer.setPreferredSize(new Dimension(600,80));
        footer.setName( "modifier_f_");
        m_flowPanel.add(footer);
        return m_flowPanel;
    }

    public UpdateTicket getUpdateActionListener(ProductInfoExt prod) {
        UpdateTicket itemActionListener = new UpdateTicket(prod);
        return itemActionListener;
    }
    public void removeModifier(JCatalogTab jTab){
        //Remove all modifier
        m_modifier = new ArrayList();
        Component[] components = jTab.flowpanel.getComponents();
        for (Component component : components){
            if(component.getName()!= null && component.getName().contains("modifier")){
                m_jcurrTab.flowpanel.remove(component);
            }
        }
    }

    private void addToTicket(ProductInfoExt prod, JCatalogTab jTab){
        // Input quantity
        Integer iValue = 1;
        if(iValue != null){
            TicketInfo.ticketlineQuantity = Double.valueOf(iValue);
            // Calculate Extra Price
            List<Modifier> modifiers = new ArrayList();
            m_modifierForMod.clear();
            for(String modifier:m_modifierForMod){
                String[] values = modifier.split("_");
                Modifier item = new Modifier();
                item.setId(values[0]);
                item.setValue(values[1]);
                item.setPrice(Double.valueOf(values[2]));
                modifiers.add(item);
            }
            processModifier(prod.getAttributeSetID());
            prod.setAttSetInstId(attInstanceId);
            prod.setAttSetInstDesc(attInstanceDescription);

            prod.setModifiers(modifiers);
            removeModifier(jTab);
            fireSelectedProduct(prod);
        }
    }

    private void updateTicket(ProductInfoExt prod){
        // Input quantity
        Integer iValue = 1;
        if(iValue !=null){
            TicketInfo.ticketlineQuantity = Double.valueOf(iValue);
            // Calculate Extra Price
            List<Modifier> modifiers = new ArrayList();
            for(String modifier:m_modifierForMod){
                String[] values = modifier.split("_");
                Modifier item = new Modifier();
                item.setId(values[0]);
                item.setValue(values[1]);
                item.setPrice(Double.valueOf(values[2]));
                modifiers.add(item);
            }

            if (prod.isVerpatrib() && modifiers.size() == 0) {
                JFrame frame = new JFrame("Swing Dialog");
                JOptionPane.showMessageDialog(frame, "Mandatory attributes!");
                return;
            }

            processModifier(prod.getAttributeSetID());
            prod.setAttSetInstId(attInstanceId);
            prod.setAttSetInstDesc(attInstanceDescription);
            prod.setModifiers(modifiers);
            updateSelectedProduct(prod);
        }
    }

    private void selectProduct(ProductInfoExt prod){
        Integer iValue = 1;
        if(iValue !=null){
            TicketInfo.ticketlineQuantity = Double.valueOf(iValue);
            fireSelectedProduct(prod);
        }
    }

    public void processModifier(String attsetid){
        SentenceFind attsetinstExistsSent = new PreparedSentence(s,
            "SELECT ID, DESCRIPTION FROM attributesetinstance WHERE ATTRIBUTESET_ID = ? AND DESCRIPTION = ?",
            new SerializerWriteBasic(Datas.STRING, Datas.STRING),
            SerializerReadString.INSTANCE);
        SentenceExec attsetSave = new PreparedSentence(s,
            "INSERT INTO attributesetinstance (ID, ATTRIBUTESET_ID, DESCRIPTION) VALUES (?, ?, ?)",
            new SerializerWriteBasic(Datas.STRING, Datas.STRING, Datas.STRING));
        SentenceExec attinstSave = new PreparedSentence(s,
            "INSERT INTO attributeinstance(ID, ATTRIBUTESETINSTANCE_ID, ATTRIBUTE_ID, VALUE) VALUES (?, ?, ?, ?)",
            new SerializerWriteBasic(Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING));
        StringBuilder description = new StringBuilder();
        for(String modifier:m_modifierForMod){
            String[] values = modifier.split("_");
            if (description.length() > 0) {
              description.append("|");
            }
            description.append(values[1]);
        }
        String id;

        if (description.length() == 0) {
          id = null;
        } else {

          try {

            id = (String) attsetinstExistsSent.find(attsetid, description.toString());

          } catch (BasicException ex) {
            return;
          }

          if (id == null) {
            id = UUID.randomUUID().toString();
            try {
                attsetSave.exec(id, attsetid, description.toString());
                for(String modifier:m_modifierForMod){
                    String[] values = modifier.split("_");
                    attinstSave.exec(UUID.randomUUID().toString(), id, values[0], values[1]);
                }
            } catch (BasicException ex) {
              return;
            }
          }
        }
        attInstanceId = id;
        attInstanceDescription = description.toString();
    }
    private void setFocusCat(String catId){
        Component[] components = m_jListTopCategories.getComponents();
        for (Component component : components){
            if(component.getName()!= null && component.getName().equals("cat_" + catId)){
                component.setBackground(Color.BLUE);                
            }
            if(component.getName()!= null && component.getName().equals("cat_" + currentCatId) && !currentCatId.equals(catId)){
                component.setBackground(new Color(34,36,40));                
            }
        }
        currentCatId = catId;
        currentSubCatId = "";
    }
    private void setFocusSubCat(String catId){
        Component[] components = m_jSCategories.getComponents();
        for (Component component : components){
            if(component.getName()!= null && component.getName().equals("cat_" + catId)){
                component.setBackground(Color.BLUE);                
            }
            if(component.getName()!= null && component.getName().equals("cat_" + currentSubCatId) && !currentSubCatId.equals(catId)){
                component.setBackground(new Color(3, 23, 67));                
            }
        }
        currentSubCatId = catId;
    }
    public void setSession(Session ds){
        s = ds;
    }
    public void enabledButton(boolean enabled){
        jPanel6.setVisible(enabled);
        jPanel9.setVisible(enabled);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JButton m_btnBack1;
    private javax.swing.JPanel m_jCategories;
    private javax.swing.JLabel m_jLblSeat;
    private javax.swing.JList m_jListCategories;
    private com.openbravo.beans.JFlowPanel m_jListTopCategories;
    private javax.swing.JPanel m_jProducts;
    private javax.swing.JPanel m_jRootCategories;
    private com.openbravo.beans.JFlowPanel m_jSCategories;
    private javax.swing.JButton m_jSeat1;
    private javax.swing.JButton m_jSeat2;
    private javax.swing.JButton m_jSeat3;
    private javax.swing.JButton m_jSeat4;
    private javax.swing.JPanel m_jSubCategories;
    private javax.swing.JScrollPane m_jscrollcat;
    private javax.swing.JLabel m_lblIndicator;
    private javax.swing.JPanel seatPanel;
    // End of variables declaration//GEN-END:variables
    
}

