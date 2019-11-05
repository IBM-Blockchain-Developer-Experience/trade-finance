package org.tradefinance.common.services.impl;

import java.util.Arrays;
import java.util.Collection;

import org.tradefinance.assets.FinanceRequest;
import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;
import org.tradefinance.common.services.FinanceRequestService;

public class FinanceRequestServiceFabricImpl extends BaseFabricImpl implements FinanceRequestService {

    public FinanceRequestServiceFabricImpl(FabricProxyConfig config, String identity) throws FabricProxyException {
        super(config, identity, "FinanceRequestContract");
    }

    @Override
    public FinanceRequest getFinanceRequest(String id) throws FabricProxyException {
        String fcn = "getFinanceRequest";
        String response = this.proxy.evaluateTransaction(this.identity, subContractName, fcn, new String[]{id});

        FinanceRequest request = gson.fromJson(response, FinanceRequest.class);
        return request;
    }

    @Override
    public FinanceRequest getFinanceRequestByHash(String hash) throws FabricProxyException {
        return null;
    }

    @Override
    public Collection<FinanceRequest> getFinanceRequestsByGroupHash(String hash) throws FabricProxyException {
        String fcn = "getFinanceRequestsByGroupHash";
        String response = this.proxy.evaluateTransaction(identity, subContractName, fcn, new String[]{hash});
        FinanceRequest[] requests = gson.fromJson(response, FinanceRequest[].class);
        return Arrays.asList(requests);
    }
}
