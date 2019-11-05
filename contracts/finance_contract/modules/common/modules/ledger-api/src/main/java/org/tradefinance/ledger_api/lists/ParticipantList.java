package org.tradefinance.ledger_api.lists;

import org.tradefinance.ledger_api.Participant;
import org.tradefinance.ledger_api.states.StateList;

import org.hyperledger.fabric.contract.Context;

public class ParticipantList<T extends Participant> extends StateList<Participant> {
    public ParticipantList(Context ctx, String listName, Class<T> clazz) {
        super(ctx, listName);

        this.use(clazz);
    }
}
