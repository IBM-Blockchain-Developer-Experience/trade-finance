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

import org.tradefinance.assets.FinanceRequest;
import org.tradefinance.assets.FinanceRequestGroup;
import org.tradefinance.porest.defs.CreateFinanceRequests;
import org.tradefinance.porest.services.FinanceRequestService;
import org.tradefinance.utils.ResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public class FinanceRequestResource extends org.tradefinance.common.resources.FinanceRequestResource {

    public static FinanceRequestService service;

    public static void setService(FinanceRequestService service) {
        org.tradefinance.common.resources.FinanceRequestResource.setService(service);
        FinanceRequestResource.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get finance requests for user without blockchain identity")
    @ApiResponse(responseCode = "200", description = "Finance requests for supplied user", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            array = @ArraySchema(
                schema = @Schema(implementation = FinanceRequest.class)
            )
        )
    )
    public Response getFinanceRequests(@Parameter(required = true) @QueryParam("behalfOf") String behalfOf) {
        if (behalfOf == null) {
            return ResponseBuilder.build(Status.BAD_REQUEST, "behalfOf is a required parameter");
        }

        return ResponseBuilder.build(() -> service.getFinanceRequests(behalfOf));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a group of finance requests")
    @ApiResponse(responseCode = "200", description = "Finance request group", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = FinanceRequestGroup.class)
        )
    )
    public Response createFinanceRequests(@Parameter(required = true) CreateFinanceRequests data) {
        return ResponseBuilder.build(() -> service.createFinanceRequest(data.requester, data.financierIds, data.purchaseOrderId, data.amount, data.interest, data.monthLength));
    }

    @PUT
    @Path("{id}/accept")
    @Operation(summary = "Accept finance request with specified id")
    public Response acceptFinanceRequest(@PathParam("id") String requestId) {
        return ResponseBuilder.build(() -> { service.acceptFinanceRequest(requestId); return null; });
    }

    @PUT
    @Path("{id}/withdraw")
    @Operation(summary = "Withdraw finance request with specified id")
    public Response withdrawFinanceRequest(@PathParam("id") String requestId) {
        return ResponseBuilder.build(() -> { service.withdrawFinanceRequest(requestId); return null; });
    }
}
