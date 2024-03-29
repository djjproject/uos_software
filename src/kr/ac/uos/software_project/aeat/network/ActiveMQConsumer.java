/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kr.ac.uos.software_project.aeat.network;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import kr.ac.uos.software_project.aeat.view.MessagePanel;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author comkeen
 */

public class ActiveMQConsumer implements ExceptionListener {

    private Connection connection;
    private MessagePanel messagePanel;
    
    public ActiveMQConsumer(String address, MessagePanel messagePanel) {
        init(address, messagePanel);
        this.messagePanel = messagePanel;
    }
    
    @Override
    public void onException(JMSException jmse) {
        System.out.println("JMS Exception occured. SHutting down client");
    }

    private void init(String address, MessagePanel messagePanel) {
        try {
            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(address);

            // Create a Connection
            this.connection = connectionFactory.createConnection();
            connection.start();

            connection.setExceptionListener(this);
        } catch (JMSException e) {
            System.out.print("연결오류가 발생하였습니다.");
            messagePanel.setTextArea("연결오류가 발생하였습니다.");
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }
    
    public void setConsumerDestination(String destinationName) {
        
        try {
            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            // Create the destination (Topic or Queue)
            Destination destination = session.createQueue(destinationName);

            // Create a MessageConsumer from the Session to the Topic or Queue
            MessageConsumer consumer = session.createConsumer(destination);

            // Wait for a message
            MessageListener listener = new MessageListener() {
                public void onMessage(Message message) {
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        try {
                            messagePanel.setTextArea(messagePanel.getTextArea()+'\n'+textMessage.getText());
                            System.out.println("Received Message:\n" + textMessage.getText());
                            
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            // Register to MessageListener
            consumer.setMessageListener(listener);
        } catch (JMSException ex) {
            Logger.getLogger(ActiveMQConsumer.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
       
    public void closeConnection() {
        try {
            connection.close();
        } catch (JMSException ex) {
            Logger.getLogger(ActiveMQProducer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}


