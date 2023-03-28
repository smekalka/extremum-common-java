package io.extremum.common.urls;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ApplicationUrlsImplTest {
    private ApplicationUrls applicationUrls = new ApplicationUrlsImpl("http://extremum.io");

    @BeforeEach
    public void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/context/some-uri");

        request.setScheme("https");
        request.setServerName("example.com");
        request.setServerPort(443);
        request.setContextPath("/context");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void givenThereIsARequest_whenCreatingExternalUrl_itShouldBeAppendedToContextUrl() {
        String url = applicationUrls.createExternalUrl("/my-url");

        assertThat(url, is("https://example.com/context/my-url"));
    }
}