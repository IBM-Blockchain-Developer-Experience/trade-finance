package org.tradefinance.porest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;
import org.tradefinance.porest.resources.FinanceRequestResource;
import org.tradefinance.porest.resources.PurchaseOrderResource;
import org.tradefinance.porest.resources.ShipmentResource;
import org.tradefinance.porest.services.FinanceRequestService;
import org.tradefinance.porest.services.PurchaseOrderService;
import org.tradefinance.porest.services.ShipmentService;
import org.tradefinance.porest.services.impl.FinanceRequestServiceFabricImpl;
import org.tradefinance.porest.services.impl.PurchaseOrderServiceFabricImpl;
import org.tradefinance.porest.services.impl.ShipmentServiceFabricImpl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class App {

    private static void startServer(String url, int port) {
        ResourceConfig rc = ResourceConfig.forApplication(new PORestApplication());
        rc.register(JacksonFeature.class);

        URI uri = UriBuilder.fromUri("http://" + url + "/api").port(port).build();

        GrizzlyHttpServerFactory.createHttpServer(uri, rc);
    }

    public static void main(String[] args) throws IOException {
        Options options = new Options();

        options.addRequiredOption("w", "wallet", true, "path to wallet");
        options.addRequiredOption("c", "connectionProfile", true, "path to connection profile");
        options.addRequiredOption("o", "org", true, "organisation ID");
        options.addRequiredOption("i", "identity", true, "Identity");
        options.addRequiredOption("p", "port", true, "API Port");
        options.addRequiredOption("j", "peer", true, "Peer name");
        options.addOption("u", "url", true, "Address to run on. Default localhost");

        CommandLineParser clp = new DefaultParser();
        CommandLine cmd = null;


        try {
            cmd = clp.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Failed to start SP REST. " + e.getMessage());
            System.exit(1);
        }

        String identity = cmd.getOptionValue("i");
        int port = Integer.parseInt(cmd.getOptionValue("p"));

        Path walletPath = Paths.get(cmd.getOptionValue("w"));
        Path connectionProfilePath = Paths.get(cmd.getOptionValue("c"));

        String url = cmd.getOptionValue("u");

        if (url == null) {
            url = "localhost";
        }

        PurchaseOrderService purchaseOrderService;
        FinanceRequestService financeRequestService;
        ShipmentService shipmentService;

        try {
            String purchaseChannelName = "tradenetpurchase";
            String purchaseContractName = "purchasecontract";
            String financeChannelName = "tradenetfinance";
            String financeContractName = "financecontract";
            String org = cmd.getOptionValue("o");
            String targetPeer = cmd.getOptionValue("j");

            FabricProxyConfig purchaseConfig = new FabricProxyConfig(walletPath, connectionProfilePath, purchaseChannelName, purchaseContractName, org);
            FabricProxyConfig financeConfig = new FabricProxyConfig(walletPath, connectionProfilePath, financeChannelName, financeContractName, org);

            financeRequestService = new FinanceRequestServiceFabricImpl(financeConfig, identity, targetPeer);
            purchaseOrderService = new PurchaseOrderServiceFabricImpl(purchaseConfig, identity);
            shipmentService = new ShipmentServiceFabricImpl(purchaseConfig, identity);
        } catch (FabricProxyException exception) {
            exception.printStackTrace();
            System.exit(1);
            return;
        }

        FinanceRequestResource.setService(financeRequestService);
        PurchaseOrderResource.setService(purchaseOrderService);
        ShipmentResource.setService(shipmentService);

        startServer(url, port);
    }
}
