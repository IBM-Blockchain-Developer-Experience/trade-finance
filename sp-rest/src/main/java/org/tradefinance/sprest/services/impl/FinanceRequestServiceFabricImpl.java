package org.tradefinance.sprest.services.impl;

import java.util.Arrays;
import java.util.Collection;

import org.tradefinance.assets.FinanceRequest;
import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;
import org.tradefinance.sprest.services.FinanceRequestService;

public class FinanceRequestServiceFabricImpl extends org.tradefinance.common.services.impl.FinanceRequestServiceFabricImpl implements FinanceRequestService {

    private String targetPeer;

    public FinanceRequestServiceFabricImpl(FabricProxyConfig config, String identity, String targetPeer) throws FabricProxyException {
        super(config, identity);

        this.targetPeer = targetPeer;
    }

    @Override
    public void approveFinanceRequest(String id) throws Exception {
        String fcn = "approveFinanceRequest";
        this.proxy.submitTransaction(new String[] { targetPeer }, identity, subContractName, fcn, id);
    }

    @Override
    public void rejectFinanceRequest(String id) throws Exception {
        String fcn = "rejectFinanceRequest";
        this.proxy.submitTransaction(new String[] { targetPeer }, identity, subContractName, fcn, id);
    }

    @Override
    public Collection<FinanceRequest> getFinanceRequests() throws Exception {
        String fcn = "getFinanceRequestsForFinancier";
        String response = this.proxy.evaluateTransaction(identity, subContractName, fcn);

        FinanceRequest[] requests = gson.fromJson(response, FinanceRequest[].class);
        return Arrays.asList(requests);
    }
}
