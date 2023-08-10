package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JCompletedOrderDialog;
import com.openbravo.beans.JJobDialog;
import com.openbravo.beans.JStringDialog;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.restaurant.RestaurantDBUtils;
import com.openbravo.pos.util.WebSocketClient;
import com.openbravo.pos.ticket.TicketInfo;
import java.applet.Applet;
import java.applet.AudioClip;
import lombok.extern.slf4j.Slf4j;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.websocket.MessageHandler;

@Slf4j
public class KitchenScreen extends JPanel implements BeanFactoryApp, JPanelView {
    private JPanel contentPane = new JPanel(null);
    private JPanel panel;
    private JPanel orderPanel;

    private Timestamp timestamp;
    private JPanel panel4;
    private JButton jButton;
    private JScrollPane scrollPanefinal;
    private JButton btnCompleted;

    private JButton btnClockIn;
    private JButton btnClockOut;

    private AppConfig m_config;
    private AppView m_App;
    private RestaurantDBUtils restDB;
    protected DataLogicSystem dlSystem;
    protected DataLogicSales dlSales;
    protected DataLogicCustomers dlCustomers;
    private DataLogicReceipts dlReceipts = null;
    private LocalDateTime refreshedTime;

    private List<Object[]> ticketids;
    private WebSocketClient socketClient;

    public KitchenScreen() {

    }

