package pe.edu.upeu.serviceImpl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pe.edu.upeu.entity.SagaLog;
import pe.edu.upeu.repository.SagaLogRepository;
import pe.edu.upeu.services.SagaLogService;

import java.util.List;

@ApplicationScoped
public class SagaLogServiceImpl implements SagaLogService {

    @Inject
    SagaLogRepository repository;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public SagaLog registrar(
            String sagaId,
            Long idcliente,
            String tipo,
            String estado,
            String pasoFallido,
            String mensajeCliente,
            String detalleTecnico,
            boolean stockCompensado
    ) {
        SagaLog log = new SagaLog();
        log.setSagaId(sagaId);
        log.setIdcliente(idcliente);
        log.setTipo(tipo);
        log.setEstado(estado);
        log.setPasoFallido(pasoFallido);
        log.setMensajeCliente(mensajeCliente);
        log.setDetalleTecnico(detalleTecnico);
        log.setStockCompensado(stockCompensado);
        repository.persist(log);
        return log;
    }

    @Override
    public List<SagaLog> findAll() {
        return repository.list("order by fecha desc");
    }
}
