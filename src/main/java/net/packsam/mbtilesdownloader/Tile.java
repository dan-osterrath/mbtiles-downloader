package net.packsam.mbtilesdownloader;

/**
 * Model class for a tile to download.
 * 
 * @author osterrath
 *
 */
public class Tile {
	/**
	 * X coordinate.
	 */
	private final long x;

	/**
	 * Y coordinate.
	 */
	private final long y;

	/**
	 * Zoom level.
	 */
	private final int z;

	/**
	 * URL to download.
	 */
	private String url;

	/**
	 * Ctor.
	 *
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param z
	 *            zoom level
	 */
	public Tile(long x, long y, int z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Getter method for the field "url".
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Setter method for the field "url".
	 *
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Getter method for the field "x".
	 *
	 * @return the x
	 */
	public long getX() {
		return x;
	}

	/**
	 * Getter method for the field "y".
	 *
	 * @return the y
	 */
	public long getY() {
		return y;
	}

	/**
	 * Getter method for the field "z".
	 *
	 * @return the z
	 */
	public int getZ() {
		return z;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (x ^ (x >>> 32));
		result = prime * result + (int) (y ^ (y >>> 32));
		result = prime * result + z;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tile other = (Tile) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Tile [x=" + x + ", y=" + y + ", z=" + z + "]";
	}

}
