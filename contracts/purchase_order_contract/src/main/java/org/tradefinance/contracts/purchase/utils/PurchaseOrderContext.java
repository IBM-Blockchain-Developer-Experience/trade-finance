package org.tradefinance.contracts.purchase.utils;

import org.tradefinance.assets.PurchaseOrder;
import org.tradefinance.assets.PurchaseOrderResponse;
import org.tradefinance.contracts.utils.BaseContext;
import org.tradefinance.ledger_api.lists.AssetList;

import org.hyperledger.fabric.shim.ChaincodeStub;

public class PurchaseOrderContext extends BaseContext {

    private AssetList<PurchaseOrder> purchaseOrderList;
    private AssetList<PurchaseOrderResponse> purchaseOrderResponseList;

    public PurchaseOrderContext(ChaincodeStub stub) {
        super(stub);

        this.purchaseOrderList = new AssetList<PurchaseOrder>(this, "org.tradefinance.PurchaseOrder", PurchaseOrder.class);
        this.purchaseOrderResponseList = new AssetList<PurchaseOrderResponse>(this, "org.tradefinance.PurchaseOrderResponse", PurchaseOrderResponse.class);
    }

    public AssetList<PurchaseOrder> getPurchaseOrderList() {
        return this.purchaseOrderList;
    }

    public AssetList<PurchaseOrderResponse> getPurchaseOrderResponseList() {
        return this.purchaseOrderResponseList;
    }
}
