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

package com.openbravo.pos.sales.shared;


import com.openbravo.pos.sales.SharedTicketInfo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.panels.SharedTicketsModel;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.restaurant.JTicketsBagRestaurantMap;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author JG uniCenta
 */
public class JTicketsBagSharedList extends javax.swing.JDialog {
    
    private String m_sDialogTicket;
    private AppView m_App;  
    private DataLogicSystem m_dlSystem;
    
    private SharedTicketsModel m_SharedTickets = null;   
    
    private TicketParser m_TTP;
    private final DateFormat df= new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");   
    
    private Session s;
    private Connection con;  
    private Statement stmt;
    private Integer result;
    private Integer dresult;    
    private String SQL;
    private ResultSet rs;
    
    private AppUser m_User;
    
    private ComboBoxValModel m_ReasonModel;  
    java.util.List<SharedTicketsModel.PaymentsLine> paymentLines;

    private JTicketsBagRestaurantMap m_restaurantmap;

    private int sortColumn = 0;
    private boolean sortDirection = true;
    private String mergeID = "";
    
    /** Creates new form JTicketsBagSharedList */
    private JTicketsBagSharedList(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }
    /** Creates new form JTicketsBagSharedList */
    private JTicketsBagSharedList(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }

    private void loadData(int column) throws BasicException {

        m_dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");

        m_jTicketTable.setModel(new DefaultTableModel());

        JTableHeader header = m_jTicketTable.getTableHeader();
        header.setPreferredSize(new Dimension(100, 30));
            
        // LoadData
        m_SharedTickets = SharedTicketsModel.loadInstance(m_App, mergeID);
        paymentLines = m_SharedTickets.getPaymentLines();
        
        if (m_SharedTickets.getPayments() != 0 
                || m_SharedTickets.getSales() != 0) {
        }

        m_jTicketTable.setModel(m_SharedTickets.getPaymentsModel());

        m_jSelectRow.setText("  Select");
        m_jButtonCancel.setText("  Cancel");

        Font font = new Font("Arial", Font.PLAIN, 12);
        m_jTicketTable.setFont(font);

        m_jTicketTable.setDefaultRenderer(Object.class, new PaddingTableCellRenderer());
        m_jTicketTable.getTableHeader().addMouseListener(new TableHeaderClickListener(m_jTicketTable));
    }

    public class TableHeaderClickListener extends MouseAdapter {
        private JTable table;

        public TableHeaderClickListener(JTable table) {
            this.table = table;
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            int column = table.columnAtPoint(event.getPoint());

            if (sortColumn == column) sortDirection = !sortDirection;
            else sortDirection = true;

            m_SharedTickets.sortTableByColumn(column, sortDirection);
            sortColumn = column;
        }
    }

    static class PaddingTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            // Get the default cell renderer component
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Set the padding
            int padding = 3; // Set your desired padding value here
            if (c instanceof JComponent) {
                ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
            }

            return c;
        }
    }

    /**
     *
     * @param atickets
     * @param dlReceipts
     * @param app
     * @return
     */
    public String showTicketsList(java.util.List<SharedTicketInfo> atickets, DataLogicReceipts dlReceipts, AppView app, String merge) {
// JG Dec 2014
        m_App = app;
        mergeID = merge;

        try {
            loadData(0);
        } catch (BasicException e) {}

        this.m_jTicketTable.setRowHeight(30);
        setVisible(true);

        return m_sDialogTicket;
    }
    
    /**
     *
     * @param 
     * @return
     */

    public static JTicketsBagSharedList newJDialog(Window window) {        
        JTicketsBagSharedList mydialog;
        if (window instanceof Frame) { 
            mydialog = new JTicketsBagSharedList((Frame) window, true);
        } else {
            mydialog = new JTicketsBagSharedList((Dialog) window, true);
        } 
 
        mydialog.initComponents();
        mydialog.m_jTicketTable.setRowHeight(30);
       
        return mydialog;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        m_jScrollTableTicket = new javax.swing.JScrollPane();
        m_jTicketTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jButtonCancel = new javax.swing.JButton();
        m_jSelectRow = new javax.swing.JButton();

        setTitle(AppLocal.getIntString("caption.tickets")); // NOI18N
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());

        m_jScrollTableTicket.setBorder(null);
        m_jScrollTableTicket.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        m_jScrollTableTicket.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jScrollTableTicket.setMinimumSize(new java.awt.Dimension(350, 140));
        m_jScrollTableTicket.setPreferredSize(new java.awt.Dimension(325, 150));

        m_jTicketTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jTicketTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        m_jTicketTable.setFocusable(false);
        m_jTicketTable.setRowHeight(30);
        m_jTicketTable.setIntercellSpacing(new java.awt.Dimension(0, 1));
        m_jTicketTable.setRequestFocusEnabled(false);
        m_jTicketTable.setShowVerticalLines(false);
        m_jScrollTableTicket.setViewportView(m_jTicketTable);

        jPanel1.add(m_jScrollTableTicket, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        jPanel3.add(jPanel4);

        m_jButtonCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        m_jButtonCancel.setText(AppLocal.getIntString("button.close")); // NOI18N
        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setFocusable(false);
        m_jButtonCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonCancel.setPreferredSize(new java.awt.Dimension(120, 45));
        m_jButtonCancel.setRequestFocusEnabled(false);
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel3.add(m_jButtonCancel);

        m_jSelectRow.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jSelectRow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        m_jSelectRow.setText(AppLocal.getIntString("button.closecash")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        m_jSelectRow.setToolTipText(bundle.getString("tooltip.btn.closecash")); // NOI18N
        m_jSelectRow.setIconTextGap(2);
        m_jSelectRow.setInheritsPopupMenu(true);
        m_jSelectRow.setMaximumSize(new java.awt.Dimension(85, 33));
        m_jSelectRow.setMinimumSize(new java.awt.Dimension(85, 33));
        m_jSelectRow.setPreferredSize(new java.awt.Dimension(110, 45));
        m_jSelectRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jSelectRowActionPerformed(evt);
            }
        });
        jPanel3.add(m_jSelectRow);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        setSize(new java.awt.Dimension(884, 715));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed
        dispose();        
    }//GEN-LAST:event_m_jButtonCancelActionPerformed

    private void m_jSelectRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jSelectRowActionPerformed
        // TODO add your handling code here:
        int selectedRow = m_jTicketTable.getSelectedRow();
        if(selectedRow >= 0) {
            m_sDialogTicket = paymentLines.get(selectedRow).getTicketID();
            
            try {
                loadData(0);
            } catch (BasicException e) {}
        }

        dispose();
    }//GEN-LAST:event_m_jSelectRowActionPerformed
       
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JScrollPane m_jScrollTableTicket;
    private javax.swing.JButton m_jSelectRow;
    private javax.swing.JTable m_jTicketTable;
    // End of variables declaration//GEN-END:variables
    
}
