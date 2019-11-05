package org.tradefinance.assets;

import java.util.Date;

import org.tradefinance.assets.enums.FinanceRequestStatus;
import org.tradefinance.ledger_api.Asset;
import org.tradefinance.ledger_api.annotations.DefaultDeserialize;
import org.tradefinance.ledger_api.annotations.Deserialize;
import org.tradefinance.ledger_api.annotations.Private;
import org.tradefinance.ledger_api.annotations.VerifyHash;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public class FinanceRequest extends Asset {
    @Property
    @Private
    private String requesterId;

    @Property
    @Private
    private String financierId;

    @Property
    @Private
    private String purchaseOrderId;

    @Property
    @Private
    private Double amount;

    @Property
    @Private
    private Double interest;

    @Property
    @Private
    private Date completionDate;

    @Property
    @Private
    private String requestGroup;

    @Property
    @Private
    private FinanceRequestStatus status;

    @VerifyHash
    @Deserialize
    public FinanceRequest(String id, String requesterId, String financierId, String purchaseOrderId, Double amount, Double interest, Date completionDate, String requestGroup, FinanceRequestStatus status) {
        super(id);

        this.requesterId = requesterId;
        this.financierId = financierId;
        this.purchaseOrderId = purchaseOrderId;
        this.amount = amount;
        this.interest = interest;
        this.completionDate = completionDate;
        this.requestGroup = requestGroup;
        this.status = status;

        this.updateHash();
    }

    @DefaultDeserialize
    public FinanceRequest(String id, String hash) {
        super(id, hash);
    }

    public String getRequesterId() {
        return this.requesterId;
    }

    public String getFinancierId() {
        return this.financierId;
    }

    public String getPurchaseOrderId() {
        return this.purchaseOrderId;
    }

    public Double getAmount() {
        return this.amount;
    }

    public Double getInterest() {
        return this.interest;
    }

    public Date getCompletionDate() {
        return this.completionDate;
    }

    public String getRequestGroup() {
        return this.requestGroup;
    }

    public FinanceRequestStatus getStatus() {
        return this.status;
    }

    public void setStatus(FinanceRequestStatus newStatus) {
        if (this.status.compareTo(newStatus) > 0) {
            throw new RuntimeException("Status cannot go backwards");
        }

        this.status = newStatus;
    }
}
