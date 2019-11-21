package controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import network.NetworkInputHandler;
import util.LogFileWriter;

public class Controller implements NetworkInputHandler {
    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    private static final Controller INSTANCE = new Controller();
    
    private Controller() {
        logger.addHandler(LogFileWriter.getInstance());
    }
    
    public static Controller getInstance() {
        return INSTANCE;
    }
    
    public void send(String message) {
        if (message.startsWith("/")) {
            sendPing();
        } else {
            sendMessage(message);
        }
    }
    
    private void sendMessage(String message) {
        logger.log(Level.INFO, "Sending message: " + message);
        
        
    }
    
    private void sendPing() {
        logger.log(Level.INFO, "Sending ping");
        
        
    }

    @Override
    public void onReceiveMessage(String message) {
        logger.log(Level.INFO, "Received message: " + message);
        
        // update GUI
        
    }

    @Override
    public void onReceivePingAck(String message) {
        logger.log(Level.INFO, "Received ping response: " + message);
        
        // compute response time
        
    }
}
