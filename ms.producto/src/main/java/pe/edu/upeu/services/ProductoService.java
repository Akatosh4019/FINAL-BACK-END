package pe.edu.upeu.services;

import pe.edu.upeu.entity.Producto;
import java.util.List;

public interface ProductoService {

    Producto create(Producto producto);

    Producto update(Long id, Producto producto);

    Producto delete(Long id);

    Producto findById(Long id);

    List<Producto> findAll();

    Producto validarProductoDisponible(Long id, int cantidad);

    Producto descontarStock(Long id, int cantidad);

    Producto restaurarStock(Long id, int cantidad);

    Producto desactivar(Long id);

    Producto aumentarStock(Long id, int cantidad);

    Producto activar(Long id);
}
