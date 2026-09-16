package org.mapuescuela.flowable.web;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Punto de entrada para que la aplicacion (backend del Integrante 2, o el
 * frontend directamente en una demo) inicie una instancia del proceso de venta
 * cuando el cliente confirma su compra.
 */
@RestController
@RequestMapping("/api/procesos/venta")
public class ProcesoVentaController {

    @Autowired
    private RuntimeService runtimeService;

    public record IniciarPedidoRequest(String pedidoId, String clienteId, String modalidadEntrega) {}

    /**
     * Arranca el proceso BPMN "proceso_venta" para un pedido recien creado.
     *
     * modalidadEntrega debe ser "retiro" o "despacho", tal como llega del
     * formulario de checkout del cliente.
     */
    @PostMapping("/iniciar")
    public Map<String, String> iniciarProceso(@RequestBody IniciarPedidoRequest request) {
        String pedidoId = (request.pedidoId() != null) ? request.pedidoId() : UUID.randomUUID().toString();

        Map<String, Object> variables = new HashMap<>();
        variables.put("pedidoId", pedidoId);
        variables.put("clienteId", request.clienteId());
        variables.put("modalidadEntrega", request.modalidadEntrega());

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("proceso_venta", pedidoId, variables);

        Map<String, String> response = new HashMap<>();
        response.put("procesoInstanciaId", instance.getId());
        response.put("pedidoId", pedidoId);
        return response;
    }
}
