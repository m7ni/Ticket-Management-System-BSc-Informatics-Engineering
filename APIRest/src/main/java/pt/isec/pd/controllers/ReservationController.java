package pt.isec.pd.controllers;


import data.cli2Serv.seriObjects.Reservation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd.utils.DB;

import java.sql.SQLException;
import java.util.List;

@RestController
public class ReservationController {
    @GetMapping("{filtros}")
    public ResponseEntity<List<Reservation>> getFilteredReservations(@PathVariable("filtros") String filter, @RequestParam("value") String idUser) {
        DB db = null;
        try {
            db = new DB();
            switch (filter.toLowerCase()) {
                case "paid" -> {
                    List<Reservation> list = db.consultPaidReservation(Integer.parseInt(idUser));
                    return ResponseEntity.ok().body(list);
                }
                case "notpaid" -> {
                    List<Reservation> list = db.consultNotPaidReservation(Integer.parseInt(idUser));
                    return ResponseEntity.ok().body(list);
                }
                default -> {
                    return ResponseEntity.badRequest().build();
                }
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
