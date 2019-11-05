package org.tradefinance.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.tradefinance.assets.enums.ShipmentStatus;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class ShipmentTest {

    public final static String hash = "some hash";
    public final static String id = "some id";

    public final static String purchaseOrderId = "some purchase order";
    public final static int units = 10;
    public final static String senderId = "some seller ID";
    public final static String receiverId = "some receiver ID";
    public final static ShipmentStatus status = ShipmentStatus.DELIVERED;

    @Nested
    class Constructors {

        @Test
        public void shouldCreateFullObject() {
            Shipment shipment = new Shipment(id, purchaseOrderId, units, senderId, receiverId, status);

            assertEquals(shipment.getId(), id);
            assertEquals(shipment.getPurchaseOrderId(), purchaseOrderId);
            assertEquals(shipment.getUnits(), units);
            assertEquals(shipment.getSenderId(), senderId);
            assertEquals(shipment.getReceiverId(), receiverId);
            assertEquals(shipment.getStatus(), status);
        }

        @Test
        public void shouldCreatePartialObject() {
            Shipment shipment = new Shipment(id, hash);

            assertEquals(shipment.getId(), id);
            assertEquals(shipment.getHash(), hash);

            assertNull(shipment.getPurchaseOrderId());
            assertNull(shipment.getUnits());
        }
    }

    @Nested
    class Getters {

        @Test
        public void shouldGetPurchaseOrderId() {
            try {
                Shipment shipment = new Shipment(id, hash);

                Field field = Shipment.class.getDeclaredField("purchaseOrderId");
                field.setAccessible(true);
                field.set(shipment, purchaseOrderId);

                assertEquals(shipment.getPurchaseOrderId(), purchaseOrderId);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetUnits() {
            try {
                Shipment shipment = new Shipment(id, hash);

                Field field = Shipment.class.getDeclaredField("units");
                field.setAccessible(true);
                field.set(shipment, units);

                assertEquals(shipment.getUnits(), units);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    @Nested
    class Setters {
        @Test
        public void shouldNotMoveStatusBackwards() {
            try {
                Shipment shipment = new Shipment(id, purchaseOrderId, units, receiverId, senderId, status);
                // Move backwards from DELIVERED to IN_TRANSIT
                shipment.setStatus(ShipmentStatus.IN_TRANSIT);
                fail("Should not be able to move the status backwards");
            } catch (Exception e) {
                assertTrue(e instanceof RuntimeException);
            }
        }

        @Test
        public void shouldMoveStatusForwards() {
            try {
                Shipment shipment = new Shipment(id, purchaseOrderId, units, receiverId, senderId, ShipmentStatus.IN_TRANSIT);
                // Move backwards from DELIVERED to IN_TRANSIT
                shipment.setStatus(ShipmentStatus.DELIVERED);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }
}