    public void initComponent() {
        int count, pages;
        LocalDateTime now = LocalDateTime.now();
        now = now.minusDays(10);
        String station_id = m_App.getAppUserView().getUser().getStationId();

        MessageHandler myHandler = new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                // Handle the received message
                if (message == "New Order") {
                    try {
                        StringBuilder text = new StringBuilder();
                        AudioClip oAudio = Applet.newAudioClip(getClass().getClassLoader().getResource("com/openbravo/audios/alarm.wav"));
                        oAudio.play();        
                    } catch (Exception e) {
                        System.out.println("Refresh Order Exception " + e.getMessage());
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    refreshOrder();
                });
            }
        };

        socketClient = ((JRootApp) m_App).getSocketClient();
        socketClient.setConnection(myHandler);
        timestamp = dlSystem.getLastOrderTime(station_id);
        if(timestamp == null)
            timestamp = Timestamp.valueOf(now);
        try {
            List<Object[]> ticketids = dlSystem.getNewOrders(station_id, Timestamp.valueOf(now));

            if (panel != null) {
                remove(scrollPanefinal);
            }

            showModel(ticketids);
            count = dlSystem.getCountOfOrderBlock();
            pages = count / 8;
        } catch (BasicException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_config = new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
        m_config.load();
        m_App = app;
        restDB = new RestaurantDBUtils(m_App);
        dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlCustomers = (DataLogicCustomers) m_App.getBean("com.openbravo.pos.customers.DataLogicCustomers");
        dlReceipts = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        initComponent();

        scrollPanefinal.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                contentPane.removeAll();
                refreshOrder();
            }
        });

    }

    public void refreshOrder(){
        try {
            String station_id = m_App.getAppUserView().getUser().getStationId();
            LocalDateTime now = LocalDateTime.now();
            now = now.minusDays(5);
            List<Object[]> ticketids = dlSystem.getNewOrders(station_id, Timestamp.valueOf(now));
            now = now.minusDays(10);
            ticketids = dlSystem.getNewOrders(station_id, Timestamp.valueOf(now));
            if(panel!=null){
                remove(scrollPanefinal);
                remove(btnCompleted);
            }
            showModel(ticketids);
            refreshedTime = LocalDateTime.now();
            timestamp = dlSystem.getLastOrderTime(station_id);
            if(timestamp == null)
                timestamp = Timestamp.valueOf(now);
        } catch (BasicException e) {
            throw new RuntimeException(e);
        }

    }

    public void refreshOrderManually(){
        try{
            String station_id = m_App.getAppUserView().getUser().getStationId();
            List<Object[]> ticketids = dlSystem.getUpdatedOrders(station_id, Timestamp.valueOf(refreshedTime));
            LocalDateTime now = LocalDateTime.now();
            now = now.minusDays(10);
            ticketids = dlSystem.getNewOrders(station_id, Timestamp.valueOf(now));
            if(panel!=null){
                remove(scrollPanefinal);
                remove(btnCompleted);
            }
            showModel(ticketids);
            refreshedTime = LocalDateTime.now();
            timestamp = dlSystem.getLastOrderTime(station_id);
            if(timestamp == null)
                timestamp = Timestamp.valueOf(now);
        } catch (BasicException e) {
            throw new RuntimeException(e);
        }

    }

    public void showModel(List<Object[]> ticketids) {

        HashMap<JButton, String> hmDoneButton = new HashMap<>();
        HashMap<JButton, String> hmDoneAll = new HashMap<>();
        HashMap<JButton, Timer> doneButtonTimer = new HashMap<>();
        HashMap<JButton, Timer> doneAllTimer = new HashMap<>();
        boolean doneButtonDisable = true;


        panel = new JPanel();
        panel.setLayout(new java.awt.GridLayout(0, 5));
        panel.setBackground(new Color(47,49,53));
        panel.setVisible(true);

        String[] ticketidsArray = new String[ticketids.size()];
        String[] orderidsArray = new String[ticketids.size()];
        LocalDateTime[] orderTimeArray = new LocalDateTime[ticketids.size()];

        int t = 0;
        for (Object[] obj : ticketids) {
            ticketidsArray[t] =  obj[0].toString();
            orderidsArray[t] =  obj[2].toString();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy");
            LocalDateTime orderTime = LocalDateTime.parse(obj[1].toString(), formatter);

            orderTimeArray[t] = orderTime;
            t++;
        }

        for (int a = 0; a< ticketidsArray.length; a++) {

            doneButtonDisable = true;
            boolean isRefireOrder = false;

            final int blockIndex = a;
            List<Object[]> order = null;
            try {
                order = dlSystem.getOrderByTicketid(orderidsArray[a], orderTimeArray[a].toString());
                isRefireOrder = dlSystem.isRefiredOrder(ticketidsArray[a]);
            } catch (BasicException e) {
                throw new RuntimeException(e);
            }

            //orderPanel - ticket Panel
            orderPanel = new JPanel();
            orderPanel.setLayout(new java.awt.FlowLayout(FlowLayout.CENTER));

            orderPanel.setBackground(new Color(47,49,53));
            orderPanel.setBorder(BorderFactory.createLineBorder(Color.black));

            //panel2 - title Panel
            JPanel panel2 = new JPanel();
            panel2.setLayout(new BorderLayout(0,10));
            panel2.setPreferredSize(new Dimension(330, 30));
            Color tPColor = new Color(71, 67, 67);
            panel2.setBackground(tPColor);
            panel2.setBorder(BorderFactory.createEmptyBorder());

            String ticketname = "";
            try {
                TicketInfo ticket = dlReceipts.getSharedTicket(ticketidsArray[a]);

                if(ticketidsArray[a] != null) {
                    if (ticketidsArray[a].contains("[")) {
                        String[] temp = ticketidsArray[a].split("\\[");
                        String[] tempName = ticket.getName().split(" - ");

                        ticketname = temp[0] + " - " + tempName[0] + "[" + temp[1];
                    } else {
                        if (ticket != null) {
                            String[] tempName = ticket.getName().split(" - ");
                            ticketname = ticketidsArray[a] + " - " + tempName[0];
                        }
                    }
                }
            } catch (BasicException e) {} 

            //textfield in title Panel
            JLabel title = new JLabel();
            title.setText(("#" + ticketname));
            title.setBorder(BorderFactory.createEmptyBorder());
            title.setFont(new Font("Serif", Font.BOLD, 16));
            // Change text font color
            title.setForeground(Color.WHITE);
            title.setBackground(tPColor);

            if(isRefireOrder) {
                panel2.setBackground(new Color(220, 92, 92));
            }

            JLabel timerLabel = new JLabel(String.format("%02d:%02d:%02d",0 , 0, 0));
            timerLabel.setForeground(Color.white);
            timerLabel.setFont(new Font("Serif", Font.BOLD, 16));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy");
            LocalDateTime orderTime = LocalDateTime.parse(order.get(0)[5].toString(), formatter);
            Long lastTickTime = orderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            Timer timer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    long runningTime = System.currentTimeMillis() - lastTickTime;
                    Duration duration = Duration.ofMillis(runningTime);
                    long hours = duration.toHours();
                    duration = duration.minusHours(hours);
                    long minutes = duration.toMinutes();
                    duration = duration.minusMinutes(minutes);
                    long millis = duration.toMillis();
                    long seconds = millis / 1000;
                    millis -= (seconds * 1000);
                    timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                }
            });
            timer.start();
            panel2.add(title, BorderLayout.WEST);
            panel2.add(timerLabel, BorderLayout.EAST);

            //panel3 - ticket lines main panel, containing all ticketlines
            JPanel panel3 = new JPanel();
            panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
            panel3.setBackground(new Color(47,49,53));
            panel3.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            int panel4Height = 0;

            //fetching out ticket lines of a particular ticket by its ticketid

            int count = 0;
            for (Object[] obj1 : order) {
                //panel4 - containing panel5 and panel6
                panel4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
                panel4.setBorder(BorderFactory.createEmptyBorder());
                if (obj1[6] == null) {
                    panel4.setBackground(new Color(47,49,53));
                } else {
                    panel4.setBackground(new Color(47,49,53));
                }

                //containing detail(textfield), attributes(textArea), note(textArea) and ordertime(textfield)
                JPanel panel5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));

                //contains the heading of dish
                JLabel detail = new JLabel();
                detail.setText((String) obj1[2] + "     X" + (String) obj1[7]); //fetching detail from query
                detail.setBorder(BorderFactory.createEmptyBorder());
                detail.setFont(new Font("Serif", Font.BOLD, 15));
                int detailAreaHeight = 30;
                detail.setPreferredSize(new Dimension(320, detailAreaHeight));

                //fetching out attributes from object
                String attributes = "";
                if (obj1[3] != null) {
                    attributes = (String) obj1[3];
                }

                String[] att = null;
                String attribute = "";
                if (attributes != null) {
                    att = attributes.split("/");
                    attribute = att[0];
                    for (int i = 1; i < att.length; i++) {
                        attribute += ", " + att[i];
                    }
                }
                System.out.println(attribute);
                String formatted = "<html><font size = '4'><div>" + attribute + "</div></font></html>";

                Font font = new Font("Serif", Font.PLAIN, 13);

                //textArea containing the attributes in form of String
                int textAreaHeight = 0;
                JLabel textArea = new JLabel();
                textArea.setText(formatted);
                textArea.setBorder(BorderFactory.createEmptyBorder());

                textArea.setFont(font);
                FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
                int attributeWidth = (int) (font.getStringBounds(attribute, frc).getWidth());
                int attributeHeight = (int) (font.getStringBounds(attribute, frc).getHeight());
                textAreaHeight = attributeHeight + 10;
                if (attributeWidth > 320) {
                    textAreaHeight = (attributeWidth / 320) * attributeHeight + attributeHeight + 30;
                }

                textArea.setPreferredSize(new Dimension(320, textAreaHeight));
                if(attribute.equals("")){
                    textArea.setVisible(false);
                    textAreaHeight =0;
                }

                //fetching out note from object
                String note = "";
                if (obj1[4] != null && !obj1[4].equals("") ) {
                    note = "NOTE: " + (String) obj1[4];
                }

                //textArea containing note
                int notesAreaHeight;
                JLabel notes = new JLabel();
                notes.setText(note);
                notes.setBorder(BorderFactory.createEmptyBorder());
                notes.setFont(font);
                int noteWidth = (int) (font.getStringBounds("NOTE: " + note, frc).getWidth());
                int noteHeight = (int) (font.getStringBounds("NOTE: " + note, frc).getHeight());
                notesAreaHeight = noteHeight + 10;
