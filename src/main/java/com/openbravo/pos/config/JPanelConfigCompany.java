//    Roxy Pos  - Touch Friendly Point Of Sale
//    Copyright © 2009-2020 uniCenta
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

package com.openbravo.pos.config;

import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import java.awt.Component;
import javax.swing.ImageIcon;

/**
 *
 * @author JG uniCenta
 */
public class JPanelConfigCompany extends javax.swing.JPanel implements PanelConfig {
    
    private final DirtyManager dirty = new DirtyManager();


    /**
     *
     */
    public JPanelConfigCompany() {
        
        initComponents();
                          
        jtxtTktHeader1.getDocument().addDocumentListener(dirty);
        jtxtTktHeader2.getDocument().addDocumentListener(dirty);
        jtxtTktHeader3.getDocument().addDocumentListener(dirty);
        jtxtTktHeader4.getDocument().addDocumentListener(dirty);
        jtxtTktHeader5.getDocument().addDocumentListener(dirty);
        jtxtTktHeader6.getDocument().addDocumentListener(dirty);

        jtxtTktFooter1.getDocument().addDocumentListener(dirty);
        jtxtTktFooter2.getDocument().addDocumentListener(dirty);
        jtxtTktFooter3.getDocument().addDocumentListener(dirty);
        jtxtTktFooter4.getDocument().addDocumentListener(dirty);
        jtxtTktFooter5.getDocument().addDocumentListener(dirty);
        jtxtTktFooter6.getDocument().addDocumentListener(dirty);
        
// JG - For future
        lblLogo.setVisible(false);
        jLbllogoPath.setVisible(false);
        webSwtch_Logo.setVisible(false);
        
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasChanged() {
        return dirty.isDirty();
    }
    
    /**
     *
     * @return
     */
    @Override
    public Component getConfigComponent() {
        return this;
    }
   
    /**
     *
     * @param config
     */
    @Override
    public void loadProperties(AppConfig config) {

        jtxtTktHeader1.setText(config.getProperty("tkt.header1"));
        jtxtTktHeader2.setText(config.getProperty("tkt.header2"));
        jtxtTktHeader3.setText(config.getProperty("tkt.header3"));  
        jtxtTktHeader4.setText(config.getProperty("tkt.header4"));  
        jtxtTktHeader5.setText(config.getProperty("tkt.header5"));  
        jtxtTktHeader6.setText(config.getProperty("tkt.header6"));  
        
        jtxtTktFooter1.setText(config.getProperty("tkt.footer1"));
        jtxtTktFooter2.setText(config.getProperty("tkt.footer2"));
        jtxtTktFooter3.setText(config.getProperty("tkt.footer3"));  
        jtxtTktFooter4.setText(config.getProperty("tkt.footer4"));  
        jtxtTktFooter5.setText(config.getProperty("tkt.footer5"));  
        jtxtTktFooter6.setText(config.getProperty("tkt.footer6"));  

/** JG - here for future per terminal
        jLbllogoPath.setText(config.getProperty("tkt.logopath"));
        ImageIcon image = new ImageIcon(jLbllogoPath.getText());        
        jLogo.setIcon(image);        
*/        
        dirty.setDirty(false);        
    }
   
    /**
     *
     * @param config
     */
    @Override
    public void saveProperties(AppConfig config) {
        
        config.setProperty("tkt.header1", jtxtTktHeader1.getText());
        config.setProperty("tkt.header2", jtxtTktHeader2.getText()); 
        config.setProperty("tkt.header3", jtxtTktHeader3.getText()); 
        config.setProperty("tkt.header4", jtxtTktHeader4.getText()); 
        config.setProperty("tkt.header5", jtxtTktHeader5.getText()); 
        config.setProperty("tkt.header6", jtxtTktHeader6.getText()); 
        
        config.setProperty("tkt.footer1", jtxtTktFooter1.getText());
        config.setProperty("tkt.footer2", jtxtTktFooter2.getText()); 
        config.setProperty("tkt.footer3", jtxtTktFooter3.getText()); 
        config.setProperty("tkt.footer4", jtxtTktFooter4.getText()); 
        config.setProperty("tkt.footer5", jtxtTktFooter5.getText()); 
        config.setProperty("tkt.footer6", jtxtTktFooter6.getText());          

        config.setProperty("tkt.logopath", jLbllogoPath.getText());
        
        dirty.setDirty(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTktHeader1 = new javax.swing.JLabel();
        lblTktFooter1 = new javax.swing.JLabel();
        webSwtch_Logo = new com.alee.extended.button.WebSwitch();
        jPanel1 = new javax.swing.JPanel();
        jtxtTktHeader3 = new javax.swing.JTextField();
        jtxtTktFooter6 = new javax.swing.JTextField();
        jtxtTktHeader2 = new javax.swing.JTextField();
        jtxtTktHeader5 = new javax.swing.JTextField();
        jtxtTktHeader6 = new javax.swing.JTextField();
        jtxtTktFooter4 = new javax.swing.JTextField();
        jtxtTktHeader4 = new javax.swing.JTextField();
        jtxtTktFooter5 = new javax.swing.JTextField();
        jtxtTktHeader1 = new javax.swing.JTextField();
        jtxtTktFooter3 = new javax.swing.JTextField();
        jtxtTktFooter2 = new javax.swing.JTextField();
        jtxtTktFooter1 = new javax.swing.JTextField();
        jLogo = new javax.swing.JLabel();
        jLbllogoPath = new javax.swing.JLabel();
        lblLogo = new javax.swing.JLabel();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(700, 500));

        lblTktHeader1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblTktHeader1.setText(AppLocal.getIntString("label.tktheader1")); // NOI18N
        lblTktHeader1.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktHeader1.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktHeader1.setPreferredSize(new java.awt.Dimension(150, 30));

        lblTktFooter1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblTktFooter1.setText(AppLocal.getIntString("label.tktfooter1")); // NOI18N
        lblTktFooter1.setMaximumSize(new java.awt.Dimension(0, 25));
        lblTktFooter1.setMinimumSize(new java.awt.Dimension(0, 0));
        lblTktFooter1.setPreferredSize(new java.awt.Dimension(150, 30));

        webSwtch_Logo.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        webSwtch_Logo.setPreferredSize(new java.awt.Dimension(80, 30));
        webSwtch_Logo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webSwtch_LogoActionPerformed(evt);
            }
        });

        jtxtTktHeader3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktHeader3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktHeader3.setBorder(null);
        jtxtTktHeader3.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktHeader3.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktHeader3.setPreferredSize(new java.awt.Dimension(300, 30));

        jtxtTktFooter6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter6.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktFooter6.setBorder(null);
        jtxtTktFooter6.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter6.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter6.setPreferredSize(new java.awt.Dimension(300, 30));

        jtxtTktHeader2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktHeader2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktHeader2.setBorder(null);
        jtxtTktHeader2.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktHeader2.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktHeader2.setPreferredSize(new java.awt.Dimension(300, 30));

        jtxtTktHeader5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktHeader5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktHeader5.setBorder(null);
        jtxtTktHeader5.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktHeader5.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktHeader5.setPreferredSize(new java.awt.Dimension(300, 30));

        jtxtTktHeader6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktHeader6.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktHeader6.setBorder(null);
        jtxtTktHeader6.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktHeader6.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktHeader6.setPreferredSize(new java.awt.Dimension(300, 30));

        jtxtTktFooter4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktFooter4.setBorder(null);
        jtxtTktFooter4.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter4.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter4.setPreferredSize(new java.awt.Dimension(300, 30));

        jtxtTktHeader4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktHeader4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktHeader4.setBorder(null);
        jtxtTktHeader4.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktHeader4.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktHeader4.setPreferredSize(new java.awt.Dimension(300, 30));

        jtxtTktFooter5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktFooter5.setBorder(null);
        jtxtTktFooter5.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter5.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter5.setPreferredSize(new java.awt.Dimension(300, 30));

        jtxtTktHeader1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktHeader1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktHeader1.setBorder(null);
        jtxtTktHeader1.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktHeader1.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktHeader1.setPreferredSize(new java.awt.Dimension(300, 30));

        jtxtTktFooter3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktFooter3.setBorder(null);
        jtxtTktFooter3.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter3.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter3.setPreferredSize(new java.awt.Dimension(300, 30));

        jtxtTktFooter2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktFooter2.setBorder(null);
        jtxtTktFooter2.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter2.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter2.setPreferredSize(new java.awt.Dimension(300, 30));

        jtxtTktFooter1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtTktFooter1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtxtTktFooter1.setBorder(null);
        jtxtTktFooter1.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtTktFooter1.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtTktFooter1.setPreferredSize(new java.awt.Dimension(300, 30));

        jLogo.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer.ticket.logo.jpg"))); // NOI18N
        jLogo.setToolTipText("");
        jLogo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLogo.setOpaque(true);
        jLogo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jtxtTktFooter5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxtTktFooter4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxtTktFooter3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxtTktFooter2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxtTktFooter1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxtTktHeader6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxtTktHeader5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxtTktHeader4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxtTktHeader3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxtTktHeader2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxtTktHeader1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxtTktFooter6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLogo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 91, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jtxtTktHeader1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jtxtTktHeader2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jtxtTktHeader3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jtxtTktHeader4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jtxtTktHeader5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jtxtTktHeader6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jtxtTktFooter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jtxtTktFooter2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jtxtTktFooter3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jtxtTktFooter4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jtxtTktFooter5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jtxtTktFooter6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLbllogoPath.setBackground(new java.awt.Color(255, 255, 255));
        jLbllogoPath.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLbllogoPath.setForeground(new java.awt.Color(153, 153, 153));
        jLbllogoPath.setText(AppLocal.getIntString("label.tktheader1")); // NOI18N
        jLbllogoPath.setMaximumSize(new java.awt.Dimension(0, 25));
        jLbllogoPath.setMinimumSize(new java.awt.Dimension(0, 0));
        jLbllogoPath.setPreferredSize(new java.awt.Dimension(150, 30));

        lblLogo.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblLogo.setText(AppLocal.getIntString("label.tktheader1")); // NOI18N
        lblLogo.setMaximumSize(new java.awt.Dimension(0, 25));
        lblLogo.setMinimumSize(new java.awt.Dimension(0, 0));
        lblLogo.setPreferredSize(new java.awt.Dimension(150, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(lblTktFooter1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTktHeader1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(webSwtch_Logo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLbllogoPath, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(214, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(webSwtch_Logo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(55, 55, 55)
                        .addComponent(lblTktHeader1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(163, 163, 163)
                        .addComponent(lblTktFooter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(70, 70, 70)
                        .addComponent(jLbllogoPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void webSwtch_LogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webSwtch_LogoActionPerformed
// JG - For future
        if (webSwtch_Logo.isSelected()) {

        } else {

        }

    }//GEN-LAST:event_webSwtch_LogoActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLbllogoPath;
    private javax.swing.JLabel jLogo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jtxtTktFooter1;
    private javax.swing.JTextField jtxtTktFooter2;
    private javax.swing.JTextField jtxtTktFooter3;
    private javax.swing.JTextField jtxtTktFooter4;
    private javax.swing.JTextField jtxtTktFooter5;
    private javax.swing.JTextField jtxtTktFooter6;
    private javax.swing.JTextField jtxtTktHeader1;
    private javax.swing.JTextField jtxtTktHeader2;
    private javax.swing.JTextField jtxtTktHeader3;
    private javax.swing.JTextField jtxtTktHeader4;
    private javax.swing.JTextField jtxtTktHeader5;
    private javax.swing.JTextField jtxtTktHeader6;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblTktFooter1;
    private javax.swing.JLabel lblTktHeader1;
    private com.alee.extended.button.WebSwitch webSwtch_Logo;
    // End of variables declaration//GEN-END:variables
    
}
