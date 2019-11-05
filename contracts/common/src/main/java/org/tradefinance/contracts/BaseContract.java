package org.tradefinance.contracts;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.tradefinance.ledger_api.collections.BooleanRulesHandler;
import org.tradefinance.contracts.utils.BaseContext;
import org.tradefinance.contracts.utils.annotations.ACLRule;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;

public class BaseContract implements ContractInterface {

    protected Map<String, String> methodRules;

    public BaseContract(Class<? extends BaseContract> childClass) {
        this.methodRules = new HashMap<String, String>();

        for (Method classMethod : childClass.getMethods()) {
            ACLRule aclRule = classMethod.getAnnotation(ACLRule.class);

            if (aclRule != null) {
                String requiredRoles = aclRule.requiredRoles();
                this.methodRules.put(classMethod.getName(), requiredRoles);
            }
        }
    }

    @Override
    public void beforeTransaction(Context ctx) {
        BaseContext bctx = (BaseContext) ctx;
        String functionCall = bctx.getStub().getFunction();
        String[] functionParts = functionCall.split(":");

        String methodName = functionParts[0];

        if (functionParts.length == 2) {
            methodName = functionParts[1];
        }

        if (this.methodRules.containsKey(methodName)) {
            BooleanRulesHandler crh = new BooleanRulesHandler(this.methodRules.get(methodName), bctx.getCallerAttributesWithValue("y"));

            if (!crh.evaluate()) {
                throw new ContractRuntimeException("User does not have permission to call function");
            }
        }
    }
}
