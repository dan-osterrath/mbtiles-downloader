package net.packsam.mbtilesdownloader.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * Configuration class that reads it values from a properties file in class path.
 * 
 * @author osterrath
 *
 */
public class PropertiesConfiguration implements Configuration {
	/**
	 * Property key for the URL pattern.
	 */
	private final static String PROP_URL_PATTERN = "url";

	/**
	 * Property key for the number of threads.
	 */
	private final static String PROP_NUM_THREADS = "threads";

	/**
	 * Property key for the user agent.
	 */
	private final static String PROP_USER_AGENT = "userAgent";

	/**
	 * Property key for the tiles mapper class name.
	 */
	private final static String PROP_TILES_MAPPER = "mapper";

	/**
	 * Properties object.
	 */
	private final Properties properties;

	/**
	 * Ctor.
	 *
	 * @param baseClass
	 *            base class to read properties from
	 * @throws IOException
	 *             could not read properties file
	 */
	public PropertiesConfiguration(Class<?> baseClass) throws IOException {
		InputStream is = null;
		try {
			is = baseClass.getResourceAsStream(baseClass.getSimpleName() + ".properties");
			properties = new Properties();
			properties.load(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Ctor.
	 *
	 * @param propertiesFile
	 *            properties file to read
	 * @throws IOException
	 *             could not read properties file
	 */
	public PropertiesConfiguration(File propertiesFile) throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(propertiesFile);
			properties = new Properties();
			properties.load(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.packsam.mbtilesdownloader.config.Configuration#getTilesURLPattern()
	 */
	public String getTilesURLPattern() {
		return properties.getProperty(PROP_URL_PATTERN);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.packsam.mbtilesdownloader.config.Configuration#getParallelThreads()
	 */
	public int getParallelThreads() {
		return Integer.parseInt(properties.getProperty(PROP_NUM_THREADS));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.packsam.mbtilesdownloader.config.Configuration#getUserAgent()
	 */
	public String getUserAgent() {
		return properties.getProperty(PROP_USER_AGENT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.packsam.mbtilesdownloader.config.Configuration#getTilesMapper()
	 */
	public String getTilesMapper() {
		return properties.getProperty(PROP_TILES_MAPPER);
	}

}
