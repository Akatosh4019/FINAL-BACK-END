package pe.edu.upeu.serviceImpl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pe.edu.upeu.entity.Cliente;
import pe.edu.upeu.repository.ClienteRepository;
import pe.edu.upeu.services.ClienteService;
import pe.edu.upeu.errors.*;

import java.util.List;

@ApplicationScoped
public class ClienteServiceImpl implements ClienteService {

    private static final String ADMIN_CORREO = "admin@saga.local";

    @Inject
    ClienteRepository repository;

    @Override
    @Transactional
    public Cliente create(Cliente cliente) {

        if (repository.find("correo", cliente.getCorreo()).firstResult() != null) {
            throw new ConflictException("El correo ya está registrado");
        }

        repository.persist(cliente);
        return cliente;
    }

    @Override
    public List<Cliente> findAll() {
        return repository.listAll();
    }

    @Override
    public Cliente findById(Long id) {
        Cliente c = repository.findById(id);

        if (c == null) {
            throw new NotFoundException("Cliente no encontrado con id: " + id);
        }

        return c;
    }

    @Override
    public Cliente findByCorreo(String correo) {
        Cliente c = repository.findByCorreo(correo);

        if (c == null) {
            throw new NotFoundException("Cliente no encontrado con correo: " + correo);
        }

        return c;
    }

    @Override
    public Cliente validarClienteActivo(Long id) {
        Cliente c = findById(id);

        if (!"A".equals(c.getEstado())) {
            throw new BadRequestException("Cliente inactivo con id: " + id);
        }

        return c;
    }

    @Override
    @Transactional
    public Cliente update(Long id, Cliente cliente) {
        Cliente entity = repository.findById(id);

        if (entity == null) {
            throw new NotFoundException("Cliente no encontrado con id: " + id);
        }

        entity.setNombres(cliente.getNombres());
        entity.setApellidos(cliente.getApellidos());
        entity.setCorreo(cliente.getCorreo());
        entity.setTelefono(cliente.getTelefono());
        if (esClienteAdmin(entity) && !"A".equals(cliente.getEstado())) {
            throw new BadRequestException("El cliente administrador no se puede desactivar");
        }

        entity.setEstado(cliente.getEstado());

        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        desactivar(id);
    }
    @Override
    @Transactional
    public Cliente desactivar(Long id) {

        Cliente c = repository.findById(id);

        if (c == null) {
            throw new NotFoundException("Cliente no encontrado con id: " + id);
        }

        if (esClienteAdmin(c)) {
            throw new BadRequestException("El cliente administrador no se puede desactivar");
        }

        c.setEstado("I");

        return c;
    }

    @Override
    @Transactional
    public Cliente activar(Long id) {

        Cliente c = repository.findById(id);

        if (c == null) {
            throw new NotFoundException("Cliente no encontrado con id: " + id);
        }

        c.setEstado("A"); // 🔥 como es String

        return c;
    }

    private boolean esClienteAdmin(Cliente cliente) {
        return cliente != null && ADMIN_CORREO.equalsIgnoreCase(cliente.getCorreo());
    }
}
