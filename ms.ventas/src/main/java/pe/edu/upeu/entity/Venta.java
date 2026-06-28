package pe.edu.upeu.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "venta")
public class Venta {
    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idventa;

    private Long idcliente;

    private Long idproducto;

    private String sagaId;

    private int cantidad;

    private double total;

    private String estado;

    private LocalDateTime fecha;

    public Venta() {
        this.fecha = LocalDateTime.now(ZONA_LIMA);
    }

    public Long getIdventa() { return idventa; }
    public void setIdventa(Long idventa) { this.idventa = idventa; }

    public Long getIdcliente() { return idcliente; }
    public void setIdcliente(Long idcliente) { this.idcliente = idcliente; }

    public Long getIdproducto() { return idproducto; }
    public void setIdproducto(Long idproducto) { this.idproducto = idproducto; }

    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
