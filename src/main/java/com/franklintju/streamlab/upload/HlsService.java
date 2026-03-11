package com.franklintju.streamlab.upload;

import com.franklintju.streamlab.config.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class HlsService {

    private final OssService ossService;

    @Value("${upload.hls.segment-duration:10}")
    private int segmentDuration;

    @Value("${upload.hls.output-dir:${java.io.tmpdir}/hls}")
    private String outputDir;

    public record HlsResult(String hlsUrl, String resolution, int bitrate, int duration) {}

    public HlsResult convertToHls(String ossUrl, Long videoId) {
        Path workDir = null;
        
        try {
            workDir = Path.of(outputDir, videoId.toString(), UUID.randomUUID().toString());
            Files.createDirectories(workDir);
            
            // FFmpeg 直接读取 OSS URL
            // ffmpeg -i https://oss.../video.mp4 ...output.m3u8
            String inputSource = ossUrl;
            
            int duration = extractDuration(ossUrl);
            log.info("视频时长: {} 秒", duration);
            
            String datePath = LocalDate.now().toString().replace("-", "/");
            String hlsDir = "videos/hls/" + datePath + "/" + videoId;
            
            Path outputPath = workDir.resolve("index");
            
            // FFmpeg 命令: 输入是 OSS URL，输出是本地文件
            List<String> command = buildFfmpegCommand(inputSource, outputPath.toString());
            log.info("FFmpeg 命令: {}", String.join(" ", command));
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("frame=") || line.contains("time=")) {
                        log.debug("FFmpeg: {}", line);
                    }
                }
            }
            
            boolean finished = process.waitFor(30, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("FFmpeg 转码超时");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg 转码失败，退出码: " + exitCode);
            }
            
            List<Path> hlsFiles = findHlsFiles(workDir);
            if (hlsFiles.isEmpty()) {
                throw new RuntimeException("未生成 HLS 文件");
            }
            
            String hlsUrl = uploadHlsFiles(hlsFiles, hlsDir);
            
            log.info("HLS 转码完成: videoId={}, hlsUrl={}", videoId, hlsUrl);
            
            return new HlsResult(hlsUrl, "1920x1080", 2000, duration);
            
        } catch (IOException | InterruptedException e) {
            log.error("HLS 转码失败: videoId={}", videoId, e);
            throw new RuntimeException("HLS 转码失败: " + e.getMessage());
        } finally {
            if (workDir != null) {
                cleanupWorkDir(workDir);
            }
        }
    }

    public int extractDuration(String videoSource) {
        return extractDurationByPath(videoSource);
    }

    public int extractDuration(Path videoPath) {
        return extractDurationByPath(videoPath.toString());
    }

    private int extractDurationByPath(String source) {
        try {
            List<String> command = List.of(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                source
            );
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }
            
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (finished && process.exitValue() == 0) {
                String durationStr = output.toString().trim();
                double durationSec = Double.parseDouble(durationStr);
                return (int) Math.ceil(durationSec);
            }
            
            log.warn("无法提取视频时长，使用默认值");
            return 0;
            
        } catch (Exception e) {
            log.warn("视频时长提取失败: {}", e.getMessage());
            return 0;
        }
    }

    private List<String> buildFfmpegCommand(String input, String output) {
        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-i");
        cmd.add(input);
        cmd.add("-c:v");
        cmd.add("libx264");
        cmd.add("-preset");
        cmd.add("medium");
        cmd.add("-c:a");
        cmd.add("aac");
        cmd.add("-b:a");
        cmd.add("128k");
        cmd.add("-b:v");
        cmd.add("2000k");
        cmd.add("-maxrate");
        cmd.add("2500k");
        cmd.add("-bufsize");
        cmd.add("4000k");
        cmd.add("-hls_time");
        cmd.add(String.valueOf(segmentDuration));
        cmd.add("-hls_playlist_type");
        cmd.add("vod");
        cmd.add("-hls_segment_filename");
        cmd.add(output + "_%03d.ts");
        cmd.add("-f");
        cmd.add("hls");
        cmd.add(output + ".m3u8");
        return cmd;
    }

    private List<Path> findHlsFiles(Path dir) throws IOException {
        List<Path> files = new ArrayList<>();
        Files.walk(dir)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".m3u8") || p.toString().endsWith(".ts"))
            .forEach(files::add);
        return files;
    }

    private String uploadHlsFiles(List<Path> hlsFiles, String hlsDir) throws IOException {
        String baseUrl = null;
        
        for (Path file : hlsFiles) {
            String fileName = file.getFileName().toString();
            String ossKey = hlsDir + "/" + fileName;
            
            String url = ossService.uploadFile(file.toFile(), ossKey);
            
            if (fileName.endsWith(".m3u8")) {
                baseUrl = url;
            }
            
            log.info("HLS 文件上传: {} -> {}", fileName, url);
        }
        
        if (baseUrl == null) {
            throw new RuntimeException("未找到 .m3u8 文件");
        }
        
        return baseUrl;
    }

    private void cleanupWorkDir(Path workDir) {
        try {
            Files.walk(workDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        log.warn("删除临时文件失败: {}", p);
                    }
                });
        } catch (IOException e) {
            log.warn("清理工作目录失败: {}", workDir);
        }
    }

    public boolean isHlsSupported() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            Process p = pb.start();
            return p.waitFor(3, TimeUnit.SECONDS) && p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
