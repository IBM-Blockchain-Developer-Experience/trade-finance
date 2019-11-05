package org.tradefinance.common.resources;

import javax.ws.rs.Path;

import org.tradefinance.common.services.ShipmentService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Shipments")
@Path("/shipments")
public class ShipmentResource {
    public static ShipmentService service;

    public static void setService(ShipmentService service) {
        ShipmentResource.service = service;
    }
}
