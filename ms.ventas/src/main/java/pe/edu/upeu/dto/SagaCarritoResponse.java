package pe.edu.upeu.dto;

import pe.edu.upeu.entity.Venta;
import java.util.List;

public class SagaCarritoResponse {

    public String sagaId;
    public String estado;
    public String mensaje;
    public List<Venta> ventas;
    public double total;

    public SagaCarritoResponse() {
    }

    public SagaCarritoResponse(String sagaId, String estado, String mensaje, List<Venta> ventas, double total) {
        this.sagaId = sagaId;
        this.estado = estado;
        this.mensaje = mensaje;
        this.ventas = ventas;
        this.total = total;
    }
}
