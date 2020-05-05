package ut.ibm.gse.eda.kstreams.lab1;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestKStreamTestDriverBase {
    private TopologyTestDriver testDriver;
    private TestInputTopic<Long, String> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;

    private final Instant recordBaseTime = Instant.parse("2019-06-01T10:00:00Z");
    private final Duration advance1Min = Duration.ofMinutes(1);

    private final Serde<String> stringSerde = new Serdes.StringSerde();
    private final Serde<Long> longSerde = new Serdes.LongSerde();
    public static final String INPUT_TOPIC = "simple-stream-input";
    public static final String OUTPUT_TOPIC = "simple-stream-output";

    public  Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "mapping-stream-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        return props;
    }

    @Before
    public void setup() {
        final StreamsBuilder builder = new StreamsBuilder();
        //Create Actual Stream Processing pipeline
        final KStream<Long, String> source = builder.stream(INPUT_TOPIC, Consumed.with(longSerde, stringSerde));
        final KStream<String, Long> mapped = source.map((key, value) -> new KeyValue<>(value, key));
        mapped.to(OUTPUT_TOPIC, Produced.with(stringSerde, longSerde));
        testDriver = new TopologyTestDriver(builder.build(), getStreamsConfig());
        inputTopic = testDriver.createInputTopic(INPUT_TOPIC, new LongSerializer(), new StringSerializer(), 
                recordBaseTime, advance1Min);
        outputTopic = testDriver.createOutputTopic(OUTPUT_TOPIC, new StringDeserializer(), new LongDeserializer());
    }


    @After
    public void tearDown() {
        try {
            testDriver.close();
        } catch (final Exception e) {
             System.out.println("Ignoring exception, test failing due this exception:" + e.getLocalizedMessage());
        } 
    }


    @Test
    public void testOneWord() {
        //Feed word "Hello" to inputTopic and a kafka key, timestamp is irrelevant in this case
        inputTopic.pipeInput(1L,"Hello");
        assertThat(outputTopic.readValue(),equalTo(1L));
        //No more record in output topic
        assertThat(outputTopic.isEmpty(), is(true));
    }

    
}