package com.ecusol.web.client;

import com.ecusol.web.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;


@Component
public class CoreBancarioClient {

    private final WebClient webClient;

    public CoreBancarioClient(@Value("${ecusol.core.url}") String coreUrl) {
        this.webClient = WebClient.builder().baseUrl(coreUrl).build();
    }



    public List<CuentaCoreDTO> obtenerCuentasPorCliente(Integer clienteIdCore) {
        try {
            return webClient.get()
                    .uri("/cuentas/por-cliente/" + clienteIdCore)
                    .retrieve()
                    .bodyToFlux(CuentaCoreDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con el Core: " + e.getMessage());
        }
    }

    public List<MovimientoCoreDTO> obtenerMovimientos(String numeroCuenta) {
        return webClient.get()
                .uri("/cuentas/" + numeroCuenta + "/movimientos")
                .retrieve()
                .bodyToFlux(MovimientoCoreDTO.class)
                .collectList()
                .block();
    }

    public CuentaCoreDTO buscarCuenta(String numeroCuenta) {
        try {
            return webClient.get().uri("/cuentas/por-numero/" + numeroCuenta).retrieve().bodyToMono(CuentaCoreDTO.class).block();
        } catch (Exception e) { return null; }
    }

    public String realizarTransferencia(TransferenciaRequest dto) {
        return webClient.post()
                .uri("/transacciones/transferencia")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public Integer crearClientePersona(RegistroCoreRequest req) {
        return webClient.post()
                .uri("/clientes")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Integer.class)
                .block();
    }

    public String crearCuenta(CrearCuentaRequest req) {
        if (req.getClienteId() == null) {
            throw new IllegalArgumentException("crearCuenta requiere clienteId en el request");
        }
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                .path("/clientes/{clienteId}/cuentas")
                .build(req.getClienteId()))
                .bodyValue(req)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public List<SucursalDTO> obtenerSucursales() {
        try { return webClient.get().uri("/sucursales").retrieve().bodyToFlux(SucursalDTO.class).collectList().block(); }
        catch (Exception e) { return List.of(); }
    }


    public TitularCuentaDTO validarTitular(String numeroCuenta) {
        try { return webClient.get().uri("/cuentas/validar-titular/" + numeroCuenta).retrieve().bodyToMono(TitularCuentaDTO.class).block(); }
        catch (Exception e) { throw new RuntimeException("No se pudo validar el titular"); }
    }

    public boolean isClienteActivo(Integer clienteIdCore) {
        try {
            String estado = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/clientes/{id}/estado")
                            .build(clienteIdCore))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return "ACTIVO".equalsIgnoreCase(estado);
        } catch (Exception e) {
            return false;
        }
    }
}

