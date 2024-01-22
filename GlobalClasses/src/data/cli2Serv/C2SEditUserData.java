package data.cli2Serv;

public class C2SEditUserData extends Cli2Serv{
    private static final long serialVersionUID = 1L;
    private String change;


    public C2SEditUserData(RequestType requestType,String username, String password, String change) {
        super(requestType,username);
        this.change = change;
    }

    public String getChange() {
        return change;
    }
}
