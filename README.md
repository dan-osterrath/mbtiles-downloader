MBtiles downloader
===================

MBtiles downloader is a small Java programm to create [MBtiles](http://wiki.openstreetmap.org/wiki/MBTiles) files from a GIS server. Is main purpose is to create offline maps for my [CarPi](https://github.com/dan-osterrath/CarPi) project. It is derived from https://github.com/jawg/mbtiles-generator.

You can download from OpenStreetMaps like servers or from Bing Maps.
> **Warning:** *If you download from OpenStreetMaps or Bing maps directly, you need a valid license. Usually you don't have such a license so **don't do such evil things**! Use your own tiles server and download from your own server.*
A good start to setup your own OpenStreetMap tiles server is https://switch2osm.org/.

----------

### Requirements
* Java 8 SDK
* Maven
* sufficient disk space

### Configuration
Before creating an MBtiles file you need a configuration to specify the server URL, number of parallel threads, tiles mapping and the user agent string.
The configuration is in the standard [Java properties file format](https://en.wikipedia.org/wiki/.properties).
```properties
url=http\://www{rand:1-9}.myserver.com/{z}/{x}/{y}.png
mapper=net.packsam.mbtilesdownloader.mapper.OSMTilesMapper
threads=10
userAgent="Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0"
```
* *url* - The URL pattern for the tiles to download. The pattern can contain several tags (*{...}*) that will be replaced when downloading a tile. The tags are dependent on the tiles mapper.
* *mapper* - The tiles mapper that creates a correct URL for a tile. It replaces mapper specific tags in the url pattern to identify a tile.
There exists 2 mappers:
 * *net.packsam.mbtilesdownloader.mapper.OSMTilesMapper* - Tiles mapper for OpenStreetMap like servers. 
 * *net.packsam.mbtilesdownloader.mapper.BingTilesMapper* - Tiles mapper for Bing maps like servers.
* *threads* - number of parallel threads to use when downloading
* *userAgent* - User agent string to use when downloading.

#### Tags for OSMTilesMapper
| tag | description                                 |
| --- | ------------------------------------------- |
| {z} | placeholder for the zoom level in the URL   |
| {x} | placeholder for the x coordinate in the URL |
| {y} | placeholder for the y coordinate in the URL |
| {rand:a-z} | tag that will be replaced by a random character, the start and end character for the random range must be supplied, use this for load balancing the requests to several servers |

#### Tags for BingTilesMapper
| tag       | description                            |
| --------- | -------------------------------------- |
| {quadkey} | placeholder for the quadkey in the URL |

You can have several configuration files for several use cases. You have to specify the configuration file when launching the MBtiles downloader.

### Usage
First compile the project and create a JAR file to execute.
```sh
$> mvn package
```

After compilation you will find a *mbtiles-downloader.jar* in the *target/* directory. To start the downloader, run:
```sh
$> java -jar target/mbtiles_downloader.jar -b <bounds> -z <zoom-level> -o <options file> -f <target file>
```

#### Parameters
* *-b* / *--bounds* - the boundaries for the rectangle to download
These coordinates are in latitude, longitude in the following format: *[western longitude],[southern latitude],[eastern longitude],[northern latitude]*
Hint: You can get your desired boundaries rectangle from http://boundingbox.klokantech.com/. Specify CSV as output format.
* *-z* / *--zoom* - the zoom levels to download
You have to specify the min and max zoom level in he following format: *[min zoom]-[max-zoom]*
* *-o* / *--options* - the options file to use
* *-f* / *--file* - the target MBtile file to write

To download an MBTiles file with the zoom levels 4 to 12 for the Suisse i.e. you would specify *-b 5.9559,45.818,10.4921,47.8084 -z 4-12 -o myoptions.properties -f suisse.mbtiles*
