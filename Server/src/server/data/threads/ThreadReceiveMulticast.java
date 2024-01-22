package server.data.threads;

import data.cli2Serv.seriObjects.DatabaseSeri;
import data.cli2Serv.seriObjects.Seat;
import data.cli2Serv.seriObjects.ServersInfo;
import data.cli2Serv.seriObjects.Show;
import data.serv2Cli.Cli2ServList;
import data.serv2Cluster.*;
import db.DataBase;
import server.Server;
import server.data.ClientInfo;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class ThreadReceiveMulticast extends Thread {
    public static int MAX_SIZE = 1000;
    protected MulticastSocket s;
    protected boolean exit = false;
    protected String id;
    protected int myVersionDB;
    protected Cli2ServList servers;
    protected Heartbeat heartbeatReceived;
    protected Heartbeat heartbeatPersonal;
    private DataBase db;
    private Prepare prepare;
    private Commit commit;
    private Abort abort;
    private Confirmation confirmation;
    private Server server;


    public ThreadReceiveMulticast(MulticastSocket s, Heartbeat info, Cli2ServList serverList, DataBase db, Server server) {
        this.s = s;
        id=info.getIdServer();
        myVersionDB = info.getDataBaseVersion();
        this.servers = serverList;
        heartbeatReceived =new Heartbeat();
        heartbeatPersonal = info;
        this.db = db;
        this.server = server;
    }

    public void terminate() {
        exit = false;
    }

    @Override
    public void interrupt() {
        exit = true;
    }

    public synchronized void run() {
        DatagramPacket dp;
        int VersionDB;
        ByteArrayInputStream bin;
        ObjectInputStream oin;
        DatagramSocket socket = null;

        ByteArrayOutputStream boutDB;
        ObjectOutputStream ooutDB;
        ByteArrayInputStream binDB;
        ObjectInputStream oinDB;
        DatagramPacket packet;
        Prepare prepareReady = null;


        if (s == null || exit) {return;}
            while (!exit) {
                dp = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
                try {
                    s.setSoTimeout(11 * 1000);
                    s.receive(dp);
                } catch (IOException e) {
                   continue;
                }

                try {
                    bin = new ByteArrayInputStream(dp.getData(), 0, dp.getLength());
                    oin = new ObjectInputStream(bin);

                    synchronized (this){
                        myVersionDB = db.getVersionDB();
                    }


                    Object aux = null;
                    try {
                        aux = oin.readUnshared();
                    } catch (IOException e) {
                        continue;
                    }


                    if (aux instanceof Heartbeat) {
                        heartbeatReceived = (Heartbeat) aux;
                        VersionDB = heartbeatReceived.getDataBaseVersion();

                        veriTime();
                        if (!heartbeatReceived.isAvailable()) {
                            servers.getDataservers().remove(heartbeatReceived.getIdServer());
                            System.out.println("Removed server because is unavailable" + heartbeatReceived.getIdServer());
                            server.sendList();
                            continue;
                        }

                        if (VersionDB > myVersionDB)  {
                            server.setRead(false);
                            heartbeatPersonal.setAvailable(false);
                            System.out.println("Detected better Database");
                            server.getTcpManager().close();
                            TCPConnection(heartbeatReceived);
                            server.getTcpManager().create();
                            heartbeatPersonal.setAvailable(true);
                            server.setRead(true);
                        }

                        if (!servers.getDataservers().contains(ServersInfo.getDummyServerInfo(heartbeatReceived.getIdServer()))) {
                            servers.getDataservers().add(new ServersInfo(heartbeatReceived.getPortTCPRegular(), heartbeatReceived.getPortTCPAsync(), heartbeatReceived.getIpTCP(), heartbeatReceived.getIdServer(), heartbeatReceived.getActiveConnections()));
                            System.out.println("Added new server " + heartbeatReceived.getIdServer());
                            servers.getDataserverID(heartbeatReceived.getIdServer()).setLastHB(Instant.now());
                            server.sendList();
                            continue;
                        }

                        servers.getDataservers().set(
                                servers.getDataservers().indexOf(ServersInfo.getDummyServerInfo(heartbeatReceived.getIdServer())),
                                new ServersInfo(heartbeatReceived.getPortTCPRegular(), heartbeatReceived.getPortTCPAsync(), heartbeatReceived.getIpTCP(), heartbeatReceived.getIdServer(), heartbeatReceived.getActiveConnections()));
                        server.sendList();
                        servers.getDataserverID(heartbeatReceived.getIdServer()).setLastHB(Instant.now());
                        //System.out.println("Replaced server " + heartbeat.getIdServer());


                    } else if ((aux instanceof Prepare)) {
                        prepare = (Prepare) aux;
                        if(Objects.equals(prepare.getIdServerOrigin(), server.getServerMulticastId()))
                            continue;
                        System.out.println("<---- Received Prepare ");
                        server.setRead(false);
                        int port = prepare.getPort();
                        socket = new DatagramSocket();
                        socket.setSoTimeout(5000);
                        boutDB = new ByteArrayOutputStream();
                        ooutDB = new ObjectOutputStream(boutDB);
                        confirmation = new Confirmation();
                        ooutDB.writeUnshared(confirmation);
                        packet = new DatagramPacket(boutDB.toByteArray(),boutDB.size(),InetAddress.getByName(prepare.getIp()),port);
                        socket.send(packet);
                        socket.close();
                        prepareReady = prepare;
                        server.setRead(true);
                    }else if((aux instanceof Commit)){
                        Commit commit = (Commit) aux;
                        if(Objects.equals(commit.getServerID(), server.getServerMulticastId()))
                            continue;
                        server.setRead(false);
                        System.out.println("<---- Received Commit");
                        if(prepare != null)
                            updateDatabase(prepare);
                        server.setRead(true);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Error receiving the Heartbeat");

                } catch (IOException e) {
                    System.err.println("Sockets failed");
                    server.close();
                    continue;
                }
            }
        }

    public void updateDatabase(Prepare prepare){
        switch(prepare.getType()){
            case REGISTER -> {
                db.registUser((String) prepare.getData().get(0), (String) prepare.getData().get(1), (String) prepare.getData().get(2));
            }
            case LOGIN -> {
                try {
                    db.loginUser((String) prepare.getData().get(0), (String) prepare.getData().get(1),(ClientInfo) prepare.getData().get(2));
                } catch (SQLException e) {
                    System.err.println("Error updating Database");
                }
            }
            case LOGOUT -> {
                db.logout((String) prepare.getData().get(0));
            }
            case EDIT_USERNAME -> {
                db.editUsername((String) prepare.getData().get(0), (String) prepare.getData().get(1));
            }
            case EDIT_PASSWORD ->  {
                try {
                    db.editPassword((String) prepare.getData().get(0), (String) prepare.getData().get(1));
                } catch (SQLException e) {
                    System.err.println("Error updating Database");
                }
            }
            case EDIT_NAME -> {
                db.editName((String) prepare.getData().get(0), (String) prepare.getData().get(1));
            }
            case RESERVATIONS -> {
                switch(prepare.getSb()) {
                    case PAY_RESERVATION -> {
                        db.payReservation(Integer.parseInt((String) prepare.getData().get(0)) , (String) prepare.getData().get(1));
                    }
                    case DELETE_RESERVATION -> {
                        db.deleteReservation(Integer.parseInt((String) prepare.getData().get(0)) , (String) prepare.getData().get(1));
                    }
                    case ADD_RESERVATION -> {
                        db.nPaidReservation((Seat) prepare.getData().get(0), Integer.parseInt((String) prepare.getData().get(1)), (String) prepare.getData().get(2));
                    }
                }
                db.notifyClientUpdate(true);
            }
            case ADMIN_SHOW -> {
                switch(prepare.getSb()) {
                    case INSERT_SHOW -> {
                        ArrayList<Seat> seats = (ArrayList<Seat>) prepare.getData().get(0);
                        String description = (String) prepare.getData().get(1);
                        String type = (String) prepare.getData().get(2);
                        String date = (String) prepare.getData().get(3);
                        int duration = Integer.parseInt((String) prepare.getData().get(4));
                        String local = (String) prepare.getData().get(5);
                        String city = (String) prepare.getData().get(6);
                        String country = (String) prepare.getData().get(7);
                        String ageRate = (String) prepare.getData().get(8);
                        int visible = Integer.parseInt((String) prepare.getData().get(9));


                        Show aux = new Show(description, type, date, duration, local, city, country, ageRate, visible);
                        aux.setSeats(seats);
                        db.addShow(aux, (String) prepare.getData().get(10));
                    }
                    case MAKE_SHOW_AVAILABLE -> {
                        db.makeShowVisible(Integer.parseInt((String) prepare.getData().get(0)), (String) prepare.getData().get(1));
                    }
                    case DELETE_SHOW -> {
                        db.deleteShow(Integer.parseInt((String) prepare.getData().get(0)), (String) prepare.getData().get(1));
                    }
                }
                db.notifyClientUpdate(true);
            }
        }

    }

    public void TCPConnection(Heartbeat heartbeat) {
        String location;
        try {
            Socket socket = new Socket(heartbeat.getIpTCPDB(), heartbeat.getPortTCPDB());
            socket.setSoTimeout(7000);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            FileOutputStream fos = new FileOutputStream(db.getDATABASE_URL_LOCAL());
            DatabaseSeri dados = null;
            out.writeUnshared(dados);
            byte[] msgBuffer = new byte[4000];
            int nbytes;
            do {
                nbytes = in.read(msgBuffer);
                if(nbytes == -1)
                    break;
                fos.write(msgBuffer,0,nbytes);
            } while (nbytes > 0);

            socket.close();
        } catch (IOException e){
            System.err.println ("Database Update failed");
        }
    }

    public void veriTime(){
        Iterator it = servers.getDataservers().iterator();

        while(it.hasNext()){
            ServersInfo si = (ServersInfo)it.next();
            long a = Duration.between(si.getLastHB(), Instant.now()).toSeconds();
            if(a>=35){
                System.out.println("Removed server because of time " + heartbeatReceived.getIdServer());
                it.remove();
            }
        }
    }

}

