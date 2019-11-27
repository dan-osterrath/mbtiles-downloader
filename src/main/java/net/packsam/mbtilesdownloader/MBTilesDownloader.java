package net.packsam.mbtilesdownloader;

import net.packsam.mbtilesdownloader.config.Configuration;
import net.packsam.mbtilesdownloader.config.PropertiesConfiguration;
import net.packsam.mbtilesdownloader.mapper.TilesMapper;
import net.packsam.mbtilesdownloader.threading.DownloadThread;
import net.packsam.mbtilesdownloader.threading.DownloadThreadFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.sqlite.JDBC;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class for downloading MBtiles files.
 *
 * @author osterrath
 */
@SuppressWarnings({"WeakerAccess", "SqlNoDataSourceInspection"})
public class MBTilesDownloader {
    /**
     * Min latitude.
     */
    private final double minLatitude;

    /**
     * Max latitude.
     */
    private final double maxLatitude;

    /**
     * Min longitude.
     */
    private final double minLongitude;

    /**
     * Max longitude.
     */
    private final double maxLongitude;

    /**
     * Min zoom level.
     */
    private final int minZoom;

    /**
     * Max zoom level.
     */
    private final int maxZoom;

    /**
     * Target MBTile file.
     */
    private final File dbFile;

    /**
     * Configuration to use.
     */
    private final Configuration config;

    /**
     * Configured tiles mapper.
     */
    private final TilesMapper tilesMapper;

    /**
     * Ctor.
     *
     * @param minLongitude min longitude
     * @param minLatitude  min latitude
     * @param maxLongitude max longitude
     * @param maxLatitude  max latitude
     * @param minZoom      min zoom level
     * @param maxZoom      max zoom level
     * @param dbFile       target MBTiles file
     * @throws IOException could not read properties file
     */
    public MBTilesDownloader(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude, int minZoom, int maxZoom, File dbFile) throws IOException {
        this(minLongitude, minLatitude, maxLongitude, maxLatitude, minZoom, maxZoom, dbFile, new PropertiesConfiguration(MBTilesDownloader.class));
    }

    /**
     * Ctor.
     *
     * @param minLatitude  min latitude
     * @param maxLatitude  max latitude
     * @param minLongitude min longitude
     * @param maxLongitude max longitude
     * @param minZoom      min zoom level
     * @param maxZoom      max zoom level
     * @param dbFile       target MBTiles file
     * @param config       configuration object
     */
    public MBTilesDownloader(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude, int minZoom, int maxZoom, File dbFile, Configuration config) {
        super();
        this.minLatitude = Math.min(minLatitude, maxLatitude);
        this.maxLatitude = Math.max(minLatitude, maxLatitude);
        this.minLongitude = Math.min(minLongitude, maxLongitude);
        this.maxLongitude = Math.max(minLongitude, maxLongitude);
        this.minZoom = Math.min(minZoom, maxZoom);
        this.maxZoom = Math.max(minZoom, maxZoom);
        this.dbFile = dbFile;
        this.config = config;

        tilesMapper = createTilesMapper(config.getTilesMapper());
        tilesMapper.setConfiguration(config);
    }

    /**
     * Starts downloading all tiles.
     */
    @SuppressWarnings("unused")
    public void download() {
        download(null);
    }

