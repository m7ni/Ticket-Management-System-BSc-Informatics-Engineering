package data.serv2Cluster;

import data.cli2Serv.Cli2Serv;
import java.io.Serializable;
import java.util.ArrayList;

public class Prepare <T> implements Serializable {
    private static final long SerialVersionUID = 10l;
    private int port;
    private int version;
    private ArrayList<T> data;
    private Cli2Serv.RequestType type;
    private Cli2Serv.SubType sb;
    private String idServerOrigin;
    private String ip;

    public String getIdServerOrigin() {
        return idServerOrigin;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setIdServerOrigin(String idServerOrigin) {
        this.idServerOrigin = idServerOrigin;
    }

    public Prepare(int port) {
        this.port = port;
    }


    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public ArrayList<T> getData() {
        return data;
    }

    public void setData(ArrayList<T> data) {
        this.data = data;
    }

    public Cli2Serv.RequestType getType() {
        return type;
    }

    public void setType(Cli2Serv.RequestType type) {
        this.type = type;
    }

    public Cli2Serv.SubType getSb() {
        return sb;
    }

    public void setSb(Cli2Serv.SubType sb) {
        this.sb = sb;
    }
}
