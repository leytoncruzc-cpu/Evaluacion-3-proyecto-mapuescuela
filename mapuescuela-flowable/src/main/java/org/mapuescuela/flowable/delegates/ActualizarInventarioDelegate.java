package org.mapuescuela.flowable.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mapuescuela.flowable.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service Task "Actualizar inventario (descontar stock)", ejecutada
 * inmediatamente despues de que el pago fue aprobado por el voluntario.
 */
@Component("actualizarInventarioDelegate")
public class ActualizarInventarioDelegate implements JavaDelegate {

    @Autowired
    private PedidoService pedidoService;

    @Override
    public void execute(DelegateExecution execution) {
        String pedidoId = (String) execution.getVariable("pedidoId");
        pedidoService.actualizarInventario(pedidoId);
    }
}
