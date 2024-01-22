package server.Connectivity;

import server.Server;

import java.io.IOException;
import java.net.*;

public class MultiCast {
    private Server server;
    protected MulticastSocket multicastSocket;
    private int port;
    private NetworkInterface nif;
    InetAddress group;

    public MultiCast(Server server) throws IOException {
        port = 4004;
        group = InetAddress.getByName("239.39.39.39");
        this.server = server;
        nif =NetworkInterface.getByName("127.0.0.1");
        multicastSocket = new MulticastSocket(port);
        multicastSocket.joinGroup(new InetSocketAddress(group, port), nif);
        multicastSocket.setSoTimeout(7000);
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public MulticastSocket getMulticastSocket() {
        return multicastSocket;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getGroup() {
        return group;
    }

}
