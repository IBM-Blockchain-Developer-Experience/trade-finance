package org.tradefinance.assets.defs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class OrderIdentificationTest {

    @Nested
    class Constructors {

        @Test
        public void shouldCreateObjectFromGln() {
            OrderIdentification oi = new OrderIdentification("some identifier", (long) 1234567890);

            assertEquals(oi.getContentOwner().getGln(), (new Party(1234567890)).getGln());
            assertNull(oi.getContentOwner().getAdditionalPartyIdentification());
            assertEquals(oi.getEntityIdentification(), "some identifier");
        }

        @Test
        public void shouldCreateObjectFromParty() {
            Party party = new Party(1234567890);
            OrderIdentification oi = new OrderIdentification("some identifier", party);

            assertEquals(oi.getContentOwner(), party);
            assertEquals(oi.getEntityIdentification(), "some identifier");
        }
    }

    @Nested
    class Getters {
        @Test
        public void shouldGetParty() {
            try {
                Party party = new Party(9876543210L);

                OrderIdentification oi = new OrderIdentification("some identifier", 1234567890);

                Field field = OrderIdentification.class.getDeclaredField("contentOwner");
                field.setAccessible(true);
                field.set(oi, party);

                assertEquals(oi.getContentOwner(), party);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }
}
