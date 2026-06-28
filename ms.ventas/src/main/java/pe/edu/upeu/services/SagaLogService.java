package pe.edu.upeu.services;

import pe.edu.upeu.entity.SagaLog;
import java.util.List;

public interface SagaLogService {

    SagaLog registrar(
            String sagaId,
            Long idcliente,
            String tipo,
            String estado,
            String pasoFallido,
            String mensajeCliente,
            String detalleTecnico,
            boolean stockCompensado
    );

    List<SagaLog> findAll();
}
