package pe.edu.upeu.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "ventas-api")
@Path("/ventas")
public interface VentaClient {

    @GET
    @Path("/producto/{idproducto}/conteo")
    long countByProducto(@PathParam("idproducto") Long idproducto);
}
