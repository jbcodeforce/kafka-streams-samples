package ibm.gse.eda.api;

import javax.ws.rs.GET;

import ibm.gse.eda.domain.shipment.EnrichedShipment;
import io.smallrye.mutiny.Uni;

public class ShipmentResource {

    @GET
    public Uni<EnrichedShipment> getShipmentById(String id){
        return null;
    }
}