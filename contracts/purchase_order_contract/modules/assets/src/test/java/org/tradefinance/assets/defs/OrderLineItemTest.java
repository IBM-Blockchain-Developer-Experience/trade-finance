package org.tradefinance.assets.defs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class OrderLineItemTest {

    @Nested
    class Constructors {

        @Test
        public void shouldCreateObjectWithGtin() {
            OrderLineItem oli = new OrderLineItem(1, 100, 50, 1234567890);

            assertEquals(oli.getLineItemNumber(), 1);
            assertEquals(oli.getRequestedQuantity(), 100);
            assertEquals(oli.getNetPrice(), 50);
            assertEquals(oli.getNetAmount(), 5000);
            assertEquals(oli.getTransactionalTradeItem().getGtin(), (new Item(1234567890)).getGtin());
        }

        @Test
        public void shouldCreateObjectWithItem() {
            Item it = new Item(1234567890);
            OrderLineItem oli = new OrderLineItem(1, 100, 50, it);

            assertEquals(oli.getLineItemNumber(), 1);
            assertEquals(oli.getRequestedQuantity(), 100);
            assertEquals(oli.getNetPrice(), 50);
            assertEquals(oli.getNetAmount(), 5000);
            assertEquals(oli.getTransactionalTradeItem(), it);
        }
    }

    @Nested
    class Getters {

        @Test
        public void shouldGetLineItemNumber() {
            try {
                OrderLineItem oli = new OrderLineItem(1, 100, 50, 1234567890);

                Field field = OrderLineItem.class.getDeclaredField("lineItemNumber");
                field.setAccessible(true);
                field.set(oli, 2);

                assertEquals(oli.getLineItemNumber(), 2);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetRequestedQuantity() {
            try {
                OrderLineItem oli = new OrderLineItem(1, 100, 50, 1234567890);

                Field field = OrderLineItem.class.getDeclaredField("requestedQuantity");
                field.setAccessible(true);
                field.set(oli, 101);

                assertEquals(oli.getRequestedQuantity(), 101);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetNetPrice() {
            try {
                OrderLineItem oli = new OrderLineItem(1, 100, 50, 1234567890);

                Field field = OrderLineItem.class.getDeclaredField("netPrice");
                field.setAccessible(true);
                field.set(oli, 51);

                assertEquals(oli.getNetPrice(), 51);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetNetAmount() {
            try {
                OrderLineItem oli = new OrderLineItem(1, 100, 50, 1234567890);

                Field field = OrderLineItem.class.getDeclaredField("netAmount");
                field.setAccessible(true);
                field.set(oli, 5001);

                assertEquals(oli.getNetAmount(), 5001);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetTransactionalTradeItem() {
            try {
                Item it = new Item(987654321L);

                OrderLineItem oli = new OrderLineItem(1, 100, 50, 1234567890);

                Field field = OrderLineItem.class.getDeclaredField("transactionalTradeItem");
                field.setAccessible(true);
                field.set(oli, it);

                assertEquals(oli.getTransactionalTradeItem(), it);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }
}
