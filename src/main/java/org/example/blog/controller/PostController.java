package org.example.blog.controller;

import org.example.blog.common.Result;
import org.example.blog.entity.Post;
import org.example.blog.repository.PostRepository;
import org.example.blog.service.MediaService;        // ✅ 新增
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    /* ⬇️ 改成构造器注入，删掉 @Autowired 字段 */
    private final PostRepository postRepository;
    private final MediaService mediaService;          // ✅ 新增

    public PostController(PostRepository postRepository,
                          MediaService mediaService) { // ✅ 新增参数
        this.postRepository = postRepository;
        this.mediaService = mediaService;             // ✅ 新增赋值
    }

    /**
     * 发贴（兼容纯文字 & 媒体）
     */
    @PostMapping
    public Result<?> create(@RequestBody Post dto, Authentication auth) {
        if (auth == null) {
            return Result.error("请先登录再发帖子");
        }
        Post p = new Post();
        p.setTitle(dto.getTitle());
        // ✅ 如果前端没传 content，就设一个默认内容
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            if (dto.getMediaKeys() != null && !dto.getMediaKeys().isBlank()) {
                p.setContent("这是一个媒体分享帖，点击查看详情。");
            } else {
                p.setContent("（暂无内容）");
            }
        } else {
            p.setContent(dto.getContent());
        }


        p.setAuthor(auth.getName());   // 当前登录用户名

        // ✅ 设置类型（图片、音频、视频或纯文本）
        if (dto.getMediaKeys() != null && !dto.getMediaKeys().isBlank()) {
            String first = dto.getMediaKeys().split(",")[0];
            if (first.endsWith("mp4")) {
                p.setPostType(Post.PostType.video);
            } else if (first.endsWith("mp3")) {
                p.setPostType(Post.PostType.audio);
            } else {
                p.setPostType(Post.PostType.image);
            }
        } else {
            p.setPostType(Post.PostType.text);
        }

        postRepository.save(p);        // 先保存，拿到 id

        /* ✅ 新增：把媒体记录绑定到本帖 */
        mediaService.bindToPost(p.getId(), dto.getMediaKeys());

        return Result.ok();            // 统一返回工具类
    }

    /**
     * 首页列表（最新在上）
     */
    @GetMapping
    public Result<List<Post>> list() {
        return Result.ok(postRepository.findAllWithMediasOrderByCreateTimeDesc());
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Post dto, Authentication auth) {
        Post p = postRepository.findById(id).orElseThrow();
        if (!p.getAuthor().equals(auth.getName())) return Result.error("只能修改自己的帖");
        p.setTitle(dto.getTitle());
        p.setContent(dto.getContent());
        postRepository.save(p);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, Authentication auth) {
        Post p = postRepository.findById(id).orElseThrow();
        if (!p.getAuthor().equals(auth.getName())) return Result.error("只能删除自己的帖");

        // ✅ 1. 先查出媒体记录
        var medias = mediaService.findByPostId(id);

        // ✅ 2. 先删数据库帖子
        postRepository.deleteById(id);

        // ✅ 3. 再删 MinIO 文件
        if (!medias.isEmpty()) {
            mediaService.deleteFromMinio(medias);
        }

        return Result.ok();
    }

}