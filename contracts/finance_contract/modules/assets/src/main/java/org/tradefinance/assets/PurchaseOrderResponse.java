package org.tradefinance.assets;

import org.tradefinance.assets.defs.Identification;
import org.tradefinance.assets.defs.OrderIdentification;
import org.tradefinance.assets.defs.Party;
import org.tradefinance.assets.enums.ResponseStatusCode;
import org.tradefinance.ledger_api.Asset;
import org.tradefinance.ledger_api.annotations.DefaultDeserialize;
import org.tradefinance.ledger_api.annotations.Deserialize;
import org.tradefinance.ledger_api.annotations.Private;
import org.tradefinance.ledger_api.annotations.VerifyHash;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class PurchaseOrderResponse extends Asset {

    @Property
    @Private
    private OrderIdentification orderResponseIdentification;

    @Property
    @Private
    private ResponseStatusCode responseStatusCode;

    @Property
    @Private
    private Party buyer;

    @Property
    @Private
    private Identification originalOrder;

    @VerifyHash
    public PurchaseOrderResponse(String id, long contentOwnerGln, ResponseStatusCode responseStatusCode, Party buyer, String originalOrderId) {
        super(id);

        this.orderResponseIdentification = new OrderIdentification(id, contentOwnerGln);
        this.responseStatusCode = responseStatusCode;
        this.buyer = buyer;
        this.originalOrder = new Identification(originalOrderId);

        this.updateHash();
    }

    @Deserialize
    public PurchaseOrderResponse(OrderIdentification orderResponseIdentification, ResponseStatusCode responseStatusCode, Party buyer, Identification originalOrder) {
        super(orderResponseIdentification.getEntityIdentification());

        this.orderResponseIdentification = orderResponseIdentification;
        this.responseStatusCode = responseStatusCode;
        this.buyer = buyer;
        this.originalOrder = originalOrder;

        this.updateHash();
    }

    @DefaultDeserialize
    public PurchaseOrderResponse(String id, String hash) {
        super(id, hash);
    }

    public OrderIdentification getOrderResponseIdentification() {
        return this.orderResponseIdentification;
    }

    public ResponseStatusCode getResponseStatusCode() {
        return this.responseStatusCode;
    }

    public Party getBuyer() {
        return this.buyer;
    }

    public Identification getOriginalOrder() {
        return this.originalOrder;
    }
}
