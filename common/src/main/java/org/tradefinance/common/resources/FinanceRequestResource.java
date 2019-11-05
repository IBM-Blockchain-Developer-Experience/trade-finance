package org.tradefinance.common.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.tradefinance.assets.FinanceRequest;
import org.tradefinance.common.services.FinanceRequestService;
import org.tradefinance.utils.ResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Finance Requests")
@Path("/financerequests")
public class FinanceRequestResource {

    public static FinanceRequestService service;

    public static void setService(FinanceRequestService service) {
        FinanceRequestResource.service = service;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get finance request with specified ID")
    @ApiResponse(responseCode = "200", description = "Finance request with supplied ID", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = FinanceRequest.class)
        )
    )
    public Response getFinanceRequest(@PathParam("id") String id) {
        Response response = ResponseBuilder.build(() -> { return service.getFinanceRequest(id); });

        if (response.getEntity() == null) {
            response = ResponseBuilder.build(Status.BAD_REQUEST, "Finance request not found with ID: " + id);
        }

        return response;
    }

    @GET
    @Path("hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get finance request with specified hash")
    @ApiResponse(responseCode = "200", description = "Finance request with supplied hash", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = FinanceRequest.class)
        )
    )
    public Response getFinanceRequestByHash(@PathParam("hash") String hash) {
        Response response = ResponseBuilder.build(() -> service.getFinanceRequestByHash(hash));

        if (response.getEntity() == null) {
            response = ResponseBuilder.build(Status.BAD_REQUEST, "Finance request not found with hash: " + hash);
        }

        return response;
    }

    @GET
    @Path("group/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get finance requests for group with specified hash")
    @ApiResponse(responseCode = "200", description = "Finance requests in group with supplied hash", content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            array = @ArraySchema(
                schema = @Schema(implementation = FinanceRequest.class)
            )
        )
    )
    public Response getFinanceRequestByGroupHash(@PathParam("hash") String hash) {
        System.out.println(service);
        return ResponseBuilder.build(() -> service.getFinanceRequestsByGroupHash(hash));
    }
}
