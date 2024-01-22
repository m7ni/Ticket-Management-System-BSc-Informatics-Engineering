package data.cli2Serv.seriObjects;

import db.DataBase;

import java.io.Serializable;

public class DatabaseSeri implements Serializable {
    private static final long serialVersionUID = 10l;
    DataBase db;

    public DatabaseSeri(DataBase db) {
        this.db = db;
    }
}
