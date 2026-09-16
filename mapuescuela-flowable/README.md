# Mapuescuela - Orquestacion del Proceso de Venta (Flowable)

Este modulo contiene el proceso BPMN de venta modelado en `src/main/resources/processes/proceso_venta_mapuescuela.bpmn20.xml`,
desplegado y ejecutado automaticamente por Flowable dentro de una app Spring Boot.

## Como levantarlo

```bash
mvn spring-boot:run
```

Queda disponible en `http://localhost:8080`.

- Consola H2 (para inspeccionar tablas de Flowable en desarrollo): `http://localhost:8080/h2-console`
  (JDBC URL: `jdbc:h2:mem:mapuescuela`, usuario `sa`, sin password)

## Como funciona (resumen para el equipo)

1. Cuando el cliente confirma su compra, el **backend (Integrante 2)** llama a
   `POST /api/procesos/venta/iniciar` con `pedidoId`, `clienteId` y `modalidadEntrega`
   ("retiro" o "despacho"). Esto arranca la instancia del proceso.
2. El proceso automaticamente:
   - marca el pedido "Pendiente de pago" (delegate `GenerarPedidoDelegate`)
   - informa los datos bancarios (delegate `InformarDatosBancariosDelegate`)
   - queda **esperando el comprobante**, con un timer de 24 horas corriendo en paralelo.
3. El **frontend del cliente (Integrante 3)** llama a
   `POST /api/procesos/venta/{pedidoId}/comprobante` cuando el cliente sube el comprobante.
   Esto cancela automaticamente el timer de 24h.
   - Si el cliente NO llama a este endpoint dentro de las 24h, Flowable cancela el
     pedido solo (delegate `CancelarPorVencimientoDelegate`) -- no requiere ninguna
     accion del backend.
4. El **panel del voluntario (Integrante 4)** consume los endpoints de
   `TareasVoluntarioController`:
   - `GET /api/voluntario/tareas` -> lista de tareas pendientes
   - `POST /api/voluntario/pedidos/{pedidoId}/revisar-pago` -> aprobar/rechazar
   - `POST /api/voluntario/pedidos/{pedidoId}/preparar`
   - `POST /api/voluntario/pedidos/{pedidoId}/listo-retiro` (si modalidad = retiro)
   - `POST /api/voluntario/pedidos/{pedidoId}/confirmar-retiro`
   - `POST /api/voluntario/pedidos/{pedidoId}/registrar-despacho` (si modalidad = despacho)
   - `POST /api/voluntario/pedidos/{pedidoId}/confirmar-entrega`

## Punto de integracion clave para el Integrante 2 (Backend)

Toda la logica de negocio real (base de datos de pedidos, descuento de stock, etc.)
debe implementarse en una nueva clase que implemente `PedidoService`
(ver `service/PedidoService.java`), por ejemplo `PedidoServiceJpaImpl`.

Mientras esa clase no exista, el proyecto usa `PedidoServiceEnMemoriaImpl` (solo
logs en consola) para que el proceso se pueda demostrar de punta a punta.

**Importante:** cuando el Integrante 2 cree su implementacion real, hay que:
1. Eliminar o deshabilitar `PedidoServiceEnMemoriaImpl` (Spring no puede tener
   dos beans para la misma interfaz sin desambiguar con `@Primary`).
2. NO modificar la interfaz `PedidoService` sin avisar al resto -- los delegates
   dependen de ella.

## Probar el flujo manualmente (con curl)

```bash
# 1. Iniciar proceso
curl -X POST http://localhost:8080/api/procesos/venta/iniciar \
  -H "Content-Type: application/json" \
  -d '{"pedidoId":"pedido-001","clienteId":"cliente-1","modalidadEntrega":"retiro"}'

# 2. Cliente adjunta comprobante
curl -X POST http://localhost:8080/api/procesos/venta/pedido-001/comprobante \
  -H "Content-Type: application/json" \
  -d '{"urlComprobante":"https://ejemplo.com/comprobante.jpg"}'

# 3. Voluntario ve tareas pendientes
curl http://localhost:8080/api/voluntario/tareas

# 4. Voluntario aprueba el pago
curl -X POST http://localhost:8080/api/voluntario/pedidos/pedido-001/revisar-pago \
  -H "Content-Type: application/json" \
  -d '{"pagoAprobado": true}'

# 5. Voluntario prepara el pedido
curl -X POST http://localhost:8080/api/voluntario/pedidos/pedido-001/preparar

# 6. Voluntario marca listo para retiro
curl -X POST http://localhost:8080/api/voluntario/pedidos/pedido-001/listo-retiro

# 7. Voluntario confirma el retiro -> proceso finalizado
curl -X POST http://localhost:8080/api/voluntario/pedidos/pedido-001/confirmar-retiro
```

## Para probar el escenario de vencimiento (24h) sin esperar 24 horas reales

Se puede editar temporalmente `<timeDuration>PT24H</timeDuration>` en el archivo
`.bpmn20.xml` a algo como `PT1M` (1 minuto) mientras se hacen pruebas locales,
y volver a dejarlo en `PT24H` antes de la entrega/demo final.

## Pendientes sugeridos (para seguir avanzando)

- [ ] Reemplazar `PedidoServiceEnMemoriaImpl` por la implementacion real con base de datos.
- [ ] Agregar autenticacion/roles reales (hoy cualquiera puede llamar a los endpoints de voluntario).
- [ ] Exponer el estado del pedido consultando variables/historial de Flowable
      (HistoryService) para que el cliente pueda "consultar el estado de su compra".
- [ ] Agregar manejo de errores HTTP mas prolijo (hoy las excepciones devuelven 500 generico).
