package db;

import data.cli2Serv.seriObjects.Reservation;
import data.cli2Serv.seriObjects.Seat;
import data.cli2Serv.seriObjects.Show;
import data.serv2Cli.S2CUpdateData;
import data.serv2Cli.Serv2Cli;
import server.Server;
import server.data.ClientInfo;

import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DataBase {
    private String DATABASE_URL_ORIGINAL = "Server\\original.db";
    private String DATABASE_URL_NDB = "Server\\db\\localDB.db";
    private String DATABASE_URL_LOCAL = null;
    Connection conn = null;
    ResultSet rs = null;
    Statement stmt = null;
    private Server server;
    public String getDATABASE_URL_LOCAL() {
        return DATABASE_URL_LOCAL;
    }

     public DataBase(String dataBase, Server server) {
        DATABASE_URL_LOCAL = dataBase;
        this.server  = server;
        File fileTem = new File(DATABASE_URL_LOCAL);
        try {
            if (!fileTem.exists()){
                createDatabase(dataBase);
            }
            conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_URL_LOCAL); //depois trocar para a string
            stmt = conn.createStatement();
            System.out.println("Successfully connected to the database");

        } catch (SQLException e) {
            System.out.println("Couldn't Connect to database. Closing...");
           server.close();
        }
    }

    public synchronized void createDatabase(String dataBase){
        int nBytes;
        byte[] content = new byte[10000];
        try {
                  File f = new File(dataBase);
            f.createNewFile();

            FileInputStream fis = new FileInputStream(DATABASE_URL_ORIGINAL);
            FileOutputStream fos = new FileOutputStream(f);

            while((nBytes = fis.read(content))>0){
                fos.write(content,0,nBytes);
            }

        } catch (IOException e) {
            System.err.println("Failed Creating the new Database");
            server.close();
        }

    }

    public synchronized void close() throws SQLException {
        if (conn != null)
            conn.close();
    }

    public synchronized boolean registUser(String username, String name, String password)  {
        boolean success = false;
        int admin =0;

        if(name.equals("admin") && password.equals("admin"))
            admin = 1;

       String sqlQueryRegist = "INSERT INTO utilizador (username, nome, password,administrador) VALUES ("+String.format("'%s', '%s', '%s','%d')", username, name, password,admin);

        try {
            stmt.executeUpdate(sqlQueryRegist);
            success = true;
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            success = false;
        }

        updateVersion();
        return success;
    }
    public synchronized UserType loginUser(String username, String password, ClientInfo a) throws SQLException {
        Statement statement = conn.createStatement();
        String sqlQuery = "SELECT username,administrador,id FROM utilizador WHERE password like '" + password + "' AND username like '" + username + "'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);
        if(!resultSet.next())
            return null;
        a.setId(resultSet.getInt("id"));

        int admin = resultSet.getInt("administrador");

        String queryAuthenticate = "UPDATE utilizador SET autenticado = 1 WHERE id = "+ a.getId();
        try {
            statement.executeUpdate(queryAuthenticate);
        } catch (SQLException e) {
            System.err.println("Error updating Database");
        }

        updateVersion();
       if(admin == 1)
           return UserType.ADMIN;
       return UserType.REGULAR;

    }
    public synchronized int nPaidReservation(Seat seat, int pay, String username) {

        int idReserva = -1;
        try {
            String query = "SELECT id FROM utilizador WHERE username like '" + username + "'";
            ResultSet resultSet = stmt.executeQuery(query);
            if (!resultSet.next())
                return -1;
            int idUser = resultSet.getInt("id");

            query = "INSERT INTO reserva(data_hora,pago,id_utilizador,id_espetaculo) VALUES ('" + new Date().toString() + "','" + pay + "','" + idUser + "', '" + seat.getIdShow() + "') returning id";

            resultSet = stmt.executeQuery(query);
            if (!resultSet.next())
                return -1;
           idReserva = resultSet.getInt("id");

            query = "SELECT id FROM lugar WHERE espetaculo_id like '" + seat.getIdShow() + "' AND fila LIKE'" + seat.getLine() + "' AND assento LIKE '" + seat.getPlace() + "'";
            resultSet = stmt.executeQuery(query);
            if (!resultSet.next())
                return -1;
            int idLugar = resultSet.getInt("id");


            query = "INSERT INTO reserva_lugar(id_reserva,id_lugar) VALUES ('" + idReserva + "','" + idLugar + "')";
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.err.println("Error updating Database");
        }
        updateVersion();
        return idReserva;
    }

    public synchronized int listMaxVersion() throws SQLException {
        Statement statement = conn.createStatement();
        String sqlQuery = "SELECT *  FROM versao";
        ResultSet resultSet = statement.executeQuery(sqlQuery);
        int vs = resultSet.getInt("versao");
        statement.close();
        return vs;
    }

    public synchronized int updateVersion() {
        Statement statement = null;
        int vs = 0;
        try {
            statement = conn.createStatement();
            int nv = listMaxVersion();
            nv++;
            String sqlQuery = "UPDATE versao set versao='" + nv + "'";
            statement.executeUpdate(sqlQuery);
            vs = getVersionDB();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error updating Database");
        }

        return vs;
    }

    public synchronized boolean editUsername(String newUsername, String username) {
        Statement statement = null;
        try {
                statement = conn.createStatement();
                String sqlQueryExist = "UPDATE utilizador SET username = '" + newUsername.toLowerCase() + "' WHERE username like '" + username.toLowerCase() + "'";
                statement.executeUpdate(sqlQueryExist);

        } catch (SQLException e) {
            System.err.println("Error updating Database");
            return false;
        }
        updateVersion();
        return true;
    }

    public synchronized boolean editName(String newName, String username) {
        try {
            Statement statement = conn.createStatement();
            String sqlQueryExist = "UPDATE utilizador SET nome = '" + newName + "' WHERE username like '" + username + "'";
            statement.executeUpdate(sqlQueryExist);
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            return false;
        }
        updateVersion();
        return true;
    }

    public synchronized boolean editPassword(String newPassword, String username) throws SQLException {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            String sqlQueryExist = "UPDATE utilizador SET password = '" + newPassword + "' WHERE username like '" + username + "'";
            statement.executeUpdate(sqlQueryExist);
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            return false;
        }
        updateVersion();
        return true;
    }

    public synchronized boolean addShow(Show show, String Username) {
        boolean success = false;

        try {
            Statement statementConfirm = conn.createStatement();
            String sqlQueryConfirm = "SELECT descricao FROM espetaculo WHERE descricao = '" + show.getDescription() + "'";
            ResultSet resultSetConfirm = statementConfirm.executeQuery(sqlQueryConfirm);
            if(resultSetConfirm.next())
                return false;

            Statement statement = conn.createStatement();
            String sqlQueryRegist = "INSERT INTO espetaculo(descricao,tipo,data_hora,duracao,local,localidade,pais,classificacao_etaria,visivel) VALUES ('" + show.getDescription() + "','" + show.getType() + "','" + show.getDate() + "','" + show.getDuration() + "','"+ show.getLocal() + "','" + show.getCity() + "','" + show.getCountry() + "','" + show.getAgeRate() + "',0)";
            statement.executeUpdate(sqlQueryRegist);


            String sqlQuery = "SELECT id FROM espetaculo WHERE descricao like '" + show.getDescription() + "'" ;
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            if(!resultSet.next())
                return false;

            int id = resultSet.getInt("id");

            addSeats(show.getSeats(), id);

            statement.close();
            success = true;
            updateVersion();
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            success = false;
        }

        return success;
    }

    public synchronized boolean addSeats(ArrayList<Seat> seats, int id) {
        boolean success = false;
        Statement statement = null;
        try {
            statement = conn.createStatement();
            for(Seat s : seats) {
                String sqlQueryRegist = "INSERT INTO lugar(fila,assento,preco,espetaculo_id) VALUES ('" + s.getLine() + "','" + s.getPlace() + "','" + s.getPrice() + "', '" + id + "')";
                statement.executeUpdate(sqlQueryRegist);
                success = true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating Database");
        }

        updateVersion();
        return success;
    }

    public synchronized int getVersionDB() {
        try {
            String query = "SELECT * FROM versao";
            ResultSet results = stmt.executeQuery(query);
            return results.getInt("versao");
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            return 0;
        }
    }

    public synchronized ArrayList<Reservation> consultNotPaidReservation(int idUser) {
        try {
            ArrayList<Reservation> reservationsNotPayed = new ArrayList<>();
            String query = "SELECT * FROM reserva WHERE pago = 0 AND id_utilizador = " + idUser;
            reservationsNotPayed = getReservas(query);
            return reservationsNotPayed;
        }catch (SQLException e){
            System.err.println("Error updating Database");
            return null;
        }
    }

    public synchronized ArrayList<Reservation> consultPaidReservation(int idUser){
        try {
            ArrayList<Reservation> reservationsPayed = new ArrayList<>();
            String query = "SELECT * FROM reserva WHERE pago = 1 AND id_utilizador =" + idUser ;
            reservationsPayed = getReservas(query);
            return reservationsPayed;
        }catch (SQLException e){
            System.err.println("Error updating Database");
            return null;
        }

    }

    private synchronized ArrayList<Reservation> getReservas( String query)throws SQLException {

        ArrayList<Reservation> reservationsNotPayed = new ArrayList<>();
        rs = stmt.executeQuery(query);

        while(rs.next())
            reservationsNotPayed.add(new Reservation(rs.getInt("id"),rs.getString("data_hora"),rs.getInt("pago"),rs.getInt("id_utilizador"),rs.getInt("id_espetaculo")));

        return reservationsNotPayed;
    }

    public synchronized Boolean payReservation(int idReservation, String username) {

        try {
            String query = "UPDATE reserva SET pago = 1 WHERE id= " +idReservation;
            stmt.executeUpdate(query);
            updateVersion();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            return false;
        }
    }

    public synchronized boolean deleteReservation(int idReservation, String username) {
        try{
            String query = "DELETE FROM reserva WHERE pago = 0 and id = "+idReservation;
            stmt.executeUpdate(query);
            query = "DELETE FROM reserva_lugar WHERE id_reserva = "+idReservation;
            stmt.executeUpdate(query);
            updateVersion();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            return false;
        }
    }

    public synchronized boolean makeShowVisible(int id, String username) {
        try {
            String query = "UPDATE espetaculo set visivel=1 Where id=" +id;
            stmt.executeUpdate(query);
            updateVersion();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            return false;
        }
    }

    public synchronized boolean deleteShow(int id, String username) {
        String queryVerifyShow = "SELECT id FROM espetaculo WHERE id = " + id ;
        String queryVerify = "SELECT * FROM reserva WHERE id_espetaculo = "+ id + " AND pago = 1";
        String query1 = "DELETE FROM espetaculo WHERE id =" + id;
        String query2 = "DELETE FROM lugar WHERE espetaculo_id =" + id;
        String queryResNotPaid = "DELETE FROM reserva WHERE id_espetaculo= '" + id + "' returning id";

        try {
            ResultSet resultSetVerifyShow = stmt.executeQuery(queryVerifyShow);
            if(!resultSetVerifyShow.next())
                return false;
            ResultSet resultSetVerify = stmt.executeQuery(queryVerify);
            if(resultSetVerify.next())
                return false;
            stmt.executeUpdate(query1);
            stmt.executeUpdate(query2);
            ResultSet resultSet = stmt.executeQuery(queryResNotPaid);
            if (resultSet.next()) {
                int idReserva = resultSet.getInt("id");
                String queryResLugar = "DELETE FROM reserva_lugar WHERE id_reserva=" + idReserva;
                stmt.executeUpdate(queryResLugar);
            }

            updateVersion();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            return false;
        }
    }

    public synchronized void consultShows(List<Show> show) {
        try {
            String sqlQuery = "SELECT * FROM espetaculo";
            ResultSet rs = stmt.executeQuery(sqlQuery);

            while(rs.next()){
                ArrayList<Seat> seat = new ArrayList<>();
                int idShow = rs.getInt("id");
                Show aux = new Show(idShow,rs.getString("descricao"),rs.getString("tipo"),
                        rs.getString("data_hora"),rs.getInt("duracao"),rs.getString("local"),rs.getString("localidade")
                        ,rs.getString("pais"),rs.getString("classificacao_etaria"),rs.getInt("visivel"));

                show.add(aux);

                String sqlSeats = "SELECT * FROM lugar WHERE espetaculo_id = "+ idShow;
                Statement stmtSeats = conn.createStatement();
                ResultSet rs1= stmtSeats.executeQuery(sqlSeats);

                while(rs1.next()){
                    int idSeat = rs1.getInt("id");
                    String sqlAvailable = "SELECT * FROM reserva_lugar WHERE id_lugar = "+ idSeat;
                    Statement stmtSeats1 = conn.createStatement();
                    ResultSet rs2= stmtSeats1.executeQuery(sqlAvailable);
                    if(!rs2.next())
                        seat.add(new Seat(idSeat,rs1.getString("fila"),rs1.getString("assento"),rs1.getInt("preco"),true,idShow));
                }
                aux.setSeats(seat);
            }
        } catch (SQLException e) {
            System.err.println("Error updating Database");
        }
    }

    public synchronized void showsNR(List<Show> show) {
        try {
            String sqlQuery = "SELECT * FROM espetaculo" ;
            ResultSet rs = stmt.executeQuery(sqlQuery);
            while(rs.next()){
                boolean flag = false;
                int id = rs.getInt("id");
                Show aux = new Show(id,rs.getString("descricao"),rs.getString("tipo"),
                        rs.getString("data_hora"),rs.getInt("duracao"),rs.getString("local"),rs.getString("localidade")
                        ,rs.getString("pais"),rs.getString("classificacao_etaria"),rs.getInt("visivel"));

                String sqlSeats = "SELECT * FROM lugar WHERE espetaculo_id = "+ id;
                Statement stmtSeats = conn.createStatement();
                ResultSet rs1= stmtSeats.executeQuery(sqlSeats);

                while(rs1.next()){
                    int idSeat = rs1.getInt("id");
                    String sqlAvailable = "SELECT * FROM reserva_lugar WHERE id_lugar LIKE '"+ idSeat + "'";
                    Statement stmtSeats1 = conn.createStatement();
                    ResultSet rs2= stmtSeats1.executeQuery(sqlAvailable);

                    if(rs2.next()) {
                        int idRes = rs2.getInt("id_reserva");
                        String sqlReservation = "SELECT * FROM reserva WHERE id = '" + idRes + "' AND pago = 1";
                        Statement stmtRes = conn.createStatement();
                        ResultSet rs3 = stmtRes.executeQuery(sqlReservation);
                        if(rs3.next())
                            flag = true;
                    }
                }
                if(!flag)
                    show.add(aux);
            }
        } catch (SQLException e) {
            System.err.println("Error updating Database");
        }
    }

    public void logout(String cliUsername) {
        String query = "UPDATE utilizador SET autenticado = 0 Where username like '" + cliUsername + "'";
        try {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
           System.err.println("Error updating Database LOGOUT");
        }
    }

    public void notifyClientUpdate(Boolean refresh) {
        synchronized (server.getClients()) {
            for (ClientInfo clientInfo : server.getClients()) {
                S2CUpdateData update = new S2CUpdateData();
                update.setRefresh(refresh);
                HashMap<Serv2Cli.SubType, ArrayList<Show>> map = new HashMap<>();
                ArrayList<Show> shows = new ArrayList<>();
                showsNR(shows);
                map.put(Serv2Cli.SubType.SHOW_NR, shows);

                TimeUnit tu = TimeUnit.HOURS;
                ArrayList<Show> shows1 = new ArrayList<>();
                SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy HH mm");
                Date timeNow = new Date();
                String auxtimeNow = formatter.format(timeNow);
                consultShows(shows1);

                try {
                    timeNow = formatter.parse(auxtimeNow);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Iterator it = shows1.iterator();
                while (it.hasNext()) {
                    Show show = (Show) it.next();
                    Date timeShow = null;
                    try {
                        timeShow = formatter.parse(show.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long diff = timeShow.getTime() - timeNow.getTime();
                    if (tu.convert(diff, TimeUnit.HOURS) < 24)
                        it.remove();
                }
                map.put(Serv2Cli.SubType.SHOWS_CLIENT, shows1);

                update.setMap(map);
                update.setPaidReservations(consultPaidReservation(clientInfo.getId()));
                update.setNotPaidReservations(consultNotPaidReservation(clientInfo.getId()));

                try {
                    clientInfo.getOssAsync().writeUnshared(update);
                } catch (IOException e) {
                    System.err.println("Couldn't send information to the clients");
                }
            }
        }
    }
}
