package org.example.blog.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    /* 原有 ok() 无数据 保持不动 */
    public static Result<?> ok() {
        return new Result<>(200, "success", null);
    }

    /* 新增：无数据但类型为 Void 的专用重载 */
    public static Result<Void> okVoid() {
        return new Result<>(200, "success", null);
    }

    /* 原有带数据 ok 保持不动 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    /* 原有 error 保持不动 */
    public static Result<?> error(String msg) {
        return new Result<>(400, msg, null);
    }
}