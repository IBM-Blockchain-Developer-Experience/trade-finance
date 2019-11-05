package org.tradefinance.contracts.finance.utils;

import org.tradefinance.assets.FinanceRequest;
import org.tradefinance.assets.FinanceRequestGroup;
import org.tradefinance.contracts.utils.BaseContext;
import org.tradefinance.ledger_api.lists.AssetList;

import org.hyperledger.fabric.shim.ChaincodeStub;

public class FinanceRequestContext extends BaseContext {

    private AssetList<FinanceRequest> financeRequestList;
    private AssetList<FinanceRequestGroup> financeRequestGroupList;

    public FinanceRequestContext(ChaincodeStub stub) {
        super(stub);

        this.financeRequestList = new AssetList<FinanceRequest>(this, "org.tradefinance.FinanceRequest", FinanceRequest.class);
        this.financeRequestGroupList = new AssetList<FinanceRequestGroup>(this, "org.tradefinance.FinanceRequestGroup", FinanceRequestGroup.class);
    }

    public AssetList<FinanceRequest> getFinanceRequestList() {
        return this.financeRequestList;
    }

    public AssetList<FinanceRequestGroup> getFinanceRequestGroupList() {
        return this.financeRequestGroupList;
    }
}
