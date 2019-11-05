package org.tradefinance.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.tradefinance.assets.defs.Identification;
import org.tradefinance.assets.defs.OrderIdentification;
import org.tradefinance.assets.defs.Party;
import org.tradefinance.assets.enums.ResponseStatusCode;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PurchaseOrderResponseTest {

    public final static String hash = "some hash";
    public final static String id = "some id";
    public final static Party buyer = new Party(1111111111);
    public final static long contentOwnerGln = 1234567890L;
    public final static OrderIdentification oi = new OrderIdentification(id, contentOwnerGln);
    public final static Identification idf = new Identification("PO0");

    @Nested
    class Constructors {

        @Test
        public void shouldCreateFullObject() {
            PurchaseOrderResponse por = new PurchaseOrderResponse(id, contentOwnerGln, ResponseStatusCode.ACCEPTED, buyer, "PO0");

            assertEquals(por.getOrderResponseIdentification().getContentOwner().getGln(), 1234567890);
            assertEquals(por.getOrderResponseIdentification().getEntityIdentification(), id);
            assertEquals(por.getResponseStatusCode(), ResponseStatusCode.ACCEPTED);
            assertEquals(por.getBuyer(), buyer);
            assertEquals(por.getOriginalOrder().getEntityIdentification(), "PO0");
        }

        @Test
        public void shouldCreateFullObjectFromAllDetails() {
            PurchaseOrderResponse por = new PurchaseOrderResponse(oi, ResponseStatusCode.REJECTED, buyer, idf);

            assertEquals(por.getOrderResponseIdentification(), oi);
            assertEquals(por.getResponseStatusCode(), ResponseStatusCode.REJECTED);
            assertEquals(por.getBuyer(), buyer);
            assertEquals(por.getOriginalOrder(), idf);
        }

        @Test void shouldCreatePartialObject() {
            PurchaseOrderResponse por = new PurchaseOrderResponse(id, hash);

            assertEquals(por.getId(), id);
            assertEquals(por.getHash(), hash);

            assertNull(por.getOrderResponseIdentification());
            assertNull(por.getResponseStatusCode());
            assertNull(por.getBuyer());
            assertNull(por.getOriginalOrder());
        }
    }

    @Nested
    class Getters {

        @Test
        public void shouldGetOrderResponseIdentification() {
            try {
                PurchaseOrderResponse po = new PurchaseOrderResponse(id, hash);

                Field field = PurchaseOrderResponse.class.getDeclaredField("orderResponseIdentification");
                field.setAccessible(true);
                field.set(po, oi);

                assertEquals(po.getOrderResponseIdentification(), oi);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetResponseCode() {
            try {
                PurchaseOrderResponse po = new PurchaseOrderResponse(id, hash);

                Field field = PurchaseOrderResponse.class.getDeclaredField("responseStatusCode");
                field.setAccessible(true);
                field.set(po, ResponseStatusCode.ACCEPTED);

                assertEquals(po.getResponseStatusCode(), ResponseStatusCode.ACCEPTED);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetBuyer() {
            try {
                PurchaseOrderResponse po = new PurchaseOrderResponse(id, hash);

                Field field = PurchaseOrderResponse.class.getDeclaredField("buyer");
                field.setAccessible(true);
                field.set(po, buyer);

                assertEquals(po.getBuyer(), buyer);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetOriginalOrder() {
            try {
                PurchaseOrderResponse po = new PurchaseOrderResponse(id, hash);

                Field field = PurchaseOrderResponse.class.getDeclaredField("originalOrder");
                field.setAccessible(true);
                field.set(po, idf);

                assertEquals(po.getOriginalOrder(), idf);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }
}
