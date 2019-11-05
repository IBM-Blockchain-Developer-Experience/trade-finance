package org.tradefinance.common.services.impl;

import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;
import org.tradefinance.common.services.ShipmentService;

public class ShipmentServiceFabricImpl extends BaseFabricImpl implements ShipmentService {

    public ShipmentServiceFabricImpl(FabricProxyConfig config, String identity) throws FabricProxyException {
        super(config, identity, "ShipmentContract");
    }
}
