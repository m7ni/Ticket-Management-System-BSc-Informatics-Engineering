package pt.isec.pd.utils;

import data.cli2Serv.seriObjects.Reservation;
import data.cli2Serv.seriObjects.Seat;
import data.cli2Serv.seriObjects.Show;
import org.apache.catalina.User;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DB {
    private final String DATABASE_URL = "jdbc:mysql://localhost:8080/database1";
    private final String USERNAME = "root";
    private final String PASSWORD = "dbpd";
    ResultSet rs = null;
    Statement stmt = null;
    private Connection conn;

    public DB() throws SQLException {
        conn = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
        stmt = conn.createStatement();
    }

    public synchronized ArrayList<Reservation> consultNotPaidReservation(int idUser) {
        try {
            ArrayList<Reservation> reservationsNotPayed = new ArrayList<>();
            String query = "SELECT * FROM reserva WHERE pago = 0 AND id_utilizador = " + idUser;
            reservationsNotPayed = getReservas(query);
            return reservationsNotPayed;
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            return null;
        }
    }

    public synchronized ArrayList<Reservation> consultPaidReservation(int idUser) {
        try {
            ArrayList<Reservation> reservationsPayed = new ArrayList<>();
            String query = "SELECT * FROM reserva WHERE pago = 1 AND id_utilizador =" + idUser;
            reservationsPayed = getReservas(query);
            return reservationsPayed;
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            return null;
        }

    }

    private synchronized ArrayList<Reservation> getReservas(String query) throws SQLException {

        ArrayList<Reservation> reservationsNotPayed = new ArrayList<>();
        rs = stmt.executeQuery(query);

        while (rs.next())
            reservationsNotPayed.add(new Reservation(rs.getInt("id"), rs.getString("data_hora"), rs.getInt("pago"), rs.getInt("id_utilizador"), rs.getInt("id_espetaculo")));

        return reservationsNotPayed;
    }

    public synchronized void getShowsBetweenDates(String firstDateString, String secondDateString, List<Show> show) {
        try {
            String sqlQuery = "SELECT * FROM espetaculo WHERE espetaculo";
            ResultSet rs = stmt.executeQuery(sqlQuery);

            while (rs.next()) {
                String dateHour = rs.getString("data_hora");
                String temp = dateHour.substring(0, 10);
                String dateString = temp.replace(" ", "/");
                Date date;
                Date firstDate;
                Date secondDate;

                try {
                    date = (Date) new SimpleDateFormat("dd/MM/yyyy").parse(dateString);
                    firstDate = (Date) new SimpleDateFormat("dd/MM/yyyy").parse(firstDateString);
                    secondDate = (Date) new SimpleDateFormat("dd/MM/yyyy").parse(secondDateString);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                //if(firstDate.before(date) && secondDate.after(date))

                if(date.before(firstDate) || date.after(secondDate))
                    continue;

                ArrayList<Seat> seat = new ArrayList<>();
                int idShow = rs.getInt("id");
                Show aux = new Show(idShow, rs.getString("descricao"), rs.getString("tipo"),
                        rs.getString("data_hora"), rs.getInt("duracao"), rs.getString("local"), rs.getString("localidade")
                        , rs.getString("pais"), rs.getString("classificacao_etaria"), rs.getInt("visivel"));

                show.add(aux);

                String sqlSeats = "SELECT * FROM lugar WHERE espetaculo_id = " + idShow;
                Statement stmtSeats = conn.createStatement();
                ResultSet rs1 = stmtSeats.executeQuery(sqlSeats);

                while (rs1.next()) {
                    int idSeat = rs1.getInt("id");
                    String sqlAvailable = "SELECT * FROM reserva_lugar WHERE id_lugar = " + idSeat;
                    Statement stmtSeats1 = conn.createStatement();
                    ResultSet rs2 = stmtSeats1.executeQuery(sqlAvailable);
                    if (!rs2.next())
                        seat.add(new Seat(idSeat, rs1.getString("fila"), rs1.getString("assento"), rs1.getInt("preco"), true, idShow));
                }
                aux.setSeats(seat);
            }
        } catch (SQLException e) {
            System.err.println("Error updating Database");
        }
    }

    public synchronized void consultShows(List<Show> show) {
        try {
            String sqlQuery = "SELECT * FROM espetaculo";
            ResultSet rs = stmt.executeQuery(sqlQuery);

            while (rs.next()) {
                ArrayList<Seat> seat = new ArrayList<>();
                int idShow = rs.getInt("id");
                Show aux = new Show(idShow, rs.getString("descricao"), rs.getString("tipo"),
                        rs.getString("data_hora"), rs.getInt("duracao"), rs.getString("local"), rs.getString("localidade")
                        , rs.getString("pais"), rs.getString("classificacao_etaria"), rs.getInt("visivel"));

                show.add(aux);

                String sqlSeats = "SELECT * FROM lugar WHERE espetaculo_id = " + idShow;
                Statement stmtSeats = conn.createStatement();
                ResultSet rs1 = stmtSeats.executeQuery(sqlSeats);

                while (rs1.next()) {
                    int idSeat = rs1.getInt("id");
                    String sqlAvailable = "SELECT * FROM reserva_lugar WHERE id_lugar = " + idSeat;
                    Statement stmtSeats1 = conn.createStatement();
                    ResultSet rs2 = stmtSeats1.executeQuery(sqlAvailable);
                    if (!rs2.next())
                        seat.add(new Seat(idSeat, rs1.getString("fila"), rs1.getString("assento"), rs1.getInt("preco"), true, idShow));
                }
                aux.setSeats(seat);
            }
        } catch (SQLException e) {
            System.err.println("Error updating Database");
        }
    }

    public synchronized boolean registUser(String username, String password) {
        boolean success = false;
        int admin = 0;

        if (username.equals("admin") && password.equals("admin"))
            admin = 1;

        String sqlQueryRegist = "INSERT INTO utilizador (username, nome, password,administrador) VALUES (" + String.format("'%s', '%s', '%s','%d')", username, username, password, admin);

        try {
            stmt.executeUpdate(sqlQueryRegist);
            success = true;
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            success = false;
        }

        return success;
    }

    public synchronized boolean loginUser(String username, String password) throws SQLException {
        Statement statement = conn.createStatement();

        //if(username.equals("admin") && password.equals("admin"))


        String sqlQuery = "SELECT username,administrador,id FROM utilizador WHERE password like '" + password + "' AND username like '" + username + "'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);
        return resultSet.next();
    }

    public synchronized boolean deleteUser(Integer id) {
        boolean success = false;

        String sqlQueryDelete = "DELETE FROM utilizador WHERE id = " + id;
        try {
            stmt.executeUpdate(sqlQueryDelete);
            success = true;
        } catch (SQLException e) {
            System.err.println("Error updating Database");
            success = false;
        }

        return success;
    }

    public synchronized void getAllUsers(List<User> list) {
        try {
            String query = "SELECT * FROM utilizador";//acabar
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
            }
        } catch (SQLException e) {
            System.err.println("Error updating Database");
        }
    }

}
