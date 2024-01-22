package server.data.threads;
import data.cli2Serv.seriObjects.Show;
import data.serv2Cluster.Abort;
import data.serv2Cluster.Commit;
import data.serv2Cluster.Confirmation;
import data.serv2Cluster.Prepare;
import db.DataBase;
import data.cli2Serv.*;
import data.cli2Serv.seriObjects.Reservation;
import server.Server;
import server.data.ClientInfo;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;

import static server.data.threads.ThreadConnectClient.MAX_SIZE;

public class ThreadClient <T> extends Thread {
    private Socket toClientSocketRegular;
    private Socket toClientSocketAsync;
    private DataBase db;
    private ObjectOutputStream oosRegular ;
    private ObjectOutputStream oosAsync;
    private ObjectInputStream oisRegular ;
    private boolean exit = false;
    private String cliUsername = null;
    private ClientInfo clientInfo;
    private Server server;
    private MulticastSocket s;
    private ArrayList<T> aux;
    public ThreadClient(Socket toClientSocketRegular, Socket toClientSocketAsync, DataBase db, ClientInfo nc, Server server,MulticastSocket s) {
        this.toClientSocketRegular = toClientSocketRegular;
        this.toClientSocketAsync = toClientSocketAsync;
        this.db = db;
        clientInfo = nc;
        this.server=server;
        this.s = s;
        aux = new ArrayList<>();
    }

    @Override
    public void interrupt() {
        exit = true;
    }

