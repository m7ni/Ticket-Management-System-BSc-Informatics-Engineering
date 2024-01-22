package data.cli2Serv.seriObjects;

import java.io.Serializable;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;

public class ServersInfo implements Serializable, Comparable<ServersInfo> {
    private static final long serialVersionUID = 10l;

    private int ServPortRegular;
    private int ServPortAsync;
    private InetAddress ServIp;
    private String IdServer;
    private int activeConnections = 0;
    transient private boolean available;
    transient private Instant lastHB;

    public Instant getLastHB() {
        return lastHB;
    }

    public void setLastHB(Instant lastHB) {
        this.lastHB = lastHB;
    }

    @Override
    public int hashCode() {
        return IdServer.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;

        if(!(obj instanceof ServersInfo aux) )
            return false;

        return this.hashCode() == aux.hashCode();
    }

    public static ServersInfo getDummyServerInfo(String id){
        return new ServersInfo(id);
    }

    private ServersInfo(String id){
        IdServer = id;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public int compareTo(ServersInfo sv) {
        if(this.getActiveConnections() == sv.getActiveConnections())
            return 0;
         else if(this.getActiveConnections() > sv.getActiveConnections())
             return 1;

         return -1;
    }

    public ServersInfo(int servPortRegular,int servPortAsync, InetAddress servIp, String id, int activeConnections) {
        this.ServPortRegular = servPortRegular;
        this.ServPortAsync = servPortAsync;
        ServIp = servIp;
        IdServer = id;
        this.activeConnections =activeConnections;
        available = true;
        lastHB = Instant.now();
    }

    public ServersInfo(int servPort, InetAddress servIp){
        ServPortRegular = servPort;
        ServIp = servIp;
    }

    public int getActiveConnections(){
        return activeConnections;
    }

    public void setActiveConnections(int activeConnections) {
        this.activeConnections = activeConnections;
    }

    public void addConnection(){
        activeConnections++;
    }

    public void deleteConnection(){
        activeConnections--;
    }

    public String getIdServer() {
        return IdServer;
    }

    public void setIdServer(String idServer) {
        IdServer = idServer;
    }

    public int getServPortRegular() {
        return ServPortRegular;
    }

    public void setServPortRegular(int servPortRegular) {
        ServPortRegular = servPortRegular;
    }

    public InetAddress getServIp() {
        return ServIp;
    }

    public void setServIp(InetAddress servIp) {
        ServIp = servIp;
    }

    public int getServPortAsync() {
        return ServPortAsync;
    }

    public void setServPortAsync(int servPortAsync) {
        ServPortAsync = servPortAsync;
    }

}
