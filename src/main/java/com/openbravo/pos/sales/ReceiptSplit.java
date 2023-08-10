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

package com.openbravo.pos.sales;

import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.GridBagConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ScrollPaneLayout;


/**
 *
 * @author  adrianromero
 */
public class ReceiptSplit extends javax.swing.JDialog {
    
    private boolean accepted;
    private DataLogicSales m_dlSales;
    private DataLogicCustomers m_dlCustomers;
    private TaxesLogic m_taxeslogic;

    SimpleReceipt[] receipts = new SimpleReceipt[10];

    GridBagConstraints gridConstraints = new java.awt.GridBagConstraints();;
    
    /** Creates new form ReceiptSplit
     * @param parent */
    protected ReceiptSplit(java.awt.Frame parent) {
        super(parent, true);
    }
    /** Creates new form ReceiptSplit
     * @param parent */
    protected ReceiptSplit(java.awt.Dialog parent) {
        super(parent, true);
    } 
    
    private void init(String ticketline, DataLogicSales dlSales, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic) {
        
        initComponents();        
        getRootPane().setDefaultButton(m_jButtonOK);

        m_dlSales = dlSales;
        m_dlCustomers = dlCustomers;
        m_taxeslogic = taxeslogic;

        for (int i=0; i<10; i++) {
            receipts[i] = new SimpleReceipt(ticketline, dlSales, dlCustomers, taxeslogic);
        }
        receipts[0].setCustomerEnabled(false);
        jScrollPane1.setAutoscrolls(true);

        rePaint(true);
    }

    private void rePaint(boolean flag) {
        jPanel3.removeAll();

        int theEnd = 9;
        if (!flag)
            for (int i=9; i>=0; i--) {
                if (receipts[i].getTicket().getLinesCount() != 0) {
                    theEnd = i;
                    break;  
                }
            }
        else theEnd = 0;

        for (int i=0; i<=theEnd; i++) {
            JPanel m_ticket = makeSplitGroup(receipts[i], i);
            m_ticket.setSize(new Dimension(375, 445));

            jPanel3.add(m_ticket);
        }
    }

