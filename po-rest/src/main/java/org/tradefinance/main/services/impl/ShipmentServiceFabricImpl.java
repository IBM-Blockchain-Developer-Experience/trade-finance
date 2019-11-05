package org.tradefinance.porest.services.impl;

import java.util.Arrays;
import java.util.Collection;

import com.google.gson.Gson;
import org.tradefinance.assets.Shipment;
import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;
import org.tradefinance.porest.services.ShipmentService;

public class ShipmentServiceFabricImpl extends org.tradefinance.common.services.impl.ShipmentServiceFabricImpl implements ShipmentService {

    public ShipmentServiceFabricImpl(FabricProxyConfig config, String identity) throws FabricProxyException {
        super(config, identity);
    }

    @Override
    public Shipment getShipment(String id) throws FabricProxyException {
        Gson gson = new Gson();
        String fcn = "getShipment";
        String response = this.proxy.evaluateTransaction(identity, subContractName, fcn, id);
        Shipment shipment = gson.fromJson(response, Shipment.class);
        return shipment;
    }

    @Override
    public Shipment getShipmentByHash(String hash) throws FabricProxyException {
        Gson gson = new Gson();
        String fcn = "getShipmentByHash";
        String response = this.proxy.evaluateTransaction(identity, subContractName, fcn, hash);
        Shipment shipment = gson.fromJson(response, Shipment.class);
        return shipment;
    }

    @Override
    public Collection<Shipment> getShipments(String behalfOfId) throws FabricProxyException {
        Gson gson = new Gson();
        String fcn = "getShipments";
        String response = this.proxy.evaluateTransaction(identity, subContractName, fcn, behalfOfId);
        Shipment[] shipments = gson.fromJson(response, Shipment[].class);
        return Arrays.asList(shipments);
    }

    @Override
    public Shipment createShipment(String purchaseOrderId, int units, String senderId, String receiverId) throws FabricProxyException{
        String unitsStr = String.valueOf(units);

        Gson gson = new Gson();
        String fcn = "createShipment";
        String response = this.proxy.submitTransaction(identity, subContractName, fcn, purchaseOrderId, unitsStr, senderId, receiverId);
        Shipment newShipment = gson.fromJson(response, Shipment.class);
        return newShipment;
    }

    @Override
    public void deliverShipment(String id) throws FabricProxyException {
        String fcn = "deliveredShipment";
        this.proxy.evaluateTransaction(identity, subContractName, fcn, id);
    }
}
