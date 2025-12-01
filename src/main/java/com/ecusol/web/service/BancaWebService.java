package com.ecusol.web.service;

import com.ecusol.web.client.CoreBancarioClient;
import com.ecusol.web.dto.*;
import com.ecusol.web.model.Beneficiario;
import com.ecusol.web.model.UsuarioWeb;
import com.ecusol.web.repository.BeneficiarioRepository;
import com.ecusol.web.repository.UsuarioWebRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BancaWebService {

    @Autowired
    private CoreBancarioClient coreClient;

    @Autowired
    private BeneficiarioRepository beneficiarioRepo;

    @Autowired
    private UsuarioWebRepository usuarioWebRepo;

    public List<CuentaWebDTO> misCuentas(Integer clienteIdCore) {
        try {
            return coreClient.obtenerCuentasPorCliente(clienteIdCore).stream()
                    .map(c -> new CuentaWebDTO(
                            c.getCuentaId().longValue(),
                            c.getNumeroCuenta(),
                            c.getSaldo(),
                            c.getEstado(),
                            c.getTipoCuentaId().longValue()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudieron obtener las cuentas en este momento",
                    e
            );
        }
    }

    public List<MovimientoWebDTO> misMovimientos(String numeroCuenta) {
        try {
            var movsCore = coreClient.obtenerMovimientos(numeroCuenta);
            return movsCore.stream()
                    .map(m -> new MovimientoWebDTO(
                            m.getFechaEjecucion(),
                            m.getTipo(),
                            m.getMonto(),
                            BigDecimal.ZERO,
                            m.getDescripcion()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudieron obtener los movimientos en este momento",
                    e
            );
        }
    }

    public String transferir(TransferenciaRequest req) {
        try {
            return coreClient.realizarTransferencia(req);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo procesar la transferencia en el Core Bancario",
                    e
            );
        }
    }

    public TitularCuentaDTO validarDestinatarioCompleto(String numeroCuenta) {
        try {
            TitularCuentaDTO titular = coreClient.validarTitular(numeroCuenta);

            if (titular == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La cuenta destino no existe"
                );
            }

            return titular;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo validar la cuenta destino en este momento",
                    e
            );
        }
    }

    public void solicitarCuenta(Integer clienteIdCore, Integer tipoCuentaId) {
        CrearCuentaRequest req = CrearCuentaRequest.builder()
                .clienteId(clienteIdCore)
                .tipoCuentaId(tipoCuentaId)
                .saldoInicial(BigDecimal.ZERO)
                .build();

        try {
            coreClient.crearCuenta(req);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo crear la cuenta en el Core Bancario",
                    e
            );
        }
    }

    public List<SucursalDTO> obtenerSucursales() {
        try {
            return coreClient.obtenerSucursales();
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudieron obtener las sucursales en este momento",
                    e
            );
        }
    }

    public void guardarBeneficiario(Integer usuarioWebId, BeneficiarioDTO dto) {
        UsuarioWeb usuario = usuarioWebRepo.findById(usuarioWebId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario Web no encontrado"
                ));

        Beneficiario b = new Beneficiario();
        b.setUsuarioWeb(usuario);
        b.setNumeroCuentaDestino(dto.numeroCuenta());
        b.setNombreTitular(dto.nombreTitular());
        b.setAlias(dto.alias());
        b.setEmailNotificacion(dto.email());
        b.setTipoCuenta(dto.tipoCuenta() != null ? dto.tipoCuenta() : "Desconocido");
        b.setFechaRegistro(LocalDateTime.now());

        beneficiarioRepo.save(b);
    }

    public List<BeneficiarioDTO> misBeneficiarios(Integer usuarioWebId) {
        return beneficiarioRepo.findByUsuarioWeb_UsuarioWebId(usuarioWebId).stream()
                .map(b -> new BeneficiarioDTO(
                        b.getBeneficiarioId(),
                        b.getNumeroCuentaDestino(),
                        b.getNombreTitular(),
                        b.getAlias(),
                        b.getEmailNotificacion(),
                        b.getTipoCuenta()
                ))
                .collect(Collectors.toList());
    }
}