package data.cli2Serv;

import data.cli2Serv.seriObjects.Seat;

public class C2SReservation extends Cli2Serv{
    private static final long serialVersionUID = 10l;
    private SubType sb;
    private int idShow;
    private int pay;
    private Seat seat;
    private int idReservaton;

    public SubType getSb() {
        return sb;
    }

    public int getIdReservaton() {
        return idReservaton;
    }

    public void setIdReservaton(int idReservaton) {
        this.idReservaton = idReservaton;
    }

    public int getPay() {
        return pay;
    }

    public Seat getSeat() {
        return seat;
    }

    public C2SReservation(RequestType requestType, SubType sb, String username) {
        super(requestType,username);
        this.sb = sb;
    }

    public C2SReservation(RequestType requestType, SubType sb, int idReservation, String username) {
        super(requestType,username);
        this.sb = sb;
        this.idReservaton = idReservation;
    }

    public C2SReservation(RequestType requestType, SubType sb,Seat seat,String username,int idShow) {
         super(requestType,username);
         this.seat = seat;
         this.sb = sb;
         this.idShow = idShow;
     }
}
