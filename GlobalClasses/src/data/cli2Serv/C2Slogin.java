
package data.cli2Serv;

import static data.cli2Serv.Cli2Serv.RequestType.LOGIN;

public class C2Slogin extends Cli2Serv{
    private static final long serialVersionUID = 1L;
    private String username,password;

    public C2Slogin( String username, String password) {
        super(LOGIN,username);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


}