    @Override
    public void run() {
        Cli2Serv cliMessage = null;

        try {
            oosRegular = new ObjectOutputStream(toClientSocketRegular.getOutputStream());
            oisRegular = new ObjectInputStream(toClientSocketRegular.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            failClient();
        }
        while (!exit) {
            synchronized (this){
                if(!server.getRead())
                    continue;
            }

            try {
                cliMessage = (Cli2Serv) oisRegular.readUnshared();
            } catch (IOException | ClassNotFoundException e) {
               failClient();
               continue;
            }
            aux.clear();

            switch (cliMessage.getRequestType()) {
                case REGISTER -> {
                    C2SRegister regData = (C2SRegister) cliMessage;
                    cliUsername = cliMessage.getUsername();
                    aux.add((T) regData.getUsername());
                    aux.add((T) regData.getName());
                    aux.add((T) regData.getPassword());
                    try {
                        if(!prepareRequest(cliMessage.getRequestType(), null,aux)) {
                            oosRegular.writeUnshared(false);
                            break;
                        }
                        if(db.registUser(regData.getUsername(), regData.getName(), regData.getPassword())) {
                            oosRegular.writeUnshared(true);
                            System.out.println("User " + cliUsername + "registed successfully");
                        } else {
                            oosRegular.writeUnshared(false);
                            System.out.println("User failed registed...");
                        }
                    } catch (IOException e) {
                        System.err.println("Unable to send information to user");
                        failClient();
                    }
                    db.notifyClientUpdate(false);
                }
                case LOGIN -> {
                    C2Slogin data = (C2Slogin) cliMessage;
                    cliUsername = data.getUsername();
                    aux.add((T) cliUsername);
                    aux.add((T) data.getPassword());
                    aux.add((T) clientInfo);

                    try {
                        if(!prepareRequest(cliMessage.getRequestType(), null,aux)){
                            oosRegular.writeUnshared(null);
                            break;
                        }
                        oosRegular.writeUnshared(db.loginUser(cliUsername, data.getPassword(),clientInfo));
                    } catch (SQLException e) {
                        System.err.println("Error while login...");
                    } catch (SocketException e) {
                        exit =true;
                    } catch (IOException e) {
                        System.err.println("Unable to send information to user");
                        failClient();
                    }
                    db.notifyClientUpdate(false);
                }
                case LOGOUT -> {
                    C2Slogout data = (C2Slogout) cliMessage;
                    System.out.println("User  " + data.getUsername() + " logged out");
                    try {
                        aux.add((T) cliUsername);
                        if(!prepareRequest(cliMessage.getRequestType(),null,aux)) {
                            oosRegular.writeObject(false);
                            break;
                        }
                        db.logout(cliUsername);
                        oosRegular.writeObject(true);
                        failClient();
                    } catch (IOException e) {
                        System.err.println("Unable to send information to user");
                        failClient();
                    }
                    db.notifyClientUpdate(false);
                    exit = true;
                }
                case EDIT_USERNAME -> {

                    C2SEditUserData data = (C2SEditUserData) cliMessage;
                    aux.add((T) data.getChange());
                    aux.add((T) data.getUsername());
                    try {
                        if(!prepareRequest(cliMessage.getRequestType(), null,aux)) {
                            oosRegular.writeUnshared(false);
                            break;
                        }
                        oosRegular.writeUnshared(db.editUsername(data.getChange(), data.getUsername()));
                    } catch (IOException e) {
                        System.err.println("Unable to send information to user");
                        failClient();
                    }
                    db.notifyClientUpdate(false);
                }
                case EDIT_PASSWORD -> {
                    C2SEditUserData data = (C2SEditUserData) cliMessage;
                    aux.add((T) data.getChange());
                    aux.add((T) data.getUsername());
                    try {
                        if(!prepareRequest(cliMessage.getRequestType(), null,aux)) {
                            oosRegular.writeUnshared(false);
                            break;
                        }
                        oosRegular.writeUnshared(db.editPassword(data.getChange(), data.getUsername()));
                    } catch (IOException | SQLException e) {
                        System.err.println("Unable to send information to user");
                        failClient();
                    }
                    db.notifyClientUpdate(false);
                }
                case EDIT_NAME -> {

                    C2SEditUserData data = (C2SEditUserData) cliMessage;
                    aux.add((T) data.getChange());
                    aux.add((T) data.getUsername());
                    try {
                        if(!prepareRequest(cliMessage.getRequestType(), null,aux)) {
                            oosRegular.writeUnshared(false);
                            break;
                        }
                        oosRegular.writeUnshared(db.editName(data.getChange(), data.getUsername()));
                    } catch (IOException e) {
                        System.err.println("Unable to send information to user");
                        failClient();
                    }
                    db.notifyClientUpdate(false);
                }
                case RESERVATIONS -> {
                    C2SReservation data = (C2SReservation) cliMessage;
                    ArrayList<Reservation> reservations = new ArrayList<>();
                    switch (data.getSb()){
                        case PAY_RESERVATION -> {
                            try {
                                aux.add((T) String.valueOf(data.getIdReservaton()));
                                aux.add((T) data.getUsername());
                                if(!prepareRequest(data.getRequestType(),data.getSb(),aux)){
                                    oosRegular.writeUnshared(false);
                                    break;
                                }
                                oosRegular.writeUnshared(db.payReservation(data.getIdReservaton(),data.getUsername()));
                            } catch (IOException e) {
                                System.err.println("Unable to send information to user");
                                failClient();
                            }

                        }
                        case DELETE_RESERVATION -> {
                            try {
                                aux.add((T) String.valueOf(data.getIdReservaton()));
                                aux.add((T) data.getUsername());
                                if(!prepareRequest(data.getRequestType(),data.getSb(),aux)){
                                    oosRegular.writeUnshared(false);
                                    break;
                                }
                                oosRegular.writeUnshared(db.deleteReservation(data.getIdReservaton(),data.getUsername()));
                            } catch (IOException e) {
                                System.err.println("Unable to send information to user");
                                failClient();
                            }

                        }
                        case ADD_RESERVATION -> {
                            try {
                                aux.add((T) data.getSeat());
                                aux.add((T) String.valueOf(data.getPay()));
                                aux.add((T) data.getUsername());
                                if(!prepareRequest(data.getRequestType(),data.getSb(),aux)) {
                                    oosRegular.writeUnshared(-1);
                                    break;
                                }
                                oosRegular.writeUnshared(db.nPaidReservation(data.getSeat(),data.getPay(),data.getUsername()));
                            } catch (IOException e) {
                                System.err.println("Unable to send information to user");
                                failClient();
                            }

                        }
                    }
                    db.notifyClientUpdate(false);
                }
                case ADMIN_SHOW -> {
                    C2SAdmin data = (C2SAdmin) cliMessage;
                    switch (data.getSb()){
                        case INSERT_SHOW -> {
                            Show show = data.getShow();
                            int duration = show.getDuration();
                            int visible = show.getVisible();
                            aux.add((T) show.getSeats());
                            aux.add((T) show.getDescription());
                            aux.add((T) show.getType());
                            aux.add((T) show.getDate());
                            aux.add((T) String.valueOf(duration));
                            aux.add((T) show.getLocal());
                            aux.add((T) show.getCity());
                            aux.add((T) show.getCountry());
                            aux.add((T) show.getAgeRate());
                            aux.add((T) String.valueOf(visible));
                            aux.add((T) data.getUsername());

                            try {
                                if(!prepareRequest(data.getRequestType(),data.getSb(),aux)){
                                    oosRegular.writeUnshared(false);
                                    break;
                                }
                                oosRegular.writeUnshared(db.addShow(data.getShow(),data.getUsername()));
                            } catch (IOException e) {
                                System.err.println("Unable to send information to user");
                                failClient();
                            }
                        }
                        case MAKE_SHOW_AVAILABLE -> {
                            aux.add((T) String.valueOf(data.getId()));
                            aux.add((T) data.getUsername());
                            try {
                                if(!prepareRequest(data.getRequestType(),data.getSb(),aux)){
                                    oosRegular.writeUnshared(false);
                                    break;
                                }
                                oosRegular.writeUnshared(db.makeShowVisible(data.getId(),data.getUsername()));
                            } catch (IOException e) {
                                System.err.println("Unable to send information to user");
                                failClient();
                            }

                        }
                        case DELETE_SHOW -> {
                            aux.add((T) String.valueOf(data.getId()));
                            aux.add((T) data.getUsername());
                            try {
                                if(!prepareRequest(data.getRequestType(),data.getSb(),aux)){
                                    oosRegular.writeUnshared(false);
                                    break;
                                }
                                oosRegular.writeUnshared(db.deleteShow(data.getId(),data.getUsername()));
                            } catch (IOException e) {
                                System.err.println("Unable to send information to user");
                                failClient();
                            }
                        }
                    }
                    db.notifyClientUpdate(false);
                }
            }
        }
    }


    public void failClient() {
        exit = true;
        server.removeClients(clientInfo);
        server.removeClient();
    }

    public <T> boolean prepareRequest(Cli2Serv.RequestType requestType, Cli2Serv.SubType sb, ArrayList<T> info){

        DatagramSocket socketUpdate = null;
        ByteArrayOutputStream boutUpdate = null;
        ObjectOutputStream ooutUpdate = null;
        ByteArrayInputStream binUpdate;
        ObjectInputStream oinUpdate;
        DatagramPacket dp = null;

        try {
            socketUpdate = new DatagramSocket(0);
            socketUpdate.setSoTimeout(1000);
            int port = socketUpdate.getLocalPort();
            Prepare prepare = new Prepare(port);
            prepare.setType(requestType);
            prepare.setSb(sb);
            prepare.setData(info);
            prepare.setIp("127.0.0.1");
            prepare.setIdServerOrigin(server.getServerMulticastId());
            boutUpdate = new ByteArrayOutputStream();
            ooutUpdate = new ObjectOutputStream(boutUpdate);
            Boolean done = true;
            ooutUpdate.writeUnshared(prepare);
            System.out.println("-------------> Sent Prepare");
            for(int j = 0;j < 2; j++) {
                dp = new DatagramPacket(boutUpdate.toByteArray(), boutUpdate.size(),InetAddress.getByName("239.39.39.39"),4004);
                s.send(dp);
                for (int i = 0; i < server.getServerList().getDataservers().size()-1; i++) {
                    try {
                        DatagramPacket receive = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
                        socketUpdate.receive(receive);
                        binUpdate = new ByteArrayInputStream(receive.getData(), 0, receive.getLength());
                        oinUpdate = new ObjectInputStream(binUpdate);

                        Confirmation confirm = (Confirmation) oinUpdate.readUnshared();
                    } catch (SocketTimeoutException e) {
                        System.out.println("-------------> Some servers did not confirm");
                        done = false;
                        break;
                    }
                }
                if(done){
                    boutUpdate = new ByteArrayOutputStream();
                    ooutUpdate = new ObjectOutputStream(boutUpdate);
                    ooutUpdate.writeUnshared(new Commit(prepare.getVersion(),server.getServerMulticastId()));
                    s.send(new DatagramPacket(boutUpdate.toByteArray(), boutUpdate.size(),InetAddress.getByName("239.39.39.39"),4004));
                    System.out.println("-------------> Received confirmation");
                    return true;
                }

            }
            if(!done){
                boutUpdate = new ByteArrayOutputStream();
                ooutUpdate = new ObjectOutputStream(boutUpdate);
                ooutUpdate.writeUnshared(new Abort(prepare.getVersion()));
                s.send(new DatagramPacket(boutUpdate.toByteArray(), boutUpdate.size(),InetAddress.getByName("239.39.39.39"),4004));
                return false;
            }
        }catch (Exception e){
            System.err.println("Update process failed");
        }
        return false;
    }
}


