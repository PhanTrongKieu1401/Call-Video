package server;

import java.net.InetAddress;

public class DeviceInfo {
    private InetAddress clientAddress;
    private int clientPort;

    public DeviceInfo(InetAddress clientAddress, int clientPort) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }
}
