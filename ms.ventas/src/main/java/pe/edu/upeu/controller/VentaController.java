package pe.edu.upeu.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pe.edu.upeu.dto.CarritoRequest;
import pe.edu.upeu.dto.SagaCarritoResponse;
import pe.edu.upeu.dto.SagaVentaResponse;
import pe.edu.upeu.entity.SagaLog;
import pe.edu.upeu.entity.Venta;
import pe.edu.upeu.services.VentaService;

import java.util.List;

@Path("/ventas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VentaController {

    @Inject
    VentaService service;

    @GET
    public List<Venta> list() {
        return service.findAll();
    }

    @GET
    @Path("/mis-ventas")
    public List<Venta> misVentas(@HeaderParam("X-Cliente-Id") Long idcliente) {
        return service.findByCliente(idcliente);
    }

    @GET
    @Path("/saga-logs")
    public List<SagaLog> sagaLogs() {
        return service.findSagaLogs();
    }

    @GET
    @Path("/{id}")
    public Venta get(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    public Venta create(Venta venta) {
        return service.create(venta);
    }

    @POST
    @Path("/saga")
    public SagaVentaResponse realizarVentaSaga(Venta venta) {
        return service.realizarVentaSaga(venta);
    }

    @POST
    @Path("/saga/cliente")
    public SagaVentaResponse realizarVentaSagaCliente(
            @HeaderParam("X-Cliente-Id") Long idcliente,
            Venta venta
    ) {
        venta.setIdcliente(idcliente);
        return service.realizarVentaSaga(venta);
    }

    @POST
    @Path("/saga/carrito/cliente")
    public SagaCarritoResponse realizarVentaSagaCarritoCliente(
            @HeaderParam("X-Cliente-Id") Long idcliente,
            CarritoRequest request
    ) {
        return service.realizarVentaSagaCarrito(idcliente, request);
    }

    @PUT
    @Path("/{id}")
    public Venta update(@PathParam("id") Long id, Venta venta) {
        return service.update(id, venta);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        service.delete(id);
    }
}