//                System.out.println("noteWidth = " + noteWidth + " " + "noteHeight = " + noteHeight);
                if (noteWidth > 320) {
                    notesAreaHeight = (noteWidth / 320) * noteHeight + noteHeight + 10;
                }
                notes.setPreferredSize(new Dimension(320, notesAreaHeight));
                notes.setForeground(Color.red);

                if (obj1[6] == null) {
                    notes.setBackground(new Color(47,49,53));
                } else {
                    notes.setBackground(new Color(47,49,53));
                }
                if(note.equals("")){
                    notesAreaHeight = 0;
                    notes.setVisible(false);
                }
                int panel5Height = detailAreaHeight + textAreaHeight + notesAreaHeight + 10;

                panel5.setPreferredSize(new Dimension(330, panel5Height));

                if("Kitchen In".equals(m_App.getAppUserView().getUser().getName())) {
                    panel5.addMouseListener(new MouseAdapter() { 
                        public void mousePressed(MouseEvent me) { 
                            try {
                                LocalDateTime now = LocalDateTime.now();
                                Timestamp checkinTime = Timestamp.valueOf(now);
                                if (obj1[6] == null)
                                    dlSystem.updateCompleteTime(checkinTime, obj1[0].toString());
                                else 
                                    dlSystem.updateCompleteTime(null, obj1[0].toString());

                                socketClient.sendMessage("Item is completed");
                                refreshOrder();
                            } catch (BasicException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                }

                //panel6 - containing done buton for particular ticket line
                JPanel panel6 = new JPanel();
                panel6.setPreferredSize(new Dimension(60, 100));

                if (obj1[6] == null) {
                    panel6.setBackground(new Color(47,49,53));
                    doneButtonDisable = false;
                } else {
                    panel6.setBackground(new Color(47,49,53));
                    panel5.setBackground(new Color(113, 131, 85));
                }   

                panel5.add(detail);
                panel5.add(textArea);
                panel5.add(notes);

                panel4.setPreferredSize(new Dimension(330, panel5Height + 10));
                panel4.add(panel5);
                panel4.add(panel6);

                panel3.add(panel4);
                panel3.setBorder(BorderFactory.createEmptyBorder());
                panel4Height += panel5Height + 10;
                count++;
            }

            //setting height as required to contain all ticketlines of a ticket
            panel3.setPreferredSize(new Dimension(330, panel4Height + 10 * count));

            //final button
            JButton doneAll = new JButton("Done");
            doneAll.setBackground(new Color(220, 92, 92));
            doneAll.setPreferredSize(new Dimension(330, 30));
            doneAll.setForeground(Color.WHITE);
            doneAll.setFont(new Font("Serif", Font.BOLD, 20));
            hmDoneAll.put(doneAll, orderidsArray[a].toString());
            doneAllTimer.put(doneAll, timer);
            doneAll.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    try {
                        List<String> list = dlSystem.getRemainingTicketLine(hmDoneAll.get(doneAll));
                        for (String detail : list) {
                            dlSystem.sendNotification(UUID.randomUUID().toString(), "Meal Prepared", detail,
                                    2, null, Timestamp.valueOf(now));
                        }
                        dlSystem.updateCompleteTimeForAll(hmDoneAll.get(doneAll), Timestamp.valueOf(orderTimeArray[blockIndex]));
                        socketClient.sendMessage("Done is clicked");
                        doneAllTimer.get(doneAll).stop();
                    } catch (BasicException ex) {
                        throw new RuntimeException(ex);
                    }
                    doneAll.getParent().setVisible(false);
                    refreshOrderManually();
                }
            });
            doneAll.setEnabled(doneButtonDisable);

            orderPanel.setPreferredSize(new Dimension(330, 50 + panel4Height + 10 * count + 10));
            orderPanel.add(panel2);
            orderPanel.add(panel3);
            orderPanel.setBorder(BorderFactory.createEmptyBorder());

            if(!"Kitchen In".equals(m_App.getAppUserView().getUser().getName())) {
                orderPanel.add(doneAll);
                panel.add(orderPanel);
            } else {
//                if (!doneButtonDisable)
                panel.add(orderPanel);
            }
        }
        //final scrollpane containing all tickets
        scrollPanefinal = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPanefinal.getVerticalScrollBar().setUnitIncrement(16);
        scrollPanefinal.getHorizontalScrollBar().setUnitIncrement(16);
        setLayout(new java.awt.BorderLayout());


        btnCompleted = new JButton("  Show Completed Ticketes");
        btnCompleted.setPreferredSize(new Dimension(150, 40));
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/com/openbravo/images/resources.png"));
        Image image = imageIcon.getImage();
        Image newImage = image.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
        btnCompleted.setIcon(new ImageIcon(newImage));
        btnCompleted.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCompletedTicket();
            }
        });

        btnClockIn = new JButton("Clock In");
        btnClockIn.setPreferredSize(new Dimension(150, 40));
        btnClockIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userClockIn();
            }
        });

        btnClockOut = new JButton("Clock Out");
        btnClockOut.setPreferredSize(new Dimension(150, 40));
        btnClockOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userClockOut();
            }
        });

        contentPane.setLayout(new BorderLayout(0,10));
        contentPane.removeAll();

        contentPane.add(btnClockOut, java.awt.BorderLayout.WEST);
        contentPane.add(btnCompleted, java.awt.BorderLayout.CENTER);
        contentPane.add(btnClockIn, java.awt.BorderLayout.EAST);

        add(contentPane, java.awt.BorderLayout.NORTH);
        add(scrollPanefinal, java.awt.BorderLayout.CENTER);
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

    private void userClockIn() {
        String sValue = JStringDialog.showEditString(this, "Employee Number");
        if(sValue != null){
            String peopleId = dlSystem.getPeopleId(sValue);
            boolean clockout  = dlSystem.getClockOutByPeopleID(peopleId);
            if (clockout != true) {
                JFrame frame = new JFrame("Swing Dialog");
                JOptionPane.showMessageDialog(frame, "Already Clocked in!");
                return;
            }

            if (peopleId != null){
                String sJob = JJobDialog.showSelectJob(dlSystem, peopleId, this, "Select Job");
                if(sJob!=null){
                LocalDateTime now = LocalDateTime.now();
                Timestamp checkinTime = Timestamp.valueOf(now);
                    Object[] checkin = new Object[5];
                    checkin[0] = UUID.randomUUID().toString();
                    checkin[1] = peopleId;
                    checkin[2] = "";                                                   
                    checkin[3] = checkinTime;                                           
                    checkin[4] = sJob;
                    dlSystem.execCheckin(checkin);
                }
            }   
        }
    }

    private void userClockOut() {
        String sValue = JStringDialog.showEditString(this, "Employee Number");
        if(sValue !=null){
            String peopleId = dlSystem.getPeopleId(sValue);
            if(peopleId!=null){
                dlSystem.execCheckout(peopleId);
                ((JRootApp) m_App).printClockOut(peopleId);
            }   
        }
    }

    private void showCompletedTicket() {
        List<Object[]> completedTickets = null;
        try {
            completedTickets = dlSystem.getCompletedTicketid();
        } catch (BasicException e) {
            throw new RuntimeException(e);
        }
        Window window = getWindow(this);
        JCompletedOrderDialog listDialog = JCompletedOrderDialog.newJDialog(window);

        listDialog.showTicketList(completedTickets, dlSystem, socketClient);
        refreshOrder();
    }

    private void onOK() {
        // add your code here
//        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
//        dispose();
    }

    public static void main(String[] args) {
        KitchenScreen dialog = new KitchenScreen();
//        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    @Override
    public Object getBean() {
        return this;
    }


    @Override
    public String getTitle() {
        String title = m_App.getAppUserView().getUser().getName();
        return title;
    }

    @Override
    public void activate() throws BasicException {

    }

    @Override
    public boolean deactivate() {
        return true;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }
}

