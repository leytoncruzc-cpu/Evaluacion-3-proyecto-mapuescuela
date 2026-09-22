package org.mapuescuela.flowable.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mapuescuela.flowable.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Service Task "Informar datos bancarios al cliente" del proceso de venta.
 * Guarda los datos bancarios como variables del proceso, para que la app
 * del cliente los muestre mientras el pedido espera el pago.
 */
@Component("informarDatosBancariosDelegate")
public class InformarDatosBancariosDelegate implements JavaDelegate {

    @Autowired
    private PedidoService pedidoService;

    @Override
    public void execute(DelegateExecution execution) {
        String pedidoId = (String) execution.getVariable("pedidoId");
        String clienteId = (String) execution.getVariable("clienteId");

        Map<String, String> datosBancarios = pedidoService.informarDatosBancarios(pedidoId, clienteId);
        execution.setVariable("datosBancarios", datosBancarios);
    }
}