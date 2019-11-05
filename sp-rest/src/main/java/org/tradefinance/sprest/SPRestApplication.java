package org.tradefinance.sprest;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Application;

import org.tradefinance.sprest.resources.FinanceRequestResource;
import org.tradefinance.sprest.resources.PurchaseOrderResource;

import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
  info = @Info(
    title = "SP-Rest",
    description = "Server for interacting with Trade Finance Demo as an financial SP",
    version = "1.0.0"
  )
)
public class SPRestApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return Stream.of(FinanceRequestResource.class, PurchaseOrderResource.class, OpenApiResource.class, AcceptHeaderOpenApiResource.class).collect(Collectors.toSet());
  }

  @Override
  public Set<Object> getSingletons() {
    return Collections.emptySet();
  }
}
