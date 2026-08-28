package movie_rating_backend.utils;

/**
 * 统一错误码枚举
 * 用于前后端错误信息标准化
 */
public enum ErrorCode {
    // 通用错误
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    SYSTEM_ERROR(500, "系统内部错误"),

    // 用户认证错误
    UNAUTHORIZED(401, "未登录或登录失效"),
    TOKEN_EXPIRED(401, "登录已过期，请重新登录"),
    TOKEN_INVALID(401, "无效的Token"),
    ACCOUNT_LOCKED(403, "账号已被锁定"),

    // 权限错误
    FORBIDDEN(403, "无权限访问"),
    ADMIN_ONLY(403, "需要管理员权限"),

    // 资源错误
    NOT_FOUND(404, "资源不存在"),
    USER_NOT_FOUND(404, "用户不存在"),
    MOVIE_NOT_FOUND(404, "电影不存在"),
    COMMENT_NOT_FOUND(404, "评论不存在"),

    // 业务错误
    DUPLICATE_USER(400, "用户名已存在"),
    DUPLICATE_MOVIE(400, "电影已存在"),
    INVALID_PASSWORD(400, "密码错误"),
    CONFIRM_PASSWORD_ERROR(400, "两次输入的密码不一致"),
    INVALID_EMAIL(400, "邮箱格式不正确"),
    USERNAME_TOO_SHORT(400, "用户名长度不能少于3位"),
    USERNAME_TOO_LONG(400, "用户名长度不能超过20位"),
    PASSWORD_TOO_SHORT(400, "密码长度不能少于6位"),
    PASSWORD_TOO_LONG(400, "密码长度不能超过20位"),
    PASSWORD_PATTERN_ERROR(400, "密码需包含大小写字母和数字"),

    // 请求错误
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    INVALID_REQUEST(400, "请求参数无效");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取错误信息
     */
    public static String getMessage(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode.getMessage();
            }
        }
        return SYSTEM_ERROR.getMessage();
    }
}
