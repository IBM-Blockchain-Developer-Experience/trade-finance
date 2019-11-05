package org.tradefinance.sprest.services.impl;

import org.tradefinance.assets.defs.Party;
import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;
import org.tradefinance.sprest.services.PurchaseOrderService;

public class PurchaseOrderServiceFabricImpl extends org.tradefinance.common.services.impl.PurchaseOrderServiceFabricImpl implements PurchaseOrderService {

    public PurchaseOrderServiceFabricImpl(FabricProxyConfig config, String identity) throws FabricProxyException {
        super(config, identity);
    }

    @Override
    public boolean verifyPurchaseOrder(String purchaseOrderId, long contentOwnerGln, Party buyer, Party seller, double price, int units, long productGtin) throws FabricProxyException {
        String contentOwnerStr = String.valueOf(contentOwnerGln);
        String buyerStr = buyer.serialize();
        String sellerStr = seller.serialize();
        String priceStr = String.valueOf(price);
        String unitsStr = String.valueOf(units);
        String productGtinStr = String.valueOf(productGtin);

        String fcn = "verifyPurchaseOrder";
        String response = this.proxy.evaluateTransaction(identity, this.subContractName, fcn, purchaseOrderId, contentOwnerStr, buyerStr, sellerStr, unitsStr, priceStr, productGtinStr);

        return response.equals("true");
    }
}
