package data.cli2Serv;

import java.io.Serializable;

public abstract class Cli2Serv implements Serializable {
    private static final long serialVersionUID = 10l;
    private RequestType requestType;
    private String username;
    private String password;

    public Cli2Serv(RequestType requestType,String username) {
        this.requestType = requestType;
        this.username = username;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public String getUsername() {
        return username;
    }

    public enum RequestType{
        REGISTER, LOGIN, LOGOUT,
        EDIT_USERNAME, EDIT_NAME, EDIT_PASSWORD,
        RESERVATIONS,
        CONSULT_SHOWS,
        DELETE_RESERVATION,
        ADMIN_SHOW,
    }

    public enum SubType{
        CONSULT_NOT_PAID_RESERVATION, CONSULT_PAID_RESERVATION,ADD_RESERVATION,CONSULT_CLIENT_RESERVATION,
        PAY_RESERVATION, DELETE_RESERVATION, RESERVATION_VALIDATION,
        INSERT_SHOW,MAKE_SHOW_AVAILABLE,DELETE_SHOW,FILTERS,NO_FILTERS,INVISIBLE
    }
}
