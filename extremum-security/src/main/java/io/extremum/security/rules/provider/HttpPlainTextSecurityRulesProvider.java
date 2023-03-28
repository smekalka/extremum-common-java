package io.extremum.security.rules.provider;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@Slf4j
public class HttpPlainTextSecurityRulesProvider extends AbstractSecurityRuleProvider implements Runnable {

    private final URI rulesUri;
    private final Long rulesTimeOut;
    private final HttpHeaders headers;

    @SneakyThrows
    public HttpPlainTextSecurityRulesProvider(String rulesUri, Long rulesTimeOut, HttpHeaders headers) {
        this.rulesUri = new URI(rulesUri);
        this.rulesTimeOut = rulesTimeOut;
        this.headers = headers;
        new Thread(this).start();
    }

    private String previousEtag;

    @Override
    protected List<String> getRulesStringsList() {
        log.debug("Retrieving security rules from IAM service");
        ClientHttpRequest request = new OkHttp3ClientHttpRequestFactory()
                .createRequest(rulesUri, HttpMethod.GET);
        headers.forEach(
                (header, values) -> values.forEach(value -> request.getHeaders().add(header, value))
        );
        try (ClientHttpResponse response = request.execute()) {
            //TODO: Fix etag checking
            String eTag = response.getHeaders().getETag();
            previousEtag = eTag;
            return Arrays.asList(
                    new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"))
                            .split(";")
            );
        } catch (IOException | RuntimeException e) {
            log.error("Unable to get security rules");
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<String> rulesStringsList = getRulesStringsList();
                if (!rulesStringsList.isEmpty()) {
                    serviceTypeToSecurityRulesMap = composeServiceTypeToSecurityRulesMap(rulesStringsList);
                    log.debug("Rules updated {}", rulesStringsList);
                } else {
                    log.warn("Rules is empty");
                }
            } catch (InvalidSecurityRulesException exception) {
                log.error("Invalid security rules", exception);
            } catch (RuntimeException ex) {
                log.error("Unable to update security rules");
            } finally {
                sleep(rulesTimeOut);
            }
        }
    }
}