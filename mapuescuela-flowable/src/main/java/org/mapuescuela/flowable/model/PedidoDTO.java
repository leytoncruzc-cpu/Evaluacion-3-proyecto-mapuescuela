package org.mapuescuela.flowable.model;

import java.util.List;

public class PedidoDTO {
    public Integer id;
    public String codigo;
    public String nombreCliente;
    public String emailCliente;
    public String telefonoCliente;
    public String direccionDespacho;
    public String modalidadEntrega;
    public String estado;
    public Double total;
    public String fechaCreacion;
    public List<DetalleDTO> detalles;

    public static class DetalleDTO {
        public Integer id;
        public Integer productoId;
        public String nombreProducto;
        public Integer cantidad;
    }
}