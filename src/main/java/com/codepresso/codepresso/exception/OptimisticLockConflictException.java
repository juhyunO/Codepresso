package com.codepresso.codepresso.exception;

/**
 * 낙관적 락 충돌로 재시도 횟수 초과 시 발생
 */
public class OptimisticLockConflictException extends RuntimeException{

    public OptimisticLockConflictException(String message) {
        super(message);
    }

    public OptimisticLockConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
