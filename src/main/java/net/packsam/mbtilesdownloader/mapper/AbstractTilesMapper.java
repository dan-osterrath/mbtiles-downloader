package net.packsam.mbtilesdownloader.mapper;

import net.packsam.mbtilesdownloader.config.Configuration;

/**
 * Abstract class for a tiles mapper with configuration.
 *
 * @author osterrath
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractTilesMapper implements TilesMapper {

    /**
     * Configuration object.
     */
    protected Configuration config;

    /*
     * (non-Javadoc)
     *
     * @see net.packsam.mbtilesdownloader.mapper.TilesMapper#setConfiguration(net.packsam.mbtilesdownloader.config.Configuration)
     */
    public void setConfiguration(Configuration config) {
        this.config = config;
    }

}
