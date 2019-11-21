package network;

public interface NetworkInputHandler {
    public void onReceiveMessage(String message);
    public void onReceiveCommand(String message);
    public void onReceiveCommandExecutionResponse(String message);
}
