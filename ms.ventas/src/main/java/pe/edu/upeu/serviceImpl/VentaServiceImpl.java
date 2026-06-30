package pe.edu.upeu.serviceImpl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import pe.edu.upeu.client.ClienteClient;
import pe.edu.upeu.client.ProductoClient;
import pe.edu.upeu.dto.CarritoItemRequest;
import pe.edu.upeu.dto.CarritoRequest;
import pe.edu.upeu.dto.ClienteDTO;
import pe.edu.upeu.dto.ProductoDTO;
import pe.edu.upeu.dto.SagaCarritoResponse;
import pe.edu.upeu.dto.SagaVentaResponse;
import pe.edu.upeu.entity.SagaLog;
import pe.edu.upeu.entity.Venta;
import pe.edu.upeu.errors.BadRequestException;
import pe.edu.upeu.errors.ConflictException;
import pe.edu.upeu.errors.NotFoundException;
import pe.edu.upeu.repository.VentaRepository;
import pe.edu.upeu.services.SagaLogService;
import pe.edu.upeu.services.VentaService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class VentaServiceImpl implements VentaService {

    private static final Logger LOG = Logger.getLogger(VentaServiceImpl.class);

    @Inject
    VentaRepository repository;

    @Inject
    @RestClient
    ProductoClient productoClient;

    @Inject
    @RestClient
    ClienteClient clienteClient;

    @Inject
    SagaLogService sagaLogService;

    @CircuitBreaker(
            requestVolumeThreshold = 4,
            failureRatio = 0.5,
            delay = 5000
    )
    @Fallback(fallbackMethod = "fallbackProducto")
    @Timeout(3000)
    public ProductoDTO obtenerProducto(Long id) {
        return productoClient.buscarProductoPorId(id);
    }

    public ProductoDTO fallbackProducto(Long id) {
        ProductoDTO p = new ProductoDTO();

        p.nombre = "Servicio producto no disponible";
        p.stock = 0;
        p.precio = 0.0;
        p.estado = "I";

        return p;
    }

    @CircuitBreaker(
            requestVolumeThreshold = 4,
            failureRatio = 0.5,
            delay = 5000
    )
    @Fallback(fallbackMethod = "fallbackCliente")
    @Timeout(3000)
    public ClienteDTO obtenerCliente(Long id) {
        return clienteClient.buscarClientePorId(id);
    }

    public ClienteDTO fallbackCliente(Long id) {
        ClienteDTO c = new ClienteDTO();

        c.estado = "I";

        return c;
    }

    @Override
    @Transactional
    public Venta create(Venta venta) {
        return realizarVentaSaga(venta).venta;
    }

    @Override
    @Transactional
    public SagaVentaResponse realizarVentaSaga(Venta venta) {
        return realizarVentaSaga(venta, false);
    }

    @Override
    @Transactional
    public SagaVentaResponse realizarVentaSaga(Venta venta, boolean simularFalloDespuesDescuento) {
        String sagaId = UUID.randomUUID().toString();
        boolean stockDescontado = false;
        boolean stockCompensado = false;
        String pasoActual = "INICIO";

        LOG.infof("SAGA %s | INICIADA | cliente=%s producto=%s cantidad=%d",
                sagaId, venta.getIdcliente(), venta.getIdproducto(), venta.getCantidad());

        validarSolicitudVenta(venta);

        try {
            pasoActual = "VALIDAR_CLIENTE";
            LOG.infof("SAGA %s | PASO 1 | Validando cliente", sagaId);
            ClienteDTO cliente = clienteClient.validarClienteActivo(venta.getIdcliente());
            validarClienteRespuesta(cliente);
            LOG.infof("SAGA %s | PASO 1 OK | Cliente activo", sagaId);

            pasoActual = "VALIDAR_PRODUCTO_STOCK";
            LOG.infof("SAGA %s | PASO 2 | Validando producto y stock", sagaId);
            ProductoDTO producto = productoClient.validarProductoDisponible(venta.getIdproducto(), venta.getCantidad());
            validarProductoRespuesta(producto, venta.getCantidad());
            LOG.infof("SAGA %s | PASO 2 OK | Producto disponible stock=%d precio=%.2f",
                    sagaId, producto.stock, producto.precio);

            pasoActual = "DESCONTAR_STOCK";
            LOG.infof("SAGA %s | PASO 3 | Descontando stock", sagaId);
            productoClient.descontarStock(venta.getIdproducto(), venta.getCantidad());
            stockDescontado = true;
            LOG.infof("SAGA %s | PASO 3 OK | Stock descontado", sagaId);

            if (simularFalloDespuesDescuento) {
                pasoActual = "ERROR_SIMULADO_POST_DESCUENTO";
                throw new BadRequestException("Error simulado despues de descontar stock para probar compensacion Saga");
            }

            pasoActual = "REGISTRAR_VENTA";
            LOG.infof("SAGA %s | PASO 4 | Registrando venta", sagaId);
            venta.setSagaId(sagaId);
            venta.setTotal(producto.precio * venta.getCantidad());
            if (venta.getEstado() == null || venta.getEstado().isBlank()) {
                venta.setEstado("A");
            }
            repository.persistAndFlush(venta);
            LOG.infof("SAGA %s | COMPLETADA | venta=%d total=%.2f",
                    sagaId, venta.getIdventa(), venta.getTotal());

            sagaLogService.registrar(
                    sagaId,
                    venta.getIdcliente(),
                    "VENTA_SIMPLE",
                    "COMPLETADA",
                    null,
                    "Compra realizada correctamente.",
                    "Venta registrada correctamente",
                    false
            );

            return new SagaVentaResponse(sagaId, "COMPLETADA", "Venta registrada correctamente", venta);
        } catch (RuntimeException ex) {
            if (ex instanceof WebApplicationException) {
                LOG.warnf("SAGA %s | FALLIDA | %s", sagaId, mensajeSeguro(ex));
            } else {
                LOG.errorf(ex, "SAGA %s | FALLIDA | %s", sagaId, mensajeSeguro(ex));
            }

            if (stockDescontado) {
                stockCompensado = compensarStock(sagaId, venta);
            }

            String mensajeCliente = mensajeClienteError(pasoActual, mensajeSeguro(ex));
            sagaLogService.registrar(
                    sagaId,
                    venta.getIdcliente(),
                    "VENTA_SIMPLE",
                    "FALLIDA",
                    pasoActual,
                    mensajeCliente,
                    mensajeSeguro(ex),
                    stockCompensado
            );

            throw new ConflictException(mensajeCliente);
        }
    }

    @Override
    @Transactional
    public SagaCarritoResponse realizarVentaSagaCarrito(Long idcliente, CarritoRequest request) {
        String sagaId = UUID.randomUUID().toString();
        String pasoActual = "INICIO";
        Map<Long, Integer> stockDescontado = new LinkedHashMap<>();
        List<Venta> ventas = new ArrayList<>();
        double total = 0.0;

        LOG.infof("SAGA %s | CARRITO INICIADO | cliente=%s", sagaId, idcliente);
        validarSolicitudCarrito(idcliente, request);

        try {
            pasoActual = "VALIDAR_CLIENTE";
            LOG.infof("SAGA %s | CARRITO PASO 1 | Validando cliente", sagaId);
            ClienteDTO cliente = clienteClient.validarClienteActivo(idcliente);
            validarClienteRespuesta(cliente);

            pasoActual = "VALIDAR_PRODUCTOS_STOCK";
            LOG.infof("SAGA %s | CARRITO PASO 2 | Validando productos y stock", sagaId);
            Map<Long, ProductoDTO> productosValidados = new LinkedHashMap<>();
            for (CarritoItemRequest item : request.items) {
                ProductoDTO producto = productoClient.validarProductoDisponible(item.idproducto, item.cantidad);
                validarProductoRespuesta(producto, item.cantidad);
                productosValidados.put(item.idproducto, producto);
            }

            pasoActual = "DESCONTAR_STOCK";
            LOG.infof("SAGA %s | CARRITO PASO 3 | Descontando stock", sagaId);
            for (CarritoItemRequest item : request.items) {
                productoClient.descontarStock(item.idproducto, item.cantidad);
                stockDescontado.put(item.idproducto, item.cantidad);
                LOG.infof("SAGA %s | CARRITO STOCK DESCONTADO | producto=%d cantidad=%d",
                        sagaId, item.idproducto, item.cantidad);
            }

            pasoActual = "REGISTRAR_VENTAS";
            LOG.infof("SAGA %s | CARRITO PASO 4 | Registrando ventas", sagaId);
            for (CarritoItemRequest item : request.items) {
                ProductoDTO producto = productosValidados.get(item.idproducto);
                Venta venta = new Venta();
                venta.setSagaId(sagaId);
                venta.setIdcliente(idcliente);
                venta.setIdproducto(item.idproducto);
                venta.setCantidad(item.cantidad);
                venta.setTotal(producto.precio * item.cantidad);
                venta.setEstado("A");
                repository.persist(venta);
                ventas.add(venta);
                total += venta.getTotal();
            }
            repository.flush();

            sagaLogService.registrar(
                    sagaId,
                    idcliente,
                    "CARRITO",
                    "COMPLETADA",
                    null,
                    "Compra realizada correctamente.",
                    "Checkout de carrito completado con " + ventas.size() + " producto(s)",
                    false
            );

            LOG.infof("SAGA %s | CARRITO COMPLETADO | ventas=%d total=%.2f",
                    sagaId, ventas.size(), total);
            return new SagaCarritoResponse(sagaId, "COMPLETADA", "Compra realizada correctamente.", ventas, total);
        } catch (RuntimeException ex) {
            boolean stockCompensado = compensarStockCarrito(sagaId, stockDescontado);
            String detalle = mensajeSeguro(ex);

            String mensajeCliente = mensajeClienteError(pasoActual, detalle);
            sagaLogService.registrar(
                    sagaId,
                    idcliente,
                    "CARRITO",
                    "FALLIDA",
                    pasoActual,
                    mensajeCliente,
                    detalle,
                    stockCompensado
            );

            LOG.warnf("SAGA %s | CARRITO FALLIDO | paso=%s detalle=%s",
                    sagaId, pasoActual, detalle);
            throw new ConflictException(mensajeCliente);
        }
    }

    private void validarSolicitudCarrito(Long idcliente, CarritoRequest request) {
        if (idcliente == null) {
            throw new BadRequestException("El cliente autenticado es obligatorio");
        }

        if (request == null || request.items == null || request.items.isEmpty()) {
            throw new BadRequestException("El carrito debe tener al menos un producto");
        }

        for (CarritoItemRequest item : request.items) {
            if (item == null || item.idproducto == null) {
                throw new BadRequestException("Cada item debe tener producto");
            }
            if (item.cantidad <= 0) {
                throw new BadRequestException("La cantidad debe ser mayor a 0");
            }
        }
    }

    private void validarSolicitudVenta(Venta venta) {
        if (venta.getIdcliente() == null) {
            throw new BadRequestException("El cliente es obligatorio");
        }

        if (venta.getIdproducto() == null) {
            throw new BadRequestException("El producto es obligatorio");
        }

        if (venta.getCantidad() <= 0) {
            throw new BadRequestException("La cantidad debe ser mayor a 0");
        }
    }

    private void validarClienteRespuesta(ClienteDTO cliente) {
        if (cliente == null) {
            throw new NotFoundException("Cliente no existe");
        }

        if (!"A".equals(cliente.estado)) {
            throw new BadRequestException("Cliente inactivo o servicio no disponible");
        }
    }

    private void validarProductoRespuesta(ProductoDTO producto, int cantidad) {
        if (producto == null) {
            throw new NotFoundException("Producto no existe");
        }

        if (!"A".equals(producto.estado)) {
            throw new BadRequestException("Producto inactivo o servicio no disponible");
        }

        if (producto.stock < cantidad) {
            throw new BadRequestException("No hay suficiente stock");
        }
    }

    private boolean compensarStock(String sagaId, Venta venta) {
        try {
            LOG.warnf("SAGA %s | COMPENSACION | Restaurando stock producto=%d cantidad=%d",
                    sagaId, venta.getIdproducto(), venta.getCantidad());
            productoClient.restaurarStock(venta.getIdproducto(), venta.getCantidad());
            LOG.warnf("SAGA %s | COMPENSACION OK | Stock restaurado", sagaId);
            return true;
        } catch (RuntimeException compensacionEx) {
            LOG.errorf(compensacionEx,
                    "SAGA %s | COMPENSACION FALLIDA | revisar producto=%d cantidad=%d",
                    sagaId, venta.getIdproducto(), venta.getCantidad());
            return false;
        }
    }

    private boolean compensarStockCarrito(String sagaId, Map<Long, Integer> stockDescontado) {
        if (stockDescontado.isEmpty()) {
            return false;
        }

        boolean todoCompensado = true;
        for (Map.Entry<Long, Integer> item : stockDescontado.entrySet()) {
            try {
                LOG.warnf("SAGA %s | CARRITO COMPENSACION | Restaurando producto=%d cantidad=%d",
                        sagaId, item.getKey(), item.getValue());
                productoClient.restaurarStock(item.getKey(), item.getValue());
            } catch (RuntimeException ex) {
                todoCompensado = false;
                LOG.errorf(ex, "SAGA %s | CARRITO COMPENSACION FALLIDA | producto=%d cantidad=%d",
                        sagaId, item.getKey(), item.getValue());
            }
        }
        return todoCompensado;
    }

    private String mensajeClienteError(String pasoActual, String detalle) {
        if ("ERROR_SIMULADO_POST_DESCUENTO".equals(pasoActual)) {
            return "No se pudo completar tu compra. Intenta nuevamente.";
        }

        if ("VALIDAR_CLIENTE".equals(pasoActual)) {
            return "Tu cuenta de cliente esta inactiva. No puedes realizar compras.";
        }

        if ("VALIDAR_PRODUCTOS_STOCK".equals(pasoActual)) {
            return "No hay stock suficiente para completar tu compra.";
        }

        if (detalle != null && detalle.toLowerCase().contains("stock")) {
            return "No hay stock suficiente para completar tu compra.";
        }

        return "No se pudo completar tu compra. Intenta nuevamente.";
    }

    private String mensajeSeguro(RuntimeException ex) {
        if (ex.getMessage() == null || ex.getMessage().isBlank()) {
            return "error durante el proceso de venta";
        }

        return ex.getMessage();
    }

    @Override
    public List<Venta> findAll() {
        return repository.listAll();
    }

    @Override
    public List<Venta> findByCliente(Long idcliente) {
        if (idcliente == null) {
            throw new BadRequestException("El cliente autenticado es obligatorio");
        }

        return repository.list("idcliente", idcliente);
    }

    @Override
    public long countByProducto(Long idproducto) {
        if (idproducto == null) {
            throw new BadRequestException("El producto es obligatorio");
        }

        return repository.countByProducto(idproducto);
    }

    @Override
    public List<SagaLog> findSagaLogs() {
        return sagaLogService.findAll();
    }

    @Override
    public Venta findById(Long id) {

        Venta v = repository.findById(id);

        if (v == null) {
            throw new NotFoundException(
                    "Venta no encontrada con id: " + id
            );
        }

        return v;
    }

    @Override
    @Transactional
    public Venta update(Long id, Venta venta) {

        Venta entity = repository.findById(id);

        if (entity == null) {
            throw new NotFoundException(
                    "Venta no encontrada con id: " + id
            );
        }

        entity.setIdcliente(venta.getIdcliente());
        entity.setIdproducto(venta.getIdproducto());
        entity.setCantidad(venta.getCantidad());
        entity.setTotal(venta.getTotal());
        entity.setEstado(venta.getEstado());

        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {

        if (!repository.deleteById(id)) {

            throw new NotFoundException(
                    "Venta no encontrada con id: " + id
            );
        }
    }
}

