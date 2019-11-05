package org.tradefinance.assets;

import org.tradefinance.ledger_api.Asset;
import org.tradefinance.ledger_api.annotations.DefaultDeserialize;
import org.tradefinance.ledger_api.annotations.Deserialize;
import org.tradefinance.ledger_api.annotations.Private;
import org.tradefinance.ledger_api.annotations.VerifyHash;

import org.hyperledger.fabric.contract.annotation.Property;

public class FinanceRequestGroup extends Asset {

    @Property
    @Private
    private String[] requestIds;

    @VerifyHash
    @Deserialize
    public FinanceRequestGroup(String id, String[] requestIds) {
        super(id);

        this.requestIds = requestIds;

        this.updateHash();
    }

    @DefaultDeserialize
    public FinanceRequestGroup(String id, String hash) {
        super(id, hash);
    }

    public String[] getRequestIds() {
        return this.requestIds;
    }
}
