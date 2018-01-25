package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.util.List;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ColorArrayList;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Encapsulates the data required by tile renderers to render polygon mesh
 * geometries.
 */
public class VertexData implements SizeProvider
{
    /** The colors for each vertex. */
    private final List<? extends Color> myColors;

    /** The model coordinates of each vertex. */
    private final List<? extends Vector3d> myModelCoords;

    /** Lighting normals. */
    private final List<? extends Vector3d> myNormals;

    /**
     * Texture coordinates.
     */
    private final List<? extends Vector2d> myTextureCoords;

    /**
     * Create the tile data.
     *
     * @param modelCoords The model coordinates of each vertex.
     * @param normals Lighting normals.
     * @param colors The vertex colors.
     * @param textureCoords The texture coordinates.
     */
    public VertexData(List<? extends Vector3d> modelCoords, List<? extends Vector3d> normals, List<? extends Color> colors,
            List<? extends Vector2d> textureCoords)
    {
        myModelCoords = New.unmodifiableList(modelCoords);
        myNormals = New.unmodifiableList(normals);
        myColors = New.unmodifiableList(colors);
        myTextureCoords = New.unmodifiableList(textureCoords);
    }

    /**
     * Standard getter.
     *
     * @return The colors.
     */
    public List<? extends Color> getColors()
    {
        return myColors;
    }

    /**
     * Get the model coordinates for each vertex.
     *
     * @return The model coordinate list.
     */
    public List<? extends Vector3d> getModelCoords()
    {
        return myModelCoords;
    }

    /**
     * Get the normals.
     *
     * @return the normals
     */
    public List<? extends Vector3d> getNormals()
    {
        return myNormals;
    }

    @Override
    public long getSizeBytes()
    {
        return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES * 3,
                Constants.MEMORY_BLOCK_SIZE_BYTES)
                + (myModelCoords == null ? 0
                        : Utilities.sizeOfArrayListBytes(myModelCoords.size()) + myModelCoords.size() * Vector3d.SIZE_BYTES)
                + (myNormals == null ? 0
                        : Utilities.sizeOfArrayListBytes(myNormals.size()) + myNormals.size() * Vector3d.SIZE_BYTES)
                + (myColors == null ? 0 : myColors instanceof ColorArrayList ? ((ColorArrayList)myColors).getSizeBytes()
                        : Utilities.sizeOfArrayListBytes(myColors.size()) + myColors.size() * Vector3d.SIZE_BYTES);
    }

    /**
     * Get the texture coordinates for each vertex.
     *
     * @return The texture coordinates.
     */
    public List<? extends Vector2d> getTextureCoords()
    {
        return myTextureCoords;
    }
}
