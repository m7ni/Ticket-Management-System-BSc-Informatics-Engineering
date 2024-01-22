package data.cli2Serv.seriObjects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.io.Serializable;
@Entity
public class Reservation implements Serializable {
    private static final long serialVersionUID = 10l;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String dateHour;
    private int paid;
    private int idUser;
    private int idShow;

    public Reservation(int id, String dateHour, int paid, int idUser, int idShow) {
        this.id = id;
        this.dateHour = dateHour;
        this.paid = paid;
        this.idUser = idUser;
        this.idShow = idShow;
    }

    public int getIdReservation() {
        return id;
    }

    public int getIdShow() {
        return idShow;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateHour() {
        return dateHour;
    }

    public void setDateHour(String dateHour) {
        this.dateHour = dateHour;
    }

    public int getPaid() {
        return paid;
    }

    public void setPaid(int paid) {
        this.paid = paid;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public void setIdShow(int idShow) {
        this.idShow = idShow;
    }
}
