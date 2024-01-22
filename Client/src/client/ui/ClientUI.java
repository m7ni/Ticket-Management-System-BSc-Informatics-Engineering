package client.ui;

import client.logic.Client;
import client.logic.Interfaces;
import client.logic.LocalData;
import client.utils.PDInput;
import data.cli2Serv.FiltersShows;
import data.cli2Serv.seriObjects.Reservation;
import data.cli2Serv.seriObjects.Seat;
import data.cli2Serv.seriObjects.Show;
import db.UserType;
import java.util.ArrayList;
import java.util.HashMap;


public class ClientUI {
    private Client logic;
    private LocalData localData;

    public ClientUI(Client logic, LocalData localData) {
        this.logic = logic;
        this.localData = localData;
        logic.setClientUI(this);
    }

    public void start() {
        updateView(Interfaces.LOGIN_AND_REGIST_MENU);
        UserType flag = null;
        while(flag != UserType.REGULAR && flag != UserType.ADMIN) {
            flag = null;
            switch (PDInput.chooseOption("Choose the action that you want to do",
                    "Login","Regist","EXIT")) {
                case 1 -> {
                    flag = LoginUI();
                    if(flag == null) {
                        System.out.println("Error logging in");
                    }

                }
                case 2 -> {
                    if(RegisterUI()!= null) {
                        System.out.println("Registed with success");
                    }
                }
                case 3 -> System.exit(0);
            }

        }
        if(flag==UserType.REGULAR)
            mainMenu();

        if(flag==UserType.ADMIN)
            adminMenu();

        return;
    }

    public UserType RegisterUI() {
        String name = PDInput.readString("Type your name: ", false);
        String username = PDInput.readString("Type your username: ", true);
        String password = PDInput.readString("Type your password: ", true);
        return logic.registClient(name,username,password);
    }

    public UserType LoginUI() {
        String username = PDInput.readString("Type your username: ", true);
        String password = PDInput.readString("Type your password: ", true);
        return logic.logClient(username, password);    //return 1 se for normal ou 2 se for admin e 0 se nao der para fazer login
    }

    public void adminMenu() {
        updateView(Interfaces.ADMIN_MENU);
        boolean flag = false;
        while (!flag) {
            flag = false;
            switch (PDInput.chooseOption("Main Menu",
                    "Upload a Show",
                    "Enable Show",
                    "Delete Show",
                    "EXIT"
            )) {
                case 1 -> {
                    String nameFile = PDInput.readString("Filename that you want to import Format Example->(filename.txt): ", true);

                    if (!logic.uploadShow(nameFile)) {
                        System.out.println("Something did not go right");
                    }else
                        System.out.println("Success uploading data");
                }
                case 2 -> enableShowUI();
                case 3 -> deleteShowUI();
                case 4 -> {
                    if(logic.logout()) {
                        System.out.println("Logged out");
                        logic.close();
                        return;
                    } else
                        System.out.println("Error logging out");
                }
            }
        }
    }

    public void deleteShowUI() {
        updateView(Interfaces.DELETE_SHOW);
        int idShow;
        for(Show s : localData.getShows2Delete())
            System.out.println(s.getId() + " " +s.getDescription());

        if(localData.getShows2Delete().isEmpty()) {
            System.out.println("There aren't any shows");
            return;
        }

        do {
            idShow = PDInput.readInt("Type the id of the show that you want to delete: ");
        } while(!logic.deleteShow(idShow));
        System.out.println("Show deleted with success");
    }

    public void enableShowUI() {
        updateView(Interfaces.ENABLE_SHOW);
        int idShow;
        for(Show s : localData.getNotAvailableShows())
            System.out.println(s.getId() + " " +s.getDescription());

        if(localData.getNotAvailableShows().isEmpty()) {
            System.out.println("There aren't any unavailable shows");
            return;
        }

        do {
            idShow = PDInput.readInt("Type the id of the show that you want to enable: ");
        } while(!logic.putShowAvailable(idShow));
        System.out.println("Show enabled with success");
    }

    public void mainMenu() {
        updateView(Interfaces.MAIN_MENU);
        boolean flag = false;
        while (!flag) {
            flag = false;
            switch (PDInput.chooseOption("Main Menu",
                    "Change the registered values",
                    "Consult reservations that await for payment confirmation",
                    "Consult payed reservations",
                    "Consult shows with filters",
                    "Select Show",
                    "Log Out"
            )) {
                case 1 -> changeRegiValUI();
                case 2 -> consultNotPaidReservationsUI();
                case 3 -> {
                    ArrayList<Show> shows = localData.getShows();
                    if(localData.getPaidReservations().isEmpty())
                        System.out.println("There aren't any paid");

                    for(Reservation r : localData.getPaidReservations()) {
                        System.out.println("Reservation: "+ r.getIdReservation());
                        for(Show s : shows) {
                            if(r.getIdShow() == s.getId())
                                System.out.println(" --> Show: " + s.getDescription() + "\n") ;
                        }
                    }
                }
                case 4 -> consultShowsFiltersUI();
                case 5 -> consultAndSelectShowAndConsultAndChooseSeatUI();
                case 6 -> {
                    if(logic.logout()) {
                        System.out.println("Logged out");
                        logic.close();
                        return;
                    } else
                        System.out.println("Error logging out");
                }
            }
        }
    }

