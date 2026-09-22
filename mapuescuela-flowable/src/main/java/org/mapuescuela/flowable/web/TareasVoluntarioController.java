package org.mapuescuela.flowable.web;

import org.flowable.engine.TaskService;
import org.flowable.engine.RuntimeService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Endpoints para el panel del VOLUNTARIO (lo consume el frontend del
 * Integrante 4). Cubre: listar tareas pendientes, revisar/aprobar/rechazar
 * el pago, preparar el pedido y registrar la entrega (retiro o despacho).
 */
@RestController
@RequestMapping("/api/voluntario")
public class TareasVoluntarioController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    /** Lista todas las tareas pendientes para el grupo "voluntarios". */
    @GetMapping("/tareas")
    public List<Map<String, Object>> listarTareasPendientes() {
        List<Task> tareas = taskService.createTaskQuery()
                .taskCandidateGroup("voluntarios")
                .list();

        return tareas.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("taskId", t.getId());
            m.put("nombre", t.getName());

            String businessKey = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult()
                    .getBusinessKey();
            m.put("pedidoId", businessKey);

            return m;
        }).toList();
    }

    public record RevisionPagoRequest(boolean pagoAprobado, String motivoRechazo) {}

    /** Aprueba o rechaza el comprobante de pago para un pedido. */
    @PostMapping("/pedidos/{pedidoId}/revisar-pago")
    public Map<String, String> revisarPago(@PathVariable String pedidoId,
                                            @RequestBody RevisionPagoRequest request) {
        Task task = buscarTareaPorPedido(pedidoId, "task_revisar_comprobante");

        Map<String, Object> variables = new HashMap<>();
        variables.put("pagoAprobado", request.pagoAprobado());
        variables.put("motivoRechazo", request.motivoRechazo());

        taskService.complete(task.getId(), variables);
        return respuesta(pedidoId, request.pagoAprobado() ? "PAGO_APROBADO" : "PAGO_RECHAZADO");
    }

    /** Marca el pedido como preparado. */
    @PostMapping("/pedidos/{pedidoId}/preparar")
    public Map<String, String> prepararPedido(@PathVariable String pedidoId) {
        Task task = buscarTareaPorPedido(pedidoId, "task_preparar_pedido");
        taskService.complete(task.getId());
        return respuesta(pedidoId, "EN_PREPARACION_COMPLETADA");
    }

    /** Marca el pedido como listo para retiro (modalidad retiro). */
    @PostMapping("/pedidos/{pedidoId}/listo-retiro")
    public Map<String, String> marcarListoParaRetiro(@PathVariable String pedidoId) {
        Task task = buscarTareaPorPedido(pedidoId, "task_listo_retiro");
        taskService.complete(task.getId());
        return respuesta(pedidoId, "LISTO_PARA_RETIRO");
    }

    /** Registra que el cliente retiro su pedido. */
    @PostMapping("/pedidos/{pedidoId}/confirmar-retiro")
    public Map<String, String> confirmarRetiro(@PathVariable String pedidoId) {
        Task task = buscarTareaPorPedido(pedidoId, "task_registrar_retiro");
        taskService.complete(task.getId());
        return respuesta(pedidoId, "FINALIZADO");
    }

    public record DespachoRequest(String empresaTransporte, String numeroSeguimiento, String fechaEnvio) {}

    /** Registra los datos del despacho (modalidad despacho). */
    @PostMapping("/pedidos/{pedidoId}/registrar-despacho")
    public Map<String, String> registrarDespacho(@PathVariable String pedidoId,
                                                  @RequestBody DespachoRequest request) {
        Task task = buscarTareaPorPedido(pedidoId, "task_registrar_despacho");

        Map<String, Object> variables = new HashMap<>();
        variables.put("empresaTransporte", request.empresaTransporte());
        variables.put("numeroSeguimiento", request.numeroSeguimiento());
        variables.put("fechaEnvio", request.fechaEnvio());

        taskService.complete(task.getId(), variables);
        return respuesta(pedidoId, "DESPACHO_REGISTRADO");
    }

    /** Confirma que el envio fue entregado. */
    @PostMapping("/pedidos/{pedidoId}/confirmar-entrega")
    public Map<String, String> confirmarEntrega(@PathVariable String pedidoId) {
        Task task = buscarTareaPorPedido(pedidoId, "task_confirmar_entrega");
        taskService.complete(task.getId());
        return respuesta(pedidoId, "FINALIZADO");
    }

    private Task buscarTareaPorPedido(String pedidoId, String taskDefinitionKey) {
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey(pedidoId)
                .taskDefinitionKey(taskDefinitionKey)
                .singleResult();
        if (task == null) {
            throw new IllegalStateException(
                    "No hay una tarea '" + taskDefinitionKey + "' pendiente para el pedido " + pedidoId);
        }
        return task;
    }

    private Map<String, String> respuesta(String pedidoId, String estado) {
        Map<String, String> r = new HashMap<>();
        r.put("pedidoId", pedidoId);
        r.put("estado", estado);
        return r;
    }
}
