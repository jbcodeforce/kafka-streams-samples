package jbcodeforce.kafka.study.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * WeatherStation
 * 
 * By adding the @RegisterForReflection annotation, it is ensured 
 * that this type can be instantiated reflectively when running the 
 * application in native mode.
 */
@RegisterForReflection
public class WeatherStation {
    public int id;
    public String name;
    
}