package org.example.blog.dto;

import lombok.Data;

@Data
public class EmailDTO {
    private String email;
    private String code;  // 当只发送验证码时，可以不传这个字段
}
