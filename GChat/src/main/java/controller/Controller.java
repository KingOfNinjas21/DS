package controller;

import network.command.PingCommandWorker;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.NetworkInputHandler;
import network.NetworkInputWorker;
import util.LogFileWriter;

public class Controller implements NetworkInputHandler {
    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    private static final Controller INSTANCE = new Controller();
    private ControllerState state = ControllerState.Login;
    private final LinkedList<ControllerListener> listeners = new LinkedList<>();
    
    private NetworkInputWorker networkInputWorker = null;
    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private String nickname;
    private String id;
    
    private Controller() {
        logger.addHandler(LogFileWriter.getInstance());
    }
    
    public static Controller getInstance() {
        return INSTANCE;
    }

    public boolean connect(String nickname, String group, int port) {
        this.nickname = nickname;
        this.id = nickname + new Random().nextLong();
        try {
            this.group = InetAddress.getByName(group);
            this.port = port;
            this.socket = new MulticastSocket(port);
            this.socket.joinGroup(this.group);
            
            networkInputWorker = new NetworkInputWorker(socket, this.group, port);
            networkInputWorker.addHandler(this);
            networkInputWorker.start();
            
            sendMessage(nickname + " joined the chat");
            logger.log(Level.INFO, "Connected to group " + group + " on port " + port);
        } catch (IOException e) {
            logger.log(Level.INFO, "Unable to connect to group " + group + " on port " + port);
            return false;
        }
        setState(ControllerState.Connected);
        return true;
    }
    
    public void disconnect() {
        try {
            sendMessage(nickname + " left the chat");
            socket.leaveGroup(group);
            socket.close();
            networkInputWorker.close();
            logger.log(Level.INFO, "Connection closed");
        } catch (Exception e){
            logger.log(Level.INFO, "Error while leaving the group: " + e.getMessage());
        }
        setState(ControllerState.Finnished);
    }
    
    public void send(String message) {
        if (!message.startsWith("/")) {
            message = nickname + ": " + message;
        }
        if (message.startsWith("/ping ")) {
            try {
                String args[] = message.substring(6).split(" ");
                int count = Integer.parseInt(args[0]);
                int length = Integer.parseInt(args[1]);
                networkInputWorker.runCommand(new PingCommandWorker(count, length, 1000));
            } catch (Exception e) {
                logger.log(Level.WARNING, "/ping syntax error, expected 2 integer arguments");
            }
        } else {
            sendMessage(message);
        }
    }
    
    private void sendMessage(String message) {
        logger.log(Level.INFO, "Sending message: " + message);
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, group, port);
        try {
            socket.send(packet);
            logger.log(Level.INFO, "Message sent successfully");
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.log(Level.INFO, "Error while sending message");
        }
    } 

    @Override
    public void onReceiveMessage(String message) {
        logger.log(Level.INFO, "Received message: " + message);
        synchronized (listeners) {
            for (ControllerListener listener : listeners) {
                listener.onReceiveMessage(message);
            }
        }
    }

    @Override
    public void onReceiveCommand(String message) {
        logger.log(Level.INFO, "Received command: " + message);
        if (message.startsWith("/ping:")) {
            String answer = "/ping-ack" + message.substring(5);
            send(answer);
        }
    }
    
    @Override
    public void onReceiveCommandExecutionResponse(String message) {
        logger.log(Level.INFO, "Received command execution response: " + message);
        synchronized (listeners) {
            for (ControllerListener listener : listeners) {
                listener.onReceiveMessage(message);
            }
        }
    }
    
    public synchronized ControllerState getState() {
        return state;
    }
    
    public synchronized void setState(ControllerState state) {
        this.state = state;
        logger.log(Level.INFO, "Controller State changed to " + state);
    }
    
    public void addListener(ControllerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(ControllerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    public String getId() {
        return id;
    }
}
