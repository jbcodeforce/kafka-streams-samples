package jbcodeforce.kafka.study.streams;

import java.time.Instant;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;

import io.quarkus.kafka.client.serialization.JsonbSerde;
import jbcodeforce.kafka.study.domain.Aggregation;
import jbcodeforce.kafka.study.domain.TemperatureMeasurement;
import jbcodeforce.kafka.study.domain.WeatherStation;

/**
 * declare a CDI producer method which returns the Kafka Streams Topology; the
 * Quarkus extension will take care of configuring, starting and stopping the
 * actual Kafka Streams engine
 * 
 * The weather-stations table is read into a GlobalKTable, representing the current state of each weather station
 * The temperature-values topic is read into a KStream; whenever a new message arrives to this topic, the pipeline will be processed for this measurement
 * The results of the pipeline are written out to the temperatures-aggregated topic
 */
@ApplicationScoped
public class TopologyProducer {
    static final String WEATHER_STATIONS_STORE = "weather-stations-store";

    private static final String WEATHER_STATIONS_TOPIC = "weather-stations";
    private static final String TEMPERATURE_VALUES_TOPIC = "temperature-values";
    private static final String TEMPERATURES_AGGREGATED_TOPIC = "temperatures-aggregated";

    @Produces
    /**
     * The message from the temperature-values topic is joined with the corresponding weather station, 
     * using the topic’s key (weather station id); the join result contains the data from the 
     * measurement and associated weather station message.
     * The values are grouped by message key (the weather station id)
     * Within each group, all the measurements of that station are aggregated, by keeping track of 
     * minimum and maximum values and calculating the average value of all measurements of that station 
     * The results of the pipeline are written out to the temperatures-aggregated topic
     */
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        JsonbSerde<WeatherStation> weatherStationSerde = new JsonbSerde<>(
                WeatherStation.class);
        JsonbSerde<Aggregation> aggregationSerde = new JsonbSerde<>(Aggregation.class);

        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore(
                WEATHER_STATIONS_STORE);

        GlobalKTable<Integer, WeatherStation> stations = builder.globalTable( 
                WEATHER_STATIONS_TOPIC,
                Consumed.with(Serdes.Integer(), weatherStationSerde));

        builder.stream(                                                       
                        TEMPERATURE_VALUES_TOPIC,
                        Consumed.with(Serdes.Integer(), Serdes.String())
                )
                .join(                                                        
                        stations,
                        (stationId, timestampAndValue) -> stationId,
                        (timestampAndValue, station) -> {
                            String[] parts = timestampAndValue.split(";");
                            return new TemperatureMeasurement(station.id, station.name,
                                    Instant.parse(parts[0]), Double.valueOf(parts[1]));
                        }
                )
                .groupByKey()                                                 
                .aggregate(                                                   
                        Aggregation::new,
                        (stationId, value, aggregation) -> aggregation.updateFrom(value),
                        Materialized.<Integer, Aggregation> as(storeSupplier)
                            .withKeySerde(Serdes.Integer())
                            .withValueSerde(aggregationSerde)
                )
                .toStream()
                .to(                                                          
                        TEMPERATURES_AGGREGATED_TOPIC,
                        Produced.with(Serdes.Integer(), aggregationSerde)
                );

        return builder.build();
    }
}