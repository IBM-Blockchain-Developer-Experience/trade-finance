package org.tradefinance.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.Date;

import org.tradefinance.assets.enums.FinanceRequestStatus;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class FinanceRequestTest {
    public final static long milliSince1970 = 1565096705000L;

    public final static String hash = "some hash";
    public final static String id = "some id";

    public final static String requesterId = "some requester";
    public final static String financierId = "some financier";
    public final static String purchaseOrderId = "some purchase order";
    public final static Double amount = 100.00;
    public final static Double interest = 0.10;
    private final static Date completionDate = new Date(milliSince1970);
    public final static String requestGroup = "TEST_GROUP";
    public final static FinanceRequestStatus status = FinanceRequestStatus.APPROVED;

    @Nested
    class Constructors {

        @Test
        public void shouldCreateFullObject() {
            FinanceRequest fr = new FinanceRequest(id, requesterId, financierId, purchaseOrderId, amount, interest, completionDate, requestGroup, status);

            assertEquals(fr.getId(), id);
            assertEquals(fr.getRequesterId(), requesterId);
            assertEquals(fr.getFinancierId(), financierId);
            assertEquals(fr.getPurchaseOrderId(), purchaseOrderId);
            assertEquals(fr.getAmount(), amount);
            assertEquals(fr.getInterest(), interest);
            assertEquals(fr.getCompletionDate(), completionDate);
        }

        @Test
        public void shouldPartialObject() {
            FinanceRequest fr = new FinanceRequest(id, hash);

            assertEquals(fr.getId(), "some id");
            assertEquals(fr.getHash(), "some hash");

            assertNull(fr.getRequesterId());
            assertNull(fr.getFinancierId());
            assertNull(fr.getPurchaseOrderId());
            assertNull(fr.getAmount());
            assertNull(fr.getInterest());
            assertNull(fr.getCompletionDate());
        }
    }

    @Nested
    class Getters {

        @Test
        public void shouldGetRequesterId() {
            try {
                FinanceRequest fr = new FinanceRequest(id, hash);

                Field field = FinanceRequest.class.getDeclaredField("requesterId");
                field.setAccessible(true);
                field.set(fr, requesterId);

                assertEquals(fr.getRequesterId(), requesterId);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetFinancierId() {
            try {
                FinanceRequest fr = new FinanceRequest(id, hash);

                Field field = FinanceRequest.class.getDeclaredField("financierId");
                field.setAccessible(true);
                field.set(fr, financierId);

                assertEquals(fr.getFinancierId(), financierId);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetPurchaseOrderId() {
            try {
                FinanceRequest fr = new FinanceRequest(id, hash);

                Field field = FinanceRequest.class.getDeclaredField("purchaseOrderId");
                field.setAccessible(true);
                field.set(fr, purchaseOrderId);

                assertEquals(fr.getPurchaseOrderId(), purchaseOrderId);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetAmount() {
            try {
                FinanceRequest fr = new FinanceRequest(id, hash);

                Field field = FinanceRequest.class.getDeclaredField("amount");
                field.setAccessible(true);
                field.set(fr, amount);

                assertEquals(fr.getAmount(), amount);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetInterest() {
            try {
                FinanceRequest fr = new FinanceRequest(id, hash);

                Field field = FinanceRequest.class.getDeclaredField("interest");
                field.setAccessible(true);
                field.set(fr, interest);

                assertEquals(fr.getInterest(), interest);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetCompletionDate() {
            try {
                FinanceRequest fr = new FinanceRequest(id, hash);

                Field field = FinanceRequest.class.getDeclaredField("completionDate");
                field.setAccessible(true);
                field.set(fr, completionDate);

                assertEquals(fr.getCompletionDate(), completionDate);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetRequestGroup() {
            try {
                FinanceRequest fr = new FinanceRequest(id, hash);

                Field field = FinanceRequest.class.getDeclaredField("requestGroup");
                field.setAccessible(true);
                field.set(fr, requestGroup);

                assertEquals(fr.getRequestGroup(), requestGroup);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldGetStatus() {
            try {
                FinanceRequest fr = new FinanceRequest(id, hash);

                Field field = FinanceRequest.class.getDeclaredField("status");
                field.setAccessible(true);
                field.set(fr, status);

                assertEquals(fr.getStatus(), status);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    @Nested
    class Setters {

        @Test
        public void shouldSetStatus() {
            try {
                FinanceRequest fr = new FinanceRequest(id, hash);

                Field field = FinanceRequest.class.getDeclaredField("status");
                field.setAccessible(true);
                field.set(fr, status);

                fr.setStatus(FinanceRequestStatus.WITHDRAWN);

                assertEquals(fr.getStatus(), FinanceRequestStatus.WITHDRAWN);
            }
            catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        public void shouldFailToSetStatusWhenBackwardStep() {
            try {
                FinanceRequest fr = new FinanceRequest(id, hash);

                Field field = FinanceRequest.class.getDeclaredField("status");
                field.setAccessible(true);
                field.set(fr, status);

                fr.setStatus(FinanceRequestStatus.PENDING);

                fail("should have failed to set status");
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), "Status cannot go backwards");
            }
        }
    }
}