    public JPanel makeSplitGroup (SimpleReceipt simpleReceipt, int index) {

        JPanel m_jPanelTemp = new JPanel();
        GridBagConstraints gridBagConstraints;
        JPanel m_jPanelButtons = new JPanel();

        JButton jBtnSplit = new javax.swing.JButton();
        JButton jBtnToRightAll = new javax.swing.JButton();
        JButton jBtnToRightOne = new javax.swing.JButton();
        JButton jBtnToLeftOne = new javax.swing.JButton();
        JButton jBtnToLeftAll = new javax.swing.JButton();

        m_jPanelButtons.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPanelButtons.setLayout(new java.awt.GridBagLayout());

        jBtnSplit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_split_sml.png"))); // NOI18N
        jBtnSplit.setToolTipText("Split item to multi-item");
        jBtnSplit.setFocusPainted(false);
        jBtnSplit.setFocusable(false);
        jBtnSplit.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jBtnSplit.setPreferredSize(new java.awt.Dimension(45, 45));
        jBtnSplit.setRequestFocusEnabled(false);
        jBtnSplit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSplitItemActionPerformed(evt, index);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 0;
        m_jPanelButtons.add(jBtnSplit, gridBagConstraints);

        jBtnToRightAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/2rightarrow.png"))); // NOI18N
        jBtnToRightAll.setToolTipText("Split All Line Items");
        jBtnToRightAll.setFocusPainted(false);
        jBtnToRightAll.setFocusable(false);
        jBtnToRightAll.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jBtnToRightAll.setPreferredSize(new java.awt.Dimension(45, 45));
        jBtnToRightAll.setRequestFocusEnabled(false);
        jBtnToRightAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToRightAllActionPerformed(evt, index);
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        m_jPanelButtons.add(jBtnToRightAll, gridBagConstraints);

        jBtnToRightOne.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/1rightarrow.png"))); // NOI18N
        jBtnToRightOne.setToolTipText("Split only one of the Line Items");
        jBtnToRightOne.setFocusPainted(false);
        jBtnToRightOne.setFocusable(false);
        jBtnToRightOne.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jBtnToRightOne.setPreferredSize(new java.awt.Dimension(45, 45));
        jBtnToRightOne.setRequestFocusEnabled(false);
        jBtnToRightOne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToRightOneActionPerformed(evt, index);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        m_jPanelButtons.add(jBtnToRightOne, gridBagConstraints);

        jBtnToLeftOne.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/1leftarrow.png"))); // NOI18N
        jBtnToLeftOne.setToolTipText("Un-Split only one of the Line Items");
        jBtnToLeftOne.setFocusPainted(false);
        jBtnToLeftOne.setFocusable(false);
        jBtnToLeftOne.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jBtnToLeftOne.setPreferredSize(new java.awt.Dimension(45, 45));
        jBtnToLeftOne.setRequestFocusEnabled(false);
        jBtnToLeftOne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToLeftOneActionPerformed(evt, index);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        m_jPanelButtons.add(jBtnToLeftOne, gridBagConstraints);

        jBtnToLeftAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/2leftarrow.png"))); // NOI18N
        jBtnToLeftAll.setToolTipText("Un-Split All Line Items");
        jBtnToLeftAll.setFocusPainted(false);
        jBtnToLeftAll.setFocusable(false);
        jBtnToLeftAll.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jBtnToLeftAll.setPreferredSize(new java.awt.Dimension(45, 45));
        jBtnToLeftAll.setRequestFocusEnabled(false);
        jBtnToLeftAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToLeftAllActionPerformed(evt, index);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        m_jPanelButtons.add(jBtnToLeftAll, gridBagConstraints);

        simpleReceipt.setPreferredSize(new Dimension(320, 445));
        m_jPanelTemp.add(simpleReceipt); 

        if (index < 9)
            m_jPanelTemp.add(m_jPanelButtons, BorderLayout.SOUTH);

        return m_jPanelTemp;
    }


    private void jBtnSplitItemActionPerformed(java.awt.event.ActionEvent evt, int index) {
        TicketLineInfo[] lines = receipts[index].getSelectedLines();
        if(lines != null) {
            int size = (int) lines[0].getMultiply();

            if(size > 1) {
                for(int i = 0; i < size; i++) {
                    TicketLineInfo[] temp = new TicketLineInfo[1];
                    TicketLineInfo lineItem = lines[0];
                    lineItem.setMultiply(1);

                    temp[0] = lineItem;
                    receipts[index].addSelectedLines(temp);
                }
            } else {
                TicketLineInfo[] temp = new TicketLineInfo[1];
                TicketLineInfo lineItem = lines[0];
                lineItem.setMultiply(0.5);

                temp[0] = lineItem;
                receipts[index].addSelectedLines(temp);
                receipts[index].addSelectedLines(temp);
            }
        }
    }

    private void jBtnToRightAllActionPerformed(java.awt.event.ActionEvent evt, int index) {
        int lineSize = receipts[index].getTicket().getLinesCount();

        if(lineSize > 0 && index < 9)
            for (int i=0; i<lineSize; i++) {
                TicketLineInfo[] lines = receipts[index].getLinesByIndex(0);
                receipts[index + 1].addSelectedLines(lines);
            }

        rePaint(false);
    }

    private void jBtnToRightOneActionPerformed(java.awt.event.ActionEvent evt, int index) {
        int lineSize = receipts[index].getTicket().getLinesCount();
        if (lineSize > 0 && index <9) {
            TicketLineInfo[] lines = receipts[index].getSelectedLines();
            if (lines != null) {
                receipts[index + 1].addSelectedLines(lines);
            }

            rePaint(false);
        }
    }

