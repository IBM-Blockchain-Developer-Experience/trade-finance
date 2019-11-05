package listener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.tradefinance.common.FabricProxy;
import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;

import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.sdk.BlockEvent;

public class ProxyEventListener {

    public static void main(String[] args) {
        Path connectionProfilePath = null;
        Path walletPath = null;
        String channelName = "mychannel";
        String contractName = "contract";
        String org = "Org1";

        FabricProxyConfig config = new FabricProxyConfig(
            walletPath,
            connectionProfilePath,
            channelName,
            contractName,
            org);

        try {
            String user = args[0];
            String eventType = args[1];
            String eventName = "";
            if (eventType.equals("contract")) {
                eventName = args[2];
            }

            FabricProxy proxy = new FabricProxy(config);
            System.out.println("Started fabric proxy!");

            switch (eventType) {
                case "contract":
                    ProxyEventListener.createContractListener(proxy, user, contractName, eventName);
                break;
                case "block":
                    ProxyEventListener.createBlockListener(proxy, user);
                break;
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException exception) {
            ProxyEventListener.printHelp();
        } catch (FabricProxyException exception) {
            exception.printStackTrace();
        }
    }

    private static void createContractListener(FabricProxy proxy, String user, String contractName,  String eventName) throws IOException, FabricProxyException {
        proxy.addContractListener(user, contractName, eventName, (ContractEvent contractEvent) -> {
            byte[] payload = contractEvent.getPayload().get();
            System.out.println("Received Contract Event: " + contractEvent.getName() + ": " + new String(payload, StandardCharsets.UTF_8));

        });
    }

    private static void createBlockListener(FabricProxy proxy, String user) throws IOException, FabricProxyException {
        proxy.addBlockListener(user, (BlockEvent blockEvent) -> {
            Long blockNumber = blockEvent.getBlockNumber();
            System.out.println("Received Block Event for block: " + String.valueOf(blockNumber));
        });
    }

    private static void printHelp() {
        System.out.println("Usage: ProxyEventListener <user> <contract|block> <eventName>");
    }
}
