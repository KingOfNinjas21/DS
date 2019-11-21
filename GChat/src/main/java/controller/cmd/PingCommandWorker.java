package controller.cmd;

import controller.Controller;
import network.NetworkInputHandler;

public class PingCommandWorker extends Thread implements NetworkInputHandler {
    // number of ping commands to issue
    private final int count;
    
    // length of the payload to send
    private final int length;
    
    // timeout in milliseconds
    private final long timeout;
    
    public PingCommandWorker(int count, int length, long timeout) {
        this.count = count;
        this.length = length;
        this.timeout = timeout;
    }
    
    public void run() {
        for (int i=0; i<count; ++i) {
            Controller.getInstance().send("/ping" + System.nanoTime());
        }
    }

    @Override
    public void onReceiveMessage(String message) {}

    @Override
    public void onReceivePingAck(String message) {
        
    }
}
