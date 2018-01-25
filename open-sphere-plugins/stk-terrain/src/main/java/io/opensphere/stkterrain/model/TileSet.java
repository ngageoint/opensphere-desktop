package io.opensphere.stkterrain.model;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * A terrain tileset representing a single terrain layer from an STK Terrain
 * Server.
 */
public class TileSet implements Serializable
{
    /**
     * Default serialization Id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * A list of input data sources that were incorporated into this terrain
     * tileset.
     */
    private final List<TileSetDataSource> myDataSources = New.list();

    /**
     * A text description of the tileset.
     */
    private String myDescription;

    /**
     * A name describing the terrain tileset.
     */
    private String myName;

    /**
     * Gets a list of input data sources that were incorporated into this
     * terrain tileset.
     *
     * @return A list of input data sources that were incorporated into this
     *         terrain tileset.
     */
    public List<TileSetDataSource> getDataSources()
    {
        return myDataSources;
    }

    /**
     * Gets a text description of the tileset.
     *
     * @return A text description of the tileset.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Gets a name describing the terrain tileset.
     *
     * @return A name describing the terrain tileset.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Sets a text description of the tileset.
     *
     * @param description A text description of the tileset.
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Sets a name describing the terrain tileset.
     *
     * @param name a name describing the terrain tileset.
     */
    public void setName(String name)
    {
        myName = name;
    }
}
