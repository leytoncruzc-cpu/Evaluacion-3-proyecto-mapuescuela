package org.mapuescuela.flowable.service;

import org.mapuescuela.flowable.model.PedidoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Primary
@Service
public class PedidoServiceRestImpl implements PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceRestImpl.class);
    private static final String ESTADO_PENDIENTE_PAGO = "PENDIENTE_PAGO";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tomas.api.url}")
    private String baseUrl;

    // Datos bancarios de Mapuescuela (se configuran en application.yml / variables de entorno)
    @Value("${mapuescuela.banco.nombre}")
    private String bancoNombre;

    @Value("${mapuescuela.banco.tipo-cuenta}")
    private String bancoTipoCuenta;

    @Value("${mapuescuela.banco.numero-cuenta}")
    private String bancoNumeroCuenta;

    @Value("${mapuescuela.banco.titular}")
    private String bancoTitular;

    @Value("${mapuescuela.banco.rut}")
    private String bancoRut;

    @Value("${mapuescuela.banco.email}")
    private String bancoEmail;

    public PedidoDTO crearPedido(PedidoDTO datosPedido) {
        String url = baseUrl + "/pedidos";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PedidoDTO> request = new HttpEntity<>(datosPedido, headers);
        try {
            return restTemplate.postForObject(url, request, PedidoDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al crear pedido en la API: " + e.getMessage(), e);
        }
    }

    @Override
    public void marcarPendienteDePago(String pedidoId) {
        // El pedido ya nace como PENDIENTE_PAGO en la API. Aquí verificamos que
        // realmente exista y esté en ese estado antes de seguir con el proceso.
        PedidoDTO pedido = obtenerPedido(pedidoId);
        if (pedido == null) {
            throw new RuntimeException("El pedido " + pedidoId + " no existe en la API");
        }
        if (!ESTADO_PENDIENTE_PAGO.equals(pedido.estado)) {
            log.warn("[Pedido {}] se esperaba estado {} pero la API informa {}",
                    pedidoId, ESTADO_PENDIENTE_PAGO, pedido.estado);
        } else {
            log.info("[Pedido {}] verificado en la API: estado {}", pedidoId, pedido.estado);
        }
    }

    @Override
    public Map<String, String> informarDatosBancarios(String pedidoId, String clienteId) {
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("banco", bancoNombre);
        datos.put("tipoCuenta", bancoTipoCuenta);
        datos.put("numeroCuenta", bancoNumeroCuenta);
        datos.put("titular", bancoTitular);
        datos.put("rut", bancoRut);
        datos.put("email", bancoEmail);
        log.info("[Pedido {}] Datos bancarios disponibles para el cliente {}", pedidoId, clienteId);
        return datos;
    }

    @Override
    public void cancelarPorVencimiento(String pedidoId) {
        // La API no tiene un endpoint de "cancelar por vencimiento". Usamos el
        // endpoint de rechazo de pago con una observación que indica el motivo real.
        String observacion = "Pedido cancelado automáticamente: venció el plazo de 24 horas para el pago.";
        llamarEndpointPago(pedidoId, "rechazar", observacion);
        log.info("[Pedido {}] Cancelado por vencimiento (vía /pago/rechazar)", pedidoId);
    }

    @Override
    public void notificarRechazoYCancelar(String pedidoId, String motivoRechazo) {
        llamarEndpointPago(pedidoId, "rechazar", motivoRechazo);
        log.info("[Pedido {}] Pago rechazado en la API. Motivo: {}", pedidoId, motivoRechazo);
    }

    @Override
    public void actualizarInventario(String pedidoId) {
        // En la API, el stock se descuenta como efecto de aprobar el pago.
        llamarEndpointPago(pedidoId, "aprobar", "Pago aprobado, inventario actualizado automáticamente.");
        log.info("[Pedido {}] Pago aprobado y stock actualizado en la API.", pedidoId);
    }

    @Override
    public void subirComprobante(String pedidoId, String nombreArchivo, String rutaArchivo, String observacion) {
        String url = baseUrl + "/pedidos/" + pedidoId + "/comprobante";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("nombreArchivo", nombreArchivo);
        body.put("rutaArchivo", rutaArchivo);
        body.put("observacion", observacion == null ? "" : observacion);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForObject(url, request, Object.class);
            log.info("[Pedido {}] Comprobante registrado en la API: {}", pedidoId, rutaArchivo);
        } catch (RestClientException e) {
            log.error("[Pedido {}] Error al registrar comprobante en la API: {}", pedidoId, e.getMessage());
            throw new RuntimeException("Error al registrar comprobante del pedido " + pedidoId + ": " + e.getMessage(), e);
        }
    }

    private void llamarEndpointPago(String pedidoId, String accion, String observacion) {
        String url = baseUrl + "/pedidos/" + pedidoId + "/pago/" + accion;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("observacion", observacion == null ? "" : observacion);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForObject(url, request, Object.class);
        } catch (RestClientException e) {
            log.error("[Pedido {}] Error al llamar /pago/{} en la API: {}", pedidoId, accion, e.getMessage());
            throw new RuntimeException("Error al " + accion + " el pago del pedido " + pedidoId + ": " + e.getMessage(), e);
        }
    }

    private PedidoDTO obtenerPedido(String pedidoId) {
        String url = baseUrl + "/pedidos/" + pedidoId;
        try {
            return restTemplate.getForObject(url, PedidoDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al consultar pedido " + pedidoId + ": " + e.getMessage(), e);
        }
    }
}