package com.eod.eod.common.aspect;

import com.eod.eod.common.annotation.RequireAdmin;
import com.eod.eod.domain.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * ADMIN 권한 검증 AOP
 *
 * @RequireAdmin 어노테이션이 붙은 메서드 실행 전에
 * 현재 사용자의 권한을 검증합니다.
 */
@Slf4j
@Aspect
@Component
public class AdminAuthorizationAspect {

    @Before("@annotation(com.eod.eod.common.annotation.RequireAdmin)")
    public void checkAdminRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireAdmin annotation = method.getAnnotation(RequireAdmin.class);

        // 메서드 파라미터에서 User 객체 찾기
        Object[] args = joinPoint.getArgs();
        User currentUser = null;

        for (Object arg : args) {
            if (arg instanceof User) {
                currentUser = (User) arg;
                break;
            }
        }

        if (currentUser == null) {
            log.error("@RequireAdmin이 적용된 메서드에 User 파라미터가 없습니다: {}", method.getName());
            throw new IllegalStateException("권한 검증을 위한 사용자 정보가 필요합니다.");
        }

        if (!currentUser.isAdmin()) {
            String message = annotation.message();
            log.warn("권한 부족: 사용자 {} (역할: {})가 {} 메서드 접근 시도",
                    currentUser.getEmail(), currentUser.getRole(), method.getName());
            throw new AccessDeniedException(message);
        }

        log.debug("ADMIN 권한 검증 통과: {} 메서드", method.getName());
    }
}
