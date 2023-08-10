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
package com.openbravo.pos.config;

import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.util.FileChooserEvent;
import lombok.extern.slf4j.Slf4j;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.skin.SkinInfo;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author JG uniCenta
 */

@Slf4j
public class JPanelConfigGeneral extends javax.swing.JPanel implements PanelConfig {

  private final DirtyManager dirty = new DirtyManager();

  /**
   * Creates new form JPanelConfigGeneral
   */
  public JPanelConfigGeneral() {

    initComponents();

    InetAddress IP = null;
    try {
      IP = InetAddress.getLocalHost();
    } catch (UnknownHostException ex) {
      log.error(ex.getMessage());
    }

    jtxtMachineHostname.getDocument().addDocumentListener(dirty);
    jtxtMachineDepartment.getDocument().addDocumentListener(dirty);
    lblIP_Address.setText(IP.toString());
    jcboLAF.addActionListener(dirty);
    jcboMachineScreenmode.addActionListener(dirty);
    jcboTicketsBag.addActionListener(dirty);
    jchkHideInfo.addActionListener(dirty);
    jtxtStartupText.getDocument().addDocumentListener(dirty);
    jbtnText.addActionListener(new FileChooserEvent(jtxtStartupText));
    jtxtStartupLogo.getDocument().addDocumentListener(dirty);
    jbtnLogo.addActionListener(new FileChooserEvent(jtxtStartupLogo));
    jtxtStartupHTML.getDocument().addDocumentListener(dirty);
    jbtnHTML.addActionListener(new FileChooserEvent(jtxtStartupHTML));

//        jtxtStartupMedia.getDocument().addDocumentListener(dirty);             // Coming later!
//        jbtnMedia.addActionListener(new FileChooserEvent(jtxtStartupHTML));    // Coming later!          

    // Installed skins
    LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
    for (LookAndFeelInfo laf : lafs) {
      jcboLAF.addItem(new LAFInfo(laf.getName(), laf.getClassName()));
    }

    // Substance skins
    // new SubstanceLookAndFeel()
    Map<String, SkinInfo> skins = SubstanceLookAndFeel.getAllSkins();

    skins.values().forEach((skin) -> {
      jcboLAF.addItem(new LAFInfo(skin.getDisplayName(), skin.getClassName()));
    });

    jcboLAF.addActionListener((java.awt.event.ActionEvent evt) -> {
      changeLAF();
    });

    jcboMachineScreenmode.addItem("window");
    jcboMachineScreenmode.addItem("fullscreen");

    jcboTicketsBag.addItem("simple");
    jcboTicketsBag.addItem("standard");
    jcboTicketsBag.addItem("restaurant");

  }

  /**
   * @return
   */
  @Override
  public boolean hasChanged() {
    return dirty.isDirty();
  }

  /**
   * @return
   */
  @Override
  public Component getConfigComponent() {
    return this;
  }

  /**
   * @param config
   */
  @Override
  public void loadProperties(AppConfig config) {

    jtxtMachineHostname.setText(config.getProperty("machine.hostname"));
    jtxtMachineDepartment.setText(config.getProperty("machine.department"));

    String lafclass = config.getProperty("swing.defaultlaf");
    jcboLAF.setSelectedItem(null);
    for (int i = 0; i < jcboLAF.getItemCount(); i++) {
      LAFInfo lafinfo = (LAFInfo) jcboLAF.getItemAt(i);
      if (lafinfo.getClassName().equals(lafclass)) {
        jcboLAF.setSelectedIndex(i);
        break;
      }
    }

    jcboMachineScreenmode.setSelectedItem(config.getProperty("machine.screenmode"));
    jcboTicketsBag.setSelectedItem(config.getProperty("machine.ticketsbag"));
    jchkHideInfo.setSelected(Boolean.parseBoolean(config.getProperty("till.hideinfo")));
    jtxtStartupLogo.setText(config.getProperty("start.logo"));
    jtxtStartupText.setText(config.getProperty("start.text"));
    jtxtStartupLogo.setText(config.getProperty("start.logo"));
    jtxtStartupHTML.setText(config.getProperty("start.html"));

    String txtPIN = (config.getProperty("override.pin"));
    if (txtPIN == null) {
      config.setProperty("override.check", "true");
      config.setProperty("override.pin", "1234");
    }

//         jtxtStartupMedia.setText(config.getProperty("start.media"));           // Coming later!! 

    dirty.setDirty(false);
  }

