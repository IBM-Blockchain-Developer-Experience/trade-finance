package org.tradefinance.contracts.purchase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.tradefinance.assets.PurchaseOrder;
import org.tradefinance.assets.PurchaseOrderResponse;
import org.tradefinance.assets.defs.OrderIdentification;
import org.tradefinance.assets.defs.Party;
import org.tradefinance.assets.enums.ResponseStatusCode;
import org.tradefinance.contracts.purchase.utils.PurchaseOrderContext;
import org.tradefinance.ledger_api.lists.AssetList;

import org.hyperledger.fabric.shim.ChaincodeStub;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PurchaseOrderContractTest {
    PurchaseOrderContract poc;
    PurchaseOrderContext ctx;
    ChaincodeStub stub;
    AssetList<PurchaseOrder> pol;
    AssetList<PurchaseOrderResponse> porl;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void beforeEach() {
        if (stub != null) {
            reset(stub, pol, ctx);
        }

        poc = spy(new PurchaseOrderContract());
        stub = mock(ChaincodeStub.class);
        pol = mock(AssetList.class);
        porl = mock(AssetList.class);
        ctx = mock(PurchaseOrderContext.class);

        when(pol.count()).thenReturn(7);

        when(ctx.getStub()).thenReturn(stub);
        when(ctx.getPurchaseOrderList()).thenReturn(pol);
        when(ctx.getPurchaseOrderResponseList()).thenReturn(porl);
        when(ctx.getCallerPrivateCollectionNames()).thenReturn(new String[]{"bankA"});
        when(ctx.getCallerGln()).thenReturn(1239874560L);
        when(ctx.getCallerOrg()).thenReturn("bankA");
    }

    @Nested
    class ContractFunctions {

        @Nested
        class CreatePurchaseOrder {

            public Party buyer = new Party(1234567890, "buyer@bankA");
            public Party seller = new Party(9876543210L, "seller@bankB");
            public final static double price = 100.00;
            public final static int units = 500;
            public final static long gtin = 1928374650;

            @Test
            public void shouldErrorWhenBadBuyerFormat() {
                Party badBuyer = new Party(1234567890, "buyer");

                RuntimeException re = assertThrows(RuntimeException.class, () -> poc.createPurchaseOrder(ctx, badBuyer, seller, price, units, gtin));

                assertEquals(re.getMessage(), "Invalid buyer additionalPartyIdentification. Should be of format <NAME>@<EP_ID>");
            }

            @Test
            public void shouldErrorWhenBadSellerFormat() {
                Party badSeller = new Party(1234567890, "seller");

                RuntimeException re = assertThrows(RuntimeException.class, () -> poc.createPurchaseOrder(ctx, buyer, badSeller, price, units, gtin));

                assertEquals(re.getMessage(), "Invalid seller additionalPartyIdentification. Should be of format <NAME>@<EP_ID>");
            }

            @Test
            public void shouldAddANewPurchaseOrder() {
                String expectedHash = "bced56b41a8ac27b7ee12dbed0eebe82b8b4a9a347c8f730ee760da8291ec066";

                PurchaseOrder po = poc.createPurchaseOrder(ctx, buyer, seller, price, units, gtin);

                assertEquals(po.getId(), "PO7");
                assertEquals(expectedHash, po.getHash());

                doNothing().when(pol).add(any(PurchaseOrder.class), any(String[].class));

                verify(pol).add(argThat((PurchaseOrder writtenPo) -> {
                    return writtenPo.getHash().equals(expectedHash);
                }), eq(new String[]{"bankA", "bankB"}));
            }
        }

        @Nested
        class GetPurchaseOrder {

            @Test
            public void shouldGetPurchaseOrder() {
                PurchaseOrder po = new PurchaseOrder("some id", "some hash");

                when(pol.get("PO0", ctx.getCallerPrivateCollectionNames())).thenReturn(po);

                assertEquals(poc.getPurchaseOrder(ctx, "PO0"), po);
            }

            @Test
            public void shouldGetPurchaseOrderByHash() {
                PurchaseOrder po = new PurchaseOrder("some id", "some hash");

                when(pol.getByHash("po hash", ctx.getCallerPrivateCollectionNames())).thenReturn(po);

                assertEquals(poc.getPurchaseOrderByHash(ctx, "po hash"), po);
            }

            @Test
            public void shouldGetPurchaseOrders() {
                PurchaseOrder po1 = new PurchaseOrder("some id", "some hash");
                PurchaseOrder po2 = new PurchaseOrder("some other id", "some other hash");

                ArrayList<PurchaseOrder> query1 = new ArrayList<PurchaseOrder>();
                query1.add(po1);

                ArrayList<PurchaseOrder> query2 = new ArrayList<PurchaseOrder>();
                query1.add(po2);

                when(pol.query(any(JSONObject.class), any(String[].class))).thenReturn(query1).thenReturn(query2);

                PurchaseOrder[] pos = poc.getPurchaseOrders(ctx, 987654321L);

                assertEquals(pos[0], po1);
                assertEquals(pos[1], po2);

                verify(pol).query(argThat((JSONObject json) -> {
                    return json.toString().equals("{\"selector\":{\"buyer\":{\"gln\":987654321}}}");
                }), argThat((String[] collections) -> {
                    return Arrays.equals(collections, ctx.getCallerPrivateCollectionNames());
                }));

                verify(pol).query(argThat((JSONObject json) -> {
                    return json.toString().equals("{\"selector\":{\"seller\":{\"gln\":987654321}}}");
                }), argThat((String[] collections) -> {
                    return Arrays.equals(collections, ctx.getCallerPrivateCollectionNames());
                }));
            }
        }

        @Nested
        class GetPurchaseOrderResponse {

            PurchaseOrderResponse por1;
            ArrayList<PurchaseOrderResponse> query1;

            @BeforeEach
            public void BeforeEach() {
                por1 = new PurchaseOrderResponse("some id", "some hash");

                query1 = new ArrayList<PurchaseOrderResponse>();

                when(porl.query(any(JSONObject.class), any(String[].class))).thenReturn(query1);
            }

            @Test
            public void shouldErrorWhenTooFew() {
                RuntimeException re = assertThrows(RuntimeException.class, () -> poc.getPurchaseOrderResponse(ctx, "PO0"));

                assertEquals(re.getMessage(), "Error retrieving purchase order response. Incorrect number of responses");
            }

            @Test
            public void shouldErrorWhenTooMany() {
                query1.add(por1);
                query1.add(por1);

                RuntimeException re = assertThrows(RuntimeException.class, () -> poc.getPurchaseOrderResponse(ctx, "PO0"));

                assertEquals(re.getMessage(), "Error retrieving purchase order response. Incorrect number of responses");
            }

            @Test
            public void shouldGetPurchaseOrderResponse() {
                query1.add(por1);

                PurchaseOrderResponse res = poc.getPurchaseOrderResponse(ctx, "PO0");
                assertEquals(res, por1);

                verify(porl).query(argThat((JSONObject json) -> {
                    return json.toString().equals("{\"selector\":{\"originalOrder\":{\"entityIdentification\":\"PO0\"}}}");
                }), argThat((String[] collections) -> {
                    return Arrays.equals(collections, ctx.getCallerPrivateCollectionNames());
                }));
            }
        }

        @Nested
        class GetPurchaseOrderResponses {

            @Test
            public void shouldReturnPurchaseOrderResponses() {
                PurchaseOrder po1 = new PurchaseOrder("some id", "some hash");
                PurchaseOrder po2 = new PurchaseOrder("some other id", "some other hash");

                ArrayList<PurchaseOrder> query1 = new ArrayList<PurchaseOrder>();
                query1.add(po1);

                ArrayList<PurchaseOrder> query2 = new ArrayList<PurchaseOrder>();
                query1.add(po2);

                when(pol.query(any(JSONObject.class), any(String[].class))).thenReturn(query1).thenReturn(query2);

                PurchaseOrderResponse por1 = new PurchaseOrderResponse("some id", "some hash");

                ArrayList<PurchaseOrderResponse> query3 = new ArrayList<PurchaseOrderResponse>();
                query3.add(por1);

                when(porl.query(any(JSONObject.class), any(String[].class))).thenReturn(query3);

                PurchaseOrderResponse[] pors = poc.getPurchaseOrderResponses(ctx, 987654321L);

                assertEquals(pors.length, 1);
                assertEquals(pors[0], por1);

                verify(poc).getPurchaseOrders(ctx, 987654321L);
                verify(porl).query(argThat((JSONObject json) -> {
                    return json.toString().equals("{\"selector\":{\"originalOrder\":{\"entityIdentification\":{\"$in\":[\"some id\",\"some other id\"]}}}}");
                }), argThat((String[] collections) -> {
                    return Arrays.equals(collections, ctx.getCallerPrivateCollectionNames());
                }));
            }
        }

        @Nested
        class AcceptPurchaseOrder {

            Party buyer;
            Party seller;
            Party contentOwner;
            OrderIdentification oi;
            PurchaseOrder po;

            @BeforeEach
            public void beforeEach() {
                buyer = mock(Party.class);
                when(buyer.getAdditionalPartyIdentification()).thenReturn("buyer@bankA");
                when(buyer.serialize()).thenReturn("{\"hello\": \"world\"}");

                seller = mock(Party.class);
                when(seller.getAdditionalPartyIdentification()).thenReturn("seller@bankB");
                when(seller.serialize()).thenReturn("{\"hello\": \"world\"}");

                contentOwner = mock(Party.class);
                when(contentOwner.getGln()).thenReturn(1234567890l);

                oi = mock(OrderIdentification.class);
                when(oi.getContentOwner()).thenReturn(contentOwner);

                po = mock(PurchaseOrder.class);
                when(po.getBuyer()).thenReturn(buyer);
                when(po.getSeller()).thenReturn(seller);
                when(po.getOrderIdentification()).thenReturn(oi);

                when(pol.get(anyString(), any(String[].class))).thenReturn(po);
            }

            @Test
            public void shouldNotAcceptPurchaseOrderWhenGetFails() {
                when(pol.get(anyString(), any(String[].class))).thenThrow(new RuntimeException("no PurchaseOrder"));

                assertThrows(RuntimeException.class, () -> poc.acceptPurchaseOrder(ctx, "PO0"));

                verify(pol).get("PO0", ctx.getCallerPrivateCollectionNames());
                verify(porl, never()).add(any(), any());
            }

            @Test
            public void shouldNotAcceptPurchaseOrderWhenWrongBank() {
                RuntimeException re = assertThrows(RuntimeException.class, () -> poc.acceptPurchaseOrder(ctx, "PO0"));

                assertEquals(re.getMessage(), "Caller must be from same PO as seller");

                verify(pol).get("PO0", ctx.getCallerPrivateCollectionNames());
                verify(porl, never()).add(any(), any());
            }

            @Test void shouldAcceptPurchaseOrder() {
                when(ctx.getCallerPrivateCollectionNames()).thenReturn(new String[] {"bankB"});
                when(ctx.getCallerOrg()).thenReturn("bankB");

                poc.acceptPurchaseOrder(ctx, "PO0");

                verify(pol).get("PO0", ctx.getCallerPrivateCollectionNames());
                verify(porl).add(argThat((PurchaseOrderResponse por) -> {
                    return por.getBuyer() == buyer &&
                           por.getResponseStatusCode() == ResponseStatusCode.ACCEPTED &&
                           por.getOrderResponseIdentification().getEntityIdentification().equals("PO0") &&
                           por.getOrderResponseIdentification().getContentOwner().getGln() == 1234567890L;
                }), argThat((String[] collections) -> {
                    return Arrays.equals(collections, new String[] {"bankB", "bankA"});
                }));
            }
        }

        @Nested
        class ClosePurchaseOrder {

            Party buyer;
            Party seller;
            Party contentOwner;
            OrderIdentification oi;
            PurchaseOrder po;

            @BeforeEach
            public void beforeEach() {
                buyer = mock(Party.class);
                when(buyer.getAdditionalPartyIdentification()).thenReturn("buyer@bankA");
                when(buyer.serialize()).thenReturn("{\"hello\": \"world\"}");

                seller = mock(Party.class);
                when(seller.getAdditionalPartyIdentification()).thenReturn("seller@bankB");
                when(seller.serialize()).thenReturn("{\"hello\": \"world\"}");

                contentOwner = mock(Party.class);
                when(contentOwner.getGln()).thenReturn(1234567890l);

                oi = mock(OrderIdentification.class);
                when(oi.getContentOwner()).thenReturn(contentOwner);

                po = mock(PurchaseOrder.class);
                when(po.getBuyer()).thenReturn(buyer);
                when(po.getSeller()).thenReturn(seller);
                when(po.getOrderIdentification()).thenReturn(oi);

                when(pol.get(anyString(), any(String[].class))).thenReturn(po);
            }

            @Test
            public void shouldNotRejectPurchaseOrderWhenGetFails() {
                when(pol.get(anyString(), any(String[].class))).thenThrow(new RuntimeException("no PurchaseOrder"));

                assertThrows(RuntimeException.class, () -> poc.closePurchaseOrder(ctx, "PO0"));

                verify(pol).get("PO0", ctx.getCallerPrivateCollectionNames());
                verify(porl, never()).add(any(), any());
            }

            @Test
            public void shouldNotRejectPurchaseOrderWhenWrongBank() {
                RuntimeException re = assertThrows(RuntimeException.class, () -> poc.closePurchaseOrder(ctx, "PO0"));

                assertEquals(re.getMessage(), "Caller must be from same PO as seller");

                verify(pol).get("PO0", ctx.getCallerPrivateCollectionNames());
                verify(porl, never()).add(any(), any());
            }

            @Test void shouldRejectPurchaseOrder() {
                when(ctx.getCallerPrivateCollectionNames()).thenReturn(new String[] {"bankB"});
                when(ctx.getCallerOrg()).thenReturn("bankB");

                poc.closePurchaseOrder(ctx, "PO0");

                verify(pol).get("PO0", ctx.getCallerPrivateCollectionNames());
                verify(porl).add(argThat((PurchaseOrderResponse por) -> {
                    return por.getBuyer() == buyer &&
                           por.getResponseStatusCode() == ResponseStatusCode.REJECTED &&
                           por.getOrderResponseIdentification().getEntityIdentification().equals("PO0") &&
                           por.getOrderResponseIdentification().getContentOwner().getGln() == 1234567890L;
                }), argThat((String[] collections) -> {
                    return Arrays.equals(collections, new String[] {"bankB", "bankA"});
                }));
            }
        }

        @Nested
        class VerifyPurchaseOrder {

            PurchaseOrder po;
            Party buyer;
            Party seller;

            long gtin = 349348612312L;
            long contentOwnerGln = 1234567890L;

            @BeforeEach
            public void beforeEach() {
                buyer = new Party(987654321L);
                seller = new Party(912837465L);

                po = new PurchaseOrder("PO0", contentOwnerGln, buyer, seller, 100, 100, gtin);
            }

            @Test
            public void shouldReturnFalseWhenDifferent() {
                when(pol.get("PO0")).thenReturn(po);

                boolean verified = poc.verifyPurchaseOrder(ctx, "PO0", contentOwnerGln, buyer, seller, 130, 100, gtin);

                assertFalse(verified);
            }

            @Test
            public void shouldReturnTrueWhenSame() {
                when(pol.get("PO0")).thenReturn(po);

                boolean verified = poc.verifyPurchaseOrder(ctx, "PO0", contentOwnerGln, buyer, seller, 100, 100, gtin);

                assertTrue(verified);
            }
        }
    }
}
