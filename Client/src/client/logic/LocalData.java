package client.logic;

import data.cli2Serv.FiltersShows;
import data.cli2Serv.seriObjects.Reservation;
import data.cli2Serv.seriObjects.Show;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LocalData {
    private ArrayList<Show> shows;
    private ArrayList<Show> shows2Delete;
    private ArrayList<Show> showsNotAvailable;
    private ArrayList<Reservation> PaidReservations;
    private ArrayList<Reservation> NotPaidReservations;
    private Client logic;

    public LocalData(Client logic) {
        this.logic = logic;
        shows = new ArrayList<>();
        shows2Delete = new ArrayList<>();
        showsNotAvailable = new ArrayList<>();
        PaidReservations = new ArrayList<>();
        NotPaidReservations = new ArrayList<>();
    }

    public ArrayList<Show> getShows() {
        return shows;
    }

    public ArrayList<Show> getShows2Delete() {
        return shows2Delete;
    }

    public void setShows2Delete(ArrayList<Show> shows2Delete) {
        this.shows2Delete = shows2Delete;
    }

    public ArrayList<Show> getShowsNotAvailable() {
        return showsNotAvailable;
    }

    public void setShowsNotAvailable(ArrayList<Show> showsNotAvailable) {
        ArrayList<Show> aux = new ArrayList<>();

        for(Show s : showsNotAvailable)
            if(s.getVisible() == 0)
                aux.add(s);

        this.showsNotAvailable = aux;
    }

    public ArrayList<Show> getShowsFilters(HashMap<FiltersShows, Object> filters) {
        return filterShows(filters);
    }

    public void setShows(ArrayList<Show> shows) {
        ArrayList<Show> aux = new ArrayList<>();

        for(Show s : shows)
            if(s.getVisible() == 1)
                aux.add(s);

        this.shows = aux;
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

    public ArrayList<Show>  getNotAvailableShows() {
        return showsNotAvailable;
    }

    public ArrayList<Show> filterShows(HashMap< FiltersShows, Object> filters) {
        ArrayList<Show> aux = new ArrayList<>();
        aux= getShows();
        for(FiltersShows f : filters.keySet()){
            Predicate<Show> predicate = getPredicate(f, filters.get(f));
            aux = (ArrayList<Show>) getShows().stream().filter(predicate).collect(Collectors.toList());
        }
        return aux;
    }

    private static <T>  Predicate<Show> getPredicate(FiltersShows criteria, T value) {
        Predicate<Show> s = switch (criteria) {
            case DESCRIPTION -> show -> show.getDescription().equals(value);
            case LOCAL -> show -> show.getLocal().equals(value);
            case TYPE -> show -> show.getType().equals(value);
            case DATE -> show -> show.getDate().equals(value);
            case DURATION -> show -> show.getDuration() == (Integer) value;
            case COUNTRY -> show -> show.getCountry().equals(value);
        }; // by default returns all messages

        return s;
    }
}