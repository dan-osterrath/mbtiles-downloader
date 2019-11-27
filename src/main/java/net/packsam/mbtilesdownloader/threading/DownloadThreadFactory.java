package net.packsam.mbtilesdownloader.threading;

import org.apache.http.conn.HttpClientConnectionManager;

import java.util.concurrent.ThreadFactory;

/**
 * Thread factory for the download threads.
 *
 * @author osterrath
 */
public class DownloadThreadFactory implements ThreadFactory {

    /**
     * Thread group to use for downloading.
     */
    private final ThreadGroup threadGroup;

    /**
     * HTTP client connection manager.
     */
    private final HttpClientConnectionManager connectionManager;

    /**
     * Current number of threads.
     */
    private int threadNums = 0;

    /**
     * Ctor.
     *
     * @param connectionManager http client connection manager.
     */
    public DownloadThreadFactory(HttpClientConnectionManager connectionManager) {
        threadGroup = new ThreadGroup("DownloadThreadGroup");
        threadGroup.setDaemon(true);
        this.connectionManager = connectionManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(Runnable r) {
        DownloadThread dt = new DownloadThread(threadGroup, threadNums++, connectionManager, r);
        dt.setDaemon(true);
        return dt;
    }

}
