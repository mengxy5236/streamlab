package com.franklintju.streamlab.upload;

import com.franklintju.streamlab.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "streamlab.transcode", name = "messaging-enabled", havingValue = "true")
public class TranscodeProducer {

    private final RabbitTemplate rabbitTemplate;
    @Qualifier("transcodeRetryScheduler")
    private final TaskScheduler retryScheduler;

    public void sendTranscodeMessage(TranscodeMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.VIDEO_TRANSCODE_EXCHANGE,
                    RabbitMqConfig.VIDEO_TRANSCODE_ROUTING_KEY,
                    message
            );
            log.info("Sent transcode message: uploadTaskId={}, videoId={}",
                    message.getUploadTaskId(), message.getVideoId());
        } catch (Exception e) {
            log.error("Failed to send transcode message: uploadTaskId={}", message.getUploadTaskId(), e);
            throw new RuntimeException("Failed to send transcode message: " + e.getMessage(), e);
        }
    }

    public void sendTranscodeMessageWithDelay(TranscodeMessage message) {
        long delayMs = calculateBackoff(message.getRetryCount());
        retryScheduler.schedule(() -> {
            try {
                sendTranscodeMessage(message);
                log.info("Scheduled retry sent: uploadTaskId={}, retryCount={}, delayMs={}",
                        message.getUploadTaskId(), message.getRetryCount(), delayMs);
            } catch (Exception e) {
                log.error("Failed to send scheduled retry: uploadTaskId={}", message.getUploadTaskId(), e);
                rabbitTemplate.convertAndSend(
                        RabbitMqConfig.VIDEO_TRANSCODE_DLQ_EXCHANGE,
                        RabbitMqConfig.VIDEO_TRANSCODE_DLQ_ROUTING_KEY,
                        message.withError(e.getMessage())
                );
            }
        }, Instant.now().plusMillis(delayMs));
    }

    private long calculateBackoff(int retryCount) {
        long baseDelay = 30_000L;
        return (long) (baseDelay * Math.pow(2, retryCount));
    }
}
