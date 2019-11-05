package org.tradefinance.contracts.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

public class BaseContext extends Context {
    private Map<String, String> attrs;

    @SuppressWarnings("unchecked")
    public BaseContext(ChaincodeStub stub) {
        super(stub);

        ClientIdentity ci = this.getClientIdentity();

        try {
            Field f = ci.getClass().getDeclaredField("attrs");
            f.setAccessible(true);

            this.attrs = (Map<String, String>) f.get(ci);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create context. Could not create tradenet client identity");
        }
    }

    public String[] getCallerAttributesWithValue(String value) {
        ArrayList<String> matchingAttrs = new ArrayList<String>();

        for (String key : this.attrs.keySet()) {
            if (this.getClientIdentity().assertAttributeValue(key, value)) {
                matchingAttrs.add(key);
            }
        }

        return matchingAttrs.toArray(new String[matchingAttrs.size()]);
    }

    public String getCallerOrg() {
        return Utils.getIdentityOrg(this.getCallerUsername());
    }

    public String getCallerUsername() {
        String username = this.getClientIdentity().getAttributeValue("tradefinance.username");
        if (username == null) {
            throw new ContractRuntimeException("Identity does not have the attribute 'tradefinance.username'");
        }
        return this.getClientIdentity().getAttributeValue("tradefinance.username");
    }

    public long getCallerGln() {
        String gln = this.getClientIdentity().getAttributeValue("global.gln");
        if (gln == null) {
            throw new ContractRuntimeException("Identity does not have the attribute 'global.gln'");
        }
        return Long.parseLong(gln);
    }

    public String[] getCallerPrivateCollectionNames() {
        return new String[]{ this.getCallerOrg() };
    }
}
