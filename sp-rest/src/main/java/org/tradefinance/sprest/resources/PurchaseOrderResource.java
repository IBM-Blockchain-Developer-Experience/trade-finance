package org.tradefinance.sprest.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.tradefinance.sprest.defs.VerifyPurchaseOrder;
import org.tradefinance.sprest.services.PurchaseOrderService;
import org.tradefinance.utils.ResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public class PurchaseOrderResource extends org.tradefinance.common.resources.PurchaseOrderResource {
    public static PurchaseOrderService service;

    public static void setService(PurchaseOrderService service) {
        org.tradefinance.common.resources.PurchaseOrderResource.setService(service);
        PurchaseOrderResource.service = service;
    }

    @POST
    @Path("{id}/verify")
    @Operation(summary = "Verify whether information pertaining to a given purchase order is correct")
    @ApiResponse(responseCode = "200", description = "Purchase order is verified", content =
        @Content(
            schema = @Schema(implementation = Boolean.class)
        )
    )
    public Response verifyPurchaseOrder(@PathParam("id") String id, @Parameter(required = true) VerifyPurchaseOrder body) {
        return ResponseBuilder.build(() -> service.verifyPurchaseOrder(id, body.contentOwnerGln, body.buyer, body.seller, body.price, body.units, body.productGtin));
    }
}
