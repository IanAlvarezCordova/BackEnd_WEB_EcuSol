package com.ecusol.web.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class CrearCuentaRequest {
    private Integer clienteId;
    private Integer tipoCuentaId;
    private BigDecimal saldoInicial;
}