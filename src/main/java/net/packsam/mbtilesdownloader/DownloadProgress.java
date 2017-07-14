package net.packsam.mbtilesdownloader;

/**
 * Model class for watching the download progress.
 * 
 * @author osterrath
 *
 */
public class DownloadProgress {
	/**
	 * Number of total tiles to download.
	 */
	private long totalTiles;
	/**
	 * Number of downloaded tiles.
	 */
	private long finishedTiles;

	/**
	 * Getter method for the field "totalTiles".
	 *
	 * @return the totalTiles
	 */
	public long getTotalTiles() {
		return totalTiles;
	}

	/**
	 * Setter method for the field "totalTiles".
	 *
	 * @param totalTiles
	 *            the totalTiles to set
	 */
	public void setTotalTiles(long totalTiles) {
		this.totalTiles = totalTiles;
	}

	/**
	 * Getter method for the field "finishedTiles".
	 *
	 * @return the finishedTiles
	 */
	public long getFinishedTiles() {
		return finishedTiles;
	}

	/**
	 * Setter method for the field "finishedTiles".
	 *
	 * @param finishedTiles
	 *            the finishedTiles to set
	 */
	public void setFinishedTiles(long finishedTiles) {
		this.finishedTiles = finishedTiles;
	}

}
