package org.tradefinance.contracts.purchase.utils;

import org.tradefinance.assets.Shipment;
import org.tradefinance.assets.ShipmentGroup;
import org.tradefinance.ledger_api.lists.AssetList;

import org.hyperledger.fabric.shim.ChaincodeStub;

public class ShipmentContext extends PurchaseOrderContext {
    private AssetList<Shipment> shipmentList;
    private AssetList<ShipmentGroup> shipmentGroupList;

    public ShipmentContext(ChaincodeStub stub) {
        super(stub);
        this.shipmentList = new AssetList<Shipment>(this, "org.tradefinance.Shipment", Shipment.class);
        this.shipmentGroupList = new AssetList<ShipmentGroup>(this, "org.tradefinance.ShipmentGroup", ShipmentGroup.class);
    }

    public AssetList<Shipment> getShipmentList() {
        return this.shipmentList;
    }

    public AssetList<ShipmentGroup> getShipmentGroupList() {
        return this.shipmentGroupList;
    }
}
