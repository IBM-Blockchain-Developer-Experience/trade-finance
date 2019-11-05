package org.tradefinance.assets.defs;

import org.tradefinance.ledger_api.annotations.DefaultDeserialize;
import org.tradefinance.ledger_api.annotations.OptionalParam;
import org.tradefinance.ledger_api.states.Concept;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class Party extends Concept {

    @Property
    private Long gln;

    @Property
    private String additionalPartyIdentification;

    public Party() {
        // function used by fabric chaincode api to create object when taken in as function
    }

    public Party(long gln) {
        this.gln = gln;
    }

    @DefaultDeserialize
    public Party(long gln, @OptionalParam String additionalPartyIdentification) {
        this.gln = gln;
        this.additionalPartyIdentification = additionalPartyIdentification;
    }

    public Long getGln() {
        return this.gln;
    }

    public String getAdditionalPartyIdentification() {
        return this.additionalPartyIdentification;
    }
}
