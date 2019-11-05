package org.tradefinance.common;

import java.nio.file.Path;

public class FabricProxyConfig {
    private Path walletPath;

    private Path connectionProfilePath;

    private String channelName;

    private String contractName;

    private String org;

    public FabricProxyConfig(
        Path walletPath,
        Path connectionProfilePath,
        String channelName,
        String contractName,
        String org)
    {
        this.walletPath = walletPath;
        this.connectionProfilePath = connectionProfilePath;
        this.contractName = contractName;
        this.channelName = channelName;
        this.org = org;
    }

    public Path getWalletPath() {
        return walletPath;
    }

    public void setWalletPath(Path walletPath) {
        this.walletPath = walletPath;
    }

    public Path getConnectionProfilePath() {
        return connectionProfilePath;
    }

    public void setConnectionProfilePath(Path connectionProfilePath) {
        this.connectionProfilePath = connectionProfilePath;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }
}
