package com.example.config;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

import java.time.Clock;

@Factory
public class ClockConfig {

    @Singleton
    public Clock clock() {
        return Clock.systemUTC();
    }
}
