package com.JiuShuFang.goods.factory.impl;

import cn.hutool.core.util.StrUtil;
import com.JiuShuFang.goods.config.MinIOConfig;
import com.JiuShuFang.goods.factory.FileStorageFactory;
import com.JiuShuFang.goods.properties.MinIOConfigProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@EnableConfigurationProperties(MinIOConfigProperties.class)
@Import(MinIOConfig.class)
@Component
@RequiredArgsConstructor
public class MinIOFileStorageFactoryImpl implements FileStorageFactory {

    private final MinioClient minioClient;
    private final MinIOConfigProperties minIOConfigProperties;
    private static final String separator = "/";

    public String builderFilePath(String dirPath, String filename) {
        StringBuilder stringBuilder = new StringBuilder(50);
        if (StrUtil.isNotBlank(dirPath)) {
            stringBuilder.append(dirPath).append(separator);
        }
        return stringBuilder.append(builderFilePath(filename)).toString();
    }

    public String builderFilePath(String filename) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate now = LocalDate.now();
        String format = dateTimeFormatter.format(now);
        return format + separator + filename;
    }

    @Override
    public String uploadImgFile(String prefix, String filename, InputStream inputStream) {
        String filePath = builderFilePath(prefix, filename);
        return uploadDefaultFileAndReturnUrl(inputStream, filePath);
    }

    @Override
    public String uploadImgFile(String prefix, String filename, InputStream inputStream, String contentType) {
        String filePath = builderFilePath(prefix, filename);
        return uploadImgFileAndReturnUrl(inputStream, filePath, contentType);
    }

    @NotNull
    private String uploadImgFileAndReturnUrl(InputStream inputStream, String filePath, String contentType) {
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType(contentType)
                    .bucket(minIOConfigProperties.getBucket())
                    .stream(inputStream, -1, 1024 * 1024 * 10)
                    .build();
            minioClient.putObject(putObjectArgs);
            return minIOConfigProperties.getReadPath() + separator + minIOConfigProperties.getBucket() + separator + filePath;
        } catch (Exception ex) {
            log.error("minio上传文件异常", ex);
            throw new RuntimeException("上传文件失败");
        }
    }

    @NotNull
    private String uploadDefaultFileAndReturnUrl(InputStream inputStream, String filePath) {
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType("image/jpg")
                    .bucket(minIOConfigProperties.getBucket())
                    .stream(inputStream, -1, 1024 * 1024 * 10)
                    .build();
            minioClient.putObject(putObjectArgs);
            return minIOConfigProperties.getReadPath() + separator + minIOConfigProperties.getBucket() + separator + filePath;
        } catch (Exception ex) {
            log.error("minio上传文件异常", ex);
            throw new RuntimeException("上传文件失败");
        }
    }

    @Override
    public String uploadImgFile(String filename, InputStream inputStream, String contentType) {
        String filePath = builderFilePath(filename);
        return uploadImgFileAndReturnUrl(inputStream, filePath, contentType);
    }

    @Override
    public String uploadImgFile(String filename, InputStream inputStream) {
        String filePath = builderFilePath(filename);
        return uploadDefaultFileAndReturnUrl(inputStream, filePath);
    }

    @Override
    public String uploadHtmlFile(String prefix, String filename, InputStream inputStream) {
        String filePath = builderFilePath(prefix, filename);
        return uploadHtmlFileAndReturnUrl(inputStream, filePath);
    }

    @NotNull
    private String uploadHtmlFileAndReturnUrl(InputStream inputStream, String filePath) {
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType("text/html")
                    .bucket(minIOConfigProperties.getBucket())
                    .stream(inputStream, -1, 1024 * 1024 * 10)
                    .build();
            minioClient.putObject(putObjectArgs);
            return minIOConfigProperties.getReadPath() + separator + minIOConfigProperties.getBucket() + separator + filePath;
        } catch (Exception ex) {
            log.error("minio上传文件异常", ex);
            throw new RuntimeException("上传文件失败");
        }
    }

    public String uploadHtmlFile(String filename, InputStream inputStream) {
        String filePath = builderFilePath(filename);
        return uploadHtmlFileAndReturnUrl(inputStream, filePath);
    }

    @Override
    public void delete(String pathUrl) {
        if (StrUtil.isBlank(pathUrl)) {
            throw new RuntimeException("路径不能为空");
        }
        try {
            // 强制使用配置的bucket，不解析路径，彻底避免-1越界
            String bucket = minIOConfigProperties.getBucket();
            String key = pathUrl.replace(minIOConfigProperties.getEndpoint() + "/" + bucket + "/", "");
            key = key.replaceAll("/+", "/");

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .build());
        } catch (Exception e) {
            log.error("minio删除文件失败", e);
            throw new RuntimeException("删除文件失败");
        }
    }

    @Override
    public byte[] downLoadFile(String pathUrl) {
        if (StrUtil.isBlank(pathUrl)) {
            return new byte[0];
        }
        try {
            String bucket = minIOConfigProperties.getBucket();
            // 直接提取文件路径
            String filePath = pathUrl.replace(minIOConfigProperties.getEndpoint() + "/" + bucket + "/", "");
            filePath = filePath.replaceAll("/+", "/");

            try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build())) {

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buff = new byte[1024];
                int rc;
                while ((rc = inputStream.read(buff)) != -1) {
                    byteArrayOutputStream.write(buff, 0, rc);
                }
                return byteArrayOutputStream.toByteArray();
            }
        } catch (Exception e) {
            log.error("minio下载文件失败 pathUrl:{}", pathUrl, e);
            return new byte[0];
        }
    }
}