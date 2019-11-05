package org.tradefinance.porest.services;

import java.util.Collection;

import org.tradefinance.assets.FinanceRequest;
import org.tradefinance.assets.FinanceRequestGroup;
import org.tradefinance.assets.defs.Party;

public interface FinanceRequestService extends org.tradefinance.common.services.FinanceRequestService {

    public Collection<FinanceRequest> getFinanceRequests(String behalfOfId) throws Exception;

    public FinanceRequestGroup createFinanceRequest(Party requester, String[] financierIds, String purchaseOrderId, double amount, double interest, int monthLength) throws Exception;

    public void acceptFinanceRequest(String id) throws Exception;

    public void withdrawFinanceRequest(String id) throws Exception;
}
