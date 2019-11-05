package org.tradefinance.common.services.impl;

import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;
import org.tradefinance.common.services.PurchaseOrderService;

public class PurchaseOrderServiceFabricImpl extends BaseFabricImpl implements PurchaseOrderService {

    public PurchaseOrderServiceFabricImpl(FabricProxyConfig config, String identity) throws FabricProxyException {
        super(config, identity, "PurchaseOrderContract");
    }
}
