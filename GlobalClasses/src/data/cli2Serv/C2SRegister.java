package data.cli2Serv;

import static data.cli2Serv.Cli2Serv.RequestType.REGISTER;

public class C2SRegister extends Cli2Serv{
    private static final long serialVersionUID = 1L;
    private String username,name,password;

    public String getUsername() {
        return username;
    }
    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }

    public C2SRegister(String username, String name, String password) {
        super(REGISTER,username);
        this.username = username;
        this.name = name;
        this.password = password;
    }
}
