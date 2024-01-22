package data.cli2Serv;

import data.cli2Serv.seriObjects.Show;

public class C2SAdmin extends Cli2Serv{
    private Cli2Serv.SubType sb;
    private Show show;
    private int id;

    public C2SAdmin(RequestType requestType,SubType sb, String username,Show show) {
        super(requestType, username);
        this.show = show;
        this.sb = sb;
    }

    public C2SAdmin(RequestType requestType,SubType sb, String username,int id) {
        super(requestType, username);
        this.id = id;
        this.sb = sb;
    }

    public int getId() {
        return id;
    }

    public SubType getSb() {
        return sb;
    }

    public Show getShow() {
        return show;
    }
}
