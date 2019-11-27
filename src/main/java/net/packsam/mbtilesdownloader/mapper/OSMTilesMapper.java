package net.packsam.mbtilesdownloader.mapper;

import net.packsam.mbtilesdownloader.Tile;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiles mapper for the OpenStreetMap tiles.
 *
 * @author osterrath
 */
public class OSMTilesMapper extends AbstractTilesMapper {
    /**
     * Regexp Pattern for extracting the random car pattern.
     */
    private final static Pattern RAND_CHAR_PATTERN = Pattern.compile("\\{rand:(.)-(.)}");

    /*
     * (non-Javadoc)
     *
     * @see net.packsam.mbtilesdownloader.mapper.TilesMapper#initURL(net.packsam.mbtilesdownloader.Tile)
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public Tile initURL(Tile tile) {
        synchronized (tile) {
            if (StringUtils.isEmpty(tile.getUrl())) {
                String url = config.getTilesURLPattern()//
                        .replaceAll("\\{z}", Integer.toString(tile.getZ(), 10)) //
                        .replaceAll("\\{x}", Long.toString(tile.getX(), 10)) //
                        .replaceAll("\\{y}", Long.toString(tile.getY(), 10)) //
                        ;
                Matcher matcher = RAND_CHAR_PATTERN.matcher(url);
                if (matcher.find()) {
                    StringBuffer sb = new StringBuffer();
                    do {
                        String start = matcher.group(1);
                        String end = matcher.group(2);
                        int startNum = start.charAt(0);
                        int endNum = end.charAt(0);
                        int min = Math.min(startNum, endNum);
                        int max = Math.max(startNum, endNum);
                        int randNum = min + (int) (Math.random() * (max - min + 1));
                        String rand = Character.toString((char) randNum);
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(rand));
                    } while (matcher.find());
                    matcher.appendTail(sb);
                    url = sb.toString();
                }
                tile.setUrl(url);
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
        return "png";
    }

}
