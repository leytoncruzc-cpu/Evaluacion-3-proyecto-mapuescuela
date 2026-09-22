package org.mapuescuela.flowable.service;

import java.util.Map;

/**
 * Contrato de integración entre el proceso BPMN (Flowable) y la lógica de negocio
 * de Mapuescuela. La implementación real es PedidoServiceRestImpl, que se comunica
 * con la API de pedidos (backend).
 *
 * Los delegates de Flowable (paquete .delegates) solo llaman a estos métodos;
 * no conocen los detalles de la API.
 */
public interface PedidoService {

    /**
     * Verifica que el pedido exista en la API y esté en estado "Pendiente de pago".
     * Se invoca justo después de crear la instancia del proceso.
     */
    void marcarPendienteDePago(String pedidoId);

    /**
     * Devuelve los datos bancarios de Mapuescuela para que el cliente haga la
     * transferencia. El delegate los guarda como variables del proceso para que
     * la app del cliente los pueda mostrar.
     */
    Map<String, String> informarDatosBancarios(String pedidoId, String clienteId);

    /**
     * Cancela el pedido por vencimiento del plazo de 24 horas para el pago.
     */
    void cancelarPorVencimiento(String pedidoId);

    /**
     * Registra que el voluntario rechazó el comprobante. El pedido queda cancelado.
     */
    void notificarRechazoYCancelar(String pedidoId, String motivoRechazo);

    /**
     * Aprueba el pago y descuenta del stock los productos del pedido.
     */
    void actualizarInventario(String pedidoId);

    /**
     * Registra en la API el comprobante de pago adjuntado por el cliente.
     */
    void subirComprobante(String pedidoId, String nombreArchivo, String rutaArchivo, String observacion);
}