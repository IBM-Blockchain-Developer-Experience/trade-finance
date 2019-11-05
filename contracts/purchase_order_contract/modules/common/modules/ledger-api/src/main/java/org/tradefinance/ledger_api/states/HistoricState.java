package org.tradefinance.ledger_api.states;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

@DataType()
public class HistoricState<T extends State> {
    @Property()
    private T value;

    @Property()
    public Long timestamp;

    @Property()
    public String txId;

    public HistoricState(Long timestamp, String txId, T value) {
        this.timestamp = timestamp;
        this.txId = txId;
        this.value = value;
    }

    public byte[] serialize() {
        return new JSONObject(this).toString().getBytes();
    }
}
