package org.tradefinance.contracts.purchase;

import java.util.ArrayList;

import org.tradefinance.assets.PurchaseOrder;
import org.tradefinance.assets.PurchaseOrderResponse;
import org.tradefinance.assets.defs.Party;
import org.tradefinance.assets.enums.ResponseStatusCode;
import org.tradefinance.contracts.BaseContract;
import org.tradefinance.contracts.purchase.utils.PurchaseOrderContext;
import org.tradefinance.contracts.utils.Utils;
import org.tradefinance.contracts.utils.annotations.ACLRule;

import java.util.logging.Logger;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.json.JSONArray;
import org.json.JSONObject;

@Contract(name = "PurchaseOrderContract",
    info = @Info(title = "PurchaseOrder contract",
                description = "A contract for handling purchase orders and trades",
                version = "0.0.1"
    )
)
@Default
public class PurchaseOrderContract extends BaseContract implements ContractInterface {
    protected final Logger logger = Logger.getLogger(PurchaseOrderContract.class.getName());
    public  PurchaseOrderContract() {
        super(PurchaseOrderContract.class);
    }

    @Override
    public PurchaseOrderContext createContext(ChaincodeStub stub) {
        return new PurchaseOrderContext(stub);
    }

    @Transaction()
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.PurchaseOrder.CREATE')"
    )
    public PurchaseOrder createPurchaseOrder(PurchaseOrderContext ctx, Party buyer, Party seller, Double price, int units, long productGtin) {

        if (!Utils.isIdentityNameValidFormat(buyer.getAdditionalPartyIdentification())) {
            throw new RuntimeException(Utils.invalidIdentityNameErrorBuilder("buyer additionalPartyIdentification"));
        } else if (!Utils.isIdentityNameValidFormat(seller.getAdditionalPartyIdentification())) {
            throw new RuntimeException(Utils.invalidIdentityNameErrorBuilder("seller additionalPartyIdentification"));
        }

        int count = ctx.getPurchaseOrderList().count();
        String[] collections = ctx.getCallerPrivateCollectionNames();
        collections = Utils.append(collections, Utils.getIdentityOrg(seller.getAdditionalPartyIdentification()));

        final PurchaseOrder po = new PurchaseOrder("PO" + count, ctx.getCallerGln(), buyer, seller, units, price, productGtin);

        ctx.getPurchaseOrderList().add(po, collections);

        return po.toPublicForm();
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.PurchaseOrder.READ')"
    )
    public PurchaseOrder getPurchaseOrder(PurchaseOrderContext ctx, String purchaseOrderId) {
        String[] collections = ctx.getCallerPrivateCollectionNames();
        return ctx.getPurchaseOrderList().get(purchaseOrderId, collections);
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.PurchaseOrder.READ')"
    )
    public PurchaseOrder getPurchaseOrderByHash(PurchaseOrderContext ctx, String hash) {
        String[] collections = ctx.getCallerPrivateCollectionNames();
        return ctx.getPurchaseOrderList().getByHash(hash, collections);
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.PurchaseOrder.READ')"
    )
    public PurchaseOrder[] getPurchaseOrders(PurchaseOrderContext ctx, long behalfOfGln) {
        String[] collections = ctx.getCallerPrivateCollectionNames();

        JSONObject buyerQuery = new JSONObject("{\"selector\": {\"buyer\": {\"gln\": " + behalfOfGln + "}}}");
        JSONObject sellerQuery = new JSONObject("{\"selector\": {\"seller\": {\"gln\": " + behalfOfGln + "}}}");

        ArrayList<PurchaseOrder> buyerMatches = ctx.getPurchaseOrderList().query(buyerQuery, collections);
        ArrayList<PurchaseOrder> sellerMatches = ctx.getPurchaseOrderList().query(sellerQuery, collections);

        buyerMatches.addAll(sellerMatches);

        return buyerMatches.toArray(new PurchaseOrder[buyerMatches.size()]);
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.PurchaseOrder.READ')"
    )
    public PurchaseOrderResponse getPurchaseOrderResponse(PurchaseOrderContext ctx, String purchaseOrderId) {
        String[] collections = ctx.getCallerPrivateCollectionNames();

        String query = "{\"selector\": { \"originalOrder\": { \"entityIdentification\": \""+purchaseOrderId+"\"}}}";

        JSONObject poQuery = new JSONObject(query);

        ArrayList<PurchaseOrderResponse> pors = ctx.getPurchaseOrderResponseList().query(poQuery, collections);

        if (pors.size() != 1) {
            throw new RuntimeException("Error retrieving purchase order response. Incorrect number of responses");
        }

        return pors.get(0);
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.PurchaseOrder.READ')"
    )
    public PurchaseOrderResponse[] getPurchaseOrderResponses(PurchaseOrderContext ctx, long behalfOfGln) {
        String[] collections = ctx.getCallerPrivateCollectionNames();

        PurchaseOrder[] pos = this.getPurchaseOrders(ctx, behalfOfGln);

        String[] poIds = new String[pos.length];

        for (int i = 0; i < pos.length; i++) {
            poIds[i] = pos[i].getId();
        }

        String foundIds = new JSONArray(poIds).toString();

        JSONObject porQuery = new JSONObject("{\"selector\": {\"originalOrder\": { \"entityIdentification\": {\"$in\": " + foundIds + "}}}}");

        ArrayList<PurchaseOrderResponse> pors = ctx.getPurchaseOrderResponseList().query(porQuery, collections);

        return pors.toArray(new PurchaseOrderResponse[pors.size()]);
    }

    @Transaction()
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.PurchaseOrder.UPDATE')"
    )
    public void acceptPurchaseOrder(PurchaseOrderContext ctx, String purchaseOrderId) {
        String[] collections = ctx.getCallerPrivateCollectionNames();

        PurchaseOrder po = ctx.getPurchaseOrderList().get(purchaseOrderId, collections);

        if (po == null) {
            throw new RuntimeException("Purchase order (" + purchaseOrderId + ") does not exist");
        }

        String buyerInfo = po.getBuyer().getAdditionalPartyIdentification();
        String sellerOrg = Utils.getIdentityOrg(po.getSeller().getAdditionalPartyIdentification());

        if (!ctx.getCallerOrg().equals(sellerOrg)) {
            throw new RuntimeException("Caller must be from same PO as seller");
        }

        collections = Utils.append(collections, Utils.getIdentityOrg(buyerInfo));

        Party contentOwner = po.getOrderIdentification().getContentOwner();

        // ID LINKED TO PURCHASE ORDER TO PREVENT MORE THAN 1 PER PURCHASE ORDER
        PurchaseOrderResponse por = new PurchaseOrderResponse(purchaseOrderId, contentOwner.getGln(), ResponseStatusCode.ACCEPTED, po.getBuyer(), purchaseOrderId);

        ctx.getPurchaseOrderResponseList().add(por, collections);
    }

    @Transaction()
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.PurchaseOrder.UPDATE')"
    )
    public void closePurchaseOrder(PurchaseOrderContext ctx, String purchaseOrderId) {
        String[] collections = ctx.getCallerPrivateCollectionNames();

        PurchaseOrder po = ctx.getPurchaseOrderList().get(purchaseOrderId, collections);

        String buyerInfo = po.getBuyer().getAdditionalPartyIdentification();
        String sellerOrg = Utils.getIdentityOrg(po.getSeller().getAdditionalPartyIdentification());

        if (!ctx.getCallerOrg().equals(sellerOrg)) {
            throw new RuntimeException("Caller must be from same PO as seller");
        }

        collections = Utils.append(collections, Utils.getIdentityOrg(buyerInfo));

        Party contentOwner = po.getOrderIdentification().getContentOwner();

        // ID LINKED TO PURCHASE ORDER TO PREVENT MORE THAN 1 PER PURCHASE ORDER
        PurchaseOrderResponse por = new PurchaseOrderResponse(purchaseOrderId, contentOwner.getGln(), ResponseStatusCode.REJECTED, po.getBuyer(), purchaseOrderId);

        ctx.getPurchaseOrderResponseList().add(por, collections);
    }

    @Transaction( submit = false )
    public boolean verifyPurchaseOrder(PurchaseOrderContext ctx, String purchaseOrderId, long contentOwnerGln, Party buyer, Party seller, int quantity, double unitPrice, long productGtin) {
        final PurchaseOrder storedPurchaseOrder = ctx.getPurchaseOrderList().get(purchaseOrderId);

        return PurchaseOrder.verifyHash(PurchaseOrder.class, storedPurchaseOrder.getHash(), purchaseOrderId, contentOwnerGln, buyer, seller, quantity, unitPrice, productGtin);
    }
}
