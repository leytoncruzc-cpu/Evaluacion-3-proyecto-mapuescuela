package org.mapuescuela.flowable.web;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.mapuescuela.flowable.service.PedidoServiceRestImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Endpoints para que el CLIENTE complete su parte del proceso: adjuntar el
 * comprobante de transferencia. Al completar esta tarea, Flowable cancela
 * automaticamente el boundary timer de 24 horas.
 */
@RestController
@RequestMapping("/api/procesos/venta/{pedidoId}/comprobante")
public class ClienteTareasController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private PedidoServiceRestImpl pedidoServiceRest;

    public record ComprobanteRequest(String urlComprobante) {}

    @PostMapping
    public Map<String, String> adjuntarComprobante(@PathVariable String pedidoId,
                                                     @RequestBody ComprobanteRequest request) {
        // La instancia de proceso se identifica con businessKey = pedidoId
        // (ver ProcesoVentaController#iniciarProceso).
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey(pedidoId)
                .taskDefinitionKey("task_adjuntar_comprobante")
                .singleResult();

        if (task == null) {
            throw new IllegalStateException(
                    "No hay una tarea pendiente de comprobante para el pedido " + pedidoId
                    + " (puede que ya haya vencido el plazo o ya fue adjuntado).");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("urlComprobante", request.urlComprobante());

        String url = request.urlComprobante();
        String nombreArchivo = url.substring(url.lastIndexOf('/') + 1);
        pedidoServiceRest.subirComprobante(pedidoId, nombreArchivo, url, null);

        taskService.complete(task.getId(), variables);

        Map<String, String> response = new HashMap<>();
        response.put("pedidoId", pedidoId);
        response.put("estado", "COMPROBANTE_RECIBIDO_PENDIENTE_REVISION");
        return response;
    }
}
