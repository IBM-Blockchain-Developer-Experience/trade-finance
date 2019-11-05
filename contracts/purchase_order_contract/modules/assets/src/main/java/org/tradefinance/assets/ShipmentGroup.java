package org.tradefinance.assets;

import org.tradefinance.ledger_api.Asset;
import org.tradefinance.ledger_api.annotations.DefaultDeserialize;
import org.tradefinance.ledger_api.annotations.Deserialize;
import org.tradefinance.ledger_api.annotations.Private;
import org.tradefinance.ledger_api.annotations.VerifyHash;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class ShipmentGroup extends Asset {

    @Property
    @Private
    private String[] shipmentIds;

    @VerifyHash
    @Deserialize
    public ShipmentGroup(String id, String[] shipmentIds) {
        super(id);

        this.shipmentIds = shipmentIds;

        this.updateHash();
    }

    @DefaultDeserialize
    public ShipmentGroup(String id, String hash) {
        super(id, hash);
    }

    public String[] getShipmentIds() {
        return this.shipmentIds;
    }

    public void addShipmentId(String id) {
        String[] newShipmentIds = new String[shipmentIds.length + 1];
        System.arraycopy(this.shipmentIds, 0, newShipmentIds, 0, shipmentIds.length);
        newShipmentIds[shipmentIds.length] = id;
        this.shipmentIds = newShipmentIds;
    }
}
