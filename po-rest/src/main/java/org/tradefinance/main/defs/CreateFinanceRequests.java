package org.tradefinance.porest.defs;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.tradefinance.assets.defs.Party;

@XmlRootElement
public class CreateFinanceRequests {

    @XmlElement
    public Party requester;

    @XmlElement
    public String[] financierIds;

    @XmlElement
    public String purchaseOrderId;

    @XmlElement
    public double amount;

    @XmlElement
    public double interest;

    @XmlElement
    public int monthLength;
}
