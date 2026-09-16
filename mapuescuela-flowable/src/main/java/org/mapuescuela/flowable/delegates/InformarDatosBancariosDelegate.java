package org.mapuescuela.flowable.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mapuescuela.flowable.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service Task "Informar datos bancarios al cliente" del proceso de venta.
 */
@Component("informarDatosBancariosDelegate")
public class InformarDatosBancariosDelegate implements JavaDelegate {

    @Autowired
    private PedidoService pedidoService;

    @Override
    public void execute(DelegateExecution execution) {
        String pedidoId = (String) execution.getVariable("pedidoId");
        String clienteId = (String) execution.getVariable("clienteId");
        pedidoService.informarDatosBancarios(pedidoId, clienteId);
    }
}
