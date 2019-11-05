package org.tradefinance.contracts.purchase;

import java.util.ArrayList;

import org.tradefinance.assets.PurchaseOrder;
import org.tradefinance.assets.Shipment;
import org.tradefinance.assets.ShipmentGroup;
import org.tradefinance.assets.enums.ShipmentStatus;
import org.tradefinance.contracts.BaseContract;
import org.tradefinance.contracts.purchase.utils.ShipmentContext;
import org.tradefinance.contracts.utils.Utils;
import org.tradefinance.contracts.utils.annotations.ACLRule;

import java.util.logging.Logger;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.json.JSONObject;

@Contract(name = "ShipmentContract",
    info = @Info(title = "ShipmentContract contract",
                description = "A contract for handling widget shipment",
                version = "0.0.1"
    )
)
@Default
public class ShipmentContract extends BaseContract implements ContractInterface {
    protected Logger logger = Logger.getLogger(ShipmentContract.class.getName());
    public ShipmentContract() {
        super(ShipmentContract.class);
    }

    @Override
    public ShipmentContext createContext(ChaincodeStub stub) {
        return new ShipmentContext(stub);
    }

    @Transaction()
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.Shipment.CREATE')"
    )
    public Shipment createShipment(ShipmentContext ctx, String purchaseOrderId, int units, String senderId, String receiverId) {

        if (!Utils.isIdentityNameValidFormat(senderId)) {
            throw new RuntimeException(Utils.invalidIdentityNameErrorBuilder("senderId"));
        } else if (!Utils.isIdentityNameValidFormat(receiverId)) {
            throw new RuntimeException(Utils.invalidIdentityNameErrorBuilder("receiverId"));
        }

        String[] collections = Utils.append(ctx.getCallerPrivateCollectionNames(), senderId, receiverId);
        PurchaseOrder po = ctx.getPurchaseOrderList().get(purchaseOrderId, collections);

        int count = ctx.getShipmentList().count();
        String shipmentId = "SHIP" + count;

        int existingUnits = 0;
        ShipmentGroup sg = null;

        if (!ctx.getShipmentGroupList().exists(purchaseOrderId)) {
            sg = new ShipmentGroup(purchaseOrderId, new String[] {});
        } else {
            sg = ctx.getShipmentGroupList().get(purchaseOrderId, collections);
        }

        for (String existingShipmentId : sg.getShipmentIds()) {
            Shipment shipment = ctx.getShipmentList().get(existingShipmentId, collections);
            existingUnits += shipment.getUnits();
        }

        if (units + existingUnits > po.getOrderLineItem().getRequestedQuantity()) {
            throw new ContractRuntimeException("Shipping more units than are in the PurchaseOrder. Adding " + units + " to " + existingUnits);
        }

        final Shipment shipment = new Shipment(shipmentId, purchaseOrderId, units, senderId, receiverId, ShipmentStatus.IN_TRANSIT);
        ctx.getShipmentList().add(shipment, collections);

        sg.addShipmentId(shipmentId);
        ctx.getShipmentGroupList().update(sg, collections, true);

        return shipment;
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.Shipment.READ')"
    )
    public Shipment getShipment(ShipmentContext ctx, String shipmentId) {
        String[] collections = ctx.getCallerPrivateCollectionNames();
        return ctx.getShipmentList().get(shipmentId, collections);
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.Shipment.READ')"
    )
    public Shipment getShipmentByHash(ShipmentContext ctx, String hash) {
        String[] collections = ctx.getCallerPrivateCollectionNames();
        return ctx.getShipmentList().getByHash(hash, collections);
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.Shipment.READ')"
    )
    public Shipment[] getShipments(ShipmentContext ctx, String interestedPartyId) {
        String[] collections = Utils.getOrgNameListFromIdentities(interestedPartyId);
        JSONObject senderQuery= new JSONObject("{\"selector\": {\"senderId\": \"" + interestedPartyId + "\"}}");
        JSONObject receiverQuery = new JSONObject("{\"selector\": {\"receiverId\": \"" + interestedPartyId + "\"}}");

        ArrayList<Shipment> senderMatches = ctx.getShipmentList().query(senderQuery, collections);
        ArrayList<Shipment> receiverMatches = ctx.getShipmentList().query(receiverQuery, collections);

        // TODO: Decide how to order/paginate these results
        senderMatches.addAll(receiverMatches);

        return senderMatches.toArray(new Shipment[senderMatches.size()]);
    }

    @Transaction()
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.Shipment.UPDATE')"
    )
    public void deliveredShipment(ShipmentContext ctx, String shipmentId) {
        String[] collections = ctx.getCallerPrivateCollectionNames();
        Shipment shipment = ctx.getShipmentList().get(shipmentId, collections);

        collections = Utils.append(collections, shipment.getReceiverId(), shipment.getSenderId());

        shipment.setStatus(ShipmentStatus.DELIVERED);

        ctx.getShipmentList().update(shipment, collections);
    }
}
