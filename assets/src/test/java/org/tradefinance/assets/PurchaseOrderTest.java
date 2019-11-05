package org.tradefinance.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.tradefinance.assets.defs.OrderIdentification;
import org.tradefinance.assets.defs.OrderLineItem;
import org.tradefinance.assets.defs.Party;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class PurchaseOrderTest {

    public final static String hash = "some hash";
    public final static String id = "some id";
    public final static OrderIdentification oi = new OrderIdentification("some order id", 1928374650L);
    public final static Party buyer = new Party(1111111111);
    public final static Party seller = new Party(2222222222L);
    public final static OrderLineItem oli = new OrderLineItem(2, 100, 50, 987654321L);

    @Nested
    class Constructors {

        @Test
        public void shouldCreateFullObject() {
            PurchaseOrder po = new PurchaseOrder("some order id", 1234567890, buyer, seller, 100, 50, 987654321L);

            assertEquals(po.getOrderIdentification().getEntityIdentification(), "some order id");
            assertEquals(po.getOrderIdentification().getContentOwner().getGln(), 1234567890);
            assertEquals(po.getOrderTypeCode(), 220);
            assertEquals(po.getOrderInstructionCode(), "PARTIAL_DELIVERY_ALLOWED");
            assertEquals(po.getBuyer(), buyer);
            assertEquals(po.getSeller(), seller);
            assertEquals(po.getOrderLineItem().getLineItemNumber(), 1);
            assertEquals(po.getOrderLineItem().getRequestedQuantity(), 100);
            assertEquals(po.getOrderLineItem().getNetPrice(), 50);
            assertEquals(po.getOrderLineItem().getTransactionalTradeItem().getGtin(), 987654321L);
        }

        @Test
        public void shouldCreateFullObjectFromAllDetails() {
            PurchaseOrder po = new PurchaseOrder(oi, 123, "some instruction", buyer, seller, oli);

            assertEquals(po.getOrderIdentification(), oi);
            assertEquals(po.getOrderTypeCode(), 123);
            assertEquals(po.getOrderInstructionCode(), "some instruction");
            assertEquals(po.getBuyer(), buyer);
            assertEquals(po.getSeller(), seller);
            assertEquals(po.getOrderLineItem(), oli);
        }

        @Test
        public void shouldCreatePartialPurchaseOrder() {
            PurchaseOrder po = new PurchaseOrder(id, hash);

            assertEquals(po.getId(), id);
            assertEquals(po.getHash(), hash);

            assertNull(po.getOrderTypeCode());
            assertNull(po.getOrderInstructionCode());
            assertNull(po.getBuyer());
            assertNull(po.getSeller());
            assertNull(po.getOrderLineItem());
        }
    }

    @Nested
    class Getters {

        @Test
        public void shouldGetOrderIdentification() {
            try {
                PurchaseOrder po = new PurchaseOrder(id, hash);

                Field field = PurchaseOrder.class.getDeclaredField("orderIdentification");
                field.setAccessible(true);
                field.set(po, oi);

                assertEquals(po.getOrderIdentification(), oi);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetOrderTypeCode() {
            try {
                PurchaseOrder po = new PurchaseOrder(id, hash);

                Field field = PurchaseOrder.class.getDeclaredField("orderTypeCode");
                field.setAccessible(true);
                field.set(po, 123);

                assertEquals(po.getOrderTypeCode(), 123);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetOrderInstructionCode() {
            try {
                PurchaseOrder po = new PurchaseOrder(id, hash);

                Field field = PurchaseOrder.class.getDeclaredField("orderInstructionCode");
                field.setAccessible(true);
                field.set(po, "some instruction code");

                assertEquals(po.getOrderInstructionCode(), "some instruction code");
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetBuyer() {
            try {
                PurchaseOrder po = new PurchaseOrder(id, hash);

                Field field = PurchaseOrder.class.getDeclaredField("buyer");
                field.setAccessible(true);
                field.set(po, buyer);

                assertEquals(po.getBuyer(), buyer);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetSeller() {
            try {
                PurchaseOrder po = new PurchaseOrder(id, hash);

                Field field = PurchaseOrder.class.getDeclaredField("seller");
                field.setAccessible(true);
                field.set(po, seller);

                assertEquals(po.getSeller(), seller);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetOrderLineItem() {
            try {
                PurchaseOrder po = new PurchaseOrder(id, hash);

                Field field = PurchaseOrder.class.getDeclaredField("orderLineItem");
                field.setAccessible(true);
                field.set(po, oli);

                assertEquals(po.getOrderLineItem(), oli);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }
}
