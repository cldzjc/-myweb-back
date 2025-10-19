package org.example.blog.service;
import org.example.blog.entity.Media;

import java.util.List;

public interface MediaService {
    /**
     * 把 media 表批量绑定到指定帖子
     * @param postId  刚生成的帖子 id
     * @param mediaKeys 逗号拼接的 objectKey
     */
    void bindToPost(Long postId, String mediaKeys);
    /**
     * ✅ 新增：根据帖子ID查媒体
     */
    List<Media> findByPostId(Long postId);

    /**
     * ✅ 新增：删除 MinIO 上的媒体文件
     */
    void deleteFromMinio(List<Media> medias);
}