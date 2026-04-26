package com.franklintju.streamlab.upload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.franklintju.streamlab.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscodeProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendTranscodeMessage(TranscodeMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(KafkaConfig.VIDEO_TRANSCODE_TOPIC, jsonMessage);
            log.info("发送转码消息成功: uploadTaskId={}, videoId={}",
                    message.getUploadTaskId(), message.getVideoId());
        } catch (Exception e) {
            log.error("发送转码消息失败", e);
            throw new RuntimeException("发送转码消息失败: " + e.getMessage(), e);
        }
    }

    public void sendTranscodeMessageWithDelay(TranscodeMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            long delayMs = calculateBackoff(message.getRetryCount());

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(KafkaConfig.VIDEO_TRANSCODE_TOPIC, jsonMessage);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("延迟转码消息发送失败: uploadTaskId={}", message.getUploadTaskId(), ex);
                } else {
                    log.info("延迟重试转码消息已发送: uploadTaskId={}, videoId={}, retryCount={}, delayMs={}",
                            message.getUploadTaskId(), message.getVideoId(), message.getRetryCount(), delayMs);
                }
            });
        } catch (Exception e) {
            log.error("延迟重试转码消息发送异常: uploadTaskId={}", message.getUploadTaskId(), e);
        }
    }

    private long calculateBackoff(int retryCount) {
        long baseDelay = 30_000L;
        return (long) (baseDelay * Math.pow(2, retryCount));
    }
}
