package com.franklintju.streamlab.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String VIDEO_TRANSCODE_TOPIC = "video-transcode";
    public static final String LIKE_EVENT_TOPIC = "like-events";
    public static final String COMMENT_LIKE_EVENT_TOPIC = "comment-like-events";
    public static final String VIEW_EVENT_TOPIC = "view-events";
    public static final String NOTIFICATION_TOPIC = "notifications";

    public static final String TRANSCODE_DLQ = "video-transcode-dlq";
    public static final String LIKE_EVENT_DLQ = "like-events-dlq";
    public static final String COMMENT_LIKE_EVENT_DLQ = "comment-like-events-dlq";
    public static final String VIEW_EVENT_DLQ = "view-events-dlq";

    @Bean
    public NewTopic videoTranscodeTopic() {
        return TopicBuilder.name(VIDEO_TRANSCODE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic likeEventTopic() {
        return TopicBuilder.name(LIKE_EVENT_TOPIC)
                .partitions(5)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic commentLikeEventTopic() {
        return TopicBuilder.name(COMMENT_LIKE_EVENT_TOPIC)
                .partitions(5)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic viewEventTopic() {
        return TopicBuilder.name(VIEW_EVENT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transcodeDlqTopic() {
        return TopicBuilder.name(TRANSCODE_DLQ).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic likeEventDlqTopic() {
        return TopicBuilder.name(LIKE_EVENT_DLQ).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic commentLikeEventDlqTopic() {
        return TopicBuilder.name(COMMENT_LIKE_EVENT_DLQ).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic viewEventDlqTopic() {
        return TopicBuilder.name(VIEW_EVENT_DLQ).partitions(1).replicas(1).build();
    }
}
