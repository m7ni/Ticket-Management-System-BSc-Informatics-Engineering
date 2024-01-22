package server.data.threads;

import db.DataBase;
import server.Server;
import server.data.ClientInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class ThreadAcceptClient extends Thread{
    private ServerSocket socketAsync;
    private DataBase db;
    private ServerSocket socketRegular;
    private Socket toClientSocketRegular;
    private Socket toClientSocketAsync;
    private Server server;
    private boolean exit = false;

    public ThreadAcceptClient(ServerSocket socketRegular, ServerSocket serverSocketAsync, DataBase db, Server server) {
        this.socketRegular = socketRegular;
        this.socketAsync = serverSocketAsync;
        this.db = db;
        this.server = server;
    }


    @Override
    public void interrupt() {
        exit = true;
    }

    @Override
    public void run() {
        while(!exit){
            try {
                toClientSocketRegular = socketRegular.accept();
                toClientSocketAsync = socketAsync.accept();
                System.out.println("New client connected");

                synchronized (server.getHeartbeat()){
                    server.getHeartbeat().addActiveConnection();
                }

                ClientInfo nc = new ClientInfo(toClientSocketAsync,toClientSocketRegular);
                synchronized (server.getClients()){
                    server.getClients().add(nc);
                }

                new ThreadClient(toClientSocketRegular,toClientSocketAsync,db,nc,server,server.getMulticast().getMulticastSocket()).start();

                server.sendHeartbeat();
            }catch (SocketTimeoutException e){
                if(exit)
                    break;
            } catch (IOException e) {

            }
        }
    }
}
