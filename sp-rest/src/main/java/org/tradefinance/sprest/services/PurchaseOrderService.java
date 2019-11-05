package org.tradefinance.sprest.services;

import org.tradefinance.assets.defs.Party;

public interface PurchaseOrderService extends org.tradefinance.common.services.PurchaseOrderService {
    public boolean verifyPurchaseOrder(String purchaseOrderId, long contentOwnerGln, Party buyer, Party seller, double price, int units, long productGtin) throws Exception;
}
