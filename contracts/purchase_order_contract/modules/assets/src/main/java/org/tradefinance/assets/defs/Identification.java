package org.tradefinance.assets.defs;

import org.tradefinance.ledger_api.annotations.DefaultDeserialize;
import org.tradefinance.ledger_api.states.Concept;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class Identification extends Concept {
    @Property
    private String entityIdentification;

    @DefaultDeserialize
    public Identification(String entityIdentification) {
        this.entityIdentification = entityIdentification;
    }

    public String getEntityIdentification() {
        return this.entityIdentification;
    }
}
