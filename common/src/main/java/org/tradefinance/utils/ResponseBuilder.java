package org.tradefinance.utils;

import java.util.concurrent.Callable;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ResponseBuilder {

    public static String format = "EEE MMM d HH:mm:ss Z yyy";
    public static Gson gson = new GsonBuilder().setDateFormat(format).create();

    public static Response build(Status status, String reason) {
        return Response.status(status).entity(reason).build();
    }

    public static Response build(Callable<Object> action) {

        String entity;
        Status status = Status.OK;

        try {
            Object got = action.call();

            if (got != null) {
                entity = gson.toJsonTree(got).toString();
            } else {
                entity = null;
            }
        } catch (Exception exception) {
            entity = gson.toJsonTree(exception).toString();
            status = Status.INTERNAL_SERVER_ERROR;
		}

        return Response.status(status).entity(entity).build();
    }
}
