package server.Connectivity;

import server.Server;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;

public class TcpManagerUpdateDB implements Serializable{
    private static final long serialVersionUID = 10l;
    private int portDB;
    private InetAddress ipDB;
    private ServerSocket serverSocketUpdateDB;

    public TcpManagerUpdateDB(Server server){
        try {
            serverSocketUpdateDB = new ServerSocket(0);
            serverSocketUpdateDB.setSoTimeout(7000);
        } catch (IOException e) {
            System.err.println("Couldn't create the Sockets. Closing...");
            server.close();
        }

        portDB = serverSocketUpdateDB.getLocalPort();
        ipDB = serverSocketUpdateDB.getInetAddress();
    }

    public InetAddress getIpDB() {
        return ipDB;
    }

    public int getPortDB() {
        return portDB;
    }

    public ServerSocket getServerSocketUpdateDB() {
        return serverSocketUpdateDB;
    }


}