    private void jBtnToLeftOneActionPerformed(java.awt.event.ActionEvent evt, int index) {
        
        if (index <9) {
            int lineSize = receipts[index + 1].getTicket().getLinesCount();
            
            if (lineSize > 0) {
                TicketLineInfo[] lines = receipts[index + 1].getSelectedLines();
                if (lines != null) {
                    receipts[index].addSelectedLines(lines);
                }

                rePaint(false);  
            }
        }
    }

    private void jBtnToLeftAllActionPerformed(java.awt.event.ActionEvent evt, int index) {
        if (index <9) {
            int lineSize = receipts[index + 1].getTicket().getLinesCount();

            if (lineSize > 0) {
                for (int i=0; i<lineSize; i++) {
                    TicketLineInfo[] lines = receipts[index + 1].getLinesByIndex(0);
                    receipts[index].addSelectedLines(lines);
                }

                rePaint(false);
            }   
        }
    }
    
    /**
     *
     * @param parent
     * @param ticketline
     * @param dlSales
     * @param dlCustomers
     * @param taxeslogic
     * @return
     */
    public static ReceiptSplit getDialog(Component parent, String ticketline, DataLogicSales dlSales, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic) {

        Window window = getWindow(parent);
        
        ReceiptSplit myreceiptsplit;
        
        if (window instanceof Frame) { 
            myreceiptsplit = new ReceiptSplit((Frame) window);
        } else {
            myreceiptsplit = new ReceiptSplit((Dialog) window);
        }
        
        myreceiptsplit.init(ticketline, dlSales, dlCustomers, taxeslogic);         
        
        return myreceiptsplit;
    }

    /**
     *
     * @param parent
     * @return
     */
    protected static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window)parent;
        } else {
            return getWindow(parent.getParent());
        }
    }
    
    /**
     *
     * @param ticket
     * @param ticket2
     * @param ticketext
     * @return
     */
    public SimpleReceipt[] showDialog(TicketInfo ticket, Object ticketext) {
        receipts[0].setTicket(ticket, ticketext);

        for (int i=1; i<10; i++) {
            TicketInfo newTicketInfo = new TicketInfo();
            newTicketInfo.setName(ticket.getName());

            receipts[i].setTicket(newTicketInfo, ticketext + "[" + i + "]");
        }
        
        setVisible(true);    
        if (accepted) return receipts;
        else return null;
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        m_jButtonCancel = new javax.swing.JButton();
        m_jButtonOK = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        m_jtickets = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("caption.split")); // NOI18N
        setResizable(false);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        m_jButtonCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        m_jButtonCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setFocusable(false);
        m_jButtonCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonCancel.setPreferredSize(new java.awt.Dimension(110, 45));
        m_jButtonCancel.setRequestFocusEnabled(false);
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonCancel);

        m_jButtonOK.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        m_jButtonOK.setText(AppLocal.getIntString("button.OK")); // NOI18N
        m_jButtonOK.setFocusPainted(false);
        m_jButtonOK.setFocusable(false);
        m_jButtonOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonOK.setPreferredSize(new java.awt.Dimension(110, 45));
        m_jButtonOK.setRequestFocusEnabled(false);
        m_jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonOKActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonOK);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        m_jtickets.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jtickets.setLayout(new java.awt.GridLayout(0, 1, 5, 5));
        jPanel3.add(m_jtickets);

        jScrollPane1.setViewportView(jPanel3);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        setSize(new java.awt.Dimension(1016, 586));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonOKActionPerformed
        accepted = true;
        dispose();

    }//GEN-LAST:event_m_jButtonOKActionPerformed

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed
        
        dispose();
        
    }//GEN-LAST:event_m_jButtonCancelActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JButton m_jButtonOK;
    private javax.swing.JPanel m_jtickets;
    // End of variables declaration//GEN-END:variables
    
}
