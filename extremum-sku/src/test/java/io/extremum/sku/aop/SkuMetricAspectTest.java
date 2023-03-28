package io.extremum.sku.aop;

import io.extremum.sku.model.SkuID;
import io.extremum.sku.model.SkuMetricMessage;
import io.extremum.test.aop.AspectWrapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageChannel;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class SkuMetricAspectTest {

    @Mock
    MessageChannel messageChannel;
    @Mock
    MessageChannel customChannel;
    @Mock
    ApplicationContext applicationContext;
    @InjectMocks
    private SkuMetricAspect aspect;
    @Captor
    private ArgumentCaptor<SkuMetricMessage> invocationCaptor;

    private TestService testServiceProxy;


    @BeforeEach
    void createProxies() {
        testServiceProxy = wrapWithAspect(new TestService());
        lenient();
    }

    private <T> T wrapWithAspect(T proxiedObject) {
        return AspectWrapping.wrapInAspect(proxiedObject, aspect);
    }

    @Test
    void should_send_sku_metrics_with_default_amount() {
        when(applicationContext.getBean("skuMetricMessageChannel", MessageChannel.class)).thenReturn(messageChannel);
        testServiceProxy.defaultAmount();
        verify(messageChannel, times(1)).send(invocationCaptor.capture());
        SkuMetricMessage value = invocationCaptor.getValue();
        Assertions.assertEquals(1L, value.getPayload().getAmount());
        Assertions.assertEquals(SkuID.DATA_ENTITIES_OPERATIONS_READ.getValue(), value.getPayload().getSku());
    }

    @Test
    void should_send_sku_metrics_with_amount_from_annotation() {
        when(applicationContext.getBean("skuMetricMessageChannel", MessageChannel.class)).thenReturn(messageChannel);
        testServiceProxy.amountFromAnnotation();
        verify(messageChannel, times(1)).send(invocationCaptor.capture());
        SkuMetricMessage value = invocationCaptor.getValue();
        Assertions.assertEquals(2L, value.getPayload().getAmount());
        Assertions.assertEquals(SkuID.DATA_ENTITIES_OPERATIONS_WRITE.getValue(), value.getPayload().getSku());
    }

    @Test
    void should_send_sku_metrics_with_amount_from_returned_value() {
        when(applicationContext.getBean("skuMetricMessageChannel", MessageChannel.class)).thenReturn(messageChannel);
        testServiceProxy.returnNumericAmount();
        verify(messageChannel, times(1)).send(invocationCaptor.capture());
        SkuMetricMessage value = invocationCaptor.getValue();
        Assertions.assertEquals(testServiceProxy.returnNumericAmount(), value.getPayload().getAmount());
        Assertions.assertEquals(SkuID.DATA_ENTITIES_OPERATIONS_WRITE.getValue(), value.getPayload().getSku());
    }

    @Test
    void should_send_sku_metrics_with_custom_value() {
        when(applicationContext.getBean("skuMetricMessageChannel", MessageChannel.class)).thenReturn(messageChannel);
        testServiceProxy.returnNumericAmount_();
        verify(messageChannel, times(1)).send(invocationCaptor.capture());
        SkuMetricMessage value = invocationCaptor.getValue();
        Assertions.assertEquals(testServiceProxy.returnNumericAmount(), value.getPayload().getAmount());
        Assertions.assertEquals(100, value.getPayload().getSku());
    }

    @Test
    void should_send_sku_metrics_to_custom_channel_with_custom_value() {
        when(applicationContext.getBean("customChannel", MessageChannel.class)).thenReturn(customChannel);
        testServiceProxy.sendToCustomChannelReturnedValue();
        verify(customChannel, times(1)).send(invocationCaptor.capture());
        SkuMetricMessage value = invocationCaptor.getValue();
        Assertions.assertEquals(testServiceProxy.sendToCustomChannelReturnedValue(), value.getPayload().getAmount());
        Assertions.assertEquals(SkuID.DATA_ENTITIES_OPERATIONS_WRITE.getValue(), value.getPayload().getSku());
    }

    @Test
    void should_throw_exception_if_returned_value_is_not_number() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> testServiceProxy.returnNonNumericAmount());
    }
}