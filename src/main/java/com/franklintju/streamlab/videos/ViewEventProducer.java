package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendViewEvent(ViewEventMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(KafkaConfig.VIEW_EVENT_TOPIC, jsonMessage);
            log.debug("发送播放事件: videoId={}, userId={}", message.getVideoId(), message.getUserId());
        } catch (Exception e) {
            log.error("发送播放事件失败: videoId={}", message.getVideoId(), e);
        }
    }
}
