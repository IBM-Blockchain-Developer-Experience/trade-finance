package org.tradefinance.porest.services.impl;

import java.util.Arrays;
import java.util.Collection;

import com.google.gson.Gson;
import org.tradefinance.assets.PurchaseOrder;
import org.tradefinance.assets.PurchaseOrderResponse;
import org.tradefinance.assets.defs.Party;
import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;
import org.tradefinance.porest.services.PurchaseOrderService;

public class PurchaseOrderServiceFabricImpl extends org.tradefinance.common.services.impl.PurchaseOrderServiceFabricImpl implements PurchaseOrderService {

    public PurchaseOrderServiceFabricImpl(FabricProxyConfig config, String identity) throws FabricProxyException {
        super(config, identity);
    }

    public Collection<PurchaseOrder> getPurchaseOrders(long behalfOfGln) throws FabricProxyException {
        Gson gson = new Gson();
        String fcn = "getPurchaseOrders";
        String response = this.proxy.evaluateTransaction(identity, this.subContractName, fcn, Long.toString(behalfOfGln));
        PurchaseOrder[] purchaseOrders = gson.fromJson(response, PurchaseOrder[].class);
        return Arrays.asList(purchaseOrders);
    }

    public PurchaseOrder getPurchaseOrder(String id) throws FabricProxyException {
        Gson gson = new Gson();
        String fcn = "getPurchaseOrder";
        String response = this.proxy.evaluateTransaction(identity, this.subContractName, fcn, new String[]{id});
        PurchaseOrder purchaseOrder = gson.fromJson(response, PurchaseOrder.class);
        return purchaseOrder;
    }

    public PurchaseOrder getPurchaseOrderByHash(String hash) throws FabricProxyException {
        Gson gson = new Gson();
        String fcn = "getPurchaseOrderByHash";
        String response = this.proxy.evaluateTransaction(identity, this.subContractName, fcn, new String[]{hash});
        PurchaseOrder purchaseOrder = gson.fromJson(response, PurchaseOrder.class);
        return purchaseOrder;
    }

    public Collection<PurchaseOrderResponse> getPurchaseOrderResponses(long behalfOfGln) throws FabricProxyException {
        Gson gson = new Gson();
        String fcn = "getPurchaseOrderResponses";
        String response = this.proxy.evaluateTransaction(identity, this.subContractName, fcn, Long.toString(behalfOfGln));
        PurchaseOrderResponse[] purchaseOrders = gson.fromJson(response, PurchaseOrderResponse[].class);
        return Arrays.asList(purchaseOrders);
    }

    public PurchaseOrderResponse getPurchaseOrderResponse(String id) throws FabricProxyException {
        Gson gson = new Gson();
        String fcn = "getPurchaseOrderResponse";
        String response = this.proxy.evaluateTransaction(identity, this.subContractName, fcn, new String[]{id});
        PurchaseOrderResponse purchaseOrderResponse = gson.fromJson(response, PurchaseOrderResponse.class);
        return purchaseOrderResponse;
    }

    public PurchaseOrder createPurchaseOrder(Party buyer, Party seller, double price, int units, long productGtin) throws FabricProxyException {
        String buyerStr = buyer.serialize();
        String sellerStr = seller.serialize();
        String priceStr = String.valueOf(price);
        String unitsStr = String.valueOf(units);
        String productGtinStr = String.valueOf(productGtin);

        String fcn = "createPurchaseOrder";
        String response = this.proxy.submitTransaction(
            identity,
            this.subContractName,
            fcn,
            buyerStr,
            sellerStr,
            priceStr,
            unitsStr,
            productGtinStr
        );
        Gson gson = new Gson();

        return gson.fromJson(response, PurchaseOrder.class);
    }

    public void acceptPurchaseOrder(String id) throws FabricProxyException {
        this.proxy.submitTransaction(identity, this.subContractName, "acceptPurchaseOrder", id);
    }

    public void closePurchaseOrder(String id) throws FabricProxyException {
        this.proxy.submitTransaction(identity, this.subContractName, "closePurchaseOrder", id);
    }
}
