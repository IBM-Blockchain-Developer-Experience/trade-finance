package org.tradefinance.porest.services;

import java.util.Collection;

import org.tradefinance.assets.Shipment;

public interface ShipmentService extends org.tradefinance.common.services.ShipmentService {

    public Collection<Shipment> getShipments(String behalfOfId) throws Exception;

    public Shipment getShipment(String id) throws Exception;

    public Shipment getShipmentByHash(String id) throws Exception;

    public Shipment createShipment(String purchaseOrderId, int units, String senderId, String receiverId) throws Exception;

    public void deliverShipment(String id) throws Exception;
}
