package net.packsam.mbtilesdownloader.mapper;

import net.packsam.mbtilesdownloader.Tile;
import net.packsam.mbtilesdownloader.config.Configuration;

/**
 * Interface for the tiles mapper.
 * 
 * @author osterrath
 *
 */
public interface TilesMapper {
	/**
	 * Sets the configuration.
	 * 
	 * @param config
	 *            configuration
	 */
	void setConfiguration(Configuration config);

	/**
	 * Initializes the url of the given tile.
	 * 
	 * @param tile
	 *            tile to initialize
	 * @return tile
	 */
	Tile initURL(Tile tile);

	/**
	 * Returns the tiles file type.
	 * 
	 * @return imge type
	 */
	String getTilesFormat();
}