    /**
     * Starts downloading all tiles.
     *
     * @param callback optional callback when a tile has been downloaded
     * @return list of return values
     */
    @SuppressWarnings("UnusedReturnValue")
    public List<Object> download(Function<DownloadProgress, Void> callback) {
        Connection connection = null;
        PreparedStatement checkExistsStmt = null;
        PreparedStatement insertStmt;
        try {

            // open SQLite file
            try {
                connection = openSQLiteFile(dbFile);
                checkExistsStmt = connection.prepareStatement("SELECT COUNT(tile_data) FROM tiles WHERE zoom_level=? AND tile_column=? AND tile_row=?");
                insertStmt = connection.prepareStatement("INSERT INTO tiles (zoom_level, tile_column, tile_row, tile_data) VALUES (?,?,?,?)");

            } catch (SQLException e) {
                throw new RuntimeException("Could not initialize SB file", e);
            }

            // create thread pool
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(config.getParallelThreads());
            connectionManager.setDefaultMaxPerRoute(config.getParallelThreads());
            ExecutorService executorService = Executors.newFixedThreadPool(config.getParallelThreads(), new DownloadThreadFactory(connectionManager));

            // create all tiles
            List<Tile> tiles = new ArrayList<>();
            for (int z = minZoom; z <= maxZoom; z++) {
                Tile t1 = ProjectionHelper.createTile(maxLatitude, maxLongitude, z);
                Tile t2 = ProjectionHelper.createTile(minLatitude, minLongitude, z);

                long startX = Math.min(t1.getX(), t2.getX());
                long endX = Math.max(t1.getX(), t2.getX());
                long startY = Math.min(t1.getY(), t2.getY());
                long endY = Math.max(t1.getY(), t2.getY());
                for (long x = startX; x <= endX; x++) {
                    for (long y = startY; y <= endY; y++) {
                        Tile t = new Tile(x, y, z);
                        tiles.add(t);
                    }
                }
            }

            // create tasks for tile
            DownloadProgress progress = new DownloadProgress();
            progress.setTotalTiles(tiles.size());
            progress.setFinishedTiles(0);
            final Connection finalConnection = connection;
            final PreparedStatement finalCheckExistsStmt = checkExistsStmt;
            final PreparedStatement finalInsertStmt = insertStmt;
            List<Runnable> tasks = tiles.stream().map(tile -> createTask(tile, finalConnection, finalCheckExistsStmt, finalInsertStmt, callback, progress))
                    .collect(Collectors.toList());

            // add all tasks to executor service
            List<Future<?>> futures = tasks.stream().map(executorService::submit).collect(Collectors.toList());

            // wait for all tasks
            return futures.stream().map(f -> {
                try {
                    return f.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Task threw error:");
                    e.printStackTrace();
                    return null;
                }
            }).collect(Collectors.toList());

        } finally {
            if (checkExistsStmt != null) {
                try {
                    checkExistsStmt.close();
                } catch (SQLException ignored) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Creates a runnable task for downloading a tile.
     *
     * @param tile            tile
     * @param connection      DB connection
     * @param checkExistsStmt statement for checking if the tile already exists in the database
     * @param insertStmt      statement for inserting a tile
     * @param callback        optional callback for updating progress
     * @param progress        current progress
     * @return task
     */
    private Runnable createTask(Tile tile, Connection connection, PreparedStatement checkExistsStmt, PreparedStatement insertStmt, Function<DownloadProgress, Void> callback,
                                DownloadProgress progress) {
        return () -> downloadTile(tile, connection, checkExistsStmt, insertStmt, callback, progress);
    }

    /**
     * Downloads the tile.
     *
     * @param tile            tile to download
     * @param connection      DB connection
     * @param checkExistsStmt statement for checking if the tile already exists in the database
     * @param insertStmt      statement for inserting a tile
     * @param callback        optional callback for updating progress
     * @param progress        current progress
     * @return tile
     */
    @SuppressWarnings({"UnusedReturnValue", "SynchronizationOnLocalVariableOrMethodParameter"})
    private Tile downloadTile(Tile tile, Connection connection, PreparedStatement checkExistsStmt, PreparedStatement insertStmt, Function<DownloadProgress, Void> callback,
                              DownloadProgress progress) {
        // look up in SQL if tile already exists
        boolean alreadyDownloaded = false;
        synchronized (connection) {
            ResultSet rs = null;
            try {
                checkExistsStmt.setInt(1, tile.getZ());
                checkExistsStmt.setLong(2, tile.getX());
                checkExistsStmt.setLong(3, getMappedY(tile.getY(), tile.getZ()));
                rs = checkExistsStmt.executeQuery();
                if (rs.next()) {
                    long count = rs.getLong(1);
                    if (count > 0) {
                        alreadyDownloaded = true;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Could not read MBTiles file", e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ignored) {
                    }
                }
            }
        }
        if (alreadyDownloaded) {
            synchronized (progress) {
                progress.setFinishedTiles(progress.getFinishedTiles() + 1);
                callback.apply(progress);
            }
            return tile;
        }

        // get URL from tiles mapper
        if (StringUtils.isEmpty(tile.getUrl())) {
            tilesMapper.initURL(tile);
        }

        // download tile
        DownloadThread downloadThread = (DownloadThread) Thread.currentThread();
        CloseableHttpClient httpClient = downloadThread.getHttpClient();
        HttpContext httpContext = downloadThread.getHttpContext();

        byte[] image = null;
        int retry = 0;
        try {
            do {
                HttpGet httpGet = new HttpGet(tile.getUrl());
                httpGet.setHeader(HttpHeaders.USER_AGENT, config.getUserAgent());
                CloseableHttpResponse response = null;
                try {
                    response = httpClient.execute(httpGet, httpContext);
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        response.getEntity().writeTo(baos);
                        image = baos.toByteArray();
                        IOUtils.closeQuietly(baos);
                    } else {
                        // wait shortly for server to create tile
                        Thread.sleep(100);
                        retry++;
                    }
                } catch (ClientProtocolException e) {
                    throw new RuntimeException("Invalid URL", e);
                } catch (IOException e) {
                    Thread.sleep(100);
                    retry++;
                    // ignore IO error for now
                } finally {
                    IOUtils.closeQuietly(response);
                }
            } while (image == null && retry < 50);
        } catch (InterruptedException ie) {
            // we got interrupted
            return null;
        }

        if (image == null) {
            System.err.println("Could not download tile " + tile);
            return tile;
        }

        // save tile in DB
        synchronized (connection) {
            try {
                insertStmt.setInt(1, tile.getZ());
                insertStmt.setLong(2, tile.getX());
                insertStmt.setLong(3, getMappedY(tile.getY(), tile.getZ()));
                insertStmt.setBytes(4, image);
                insertStmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Could not insert image in MBTiles file", e);
            }
        }

        // update progress and call callback
        synchronized (progress) {
            progress.setFinishedTiles(progress.getFinishedTiles() + 1);
            callback.apply(progress);
        }

        return tile;
    }

    /**
     * Creates the tiles mapper with the given class name.
     *
     * @param tilesMapperClass class for the tiles mapper
     * @return tiles mapper class
     */
    private TilesMapper createTilesMapper(String tilesMapperClass) {
        try {
            Class<?> clazz = Class.forName(tilesMapperClass);
            if (TilesMapper.class.isAssignableFrom(clazz)) {
                return (TilesMapper) clazz.newInstance();
            } else {
                throw new RuntimeException("Tiles mapper class is no tiles mapper");
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Could not find tiles mapper class", e);
        }
    }

    /**
     * Opens the SQLite file or creates a new file of not found.
     *
     * @param file SQLite file
     * @return SB connection
     * @throws SQLException an error occurred while initializing the DB file
     */
    private Connection openSQLiteFile(File file) throws SQLException {
        try {
            Class.forName(JDBC.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find JDBC driver");
        }

        boolean createFile = !file.exists();

        // open file
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());

        if (createFile) {
            // create tables and indizes
            String boundsString = Double.toString(maxLongitude) + ',' + minLatitude + ',' + minLongitude + ',' + maxLatitude;
            executeUpdateStatement("CREATE TABLE metadata (name text, value text)", connection);
            executeUpdateStatement("CREATE TABLE tiles (zoom_level integer, tile_column integer, tile_row integer, tile_data blob)", connection);
            executeUpdateStatement("CREATE UNIQUE INDEX idx_tiles_tile ON tiles (zoom_level, tile_column, tile_row)", connection);
            executeUpdateStatement("CREATE INDEX idx_tiles_zoom_level ON tiles (zoom_level)", connection);
            insertMetaDataValues("name", "Mapsquare MBTile", connection);
            insertMetaDataValues("type", "baselayer", connection);
            insertMetaDataValues("version", "1", connection);
            insertMetaDataValues("description", "Base Layer generated by Mapsquare", connection);
            insertMetaDataValues("format", "png", connection);
            insertMetaDataValues("bounds", boundsString, connection);

        }

        return connection;
    }

    /**
     * Executes the given SQL on the given connection.
     *
     * @param sql        SQL
     * @param connection DB connection
     * @throws SQLException some error occured when executing SQL
     */
    private void executeUpdateStatement(String sql, Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Inserts some meta data into the MBtiles database.
     *
     * @param key        meta data key
     * @param value      meta data value
     * @param connection SB connection
     * @throws SQLException some error occurred when executing SQL
     */
    private void insertMetaDataValues(String key, String value, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO metadata VALUES (?, ?)")) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.executeUpdate();
        }
    }

    /**
     * Returns the mapped y value for MBTiles files
     *
     * @param y raw y value
     * @param z zoom level
     * @return mapped y value
     */
    private long getMappedY(long y, long z) {
        return (long) (Math.pow(2, z) - y - 1);
    }
}
