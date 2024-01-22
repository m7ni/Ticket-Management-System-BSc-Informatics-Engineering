package data.serv2Cli;

import data.cli2Serv.seriObjects.Reservation;
import data.cli2Serv.seriObjects.Show;
import db.UserType;

import java.util.ArrayList;
import java.util.HashMap;

public class S2CUpdateData extends Serv2Cli{
    private HashMap<Serv2Cli.SubType, ArrayList<Show>> map;
    private ArrayList<Reservation> PaidReservations;
    private ArrayList<Reservation> NotPaidReservations;
    private SubType sb;
    private UserType ut;
    private boolean refresh;

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public S2CUpdateData() {
        super(RequestType.UPDATE);
    }

    public UserType getUt() {
        return ut;
    }

    public void setUt(UserType ut) {
        this.ut = ut;
    }

    public SubType getSb() {
        return sb;
    }

    public void setSb(SubType sb) {
        this.sb = sb;
    }

    public HashMap<SubType, ArrayList<Show>> getMap() {
        return map;
    }

    public void setMap(HashMap<SubType, ArrayList<Show>> map) {
        this.map = map;
    }

    public ArrayList<Reservation> getPaidReservations() {
        return PaidReservations;
    }

    public void setPaidReservations(ArrayList<Reservation> paidReservations) {
        PaidReservations = paidReservations;
    }

    public ArrayList<Reservation> getNotPaidReservations() {
        return NotPaidReservations;
    }

    public void setNotPaidReservations(ArrayList<Reservation> notPaidReservations) {
        NotPaidReservations = notPaidReservations;
    }
}