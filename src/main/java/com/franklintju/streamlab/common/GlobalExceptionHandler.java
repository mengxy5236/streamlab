package com.franklintju.streamlab.common;

import com.franklintju.streamlab.exceptions.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getStatus().value(), e.getMessage());
    }

    // 资源不存在
    @ExceptionHandler({NoHandlerFoundException.class, org.springframework.data.mapping.PropertyReferenceException.class})
    public ApiResponse<Void> handleNotFound(Exception e) {
        return ApiResponse.error(404, "资源不存在");
    }

    // 参数类型错误
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ApiResponse.error(400, "参数类型错误: " + e.getName());
    }

    // 参数校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ApiResponse.error(400, "参数校验失败");
    }

    // 请求体解析失败
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleUnreadableMessage() {
        return ApiResponse.error(400, "请求体格式错误");
    }

    // 乐观锁冲突
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ApiResponse<Void> handleOptimisticLock() {
        return ApiResponse.error(409, "数据已被其他用户修改，请重试");
    }

    // 数据约束冲突
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        return ApiResponse.error(409, "数据已存在或被关联，无法操作");
    }

    // 权限不足
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException e) {
        return ApiResponse.error(403, "权限不足");
    }

    // 未登录
    @ExceptionHandler(AuthenticationException.class)
    public ApiResponse<Void> handleAuthenticationException(AuthenticationException e) {
        return ApiResponse.error(401, "请先登录");
    }

    // 其他未知异常
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        return ApiResponse.error(500, "服务器内部错误");
    }
}
