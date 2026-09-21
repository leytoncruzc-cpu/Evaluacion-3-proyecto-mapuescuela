package org.mapuescuela.flowable.service;

import org.mapuescuela.flowable.model.PedidoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Primary
@Service
public class PedidoServiceRestImpl implements PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceRestImpl.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tomas.api.url}")
    private String baseUrl;

    public PedidoDTO crearPedido(PedidoDTO datosPedido) {
        String url = baseUrl + "/pedidos";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PedidoDTO> request = new HttpEntity<>(datosPedido, headers);
        try {
            return restTemplate.postForObject(url, request, PedidoDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al crear pedido en API de Tomas: " + e.getMessage(), e);
        }
    }

    @Override
    public void marcarPendienteDePago(String pedidoId) {
        log.info("[Pedido {}] ya nace como PENDIENTE_PAGO en la API de Tomas", pedidoId);
    }

    @Override
    public void informarDatosBancarios(String pedidoId, String clienteId) {
        log.info("[Pedido {}] Datos bancarios informados al cliente {} (sin endpoint especifico en la API de Tomas)", pedidoId, clienteId);
    }

    @Override
    public void cancelarPorVencimiento(String pedidoId) {
        // No existe un endpoint dedicado a "cancelar por vencimiento" en la
        // API de Tomas. Workaround: usamos el endpoint de RECHAZO de pago,
        // con una observacion que deja claro el motivo real. Asi el pedido
        // no queda colgado, aunque en la API quede como PAGO_RECHAZADO y no
        // como un estado "CANCELADO_VENCIMIENTO" que no existe.
        String observacion = "Pedido cancelado automáticamente: venció el plazo de 24 horas para el pago.";
        llamarEndpointPago(pedidoId, "rechazar", observacion);
        log.info("[Pedido {}] Cancelado por vencimiento (via /pago/rechazar)", pedidoId);
    }

    @Override
    public void notificarRechazoYCancelar(String pedidoId, String motivoRechazo) {
        llamarEndpointPago(pedidoId, "rechazar", motivoRechazo);
        log.info("[Pedido {}] Rechazo notificado a la API real. Motivo: {}", pedidoId, motivoRechazo);
    }

    @Override
    public void actualizarInventario(String pedidoId) {
        // En la API real de Tomás, el stock se descuenta como efecto
        // secundario de aprobar el pago. Por eso este metodo llama
        // directamente a /pago/aprobar en vez de tener un endpoint propio.
        llamarEndpointPago(pedidoId, "aprobar", "Pago aprobado, inventario actualizado automáticamente.");
        log.info("[Pedido {}] Pago aprobado y stock actualizado en la API real.", pedidoId);
    }
    public void aprobarPago(String pedidoId, String observacion) {
        llamarEndpointPago(pedidoId, "aprobar", observacion == null ? "" : observacion);
        log.info("[Pedido {}] Pago aprobado en la API real. Stock actualizado automáticamente.", pedidoId);
    }

    private void llamarEndpointPago(String pedidoId, String accion, String observacion) {
        String url = baseUrl + "/pedidos/" + pedidoId + "/pago/" + accion;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("observacion", observacion);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForObject(url, request, Object.class);
        } catch (RestClientException e) {
            log.error("[Pedido {}] Error al llamar /pago/{} en la API de Tomás: {}", pedidoId, accion, e.getMessage());
            throw new RuntimeException("Error al " + accion + " el pago del pedido " + pedidoId + ": " + e.getMessage(), e);
        }
    }

    private void actualizarEstado(String pedidoId, String nuevoEstado) {
        PedidoDTO pedido = obtenerPedido(pedidoId);
        pedido.estado = nuevoEstado;
        actualizarPedido(pedidoId, pedido);
        log.info("[Pedido {}] estado -> {} (via API)", pedidoId, nuevoEstado);
    }

    private PedidoDTO obtenerPedido(String pedidoId) {
        String url = baseUrl + "/pedidos/" + pedidoId;
        try {
            return restTemplate.getForObject(url, PedidoDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al consultar pedido " + pedidoId + ": " + e.getMessage(), e);
        }
    }

    private void actualizarPedido(String pedidoId, PedidoDTO pedido) {
        String url = baseUrl + "/pedidos/" + pedidoId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PedidoDTO> request = new HttpEntity<>(pedido, headers);
        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, PedidoDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al actualizar pedido " + pedidoId + ": " + e.getMessage(), e);
        }
    }
}