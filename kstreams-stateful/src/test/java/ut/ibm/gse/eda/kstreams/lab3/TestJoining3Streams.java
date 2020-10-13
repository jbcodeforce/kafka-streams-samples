package ut.ibm.gse.eda.kstreams.lab3;

import org.apache.kafka.streams.Topology;

/**
 * This demonstration highlights how to merge 3 streams into one to do: - data
 * enrichement from reference data - data transformation by merging data
 * 
 * This represents a classical use case of data pipeline with CDC generating
 * events from three different tables: - products - shipments: includes static
 * information on where to ship the ordered product - shipmentReferences:
 * includes detailed about the shipment routes, legs and costs
 * 
 * and the goal is to build a shipmentEnriched object to be send to a data lake
 * for at rest analytics.
 * 
 * This process is done in batch mode, but moving to a CDC -> streams -> data
 * lake pipeline brings a lot of visibility to the shipment process and help to
 * have a real time view of aggregated object, that can be used by new event
 * driven services.
 */
public class TestJoining3Streams {
    
    public Topology buildProcess(){
        // stream from products
        return null;
    }
}