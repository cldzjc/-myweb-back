package org.example.blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@NoArgsConstructor
@Entity
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mediaId;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 255, nullable = false)
    private String objectKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    private String mimeType;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
    private Integer durationSecond;
    private String thumbnailKey;
    private Boolean isPublic = true;

    @Column(updatable = false)
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    private java.time.LocalDateTime updatedAt = java.time.LocalDateTime.now();

    // ✅ 关键：这里是“多对一”关系（Media 多，Post 一）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")  // media 表中存储的外键列
    @com.fasterxml.jackson.annotation.JsonBackReference // 防止序列化循环
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    public enum MediaType { image, audio, video }
}
