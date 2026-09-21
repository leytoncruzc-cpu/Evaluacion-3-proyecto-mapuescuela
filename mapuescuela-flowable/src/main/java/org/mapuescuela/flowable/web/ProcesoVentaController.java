package org.mapuescuela.flowable.web;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.mapuescuela.flowable.model.PedidoDTO;
import org.mapuescuela.flowable.service.PedidoServiceRestImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/procesos/venta")
public class ProcesoVentaController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private PedidoServiceRestImpl pedidoServiceRest;

    public record IniciarPedidoRequest(
            String nombreCliente,
            String emailCliente,
            String telefonoCliente,
            String direccionDespacho,
            String modalidadEntrega,
            List<PedidoDTO.DetalleDTO> detalles
    ) {}

    @PostMapping("/iniciar")
    public Map<String, String> iniciarProceso(@RequestBody IniciarPedidoRequest request) {
        PedidoDTO datosPedido = new PedidoDTO();
        datosPedido.nombreCliente = request.nombreCliente();
        datosPedido.emailCliente = request.emailCliente();
        datosPedido.telefonoCliente = request.telefonoCliente();
        datosPedido.direccionDespacho = request.direccionDespacho();
        datosPedido.modalidadEntrega = request.modalidadEntrega();
        datosPedido.detalles = request.detalles();

        PedidoDTO pedidoCreado = pedidoServiceRest.crearPedido(datosPedido);
        String pedidoId = String.valueOf(pedidoCreado.id);

        Map<String, Object> variables = new HashMap<>();
        variables.put("pedidoId", pedidoId);
        variables.put("clienteId", request.emailCliente());
        variables.put("modalidadEntrega", request.modalidadEntrega());

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("proceso_venta", pedidoId, variables);

        Map<String, String> response = new HashMap<>();
        response.put("procesoInstanciaId", instance.getId());
        response.put("pedidoId", pedidoId);
        response.put("codigoPedido", pedidoCreado.codigo);
        return response;
    }
}