  /**
   * @param config
   */
  @Override
  public void saveProperties(AppConfig config) {
    config.setProperty("machine.hostname", jtxtMachineHostname.getText());
    config.setProperty("machine.department", jtxtMachineDepartment.getText());

    LAFInfo laf = (LAFInfo) jcboLAF.getSelectedItem();
    config.setProperty("swing.defaultlaf", laf == null
            ? System.getProperty("swing.defaultlaf", "javax.swing.plaf.metal.MetalLookAndFeel")
            : laf.getClassName());

    config.setProperty("machine.screenmode", comboValue(jcboMachineScreenmode.getSelectedItem()));
    config.setProperty("machine.ticketsbag", comboValue(jcboTicketsBag.getSelectedItem()));
    config.setProperty("till.hideinfo", Boolean.toString(jchkHideInfo.isSelected()));
    config.setProperty("start.logo", jtxtStartupLogo.getText());
    config.setProperty("start.text", jtxtStartupText.getText());
    config.setProperty("start.html", jtxtStartupHTML.getText());

//        config.setProperty("start.media", jtxtStartupMedia.getText());          // Coming later!     

    dirty.setDirty(false);
  }

  private String comboValue(Object value) {
    return value == null ? "" : value.toString();
  }

  private void changeLAF() {

    final LAFInfo laf = (LAFInfo) jcboLAF.getSelectedItem();
    if (laf != null && !laf.getClassName().equals(UIManager.getLookAndFeel().getClass().getName())) {
      // The selected look and feel is different from the current look and feel.
      SwingUtilities.invokeLater(() -> {
        try {
          String lafname = laf.getClassName();
          Object laf1 = Class.forName(lafname).newInstance();
          if (laf1 instanceof LookAndFeel) {
            UIManager.setLookAndFeel((LookAndFeel) laf1);
          } else if (laf1 instanceof SubstanceSkin) {
            // Ultimate hack ;)
            String substanceClassName = "Substance"+lafname.split("skin.")[1].split("Skin")[0]+"LookAndFeel";
            String substancePackageName = "org.pushingpixels.substance.api.skin.";
            String substanceLookAndFeel = substancePackageName+substanceClassName;
            LookAndFeel lookAndFeel = (LookAndFeel) Class.forName(substanceLookAndFeel).newInstance();
            UIManager.setLookAndFeel(lookAndFeel);
          }
          SwingUtilities.updateComponentTreeUI(JPanelConfigGeneral.this.getTopLevelAncestor());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
          log.error("There has been an issue changing the look and feel: "+e);
        }
      });
    }
  }

  private static class LAFInfo {

    private final String name;
    private final String classname;

    public LAFInfo(String name, String classname) {
      this.name = name;
      this.classname = classname;
    }

    public String getName() {
      return name;
    }

