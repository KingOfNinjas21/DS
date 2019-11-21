package network.command;

import network.NetworkInputHandler;
import network.NetworkInputWorker;

public abstract class CommandWorker extends Thread implements NetworkInputHandler {
    protected NetworkInputWorker parent;

    public void setParent(NetworkInputWorker parent) {
        this.parent = parent;
    }
}
