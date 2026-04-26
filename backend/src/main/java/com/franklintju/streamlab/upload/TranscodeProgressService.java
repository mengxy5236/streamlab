package com.franklintju.streamlab.upload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscodeProgressService {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String TOPIC_PREFIX = "/topic/transcode/";

    public void sendProgress(TranscodeProgressMessage progress) {
        String destination = TOPIC_PREFIX + progress.getVideoId();
        messagingTemplate.convertAndSend(destination, progress);
        log.debug("发送转码进度: videoId={}, progress={}", progress.getVideoId(), progress.getProgress());
    }

    public void sendProgress(Long videoId, Integer progress, String status, String message) {
        TranscodeProgressMessage progressMessage = TranscodeProgressMessage.builder()
                .videoId(videoId)
                .progress(progress)
                .status(status)
                .message(message)
                .build();
        sendProgress(progressMessage);
    }
}
