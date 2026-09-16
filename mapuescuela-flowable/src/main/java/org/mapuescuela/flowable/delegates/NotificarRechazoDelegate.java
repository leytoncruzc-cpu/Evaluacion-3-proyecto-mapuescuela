package org.mapuescuela.flowable.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mapuescuela.flowable.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service Task "Notificar rechazo y cancelar pedido", ejecutada cuando el
 * voluntario rechaza el comprobante de pago.
 */
@Component("notificarRechazoDelegate")
public class NotificarRechazoDelegate implements JavaDelegate {

    @Autowired
    private PedidoService pedidoService;

    @Override
    public void execute(DelegateExecution execution) {
        String pedidoId = (String) execution.getVariable("pedidoId");
        String motivoRechazo = (String) execution.getVariable("motivoRechazo");
        pedidoService.notificarRechazoYCancelar(pedidoId, motivoRechazo);
    }
}
