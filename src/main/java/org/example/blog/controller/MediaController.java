package org.example.blog.controller;

import org.example.blog.common.Result;
import org.example.blog.entity.Media;
import org.example.blog.entity.Post;
import org.example.blog.service.MediaService;
import org.example.blog.repository.MediaRepository;
import org.example.blog.repository.PostRepository;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;



import java.security.Principal;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final S3Presigner s3Presigner;
    private final MediaRepository mediaRepository;
    private final MediaService mediaService;
    private final PostRepository postRepository;
    private final String bucket = "media";

    public MediaController(S3Presigner s3Presigner,
                           MediaRepository mediaRepository,
                           MediaService mediaService,
                           PostRepository postRepository) {
        this.s3Presigner = s3Presigner;
        this.mediaRepository = mediaRepository;
        this.mediaService = mediaService;
        this.postRepository = postRepository;
    }

    /* ---------- 1. 获取上传签名 ---------- */
    @PostMapping("/presign")
    public Map<String, Object> presign(@RequestBody PresignRequest req, Authentication authentication) {
        String username = "anon";
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
        }

        String key = String.format("users/%s/%s-%s", username, UUID.randomUUID(), req.filename);

        var putReq = software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
                .bucket(bucket).key(key).contentType(req.mimeType).build();
        var presignReq = software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15)).putObjectRequest(putReq).build();
        var presigned = s3Presigner.presignPutObject(presignReq);

        Map<String, Object> resp = new HashMap<>();
        resp.put("uploadUrl", presigned.url().toString());
        resp.put("objectKey", key);
        resp.put("expiresInSeconds", 15 * 60);
        return resp;
    }




    /* ---------- 2. 上传完成后写入数据库 ---------- */
    @PostMapping("/complete")
    public Result<Void> complete(@RequestBody CompleteReq req, Principal principal) {
        String username = (principal != null) ? principal.getName() : "anon";

        Media m = new Media();
        m.setObjectKey(req.objectKey);
        m.setUserId(getUserId(username));
        m.setMediaType(parseMediaType(req.mediaType));
        m.setMimeType(req.mediaType);
        m.setSizeBytes(req.sizeBytes);
        m.setPost(null);

        mediaRepository.save(m);
        return Result.okVoid();
    }

    /* ---------- 3. 创建带媒体的帖子 ---------- */
    @PostMapping("/post")
    public Result<Void> createMediaPost(
            @RequestParam String title,
            @RequestParam String mediaKeys,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String postType,  // ✅ 新增
            Principal principal) {

        String username = (principal != null) ? principal.getName() : "anon";

        Post p = new Post();
        p.setTitle(title);
        p.setContent(content == null ? "" : content);
        p.setAuthor(username);

        // ✅ 优先使用前端传的 postType
        if (postType != null && !postType.isEmpty()) {
            switch (postType) {
                case "video" -> p.setPostType(Post.PostType.video);
                case "audio" -> p.setPostType(Post.PostType.audio);
                case "image" -> p.setPostType(Post.PostType.image);
                default -> p.setPostType(Post.PostType.text);
            }
        } else {
            // 如果前端没传，再根据媒体类型决定
            String[] keys = mediaKeys.split(",");
            Optional<Media> firstMediaOpt = mediaRepository.findByObjectKey(keys[0]);
            if (firstMediaOpt.isPresent()) {
                Media firstMedia = firstMediaOpt.get();
                switch (firstMedia.getMediaType()) {
                    case video -> p.setPostType(Post.PostType.video);
                    case audio -> p.setPostType(Post.PostType.audio);
                    default -> p.setPostType(Post.PostType.image);
                }
            } else {
                p.setPostType(Post.PostType.image);
            }
        }

        postRepository.save(p);
        mediaService.bindToPost(p.getId(), mediaKeys);

        return Result.okVoid();
    }


    /* ---------- 4. 获取媒体预览地址 ---------- */
    @GetMapping("/preview")
    public Result<String> preview(@RequestParam String key) {
        var getReq = GetObjectRequest.builder().bucket(bucket).key(key).build();
        var presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(getReq)
                .build();
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignReq);
        return Result.ok(presigned.url().toString());
    }

    /* ---------- 工具方法 ---------- */
    private Long getUserId(String username) {
        // TODO: 从 UserRepository 查 ID；临时写死 1L
        return 1L;
    }

    private Media.MediaType parseMediaType(String type) {
        if (type == null) return Media.MediaType.image;
        type = type.toLowerCase();
        if (type.contains("video")) return Media.MediaType.video;
        if (type.contains("audio")) return Media.MediaType.audio;
        return Media.MediaType.image;
    }

    /* ---------- DTO ---------- */
    public static class PresignRequest {
        public String filename;
        public String mimeType;
        public String mediaType;
    }

    public static class CompleteReq {
        public String objectKey;
        public String filename;
        public String mediaType;
        public Long sizeBytes;
    }
}
