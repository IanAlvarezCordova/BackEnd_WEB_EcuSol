//ubi: src/main/java/com/ecusol/web/model/UsuarioWeb.java
package com.ecusol.web.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_web", schema = "ecusol_web")
@Data
public class UsuarioWeb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_web_id")
    private Integer usuarioWebId;

    @Column(name = "cliente_id_core", nullable = false)
    private Integer clienteIdCore;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "email_contacto", nullable = false)
    private String email;

    @Column(nullable = false)
    private String estado;

    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
}