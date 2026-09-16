package org.mapuescuela.flowable.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementacion de prueba (in-memory) de PedidoService.
 *
 * Sirve para que el proceso BPMN se pueda ejecutar y demostrar de punta a punta
 * ANTES de que el backend real (base de datos, JPA, etc.) este listo.
 *
 * IMPORTANTE para el equipo: cuando el Integrante 2 tenga su implementacion real
 * (por ejemplo con JPA/Postgres), esta clase debe quedar deshabilitada o eliminada
 * -- Spring no puede tener dos beans @Service implementando la misma interfaz sin
 * marcar cual es el @Primary.
 */
@Service
@Primary
public class PedidoServiceEnMemoriaImpl implements PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceEnMemoriaImpl.class);

    // Solo para demo: guarda el estado de cada pedido en memoria.
    private final Map<String, String> estadosPedido = new ConcurrentHashMap<>();

    @Override
    public void marcarPendienteDePago(String pedidoId) {
        estadosPedido.put(pedidoId, "PENDIENTE_DE_PAGO");
        log.info("[Pedido {}] estado -> PENDIENTE_DE_PAGO", pedidoId);
    }

    @Override
    public void informarDatosBancarios(String pedidoId, String clienteId) {
        log.info("[Pedido {}] Se informaron los datos bancarios al cliente {}", pedidoId, clienteId);
    }

    @Override
    public void cancelarPorVencimiento(String pedidoId) {
        estadosPedido.put(pedidoId, "CANCELADO_VENCIMIENTO");
        log.info("[Pedido {}] estado -> CANCELADO_VENCIMIENTO. Stock liberado.", pedidoId);
    }

    @Override
    public void notificarRechazoYCancelar(String pedidoId, String motivoRechazo) {
        estadosPedido.put(pedidoId, "CANCELADO_PAGO_RECHAZADO");
        log.info("[Pedido {}] estado -> CANCELADO_PAGO_RECHAZADO. Motivo: {}", pedidoId, motivoRechazo);
    }

    @Override
    public void actualizarInventario(String pedidoId) {
        log.info("[Pedido {}] Inventario actualizado (stock descontado)", pedidoId);
    }

    public String consultarEstado(String pedidoId) {
        return estadosPedido.getOrDefault(pedidoId, "DESCONOCIDO");
    }
}
