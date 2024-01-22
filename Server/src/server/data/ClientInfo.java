package server.data;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class ClientInfo implements Serializable {
    private static final long SerialVersionUID = 10l;

    transient private Socket socketAsync;
    transient private Socket socketRegular;
    private int id;
    transient private ObjectOutputStream ossAsync;

    public ClientInfo(Socket socketAsync, Socket socketRegular) {
        this.socketAsync = socketAsync;
        this.socketRegular = socketRegular;

        try {
            ossAsync = new ObjectOutputStream(socketAsync.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectOutputStream getOssAsync() {
        return ossAsync;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
