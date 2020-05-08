package  jbcodeforce.kafka.study.streams;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jbcodeforce.kafka.study.domain.Aggregation;
import jbcodeforce.kafka.study.domain.WeatherStationData;
import jbcodeforce.kafka.study.rest.WeatherStationDataResult;

@ApplicationScoped
public class InteractiveQueries {
    private static final Logger LOG = Logger.getLogger(InteractiveQueries.class.getName());
    
    @ConfigProperty(name = "hostname")
    String host;
    
    @Inject
    KafkaStreams streams;

    /**
     * Support multiples distributed stores. 
     * From the streams get the metadata about the distributed stores.
     */
	public WeatherStationDataResult getWeatherStationData(int id) {
        StreamsMetadata metadata = streams.metadataForKey(                  
            TopologyProducer.WEATHER_STATIONS_STORE,
            id,
            Serdes.Integer().serializer()
        );
        if (metadata == null || metadata == StreamsMetadata.NOT_AVAILABLE) {
            LOG.warning("Found no metadata for key " + id);
            return WeatherStationDataResult.notFound();
        }
        else if (metadata.host().equals(host)) {                            
            LOG.info("Found data for key " + id + " locally");
            Aggregation result = getWeatherStationStore().get(id);
    
            if (result != null) {
                return WeatherStationDataResult.found(WeatherStationData.from(result));
            }
            else {
                return WeatherStationDataResult.notFound();
            }
        }
        else {                                                              
            LOG.info(
                "Found data for key " + id + " on remote host " + 
                metadata.host() + ":" + metadata.port()
            );
            return WeatherStationDataResult.foundRemotely(metadata.host(), metadata.port());
        }
    }
    
    public List<PipelineMetadata> getMetaData() {
        return streams.allMetadataForStore(TopologyProducer.WEATHER_STATIONS_STORE)
                .stream()
                .map(m -> new PipelineMetadata(
                        m.hostInfo().host() + ":" + m.hostInfo().port(),
                        m.topicPartitions()
                                .stream()
                                .map(TopicPartition::toString)
                                .collect(Collectors.toSet())))
                .collect(Collectors.toList());
    }
    
    private ReadOnlyKeyValueStore<Integer, Aggregation> getWeatherStationStore() {
        while (true) {
            try {
                return streams.store(TopologyProducer.WEATHER_STATIONS_STORE, QueryableStoreTypes.keyValueStore());
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }
}