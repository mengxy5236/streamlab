package com.franklintju.streamlab.upload;

import com.franklintju.streamlab.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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
}
