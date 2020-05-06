package ut.ibm.gse.eda.kstreams.lab2;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Window;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Send an alert if we have less than 4 event per hour for a vessel positions.
 * The process has to wait until we are sure we won't see any more events within the specified window
 * and then generate alert.
 * It uses the suppress api to suppress intermediate events, and emit event when window is closed.
 */
public class TestSendAlertForNotEnoughEvent {
    
    private static TopologyTestDriver testDriver;
    private static String inTopicName = "in-topic";
    private static String outTopicName = "out-topic";
    private static String storeName = "transactionCountStore";
    private static TestInputTopic<String, String> inTopic;
    private static TestOutputTopic<String, String> outTopic;
    private static Instant startTime;

    public static Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-lab3");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummmy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        return props;
    }
    
    @BeforeAll
    /**
     * In Kafka Streams, windowed computations update their results continuously.
     * Here we want to create output event only on the final result of result of a windowed computation
     * https://kafka.apache.org/21/documentation/streams/developer-guide/dsl-api.html#window-final-results
     */
    public static void buildTopology() {
        final StreamsBuilder builder = new StreamsBuilder();

        KGroupedStream<String,String> groupedVessels =   builder.stream(
            inTopicName, 
            Consumed.with(Serdes.String(), Serdes.String())
        ).groupByKey();
        groupedVessels
            // every one hour window with +10 mn grace period
            .windowedBy(TimeWindows.of(Duration.ofHours(1)).grace(Duration.ofMinutes(10)))
            .count()
            .suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded()))
            .filter((windowedVesselId, count) -> count < 4)
            .toStream()
            .foreach((windowedVesselId, count) -> sendAlert(windowedVesselId.window(), 
                                                    windowedVesselId.key(), count));
        
        testDriver = new TopologyTestDriver(builder.build(), getStreamsConfig(),startTime);
        inTopic = testDriver.createInputTopic(inTopicName, new StringSerializer(), new StringSerializer());
        outTopic = testDriver.createOutputTopic(outTopicName, new StringDeserializer(), new StringDeserializer());
    }

    public static void sendAlert(Window w, String k, Long count) {
        
    }

    @Test 
    public void shouldHaveOneAlertAsThereIsNoEventWithinOneHour() {
        inTopic.pipeInput("ID_1", "{'vesselId': 'ID_1', 'lat': 123.00, 'long' : '45'}");
        Assertions.assertTrue(outTopic.isEmpty()); 
        inTopic.advanceTime(Duration.ofMinutes(10));
        inTopic.pipeInput("ID_1", "{'vesselId': 'ID_1', 'lat': 123.03, 'long' : '45'}");
        Assertions.assertTrue(outTopic.isEmpty()); 
        inTopic.advanceTime(Duration.ofMinutes(10));
        inTopic.pipeInput("ID_1", "{'vesselId': 'ID_1', 'lat': 123.05, 'long' : '45'}");
        inTopic.advanceTime(Duration.ofMinutes(10));
        inTopic.pipeInput("ID_1", "{'vesselId': 'ID_1', 'lat': 123.06, 'long' : '45'}");
        inTopic.advanceTime(Duration.ofMinutes(90));
        Assertions.assertFalse(outTopic.isEmpty()); 
        inTopic.pipeInput("ID_1", "{'vesselId': 'ID_1', 'lat': 123.20, 'long' : '45'}");
    }

}