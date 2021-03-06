package stroom.config.global.impl;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import stroom.config.global.shared.ConfigProperty;
import stroom.util.shared.RestResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Api(value = "config - /v1")
@Path("/config/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface GlobalConfigResource extends RestResource {
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    List<ConfigProperty> getAllConfig();

    @GET
    @Path("/{propertyName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    ConfigProperty getPropertyByName(final @PathParam("propertyName") String propertyName);

    @GET
    @Path("/yamlValue/{propertyName}")
    @Produces(MediaType.TEXT_PLAIN)
    @Timed
    String getYamlValueByName(final @PathParam("propertyName") String propertyName);
}
