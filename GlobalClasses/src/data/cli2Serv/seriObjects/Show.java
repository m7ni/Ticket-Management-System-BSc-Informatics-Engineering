package data.cli2Serv.seriObjects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

@Entity
public class Show implements Serializable, Iterable {
    private static final long serialVersionUID = 10l;
    private ArrayList<Seat> seats =new ArrayList<>();
    private String description;
    private String type;
    private String date;
    private int duration;
    private String local;
    private String city;
    private String country;
    private String ageRate;
    private int visible;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    public Show(int id,String description, String type, String date, int duration, String local, String city, String country, String ageRate, int visible) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.date = date;
        this.duration = duration;
        this.local = local;
        this.city = city;
        this.country = country;
        this.ageRate = ageRate;
        this.visible = visible;
    }

    public Show(String description, String type, String date, int duration, String local, String city, String country, String ageRate, int visible) {
        this.description = description;
        this.type = type;
        this.date = date;
        this.duration = duration;
        this.local = local;
        this.city = city;
        this.country = country;
        this.ageRate = ageRate;
        this.visible = visible;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nSHOW: "+ id + "\n" + "\tDescription: " + description + "\n" + "\tType: " + type + "\n" + "\tDuration: " +duration + "\n" + "\tAge rate: " + ageRate +"\n");
        sb.append("\tDate Hour: "+ date + "\n" + "\tLocal: " + local + "\n" + "\tCity: " + city + "\n" + "\tCountry: " +country+"\n");

        return sb.toString();
    }

    public String printSeats() {
        StringBuilder sb = new StringBuilder();
        String previousLine = "1";
        for(Seat s : seats) {
            if(s.isAvailable()) {
                if(!s.getLine().equalsIgnoreCase(previousLine))
                    sb.append("\n").append(s.getLine() + "-> ");
                sb.append("|").append(s.getPlace() + ":" + s.getPrice());

                previousLine = s.getLine();
            }
        }

        return sb.toString();
    }

    @Override
    public Iterator iterator(){
        return new Iterator() {
            int count = 0;
            @Override
            public boolean hasNext() {
                return count<9;
            }

            @Override
            public Object next() {
                switch (count++){
                    case 0 ->{
                        return id;
                    }
                    case 1 ->{
                        return description;
                    }
                    case 2 ->{
                        return type;
                    }
                    case 3 ->{
                        return date;
                    }
                    case 4 ->{
                        return duration;
                    }
                    case 5 ->{
                        return local;
                    }
                    case 6 ->{
                        return city;
                    }
                    case 7 ->{
                        return country;
                    }
                    case 8 ->{
                        return ageRate;
                    }
                    default -> {
                        throw new NoSuchElementException();
                    }
                }
            }
        };
    }

    public Show() {
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }


    public int getDuration() {
        return duration;
    }

    public ArrayList<Seat> getSeats() {
        return seats;
    }

    public void setSeats(ArrayList<Seat> seats) {
        this.seats = seats;
    }

    public String getLocal() {
        return local;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getAgeRate() {
        return ageRate;
    }

    public int getVisible() {
        return visible;
    }

}
