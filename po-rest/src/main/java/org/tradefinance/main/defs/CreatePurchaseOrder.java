package org.tradefinance.porest.defs;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.tradefinance.assets.defs.Party;

@XmlRootElement
public class CreatePurchaseOrder {

    @XmlElement
    public Party buyer;

    @XmlElement
    public Party seller;

    @XmlElement
    public double price;

    @XmlElement
    public int units;

    @XmlElement
    public long productGtin;
}
