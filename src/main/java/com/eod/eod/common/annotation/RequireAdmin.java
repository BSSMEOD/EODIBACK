package com.eod.eod.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ADMIN 권한이 필요한 메서드에 사용하는 어노테이션
 *
 * AOP를 통해 메서드 실행 전 현재 사용자의 권한을 검증합니다.
 * ADMIN 권한이 없는 경우 AccessDeniedException을 발생시킵니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {
    /**
     * 권한이 없을 때 표시할 메시지
     */
    String message() default "ADMIN 권한이 필요합니다.";
}