    public String getClassName() {
      return classname;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jPanel11 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();
    jtxtMachineHostname = new javax.swing.JTextField();
    jcboLAF = new javax.swing.JComboBox();
    jcboMachineScreenmode = new javax.swing.JComboBox();
    jcboTicketsBag = new javax.swing.JComboBox();
    jchkHideInfo = new javax.swing.JCheckBox();
    jLabel18 = new javax.swing.JLabel();
    jtxtStartupLogo = new javax.swing.JTextField();
    jLabel19 = new javax.swing.JLabel();
    jtxtStartupText = new javax.swing.JTextField();
    jbtnLogo = new javax.swing.JButton();
    jbtnText = new javax.swing.JButton();
    jbtnTextClear = new javax.swing.JButton();
    jLabel6 = new javax.swing.JLabel();
    jtxtMachineDepartment = new javax.swing.JTextField();
    lblIP_Address = new com.alee.laf.label.WebLabel();
    webLabel1 = new com.alee.laf.label.WebLabel();
    jLblURL = new javax.swing.JLabel();
    jtxtStartupHTML = new javax.swing.JTextField();
    jbtnHTML = new javax.swing.JButton();
    jbtnClearHTML = new javax.swing.JButton();

    setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
    setOpaque(false);
    setPreferredSize(new java.awt.Dimension(800, 450));

    jPanel11.setBackground(new java.awt.Color(255, 255, 255));
    jPanel11.setOpaque(false);
    jPanel11.setPreferredSize(new java.awt.Dimension(750, 450));

    jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jLabel1.setText(AppLocal.getIntString("label.MachineName")); // NOI18N
    jLabel1.setPreferredSize(new java.awt.Dimension(150, 30));

    jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jLabel2.setText(AppLocal.getIntString("label.looknfeel")); // NOI18N
    jLabel2.setPreferredSize(new java.awt.Dimension(150, 30));

    jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jLabel3.setText(AppLocal.getIntString("label.MachineScreen")); // NOI18N
    jLabel3.setPreferredSize(new java.awt.Dimension(150, 30));

    jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jLabel4.setText(AppLocal.getIntString("label.Ticketsbag")); // NOI18N
    jLabel4.setPreferredSize(new java.awt.Dimension(150, 30));

    jtxtMachineHostname.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jtxtMachineHostname.setToolTipText(AppLocal.getIntString("tooltip.config.general.terminal")); // NOI18N
    jtxtMachineHostname.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    jtxtMachineHostname.setMinimumSize(new java.awt.Dimension(130, 25));
    jtxtMachineHostname.setPreferredSize(new java.awt.Dimension(200, 30));

    jcboLAF.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jcboLAF.setToolTipText(AppLocal.getIntString("tooltip.config.general.skin")); // NOI18N
    jcboLAF.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    jcboLAF.setPreferredSize(new java.awt.Dimension(200, 30));
    jcboLAF.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jcboLAFActionPerformed(evt);
      }
    });

    jcboMachineScreenmode.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jcboMachineScreenmode.setToolTipText(AppLocal.getIntString("tooltip.config.general.screen")); // NOI18N
    jcboMachineScreenmode.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    jcboMachineScreenmode.setPreferredSize(new java.awt.Dimension(200, 30));

    jcboTicketsBag.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jcboTicketsBag.setToolTipText(AppLocal.getIntString("tooltip.config.general.tickets")); // NOI18N
    jcboTicketsBag.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    jcboTicketsBag.setPreferredSize(new java.awt.Dimension(200, 30));

    jchkHideInfo.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jchkHideInfo.setSelected(true);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
    jchkHideInfo.setText(bundle.getString("label.Infopanel")); // NOI18N
    jchkHideInfo.setToolTipText(AppLocal.getIntString("tooltip.config.general.footer")); // NOI18N
    jchkHideInfo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    jchkHideInfo.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    jchkHideInfo.setMaximumSize(new java.awt.Dimension(0, 25));
    jchkHideInfo.setMinimumSize(new java.awt.Dimension(0, 0));
    jchkHideInfo.setOpaque(false);
    jchkHideInfo.setPreferredSize(new java.awt.Dimension(150, 30));

    jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jLabel18.setText(bundle.getString("label.startuplogo")); // NOI18N
    jLabel18.setMaximumSize(new java.awt.Dimension(0, 25));
    jLabel18.setMinimumSize(new java.awt.Dimension(0, 0));
    jLabel18.setPreferredSize(new java.awt.Dimension(150, 30));

    jtxtStartupLogo.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jtxtStartupLogo.setToolTipText(AppLocal.getIntString("tooltip.config.general.logo")); // NOI18N
    jtxtStartupLogo.setMaximumSize(new java.awt.Dimension(0, 25));
    jtxtStartupLogo.setMinimumSize(new java.awt.Dimension(0, 0));
    jtxtStartupLogo.setPreferredSize(new java.awt.Dimension(400, 30));

    jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jLabel19.setText(AppLocal.getIntString("label.startuptext")); // NOI18N
    jLabel19.setMaximumSize(new java.awt.Dimension(0, 25));
    jLabel19.setMinimumSize(new java.awt.Dimension(0, 0));
    jLabel19.setPreferredSize(new java.awt.Dimension(150, 30));

    jtxtStartupText.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jtxtStartupText.setToolTipText(AppLocal.getIntString("tooltip.config.general.text")); // NOI18N
    jtxtStartupText.setMaximumSize(new java.awt.Dimension(0, 25));
    jtxtStartupText.setMinimumSize(new java.awt.Dimension(0, 0));
    jtxtStartupText.setPreferredSize(new java.awt.Dimension(400, 30));
    jtxtStartupText.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        jtxtStartupTextFocusGained(evt);
      }
    });
    jtxtStartupText.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jtxtStartupTextActionPerformed(evt);
      }
    });

    jbtnLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileopen.png"))); // NOI18N
    jbtnLogo.setText("  ");
    jbtnLogo.setToolTipText(AppLocal.getIntString("tooltip.config.general.logo")); // NOI18N
    jbtnLogo.setMaximumSize(new java.awt.Dimension(64, 32));
    jbtnLogo.setMinimumSize(new java.awt.Dimension(64, 32));
    jbtnLogo.setPreferredSize(new java.awt.Dimension(80, 45));
    jbtnLogo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jbtnLogoActionPerformed(evt);
      }
    });

    jbtnText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileopen.png"))); // NOI18N
    jbtnText.setText("  ");
    jbtnText.setToolTipText(AppLocal.getIntString("tooltip.config.general.text")); // NOI18N
    jbtnText.setMaximumSize(new java.awt.Dimension(64, 32));
    jbtnText.setMinimumSize(new java.awt.Dimension(64, 32));
    jbtnText.setPreferredSize(new java.awt.Dimension(80, 45));
    jbtnText.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jbtnTextActionPerformed(evt);
      }
    });

    jbtnTextClear.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
    jbtnTextClear.setForeground(new java.awt.Color(255, 0, 153));
    jbtnTextClear.setText("X");
    jbtnTextClear.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jbtnTextClearActionPerformed(evt);
      }
    });

    jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jLabel6.setText(AppLocal.getIntString("label.MachineDepartment")); // NOI18N
    jLabel6.setPreferredSize(new java.awt.Dimension(150, 30));

    jtxtMachineDepartment.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jtxtMachineDepartment.setToolTipText(AppLocal.getIntString("tooltip.config.general.dept")); // NOI18N
    jtxtMachineDepartment.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    jtxtMachineDepartment.setMinimumSize(new java.awt.Dimension(130, 25));
    jtxtMachineDepartment.setPreferredSize(new java.awt.Dimension(200, 30));

    lblIP_Address.setBackground(new java.awt.Color(240, 240, 240));
    lblIP_Address.setBorder(null);
    lblIP_Address.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    lblIP_Address.setToolTipText(AppLocal.getIntString("tooltip.config.general.compip")); // NOI18N
    lblIP_Address.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    lblIP_Address.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
    lblIP_Address.setPreferredSize(new java.awt.Dimension(230, 30));

    webLabel1.setBackground(new java.awt.Color(240, 240, 240));
    webLabel1.setBorder(null);
    webLabel1.setText(bundle.getString("label.nameIP")); // NOI18N
    webLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    webLabel1.setPreferredSize(new java.awt.Dimension(300, 30));

    jLblURL.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jLblURL.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/pay.png"))); // NOI18N
    jLblURL.setText(AppLocal.getIntString("label.URL")); // NOI18N
    jLblURL.setToolTipText(bundle.getString("tooltip.config.general.URL")); // NOI18N
    jLblURL.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
    jLblURL.setMaximumSize(new java.awt.Dimension(0, 25));
    jLblURL.setMinimumSize(new java.awt.Dimension(0, 0));
    jLblURL.setPreferredSize(new java.awt.Dimension(150, 30));
    jLblURL.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jLblURLMouseClicked(evt);
      }
    });

    jtxtStartupHTML.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
    jtxtStartupHTML.setToolTipText(AppLocal.getIntString("tooltip.config.general.text")); // NOI18N
    jtxtStartupHTML.setMaximumSize(new java.awt.Dimension(0, 25));
    jtxtStartupHTML.setMinimumSize(new java.awt.Dimension(0, 0));
    jtxtStartupHTML.setPreferredSize(new java.awt.Dimension(400, 30));
    jtxtStartupHTML.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        jtxtStartupHTMLFocusGained(evt);
      }
    });
    jtxtStartupHTML.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jtxtStartupHTMLActionPerformed(evt);
      }
    });

    jbtnHTML.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileopen.png"))); // NOI18N
    jbtnHTML.setText("  ");
    jbtnHTML.setToolTipText(AppLocal.getIntString("tooltip.config.general.text")); // NOI18N
    jbtnHTML.setMaximumSize(new java.awt.Dimension(64, 32));
    jbtnHTML.setMinimumSize(new java.awt.Dimension(64, 32));
    jbtnHTML.setPreferredSize(new java.awt.Dimension(80, 45));
    jbtnHTML.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jbtnHTMLActionPerformed(evt);
      }
    });

    jbtnClearHTML.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
    jbtnClearHTML.setForeground(new java.awt.Color(255, 0, 153));
    jbtnClearHTML.setText("X");
    jbtnClearHTML.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jbtnClearHTMLActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
    jPanel11.setLayout(jPanel11Layout);
    jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel11Layout.createSequentialGroup()
                                                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                            .addComponent(jtxtMachineHostname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                            .addComponent(jtxtMachineDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(webLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(lblIP_Address, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jcboLAF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jcboMachineScreenmode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel11Layout.createSequentialGroup()
                                                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                            .addComponent(jtxtStartupLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel11Layout.createSequentialGroup()
                                                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                    .addComponent(jLblURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                    .addComponent(jchkHideInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                    .addComponent(jtxtStartupText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                    .addComponent(jtxtStartupHTML, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jbtnLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                                            .addComponent(jbtnText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(jbtnTextClear, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                                            .addComponent(jbtnHTML, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(jbtnClearHTML, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jcboTicketsBag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jtxtMachineHostname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(webLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(lblIP_Address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jtxtMachineDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jcboLAF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jcboMachineScreenmode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jcboTicketsBag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(48, 48, 48)
                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jtxtStartupLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jbtnLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jtxtStartupText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jbtnText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jbtnTextClear))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLblURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jtxtStartupHTML, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jbtnHTML, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jbtnClearHTML))
                            .addGap(18, 18, 18)
                            .addComponent(jchkHideInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
    );
    layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
    );

    getAccessibleContext().setAccessibleName("");
  }// </editor-fold>//GEN-END:initComponents

  private void jcboLAFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboLAFActionPerformed

  }//GEN-LAST:event_jcboLAFActionPerformed

  private void jtxtStartupTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxtStartupTextActionPerformed

  }//GEN-LAST:event_jtxtStartupTextActionPerformed

  private void jtxtStartupTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jtxtStartupTextFocusGained
    // JG 31 August 2103 GNU GPL License Warning

//        transferFocus();

//        JOptionPane.showMessageDialog(jPanel1,"<html>Changing default Startup Text content may violate the <br>"
//           + " Free Software Foundation's GNU General Public License GPL","GNU GPL Warning",JOptionPane.WARNING_MESSAGE);

  }//GEN-LAST:event_jtxtStartupTextFocusGained

  private void jbtnTextClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnTextClearActionPerformed
    jtxtStartupText.setText("");
  }//GEN-LAST:event_jbtnTextClearActionPerformed

  private void jtxtStartupHTMLFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jtxtStartupHTMLFocusGained
    // TODO add your handling code here:
  }//GEN-LAST:event_jtxtStartupHTMLFocusGained

  private void jtxtStartupHTMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxtStartupHTMLActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jtxtStartupHTMLActionPerformed

  private void jbtnClearHTMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnClearHTMLActionPerformed
    jtxtStartupHTML.setText("");
  }//GEN-LAST:event_jbtnClearHTMLActionPerformed

  private void jbtnHTMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnHTMLActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jbtnHTMLActionPerformed

  private void jbtnLogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnLogoActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jbtnLogoActionPerformed

  private void jLblURLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLblURLMouseClicked
    JOptionPane.showMessageDialog(this,
            AppLocal.getIntString("message.URL"),
            "URL",
            JOptionPane.INFORMATION_MESSAGE);
  }//GEN-LAST:event_jLblURLMouseClicked

  private void jbtnTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnTextActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_jbtnTextActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel18;
  private javax.swing.JLabel jLabel19;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLblURL;
  private javax.swing.JPanel jPanel11;
  private javax.swing.JButton jbtnClearHTML;
  private javax.swing.JButton jbtnHTML;
  private javax.swing.JButton jbtnLogo;
  private javax.swing.JButton jbtnText;
  private javax.swing.JButton jbtnTextClear;
  private javax.swing.JComboBox jcboLAF;
  private javax.swing.JComboBox jcboMachineScreenmode;
  private javax.swing.JComboBox jcboTicketsBag;
  private javax.swing.JCheckBox jchkHideInfo;
  private javax.swing.JTextField jtxtMachineDepartment;
  private javax.swing.JTextField jtxtMachineHostname;
  private javax.swing.JTextField jtxtStartupHTML;
  private javax.swing.JTextField jtxtStartupLogo;
  private javax.swing.JTextField jtxtStartupText;
  private com.alee.laf.label.WebLabel lblIP_Address;
  private com.alee.laf.label.WebLabel webLabel1;
  // End of variables declaration//GEN-END:variables
}
