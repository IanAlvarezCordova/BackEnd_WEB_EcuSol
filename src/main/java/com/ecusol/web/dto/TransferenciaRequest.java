//ubi: src/main/java/com/ecusol/web/dto/TransferenciaRequest.java
package com.ecusol.web.dto;
import java.math.BigDecimal;
public record TransferenciaRequest(
    String cuentaOrigen, String cuentaDestino, BigDecimal monto, String descripcion
) {}