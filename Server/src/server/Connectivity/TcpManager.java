package server.Connectivity;

import data.cli2Serv.seriObjects.ServersInfo;
import data.serv2Cluster.Heartbeat;
import server.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class TcpManager {

    private Server server;
    private int portRegular;
    private int portAsync;
    private ServerSocket serverSocketRegular;
    private ServerSocket serverSocketAsync;
    private Heartbeat heartbeat;
    public TcpManager(Server server, Heartbeat heartbeat) throws UnknownHostException {
        this.server = server;
        this.heartbeat= heartbeat;
        create();
    }
    public void close(){
            try {
                if(serverSocketRegular!=null) serverSocketRegular.close();
                if(serverSocketAsync!=null)    serverSocketAsync.close();
            } catch (IOException e) {
                System.err.println("Error closing sockets");
            }
        }

    public void create() throws UnknownHostException {
        try {
            serverSocketRegular = new ServerSocket(0);
            serverSocketRegular.setSoTimeout(7000);
            serverSocketAsync = new ServerSocket(0);
            serverSocketAsync.setSoTimeout(7000);
        } catch (IOException e) {
            System.err.println("Couldn't create the Sockets. Closing...");
          server.close();
        }

        portRegular = serverSocketRegular.getLocalPort();
        portAsync = serverSocketAsync.getLocalPort();
        System.out.println(portRegular);
        System.out.println(portAsync);
        synchronized (this){
            heartbeat.setPortTCPRegular(portRegular);
            heartbeat.setPortTCPAsync(portAsync);
            server.setSi(new ServersInfo(portRegular,portAsync, InetAddress.getByName("127.0.0.1"), server.getServerMulticastId(),0));
            server.getServerList().getDataservers().add(server.getSi());
        }
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public ServerSocket getServerSocketRegular() {
        return serverSocketRegular;
    }

    public ServerSocket getServerSocketAsync() {
        return serverSocketAsync;
    }


}