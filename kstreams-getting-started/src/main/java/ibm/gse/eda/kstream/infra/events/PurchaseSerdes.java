package ibm.gse.eda.kstream.infra.events;

import org.apache.kafka.common.serialization.Serde;

import ibm.gse.eda.kstream.domain.Purchase;

public class PurchaseSerdes {

    public static Serde<Purchase> PurchaseSerde() {
        return new JSONSerde<Purchase>(Purchase.class.getCanonicalName());
    }

}
