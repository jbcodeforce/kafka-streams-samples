package jbcodeforce.kafka.study.rest;

import java.util.Optional;
import java.util.OptionalInt;

import jbcodeforce.kafka.study.domain.WeatherStationData;

public class WeatherStationDataResult {
    private static WeatherStationDataResult NOT_FOUND =
            new WeatherStationDataResult(null);

    private final WeatherStationData result;
    private final String host;
    private final Integer port;

    private WeatherStationDataResult(WeatherStationData result) {
        this.result = result;
        this.host = "localhost";
        this.port = 9080;
    }

    private WeatherStationDataResult(WeatherStationData result, 
                    String host,
                    Integer port) {
        this.result = result;
        this.host = host;
        this.port = port;
    }

    public static WeatherStationDataResult found(WeatherStationData data) {
        return new WeatherStationDataResult(data);
    }

    public static WeatherStationDataResult notFound() {
        return NOT_FOUND;
    }

    public Optional<WeatherStationData> getResult() {
        return Optional.ofNullable(result);
    }

    public static WeatherStationDataResult foundRemotely(String host, int port) {
        return new WeatherStationDataResult(null, host, port);
    }

    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    public OptionalInt getPort() {
        return port != null ? OptionalInt.of(port) : OptionalInt.empty();
    }
}