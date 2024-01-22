package data.serv2Cluster;

import server.Connectivity.TcpManagerUpdateDB;

import java.io.Serializable;
import java.net.InetAddress;
import java.time.Instant;

//sao para mandar por multicast aos restantes servers para eles saberem se precisam de dar uptdate a base de dados ou nao
public class Heartbeat implements Serializable{
    private static final long SerialVersionUID = 10l;
    private String IdServer;
    private int portTCPRegular;
    private int portTCPAsync;
    private InetAddress ipTCP;
    private int portTCPDB;
    private InetAddress ipTCPDB;
    private boolean available;
    private int DataBaseVersion;
    private int activeConnections;
    private Instant lastHBTime;
    private TcpManagerUpdateDB dataUpdateDB;


    public int getPortTCPDB() {
        return portTCPDB;
    }

    public InetAddress getIpTCPDB() {
        return ipTCPDB;
    }

    public Heartbeat(InetAddress ipTCP, boolean available, int dataBaseVersion, String idServer, int portTCPDB, InetAddress ipTCPDB) {
        this.ipTCP = ipTCP;
        this.available = available;
        this.DataBaseVersion = dataBaseVersion;
        this.activeConnections = 0;
        this.IdServer = idServer;
        this.portTCPDB= portTCPDB;
        this.ipTCPDB=ipTCPDB;
        this.dataUpdateDB = dataUpdateDB;
    }

    public int getPortTCPAsync() {
        return portTCPAsync;
    }

    public void setPortTCPAsync(int portTCPAsync) {
        this.portTCPAsync = portTCPAsync;
    }

    public Heartbeat() {}

    public TcpManagerUpdateDB getDataUpdateDB() {
        return dataUpdateDB;
    }

    public Instant getLastHBTime() {
        return lastHBTime;
    }

    public void setLastHBTime(Instant lastHBTime) {
        this.lastHBTime = lastHBTime;
    }

    public String getIdServer() {
        return IdServer;
    }

    public void setIdServer(String idServer) {
        IdServer = idServer;
    }

    public int getPortTCPRegular() {
        return portTCPRegular;
    }

    public void setPortTCPRegular(int portTCPRegular) {
        this.portTCPRegular = portTCPRegular;
    }

    public InetAddress getIpTCP() {
        return ipTCP;
    }

    public void setIpTCP(InetAddress ipTCP) {
        this.ipTCP = ipTCP;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getDataBaseVersion() {
        return DataBaseVersion;
    }

    public void setDataBaseVersion(int dataBaseVersion) {
        DataBaseVersion = dataBaseVersion;
    }

    public int getActiveConnections() {
        return activeConnections;
    }

    public void addActiveConnection(){
        activeConnections++;

    }

    public void removeActiveConnection(){
        activeConnections--;
    }

    public void setActiveConnections(int activeConnections) {
        this.activeConnections = activeConnections;
    }
}
