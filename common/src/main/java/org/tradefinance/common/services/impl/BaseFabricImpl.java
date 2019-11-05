package org.tradefinance.common.services.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.tradefinance.common.FabricProxy;
import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;

public class BaseFabricImpl {

    protected String subContractName;

    protected FabricProxy proxy;

    String format = "EEE MMM d HH:mm:ss Z yyy";
    protected Gson gson = new GsonBuilder().setDateFormat(format).create();
    protected String identity;

    public BaseFabricImpl(FabricProxyConfig config, String identity, String subContractName) throws FabricProxyException {
        this.proxy = new FabricProxy(config);
        this.identity = identity;
        this.subContractName = subContractName;
    }
}
