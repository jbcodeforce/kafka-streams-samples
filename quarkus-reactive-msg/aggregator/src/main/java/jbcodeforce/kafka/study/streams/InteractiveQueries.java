package  jbcodeforce.kafka.study.streams;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import jbcodeforce.kafka.study.domain.Aggregation;
import jbcodeforce.kafka.study.domain.WeatherStationData;
import jbcodeforce.kafka.study.rest.WeatherStationDataResult;

@ApplicationScoped
public class InteractiveQueries {

    @Inject
    KafkaStreams streams;

	public WeatherStationDataResult getWeatherStationData(int id) {
		Aggregation result = getWeatherStationStore().get(id);

        if (result != null) {
            return WeatherStationDataResult.found(WeatherStationData.from(result)); 
        }
        else {
            return WeatherStationDataResult.notFound();                             
        }
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