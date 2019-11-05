package org.tradefinance.ledger_api.lists;

import org.tradefinance.ledger_api.Asset;
import org.tradefinance.ledger_api.states.StateList;

import org.hyperledger.fabric.contract.Context;

public class AssetList<T extends Asset> extends StateList<T> {
    public AssetList(Context ctx, String listName, Class<T> clazz) {
        super(ctx, listName);

        this.use(clazz);
    }
}
