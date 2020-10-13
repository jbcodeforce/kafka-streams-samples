package ibm.gse.eda.domain.shipment;

import java.util.List;

public class EnrichedShipment {
    public String shipmentId;
    public String sourceLocation;
    public String targetLocation;
    public Double cost;
    public String productName;
    public String productCategory;
    public List<String> legs;

    public EnrichedShipment() {
        super();
    }
}