package org.tradefinance.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.tradefinance.contracts.utils.BaseContext;
import org.tradefinance.contracts.utils.annotations.ACLRule;

import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class BaseContractTest {

    class MyContract extends BaseContract {

        public MyContract() {
            super(MyContract.class);
        }

        @ACLRule(
            requiredRoles = "AnyOf('youCanDoSomething')"
        )
        public void doSomething() {}

        public void anyoneCanDoThis() {}

        @ACLRule(
            requiredRoles = "AnyOf('youCanDoAnotherThing')"
        )
        public void doAnotherThing() {}
    }

    public final static Map<String, String> myClassMethodRules = new HashMap<String, String>() {
        /**
        *
        */
        private static final long serialVersionUID = 1L;

        {
        put("doSomething", "AnyOf('youCanDoSomething')");
        put("doAnotherThing", "AnyOf('youCanDoAnotherThing')");
    }};

    @Nested
    class Constructors {

        @Test
        public void shouldConfigureBaseContract() {
            BaseContract bc = new BaseContract(MyContract.class);

            assertEquals(bc.methodRules, myClassMethodRules);
        }
    }

    @Nested
    class beforeTransaction {
        BaseContext ctx;
        ChaincodeStub stub;
        BaseContract bc;

        @BeforeEach
        public void beforeEach() {
            ctx = mock(BaseContext.class);
            stub = mock(ChaincodeStub.class);

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getCallerAttributesWithValue("y")).thenReturn(new String[] {"youCanDoSomething"});

            bc = new BaseContract(MyContract.class);
        }

        @Test
        public void shouldDoNothingWhenFunctionHasNoAclRule() {
            when(stub.getFunction()).thenReturn("anyoneCanDoThis");

            bc.beforeTransaction(ctx);
        }

        @Test
        public void shouldDoNothingWhenFunctionHasAclRuleAndUserMeetsIt() {
            when(stub.getFunction()).thenReturn("doSomething");

            bc.beforeTransaction(ctx);
        }

        @Test
        public void shouldErrorWhenFunctionHasAclRuleAndUserDoesNotMeetIt() {
            when(stub.getFunction()).thenReturn("doAnotherThing");

            ContractRuntimeException cre = assertThrows(ContractRuntimeException.class, () -> { bc.beforeTransaction(ctx); });
            assertEquals(cre.getMessage(), "User does not have permission to call function");
        }

        @Test
        public void shouldErrorWhenFunctionHasAclRuleAndUserDoesNotMeetItAndSentInFunctionUsesNamespace() {
            when(stub.getFunction()).thenReturn("hello:doAnotherThing");

            ContractRuntimeException cre = assertThrows(ContractRuntimeException.class, () -> { bc.beforeTransaction(ctx); });
            assertEquals(cre.getMessage(), "User does not have permission to call function");
        }
    }
}
