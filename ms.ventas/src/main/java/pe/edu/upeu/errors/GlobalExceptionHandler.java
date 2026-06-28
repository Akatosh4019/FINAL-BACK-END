package pe.edu.upeu.errors;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {
    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Override
    public Response toResponse(Throwable exception) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now(ZONA_LIMA));
        error.put("message", exception.getMessage());

        if (exception instanceof ApiException apiEx) {
            error.put("status", apiEx.getStatus());
            return Response.status(apiEx.getStatus()).entity(error).build();
        }

        error.put("status", 500);
        return Response.status(500).entity(error).build();
    }
}
