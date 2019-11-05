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

import org.tradefinance.assets.Shipment;
import org.tradefinance.porest.defs.CreateShipment;
import org.tradefinance.porest.services.ShipmentService;
import org.tradefinance.utils.ResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public class ShipmentResource extends org.tradefinance.common.resources.ShipmentResource {
    public static ShipmentService service;

    public static void setService(ShipmentService service) {
        org.tradefinance.common.resources.ShipmentResource.setService(service);
        ShipmentResource.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get shipments for user without blockchain identity")
    @ApiResponse(responseCode = "200", description = "Shipments where supplied user is sender or receiver", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            array = @ArraySchema(
                schema = @Schema(implementation = Shipment.class)
            )
        )
    )
    public Response getShipments(@QueryParam("behalfOf") String behalfOf) {
        if (behalfOf == null) {
            return ResponseBuilder.build(Status.BAD_REQUEST, "behalfOf is a required parameter");
        }

        return ResponseBuilder.build(() -> service.getShipments(behalfOf));
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get shipment for given ID")
    @ApiResponse(responseCode = "200", description = "Shipment with supplied ID", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Shipment.class)
        )
    )
    public Response getPurchaseOrder(@PathParam("id") String id) {
        Response response = ResponseBuilder.build(() -> { return service.getShipment(id); });

        if (response.getEntity() == null) {
            response = ResponseBuilder.build(Status.BAD_REQUEST, "Shipment not found with ID: " + id);
        }

        return response;
    }

    @GET
    @Path("hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get shipment with supplied hash")
    @ApiResponse(responseCode = "200", description = "Shipment with supplied hash", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Shipment.class)
        )
    )
    public Response getShipmentByHash(@PathParam("hash") String hash) {
        Response response = ResponseBuilder.build(() -> { return service.getShipmentByHash(hash); });

        if (response.getEntity() == null) {
            response = ResponseBuilder.build(Status.BAD_REQUEST, "Shipment not found with hash: " + hash);
        }

        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a shipment matching to a purchase order")
    @ApiResponse(responseCode = "200", description = "Shipment", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Shipment.class)
        )
    )
    public Response createShipment(@Parameter(required = true) CreateShipment body) {
        return ResponseBuilder.build(() -> service.createShipment(body.purchaseOrderId, body.units, body.senderId, body.receiverId));
    }

    @PUT
    @Path("{id}/deliver")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Mark shipment as delivered")
    public Response deliverShipment(@PathParam("id") String id) {
        return ResponseBuilder.build(() -> { service.deliverShipment(id); return null; });
    }
}
