package net.packsam.mbtilesdownloader;

/**
 * Helper class for calculating x and y values from latitude and longitudes.
 *
 * @author osterrath
 */
class ProjectionHelper {
    /**
     * Creates a tile object from the given latitude and longitude at the given zoom level.
     *
     * @param latitude  latitude in degrees
     * @param longitude longitude in degrees
     * @param zoomLevel zoom level
     * @return tiles object
     */
    static Tile createTile(double latitude, double longitude, int zoomLevel) {
        double n = Math.pow(2, zoomLevel);
        double x = n * ((longitude + 180) / 360);
        double latRad = latitude * 2 * Math.PI / 360;
        double y = n * (1 - (Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI)) / 2;
        return new Tile((long) Math.floor(x), (long) Math.floor(y), zoomLevel);
    }
}
