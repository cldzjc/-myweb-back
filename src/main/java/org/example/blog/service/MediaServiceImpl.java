package org.example.blog.service;

import org.example.blog.entity.Media;
import org.example.blog.repository.MediaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Arrays;
import java.util.List;

@Service
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final S3Client s3Client;           // ✅ 新增
    private final String bucket = "media";     // ✅ 你的 MinIO bucket

    // 构造器注入（保留原来的）
    public MediaServiceImpl(MediaRepository mediaRepository, S3Client s3Client) {
        this.mediaRepository = mediaRepository;
        this.s3Client = s3Client;
    }

    @Override
    @Transactional
    public void bindToPost(Long postId, String mediaKeys) {
        if (mediaKeys == null || mediaKeys.isBlank()) return;
        List<String> keys = Arrays.asList(mediaKeys.split(","));
        mediaRepository.updatePostIdByObjectKeys(postId, keys);
    }

    // ✅ 新增：查找与帖子关联的媒体
    @Override
    public List<Media> findByPostId(Long postId) {
        return mediaRepository.findByPostId(postId);
    }

    // ✅ 新增：删除 MinIO 文件
    @Override
    public void deleteFromMinio(List<Media> medias) {
        for (Media m : medias) {
            try {
                s3Client.deleteObject(b -> b.bucket(bucket).key(m.getObjectKey()));
                System.out.println("✅ 已从 MinIO 删除: " + m.getObjectKey());
            } catch (Exception e) {
                System.err.println("⚠️ 删除失败: " + m.getObjectKey() + " - " + e.getMessage());
            }
        }
    }
}
