package org.tradefinance.contracts.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.tradefinance.assets.FinanceRequest;
import org.tradefinance.assets.FinanceRequestGroup;
import org.tradefinance.assets.PurchaseOrder;
import org.tradefinance.assets.PurchaseOrderResponse;
import org.tradefinance.assets.defs.Party;
import org.tradefinance.assets.enums.FinanceRequestStatus;
import org.tradefinance.assets.enums.ResponseStatusCode;
import org.tradefinance.ledger_api.lists.AssetList;
import org.tradefinance.contracts.finance.FinanceRequestContract;
import org.tradefinance.contracts.finance.utils.FinanceRequestContext;
import org.tradefinance.contracts.utils.Utils;

import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.Chaincode.Response.Status;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class FinanceRequestContractTest {

    FinanceRequestContract frc;
    FinanceRequestContext ctx;
    Response response;
    ChaincodeStub stub;
    AssetList<FinanceRequestGroup> frgl;
    AssetList<FinanceRequest> frl;
    AssetList<PurchaseOrder> pol;
    AssetList<PurchaseOrderResponse> porl;

    Party requester;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void beforeEach() {
        frc = spy(new FinanceRequestContract());
        response = mock(Response.class);
        stub = mock(ChaincodeStub.class);
        pol = mock(AssetList.class);
        porl = mock(AssetList.class);
        frl = mock(AssetList.class);
        frgl = mock(AssetList.class);
        ctx = mock(FinanceRequestContext.class);

        requester = mock(Party.class);

        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse("2019-09-30");
        } catch (ParseException e) {
            // will never hit
        }
        Instant inst = date.toInstant();

        when(requester.getAdditionalPartyIdentification()).thenReturn("seller@bankB");
        when(requester.getGln()).thenReturn(1234567890L);
        when(stub.getTxTimestamp()).thenReturn(inst);
        when(stub.invokeChaincode(any(), any(), any())).thenReturn(response);
        when(ctx.getStub()).thenReturn(stub);
        when(ctx.getFinanceRequestList()).thenReturn(frl);
        when(ctx.getFinanceRequestGroupList()).thenReturn((frgl));
        when(ctx.getCallerPrivateCollectionNames()).thenReturn(new String[] {"bankB"});
        when(ctx.getCallerOrg()).thenReturn("bankB");
    }

    @Nested
    class ContractFunctions {

        @Nested
        class CreateFinanceRequest {
            Party buyer;
            Party seller;
            PurchaseOrder po;
            PurchaseOrderResponse por;

            @BeforeEach
            public void beforeEach() {
                buyer = new Party(987654321L);
                seller = new Party(requester.getGln());
                po = new PurchaseOrder("some id", 123456789L, buyer, seller, 100, 10, 901837573L);
                por = new PurchaseOrderResponse("some id", 1234556789L, ResponseStatusCode.ACCEPTED, buyer, "PO0");

                when(response.getStatus()).thenReturn(Status.SUCCESS);
                when(response.getStringPayload()).thenReturn(po.serialize("", true)).thenReturn(por.serialize("", true));
            }

            @Test
            public void shouldErrorWhenMonthLengthTooShort() {
                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.createFinanceRequest(ctx, requester, new String[] {"bankA", "bankB"}, "PO0", 100.00, 1.05, 0));

                assertEquals(re.getMessage(), "Finance request must have minimum month length of 1");
            }

            @Test
            public void shouldErrorWhenRequesterHasBadAdditional() {
                when(requester.getAdditionalPartyIdentification()).thenReturn("bankB");

                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.createFinanceRequest(ctx, requester, new String[] {"bankA", "bankB"}, "PO0", 100.00, 1.05, 1));

                assertEquals(re.getMessage(), "Invalid requester additionalPartyInformation. Should be of format <NAME>@<EP_ID>");
            }

            @Test
            public void shouldErrorWhenGetPurchaseOrderErrors() {
                when(response.getStatus()).thenReturn(Status.INTERNAL_SERVER_ERROR);

                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.createFinanceRequest(ctx, requester, new String[] {"bankA", "bankB"}, "PO0", 100.00, 1.05, 1));

                verify(stub).invokeChaincode(eq("purchasecontract"), argThat((List<byte[]> list) -> {
                    return list.size() == 2 &&
                           new String(list.get(0)).equals("PurchaseOrderContract:getPurchaseOrder") &&
                           new String(list.get(1)).equals("PO0");
                }), eq("tradenetpurchase"));
                assertEquals(re.getMessage(), "Failed to read purchase order");
            }

            @Test
            public void shouldErrorWhenDoNotHaveAccessToFullPurchaseOrder() {
                PurchaseOrder po = new PurchaseOrder("some id", "some hash");

                when(response.getStringPayload()).thenReturn(po.serialize());

                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.createFinanceRequest(ctx, requester, new String[] {"bankA", "bankB"}, "PO0", 100.00, 1.05, 1));

                assertEquals(re.getMessage(), "Partial purchase order retrieved. Cannot create the finance request.");
            }

            @Test
            public void shouldErrorWhenSellerIsNotTheRequester() {
                long gln = requester.getGln();
                when(requester.getGln()).thenReturn(gln + 1);

                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.createFinanceRequest(ctx, requester, new String[] {"bankA", "bankB"}, "PO0", 100.00, 1.05, 1));

                assertEquals(re.getMessage(), "Unable to create a finance request for PO0 as the seller does not match");
            }

            @Test
            public void shouldErrorWhenGetPurchaseOrderResponseErrors() {
                when(response.getStatus()).thenReturn(Status.SUCCESS).thenReturn(Status.INTERNAL_SERVER_ERROR);

                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.createFinanceRequest(ctx, requester, new String[] {"bankA", "bankB"}, "PO0", 100.00, 1.05, 1));

                verify(stub).invokeChaincode(eq("purchasecontract"), argThat((List<byte[]> list) -> {
                    return list.size() == 2 &&
                           new String(list.get(0)).equals("PurchaseOrderContract:getPurchaseOrderResponse") &&
                           new String(list.get(1)).equals("PO0");
                }), eq("tradenetpurchase"));

                assertEquals(re.getMessage(), "Unable to create a finance request for PO0. Purchase order not accepted");
            }

            @Test
            public void shouldErrorWhenPurchaseOrderResponseNotAccepted() {
                PurchaseOrderResponse por = new PurchaseOrderResponse("some id", 1234556789L, ResponseStatusCode.REJECTED, buyer, "PO0");

                when(response.getStringPayload()).thenReturn(po.serialize("", true)).thenReturn(por.serialize("", true));

                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.createFinanceRequest(ctx, requester, new String[] {"bankA", "bankB"}, "PO0", 100.00, 1.05, 1));

                assertEquals(re.getMessage(), "Unable to create a finance request for PO0. Purchase order not accepted");
            }

            @Test
            public void shouldCreateFinanceRequestsForEachFinancierAndGroupThem() {
                when(frl.count()).thenReturn(12);
                when(frgl.count()).thenReturn(7);

                FinanceRequestGroup frg = frc.createFinanceRequest(ctx, requester, new String[] {"bankA", "bankC"}, "PO0", 100.00, 1.05, 1);

                assertEquals("5ad5e720fd216743646f3df026cda8ce64f3f188e6e471c71c9cbbbdcd302d58", frg.getHash());

                verify(frgl).add(argThat((FinanceRequestGroup writtenFrg) -> {
                    return writtenFrg.getId().equals("REQ_GRP7") &&
                           Arrays.equals(writtenFrg.getRequestIds(), new String[] {"REQ12", "REQ13"});
                }), eq(new String[]{"bankB"}));

                verify(frl).add(argThat((FinanceRequest writtenFr) -> {
                    return writtenFr.getId().equals("REQ12") &&
                           writtenFr.getFinancierId().equals("bankA") &&
                           writtenFr.getRequestGroup().equals("REQ_GRP7") &&
                           writtenFr.getPurchaseOrderId().equals("PO0") &&
                           writtenFr.getAmount() == 100.00 &&
                           writtenFr.getInterest() == 1.05 &&
                           writtenFr.getStatus() == FinanceRequestStatus.PENDING &&
                           Long.compare(writtenFr.getCompletionDate().getTime(), 1572393600000L) == 0;
                }), eq(new String[]{"bankB", "bankA"}));

                verify(frl).add(argThat((FinanceRequest writtenFr) -> {

                    return writtenFr.getId().equals("REQ13") &&
                           writtenFr.getFinancierId().equals("bankC") &&
                           writtenFr.getRequestGroup().equals("REQ_GRP7") &&
                           writtenFr.getPurchaseOrderId().equals("PO0") &&
                           writtenFr.getAmount() == 100.00 &&
                           writtenFr.getInterest() == 1.05 &&
                           writtenFr.getStatus() == FinanceRequestStatus.PENDING &&
                           Long.compare(writtenFr.getCompletionDate().getTime(), 1572393600000L) == 0;
                }), eq(new String[]{"bankB", "bankC"}));
            }
        }

        @Nested
        class GetFinanceRequest {

            @Test
            public void shouldGetFinanceRequest() {
                FinanceRequest fr = new FinanceRequest("some id", "some hash");

                when(frl.get("REQ0", ctx.getCallerPrivateCollectionNames())).thenReturn(fr);

                assertEquals(frc.getFinanceRequest(ctx, "REQ0"), fr);
            }

            @Test
            public void shouldGetFinanceRequestByHash() {
                FinanceRequest fr = new FinanceRequest("some id", "some hash");

                when(frl.getByHash("po hash", ctx.getCallerPrivateCollectionNames())).thenReturn(fr);

                assertEquals(frc.getFinanceRequestByHash(ctx, "po hash"), fr);
            }

            @Test
            public void shouldGetFinancierRequestsForFinancier() {
                FinanceRequest fr = new FinanceRequest("some id", "some hash");

                ArrayList<FinanceRequest> query1 = new ArrayList<FinanceRequest>();
                query1.add(fr);

                when(frl.query(any(JSONObject.class), any(String[].class))).thenReturn(query1);

                FinanceRequest[] frs = frc.getFinanceRequestsForFinancier(ctx);

                assertEquals(frs[0], fr);

                verify(frl).query(argThat((JSONObject json) -> {
                    return json.toString().equals("{\"selector\":{\"financierId\":\"bankB\"}}");
                }), argThat((String[] collections) -> {
                    return Arrays.equals(collections, ctx.getCallerPrivateCollectionNames());
                }));
            }

            @Test
            public void shouldGetFinancierRequestsForRequester() {
                FinanceRequest fr = new FinanceRequest("some id", "some hash");

                ArrayList<FinanceRequest> query1 = new ArrayList<FinanceRequest>();
                query1.add(fr);

                when(frl.query(any(JSONObject.class), any(String[].class))).thenReturn(query1);

                FinanceRequest[] frs = frc.getFinanceRequestsForRequester(ctx, requester.getAdditionalPartyIdentification());

                assertEquals(frs[0], fr);

                verify(frl).query(argThat((JSONObject json) -> {
                    return json.toString().equals("{\"selector\":{\"requesterId\":\"" + requester.getAdditionalPartyIdentification() + "\"}}");
                }), argThat((String[] collections) -> {
                    return Arrays.equals(collections, ctx.getCallerPrivateCollectionNames());
                }));
            }

            @Test
            public void shouldGetFinancierRequestsByGroupHash() {
                FinanceRequestGroup frg = new FinanceRequestGroup("group id", "group hash");
                FinanceRequest fr = new FinanceRequest("some id", "some hash");

                ArrayList<FinanceRequest> query1 = new ArrayList<FinanceRequest>();
                query1.add(fr);

                when(frgl.getByHash(anyString(), any(String[].class))).thenReturn(frg);
                when(frl.query(any(JSONObject.class), any(String[].class))).thenReturn(query1);

                FinanceRequest[] frs = frc.getFinanceRequestsByGroupHash(ctx, "group hash");

                assertEquals(frs[0], fr);

                verify(frgl).getByHash(eq("group hash"), argThat((String[] collections) -> {
                    return Arrays.equals(collections, ctx.getCallerPrivateCollectionNames());
                }));

                verify(frl).query(argThat((JSONObject json) -> {
                    return json.toString().equals("{\"selector\":{\"requestGroup\":\"group id\"}}");
                }), argThat((String[] collections) -> {
                    return Arrays.equals(collections, ctx.getCallerPrivateCollectionNames());
                }));
            }
        }

        @Nested
        class ApproveFinanceRequest {

            FinanceRequest fr;

            @BeforeEach
            public void beforeEach() {
                fr = mock(FinanceRequest.class);

                when(frl.get(anyString(), any(String[].class))).thenReturn(fr);
                when(fr.getRequesterId()).thenReturn("seller@bankB");
            }

            @Test
            public void shouldErrorWhenNotPending() {
                when(fr.getStatus()).thenReturn(FinanceRequestStatus.APPROVED);

                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.approveFinanceRequest(ctx, "REQ0"));

                assertEquals(re.getMessage(), "Can only accept a finance request which is pending");
            }

            @Test
            public void shouldSetStatusToApproved() {
                when(fr.getStatus()).thenReturn(FinanceRequestStatus.PENDING);

                frc.approveFinanceRequest(ctx, "REQ0");

                verify(frl).get(eq("REQ0"), argThat((String[] collections) -> {
                    return Arrays.equals(collections, ctx.getCallerPrivateCollectionNames());
                }));

                verify(fr).setStatus(FinanceRequestStatus.APPROVED);

                verify(frl).update(eq(fr), argThat((String[] collections) -> {
                    return Arrays.equals(collections, Utils.append(ctx.getCallerPrivateCollectionNames(), "bankB"));
                }));
            }
        }

        @Nested
        class RejectFinanceRequest {

            FinanceRequest fr;

            @BeforeEach
            public void beforeEach() {
                fr = mock(FinanceRequest.class);

                when(frl.get(anyString(), any(String[].class))).thenReturn(fr);
                when(fr.getRequesterId()).thenReturn("seller@bankB");
            }

            @Test
            public void shouldErrorWhenNotPending() {
                when(fr.getStatus()).thenReturn(FinanceRequestStatus.APPROVED);

                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.rejectFinanceRequest(ctx, "REQ0"));

                assertEquals(re.getMessage(), "Can only reject a finance request which is pending");
            }

            @Test
            public void shouldSetStatusToRejected() {
                when(fr.getStatus()).thenReturn(FinanceRequestStatus.PENDING);

                frc.rejectFinanceRequest(ctx, "REQ0");

                verify(frl).get(eq("REQ0"), argThat((String[] collections) -> {
                    return Arrays.equals(collections, ctx.getCallerPrivateCollectionNames());
                }));

                verify(fr).setStatus(FinanceRequestStatus.REJECTED);

                verify(frl).update(eq(fr), argThat((String[] collections) -> {
                    return Arrays.equals(collections, Utils.append(ctx.getCallerPrivateCollectionNames(), "bankB"));
                }));
            }
        }

        @Nested
        class AcceptFinanceRequest {

            FinanceRequest acceptedFr;

            @BeforeEach
            public void beforeEach() {
                acceptedFr = mock(FinanceRequest.class);

                when(frl.get(anyString(), any(String[].class))).thenReturn(acceptedFr);
                when(acceptedFr.getFinancierId()).thenReturn("bankC");
            }

            @Test
            public void shouldErrorWhenNotApproved() {
                when(acceptedFr.getStatus()).thenReturn(FinanceRequestStatus.PENDING);

                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.acceptFinanceRequest(ctx, "REQ0"));

                assertEquals(re.getMessage(), "Cannot accept finance requests which are not approved");
            }

            @Test
            public void shouldAcceptFinanceRequestAndWithdrawOthers() {
                FinanceRequestGroup frg = mock(FinanceRequestGroup.class);
                when(frg.getRequestIds()).thenReturn(new String[] {"REQ0", "REQ1", "REQ2", "REQ3"});

                when(frgl.get("REQ_GRP1", ctx.getCallerPrivateCollectionNames())).thenReturn(frg);

                FinanceRequest approvedFr = mock(FinanceRequest.class);
                FinanceRequest pendingFr = mock(FinanceRequest.class);
                FinanceRequest rejectedFr = mock(FinanceRequest.class);

                when(acceptedFr.getRequestGroup()).thenReturn("REQ_GRP1");
                when(acceptedFr.getFinancierId()).thenReturn("bankA");
                when(acceptedFr.getStatus()).thenReturn(FinanceRequestStatus.APPROVED);

                when(approvedFr.getStatus()).thenReturn(FinanceRequestStatus.APPROVED);
                when(approvedFr.getFinancierId()).thenReturn("bankC");

                when(pendingFr.getStatus()).thenReturn(FinanceRequestStatus.PENDING);
                when(pendingFr.getFinancierId()).thenReturn("bankD");

                when(rejectedFr.getStatus()).thenReturn(FinanceRequestStatus.REJECTED);
                when(rejectedFr.getFinancierId()).thenReturn("bankE");

                when(frl.get("REQ0", ctx.getCallerPrivateCollectionNames())).thenReturn(acceptedFr);
                when(frl.get("REQ1", ctx.getCallerPrivateCollectionNames())).thenReturn(approvedFr);
                when(frl.get("REQ2", ctx.getCallerPrivateCollectionNames())).thenReturn(pendingFr);
                when(frl.get("REQ3", ctx.getCallerPrivateCollectionNames())).thenReturn(rejectedFr);

                frc.acceptFinanceRequest(ctx, "REQ0");

                verify(approvedFr).setStatus(FinanceRequestStatus.WITHDRAWN);
                verify(pendingFr).setStatus(FinanceRequestStatus.WITHDRAWN);
                verify(rejectedFr, never()).setStatus(FinanceRequestStatus.WITHDRAWN);
                verify(acceptedFr).setStatus(FinanceRequestStatus.ACCEPTED);

                verify(frl).update(eq(acceptedFr), argThat((String[] collections) -> {
                    return Arrays.equals(collections, Utils.append(ctx.getCallerPrivateCollectionNames(), "bankA"));
                }));

                verify(frl).update(eq(approvedFr), argThat((String[] collections) -> {
                    return Arrays.equals(collections, Utils.append(ctx.getCallerPrivateCollectionNames(), "bankC"));
                }));

                verify(frl).update(eq(pendingFr), argThat((String[] collections) -> {
                    return Arrays.equals(collections, Utils.append(ctx.getCallerPrivateCollectionNames(), "bankD"));
                }));

                verify(frl, never()).update(eq(rejectedFr), argThat((String[] collections) -> {
                    return Arrays.equals(collections, Utils.append(ctx.getCallerPrivateCollectionNames(), "bankE"));
                }));
            }
        }

        @Nested
        class WithdrawFinanceRequest {

            FinanceRequest fr;

            @BeforeEach
            public void beforeEach() {
                fr = mock(FinanceRequest.class);

                when(frl.get(anyString(), any(String[].class))).thenReturn(fr);
                when(fr.getFinancierId()).thenReturn("bankC");
            }

            @Test
            public void shouldErrorWhenAccepted() {
                when(fr.getStatus()).thenReturn(FinanceRequestStatus.ACCEPTED);

                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.withdrawFinanceRequest(ctx, "REQ0"));

                assertEquals(re.getMessage(), "Cannot withdraw finance requests that are accepted or rejected");
            }

            @Test
            public void shouldErrorWhenRejected() {
                when(fr.getStatus()).thenReturn(FinanceRequestStatus.REJECTED);

                RuntimeException re = assertThrows(RuntimeException.class, () -> frc.withdrawFinanceRequest(ctx, "REQ0"));

                assertEquals(re.getMessage(), "Cannot withdraw finance requests that are accepted or rejected");
            }

            @Test
            public void shouldSetStatusToApproved() {
                when(fr.getStatus()).thenReturn(FinanceRequestStatus.PENDING);

                frc.withdrawFinanceRequest(ctx, "REQ0");

                verify(frl).get(eq("REQ0"), argThat((String[] collections) -> {
                    return Arrays.equals(collections, ctx.getCallerPrivateCollectionNames());
                }));

                verify(fr).setStatus(FinanceRequestStatus.WITHDRAWN);

                verify(frl).update(eq(fr), argThat((String[] collections) -> {
                    return Arrays.equals(collections, Utils.append(ctx.getCallerPrivateCollectionNames(), "bankC"));
                }));
            }
        }
    }
}
