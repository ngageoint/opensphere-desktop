package io.opensphere.stkterrain.model;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * Contains metadata about a tile set from an STK Terrain Server.
 */
public class TileSetMetadata implements Serializable
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Optional default null. Contains an attribution to be displayed when the
     * map is shown to the user.
     */
    private String myAttribution;

    /**
     * For each tile zoom level, a list of tile ranges are included that defines
     * the tiles that are available in this Tileset. Each tile range is defined
     * by x and y Tile Map Service (TMS) coordinates that bound a range of
     * tiles. Each zoom level, from 0 to maxzoom, will define a list of
     * available Tile Ranges, where the first list of Tile Ranges belongs to TMS
     * level 0, index 1into the available list property defines the Terrain
     * Tiles available for TMS level1, and so on.
     */
    private final List<List<TileRange>> myAvailable = New.list();

    /**
     * Optional default {-180, -90, 180, 90}. The maximum extent of available
     * map tiles. Bounds Must define an area covered by all zoom levels. The
     * bounds are represented in WGS84 latitude and longitude values. In order
     * left bottom right top. Values may be integers or floating point numbers.
     * The terrain server will at a minimum generate terrain tiles for levels 0
     * through 4, in the absence of input elevations, the EGM96 15-minute Geoid
     * is used to fill the remainder of the globe with sea level offsets from
     * the wgs84 ellipsoid. For that reason bounds will always specifythe full
     * WGS84 extents, {-180, -90, 180, 90}.
     */
    private float[] myBounds = new float[] { -180, -90, 180, 90 };

    /**
     * Optional Default null. A text description of the Tileset. The description
     * can contain any legal character implementations should not interperet as
     * html.
     */
    private String myDescription;

    /**
     * Optional. A list of extension data available for each Terrain Tile in
     * this Tileset. These extensions are document in the Quantized-Mesh format
     * document.
     */
    private List<String> myExtensions = New.list();

    /**
     * Describes the data structure used fro Terrain Tile geometry.
     */
    private String myFormat;

    /**
     * Optional default 22. An integer specifying the maximum zoom level.
     */
    private int myMaxzoom = 22;

    /**
     * Optional default 0. An integer specifying the minimum zoom level.
     */
    private int myMinzoom;

    /**
     * Optional Default null. A name describing the tileset. The name can
     * contain any legal character and is not html.
     */
    private String myName;

    /**
     * The coordinate system of the height data defined in the Terrain Tiles.
     * For the Terrain Server, this is always WGS84.
     */
    private String myProjection;

    /**
     * Either xyz or tms. Influences the y direction of the tile coordinates.
     * For the terrain server this is always tms.
     */
    private String myScheme;

    /**
     * A semver.org style version number. Describes the version of the TileJSON
     * spec implemented by this JSON object.
     */
    private String myTilejson;

    /**
     * A list of tile endpoints, {z}, {x}, and {y}, if present, are replaced
     * with the corresponding integers. If multiple endpoints are specified,
     * clients may use any combination of endpoints. All endpoints must return
     * the same sontent for the same URL. The list must contain at least one
     * endpoint.
     */
    private List<String> myTiles = New.list();

    /**
     * A semver.org style version number. When changes across tiles are
     * introduced the minor version will be increased. Implementors can decide
     * to clean their cache when the minor version changes.
     */
    private String myVersion;

    /**
     * Optional default null. Contains an attribution to be displayed when the
     * map is shown to the user.
     *
     * @return the attribution
     */
    public String getAttribution()
    {
        return myAttribution;
    }

    /**
     * For each tile zoom level, a list of tile ranges are included that defines
     * the tiles that are available in this Tileset. Each tile range is defined
     * by x and y Tile Map Service (TMS) coordinates that bound a range of
     * tiles. Each zoom level, from 0 to maxzoom, will define a list of
     * available Tile Ranges, where the first list of Tile Ranges belongs to TMS
     * level 0, index 1into the available list property defines the Terrain
     * Tiles available for TMS level1, and so on.
     *
     * @return the available
     */
    public List<List<TileRange>> getAvailable()
    {
        return myAvailable;
    }

    /**
     * Optional default {-180, -90, 180, 90}. The maximum extent of available
     * map tiles. Bounds Must define an area covered by all zoom levels. The
     * bounds are represented in WGS84 latitude and longitude values. In order
     * left bottom right top. Values may be integers or floating point numbers.
     * The terrain server will at a minimum generate terrain tiles for levels 0
     * through 4, in the absence of input elevations, the EGM96 15-minute Geoid
     * is used to fill the remainder of the globe with sea level offsets from
     * the wgs84 ellipsoid. For that reason bounds will always specifythe full
     * WGS84 extents, {-180, -90, 180, 90}.
     *
     * @return the bounds
     */
    public float[] getBounds()
    {
        float[] returnArray = myBounds;

        return returnArray;
    }

    /**
     * Optional Default null. A text description of the Tileset. The description
     * can contain any legal character implementations should not interperet as
     * html.
     *
     * @return the description
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Optional. A list of extension data available for each Terrain Tile in
     * this Tileset. These extensions are document in the Quantized-Mesh format
     * document.
     *
     * @return the extensions
     */
    public List<String> getExtensions()
    {
        return myExtensions;
    }

    /**
     * Describes the data structure used fro Terrain Tile geometry.
     *
     * @return the format
     */
    public String getFormat()
    {
        return myFormat;
    }

    /**
     * Optional default 22. An integer specifying the maximum zoom level.
     *
     * @return the maxZoom
     */
    public int getMaxzoom()
    {
        return myMaxzoom;
    }

    /**
     * Optional default 0. An integer specifying the minimum zoom level.
     *
     * @return the minZoom
     */
    public int getMinzoom()
    {
        return myMinzoom;
    }

    /**
     * Optional Default null. A name describing the tileset. The name can
     * contain any legal character and is not html.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * The coordinate system of the height data defined in the Terrain Tiles.
     * For the Terrain Server, this is always WGS84.
     *
     * @return the projection
     */
    public String getProjection()
    {
        return myProjection;
    }

    /**
     * Either xyz or tms. Influences the y direction of the tile coordinates.
     * For the terrain server this is always tms.
     *
     * @return the scheme
     */
    public String getScheme()
    {
        return myScheme;
    }

    /**
     * A semver.org style version number. Describes the version of the TileJSON
     * spec implemented by this JSON object.
     *
     * @return the tilejson
     */
    public String getTilejson()
    {
        return myTilejson;
    }

    /**
     * A list of tile endpoints, {z}, {x}, and {y}, if present, are replaced
     * with the corresponding integers. If multiple endpoints are specified,
     * clients may use any combination of endpoints. All endpoints must return
     * the same sontent for the same URL. The list must contain at least one
     * endpoint.
     *
     * @return the tiles
     */
    public List<String> getTiles()
    {
        return myTiles;
    }

    /**
     * A semver.org style version number. When changes across tiles are
     * introduced the minor version will be increased. Implementors can decide
     * to clean their cache when the minor version changes.
     *
     * @return the version
     */
    public String getVersion()
    {
        return myVersion;
    }

    /**
     * Optional default null. Contains an attribution to be displayed when the
     * map is shown to the user.
     *
     * @param attribution the attribution to set
     */
    public void setAttribution(String attribution)
    {
        myAttribution = attribution;
    }

    /**
     * Optional default {-180, -90, 180, 90}. The maximum extent of available
     * map tiles. Bounds Must define an area covered by all zoom levels. The
     * bounds are represented in WGS84 latitude and longitude values. In order
     * left bottom right top. Values may be integers or floating point numbers.
     * The terrain server will at a minimum generate terrain tiles for levels 0
     * through 4, in the absence of input elevations, the EGM96 15-minute Geoid
     * is used to fill the remainder of the globe with sea level offsets from
     * the wgs84 ellipsoid. For that reason bounds will always specifythe full
     * WGS84 extents, {-180, -90, 180, 90}.
     *
     * @param bounds the bounds to set
     */
    public void setBounds(float[] bounds)
    {
        if (bounds != null)
        {
            myBounds = bounds.clone();
        }
        else
        {
            myBounds = null;
        }
    }

    /**
     * Optional Default null. A text description of the Tileset. The description
     * can contain any legal character implementations should not interperet as
     * html.
     *
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Optional. A list of extension data available for each Terrain Tile in
     * this Tileset. These extensions are document in the Quantized-Mesh format
     * document.
     *
     * @param extensions the extensions to set
     */
    public void setExtensions(List<String> extensions)
    {
        myExtensions = extensions;
    }

    /**
     * Describes the data structure used fro Terrain Tile geometry.
     *
     * @param format the format to set
     */
    public void setFormat(String format)
    {
        myFormat = format;
    }

    /**
     * Optional default 22. An integer specifying the maximum zoom level.
     *
     * @param maxzoom the maxZoom to set
     */
    public void setMaxzoom(int maxzoom)
    {
        myMaxzoom = maxzoom;
    }

    /**
     * Optional default 0. An integer specifying the minimum zoom level.
     *
     * @param minzoom the minZoom to set
     */
    public void setMinZoom(int minzoom)
    {
        myMinzoom = minzoom;
    }

    /**
     * Optional Default null. A name describing the tileset. The name can
     * contain any legal character and is not html.
     *
     * @param name the name to set
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * The coordinate system of the height data defined in the Terrain Tiles.
     * For the Terrain Server, this is always WGS84.
     *
     * @param projection the projection to set
     */
    public void setProjection(String projection)
    {
        myProjection = projection;
    }

    /**
     * Either xyz or tms. Influences the y direction of the tile coordinates.
     * For the terrain server this is always tms.
     *
     * @param scheme the scheme to set
     */
    public void setScheme(String scheme)
    {
        myScheme = scheme;
    }

    /**
     * A semver.org style version number. Describes the version of the TileJSON
     * spec implemented by this JSON object.
     *
     * @param tilejson the tilejson to set
     */
    public void setTilejson(String tilejson)
    {
        myTilejson = tilejson;
    }

    /**
     * A list of tile endpoints, {z}, {x}, and {y}, if present, are replaced
     * with the corresponding integers. If multiple endpoints are specified,
     * clients may use any combination of endpoints. All endpoints must return
     * the same sontent for the same URL. The list must contain at least one
     * endpoint.
     *
     * @param tiles the tiles to set
     */
    public void setTiles(List<String> tiles)
    {
        myTiles = tiles;
    }

    /**
     * A semver.org style version number. When changes across tiles are
     * introduced the minor version will be increased. Implementors can decide
     * to clean their cache when the minor version changes.
     *
     * @param version the version to set
     */
    public void setVersion(String version)
    {
        myVersion = version;
    }
}
