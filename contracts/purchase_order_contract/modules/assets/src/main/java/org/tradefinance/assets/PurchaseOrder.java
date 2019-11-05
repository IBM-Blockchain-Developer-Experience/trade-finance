package org.tradefinance.assets;

import org.tradefinance.assets.defs.OrderIdentification;
import org.tradefinance.assets.defs.OrderLineItem;
import org.tradefinance.assets.defs.Party;
import org.tradefinance.ledger_api.Asset;
import org.tradefinance.ledger_api.annotations.DefaultDeserialize;
import org.tradefinance.ledger_api.annotations.Deserialize;
import org.tradefinance.ledger_api.annotations.Private;
import org.tradefinance.ledger_api.annotations.VerifyHash;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public class PurchaseOrder extends Asset {

    @Property
    @Private
    private OrderIdentification orderIdentification;

    @Property
    @Private
    private Integer orderTypeCode;

    @Property
    @Private
    private String orderInstructionCode;

    @Property
    @Private
    private Party buyer;

    @Property
    @Private
    private Party seller;

    @Property
    @Private
    private OrderLineItem orderLineItem;

    @VerifyHash
    public PurchaseOrder(String id, long contentOwnerGln, Party buyer, Party seller, int quantity, double unitPrice, long productGtin) {
        super(id);

        this.orderIdentification = new OrderIdentification(id, contentOwnerGln);
        this.orderTypeCode = 220;
        this.orderInstructionCode = "PARTIAL_DELIVERY_ALLOWED";
        this.buyer = buyer;
        this.seller = seller;
        this.orderLineItem = new OrderLineItem(1, quantity, unitPrice, productGtin);

        this.updateHash();
    }

    @Deserialize
    public PurchaseOrder(OrderIdentification orderIdentification, Integer orderTypeCode, String orderInstructionCode, Party buyer, Party seller, OrderLineItem orderLineItem) {
        super(orderIdentification.getEntityIdentification());

        this.orderIdentification = orderIdentification;
        this.orderTypeCode = orderTypeCode;
        this.orderInstructionCode = orderInstructionCode;
        this.buyer = buyer;
        this.seller = seller;
        this.orderLineItem = orderLineItem;

        this.updateHash();
    }

    @DefaultDeserialize
    public PurchaseOrder(String id, String hash) {
        super(id, hash);
    }

    public OrderIdentification getOrderIdentification() {
        return this.orderIdentification;
    }

    public Integer getOrderTypeCode() {
        return this.orderTypeCode;
    }

    public String getOrderInstructionCode() {
        return this.orderInstructionCode;
    }

    public Party getBuyer() {
        return this.buyer;
    }

    public Party getSeller() {
        return this.seller;
    }

    public OrderLineItem getOrderLineItem() {
        return this.orderLineItem;
    }
}
