package org.tradefinance.contracts.finance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.tradefinance.assets.FinanceRequest;
import org.tradefinance.assets.FinanceRequestGroup;
import org.tradefinance.assets.PurchaseOrder;
import org.tradefinance.assets.PurchaseOrderResponse;
import org.tradefinance.assets.defs.Party;
import org.tradefinance.assets.enums.FinanceRequestStatus;
import org.tradefinance.assets.enums.ResponseStatusCode;
import org.tradefinance.contracts.BaseContract;
import org.tradefinance.contracts.finance.utils.FinanceRequestContext;
import org.tradefinance.contracts.utils.Utils;
import org.tradefinance.contracts.utils.annotations.ACLRule;
import org.tradefinance.ledger_api.states.State;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.Chaincode.Response.Status;
import org.json.JSONObject;

@Contract(name = "FinanceRequestContract",
    info = @Info(title = "FinanceRequest contract",
                description = "A contract for handling finance requests and trades",
                version = "0.0.1"
    )
)
@Default
public class FinanceRequestContract extends BaseContract implements ContractInterface {
    protected Logger logger = Logger.getLogger(FinanceRequestContract.class.getName());

    public  FinanceRequestContract() {
        super(FinanceRequestContract.class);
    }

    @Override
    public FinanceRequestContext createContext(ChaincodeStub stub) {
        return new FinanceRequestContext(stub);
    }

