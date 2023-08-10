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

package com.openbravo.pos.printer;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.LocalRes;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.ticket.TicketInfo;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.openbravo.pos.util.QRCode;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author JG uniCenta
 */
public class TicketParser extends DefaultHandler {
    
    private static SAXParser m_sp = null;
    
    private DeviceTicket m_printer;
    private DataLogicSystem m_system;
    
    private StringBuilder text;
    
    private String bctype;
    private String bcposition;
    private int m_iTextAlign;
    private int m_iTextLength;
    private int m_iTextStyle;
    private int size;
    
    private StringBuilder m_sVisorLine;
    private int m_iVisorAnimation;
    private String m_sVisorLine1;
    private String m_sVisorLine2;
    
    private double m_dValue1;
    private double m_dValue2;
    private int attribute3;
    
    private int m_iOutputType;
    private static final int OUTPUT_NONE = 0;
    private static final int OUTPUT_DISPLAY = 1;
    private static final int OUTPUT_TICKET = 2;
    private static final int OUTPUT_FISCAL = 3;
    private DevicePrinter m_oOutputPrinter;   
    private DateFormat df= new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private Date today;
    private String cUser;
    private String ticketId;
    private String pickupId;
    
    
    /** Creates a new instance of TicketParser
     * @param printer
     * @param system */
    public TicketParser(DeviceTicket printer, DataLogicSystem system) {
        m_printer = printer;
        m_system = system;
        today = Calendar.getInstance().getTime(); 
    }
    
    /**
     *
     * @param sIn
     * @param ticket
     * @throws TicketPrinterException
     */
    public void printTicket(String sIn, TicketInfo ticket) throws TicketPrinterException {
        cUser=ticket.getName();
        ticketId=Integer.toString(ticket.getTicketId()); 
        pickupId=Integer.toString(ticket.getPickupId());
        
        if (ticket.getTicketId()==0){
            ticketId="No Sale";
        }
        if (ticket.getPickupId()==0){
            pickupId="No PickupId";
        }        
        printTicket(new StringReader(sIn));
    }
    
    /**
     *
     * @param sIn
     * @throws TicketPrinterException
     */
    public void printTicket(String sIn) throws TicketPrinterException { 
        printTicket(new StringReader(sIn));
    }

    /**
     *
     * @param in
     * @throws TicketPrinterException
     */
    public void printTicket(Reader in) throws TicketPrinterException  {
        
        try {
            
            if (m_sp == null) {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                m_sp = spf.newSAXParser();
            }
            m_sp.parse(new InputSource(in), this);
                        
        } catch (ParserConfigurationException ePC) {
            throw new TicketPrinterException(LocalRes.getIntString("exception.parserconfig") , ePC);
        } catch (SAXException eSAX) {
            throw new TicketPrinterException(LocalRes.getIntString("exception.xmlfile") , eSAX);
        } catch (IOException eIO) {
            throw new TicketPrinterException(LocalRes.getIntString("exception.iofile") , eIO);
        }
    }    
    
    @Override
    public void startDocument() throws SAXException {
        // inicalizo las variables pertinentes
        text = null;
        bctype = null;
        bcposition = null;
        m_sVisorLine = null;
        m_iVisorAnimation = DeviceDisplayBase.ANIMATION_NULL;
        m_sVisorLine1 = null;
        m_sVisorLine2 = null;
        m_iOutputType = OUTPUT_NONE;
        m_oOutputPrinter = null;
    }

