package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.util.List;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;

/**
 * Encapsulates the data required by tile renderers to render polygon mesh
 * geometries.
 */
public class PolygonMeshData implements SizeProvider, AbstractRenderer.ModelData
{
    /**
     * The core size of this object, not counting other objects I reference and
     * not rounded up to the nearest word.
     */
    protected static final int CORE_SIZE = Constants.OBJECT_SIZE_BYTES + 2 * Constants.REFERENCE_SIZE_BYTES
            + 2 * Constants.BOOLEAN_SIZE_BYTES + Constants.INT_SIZE_BYTES;

    /** List of indices indicating the order to draw the vertices. */
    private final PetrifyableTIntList myModelIndices;

    /** True when I a the only one using the vertex data. */
    private final boolean myOwnVertexData;

    /**
     * True when this comes from a set of petrified tesserae and may span
     * multiple projection snapshots.
     */
    private final boolean myPetrified;

    /** The number of vertices in each tessera. */
    private final int myTesseraVertexCount;

    /** Vertex data. */
    private final VertexData myVertexData;

    /**
     * Constructor.
     *
     * @param modelCoords The model coordinates of each vertex.
     * @param normals Lighting normals.
     * @param modelIndices The order to draw the vertices.
     * @param colors The colors for each vertex.
     * @param textureCoords The texture coordinates for each vertex.
     * @param tesseraVertexCount The number of vertices in each tessera.
     * @param petrified When true this comes from a set of tesserae which are
     *            immutable and may span multiple projection snapshots.
     */
    public PolygonMeshData(List<? extends Vector3d> modelCoords, List<? extends Vector3d> normals,
            PetrifyableTIntList modelIndices, List<? extends Color> colors, List<? extends Vector2d> textureCoords,
            int tesseraVertexCount, boolean petrified)
    {
        myVertexData = new VertexData(modelCoords, normals, colors, textureCoords);
        myOwnVertexData = true;
        myPetrified = petrified;

        myTesseraVertexCount = tesseraVertexCount;
        myModelIndices = modelIndices;
        if (modelIndices != null)
        {
            myModelIndices.petrify();
        }
    }

    /**
     * Constructor. This constructor should be used when the vertex data is
     * shared.
     *
     * @param vertexData Vertex data.
     * @param modelIndices The order to draw the vertices.
     * @param tesseraVertexCount The number of vertices in each tessera.
     * @param petrified When true this comes from a set of tesserae which are
     *            immutable and may span multiple projection snapshots.
     */
    public PolygonMeshData(VertexData vertexData, PetrifyableTIntList modelIndices, int tesseraVertexCount, boolean petrified)
    {
        myVertexData = vertexData;
        myOwnVertexData = false;
        myPetrified = petrified;

        myTesseraVertexCount = tesseraVertexCount;
        if (modelIndices == null)
        {
            myModelIndices = null;
        }
        else
        {
            myModelIndices = modelIndices;
            myModelIndices.petrify();
        }
    }

    /**
     * Get the order that the vertices should be drawn.
     *
     * @return A list of vertex indices.
     */
    public PetrifyableTIntList getModelIndices()
    {
        return myModelIndices;
    }

    @Override
    public long getSizeBytes()
    {
        return MathUtil.roundUpTo(CORE_SIZE, Constants.MEMORY_BLOCK_SIZE_BYTES) + getReferencedObjectSize();
    }

    /**
     * Get the number of vertices in each tessera.
     *
     * @return The vertex count.
     */
    public int getTesseraVertexCount()
    {
        return myTesseraVertexCount;
    }

    /**
     * Get the vertexData.
     *
     * @return the vertexData
     */
    public VertexData getVertexData()
    {
        return myVertexData;
    }

    /**
     * Get the petrified.
     *
     * @return the petrified
     */
    public boolean isPetrified()
    {
        return myPetrified;
    }

    /**
     * Get the size of my referenced objects, in bytes.
     *
     * @return The size of the references objects in bytes.
     */
    protected long getReferencedObjectSize()
    {
        long vertexDataSize = myOwnVertexData ? myVertexData.getSizeBytes() : 0L;
        long intArraySize = myModelIndices == null ? 0 : myModelIndices.getSizeBytes();
        return vertexDataSize + intArraySize;
    }
}
