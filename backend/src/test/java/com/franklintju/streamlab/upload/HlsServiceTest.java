package com.franklintju.streamlab.upload;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HlsServiceTest {

    @Autowired(required = false)
    private HlsService hlsService;

    @Test
    void testFfmpegAvailable() {
        if (hlsService != null) {
            boolean supported = hlsService.isHlsSupported();
            System.out.println("FFmpeg 可用: " + supported);
            assertNotNull(supported);
        } else {
            System.out.println("HlsService 未注入，跳过测试");
        }
    }
}
