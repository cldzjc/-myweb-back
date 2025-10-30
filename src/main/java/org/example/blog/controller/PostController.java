package org.example.blog.controller;

import org.example.blog.common.Result;
import org.example.blog.entity.Post;
import org.example.blog.entity.User;
import org.example.blog.repository.PostRepository;
import org.example.blog.repository.UserRepository;   // ✅ 新增
import org.example.blog.service.MediaService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final MediaService mediaService;
    private final UserRepository userRepository;      // ✅ 新增

    public PostController(PostRepository postRepository,
                          MediaService mediaService,
                          UserRepository userRepository) {  // ✅ 新增参数
        this.postRepository = postRepository;
        this.mediaService = mediaService;
        this.userRepository = userRepository;          // ✅ 新增赋值
    }

    /**
     * 发贴（兼容纯文字 & 媒体）
     */
    @PostMapping
    public Result<?> create(@RequestBody Post dto, Authentication auth) {
        if (auth == null) {
            return Result.error("请先登录再发帖子");
        }

        // ✅ 新增：通过邮箱找到用户，拿到 username
        String loginEmail = auth.getName();
        User user = userRepository.findByEmail(loginEmail);
        if (user == null) {
            return Result.error("无法找到当前登录用户");
        }

        Post p = new Post();
        p.setTitle(dto.getTitle());
        p.setAuthor(user.getUsername());  // ✅ 改为昵称（用户名），不再是邮箱

        // ✅ 内容处理
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            if (dto.getMediaKeys() != null && !dto.getMediaKeys().isBlank()) {
                p.setContent("这是一个媒体分享帖，点击查看详情。");
            } else {
                p.setContent("（暂无内容）");
            }
        } else {
            p.setContent(dto.getContent());
        }

        // ✅ 类型判断
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

        postRepository.save(p);
        mediaService.bindToPost(p.getId(), dto.getMediaKeys());
        return Result.ok();
    }

    /**
     * 首页列表
     */
    @GetMapping
    public Result<List<Post>> list() {
        return Result.ok(postRepository.findAllWithMediasOrderByCreateTimeDesc());
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Post dto, Authentication auth) {
        Post p = postRepository.findById(id).orElseThrow();

        // 获取当前登录用户的用户名
        User user = userRepository.findByEmail(auth.getName());
        if (user == null || !p.getAuthor().equals(user.getUsername())) {
            return Result.error("只能修改自己的帖");
        }

        p.setTitle(dto.getTitle());
        p.setContent(dto.getContent());
        postRepository.save(p);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, Authentication auth) {
        Post p = postRepository.findById(id).orElseThrow();

        // 获取当前登录用户的用户名
        User user = userRepository.findByEmail(auth.getName());
        if (user == null || !p.getAuthor().equals(user.getUsername())) {
            return Result.error("只能删除自己的帖");
        }

        // 删除帖子的媒体和帖子
        var medias = mediaService.findByPostId(id);
        postRepository.deleteById(id);
        if (!medias.isEmpty()) {
            mediaService.deleteFromMinio(medias);
        }

        return Result.ok();
    }

}