    @Transaction()
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.FinanceRequest.CREATE')"
    )
    public FinanceRequestGroup createFinanceRequest(FinanceRequestContext ctx, Party requester, String[] financierIds, String purchaseOrderId, Double amount, Double interest, int monthLength) {
        if (monthLength < 1) {
            throw new RuntimeException("Finance request must have minimum month length of 1");
        } else if (!Utils.isIdentityNameValidFormat(requester.getAdditionalPartyIdentification())) {
            throw new RuntimeException(Utils.invalidIdentityNameErrorBuilder("requester additionalPartyInformation"));
        }

        String[] collections = ctx.getCallerPrivateCollectionNames();
        logger.log(Level.SEVERE, "[createFinanceRequest] Number of collections: " + collections.length);
        for (String col : collections) {
            logger.log(Level.SEVERE, "[createFinanceRequest] Collection" + col);
        }

        List<byte[]> getPurchaseOrderArgs = new ArrayList<byte[]>();
        getPurchaseOrderArgs.add("PurchaseOrderContract:getPurchaseOrder".getBytes());
        getPurchaseOrderArgs.add(purchaseOrderId.getBytes());

        Response purchaseOrderQuery = ctx.getStub().invokeChaincode("purchasecontract", getPurchaseOrderArgs, "tradenetpurchase");
        if (purchaseOrderQuery.getStatus() != Status.SUCCESS) {
            logger.log(Level.SEVERE, "QUERY ERROR: " + purchaseOrderQuery.getMessage());
            throw new RuntimeException("Failed to read purchase order");
        }

        PurchaseOrder purchaseOrder;

        try {
            purchaseOrder = State.deserialize(PurchaseOrder.class, purchaseOrderQuery.getStringPayload(), collections);
        } catch (Exception e) {
            throw new RuntimeException("Partial purchase order retrieved. Cannot create the finance request.");
        }

        if (!purchaseOrder.getSeller().getGln().equals(requester.getGln())) {
            throw new RuntimeException("Unable to create a finance request for " + purchaseOrderId + " as the seller does not match");
        }

        List<byte[]> getPurchaseOrderResponseArgs = new ArrayList<byte[]>();
        getPurchaseOrderResponseArgs.add("PurchaseOrderContract:getPurchaseOrderResponse".getBytes());
        getPurchaseOrderResponseArgs.add(purchaseOrderId.getBytes());

        Response purchaseOrderResponseQuery = ctx.getStub().invokeChaincode("purchasecontract", getPurchaseOrderResponseArgs, "tradenetpurchase");

        try {
            if (purchaseOrderResponseQuery.getStatus() != Status.SUCCESS) {
                logger.log(Level.SEVERE, "QUERY ERROR: " + purchaseOrderQuery.getMessage());
                throw new RuntimeException();
            }

            PurchaseOrderResponse por = State.deserialize(PurchaseOrderResponse.class, purchaseOrderResponseQuery.getStringPayload(), collections);

            if (por.getResponseStatusCode() != ResponseStatusCode.ACCEPTED) {
                throw new RuntimeException();
            }

        } catch (RuntimeException e) {
            throw new RuntimeException("Unable to create a finance request for " + purchaseOrderId + ". Purchase order not accepted");
        }

        String requesterId = requester.getAdditionalPartyIdentification();

        int requestCount = ctx.getFinanceRequestList().count();
        int groupCount = ctx.getFinanceRequestGroupList().count();

        Calendar calDate = Calendar.getInstance();
        calDate.setTimeInMillis(ctx.getStub().getTxTimestamp().toEpochMilli());
        calDate.add(Calendar.MONTH, monthLength);

        Date completionDate = new Date(calDate.getTimeInMillis());

        String requestGroupId = "REQ_GRP" + groupCount;

        String[] requestIds = new String[financierIds.length];

        for (int i = 0; i < financierIds.length; i++) {
            final String requestId = "REQ" + (requestCount + i);

            final FinanceRequest fr = new FinanceRequest(requestId, requesterId, financierIds[i], purchaseOrderId, amount, interest, completionDate, requestGroupId, FinanceRequestStatus.PENDING);
            ctx.getFinanceRequestList().add(fr, new String[]{Utils.getIdentityOrg(requesterId), financierIds[i]});

            requestIds[i] = requestId;
        }

        final FinanceRequestGroup frg = new FinanceRequestGroup(requestGroupId, requestIds);
        ctx.getFinanceRequestGroupList().add(frg, new String[]{Utils.getIdentityOrg(requesterId)});

        return frg.toPublicForm();
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.FinanceRequest.READ')"
    )
    public FinanceRequest getFinanceRequest(FinanceRequestContext ctx, String financeRequestId) {
        String[] collections = ctx.getCallerPrivateCollectionNames();
        return ctx.getFinanceRequestList().get(financeRequestId, collections);
    }

    @Transaction( submit = false)
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.FinanceRequest.READ')"
    )
    public FinanceRequest getFinanceRequestByHash(FinanceRequestContext ctx, String hash) {
        String[] collections = ctx.getCallerPrivateCollectionNames();
        return ctx.getFinanceRequestList().getByHash(hash, collections);
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.FinanceRequest.READ')"
    )
    public FinanceRequest[] getFinanceRequestsForFinancier(FinanceRequestContext ctx) {
        JSONObject financierQuery = new JSONObject("{\"selector\": {\"financierId\": \"" + ctx.getCallerOrg() + "\"}}");

        String[] collections = ctx.getCallerPrivateCollectionNames();
        ArrayList<FinanceRequest> financierMatches = ctx.getFinanceRequestList().query(financierQuery, collections);

        return financierMatches.toArray(new FinanceRequest[financierMatches.size()]);
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.FinanceRequest.READ')"
    )
    public FinanceRequest[] getFinanceRequestsForRequester(FinanceRequestContext ctx, String behalfOfId) {
        JSONObject requesterQuery = new JSONObject("{\"selector\": {\"requesterId\": \"" + behalfOfId + "\"}}");

        String[] collections = ctx.getCallerPrivateCollectionNames();
        ArrayList<FinanceRequest> requesterMatches = ctx.getFinanceRequestList().query(requesterQuery, collections);

        return requesterMatches.toArray(new FinanceRequest[requesterMatches.size()]);
    }

    @Transaction( submit = false )
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.FinanceRequest.READ')"
    )
    public FinanceRequest[] getFinanceRequestsByGroupHash(FinanceRequestContext ctx, String hash) {
        String[] callerCollections = ctx.getCallerPrivateCollectionNames();
        FinanceRequestGroup financeRequestGroup = ctx.getFinanceRequestGroupList().getByHash(hash, callerCollections);

        JSONObject query = new JSONObject("{\"selector\": {\"requestGroup\": \"" + financeRequestGroup.getId() + "\"}}");
        ArrayList<FinanceRequest> financeRequests = ctx.getFinanceRequestList().query(query, callerCollections);

        return financeRequests.toArray(new FinanceRequest[financeRequests.size()]);
    }

    @Transaction()
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.FinanceRequest.UPDATE')"
    )
    public void approveFinanceRequest(FinanceRequestContext ctx, String financeRequestId) {
        String[] collections = ctx.getCallerPrivateCollectionNames();
        FinanceRequest fr = ctx.getFinanceRequestList().get(financeRequestId, collections);

        if (fr == null) {
            throw new RuntimeException("Finance request ("+financeRequestId+") does not exist");
        }

        if (!fr.getStatus().equals(FinanceRequestStatus.PENDING)) {
            throw new RuntimeException("Can only accept a finance request which is pending");
        }

        fr.setStatus(FinanceRequestStatus.APPROVED);
        collections = Utils.append(collections, Utils.getIdentityOrg(fr.getRequesterId()));

        ctx.getFinanceRequestList().update(fr, collections);
    }

    @Transaction()
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.FinanceRequest.UPDATE')"
    )
    public void rejectFinanceRequest(FinanceRequestContext ctx, String financeRequestId) {
        String[] collections = ctx.getCallerPrivateCollectionNames();
        FinanceRequest fr = ctx.getFinanceRequestList().get(financeRequestId, collections);

        if (!fr.getStatus().equals(FinanceRequestStatus.PENDING)) {
            throw new RuntimeException("Can only reject a finance request which is pending");
        }

        fr.setStatus(FinanceRequestStatus.REJECTED);
        collections = Utils.append(collections, Utils.getIdentityOrg(fr.getRequesterId()));

        ctx.getFinanceRequestList().update(fr, collections);
    }

    @Transaction()
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.FinanceRequest.UPDATE')"
    )
    public void acceptFinanceRequest(FinanceRequestContext ctx, String financeRequestId) {
        String[] callerCollections = ctx.getCallerPrivateCollectionNames();
        FinanceRequest toAccept = ctx.getFinanceRequestList().get(financeRequestId, callerCollections);

        if (!toAccept.getStatus().equals(FinanceRequestStatus.APPROVED)) {
            throw new RuntimeException("Cannot accept finance requests which are not approved");
        }

        toAccept.setStatus(FinanceRequestStatus.ACCEPTED);
        String[] interestedCollections = Utils.append(callerCollections, toAccept.getFinancierId()); // financierId matches private collection name

        ctx.getFinanceRequestList().update(toAccept, interestedCollections);

        FinanceRequestGroup frg = ctx.getFinanceRequestGroupList().get(toAccept.getRequestGroup(), callerCollections);

        for (String requestId : frg.getRequestIds()) {
            if (!requestId.equals(financeRequestId)) {
                FinanceRequest fr = ctx.getFinanceRequestList().get(requestId, callerCollections);
                FinanceRequestStatus frStatus = fr.getStatus();

                if (frStatus.equals(FinanceRequestStatus.PENDING) || frStatus.equals(FinanceRequestStatus.APPROVED)) {
                    String[] withdrawnCollections = Utils.append(callerCollections, fr.getFinancierId()); // financierId matches private collection name
                    fr.setStatus(FinanceRequestStatus.WITHDRAWN);

                    ctx.getFinanceRequestList().update(fr, withdrawnCollections);
                }
            }
        }
    }

    @Transaction()
    @ACLRule(
        requiredRoles = "AnyOf('tradenet.FinanceRequest.UPDATE')"
    )
    public void withdrawFinanceRequest(FinanceRequestContext ctx, String financeRequestId) {
        String[] callerCollections = ctx.getCallerPrivateCollectionNames();
        FinanceRequest fr = ctx.getFinanceRequestList().get(financeRequestId, callerCollections);

        if (fr.getStatus().equals(FinanceRequestStatus.ACCEPTED) || fr.getStatus().equals(FinanceRequestStatus.REJECTED)) {
            throw new RuntimeException("Cannot withdraw finance requests that are accepted or rejected");
        }

        fr.setStatus(FinanceRequestStatus.WITHDRAWN);
        String[] interestedCollections = Utils.append(callerCollections, fr.getFinancierId()); // financierId matches private collection name

        ctx.getFinanceRequestList().update(fr, interestedCollections);
    }
}
