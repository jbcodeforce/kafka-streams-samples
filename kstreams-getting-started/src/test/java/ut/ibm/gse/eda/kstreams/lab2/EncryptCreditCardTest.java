package ut.ibm.gse.eda.kstreams.lab2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ibm.gse.eda.domain.Purchase;
import ibm.gse.eda.util.JSONSerde;

/**
 * 
 */
public class EncryptCreditCardTest {

    private static TopologyTestDriver testDriver;
    private static String inTopicName = "transactions";
    private static String outTopicName = "output";
    private static TestInputTopic<String, Purchase> inTopic;
    private static TestOutputTopic<String, Purchase> outTopic;

    public static Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-gs-2");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummmy:1234");
        return props;
    }


    @BeforeAll
    public static void buildTopology(){
        final StreamsBuilder builder = new StreamsBuilder();
        
        KStream<String,Purchase> purchaseStream = builder.stream(inTopicName, Consumed.with(Serdes.String(),
            new JSONSerde<Purchase>()))
            .mapValues(p -> Purchase.builder(p).maskCreditCard().build());

        purchaseStream.print(Printed.<String, Purchase>toSysOut().withLabel("encryptedPurchase"));

        purchaseStream.to(outTopicName, Produced.with(Serdes.String(), new JSONSerde<Purchase>()));

        testDriver = new TopologyTestDriver(builder.build(), getStreamsConfig());
        inTopic = testDriver.createInputTopic(inTopicName, new StringSerializer(), new JSONSerde<Purchase>());
        outTopic = testDriver.createOutputTopic(outTopicName,new StringDeserializer(), new JSONSerde<Purchase>());
    }

    @Test
    public void ccShouldGetEncrypted(){
        Purchase p = new Purchase();
        p.setCustomerId("CUST01");
        p.setCreditCardNumber("1234-3456789012");
        
        inTopic.pipeInput(p.getCustomerId(),p);
        assertThat(outTopic.isEmpty(), is(false));
        assertThat(outTopic.readValue().getCreditCardNumber(),equalTo("xxxx"));

    }
}