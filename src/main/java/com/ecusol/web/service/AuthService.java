package com.ecusol.web.service;

import com.ecusol.web.config.JwtTokenProvider;
import com.ecusol.web.dto.LoginRequest;
import com.ecusol.web.dto.RegisterRequest;
import com.ecusol.web.dto.RegistroCoreRequest;
import com.ecusol.web.model.UsuarioWeb;
import com.ecusol.web.repository.UsuarioWebRepository;
import com.ecusol.web.client.CoreBancarioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UsuarioWebRepository usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CoreBancarioClient coreBancarioClient;

    public String login(LoginRequest req) {
        UsuarioWeb user = usuarioRepo.findByUsername(req.getUsuario())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        if (!"ACTIVO".equalsIgnoreCase(user.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuario Web inactivo. Contacte al banco."
            );
        }

        try {
            Boolean clienteActivo = coreBancarioClient.isClienteActivo(user.getClienteIdCore());
            if (Boolean.FALSE.equals(clienteActivo)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Su cliente bancario está inactivo/bloqueado. Por favor acérquese a una agencia."
                );
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Error verificando estado bancario. Intente más tarde.",
                    e
            );
        }

        user.setUltimoAcceso(LocalDateTime.now());
        usuarioRepo.save(user);

        return tokenProvider.createToken(
                user.getUsername(),
                user.getUsuarioWebId(),
                user.getClienteIdCore()
        );
    }

    public void registrar(RegisterRequest req) {
        if (usuarioRepo.existsByUsername(req.usuario())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario ya existe"
            );
        }

        RegistroCoreRequest coreReq = RegistroCoreRequest.builder()
                .cedula(req.cedula())
                .nombres(req.nombres())
                .apellidos(req.apellidos())
                .direccion(req.direccion())
                .telefono(req.telefono())
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .build();

        Integer idCoreGenerado;
        try {
            idCoreGenerado = coreBancarioClient.crearClientePersona(coreReq);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Error creando cliente en el Core Bancario",
                    e
            );
        }

        UsuarioWeb u = new UsuarioWeb();
        u.setUsername(req.usuario());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setEmail(req.email());
        u.setClienteIdCore(idCoreGenerado);
        u.setEstado("ACTIVO");
        u.setFechaRegistro(LocalDateTime.now());

        usuarioRepo.save(u);
    }
}