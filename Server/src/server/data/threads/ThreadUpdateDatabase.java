package server.data.threads;

import data.cli2Serv.seriObjects.DatabaseSeri;
import db.DataBase;
import server.Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ThreadUpdateDatabase extends Thread{
    private DataBase db;
    private ServerSocket socketDB;
    private Socket toServerSocketDB;
    private Server server;
    private DatabaseSeri dados;
    private boolean exit = false;
    public ThreadUpdateDatabase(ServerSocket socket, DataBase db, Server server) {
        this.socketDB = socket;
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
                toServerSocketDB = socketDB.accept();
                System.out.println("Sending Database information to another Server");
                ObjectOutputStream ous = new ObjectOutputStream(toServerSocketDB.getOutputStream());
                FileInputStream fis = new FileInputStream(db.getDATABASE_URL_LOCAL());
                int nBytes = 0;

                while(fis.available()>0){
                    byte[] fileChunk = new byte[4000];
                    nBytes = fis.read(fileChunk);
                    ous.write(fileChunk,0,nBytes);
                }
                toServerSocketDB.close();
            }catch (SocketTimeoutException e){
                if(exit)
                    break;
            } catch (IOException e) {
                System.err.println("Error sending database to other Server");
            }

        }
    }
}
