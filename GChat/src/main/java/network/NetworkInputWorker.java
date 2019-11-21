package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.LogFileWriter;

public class NetworkInputWorker extends Thread {
    private static final Logger logger = Logger.getLogger(NetworkInputHandler.class.getName());
    private final int MAX_MESSAGE_LENGTH = 1024;
    private final NetworkInputHandler handler;
    private final MulticastSocket socket;
    private final InetAddress group;
    private final int port;
    
    public NetworkInputWorker(
        NetworkInputHandler handler,
        MulticastSocket socket,
        InetAddress group,
        int port
    ) {
        logger.addHandler(LogFileWriter.getInstance());
        this.handler = handler;
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

                if (message.startsWith("/ping-ack")) {
                    handler.onReceivePingAck(message);
                } else if (!message.isEmpty()) {
                    handler.onReceiveMessage(message);
                }
            } catch (IOException e) {
                close();
            }
        }
    }
    
    public void close() {
        logger.log(Level.INFO, "Socket closed");
        socket.close();
    }
}
