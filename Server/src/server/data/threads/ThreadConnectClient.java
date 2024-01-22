package server.data.threads;

import data.serv2Cli.Cli2ServList;
import server.Server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Collections;

public class ThreadConnectClient extends Thread{
    public static final int MAX_SIZE = 10000;
    private Cli2ServList servers;
    private int listeningPort;
    private DatagramSocket socket = null;
    private DatagramPacket packet;
    private ByteArrayInputStream bin;
    private ObjectInputStream oin;
    private ByteArrayOutputStream bout;
    private ObjectOutputStream oout;
    private Cli2ServList receive;
    private boolean exit = false;
    private Server server;
    public ThreadConnectClient (int listeningPort, Cli2ServList serversList,Server server){
        this.listeningPort=listeningPort;
        this.servers=serversList;
        this.server = server;
    }

    @Override
    public void interrupt() {
        exit = true;
    }

    @Override
    public void run() {
            try {
                socket = new DatagramSocket(listeningPort);

                while (!exit) {

                    packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
                    socket.receive(packet);

                    bin = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    oin = new ObjectInputStream(bin);
                    receive = (Cli2ServList) oin.readUnshared();

                    Collections.sort(servers.getDataservers());
                    bout = new ByteArrayOutputStream();
                    oout = new ObjectOutputStream(bout);
                    oout.writeUnshared(servers);
                    packet.setData(bout.toByteArray());
                    packet.setLength(bout.size());

                    socket.send(packet);
                }

            } catch (Exception e) {
                System.err.println("That port is already being used try again later");
                server.close();
                return;
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }
}




