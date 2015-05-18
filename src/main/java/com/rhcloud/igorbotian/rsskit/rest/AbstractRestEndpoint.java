package com.rhcloud.igorbotian.rsskit.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author Igor Botian <igor.botian@gmail.com>
 */
public abstract class AbstractRestEndpoint implements RestEndpoint {

    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int SOCKET_TIMEOUT = 60000;

    protected static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    protected static final HttpClient HTTP_CLIENT;

    static {
        RequestConfig.Builder rcBuilder = RequestConfig.custom();

        rcBuilder.setConnectTimeout(CONNECTION_TIMEOUT);
        rcBuilder.setSocketTimeout(SOCKET_TIMEOUT);
        rcBuilder.setConnectionRequestTimeout(CONNECTION_TIMEOUT);

        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");

        if(StringUtils.isNotEmpty(proxyHost) && StringUtils.isNotEmpty(proxyPort)) {
            rcBuilder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
        }

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultRequestConfig(rcBuilder.build());

        HTTP_CLIENT = builder.build();
    }

    @Override
    public JsonNode makeRequest(String endpoint, List<NameValuePair> params) throws IOException {
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(params);

        byte[] content = makeRawRequest(endpoint, params);
        return JSON_MAPPER.readTree(content);
    }

    protected byte[] makeRawRequest(String endpoint, List<NameValuePair> params) throws IOException {
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(params);

        HttpResponse response = requestor().request(endpoint, params);

        try {
            long contentLength = response.getEntity().getContentLength();
            return (contentLength < 0)
                    ? IOUtils.toByteArray(response.getEntity().getContent())
                    : IOUtils.toByteArray(response.getEntity().getContent(), contentLength);
        } finally {
            EntityUtils.consumeQuietly(response.getEntity());
        }
    }

    protected abstract Requestor requestor();

    protected interface Requestor {

        HttpResponse request(String endpoint, List<NameValuePair> params) throws IOException;
    }
}
