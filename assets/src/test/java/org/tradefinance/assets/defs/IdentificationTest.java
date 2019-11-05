package org.tradefinance.assets.defs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class IdentificationTest {
    @Nested
    class Constructors {

        @Test
        public void shouldCreateFullObject() {
            Identification idf = new Identification("some identifier");

            assertEquals(idf.getEntityIdentification(), "some identifier");
        }
    }

    @Nested
    class Getters {

        @Test
        public void shouldGetEntityIdentification() {
            try {
                Identification idf = new Identification("some identifier");

                Field field = Identification.class.getDeclaredField("entityIdentification");
                field.setAccessible(true);
                field.set(idf, "a different identifier");

                assertEquals(idf.getEntityIdentification(), "a different identifier");
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }
}
