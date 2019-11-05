package org.tradefinance.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class FinanceRequestGroupTest {

    public final static String id = "some id";
    public final static String hash = "some hash";

    final static String[] requestIds = {"some", "ids"};

    @Nested
    class Constructors {

        @Test
        public void shouldCreateFullObject() {
            FinanceRequestGroup frg = new FinanceRequestGroup(id, requestIds);

            assertEquals(frg.getId(), id);
            assertEquals(frg.getRequestIds(), requestIds);
        }

        @Test
        public void shouldPartialObject() {
            FinanceRequestGroup frg = new FinanceRequestGroup(id, hash);

            assertEquals(frg.getId(), id);
            assertEquals(frg.getHash(), hash);

            assertNull(frg.getRequestIds());
        }
    }

    @Nested
    class Getters {

        @Test
        public void shouldGetRequestIds() {
            try {
                FinanceRequestGroup frg = new FinanceRequestGroup(id, hash);

                Field field = FinanceRequestGroup.class.getDeclaredField("requestIds");
                field.setAccessible(true);
                field.set(frg, requestIds);

                assertEquals(frg.getRequestIds(), requestIds);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }
}
