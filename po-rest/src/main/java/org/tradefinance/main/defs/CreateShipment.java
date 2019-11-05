package org.tradefinance.porest.defs;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CreateShipment {

    @XmlElement
    public String purchaseOrderId;

    @XmlElement
    public int units;

    @XmlElement
    public String senderId;

    @XmlElement
    public String receiverId;
}
