package test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.tradefinance.common.FabricProxy;
import org.tradefinance.common.FabricProxyConfig;
import org.tradefinance.common.FabricProxyException;

import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sk.PrettyTable;

public class ProxyTestHarness {


    public static void main(String[] args) {

        Path connectionProfilePath = null;
        Path walletPath = null;
        String channelName = "mychannel";
        String contractName = "contract";
        String org = "Org1";

        String callType = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);

        FabricProxyConfig config = new FabricProxyConfig(
            walletPath,
            connectionProfilePath,
            channelName,
            contractName,
            org);

            if (callType.equals("call")) {
                ProxyTestHarness.call(args, config);
            } else if (callType.equals("listen")) {
                ProxyTestHarness.listen(args, config);
            }
    }

    private static void call(String[] args, FabricProxyConfig config) {
        try {
            String contractName = "PurchaseOrderContract";
            String isSubmitStr = args[0];
            boolean isSubmit = isSubmitStr.equals("submit");
            String fcnName = args[1];
            String fcnArgsStr = args[2];
            String user;
            try {
                user = args[3];
            } catch (ArrayIndexOutOfBoundsException exception) {
                user = "admin";
            }

            JSONArray jsonArgs = new JSONArray(fcnArgsStr);
            String[] fcnArgs = jsonArgs.toList().toArray(new String[jsonArgs.length()]);

            FabricProxy proxy = new FabricProxy(config);
            System.out.println("Started fabric proxy!");

            String result;
            if (isSubmit) {
                // result = proxy.submitTransaction(user, contractName, fcnName, fcnArgs);
            } else {
                // result = proxy.evaluateTransaction(user, contractName, fcnName, fcnArgs);
            }


            System.out.println(ProxyTestHarness.jsonStringToTable(null));
            // Thread.sleep(100);
        } catch (FabricProxyException exception) {
            exception.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException exception) {
            exception.printStackTrace();
            ProxyTestHarness.printCallHelp();
        }
    }

    private static void listen(String[] args, FabricProxyConfig config) {
        try {
            String contractName = "purchaseOrderContract";
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
                    ProxyTestHarness.createContractListener(proxy, contractName, user, eventName);
                break;
                case "block":
                    ProxyTestHarness.createBlockListener(proxy, user);
                break;
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException exception) {
            ProxyTestHarness.printListenerHelp();
        } catch (FabricProxyException exception) {
            exception.printStackTrace();
        }

        while(true) {}
    }

    private static void createContractListener(FabricProxy proxy, String user, String contractName, String eventName) throws IOException, FabricProxyException {
        proxy.addContractListener(user, eventName, contractName, (ContractEvent contractEvent) -> {
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

    private static void printCallHelp() {
        System.out.println("Usage: ProxyTestHarness call <submit|evaluate> <function name> <string args> <user>");
    }

    private static void printListenerHelp() {
        System.out.println("Usage: ProxyEventListener listen <user> <contract|block> <eventName>");
    }

    private static PrettyTable jsonStringToTable(String json) {
        JSONObject jsonObject;
        JSONArray jsonArray;
        PrettyTable table = null;
        try {
            jsonObject = new JSONObject(json);
            table = ProxyTestHarness.addRowToTable(null, jsonObject);
        } catch (Exception ex) {
            jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                table = ProxyTestHarness.addRowToTable(table, jsonObject);
              }
        }
        return table;
    }


    private static PrettyTable addRowToTable(PrettyTable table, JSONObject jsonObject) {
        JSONArray jsonKeys = jsonObject.names();
        String[] keys = jsonKeys.toList().toArray(new String[]{});

        if (table == null) {
            table = new PrettyTable(keys);
        }

        ArrayList<String> valueList = new ArrayList<String>();
        for (String key : keys) {
            try {
                valueList.add(jsonObject.getString(key));
            } catch (JSONException err) {
                // Ignore this error. Key not a string
                valueList.add("NP");
            }
        }
        table.addRow(valueList.toArray(new String[]{}));

        return table;
    }
}
