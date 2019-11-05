package org.tradefinance.sprest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.tradefinance.assets.FinanceRequest;
import org.tradefinance.sprest.services.FinanceRequestService;
import org.tradefinance.utils.ResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
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
    public Response getFinanceRequests() {
        return ResponseBuilder.build(() -> service.getFinanceRequests());
    }

    @PUT
    @Path("{id}/approve")
    @Operation(summary = "Approve finance request with specified id")
    public Response acceptFinanceRequest(@PathParam("id") String requestId) {
        return ResponseBuilder.build(() -> { service.approveFinanceRequest(requestId); return null; });
    }

    @PUT
    @Path("{id}/reject")
    @Operation(summary = "Reject finance request with specified id")
    public Response rejectFinanceRequest(@PathParam("id") String requestId) {
        return ResponseBuilder.build(() -> { service.rejectFinanceRequest(requestId); return null; });
    }
}
