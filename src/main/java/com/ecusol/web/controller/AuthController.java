package com.ecusol.web.controller;

import com.ecusol.web.dto.JwtResponse;
import com.ecusol.web.dto.LoginRequest;
import com.ecusol.web.dto.RegisterRequest;
import com.ecusol.web.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired 
    private AuthService authService;

    @PostMapping("/auth/tokens")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest req) {
        try {
            String token = authService.login(req);
            JwtResponse response = new JwtResponse(token, req.getUsuario());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", ex);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<String> register(@RequestBody RegisterRequest req) {
        try {
            authService.registrar(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Usuario registrado correctamente");
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo registrar el usuario", ex);
        }
    }
}