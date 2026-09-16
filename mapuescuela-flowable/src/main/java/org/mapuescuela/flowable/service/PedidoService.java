package org.mapuescuela.flowable.service;

/**
 * Contrato de integracion entre el proceso BPMN (Flowable) y la logica de negocio
 * real de la aplicacion (base de datos de pedidos, productos, inventario, etc.).
 *
 * El Integrante 2 (Backend Developer) debe crear una implementacion real de esta
 * interfaz (por ejemplo PedidoServiceJpaImpl) que reemplace a PedidoServiceEnMemoriaImpl
 * una vez que el modelo de datos y la base de datos esten listos.
 *
 * Los delegates de Flowable (paquete .delegates) solo llaman a estos metodos;
 * no conocen los detalles de persistencia.
 */
public interface PedidoService {

    /**
     * Marca el pedido con estado "Pendiente de pago".
     * Se invoca justo despues de crear la instancia del proceso.
     */
    void marcarPendienteDePago(String pedidoId);

    /**
     * Envia al cliente los datos bancarios de Mapuescuela para realizar la transferencia
     * (por ejemplo via email, o dejandolos disponibles en la app).
     */
    void informarDatosBancarios(String pedidoId, String clienteId);

    /**
     * Cancela el pedido por vencimiento del plazo de 24 horas y libera el stock
     * reservado (los productos vuelven a estar disponibles).
     */
    void cancelarPorVencimiento(String pedidoId);

    /**
     * Registra que el voluntario rechazo el comprobante y notifica al cliente.
     * El pedido queda cancelado.
     */
    void notificarRechazoYCancelar(String pedidoId, String motivoRechazo);

    /**
     * Descuenta del stock los productos del pedido, una vez aprobado el pago.
     */
    void actualizarInventario(String pedidoId);
}
