package org.tradefinance.porest;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Application;

import org.tradefinance.porest.resources.FinanceRequestResource;
import org.tradefinance.porest.resources.PurchaseOrderResource;
import org.tradefinance.porest.resources.ShipmentResource;

import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
  info = @Info(
    title = "PO-Rest",
    description = "Server for interacting with the Trade Finance Demo as an PO",
    version = "1.0.0"
  )
)
public class PORestApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return Stream.of(PurchaseOrderResource.class, FinanceRequestResource.class, ShipmentResource.class, OpenApiResource.class, AcceptHeaderOpenApiResource.class).collect(Collectors.toSet());
  }

  @Override
  public Set<Object> getSingletons() {
    return Collections.emptySet();
  }
}
