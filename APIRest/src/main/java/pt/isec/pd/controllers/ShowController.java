package pt.isec.pd.controllers;

import data.cli2Serv.seriObjects.Show;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd.utils.DB;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ShowController {


    @GetMapping("{filter}")
    public ResponseEntity<List<Show>> getFilteredShows(@PathVariable("filter") String filter,
                                                       @RequestParam("value") String value) {
        DB db = null;
        try {
            db = new DB();
            switch (filter.toLowerCase()) {
                case "date" -> {

                    String firstDate = value.substring(0, value.indexOf(" ") - 1);
                    String secondDate = value.substring(value.indexOf(" ") + 1, value.length());
                    List<Show> list = new ArrayList<>();
                    db.getShowsBetweenDates(firstDate, secondDate, list);
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

    @GetMapping()
    public ResponseEntity<List<Show>> getAllShows() {
        DB db = null;
        try {
            db = new DB();
            List<Show> list = new ArrayList<>();
            db.consultShows(list);
            return ResponseEntity.ok().body(list);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

}
