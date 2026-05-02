package com.franklintju.streamlab.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ConditionalOnProperty(prefix = "streamlab.transcode", name = "messaging-enabled", havingValue = "true")
public class RabbitMqConfig {

    public static final String VIDEO_TRANSCODE_EXCHANGE = "video.transcode.exchange";
    public static final String VIDEO_TRANSCODE_QUEUE = "video.transcode.queue";
    public static final String VIDEO_TRANSCODE_ROUTING_KEY = "video.transcode";

    public static final String VIDEO_TRANSCODE_DLQ_EXCHANGE = "video.transcode.dlq.exchange";
    public static final String VIDEO_TRANSCODE_DLQ = "video.transcode.dlq";
    public static final String VIDEO_TRANSCODE_DLQ_ROUTING_KEY = "video.transcode.dlq";

    @Bean
    public DirectExchange videoTranscodeExchange() {
        return new DirectExchange(VIDEO_TRANSCODE_EXCHANGE, true, false);
    }

    @Bean
    public Queue videoTranscodeQueue() {
        return new Queue(VIDEO_TRANSCODE_QUEUE, true);
    }

    @Bean
    public Binding videoTranscodeBinding() {
        return BindingBuilder.bind(videoTranscodeQueue())
                .to(videoTranscodeExchange())
                .with(VIDEO_TRANSCODE_ROUTING_KEY);
    }

    @Bean
    public DirectExchange videoTranscodeDlqExchange() {
        return new DirectExchange(VIDEO_TRANSCODE_DLQ_EXCHANGE, true, false);
    }

    @Bean
    public Queue videoTranscodeDlq() {
        return new Queue(VIDEO_TRANSCODE_DLQ, true);
    }

    @Bean
    public Binding videoTranscodeDlqBinding() {
        return BindingBuilder.bind(videoTranscodeDlq())
                .to(videoTranscodeDlqExchange())
                .with(VIDEO_TRANSCODE_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean(name = "transcodeRetryScheduler")
    public TaskScheduler transcodeRetryScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("transcode-retry-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }
}