    @Override
    public void endDocument() throws SAXException {
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{       
        String openDate = df.format(today);
        Date dNow = new Date();
        
        switch (m_iOutputType) {
        case OUTPUT_NONE:
        switch (qName) {
            case "opendrawer":
                m_printer.getDevicePrinter(readString(attributes.getValue("printer"), "1")).openDrawer();
                // Cashdrawer has been activated record the data in the table
                try {
                    m_system.execDrawerOpened(
                            //new Object[] {df.format(dNow),cUser,ticketId});
                             new Object[] {cUser,ticketId});
                } catch (BasicException ex) {}
                break;
            case "play":
                text = new StringBuilder();
                break;
            case "ticket":
                m_iOutputType = OUTPUT_TICKET;
                m_oOutputPrinter = m_printer.getDevicePrinter(readString(attributes.getValue("printer"), "1"));
                m_oOutputPrinter.beginReceipt();
                break;
            case "display":
                m_iOutputType = OUTPUT_DISPLAY;
                String animation = attributes.getValue("animation");
                if ("scroll".equals(animation)) {
                    m_iVisorAnimation = DeviceDisplayBase.ANIMATION_SCROLL;
                } else if ("flyer".equals(animation)) {
                    m_iVisorAnimation = DeviceDisplayBase.ANIMATION_FLYER;
                } else if ("blink".equals(animation)) {
                    m_iVisorAnimation = DeviceDisplayBase.ANIMATION_BLINK;
                } else if ("curtain".equals(animation)) {
                    m_iVisorAnimation = DeviceDisplayBase.ANIMATION_CURTAIN;
                } else { // "none"
                    m_iVisorAnimation = DeviceDisplayBase.ANIMATION_NULL;
                }
                m_sVisorLine1 = null;
                m_sVisorLine2 = null;                
                m_oOutputPrinter = null;
                break;
            case "fiscalreceipt":
                m_iOutputType = OUTPUT_FISCAL;
                m_printer.getFiscalPrinter().beginReceipt();
                break;
            case "fiscalzreport":
                m_printer.getFiscalPrinter().printZReport();
                break;
            case "fiscalxreport":
                m_printer.getFiscalPrinter().printXReport();
                break;
        }
            break;
        case OUTPUT_TICKET:
            if (null != qName)switch (qName) {
            case "logo":
                text = new StringBuilder();
                break;
            case "image":
                text = new StringBuilder();
                break;
            case "barcode":
                text = new StringBuilder();
                bctype = attributes.getValue("type");
                bcposition = attributes.getValue("position");
                break;
            case "qr-code":
                text = new StringBuilder();
                break;
            case "line":
                m_oOutputPrinter.beginLine(parseInt(attributes.getValue("size"), DevicePrinter.SIZE_0));
                break;
            case "text":
                text = new StringBuilder();
                m_iTextStyle = ("true".equals(attributes.getValue("bold")) 
                        ? DevicePrinter.STYLE_BOLD : DevicePrinter.STYLE_PLAIN)
                        | ("true".equals(attributes.getValue("underline")) 
                        ? DevicePrinter.STYLE_UNDERLINE : DevicePrinter.STYLE_PLAIN);

                m_iTextStyle = DevicePrinter.STYLE_BOLD;
                String sAlign = attributes.getValue("align");
                if (null == sAlign) {
                    m_iTextAlign = DevicePrinter.ALIGN_LEFT;
                } else switch (sAlign) {
                    case "right":
                        m_iTextAlign = DevicePrinter.ALIGN_RIGHT;
                        break;
                    case "center":
                        m_iTextAlign = DevicePrinter.ALIGN_CENTER;
                        break;
                    default:
                        m_iTextAlign = DevicePrinter.ALIGN_LEFT;
                        break;
                }
                m_iTextLength = parseInt(attributes.getValue("length"), 0);
                break;
            default:
                break;
        }
            break;
        case OUTPUT_DISPLAY:
            if (null != qName) switch (qName) {
            case "line":
                // line 1 or 2 of the display
                m_sVisorLine = new StringBuilder();
                break;
            case "line1":
                // linea 1 del visor
                m_sVisorLine = new StringBuilder();
                break;
            case "line2":
                // linea 2 del visor
                m_sVisorLine = new StringBuilder();
                break;
            case "text":
                text = new StringBuilder();
                String sAlign = attributes.getValue("align");
                if (null == sAlign) {
                    m_iTextAlign = DevicePrinter.ALIGN_LEFT;
                } else switch (sAlign) {
            case "right":
                m_iTextAlign = DevicePrinter.ALIGN_RIGHT;
                break;
            case "center":
                m_iTextAlign = DevicePrinter.ALIGN_CENTER;
                break;
            default:
                m_iTextAlign = DevicePrinter.ALIGN_LEFT;
                break;
        }
                m_iTextLength = parseInt(attributes.getValue("length"));
                break;
            default:
                break;
        }
            break;
        case OUTPUT_FISCAL:
            if (null != qName) 
                switch (qName) {
                    case "line":   
                        text = new StringBuilder();
                        m_dValue1 = parseDouble(attributes.getValue("price"));
                        m_dValue2 = parseDouble(attributes.getValue("units"), 1.0);
                        attribute3 = parseInt(attributes.getValue("tax"));
                        break; 
                    case "message":
                        text = new StringBuilder();
                        break;
                    case "total":    
                        text = new StringBuilder();
                        m_dValue1 = parseDouble(attributes.getValue("paid"));
                        break;
                    default:
                        break;
                }
            break;
        }
    } 
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        switch (m_iOutputType) {
        case OUTPUT_NONE:
            if ("play".equals(qName)) {
                try { 
                    AudioClip oAudio = Applet.newAudioClip(getClass().getClassLoader().getResource(text.toString()));
                    oAudio.play();
                } catch (Exception fnfe) {
                    //throw new ResourceNotFoundException( fnfe.getMessage() );
                }
                text = null;
            } 
            break;    
            
// Added 23.05.13 used by star TSP700 to print stored logo image JDL            
        case OUTPUT_TICKET:
            if ("logo".equals(qName)){
                    m_oOutputPrinter.printLogo();
                  // }        
            }
            if ("image".equals(qName)){
                try {
//                    BufferedImage image = ImageIO.read(getClass().getClassLoader().getResourceAsStream(text.toString()));
                    BufferedImage image = m_system.getResourceAsImage(text.toString());

                    if (image == null) {
                        image = decodeToImage(text.toString());
                    }
                    if (image != null) {
                        m_oOutputPrinter.printImage(image);
                    }
                    
                } catch (Exception fnfe) {
                    //throw new ResourceNotFoundException( fnfe.getMessage() );
                }
                text = null;
            } else if ("qr-code".equals(qName)) {
                m_oOutputPrinter.printImage(QRCode.printQRCode(text.toString()));
            }
            else if ("barcode".equals(qName)) {
                m_oOutputPrinter.printBarCode(bctype, bcposition, text.toString());
                text = null;
            } else if ("text".equals(qName)) {
                if (m_iTextLength > 0) {
                    switch(m_iTextAlign) {
                    case DevicePrinter.ALIGN_RIGHT:
                        m_oOutputPrinter.printText(m_iTextStyle, DeviceTicket.alignRight(text.toString(), m_iTextLength));
                        break;
                    case DevicePrinter.ALIGN_CENTER:
                        m_oOutputPrinter.printText(m_iTextStyle, DeviceTicket.alignCenter(text.toString(), m_iTextLength));
                        break;
                    default: // DevicePrinter.ALIGN_LEFT
                        m_oOutputPrinter.printText(m_iTextStyle, DeviceTicket.alignLeft(text.toString(), m_iTextLength));
                        break;
                    }
                } else {
                    m_oOutputPrinter.printText(m_iTextStyle, text.toString());
                }
                text = null;
            } else if ("line".equals(qName)) {
                m_oOutputPrinter.endLine();
            } else if ("ticket".equals(qName)) {
                m_oOutputPrinter.endReceipt();
                m_iOutputType = OUTPUT_NONE;
                m_oOutputPrinter = null;
            }
            break;
        case OUTPUT_DISPLAY:
            if ("line".equals(qName)) { // line 1 or 2 of the display
                if (m_sVisorLine1 == null) {
                    m_sVisorLine1 = m_sVisorLine.toString();
                } else {
                    m_sVisorLine2 = m_sVisorLine.toString();
                }
                m_sVisorLine = null;
            } else if ("line1".equals(qName)) { // linea 1 del visor
                m_sVisorLine1 = m_sVisorLine.toString();
                m_sVisorLine = null;
            } else if ("line2".equals(qName)) { // linea 2 del visor
                m_sVisorLine2 = m_sVisorLine.toString();
                m_sVisorLine = null;
            } else if ("text".equals(qName)) {
                if (m_iTextLength > 0) {
                    switch(m_iTextAlign) {
                    case DevicePrinter.ALIGN_RIGHT:
                        m_sVisorLine.append(DeviceTicket.alignRight(text.toString(), m_iTextLength));
                        break;
                    case DevicePrinter.ALIGN_CENTER:
                        m_sVisorLine.append(DeviceTicket.alignCenter(text.toString(), m_iTextLength));
                        break;
                    default: // DevicePrinter.ALIGN_LEFT
                        m_sVisorLine.append(DeviceTicket.alignLeft(text.toString(), m_iTextLength));
                        break;
                    }
                } else {
                    m_sVisorLine.append(text);
                }
                text = null;
            } else if ("display".equals(qName)) {
                m_printer.getDeviceDisplay().writeVisor(m_iVisorAnimation, m_sVisorLine1, m_sVisorLine2);        
                m_iVisorAnimation = DeviceDisplayBase.ANIMATION_NULL;                
                m_sVisorLine1 = null;
                m_sVisorLine2 = null;
                m_iOutputType = OUTPUT_NONE;
                m_oOutputPrinter = null;
            }
            break;
        case OUTPUT_FISCAL:
            if ("fiscalreceipt".equals(qName)) {
                m_printer.getFiscalPrinter().endReceipt();
                m_iOutputType = OUTPUT_NONE;
            } else if ("line".equals(qName)) {
                m_printer.getFiscalPrinter().printLine(text.toString(), m_dValue1, m_dValue2, attribute3);
                text = null;               
            } else if ("message".equals(qName)) {
                m_printer.getFiscalPrinter().printMessage(text.toString());
                text = null;               
            } else if ("total".equals(qName)) {
                m_printer.getFiscalPrinter().printTotal(text.toString(), m_dValue1);
                text = null;               
            }
            break;
        }          
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (text != null) {
            text.append(ch, start, length);
        }
    }
    
    private int parseInt(String sValue, int iDefault) {
        try {
            return Integer.parseInt(sValue);
        } catch (NumberFormatException eNF) {
            return iDefault;
        }
    }
    
    private int parseInt(String sValue) {
        return parseInt(sValue, 0);
    }
    
    private double parseDouble(String sValue, double ddefault) {
        try {
            return Double.parseDouble(sValue);
        } catch (NumberFormatException eNF) {
            return ddefault;
        }
    }
    
    private double parseDouble(String sValue) {
        return parseDouble(sValue, 0.0);
    }
    
    private String readString(String sValue, String sDefault) {
        if (sValue == null || sValue.equals("")) {
            return sDefault;
        } else {
            return sValue;
        }
    }
    private static BufferedImage decodeToImage(String imageString) {

        BufferedImage image = null;
        byte[] imageByte;
        try {
            Base64.Decoder decoder = Base64.getMimeDecoder();
            imageByte = decoder.decode(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;


    }    
}