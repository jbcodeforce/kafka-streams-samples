package ut.ibm.gse.eda.kstreams.lab1;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This is a simple example of loading some reference data into a ktable for
 * lookup
 */
public class TestLoadKtableFromTopic {
    private static TopologyTestDriver testDriver;
    private static String productTypesTopic = "product-types";
    private static String storeName = "product-types-store";

    private static TestInputTopic<String, String> inTopic;
    private static TestOutputTopic<String, Long> outTopic;
    private static TestOutputTopic<String, String> errorTopic;
    
    public static Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-lab3");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummmy:1234");
        return props;
    }
    
    @BeforeClass
    public static void buildTopology(){
        final StreamsBuilder builder = new StreamsBuilder();
        // Adding a state store is a simple matter of creating a StoreSupplier 
        // instance with one of the static factory methods on the Stores class.
        // all persistent StateStore instances provide local storage using RocksDB
        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore(storeName);
        StoreBuilder<KeyValueStore<String, String>> storeBuilder =
                        Stores.keyValueStoreBuilder(storeSupplier,
                                Serdes.String(),
                                Serdes.String());
        builder.addStateStore(storeBuilder);

        KTable<String, String> productTypeTable = builder.table(productTypesTopic, 
                Consumed.with(Serdes.String(), Serdes.String()));
        
        testDriver = new TopologyTestDriver(builder.build(), getStreamsConfig());
        inTopic = testDriver.createInputTopic(productTypesTopic, new StringSerializer(), new StringSerializer());
            
    }

    @Test
    public void shouldHaveSixProductTypes(){
        inTopic.pipeInput("PT01","Book");
        inTopic.pipeInput("PT02","Magazine");
        inTopic.pipeInput("PT03","Comic");
        inTopic.pipeInput("PT04","BoardGame");
        inTopic.pipeInput("PT05","Toy");
        inTopic.pipeInput("PT06","Disk");
       // testDriver.getStateStore(name)
        for ( StateStore s: testDriver.getAllStateStores().values()) {
            System.out.println(s.name());
        }
    }

    
}