package client.logic;

import data.serv2Cli.Cli2ServList;
import data.serv2Cli.S2CUpdateData;
import data.serv2Cli.Serv2Cli;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;


public class ThreadAsync extends Thread {
    private Socket sCli;
    private ObjectInputStream inServ;
    private Client client;
    private boolean exit =false;

    public ThreadAsync(Socket sCli, ObjectInputStream inServ, Client client) {
        this.sCli = sCli;
        this.inServ = inServ;
        this.client = client;
    }

    @Override
    public void interrupt() {
        exit = true;
    }

    @Override
    public void run() {
        Serv2Cli servMessage = null;


        while (!exit) {
            try {
                servMessage = (Serv2Cli) inServ.readUnshared();
            } catch (IOException | ClassNotFoundException e) {
                exit = true;
                client.ConnectServer();
                continue;
            }

            switch (servMessage.getRt()) {
                case LIST -> {
                    Cli2ServList list = (Cli2ServList) servMessage;
                    synchronized (client.getServersList()) {
                        client.setServersList(list.getDataservers());
                    }
                }
                case UPDATE -> {
                    S2CUpdateData data = (S2CUpdateData) servMessage;
                    synchronized (client.getLd()) {
                        for(Serv2Cli.SubType type : data.getMap().keySet()) {
                            if(type == Serv2Cli.SubType.SHOWS_CLIENT) {
                                client.getLd().setShows(data.getMap().get(type));
                                client.getLd().setShowsNotAvailable(data.getMap().get(type));
                            }
                            if(type == Serv2Cli.SubType.SHOW_NR) {
                                client.getLd().setShows2Delete(data.getMap().get(type));
                            }
                        }
                        client.getLd().setNotPaidReservations(data.getNotPaidReservations());
                        client.getLd().setPaidReservations(data.getPaidReservations());
                    }
                    synchronized (this){
                        if(data.isRefresh()) {
                            System.out.println("\nNew information just arrived!\n");
                            client.setExistChange(true);
                        }
                    }
                }
            }
        }
    }
}