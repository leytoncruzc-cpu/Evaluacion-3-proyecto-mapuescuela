package org.mapuescuela.flowable.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mapuescuela.flowable.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service Task "Generar pedido (Pendiente de pago)" del proceso de venta.
 * Se ejecuta automaticamente al llegar el token a esta tarea.
 */
@Component("generarPedidoDelegate")
public class GenerarPedidoDelegate implements JavaDelegate {

    @Autowired
    private PedidoService pedidoService;

    @Override
    public void execute(DelegateExecution execution) {
        String pedidoId = (String) execution.getVariable("pedidoId");
        pedidoService.marcarPendienteDePago(pedidoId);
    }
}
