package org.tradefinance.assets.defs;

import org.tradefinance.ledger_api.annotations.DefaultDeserialize;
import org.tradefinance.ledger_api.states.Concept;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class OrderLineItem extends Concept {

    @Property
    private int lineItemNumber;

    @Property
    private int requestedQuantity;

    @Property
    private double netPrice;

    @Property
    private double netAmount;

    @Property
    private Item transactionalTradeItem;

    public OrderLineItem(int lineItemNumber, int requestedQuantity, double netPrice, long gtin) {
        this.lineItemNumber = lineItemNumber;
        this.requestedQuantity = requestedQuantity;
        this.netPrice = netPrice;
        this.netAmount = netPrice * requestedQuantity;
        this.transactionalTradeItem = new Item(gtin);
    }

    @DefaultDeserialize
    public OrderLineItem(int lineItemNumber, int requestedQuantity, double netPrice, Item transactionalTradeItem) {
        this.lineItemNumber = lineItemNumber;
        this.requestedQuantity = requestedQuantity;
        this.netPrice = netPrice;
        this.netAmount = netPrice * requestedQuantity;
        this.transactionalTradeItem = transactionalTradeItem;
    }

    public int getLineItemNumber() {
        return this.lineItemNumber;
    }

    public int getRequestedQuantity() {
        return this.requestedQuantity;
    }

    public double getNetPrice() {
        return this.netPrice;
    }

    public double getNetAmount() {
        return this.netAmount;
    }

    public Item getTransactionalTradeItem() {
        return this.transactionalTradeItem;
    }
}
