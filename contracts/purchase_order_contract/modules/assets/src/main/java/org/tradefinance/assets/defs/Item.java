package org.tradefinance.assets.defs;

import org.tradefinance.ledger_api.annotations.DefaultDeserialize;
import org.tradefinance.ledger_api.states.Concept;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class Item extends Concept {

    @Property
    private long gtin;

    @DefaultDeserialize
    public Item(long gtin) {
        this.gtin = gtin;
    }

    public long getGtin() {
        return this.gtin;
    }
}
