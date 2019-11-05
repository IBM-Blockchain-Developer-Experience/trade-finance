package org.tradefinance.sprest.services;

import java.util.Collection;

import org.tradefinance.assets.FinanceRequest;

public interface FinanceRequestService extends org.tradefinance.common.services.FinanceRequestService {
    public Collection<FinanceRequest> getFinanceRequests() throws Exception;

    public void approveFinanceRequest(String id) throws Exception;

    public void rejectFinanceRequest(String id) throws Exception;
}
