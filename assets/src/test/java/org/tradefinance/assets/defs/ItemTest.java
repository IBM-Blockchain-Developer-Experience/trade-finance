package org.tradefinance.assets.defs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class ItemTest {
    @Nested
    class Constructors {

        @Test
        public void shouldCreateFullObject() {
            Item it = new Item(1234567890);

            assertEquals(it.getGtin(), 1234567890);
        }
    }

    @Nested
    class Getters {

        @Test
        public void shouldGetGtin() {
            try {
                Item it = new Item(1234567890);

                Field field = Item.class.getDeclaredField("gtin");
                field.setAccessible(true);
                field.set(it, 9876543210L);

                assertEquals(it.getGtin(), 9876543210L);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }
}
