package com.openbravo.pos.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.apache.fop.fonts.type1.AdobeStandardEncoding.w;

public class NotifyCenter{
    JWindow w;

//    JWindow w2;

    public NotifyCenter(){}

    public NotifyCenter(String title, String msg){
        w = new JWindow();
        w.setSize(500,200);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)screenSize.getWidth();
        int height = (int)screenSize.getHeight();

        JPanel p = new JPanel();
        FlowLayout layout1 = (FlowLayout) p.getLayout();
        layout1.setVgap(0);
        p.setBackground(new Color(206, 243, 250));
        p.setPreferredSize(new Dimension(500,100));

        JPanel titlePanel = new JPanel();
        titlePanel.setPreferredSize(new Dimension(500,50));
        titlePanel.setBackground(new Color(243, 253, 255));

        JLabel titleLabel = new JLabel(title);
        Font f1 = new Font("Serif",Font.BOLD,24);
        titleLabel.setFont(f1);
        titleLabel.setForeground(new Color(105, 150, 255));


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

        titlePanel.add(titleLabel);
        p.add(titlePanel);
        p.add(content);
        p.setVisible(true);
        w.add(p);

        w.setLocation(width/2 - 250,height/2 - 50);
        w.setVisible(true);
        Timer timer = new Timer( 5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                w.dispose();
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        NotifyCenter m = new NotifyCenter("Unthinkable Solutions","Please see the content . Thanks !!!");
    }

}