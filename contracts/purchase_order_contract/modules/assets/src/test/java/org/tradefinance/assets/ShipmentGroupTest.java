package org.tradefinance.assets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class ShipmentGroupTest {

    public final static String id = "some id";
    public final static String hash = "some hash";

    final static String[] shipmentIds = {"some", "ids"};

    @Nested
    class Constructors {

        @Test
        public void shouldCreateFullObject() {
            ShipmentGroup sg = new ShipmentGroup(id, shipmentIds);

            assertEquals(sg.getId(), id);
            assertEquals(sg.getShipmentIds(), shipmentIds);
        }

        @Test
        public void shouldPartialObject() {
            ShipmentGroup sg = new ShipmentGroup(id, hash);

            assertEquals(sg.getId(), id);
            assertEquals(sg.getHash(), hash);

            assertNull(sg.getShipmentIds());
        }
    }

    @Nested
    class Getters {

        @Test
        public void shouldGetShipmentIds() {
            try {
                ShipmentGroup sg = new ShipmentGroup(id, hash);

                Field field = ShipmentGroup.class.getDeclaredField("shipmentIds");
                field.setAccessible(true);
                field.set(sg, shipmentIds);

                assertEquals(sg.getShipmentIds(), shipmentIds);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    @Nested
    class Setters {

        @Test
        public void shouldAddShipmentId() {
            try {
                ShipmentGroup sg = new ShipmentGroup(id, hash);

                Field field = ShipmentGroup.class.getDeclaredField("shipmentIds");
                field.setAccessible(true);
                field.set(sg, shipmentIds);

                sg.addShipmentId("another id");

                assertArrayEquals(sg.getShipmentIds(), new String[] {"some", "ids", "another id"});
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }
}
