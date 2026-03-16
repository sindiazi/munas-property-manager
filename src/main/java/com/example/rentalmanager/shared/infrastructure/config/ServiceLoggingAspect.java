package com.example.rentalmanager.shared.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * AOP aspect that instruments every public method in any application-layer
 * service class.
 *
 * <p>Pointcut: all public methods inside {@code *.application.service.*}.
 *
 * <p>Log format (DEBUG):
 * <pre>
 *   [SERVICE] TenantApplicationService.registerTenant(args=[...])
 *   [SERVICE] TenantApplicationService.registerTenant completed (18 ms)
 *   [SERVICE] TenantApplicationService.registerTenant error after 5 ms: ...
 * </pre>
 *
 * <p>For reactive return types ({@link Mono}/{@link Flux}) the timing hooks are
 * attached to the reactive pipeline so execution time is measured correctly.
 */
@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    @Around("execution(public * com.example.rentalmanager.*.application.service.*.*(..))")
    public Object logServiceMethod(ProceedingJoinPoint pjp) throws Throwable {
        var sig        = (MethodSignature) pjp.getSignature();
        var className  = sig.getDeclaringType().getSimpleName();
        var methodName = sig.getName();
        var args       = Arrays.toString(pjp.getArgs());
        var label      = className + "." + methodName;

        log.debug("[SERVICE] {}(args={})", label, args);
        long start = System.currentTimeMillis();

        Object result = pjp.proceed();

        if (result instanceof Mono<?> mono) {
            return mono
                    .doOnSuccess(v  -> log.debug("[SERVICE] {} completed ({} ms)", label, elapsed(start)))
                    .doOnError(err  -> log.debug("[SERVICE] {} error after {} ms: {}",
                            label, elapsed(start), err.getMessage()));
        }

        if (result instanceof Flux<?> flux) {
            return flux
                    .doOnComplete(() -> log.debug("[SERVICE] {} completed ({} ms)", label, elapsed(start)))
                    .doOnError(err  -> log.debug("[SERVICE] {} error after {} ms: {}",
                            label, elapsed(start), err.getMessage()));
        }

        // Synchronous return (unlikely for reactive services but handled for safety)
        log.debug("[SERVICE] {} completed ({} ms)", label, elapsed(start));
        return result;
    }

    private long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }
}
