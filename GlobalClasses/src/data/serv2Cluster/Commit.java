package data.serv2Cluster;

import java.io.Serializable;

public class Commit implements Serializable {
    private static final long SerialVersionUID = 10l;
    private int db;
    private String serverID;



    public int getDb() {
        return db;
    }

    public String getServerID() {
        return serverID;
    }

    public Commit(int db, String id) {
        this.db = db;
        serverID = id;
    }

    public void setDb(int db) {
        this.db = db;
    }
}
