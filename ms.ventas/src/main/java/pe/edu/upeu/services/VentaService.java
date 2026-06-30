package pe.edu.upeu.services;

import pe.edu.upeu.entity.Venta;
import pe.edu.upeu.entity.SagaLog;
import pe.edu.upeu.dto.CarritoRequest;
import pe.edu.upeu.dto.SagaCarritoResponse;
import pe.edu.upeu.dto.SagaVentaResponse;
import java.util.List;

public interface VentaService {

    Venta create(Venta venta);

    SagaVentaResponse realizarVentaSaga(Venta venta);

    SagaVentaResponse realizarVentaSaga(Venta venta, boolean simularFalloDespuesDescuento);

    SagaCarritoResponse realizarVentaSagaCarrito(Long idcliente, CarritoRequest request);

    List<Venta> findAll();

    List<Venta> findByCliente(Long idcliente);

    long countByProducto(Long idproducto);

    List<SagaLog> findSagaLogs();

    Venta findById(Long id);

    Venta update(Long id, Venta venta);

    void delete(Long id);
}

