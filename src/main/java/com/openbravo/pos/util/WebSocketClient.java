/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.openbravo.pos.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.tyrus.client.ClientManager;

/**
 *
 * @author Masha-0222
 */
@ClientEndpoint
@Slf4j
public class WebSocketClient {
    private Session m_session;

    public WebSocketClient() {
    
    }

    @OnOpen
    public void onOpen (javax.websocket.Session session) {
        System.out.println("[CLIENT]: Connection established..... \n[CLIENT]: Session ID: " + session.getId() );
    }

    @OnClose
    public void onClose (javax.websocket.Session session, CloseReason closeReason) {
        System.out.println("[CLIENT]: Session " + session.getId() + " close, because " + closeReason);
    }

    @OnError
    public void onError (javax.websocket.Session session, Throwable err) {
        System.out.println("[CLIENT]: Error!!!!!, Session ID: " + session.getId() + ", " + err.getMessage());
    }

    public void sendMessage(String message) {
        if(m_session != null) {
            System.out.println("[CLIENT]: Send message to Client_Session: " + m_session.getId() + " Message: " + message );
            try {
                m_session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeConnection() {
        if(m_session != null) {
            try {
                m_session.close();
            } catch (IOException e) {}
        }
    }

    public void setConnection(MessageHandler myHandler) {
        ClientManager clientManager = ClientManager.createClient();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxSessionIdleTimeout(0);  // 0 means no timeout
        URI uri = null;
        try {
            uri = new URI("ws://192.168.0.12:9090/socket/server");
            m_session = clientManager.connectToServer(WebSocketClient.class, uri);
            m_session.removeMessageHandler(myHandler);
            m_session.addMessageHandler(myHandler);
            m_session.setMaxIdleTimeout(2147483647);
        } catch (URISyntaxException | DeploymentException e) {
            System.out.println("Websocket Exception: " + e.getMessage());
        }
    }
}