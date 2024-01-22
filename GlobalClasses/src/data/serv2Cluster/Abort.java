package data.serv2Cluster;

import java.io.Serializable;

public class Abort implements Serializable {
    private static final long SerialVersionUID = 10l;
    private int version;

    public Abort(int version) {
        this.version = version;
    }
}
