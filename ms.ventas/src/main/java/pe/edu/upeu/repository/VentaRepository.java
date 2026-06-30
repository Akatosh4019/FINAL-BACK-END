package pe.edu.upeu.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import pe.edu.upeu.entity.Venta;

@ApplicationScoped
public class VentaRepository implements PanacheRepository<Venta> {

    public long countByProducto(Long idproducto) {
        return count("idproducto", idproducto);
    }
}
