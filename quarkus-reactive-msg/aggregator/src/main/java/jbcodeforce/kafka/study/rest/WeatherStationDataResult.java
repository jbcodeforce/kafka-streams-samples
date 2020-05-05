package jbcodeforce.kafka.study.rest;

import java.util.Optional;

import jbcodeforce.kafka.study.domain.WeatherStationData;

public class WeatherStationDataResult {
    private static WeatherStationDataResult NOT_FOUND =
            new WeatherStationDataResult(null);

    private final WeatherStationData result;

    private WeatherStationDataResult(WeatherStationData result) {
        this.result = result;
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
}