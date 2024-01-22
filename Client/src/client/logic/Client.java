package client.logic;

import client.ui.ClientUI;
import client.utils.CSVModder;
import client.utils.PDInput;
import data.cli2Serv.*;
import data.cli2Serv.seriObjects.Reservation;
import data.cli2Serv.seriObjects.Seat;
import data.cli2Serv.seriObjects.ServersInfo;
import data.cli2Serv.seriObjects.Show;

import data.serv2Cli.Cli2ServList;
import db.UserType;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import static data.cli2Serv.Cli2Serv.RequestType.*;
import static data.cli2Serv.Cli2Serv.SubType.*;
import static data.cli2Serv.Cli2Serv.SubType.DELETE_RESERVATION;

public class Client {
    private int ServPort;
    private String ServIp;
    private DatagramSocket ds;
    private Socket sCliRegular = null;
    private Socket sCliAsync= null;
    private ObjectOutputStream outServRegular = null;
    private ObjectInputStream inServRegular = null;
    private ObjectInputStream inServAsync = null;
    private boolean noServer = false;
    private String username = null;
    private String password = null;
    private Seat selectedSeat = null;
    private CSVModder csvModder = null;
    private ArrayList<ServersInfo> ServersList;
    private int idReservation;
    private ClientUI clientUI;
    private LocalData ld;
    private Interfaces uiState;
    private boolean canChange;
    private boolean existChange;
    private Thread threadAsync;

    public Client(String[] args) throws IOException {
        this.ServIp = args[1];
        this.ServPort = Integer.parseInt(args[0]);
        ServersList = new ArrayList<>();
        ds = new DatagramSocket();
        if (!connect2serv())
            throw new ConnectException();
    }

    public void setSelectedSeat(Seat selectedSeat) {
        this.selectedSeat = selectedSeat;
    }

    public void setUiState(Interfaces uiState) {
        this.uiState = uiState;
    }

    public void setClientUI(ClientUI clientUI) {
        this.clientUI = clientUI;
    }

    public void setExistChange(boolean existChange) {
        this.existChange=existChange;
    }

    public void setCanChange(boolean canChange) {
        this.canChange = canChange;
        if(canChange && existChange && uiState != null) {
            setExistChange(false);
            changeUI(uiState);
        }
    }

    public LocalData getLd() {
        return ld;
    }

    public void setLd(LocalData ld) {
        this.ld = ld;
    }

    public ArrayList<ServersInfo> getServersList() {
        return ServersList;
    }

    public void setServersList(ArrayList<ServersInfo> serversList) {
        ServersList = serversList;
    }

    public Seat getSelectedSeat() {
        return selectedSeat;
    }

    public boolean getNoServer() {
        return noServer;
    }

    public boolean connect2serv() {
        try {
            inicialComsSend();
            inicialComsReceived();
        } catch (IOException e) {
            System.err.println("Couldn't establish connection with any server. Closing...");
            close();
            return false;
        }

        return true;
    }

    public void inicialComsSend() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        Cli2ServList send2serv = new Cli2ServList();
        out.writeObject(send2serv);
        out.flush();

        byte[] req = baos.toByteArray();

