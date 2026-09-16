package org.mapuescuela.flowable.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PedidoServiceRestImpl implements PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceRestImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tomas.api.url}")
    private String baseUrl;

    @Override
    public void marcarPendienteDePago(String pedidoId) {
        putEstado(pedidoId, "PENDIENTE_DE_PAGO");
    }

    @Override
    public void informarDatosBancarios(String pedidoId, String clienteId) {
        String url = baseUrl + "/pedidos/" + pedidoId + "/datos-bancarios";
        Map<String, Object> body = Map.of("clienteId", clienteId);
        post(url, body);
        log.info("[Pedido {}] Datos bancarios informados al cliente {} via API", pedidoId, clienteId);
    }

    @Override
    public void cancelarPorVencimiento(String pedidoId) {
        putEstado(pedidoId, "CANCELADO_VENCIMIENTO");
    }

    @Override
    public void notificarRechazoYCancelar(String pedidoId, String motivoRechazo) {
        String url = baseUrl + "/pedidos/" + pedidoId + "/estado";
        Map<String, Object> body = Map.of("estado", "CANCELADO_PAGO_RECHAZADO", "motivo", motivoRechazo);
        put(url, body);
        log.info("[Pedido {}] Rechazo notificado. Motivo: {}", pedidoId, motivoRechazo);
    }

    @Override
    public void actualizarInventario(String pedidoId) {
        String url = baseUrl + "/pedidos/" + pedidoId + "/actualizar-inventario";
        post(url, Map.of());
        log.info("[Pedido {}] Inventario actualizado via API", pedidoId);
    }

    // ---- helpers ----

    private void putEstado(String pedidoId, String estado) {
        String url = baseUrl + "/pedidos/" + pedidoId + "/estado";
        Map<String, Object> body = Map.of("estado", estado);
        put(url, body);
        log.info("[Pedido {}] estado -> {} (via API)", pedidoId, estado);
    }

    private void post(String url, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al llamar a " + url + ": " + e.getMessage(), e);
        }
    }

    private void put(String url, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al llamar a " + url + ": " + e.getMessage(), e);
        }
    }
}