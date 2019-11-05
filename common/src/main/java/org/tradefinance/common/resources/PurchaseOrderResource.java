package org.tradefinance.common.resources;

import javax.ws.rs.Path;

import org.tradefinance.common.services.PurchaseOrderService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Purchase Orders")
@Path("/purchaseorders")
public class PurchaseOrderResource {
    public static PurchaseOrderService service;

    public static void setService(PurchaseOrderService service) {
        PurchaseOrderResource.service = service;
    }
}
