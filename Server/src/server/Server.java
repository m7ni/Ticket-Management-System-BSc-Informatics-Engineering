package server;

import data.cli2Serv.seriObjects.ServersInfo;
import data.serv2Cli.Cli2ServList;
import db.DataBase;
import data.serv2Cluster.Heartbeat;
import server.Connectivity.MultiCast;
import server.Connectivity.TcpManager;
import server.Connectivity.TcpManagerUpdateDB;
import server.data.ClientInfo;
import server.data.threads.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import static java.lang.Thread.sleep;
import static server.data.threads.ThreadSendHeartbeat.MAX_SIZE;


public class Server{
    static final String INICIALDB = "Server\\src\\db\\";
    private static String urlDB;
    private static int listeningPort;
    private Integer portUDP;
    private String dataBaseDirectory;
    private Cli2ServList serverList;
    final String serverMulticastId;
    public final long serverStartTimestamp = System.nanoTime();
    private Heartbeat heartbeat;
    private MultiCast multicast;
    private ArrayList<ClientInfo> clients;
    private ServersInfo si;
    private Boolean read = true;
    private Thread threadReceiveHb;
    private Thread ThreadUpdateDataBase;
    private Thread ThreadConnectClient;
    private Thread ThreadAcceptClient;
    private Thread ThreadSendHb;
    private DataBase db;
    private TcpManagerUpdateDB tcpManagerUpdateDB;
    private TcpManager tcpManager;

    public TcpManager getTcpManager() {
        return tcpManager;
    }

    public Server(int port, String directory, String dbURL) throws IOException, InterruptedException {

        clients = new ArrayList<>();
        this.portUDP = port;
        this.dataBaseDirectory = directory + dbURL;
        serverMulticastId = UUID.randomUUID().toString();
        System.out.println("ID: " +serverMulticastId );
        serverList = new Cli2ServList();
        InetAddress ipServer = InetAddress.getByName("127.0.0.1");

        db = new DataBase(dataBaseDirectory,this);

        multicast = new MultiCast(this);

        tcpManagerUpdateDB = new TcpManagerUpdateDB(this);

        heartbeat = new Heartbeat(ipServer,true,db.getVersionDB(),serverMulticastId,tcpManagerUpdateDB.getPortDB(),tcpManagerUpdateDB.getIpDB());

         tcpManager = new TcpManager(this, heartbeat);

        ThreadUpdateDataBase= new ThreadUpdateDatabase(tcpManagerUpdateDB.getServerSocketUpdateDB(),db,this);
        ThreadUpdateDataBase.start();

        threadReceiveHb = new ThreadReceiveMulticast(multicast.getMulticastSocket(), heartbeat, serverList, db,this);
        threadReceiveHb.start();

        sleep(5*1000);
        ThreadConnectClient =  new ThreadConnectClient(portUDP,serverList,this);
        ThreadConnectClient.start();

        ThreadSendHb = new ThreadSendHeartbeat(multicast.getMulticastSocket(), heartbeat,this,multicast.getPort(),multicast.getGroup());
        ThreadSendHb.start();

        ThreadAcceptClient= new ThreadAcceptClient(tcpManager.getServerSocketRegular(),tcpManager.getServerSocketAsync(),db,this);
        ThreadAcceptClient.start();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        /*if(args.length != 2)
            System.err.println("Insuficient number of Arguments");
        else
            new Server(Integer.parseInt(args[0]),INICIALDB,args[1]);*/

        Scanner sc = new Scanner(System.in);

        System.out.print("Port: ");
        String port = sc.nextLine();

        System.out.print("DB name: ");
        String dbURL = sc.nextLine();

        new Server(Integer.parseInt(port),INICIALDB,dbURL);

    }

    public void close(){
        try {
            if(multicast!=null) multicast.getMulticastSocket().close();
            if(tcpManager.getServerSocketRegular() != null) tcpManager.getServerSocketRegular().close();
            if(tcpManager.getServerSocketAsync() != null) tcpManager.getServerSocketAsync().close();
            if(tcpManagerUpdateDB.getServerSocketUpdateDB()!= null) tcpManagerUpdateDB.getServerSocketUpdateDB().close();
            if(ThreadAcceptClient!= null) ThreadAcceptClient.interrupt();
            if(ThreadSendHb!= null) ThreadSendHb.interrupt();
            if(ThreadConnectClient!= null) ThreadConnectClient.interrupt();
            if(threadReceiveHb!= null) threadReceiveHb.interrupt();
            if(ThreadUpdateDataBase!= null) ThreadUpdateDataBase.interrupt();
            System.exit(0);
        } catch (IOException ignored) {
        }
    }
    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

    public MultiCast getMulticast() {
        return multicast;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public void sendList(){
        synchronized (clients){
            for(ClientInfo ci: clients){
                if(ci == null)
                    continue;
                try {
                    ci.getOssAsync().writeUnshared(serverList);
                } catch (IOException e) {
                    removeClient();
                }
            }
        }

    }

    public ArrayList<ClientInfo> getClients() {
        return clients;
    }

    public synchronized void removeClient(){
        heartbeat.removeActiveConnection();
        si.setActiveConnections(heartbeat.getActiveConnections());
        sendHeartbeat();
        sendList();
    }

    public ServersInfo getSi() {
        return si;
    }

    public void setSi(ServersInfo si) {
        this.si = si;
    }

    public void removeClients(ClientInfo client) {
        clients.remove(client);
    }

    public void sendHeartbeat(){
        DatagramPacket pkt;
        ByteArrayInputStream bin;
        ObjectInputStream oin;

        pkt = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
        try{
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            heartbeat.setLastHBTime(Instant.now());
            oout.writeUnshared(heartbeat);
            multicast.getMulticastSocket().send(new DatagramPacket(bout.toByteArray(),bout.size(),multicast.getGroup(),multicast.getPort()));
        } catch(IOException e){
            System.err.println("Couldn't send heartbeat");
        }
    }

    public Cli2ServList getServerList() {
        return serverList;
    }

    public String getServerMulticastId() {
        return serverMulticastId;
    }

}
