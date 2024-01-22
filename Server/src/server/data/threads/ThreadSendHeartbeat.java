package server.data.threads;

import data.serv2Cluster.Heartbeat;
import server.Server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ThreadSendHeartbeat extends Thread{
    public static final String LIST = "LIST";
    public static String EXIT = "EXIT";
    public static int MAX_SIZE = 1000;
    protected String id;
    protected Heartbeat heartbeat;
    protected MulticastSocket s;
    protected boolean exit = false;
    protected InetAddress group;
    protected int port;
    protected Server sv;

    public ThreadSendHeartbeat(MulticastSocket s, Heartbeat info, Server server, int port, InetAddress ip) {
        this.s = s;
        id=info.getIdServer();

        heartbeat = info;
        this.group=ip;
        this.port = port;
        sv = server;
    }

    @Override
    public void interrupt() {
        exit = true;
    }

    public void run() {
        DatagramPacket pkt;
        ByteArrayInputStream bin;
        ObjectInputStream oin;

        if(s == null || exit){return;}

        while(!exit){
            pkt = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);

            try{
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout);
                oout.writeUnshared(heartbeat);
                s.send(new DatagramPacket(bout.toByteArray(),bout.size(),group,port));
                sleep(10*1000);
            } catch(IOException e){
                System.err.println("Couldn't send heartbeat");
            } catch (InterruptedException ignored) {
            }
        }

    }
}
