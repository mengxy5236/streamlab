package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentLikeEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendCommentLikeEvent(CommentLikeEventMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(KafkaConfig.COMMENT_LIKE_EVENT_TOPIC, jsonMessage);
            log.info("发送评论点赞事件: userId={}, commentId={}, eventType={}",
                    message.getUserId(), message.getCommentId(), message.getEventType());
        } catch (Exception e) {
            log.error("发送评论点赞事件失败: userId={}, commentId={}",
                    message.getUserId(), message.getCommentId(), e);
        }
    }
}
