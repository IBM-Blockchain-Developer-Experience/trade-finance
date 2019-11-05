package org.tradefinance.assets.defs;

import org.tradefinance.ledger_api.annotations.DefaultDeserialize;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class OrderIdentification extends Identification {
    @Property
    private Party contentOwner;

    public OrderIdentification(String entityIdentification, long contentOwnerGln) {
        super(entityIdentification);
        this.contentOwner = new Party(contentOwnerGln);
    }

    @DefaultDeserialize
    public OrderIdentification(String entityIdentification, Party contentOwner) {
        super(entityIdentification);
        this.contentOwner = contentOwner;
    }

    public Party getContentOwner() {
        return this.contentOwner;
    }
}
