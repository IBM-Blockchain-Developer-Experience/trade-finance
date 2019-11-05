package org.tradefinance.common.services;

import java.util.Collection;

import org.tradefinance.assets.FinanceRequest;

public interface FinanceRequestService {

    public FinanceRequest getFinanceRequest(String id) throws Exception;

    public FinanceRequest getFinanceRequestByHash(String hash) throws Exception;

    public Collection<FinanceRequest> getFinanceRequestsByGroupHash(String hash) throws Exception;
}
