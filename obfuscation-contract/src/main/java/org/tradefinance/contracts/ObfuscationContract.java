package org.tradefinance.contracts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.json.JSONArray;

public class ObfuscationContract extends ChaincodeBase {

    public ObfuscationContract() {}

    public Response init(ChaincodeStub stub) {
        return ResponseUtils.newSuccessResponse();
    }

    public Response invoke(ChaincodeStub stub) {
        Map<String, byte[]> transientMap = stub.getTransient();

        if (!transientMap.containsKey("chaincodeName")) {
            throw new ChaincodeException("Missing required field chaincodeName from transient data");
        } else if (!transientMap.containsKey("functionName")) {
            throw new ChaincodeException("Missing required field functionName from transient data");
        }

        String chaincodeName = new String(transientMap.get("chaincodeName"));
        String functionName = new String(transientMap.get("functionName"));

        List<byte[]> argsList = new ArrayList<byte[]>();
        argsList.add(functionName.getBytes());

        if (transientMap.containsKey("args")) {
            String argsArrayString = new String(transientMap.get("args"));
            JSONArray argsArray = new JSONArray(argsArrayString);

            for (int i = 0; i < argsArray.length(); i++) {
                try {
                    String arg = argsArray.getString(i);
                    argsList.add(arg.getBytes());
                } catch (Exception e) {
                    throw new ChaincodeException("Args array should contain only strings");
                }
            }
        }

        return stub.invokeChaincode(chaincodeName, argsList);
    }

    public static void main(String [] args){
        new ObfuscationContract().start(args);
    }
}