    public void changeRegiValUI() {
        updateView(Interfaces.EDIT_MENU);
        boolean flag = false;
        while(!flag) {
            flag = false;
            switch(PDInput.chooseOption("Choose what you want to edit: ",
                    "Username",
                    "Name",
                    "Password",
                    "Back to Main Menu"
            )) {
                case 1 -> {
                    if(editUsernameUI()) {
                        System.out.println("Username edited with success");
                        flag = true;
                    }
                }
                case 2 ->{
                    if(editNameUI()) {
                        System.out.println("Name edited with success");
                        flag = true;
                    }
                }
                case 3 ->{
                    if(editPasswordUI()) {
                        System.out.println("Password edited with success");
                        flag = true;
                    }
                }
                case 4 -> flag = true;
            }
        }
    }

    public boolean editUsernameUI() {
        updateView(Interfaces.EDIT_USERNAME);
        System.out.println("Type your new Username: ");
        String nUsername = PDInput.readString("", true);
        return logic.editUsername(nUsername);
    }



    public boolean editNameUI() {
        updateView(Interfaces.EDIT_NAME);
        System.out.println("Type your new Name: ");
        String nName = PDInput.readString("", false);
        return logic.editName(nName);
    }

    public boolean editPasswordUI() {
        updateView(Interfaces.EDIT_PASSWORD);
        System.out.println("Type your new Password: ");
        String nPassword = PDInput.readString("", true);
        return logic.editPassword(nPassword);
    }

    public void consultShowsFiltersUI() {
        updateView(Interfaces.CONSULT_SHOWS_FILTERS);
        HashMap<FiltersShows, Object> filters = new HashMap<>();
        ArrayList<Show> aux = new ArrayList<>();

        boolean flag = false;
        while(!flag) {
            flag = false;
            switch(PDInput.chooseOption("Choose the filters that you want for your search: ",
                    "Description",
                    "Local",
                    "Type",
                    "Date",
                    "Duration",
                    "Country",
                    "I have no more filters to add to the search"
            )) {
                case 1 -> {
                    String description = PDInput.readString("Type the description of the show: ", false);
                    filters.put(FiltersShows.DESCRIPTION, description);
                }
                case 2 -> {
                    String local = PDInput.readString("Type the local of the show: ", false);
                    filters.put(FiltersShows.LOCAL, local);
                }
                case 3 -> {
                    String type = PDInput.readString("Type the type of the show: ", false);
                    filters.put(FiltersShows.TYPE, type);
                }
                case 4 -> {
                    String date = PDInput.readString("Type the date of the show: ", false);
                    filters.put(FiltersShows.DATE, date);
                }
                case 5 -> {
                    int duration = PDInput.readInt("Type the duration of the show: ");
                    filters.put(FiltersShows.DURATION, duration);
                }
                case 6 -> {
                    String country = PDInput.readString("Type the country of the show: ", false);
                    filters.put(FiltersShows.COUNTRY, country);
                }
                case 7 -> {
                    aux = localData.getShowsFilters(filters);
                    if(aux.isEmpty())
                        System.out.println("There aren´t any show that match these filters");
                    else{
                        for(Show s : aux)
                            System.out.println(s.toString());
                    }
                    flag = true;
                }
            }
        }
    }

    public void consultAndSelectShowAndConsultAndChooseSeatUI() {
        updateView(Interfaces.CONSULT_SHOWS);

        ArrayList<Show> showsAux = localData.getShows();
        for(Show s : showsAux)
            System.out.println(s.toString());

        switch(PDInput.chooseOption("Do you want to select a show or leave? ",
                "Select Show",
                "Go back"
        )) {
            case 1 -> selectShowUI();
            case 2 -> {
                return;
            }
        }
    }

    public void selectShowUI() {
        updateView(Interfaces.SELECT_SHOW);
        int id;

        if(localData.getShows().isEmpty()) {
            System.out.println("There no available shows");
            return;
        }

        do {
            id = PDInput.readInt("Type the Show id that you want to select: ");
        } while (!logic.verifyId(id));

        switch(PDInput.chooseOption("Do you want to consult this show's available seats or leave? ",
                "Consult seats",
                "Go back"
        )) {
            case 1 -> consultSeats(id);
            case 2 -> {
                return;
            }
        }
    }

