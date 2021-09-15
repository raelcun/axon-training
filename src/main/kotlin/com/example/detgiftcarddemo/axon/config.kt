package com.example.detgiftcarddemo.axon

import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class AxonConfig {
    @Autowired
    fun processorConfig(configurer: EventProcessingConfigurer) {
        configurer.registerTrackingEventProcessorConfiguration {
            TrackingEventProcessorConfiguration.forParallelProcessing(50)
        }
    }
}