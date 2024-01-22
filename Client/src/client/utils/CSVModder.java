package client.utils;

import client.logic.Client;
import data.cli2Serv.seriObjects.Seat;
import data.cli2Serv.seriObjects.Show;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class CSVModder {
    private ArrayList<Seat> seats = new ArrayList<>();
    private String description;
    private String type;
    private String date;
    private String hour;
    private int duration;
    private String local;
    private String city;
    private String country;
    private String ageRate;

    private Client client;

    public CSVModder(Client client) {
        this.client = client;
    }

    public Show uploadShow(String nameFile) throws FileNotFoundException {

        File file = new File(nameFile);

        if(!file.exists())
            return null;

        Scanner myReader = null;

        myReader = new Scanner(file);

        int i=0;
        while (myReader.hasNextLine()) {
            if(i<9) {
                String commaDelimited = myReader.nextLine();
                String[] values = commaDelimited.split(";");
                if(values.length<2)
                    return null;
                String newString = values[1].replaceAll("\"", "");         //há umas que têm mais que uma só string
                switch(i) {
                    case 0 -> description = newString;
                    case 1 -> type = newString;
                    case 2 -> {
                        newString = newString + " " + values[2].replaceAll("\"", "")+" "+values[3].replaceAll("\"", "");
                        date = newString;
                    }
                    case 3 -> {
                        newString = newString + " " + values[2].replaceAll("\"", "");
                        hour = newString;
                    }
                    case 4 -> duration = Integer.parseInt(newString);
                    case 5 -> local = newString;
                    case 6 -> city = newString;
                    case 7 -> country = newString;
                    case 8 -> ageRate = newString;
                }
                i++;
            } else if (i>=9) {
                String commaDelimited = myReader.nextLine();
                String[] values = commaDelimited.split(";");
                if(values[0].equals("\"Fila\"") && values[1].equals("\"Lugar:Preço\"") || values[0].isBlank())
                    continue;
                String[] noAspas = new String[values.length];
                for(int j = 0 ; j < values.length ; j++) {
                    String newString = values[j].replaceAll("\"", "");
                    noAspas[j] = newString;
                }
                for(int j = 1 ; j < noAspas.length ; j++) {
                    String[] delimited = noAspas[j].split(":");
                    Seat seat = new Seat(noAspas[0],delimited[0], Integer.parseInt(delimited[1]) , true);
                    if(seats.contains(seat)) {
                        return null;
                    }

                    seats.add(seat);
                }
            }
        }
        myReader.close();
        String dateHour = date + " " + hour;
        Show show = new Show(description, type, dateHour, duration, local, city, country, ageRate, 0);
        show.setSeats(seats);
        return show;
    }

}
