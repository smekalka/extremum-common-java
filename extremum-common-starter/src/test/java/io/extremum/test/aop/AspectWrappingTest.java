package io.extremum.test.aop;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.annotation.Order;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class AspectWrappingTest {
    @Mock
    private Reporter reporter;

    @Test
    void springBeanOrderShouldBeRespected() {
        Target target = AspectWrapping.wrapInAspects(new Target(),
                new LowPriorityAspect(reporter), new HighPriorityAspect(reporter));

        target.act();

        assertThatHighThanLowPriorityWereCalled();
    }

    private void assertThatHighThanLowPriorityWereCalled() {
        InOrder inOrder = inOrder(reporter);
        inOrder.verify(reporter).highPriority();
        inOrder.verify(reporter).lowPriority();
        inOrder.verifyNoMoreInteractions();
    }

    @NoArgsConstructor
    private static class Target {
        public void act() {
        }
    }

    private interface Reporter {
        void highPriority();
        void lowPriority();
    }

    @Aspect
    @Order(1)
    @RequiredArgsConstructor
    private static class HighPriorityAspect {
        private final Reporter reporter;

        @Around("returnsVoid()")
        public Object executeAroundController(ProceedingJoinPoint point) throws Throwable {
            reporter.highPriority();
            return point.proceed();
        }

        @Pointcut("execution(void *..*.*(..))")
        private void returnsVoid() {
        }
    }

    @Aspect
    @Order(2)
    @RequiredArgsConstructor
    private static class LowPriorityAspect {
        private final Reporter reporter;

        @Around("returnsVoid()")
        public Object executeAroundController(ProceedingJoinPoint point) throws Throwable {
            reporter.lowPriority();
            return point.proceed();
        }

        @Pointcut("execution(void *..*.*(..))")
        private void returnsVoid() {
        }
    }
}