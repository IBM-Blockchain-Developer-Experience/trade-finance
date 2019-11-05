package org.tradefinance.ledger_api;

import org.tradefinance.ledger_api.states.State;

import org.hyperledger.fabric.contract.annotation.Property;

public abstract class Asset extends State {
    @Property()
    private String id;

    public Asset(String id) {
        super(new String[]{id});
        this.id = id;
    }

    public Asset(String id, String hash) {
        super(new String[]{id}, hash);
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
