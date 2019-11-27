package net.packsam.mbtilesdownloader.threading;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;

/**
 * Thread for downloading tiles.
 *
 * @author osterrath
 */
public class DownloadThread extends Thread {
    /**
     * HTTP client used in this thread.
     */
    private final CloseableHttpClient httpClient;

    /**
     * HTTP context.
     */
    private final HttpContext httpContext;

    /**
     * Ctor.
     *
     * @param threadGroup       thread group
     * @param threadNum         thread num
     * @param connectionManager http client connection manager
     * @param target            target runnable to execute
     */
    DownloadThread(ThreadGroup threadGroup, int threadNum, HttpClientConnectionManager connectionManager, Runnable target) {
        super(threadGroup, target, "DownloadThread-" + threadNum);
        httpClient = createHttpClient(connectionManager);
        httpContext = createHttpContext();
    }

    /**
     * Creates the http client for this thread.
     *
     * @param connectionManager http client connection manager
     * @return http client
     */
    private CloseableHttpClient createHttpClient(HttpClientConnectionManager connectionManager) {
        HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManager(connectionManager);
        String httpProxyHost = System.getProperty("http.proxyHost");
        String httpProxyPort = System.getProperty("http.proxyPort");
        if (StringUtils.isNoneEmpty(httpProxyHost, httpProxyPort)) {
            clientBuilder.setProxy(new HttpHost(httpProxyHost, Integer.parseInt(httpProxyPort, 10)));
        }
        return clientBuilder.build();
    }

    /**
     * Creates the http context for this thread.
     *
     * @return http context
     */
    private HttpContext createHttpContext() {
        return HttpClientContext.create();
    }

    /**
     * Getter method for the field "httpClient".
     *
     * @return the httpClient
     */
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Getter method for the field "httpContext".
     *
     * @return the httpContext
     */
    public HttpContext getHttpContext() {
        return httpContext;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        IOUtils.closeQuietly(httpClient);
    }
}
