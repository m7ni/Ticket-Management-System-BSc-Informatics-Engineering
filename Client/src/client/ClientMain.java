package client;

import client.logic.Client;
import client.logic.LocalData;
import client.ui.ClientUI;
import client.utils.PDInput;

import java.io.IOException;
import java.net.ConnectException;

public class ClientMain {
    public static void main(String[] args) {
        try{
/*
            if(args.length == 2) {
                Client logic = new Client(args);
                LocalData lc = new LocalData(logic);
                logic.setLd(lc);
                ClientUI ui = new ClientUI(logic,lc);
                if(logic.getNoServer())
                    return;
                ui.start();
            }
            else
                throw new IllegalArgumentException("Invalid arguments! You must use one of the following formatting: <PortUDP> <IPServer>");*/

            String[] argms = new String[2];
            argms[0]= PDInput.readString("portUDP: ", true);
            argms[1] = PDInput.readString("IPServer: ", true);

            Client logic = new Client(argms);
            LocalData lc = new LocalData(logic);
            logic.setLd(lc);
            ClientUI ui = new ClientUI(logic,lc);
            if(logic.getNoServer())
                return;
            ui.start();
        } catch (ConnectException e) {
            System.err.println("It is not possible to run the server since not all initial conditions are met!");
        }
        catch (IllegalArgumentException | IOException e){
            System.err.println(e.getMessage());
        }
    }
}
