package com.JiuShuFangSecondHand.controller;

import cn.hutool.core.lang.UUID;
import com.JiuShuFangSecondHand.exception.BusinessException;
import com.JiuShuFangSecondHand.exception.UnauthorizedException;
import com.JiuShuFangSecondHand.factory.FileStorageFactory;
import com.JiuShuFangSecondHand.properties.MinIOConfigProperties;
import com.JiuShuFangSecondHand.util.Result;
import com.JiuShuFangSecondHand.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@Tag(name = "图片", description = "图片相关接口")
@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {

    private final FileStorageFactory fileStorageFactory;
    private final MinIOConfigProperties minIOConfigProperties;

    /**
     * 上传图片
     * @param file 头像文件
     * @return 头像 URL
     */
    @PostMapping("/picture/upload")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getUserId();

        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }

        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("请上传图片文件");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = UUID.fastUUID().toString(true) + extension;

            String url = fileStorageFactory.uploadImgFile(filename, file.getInputStream(), contentType);

            return Result.success(url);
        } catch (IOException e) {
            log.error("上传头像失败", e);
            throw new BusinessException("上传头像失败");
        }
    }

    @GetMapping("/book/upload/{year}/{month}/{day}/{filename}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String filename) {
        try {
            // 修复：直接拼接文件路径，不拼接 Endpoint，从根源解决解析错误
            String filePath = "壁纸1.png";
            // 构造标准 MinIO 访问路径
            String objectName = minIOConfigProperties.getEndpoint() + "/" + minIOConfigProperties.getBucket() + "/" + filePath;

            byte[] imageBytes = fileStorageFactory.downLoadFile(objectName);
            System.out.println(imageBytes);
            if (imageBytes == null || imageBytes.length == 0) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "image/jpeg";
            if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);

        } catch (Exception e) {
            log.error("获取图片失败：{}/{}/{}/{}", year, month, day, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{*filename}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable String filename) {
        System.out.println("开始下载文件");
        return ResponseEntity.ok(fileStorageFactory.downLoadFile(filename));
    }


}