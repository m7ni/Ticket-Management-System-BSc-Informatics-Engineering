package data.cli2Serv;

import static data.cli2Serv.Cli2Serv.RequestType.LOGOUT;

public class C2Slogout extends Cli2Serv{
    private static final long serialVersionUID = 1L;
    private String username;

    public C2Slogout( String username) {
        super(LOGOUT,username);
        this.username = username;
    }

    public String getUsername(){
        return username;
    }
}
