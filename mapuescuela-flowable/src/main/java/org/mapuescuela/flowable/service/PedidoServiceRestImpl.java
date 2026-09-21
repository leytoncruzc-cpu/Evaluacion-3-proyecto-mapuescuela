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
        actualizarEstado(pedidoId, "CANCELADO_VENCIMIENTO");
    }

    @Override
    public void notificarRechazoYCancelar(String pedidoId, String motivoRechazo) {
        actualizarEstado(pedidoId, "CANCELADO_PAGO_RECHAZADO");
        log.info("[Pedido {}] Rechazo notificado. Motivo: {}", pedidoId, motivoRechazo);
    }

    @Override
    public void actualizarInventario(String pedidoId) {
        log.info("[Pedido {}] Inventario actualizado (pendiente de endpoint especifico en /api/productos)", pedidoId);
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