    public void consultSeats(int id) {
        updateView(Interfaces.CONSULT_SEATS);

        ArrayList<Seat> seatsAux = new ArrayList<>();
        for(Show s : localData.getShows()) {
            if(s.getId() == id) {
                System.out.println(s.printSeats());
                for(int i = 0 ; i < s.getSeats().size() ; i++) {
                    if(s.getSeats().get(i).isAvailable()) {
                        seatsAux.add(s.getSeats().get(i));
                    }
                }
            }
        }

        switch(PDInput.chooseOption("Do you want to select seats on this show's available seats or leave? ",
                    "Select seats",
                "Go back"
        )) {
            case 1 -> selectSeats(id, seatsAux);
            case 2 -> {
                return;
            }
        }

    }

    public void selectSeats(int id, ArrayList<Seat> availableSeats) {
        updateView(Interfaces.SELECT_SEATS);
        String line;
        String place;

            if(availableSeats.isEmpty()){
                System.out.println("There are no available seats for this show");
                return;
            }

            do {
                line = PDInput.readString("Type the seat's line that you want to select: ", true);
            } while (!logic.verifyLine(line, availableSeats));

            do {
                place = PDInput.readString("Type the line's place that you want to select: ", true);
            } while (!logic.verifyPlace(id, line, place, availableSeats));



        switch(PDInput.chooseOption("Do you want to reserve the selected seats or leave? ",
                "Reserve selected seats",
                "Go back to the menu"
        )) {
            case 1 -> submitResRequestUI();
            case 2 -> mainMenu();
        }
    }

    public void submitResRequestUI() {
        updateView(Interfaces.SUBMIT_RESERVATION);
        int idRes = logic.verifyPlaceSelect2Reservate();
        if(idRes == -1) {
            System.out.println("An error has occurred reservating the selected seats");
            return;
        }
        System.out.println("\nSuccess doing the reservation\n");

        switch(PDInput.chooseOption("Do you want to already pay the reservation or leave? ",
                "Pay reserved seats",
                "Go back"
        )) {
            case 1 -> {
                if(logic.paySeat(idRes, localData.getNotPaidReservations()))
                    System.out.println("Success paying the reservation");
                else
                    System.out.println("Error paying reservation");
                mainMenu();
            }
            case 2 -> mainMenu();
        }
    }

    public void paySeatsUI() {
        updateView(Interfaces.PAY_SEATS);
        ArrayList<Reservation> clientReservationsNotPayed = new ArrayList<>(localData.getNotPaidReservations());
        int idRes;

        ArrayList<Show> shows = localData.getShows();

        if( clientReservationsNotPayed.isEmpty()){
            System.out.println("There are no reserved seats to pay");
            return;
        }
        for(Reservation r : clientReservationsNotPayed) {
            System.out.println("Reservation: "+ r.getIdReservation());
            for(Show s : shows) {
                if(r.getIdShow() == s.getId())
                    System.out.println("\n\tShow: " + s.getDescription() + "\n") ;
            }
        }

        do {
            idRes = PDInput.readInt("Type the id of the reservation that you want to pay: ");
        } while(!logic.paySeat(idRes, clientReservationsNotPayed));
        System.out.println("\nSucess paying the reservation\n");
        mainMenu();
    }

    public void consultNotPaidReservationsUI() {
        updateView(Interfaces.CONSULT_NOT_PAID_RESERVATION);
        ArrayList<Show> shows = localData.getShows();
        if(localData.getNotPaidReservations().isEmpty())
            System.out.println("There aren't any Reservations awaiting payment");
        for(Reservation r : localData.getNotPaidReservations()) {
            System.out.println("Reservation: "+ r.getIdReservation());
            for(Show s : shows) {
                if(r.getIdShow() == s.getId())
                    System.out.println("--> Show: " + s.getDescription() + "\n") ;
            }
        }

        switch(PDInput.chooseOption("Do you want to already pay the reservation, delete them or leave? ",
                "Pay reservation",
                "Delete reservation",
                "Go back"
        )) {
            case 1 -> paySeatsUI();
            case 2 -> eliminateResWithoutPaymentUI(localData.getNotPaidReservations());
            case 3 -> mainMenu();
        }

    }

    public void eliminateResWithoutPaymentUI(ArrayList<Reservation> clientReservationsNotPayed) {
        updateView(Interfaces.ELIMINATE_RESERVATION);
        int idRes;
        ArrayList<Show> shows = localData.getShows();

        do {
            idRes = PDInput.readInt("Type the id of the reservation that you want to eliminate: ");
        } while(!logic.eliminateReservations(idRes, clientReservationsNotPayed));

        System.out.println("\nSucess eliminating the reservation\n");
    }

    private void updateView(Interfaces inter) {
        logic.setCanChange(true);
        logic.setUiState(inter);
        logic.setCanChange(false);
    }
}
