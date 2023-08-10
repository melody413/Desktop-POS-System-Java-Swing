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

/*
 * uniCenta Feb 2017
 * Original code supplied Ryan Airey Feb 2017
*/

package com.openbravo.pos.scale;

import gnu.io.*;
import java.io.*;
import java.util.TooManyListenersException;

public class ScaleAvery implements Scale, SerialPortEventListener {
   
    private CommPortIdentifier m_PortIdPrinter;
    private SerialPort m_CommPortPrinter;  
    
    private String m_sPortScale;
    private OutputStream m_out;
    private InputStream m_in;

    private static final int SCALE_READY = 0;
    private static final int SCALE_READING = 1;
    private static final int SCALE_READINGDECIMALS = 2;
    private static int SCALE_NOMORE = 1;
    
    private double m_dWeightBuffer;
    private double m_dWeightDecimals;
    private int m_iStatusScale;
        
    public ScaleAvery(String sPortPrinter) {
        m_sPortScale = sPortPrinter;
        m_out = null;
        m_in = null;
        
        m_iStatusScale = SCALE_READY; 
        m_dWeightBuffer = 0.0;
        m_dWeightDecimals = 1.0;
      
    }
    
    @Override
    public Double readWeight() {
        synchronized(this) {
            if (m_iStatusScale != SCALE_READY) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                }
                if (m_iStatusScale != SCALE_READY) {

                    m_iStatusScale = SCALE_READY;
                }
            }
            
            m_dWeightBuffer = 0.0;
            m_dWeightDecimals = 1.0;
            
            // Send command to get data from scale
            write(new byte[] {0x0057});
            flush();
            write(new byte[] {0x000D});
                        try {
                wait(1000);
            } catch (InterruptedException e) {
            }
            
            if (m_iStatusScale == SCALE_READY) {
                double dWeight = m_dWeightBuffer / m_dWeightDecimals;
//            System.out.println(" ----- ScaleAvery : readWeight --- if   ------ "+dWeight);
                m_dWeightBuffer = 0.0;
                m_dWeightDecimals = 1.0;
                return dWeight;
            } else {
                m_iStatusScale = SCALE_READY;
                m_dWeightBuffer = 0.0;
                m_dWeightDecimals = 1.0;
                return 0.0;
            }
        }
    }
    
    private void flush() {
        try {
            m_out.flush();
        } catch (IOException e) {
        }        
    }
    
    private void write(byte[] data) {
        try {  
            
            
            if (m_out == null) {
                
                m_PortIdPrinter = CommPortIdentifier.getPortIdentifier(m_sPortScale);                  
                m_CommPortPrinter = (SerialPort) m_PortIdPrinter.open("PORTID", 2000);      
                m_out = m_CommPortPrinter.getOutputStream();  
                m_in = m_CommPortPrinter.getInputStream();
                
                m_CommPortPrinter.addEventListener(this);
                m_CommPortPrinter.notifyOnDataAvailable(true);
                
                m_CommPortPrinter.setSerialPortParams(9600, 
                        SerialPort.DATABITS_7, 
                        SerialPort.STOPBITS_1, 
                        SerialPort.PARITY_EVEN);
            }
            m_out.write(data);
            
        } catch (NoSuchPortException | PortInUseException 
                | UnsupportedCommOperationException | TooManyListenersException 
                | IOException e) {
//        System.out.println(" Exception While Writing the Data, Might be Serial Dll Missing or COM Port not Supported, Or COM Port already in use"); 
        }        
    }
    
    @Override
    public void serialEvent(SerialPortEvent e) {

	switch (e.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
            try {
                while (m_in.available() > 0) {
                    int b = m_in.read();
                    if (b==0x0003 || b==3) {
                        synchronized (this) {
                            SCALE_NOMORE = 1;
                            m_iStatusScale = SCALE_READY;
                            notifyAll(); }
                    } else if (b == 0x004C || b==76) {
                        synchronized (this) {
                            SCALE_NOMORE = 0; }
                    } else if (SCALE_NOMORE==0){
                        m_iStatusScale = SCALE_READY;
                    } else if (b > 0x002F 
                            && b < 0x003A 
                            && SCALE_NOMORE==1 
                            || b == 0x002E) {
                        synchronized(this) {
                        if (m_iStatusScale == SCALE_READY) {
                            m_dWeightBuffer = 0.0;
                            m_dWeightDecimals = 1.0;
                            m_iStatusScale = SCALE_READING;
                        }
                        if (b == 0x002E) {
                            m_iStatusScale = SCALE_READINGDECIMALS;
                        } else {
                            m_dWeightBuffer = m_dWeightBuffer * 10.0 + b - 0x0030;
                            if (m_iStatusScale == SCALE_READINGDECIMALS) {
                                m_dWeightDecimals *= 10.0;
                            }
                        }
                        }
                    } else {
                        m_dWeightBuffer = 0.0;
                        m_dWeightDecimals = 1.0;
                        m_iStatusScale = SCALE_READY;
                    }
                }
            } catch (IOException eIO) {
//                    System.out.println(" Error At serialEvent due to unknowen response data from scale "); 
            }
                break;
            }
        }
    }