package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.command.CommandWorker;
import util.LogFileWriter;

public class NetworkInputWorker extends Thread {
    private static final Logger logger = Logger.getLogger(NetworkInputHandler.class.getName());
    private final int MAX_MESSAGE_LENGTH = 1024;
    private final MulticastSocket socket;
    private final InetAddress group;
    private final int port;
    
    private final LinkedList<NetworkInputHandler> handlers = new LinkedList<>();
    
    public NetworkInputWorker(MulticastSocket socket, InetAddress group, int port) {
        logger.addHandler(LogFileWriter.getInstance());
        this.socket = socket;
        this.group = group;
        this.port = port;
    }
    
    @Override
    public void run() {
        while (!socket.isClosed()) {
            byte[] buffer = new byte[MAX_MESSAGE_LENGTH];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            try {
                socket.receive(packet);
                String message = new String(buffer, 0, packet.getLength(), "UTF-8");
                if (message.startsWith("/")) {
                    synchronized (handlers) {
                        for (NetworkInputHandler handler : handlers) {
                            handler.onReceiveCommand(message);
                        }
                    }
                } else if (!message.isEmpty()) {
                    synchronized (handlers) {
                        for (NetworkInputHandler handler : handlers) {
                            handler.onReceiveMessage(message);
                        }
                    }
                }
            } catch (IOException e) {
                close();
            }
        }
    }
    
    public void close() {
        socket.close();
        logger.log(Level.INFO, "Socket closed");
    }
    
    public void runCommand(CommandWorker worker) {
        addHandler(worker);
        worker.setParent(this);
        worker.start();
    }
    
    public void onCommandExecutionResponse(CommandWorker worker, String response) {
        removeHandler(worker);
        synchronized (handlers) {
            for (NetworkInputHandler handler : handlers) {
                handler.onReceiveCommandExecutionResponse(response);
            }
        }
    }
    
    public void addHandler(NetworkInputHandler handler) {
        synchronized (handlers) {
            handlers.add(handler);
        }
    }
    
    public void removeHandler(NetworkInputHandler handler) {
        synchronized (handlers) {
            handlers.remove(handler);
        }
    }
}
