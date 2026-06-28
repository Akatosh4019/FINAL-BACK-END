package pe.edu.upeu.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pe.edu.upeu.entity.Producto;
import pe.edu.upeu.repository.ProductoRepository;

@ApplicationScoped
public class DataInitializer {

    @Inject
    ProductoRepository productoRepository;

    @ConfigProperty(name = "producto.datos-iniciales.enabled", defaultValue = "true")
    boolean datosInicialesEnabled;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (!datosInicialesEnabled) {
            return;
        }

        crearProductoSiNoExiste("Papa", 3.50, 100);
        crearProductoSiNoExiste("Arroz", 4.20, 80);
        crearProductoSiNoExiste("Aceite", 9.90, 50);
    }

    private void crearProductoSiNoExiste(String nombre, Double precio, Integer stock) {
        Producto producto = productoRepository.find("nombre", nombre).firstResult();

        if (producto == null) {
            producto = new Producto();
            producto.setNombre(nombre);
            producto.setPrecio(precio);
            producto.setStock(stock);
            producto.setEstado('A');
            productoRepository.persist(producto);
            return;
        }

        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setEstado('A');
    }
}
