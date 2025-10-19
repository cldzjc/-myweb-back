package org.example.blog.repository;

import org.example.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 最新在上
    @Query("select p from Post p left join fetch p.medias order by p.createTime desc")
    List<Post> findAllWithMediasOrderByCreateTimeDesc();

}