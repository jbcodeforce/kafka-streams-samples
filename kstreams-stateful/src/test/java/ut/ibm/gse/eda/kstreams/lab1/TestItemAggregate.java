package ut.ibm.gse.eda.kstreams.lab1;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ibm.gse.eda.domain.Item;
import io.quarkus.kafka.client.serialization.JsonbSerde;

/**
 * input is itemSold events
 */
public class TestItemAggregate {
    private static TopologyTestDriver testDriver;
    private static String inTopicName = "itemSold";
    private static String resultTopicName = "itemCount";
    private static String storeName = "ItemSoldStore";
    private TestInputTopic<String, Item> inputTopic;
    private TestOutputTopic<String, Integer> outputTopic;
    private Serde<String> stringSerde = new Serdes.StringSerde();
    private Serde<Long> longSerde = new Serdes.LongSerde();
    private Serde<Integer> intSerde = new Serdes.IntegerSerde();
    private JsonbSerde<Item> itemSerde = new JsonbSerde<>(Item.class);
    private KeyValueBytesStoreSupplier itemSoldStore = Stores.persistentKeyValueStore(storeName);
      

    public  Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "item-aggregator");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummmy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,  Serdes.String().getClass().getName());
        return props;
    }


    /**
     * process item sale events, and aggregate per key
     */
    @BeforeEach
    public void setup() {
        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(inTopicName, Consumed.with(Serdes.String(), itemSerde))
            .map((k,v) ->  new KeyValue<>((String) k,(Integer) v.quantity))
            .groupByKey(Grouped.with(Serdes.String(), Serdes.Integer()))
            .reduce(Integer::sum,Materialized.as(itemSoldStore))
            .toStream()
            .to(resultTopicName);
        testDriver = new TopologyTestDriver(builder.build(), getStreamsConfig());
        inputTopic = testDriver.createInputTopic(inTopicName, stringSerde.serializer(),itemSerde.serializer());
        outputTopic = testDriver.createOutputTopic(resultTopicName, stringSerde.deserializer(), intSerde.deserializer());

    }

    @AfterEach
    public void tearDown() {
        try {
            testDriver.close();
        } catch (final Exception e) {
             System.out.println("Ignoring exception, test failing due this exception:" + e.getLocalizedMessage());
        } 
    }

    @Test
    public void shouldGetTwoItemSold(){
        //given an item is sold in a store
        Item item = new Item("Store-1","Item-1",2,33.2);
        inputTopic.pipeInput(item.itemCode, item);
        Assertions.assertFalse(outputTopic.isEmpty()); 
        Assertions.assertEquals(2, outputTopic.readKeyValue().value);
    }
    
    @Test
    public void shouldGetFiveItemsSoldOverMultipleStores(){
        //given an item is sold in a store
        Item itemSold1 = new Item("Store-1","Item-1",2,33.2);
        Item itemSold2 = new Item("Store-2","Item-1",3,30.2);
        inputTopic.pipeInput(itemSold1.itemCode, itemSold1);
        inputTopic.pipeInput(itemSold2.itemCode, itemSold2);
        Assertions.assertFalse(outputTopic.isEmpty()); 
        Assertions.assertEquals(2, outputTopic.readKeyValue().value);
        Assertions.assertEquals(5, outputTopic.readKeyValue().value);
        ReadOnlyKeyValueStore<String,Integer> keyValueStore = testDriver.getKeyValueStore(storeName);
        Assertions.assertEquals(5, keyValueStore.get("Item-1"));
    }
}