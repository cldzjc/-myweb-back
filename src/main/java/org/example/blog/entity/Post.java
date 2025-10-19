package org.example.blog.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void onCreate() {
        createTime = LocalDateTime.now();
    }

    // ✅ 正确的映射：一个帖子包含多个 media
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Media> medias = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", columnDefinition = "ENUM('text','image','audio','video')")
    private PostType postType = PostType.text;

    @Transient
    private String mediaKeys;   // 前端上传时用，不入库

    public enum PostType { text, image, audio, video }
}
