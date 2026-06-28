package pe.edu.upeu.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saga_log")
public class SagaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sagaId;

    private Long idcliente;

    private String tipo;

    private String estado;

    private String pasoFallido;

    @Column(length = 500)
    private String mensajeCliente;

    @Column(length = 1200)
    private String detalleTecnico;

    private boolean stockCompensado;

    private LocalDateTime fecha;

    public SagaLog() {
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }

    public Long getIdcliente() { return idcliente; }
    public void setIdcliente(Long idcliente) { this.idcliente = idcliente; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPasoFallido() { return pasoFallido; }
    public void setPasoFallido(String pasoFallido) { this.pasoFallido = pasoFallido; }

    public String getMensajeCliente() { return mensajeCliente; }
    public void setMensajeCliente(String mensajeCliente) { this.mensajeCliente = mensajeCliente; }

    public String getDetalleTecnico() { return detalleTecnico; }
    public void setDetalleTecnico(String detalleTecnico) { this.detalleTecnico = detalleTecnico; }

    public boolean isStockCompensado() { return stockCompensado; }
    public void setStockCompensado(boolean stockCompensado) { this.stockCompensado = stockCompensado; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
