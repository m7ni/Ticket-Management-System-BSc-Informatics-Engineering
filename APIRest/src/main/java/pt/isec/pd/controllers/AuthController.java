package pt.isec.pd.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.model.User;
import pt.isec.pd.security.TokenService;
import pt.isec.pd.utils.DB;

import java.sql.SQLException;


@RestController
public class AuthController {
    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /*
    @PostMapping("/login") //associa a uri ao metodo
    public String login(Authentication authentication) {
        return tokenService.generateToken(authentication);
    }*/

    @PostMapping("/login")
    public ResponseEntity<Boolean> login(Authentication authentication)
    {
        DB db = null;
        try {
            db = new DB();
            tokenService.generateToken(authentication);     // Caso não exista token, este seja inválido ou tenha sido emitido há mais de 2 minutos,
                                                            // os pedidos HTTP devem devolver um código de resposta 401 (Unauthorized)
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }

    @PostMapping("/regist")
    public ResponseEntity<User> regist(@RequestBody User user)
    {
        DB db = null;
        try {
            db = new DB();
            if (!db.registUser(user.getUsername(), user.getPassword()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Boolean> delete(@PathVariable("id") Integer id)
    {
        DB db = null;
        try {
            db = new DB();
            if (!db.deleteUser(id))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }

}
