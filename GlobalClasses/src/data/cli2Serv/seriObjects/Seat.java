package data.cli2Serv.seriObjects;

public class Seat extends Show{
    private String line;
    private String place;
    private int price;
    private boolean available;
    private int id;
    private int idShow;

    public Seat(int id, String line, String place, int price, boolean available, int idShow) {
        this.id = id;
        this.line = line;
        this.place = place;
        this.price = price;
        this.available = available;
        this.idShow = idShow;
    }

    public Seat(String line, String place, int price, boolean available) {
        this.line = line;
        this.place = place;
        this.price = price;
        this.available = available;
    }

    public int getIdShow() {
        return idShow;
    }

    public String getPlace() {
        return place;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public int hashCode() {
        return (line + place).hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if(!(obj instanceof Seat) )
            return false;

        Seat aux = (Seat) obj;

        if(aux.getLine().equalsIgnoreCase(line) && aux.getPlace().equalsIgnoreCase(place))
            return true;

      return false;
    }
}
