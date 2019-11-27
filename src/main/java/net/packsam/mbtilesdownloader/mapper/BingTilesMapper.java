package net.packsam.mbtilesdownloader.mapper;

import net.packsam.mbtilesdownloader.Tile;
import org.apache.commons.lang3.StringUtils;

/**
 * Tiles mapper for Bing maps styled maps.
 *
 * @author osterrath
 */
public class BingTilesMapper extends AbstractTilesMapper {

    /*
     * (non-Javadoc)
     *
     * @see net.packsam.mbtilesdownloader.mapper.TilesMapper#initURL(net.packsam.mbtilesdownloader.Tile)
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Override
    public Tile initURL(Tile tile) {
        synchronized (tile) {
            if (StringUtils.isEmpty(tile.getUrl())) {
                StringBuilder quadKey = new StringBuilder();
                for (int i = tile.getZ(); i > 0; i--) {
                    int digit = 0;
                    long mask = 1 << (i - 1);
                    if ((tile.getX() & mask) > 0) {
                        digit++;
                    }
                    if ((tile.getY() & mask) > 0) {
                        digit += 2;
                    }
                    quadKey.append(digit);
                }

                String tilesURL = config.getTilesURLPattern().replaceAll("\\{quadkey}", quadKey.toString());
                tile.setUrl(tilesURL);
            }
        }

        return tile;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.packsam.mbtilesdownloader.mapper.TilesMapper#getTilesFormat()
     */
    @Override
    public String getTilesFormat() {
        return "jpeg";
    }

}
