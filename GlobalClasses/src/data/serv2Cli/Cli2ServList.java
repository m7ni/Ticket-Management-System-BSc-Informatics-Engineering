package data.serv2Cli;

import data.cli2Serv.seriObjects.ServersInfo;
import java.util.ArrayList;


public class Cli2ServList extends Serv2Cli{
    private static final long serialVersionUID = 10l;
    private ArrayList<ServersInfo> dataservers;

    public Cli2ServList(){
        super(RequestType.LIST);
        dataservers= new ArrayList<>();
    }

    public void add (ServersInfo a){
        dataservers.add(a);
    }


    public ArrayList<ServersInfo> getDataservers() {
        return dataservers;
    }

    public ServersInfo getDataserverID(String id) {
       for(ServersInfo si : dataservers)
           if(si.getIdServer().equalsIgnoreCase(id))
               return si;

        return null;
    }

}