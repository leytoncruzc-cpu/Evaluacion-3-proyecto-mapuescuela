package org.mapuescuela.flowable.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mapuescuela.flowable.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service Task que se dispara cuando el boundary timer de 24 horas vence
 * sin que el cliente haya adjuntado el comprobante.
 */
@Component("cancelarPorVencimientoDelegate")
public class CancelarPorVencimientoDelegate implements JavaDelegate {

    @Autowired
    private PedidoService pedidoService;

    @Override
    public void execute(DelegateExecution execution) {
        String pedidoId = (String) execution.getVariable("pedidoId");
        pedidoService.cancelarPorVencimiento(pedidoId);
    }
}
