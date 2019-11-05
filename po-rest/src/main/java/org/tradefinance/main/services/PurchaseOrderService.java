package org.tradefinance.porest.services;

import java.util.Collection;

import org.tradefinance.assets.PurchaseOrder;
import org.tradefinance.assets.PurchaseOrderResponse;
import org.tradefinance.assets.defs.Party;

public interface PurchaseOrderService extends org.tradefinance.common.services.PurchaseOrderService {
    public Collection<PurchaseOrder> getPurchaseOrders(long behalfOfGln) throws Exception;

    public PurchaseOrder getPurchaseOrder(String id) throws Exception;

    public PurchaseOrder getPurchaseOrderByHash(String hash) throws Exception;

    public Collection<PurchaseOrderResponse> getPurchaseOrderResponses(long behalfOfGln) throws Exception;

    public PurchaseOrderResponse getPurchaseOrderResponse(String id) throws Exception;

    public PurchaseOrder createPurchaseOrder(Party buyer, Party seller, double price, int units, long productGtin) throws Exception;

    public void acceptPurchaseOrder(String id) throws Exception;

    public void closePurchaseOrder(String id) throws Exception;
}
