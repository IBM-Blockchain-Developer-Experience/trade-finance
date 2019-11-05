package org.tradefinance.porest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.tradefinance.assets.PurchaseOrder;
import org.tradefinance.assets.PurchaseOrderResponse;
import org.tradefinance.porest.defs.CreatePurchaseOrder;
import org.tradefinance.porest.services.PurchaseOrderService;
import org.tradefinance.utils.ResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public class PurchaseOrderResource extends org.tradefinance.common.resources.PurchaseOrderResource {
    public static PurchaseOrderService service;

    public static void setService(PurchaseOrderService service) {
        org.tradefinance.common.resources.PurchaseOrderResource.setService(service);
        PurchaseOrderResource.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get purchase orders for user without blockchain identity")
    @ApiResponse(responseCode = "200", description = "Purchase orders where supplied user is buyer or seller", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            array = @ArraySchema(
                schema = @Schema(implementation = PurchaseOrder.class)
            )
        )
    )
    public Response getPurchaseOrders(@QueryParam("behalfOf") Long behalfOf) {
        if (behalfOf == null) {
            return ResponseBuilder.build(Status.BAD_REQUEST, "behalfOf is a required parameter");
        }

        return ResponseBuilder.build(() -> service.getPurchaseOrders(behalfOf));
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get purchase order for given ID")
    @ApiResponse(responseCode = "200", description = "Purchase order for supplied id", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = PurchaseOrder.class)
        )
    )
    public Response getPurchaseOrder(@PathParam("id") String id) {
        Response response = ResponseBuilder.build(() -> { return service.getPurchaseOrder(id); });

        if (response.getEntity() == null) {
            response = ResponseBuilder.build(Status.BAD_REQUEST, "Purchase order not found with ID: " + id);
        }

        return response;
    }

    @GET
    @Path("hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get purchase order with specified hash")
    @ApiResponse(responseCode = "200", description = "Purchase order with supplied hash", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = PurchaseOrder.class)
        )
    )
    public Response getPurchaseOrderByHash(@PathParam("hash") String hash) {
        Response response = ResponseBuilder.build(() -> service.getPurchaseOrderByHash(hash));

        if (response.getEntity() == null) {
            response = ResponseBuilder.build(Status.BAD_REQUEST, "Purchase order not found with hash: " + hash);
        }

        return response;
    }

    @GET
    @Path("responses")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get purchase order responses for user without blockchain identity")
    @ApiResponse(responseCode = "200", description = "Purchase orders where supplied user is buyer or seller", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            array = @ArraySchema(
                schema = @Schema(implementation = PurchaseOrderResponse.class)
            )
        )
    )
    public Response getPurchaseOrderResponses(@QueryParam("behalfOf") Long behalfOf) {
        if (behalfOf == null) {
            return ResponseBuilder.build(Status.BAD_REQUEST, "behalfOf is a required parameter");
        }

        return ResponseBuilder.build(() -> service.getPurchaseOrderResponses(behalfOf));
    }

    @GET
    @Path("{id}/responses")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get purchase order responses for user without blockchain identity")
    @ApiResponse(responseCode = "200", description = "Purchase orders where supplied user is buyer or seller", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            array = @ArraySchema(
                schema = @Schema(implementation = PurchaseOrderResponse.class)
            )
        )
    )
    public Response getPurchaseOrderResponse(@PathParam("id") String id) {
        return ResponseBuilder.build(() -> service.getPurchaseOrderResponse(id));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a purchase order")
    @ApiResponse(responseCode = "200", description = "Purchase order", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = PurchaseOrder.class)
        )
    )
    public Response createPurchaseOrder(@Parameter( required = true ) CreatePurchaseOrder body) {
        return ResponseBuilder.build(() -> service.createPurchaseOrder(body.buyer, body.seller, body.price, body.units, body.productGtin));
    }

    @PUT
    @Path("{id}/accept")
    @Operation(summary = "Accept purchase order with specified ID")
    public Response acceptPurchaseOrder(@PathParam("id") String id) {
        return ResponseBuilder.build(() -> { service.acceptPurchaseOrder(id); return null; });
    }

    @PUT
    @Path("{id}/close")
    @Operation(summary = "Reject purchase order with specified ID")
    public Response closePurchaseOrder(@PathParam("id") String id) {
        return ResponseBuilder.build(() -> { service.closePurchaseOrder(id); return null; });
    }
}
