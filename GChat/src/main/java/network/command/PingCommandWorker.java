package network.command;

import controller.Controller;
import java.util.LinkedList;
import java.util.Random;

public class PingCommandWorker extends CommandWorker {    
    // number of ping commands to issue
    private final int count;
    
    // length of the payload to send
    private final int length;
    
    // timeout in milliseconds
    private final long timeout;
    
    // list of round-trip-times for the received responses
    private final LinkedList<Long> delays = new LinkedList<>();
    
    public PingCommandWorker(int count, int length, long timeout) {
        this.count = count;
        this.length = length;
        this.timeout = timeout;
    }
    
    @Override
    public void run() {
        for (int i=0; i<count; ++i) {
            String header = "/ping:" + Controller.getInstance().getId() + ":" + System.nanoTime() + ":";
            Controller.getInstance().send(header + buildPayload(length));
        }
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {}
        
        // compute the average round-trip-time delay
        long averageRtt = 0L;
        synchronized (delays) {
            for (long rtt : delays) {
                averageRtt += rtt;
            }
            averageRtt /= (delays.size() * 1000000L);
        }
        parent.onCommandExecutionResponse(this, "average rtt: " + averageRtt + "ms");
    }
    
    public String buildPayload(int size) {
        if (size <= 0) {
            return buildRandomPayload();
        }
        return buildFixedSizePayload(size);
    }
    
    public String buildRandomPayload() {
        return buildFixedSizePayload(1 + new Random().nextInt(1000));
    }
    
    public String buildFixedSizePayload(int size) {
        String payload = "";
        for (int i=0; i<size; ++i) {
            payload += ('a' + Math.random() * 25);
        }
        return payload;
    }

    @Override
    public void onReceiveMessage(String message) {}

    @Override
    public void onReceiveCommand(String message) {
        // only process ping responses
        if (!message.startsWith("/ping-ack")) {
            return;
        }
        // only process responses to ping commands sent by this client
        String[] args = message.split(":");
        if (!args[1].equals(Controller.getInstance().getId())) {
            return;
        }
        
        // compute the round-trip-time delay
        Long delay = System.nanoTime() - Long.parseLong(args[2]);
        synchronized (delays) {
            delays.add(delay);
        }
    }

    @Override
    public void onReceiveCommandExecutionResponse(String message) {}
}
