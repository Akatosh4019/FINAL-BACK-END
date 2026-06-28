package pe.edu.upeu.serviceImpl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import pe.edu.upeu.entity.Producto;
import pe.edu.upeu.errors.BadRequestException;
import pe.edu.upeu.errors.ConflictException;
import pe.edu.upeu.errors.NotFoundException;
import pe.edu.upeu.repository.ProductoRepository;
import pe.edu.upeu.services.ProductoService;

import java.util.List;

@ApplicationScoped
public class ProductoServiceImpl implements ProductoService {

    private static final Logger LOG = Logger.getLogger(ProductoServiceImpl.class);

    @Inject
    ProductoRepository repository;

    @Override
    @Transactional
    public Producto create(Producto producto) {

        if (repository.find("nombre", producto.getNombre()).firstResult() != null) {
            throw new ConflictException("El producto ya existe");
        }

        repository.persist(producto);
        return producto;
    }

    @Override
    @Transactional
    public Producto update(Long id, Producto producto) {

        Producto entity = repository.findById(id);

        if (entity == null) {
            throw new NotFoundException("Producto no encontrado con id: " + id);
        }

        entity.setNombre(producto.getNombre());
        entity.setPrecio(producto.getPrecio());
        entity.setStock(producto.getStock());
        entity.setEstado(producto.getEstado());

        return entity;
    }

    @Override
    @Transactional
    public Producto delete(Long id) {
        return desactivar(id);
    }

    @Override
    public Producto findById(Long id) {

        Producto p = repository.findById(id);

        if (p == null) {
            throw new NotFoundException("Producto no encontrado con id: " + id);
        }

        return p;
    }

    @Override
    public List<Producto> findAll() {
        return repository.listAll();
    }

    @Override
    public Producto validarProductoDisponible(Long id, int cantidad) {
        Producto p = findById(id);

        if (!Character.valueOf('A').equals(p.getEstado())) {
            throw new BadRequestException("Producto inactivo con id: " + id);
        }

        if (cantidad <= 0) {
            throw new BadRequestException("La cantidad debe ser mayor a 0");
        }

        if (p.getStock() < cantidad) {
            throw new BadRequestException("No hay stock suficiente");
        }

        return p;
    }

    @Override
    @Transactional
    public Producto descontarStock(Long id, int cantidad) {
        Producto p = validarProductoDisponible(id, cantidad);

        p.setStock(p.getStock() - cantidad);
        LOG.infof("SAGA PRODUCTO | stock descontado | producto=%d cantidad=%d stockActual=%d", id, cantidad, p.getStock());

        return p;
    }

    @Override
    @Transactional
    public Producto restaurarStock(Long id, int cantidad) {
        Producto p = aumentarStock(id, cantidad);
        LOG.infof("SAGA PRODUCTO | stock restaurado | producto=%d cantidad=%d stockActual=%d", id, cantidad, p.getStock());
        return p;
    }

    @Override
    @Transactional
    public Producto desactivar(Long id) {

        Producto p = findById(id);

        p.setEstado('I');

        return p;
    }

    @Override
    @Transactional
    public Producto aumentarStock(Long id, int cantidad) {

        Producto p = findById(id);

        if (cantidad <= 0) {
            throw new BadRequestException("La cantidad debe ser mayor a 0");
        }

        p.setStock(p.getStock() + cantidad);

        return p;
    }

    @Override
    @Transactional
    public Producto activar(Long id) {

        Producto p = findById(id);

        p.setEstado('A');

        return p;
    }
}
