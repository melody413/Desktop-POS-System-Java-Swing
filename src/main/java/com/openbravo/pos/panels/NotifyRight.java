package com.openbravo.pos.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class NotifyRight{
    JWindow w;

//    JWindow w2;

    public NotifyRight(){

    }

    public NotifyRight(String title, String msg, int height){
        w = new JWindow();
        w.setSize(500,200);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)screenSize.getWidth();
        int screenHeight = (int)screenSize.getHeight();

        JPanel p = new JPanel();
        FlowLayout layout1 = (FlowLayout) p.getLayout();
        layout1.setVgap(0);
        p.setBackground(new Color(206, 243, 250));
        p.setPreferredSize(new Dimension(500,100));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING,10,0));
        titlePanel.setPreferredSize(new Dimension(500,50));
        titlePanel.setBackground(new Color(243, 253, 255));

        JTextField textField = new JTextField(title);
        Font f1 = new Font("Serif",Font.BOLD,24);
        textField.setFont(f1);
        textField.setForeground(new Color(105, 150, 255));
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setBackground(new Color(243, 253, 255));
        textField.setPreferredSize(new Dimension(400,50));

        JButton close = new JButton();
        Font f3 = new Font("Sans-Serif",Font.BOLD,20);
        close.setFont(f3);
        close.setText("X");
        close.setPreferredSize(new Dimension(70,50));
        close.setForeground(new Color(199,197,197));
        close.setBackground(new Color(243, 253, 255));
        close.setBorder(BorderFactory.createEmptyBorder());
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                w.dispose();
            }
        });

        JTextArea content = new JTextArea(msg);
        Font f2 = new Font("Serif",Font.BOLD,20);
        content.setPreferredSize(new Dimension(500,140));
        content.setMargin(new Insets(10,10,10,10));
        content.setBackground(new Color(206, 243, 250));
        content.setFont(f2);
        content.setEditable(false);
        content.setWrapStyleWord(true);
        content.setLineWrap(true);
        content.setLayout(new CardLayout());

        titlePanel.add(textField);
        titlePanel.add(close);
        p.add(titlePanel);
        p.add(content);
        p.setVisible(true);
        w.add(p);

        w.setLocation(width-560, height);
        w.setVisible(true);
        Timer timer = new Timer( 4000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                w.dispose();
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        NotifyRight m = new NotifyRight("Unthinkable Solutions","Please see the content . Thanks !!!", 100);
    }

}