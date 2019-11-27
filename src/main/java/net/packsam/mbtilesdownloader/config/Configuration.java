package net.packsam.mbtilesdownloader.config;

/**
 * Configuration interface for configuring the MBTiles downloader.
 *
 * @author osterrath
 */
public interface Configuration {
    /**
     * Returns the tiles URL pattern for downloading the tiles.
     *
     * @return URL pattern
     */
    String getTilesURLPattern();

    /**
     * Number of parallel threads to download.
     *
     * @return number of threads
     */
    int getParallelThreads();

    /**
     * User agent string for faking a browser.
     *
     * @return user agent
     */
    String getUserAgent();

    /**
     * The class name for the tiles mapper.
     *
     * @return tiles mapper
     */
    String getTilesMapper();
}
