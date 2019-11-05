package org.tradefinance.assets.defs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PartyTest {

    @Nested
    class Constructors {

        @Test
        public void shouldCreateBlankParty() {
            Party party = new Party();

            assertNull(party.getGln());
            assertNull(party.getAdditionalPartyIdentification());
        }

        @Test
        public void shouldCreatePartyFromGln() {
            Party party = new Party(1234567890);

            assertEquals(party.getGln(), 1234567890);
            assertNull(party.getAdditionalPartyIdentification());
        }

        @Test
        public void shouldCreateFullParty() {
            Party party = new Party(1234567890, "additional info");

            assertEquals(party.getGln(), 1234567890);
            assertEquals(party.getAdditionalPartyIdentification(), "additional info");
        }
    }

    @Nested
    class Getters {

        @Test
        public void shouldGetGln() {
            try {
                Party party = new Party(1234567890, "additional info");

                Field field = Party.class.getDeclaredField("gln");
                field.setAccessible(true);
                field.set(party, 9876543210L);

                assertEquals(party.getGln(), 9876543210L);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetAdditionalPartyIdentification() {
            try {
                Party party = new Party(1234567890, "additional info");

                Field field = Party.class.getDeclaredField("additionalPartyIdentification");
                field.setAccessible(true);
                field.set(party, "other info");

                assertEquals(party.getAdditionalPartyIdentification(), "other info");
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }
}
