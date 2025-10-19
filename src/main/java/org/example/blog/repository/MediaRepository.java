package org.example.blog.repository;

import org.example.blog.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    /**
     * 批量绑定帖子 ID
     */
    @Modifying
    @Query("UPDATE Media m SET m.post.id = :postId WHERE m.objectKey IN :keys")
    void updatePostIdByObjectKeys(@Param("postId") Long postId,
                                  @Param("keys") List<String> keys);

    /**
     * 根据 objectKey 查找媒体
     */
    Optional<Media> findByObjectKey(String objectKey);

    /**
     * ✅ 新增：根据帖子 ID 查找媒体列表
     * 用于删除帖子时同时清理 MinIO 文件
     */
    @Query("SELECT m FROM Media m WHERE m.post.id = :postId")
    List<Media> findByPostId(@Param("postId") Long postId);
}
