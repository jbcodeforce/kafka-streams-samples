package jbcodeforce.kafka.study.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * used to keep track of the aggregated values while the events are processed in
 * the streaming pipeline.
 * 
 *  By adding the @RegisterForReflection annotation, it is ensured 
 * that this type can be instantiated reflectively when running the 
 * application in native mode.
 */
@RegisterForReflection
public class Aggregation {
    public int stationId;
    public String stationName;
    public double min = Double.MAX_VALUE;
    public double max = Double.MIN_VALUE;
    public int count;
    public double sum;
    public double avg;

    public Aggregation updateFrom(TemperatureMeasurement measurement) {
        stationId = measurement.stationId;
        stationName = measurement.stationName;

        count++;
        sum += measurement.value;
        avg = BigDecimal.valueOf(sum / count)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        min = Math.min(min, measurement.value);
        max = Math.max(max, measurement.value);

        return this;
    }
}