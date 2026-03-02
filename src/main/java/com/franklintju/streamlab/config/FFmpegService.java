package com.franklintju.streamlab.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FFmpegService {

    @Value("${ffmpeg.path:/usr/bin/ffmpeg}")
    private String ffmpegPath;

    @Value("${ffmpeg.ffprobe:/usr/bin/ffprobe}")
    private String ffprobePath;

    public double getDuration(Path videoPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(ffprobePath, "-v", "error", "-show_entries",
                    "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", videoPath.toString());
            Process p = pb.start();
            String output = new String(p.getInputStream().readAllBytes()).trim();
            p.waitFor();
            return Double.parseDouble(output);
        } catch (Exception e) {
            log.warn("获取视频时长失败: {}", videoPath, e);
            return 0;
        }
    }

    public void generateCover(Path videoPath, Path coverPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(ffmpegPath, "-i", videoPath.toString(),
                    "-ss", "00:00:01", "-vframes", "1", "-vf", "scale=320:-1",
                    coverPath.toString());
            pb.inheritIO().start().waitFor();
            log.info("截图生成成功: {}", coverPath);
        } catch (Exception e) {
            log.error("截图生成失败: {}", videoPath, e);
        }
    }

    public void transcodeToHLS(Path inputPath, Path outputDir, String quality) {
        try {
            Files.createDirectories(outputDir);
            String variantPlaylist = outputDir.resolve("index.m3u8").toString();

            List<String> cmd = new ArrayList<>();
            cmd.add(ffmpegPath);
            cmd.add("-i");
            cmd.add(inputPath.toString());
            cmd.add("-profile:v");
            cmd.add("baseline");
            cmd.add("-level");
            cmd.add("3.0");
            cmd.add("-start_number");
            cmd.add("0");
            cmd.add("-hls_time");
            cmd.add("10");
            cmd.add("-hls_list_size");
            cmd.add("0");
            cmd.add("-f");
            cmd.add("hls");
            cmd.add(variantPlaylist);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.inheritIO().start().waitFor();
            log.info("HLS转码成功: {}", outputDir);
        } catch (Exception e) {
            log.error("HLS转码失败: {}", inputPath, e);
        }
    }
}
