package network;

public interface NetworkInputHandler {
    public void onReceiveMessage(String message);
    public void onReceivePingAck(String message);
}
