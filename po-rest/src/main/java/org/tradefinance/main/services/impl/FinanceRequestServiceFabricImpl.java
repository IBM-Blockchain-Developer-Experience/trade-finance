package org.tradefinance.porest.services.impl;

import java.util.Arrays;
import java.util.Collection;

import org.tradefinance.assets.FinanceRequest;
import org.tradefinance.assets.FinanceRequestGroup;
import org.tradefinance.assets.defs.Party;
import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;
import org.tradefinance.porest.services.FinanceRequestService;

public class FinanceRequestServiceFabricImpl extends org.tradefinance.common.services.impl.FinanceRequestServiceFabricImpl implements FinanceRequestService {
    private String targetPeer;

    public FinanceRequestServiceFabricImpl(FabricProxyConfig config, String identity, String targetPeer) throws FabricProxyException {
        super(config, identity);
        this.targetPeer = targetPeer;
    }

    @Override
    public Collection<FinanceRequest> getFinanceRequests(String behalfOfId) throws FabricProxyException {
        String fcn = "getFinanceRequestsForRequester";
        String response = this.proxy.evaluateTransaction(identity, subContractName, fcn, new String[]{behalfOfId});

        FinanceRequest[] requests = gson.fromJson(response, FinanceRequest[].class);
        return Arrays.asList(requests);
    }

    @Override
    public FinanceRequestGroup createFinanceRequest(Party requester, String[] financierIds, String purchaseOrderId, double amount, double interest, int monthLength) throws FabricProxyException {
        String requesterStr = requester.serialize();
        String amountStr = Double.toString(amount);
        String interestStr = Double.toString(interest);
        String monthLengthStr = Integer.toString(monthLength);

        String fcn = "createFinanceRequest";
        String response = this.proxy.submitTransaction(new String[] {this.targetPeer}, this.identity, subContractName, fcn, requesterStr, Arrays.toString(financierIds), purchaseOrderId, amountStr, interestStr, monthLengthStr);

        return gson.fromJson(response, FinanceRequestGroup.class);
    }

    @Override
    public void acceptFinanceRequest(String id) throws Exception {
        String fcn = "acceptFinanceRequest";
        this.proxy.submitTransaction(this.identity, subContractName, fcn, id);
    }

    @Override
    public void withdrawFinanceRequest(String id) throws FabricProxyException {
        String fcn = "withdrawFinanceRequest";
        this.proxy.submitTransaction(identity, subContractName, fcn, id);
    }
}
