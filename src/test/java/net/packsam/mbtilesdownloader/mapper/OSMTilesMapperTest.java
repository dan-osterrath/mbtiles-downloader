package net.packsam.mbtilesdownloader.mapper;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.packsam.mbtilesdownloader.Tile;
import net.packsam.mbtilesdownloader.config.Configuration;

/**
 * Test class for {@link OSMTilesMapper}.
 * 
 * @author osterrath
 *
 */
public class OSMTilesMapperTest {
	/**
	 * Test case for {@link OSMTilesMapper#initURL(Tile)}.
	 */
	@Test
	public void testInitURL() {
		OSMTilesMapper mapper = new OSMTilesMapper();
		mapper.setConfiguration(new Configuration() {

			public String getUserAgent() {
				return null;
			}

			public String getTilesURLPattern() {
				return "http://{rand:a-z}{rand:a-z}{rand:a-z}.myserver/{z}/{x}/{y}.png";
			}

			public String getTilesMapper() {
				return null;
			}

			public int getParallelThreads() {
				return 0;
			}
		});

		Tile t = new Tile(10, 12, 3);
		mapper.initURL(t);

		assertTrue(t.getUrl().matches("http://[a-z][a-z][a-z].myserver/3/10/12.png"));
	}
}
