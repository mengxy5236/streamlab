package com.franklintju.streamlab.interaction;

import com.franklintju.streamlab.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendLikeEvent(LikeEventMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(KafkaConfig.LIKE_EVENT_TOPIC, jsonMessage);
            log.info("发送点赞事件成功: userId={}, videoId={}, eventType={}",
                    message.getUserId(), message.getVideoId(), message.getEventType());
        } catch (Exception e) {
            log.error("发送点赞事件失败: userId={}, videoId={}", message.getUserId(), message.getVideoId(), e);
        }
    }
}
