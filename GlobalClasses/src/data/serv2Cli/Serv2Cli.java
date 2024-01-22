package data.serv2Cli;

import java.io.Serializable;

public abstract class Serv2Cli implements Serializable {
    private static final long serialVersionUID = 10l;
    RequestType rt;

    public RequestType getRt() {
        return rt;
    }

    public Serv2Cli(RequestType rt) {
        this.rt = rt;
    }

    public enum RequestType{
        LIST,
        UPDATE,
    }

    public enum SubType{
        SHOW_NR,SHOWS_CLIENT,
        NOT_PAID_RESERVATIONS,
        PAID_RESERVATIONS,
        USER_TYPE
    }
}