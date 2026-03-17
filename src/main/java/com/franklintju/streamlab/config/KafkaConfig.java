package com.franklintju.streamlab.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String VIDEO_TRANSCODE_TOPIC = "video-transcode";

    @Bean
    public NewTopic videoTranscodeTopic() {
        return TopicBuilder.name(VIDEO_TRANSCODE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
