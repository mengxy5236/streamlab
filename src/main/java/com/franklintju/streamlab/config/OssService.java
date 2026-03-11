package com.franklintju.streamlab.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    public String uploadVideo(MultipartFile file) throws IOException {
        String key = "videos/" + UUID.randomUUID() + "/" + file.getOriginalFilename();
        return upload(key, file.getInputStream(), file.getSize(), file.getContentType());
    }

    public String uploadCover(MultipartFile file) throws IOException {
        String key = "covers/" + UUID.randomUUID() + "/" + file.getOriginalFilename();
        return upload(key, file.getInputStream(), file.getSize(), file.getContentType());
    }

    public String upload(File file) throws IOException {
        String ext = getFileExtension(file.getName());
        String key = "covers/" + UUID.randomUUID() + ext;
        String contentType = Files.probeContentType(file.toPath());
        return upload(key, new FileInputStream(file), file.length(), contentType);
    }

    public String uploadFile(File file, String key) throws IOException {
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return upload(key, new FileInputStream(file), file.length(), contentType);
    }

    private String getFileExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx > 0 ? filename.substring(idx) : "";
    }

    public String upload(String key, java.io.InputStream input, long size, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(size);
        metadata.setContentType(contentType);

        ossClient.putObject(ossConfig.getBucketName(), key, input, metadata);

        String url = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + key;
        log.info("OSS上传成功: {}", url);
        return url;
    }

    public void delete(String url) {
        String key = extractKey(url);
        ossClient.deleteObject(ossConfig.getBucketName(), key);
        log.info("OSS删除成功: {}", key);
    }

    public void downloadToFile(String url, File destination) throws IOException {
        String key = extractKey(url);
        var ossObject = ossClient.getObject(ossConfig.getBucketName(), key);
        
        try (InputStream input = ossObject.getObjectContent();
             java.io.FileOutputStream output = new java.io.FileOutputStream(destination)) {
            input.transferTo(output);
        }
        
        log.info("OSS下载成功: {} -> {}", url, destination.getPath());
    }

    public InputStream download(String url) {
        String key = extractKey(url);
        return ossClient.getObject(ossConfig.getBucketName(), key).getObjectContent();
    }

    private String extractKey(String url) {
        return url.replace("https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/", "");
    }
}