        DatagramPacket dpSend = new DatagramPacket(req, req.length, InetAddress.getByName(ServIp), ServPort);
        ds.send(dpSend);
    }

    public void inicialComsReceived() throws IOException {
        DatagramPacket dpReceived = new DatagramPacket(new byte[5000], 5000);

        ds.setSoTimeout(15 * 1000);
        ds.receive(dpReceived);

        if (dpReceived.getLength() == 0) {
            System.err.println("No servers available. Try again later...");
            ds.close();
            noServer = true;
            return;
        }

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(dpReceived.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);

            Cli2ServList infoServ = null;

            infoServ = (Cli2ServList) ois.readUnshared();
            ServersList.addAll(infoServ.getDataservers());
            for(ServersInfo a : infoServ.getDataservers()){
                System.out.println(a.getServIp().getHostAddress()+" " + a.getServPortRegular() + " " + a.getServPortAsync()+ " " +a.getActiveConnections());
            }
            ConnectServer();
            ds.close();

        } catch (ClassNotFoundException | IOException e) {
            System.err.println("It was not possible to receive information from the Server. Closing...");
            ds.close();
            close();
        }
    }

    public UserType registClient(String name, String username, String password) {
        try {
            C2SRegister regis = new C2SRegister(username, name, password);
            outServRegular.writeUnshared(regis);
            boolean isLogged = (boolean)  inServRegular.readUnshared();
            if (isLogged) {
                this.username = regis.getUsername();
                this.password = regis.getPassword();

                if (regis.getUsername().equals("admin"))
                    return UserType.ADMIN;
                else
                    return UserType.REGULAR;
            }
        }catch (SocketException e){
            ConnectServer();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error Register in communication with server!");
        }
        return null;
    }

    public void ConnectServer(){
        for (ServersInfo serv : ServersList) {
            try {
                sCliRegular = new Socket(serv.getServIp(),serv.getServPortRegular());
                sCliRegular.setSoTimeout(3*1000);
                sCliAsync = new Socket(serv.getServIp(),serv.getServPortAsync());
                System.out.println("Connection found");
                break;
            } catch (Exception e) {
                System.err.println("\nUnable to connect, trying a different server");
            }
        }

        try {
            outServRegular = new ObjectOutputStream(sCliRegular.getOutputStream());
            inServRegular = new ObjectInputStream(sCliRegular.getInputStream());
            new ObjectOutputStream(sCliAsync.getOutputStream());
            inServAsync = new ObjectInputStream(sCliAsync.getInputStream());

            ThreadAsync threadAsync = new ThreadAsync(sCliAsync,inServAsync,this);
            threadAsync.start();
        } catch (IOException e) {
            System.err.println("It was not possible to establish a connection with any server, try again later");
            close();
            return;
        }

    }

    public void close(){
        try {
            if(outServRegular != null) outServRegular.close();
            if(inServRegular != null) inServRegular.close();
            if(inServAsync != null) inServAsync.close();
            if(threadAsync != null) {
                threadAsync.join();
                threadAsync.interrupt();
            }
            System.exit(0);
        } catch (IOException e) {

        } catch (InterruptedException e) {

        }

    }

    public UserType logClient(String username, String password) {
        try {
            C2Slogin login = new C2Slogin(username,password);
            outServRegular.writeUnshared(login);

            UserType ut = (UserType) inServRegular.readUnshared();

            if(ut!=null) {
                this.username = login.getUsername();
                this.password = login.getPassword();
            }
            return ut;
        }catch (SocketException e){
            ConnectServer();
        }catch (SocketTimeoutException e) {
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with server");
        }
        return null;
    }

    public boolean editUsername(String nUsername) {
        C2SEditUserData edUser = new C2SEditUserData(EDIT_USERNAME,username,password,nUsername);
        boolean success = false;
        try {
            outServRegular.writeUnshared(edUser);
            success = (boolean) inServRegular.readUnshared();

            if(success)
                this.username = nUsername;
        }catch (SocketException e){
            ConnectServer();
        }catch (SocketTimeoutException e) {
            return false;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with server");
        }
        return success;
    }

    public boolean editName(String nName) {
        C2SEditUserData edName = new C2SEditUserData(EDIT_NAME,username,password,nName);
        boolean success = false;
        try {
            outServRegular.writeUnshared(edName);
            success = (boolean) inServRegular.readUnshared();

        }catch (SocketException e){
            ConnectServer();
        }catch (SocketTimeoutException e) {
            return false;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with server");
        }
        return success;
    }

    public boolean editPassword(String nPassword) {
        C2SEditUserData edPass = new C2SEditUserData(EDIT_PASSWORD,username,password,nPassword);
        boolean success = false;
        try {
            outServRegular.writeUnshared(edPass);
            success = (boolean) inServRegular.readUnshared();

            if(success)
                password = nPassword;
        }catch (SocketException e){
            ConnectServer();
        }catch (SocketTimeoutException e) {
            return false;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with server");
        }
        return success;
    }

    public boolean verifyId(int id) {
        ArrayList<Show> showsAux = new ArrayList<>(ld.getShows());
        for(Show show : showsAux)
            if(show.getId() == id) {
                return true;
            }
        return false;
    }

    public int verifyPlaceSelect2Reservate() {
        int answer = -1;

        C2SReservation reservate = new C2SReservation(RESERVATIONS, ADD_RESERVATION,getSelectedSeat(), username, getSelectedSeat().getIdShow());
        try {
            outServRegular.writeUnshared(reservate);
            answer = (int) inServRegular.readUnshared();

        }catch (SocketException e){
            ConnectServer();
        }catch (SocketTimeoutException e) {
            return -1;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with server");
        }
        if(answer !=-1)
            this.idReservation=answer;
        setSelectedSeat(null);
        return answer;
    }

    public boolean paySeat(int idRes, ArrayList<Reservation> clientReservationsNotPayed) {
        boolean answer = false;
        for (Reservation r : clientReservationsNotPayed){
            if(r.getIdReservation() == idRes){
                C2SReservation reservate = new C2SReservation(RESERVATIONS, PAY_RESERVATION, idRes, username);
                try {
                    outServRegular.writeUnshared(reservate);
                    return (boolean) inServRegular.readUnshared();
                }catch (SocketException e){
                    ConnectServer();
                }catch (SocketTimeoutException e) {
                    return false;
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error communicating with server");
                }
                return answer;
            }
        }
        return answer;
    }

    public boolean verifyLine(String line, ArrayList<Seat> seatsAux) {
        for (Seat seat : seatsAux) {
            if (seat.getLine().equalsIgnoreCase(line)) {
                return true;
            }
        }
        return false;
    }

    public boolean verifyPlace(int idShow, String line, String place, ArrayList<Seat> seatsAux) {
        for(Seat seat : seatsAux){
            if(seat.getLine().equalsIgnoreCase(line) && seat.getPlace().equalsIgnoreCase(place)) {
                setSelectedSeat(seat);
                return true;
            }
        }
        return false;
    }

    public boolean eliminateReservations(int idReservation, ArrayList<Reservation> clientReservationsNotPayed) {
        for (Reservation r : clientReservationsNotPayed) {
            if(r.getIdReservation() == idReservation) {
                C2SReservation eliminateRes = new C2SReservation(RESERVATIONS, DELETE_RESERVATION, username);
                eliminateRes.setIdReservaton(idReservation);
                try {
                    outServRegular.writeUnshared(eliminateRes);
                    return (boolean) inServRegular.readUnshared();
                }catch (SocketException e){
                    ConnectServer();
                }catch (SocketTimeoutException e) {
                    return false;
                } catch (IOException | ClassNotFoundException  e) {
                    System.err.println("Error communicating with server");
                    return false;
                }
            }
        }
        return false;
    }

    public boolean putShowAvailable(int idShow)  {
        try {
            outServRegular.writeUnshared(new C2SAdmin(ADMIN_SHOW,MAKE_SHOW_AVAILABLE,username,idShow));
            Boolean a = (boolean) inServRegular.readUnshared();
            return a;
        }catch (SocketException  e){
            ConnectServer();
        }catch (SocketTimeoutException e) {
            return false;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with server");
        }
        return false;
    }

    public boolean uploadShow(String nameFile) {
        this.csvModder = new CSVModder(this);
        Show s = null;
        try {
            s = csvModder.uploadShow(nameFile);
        } catch (FileNotFoundException e) {
            return false;
        }
        if(s!=null) {
            try {
                outServRegular.writeUnshared(new C2SAdmin(ADMIN_SHOW,INSERT_SHOW,username,s));
                return (boolean) inServRegular.readUnshared();
            }catch (SocketException e){
                ConnectServer();
            }catch (SocketTimeoutException e) {
                return false;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error communicating with server");
            }
        }
        return false;
    }

    public boolean deleteShow(int idShow) {
        C2SAdmin eliminateShow = new C2SAdmin(ADMIN_SHOW,DELETE_SHOW,username,idShow);
        try {
            outServRegular.writeUnshared(eliminateShow);
            return (boolean) inServRegular.readUnshared();
        }catch (SocketException e){
            ConnectServer();
        }catch (SocketTimeoutException e) {
            return false;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with server");
        }
        return false;
    }

    public boolean logout() {
        C2Slogout logout = new C2Slogout(username);
        try {
            outServRegular.writeUnshared(logout);
            return (boolean) inServRegular.readUnshared();
        }catch (SocketTimeoutException e) {
            return false;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with server");
        }
        return false;
    }

    public void changeUI(Interfaces ui) {
        PDInput.resetScanner();
        switch(ui) {
            case LOGIN_AND_REGIST_MENU -> clientUI.start();
            case ADMIN_MENU -> clientUI.adminMenu();
            case MAIN_MENU -> clientUI.mainMenu();
            case EDIT_MENU-> clientUI.changeRegiValUI();
            case EDIT_NAME -> clientUI.editNameUI();
            case EDIT_USERNAME -> clientUI.editUsernameUI();
            case EDIT_PASSWORD -> clientUI.editPasswordUI();
            case ENABLE_SHOW -> clientUI.enableShowUI();
            case DELETE_SHOW -> clientUI.deleteShowUI();
            case CONSULT_SHOWS_FILTERS -> clientUI.consultShowsFiltersUI();
            case CONSULT_SHOWS,SELECT_SHOW, CONSULT_SEATS, SELECT_SEATS, SUBMIT_RESERVATION -> clientUI.consultAndSelectShowAndConsultAndChooseSeatUI();
            case CONSULT_NOT_PAID_RESERVATION,PAY_SEATS,ELIMINATE_RESERVATION -> clientUI.consultNotPaidReservationsUI();
        }
    }
}
