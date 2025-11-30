//ubi: src/main/java/com/ecusol/web/model/Beneficiario.java
package com.ecusol.web.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiario", schema = "ecusol_web")
@Data
public class Beneficiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "beneficiario_id") // SQL: beneficiario_id (snake_case)
    private Integer beneficiarioId;

    @ManyToOne(fetch = FetchType.LAZY) // Lazy para evitar logs gigantes
    @JoinColumn(name = "usuario_web_id", nullable = false) // SQL: usuario_web_id
    private UsuarioWeb usuarioWeb;

    @Column(name = "numero_cuenta_destino", nullable = false)
    private String numeroCuentaDestino;

    @Column(name = "nombre_titular", nullable = false)
    private String nombreTitular;

    @Column(name = "tipo_cuenta") // Agregado para integridad
    private String tipoCuenta;

    @Column(name = "alias")
    private String alias;

    @Column(name = "email_notificacion")
    private String emailNotificacion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
}