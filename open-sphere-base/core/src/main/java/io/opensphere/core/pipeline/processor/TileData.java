package io.opensphere.core.pipeline.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;

/**
 * Encapsulates the data required by tile renderers to render tile geometries.
 */
public class TileData implements SizeProvider, AbstractRenderer.ModelData
{
    /** The image texture coordinates used to generate this tile data. */
    private final TextureCoords myImageTextureCoords;

    /** The meshes which back the tile. */
    private final List<? extends TileMeshData> myMeshes;

    /** The hash code of the projection form which my data originates. */
    private final int mySourceProjectionHash;

    /**
     * Create the tile data.
     *
     * @param imageTexCoords The image texture coordinates for the tile.
     * @param meshes The meshes which back the tile.
     * @param sourceProjectionHash The hash code of the projection form which my
     *            data originates.
     */
    public TileData(TextureCoords imageTexCoords, List<TileMeshData> meshes, int sourceProjectionHash)
    {
        mySourceProjectionHash = sourceProjectionHash;
        myImageTextureCoords = imageTexCoords;
        myMeshes = New.unmodifiableList(meshes);
    }

    /**
     * Create the tile data.
     *
     * @param imageTexCoords The image texture coordinates for the tile.
     * @param modelCoords The model coordinates of each vertex.
     * @param textureCoords The texture coordinates.
     * @param modelIndices The indices for the texture coordinates to match the
     *            model coords.
     * @param tesseraVertexCount The number of vertices in each tessera.
     * @param sourceProjectionHash The hash code of the projection form which my
     *            data originates.
     */
    public TileData(TextureCoords imageTexCoords, List<Vector3d> modelCoords, List<Vector2d> textureCoords,
            PetrifyableTIntList modelIndices, int tesseraVertexCount, int sourceProjectionHash)
    {
        mySourceProjectionHash = sourceProjectionHash;
        myImageTextureCoords = imageTexCoords;
        PolygonMeshData meshData = new PolygonMeshData(modelCoords, null, modelIndices, null, null, tesseraVertexCount, false);
        myMeshes = Collections.singletonList(new TileMeshData(meshData, textureCoords));
    }

    /**
     * Get the image texture coordinates used to generate this tile data.
     *
     * @return The coordinates.
     */
    public TextureCoords getImageTextureCoords()
    {
        return myImageTextureCoords;
    }

    /**
     * Get the meshes.
     *
     * @return the meshes
     */
    public List<? extends TileMeshData> getMeshes()
    {
        return myMeshes;
    }

    @Override
    public long getSizeBytes()
    {
        long imageTexCoordSize = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.FLOAT_SIZE_BYTES * 4,
                Constants.MEMORY_BLOCK_SIZE_BYTES);
        long size = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES * 3,
                Constants.MEMORY_BLOCK_SIZE_BYTES);
        size += Utilities.sizeOfArrayListBytes(myMeshes.size());
        for (TileMeshData mesh : myMeshes)
        {
            size += Constants.REFERENCE_SIZE_BYTES + mesh.getSizeBytes();
        }
        return size + imageTexCoordSize;
    }

    /**
     * Get the sourceProjectionHash.
     *
     * @return the sourceProjectionHash
     */
    public int getSourceProjectionHash()
    {
        return mySourceProjectionHash;
    }

    /** Mesh data which backs the tile. */
    public static class TileMeshData implements SizeProvider
    {
        /** The polygon mesh which backs this tile. */
        private final PolygonMeshData myMeshData;

        /** The texture coordinates for each vertex. */
        private final List<Vector2d> myTextureCoords;

        /**
         * Constructor.
         *
         * @param meshData The polygon mesh which backs this tile.
         * @param textureCoords The texture coordinates.
         */
        public TileMeshData(PolygonMeshData meshData, List<Vector2d> textureCoords)
        {
            myMeshData = meshData;
            if (textureCoords == null)
            {
                myTextureCoords = null;
            }
            else
            {
                assert !textureCoords.isEmpty();
                myTextureCoords = Collections.unmodifiableList(new ArrayList<Vector2d>(textureCoords));
            }
        }

        /**
         * Get the meshData.
         *
         * @return the meshData
         */
        public PolygonMeshData getMeshData()
        {
            return myMeshData;
        }

        @Override
        public long getSizeBytes()
        {
            return MathUtil.roundUpTo(PolygonMeshData.CORE_SIZE + 2 * Constants.REFERENCE_SIZE_BYTES,
                    Constants.MEMORY_BLOCK_SIZE_BYTES) + getReferencedObjectSize();
        }

        /**
         * Get the texture coordinates for this tile.
         *
         * @return The texture coordinates.
         */
        public List<Vector2d> getTextureCoords()
        {
            return myTextureCoords;
        }

        /**
         * Get the size of my referenced objects, in bytes.
         *
         * @return The size of the references objects in bytes.
         */
        protected long getReferencedObjectSize()
        {
            long size = myMeshData.getSizeBytes();
            if (myTextureCoords != null)
            {
                size += Utilities.sizeOfArrayListBytes(myTextureCoords.size()) + myTextureCoords.size() * Vector2d.SIZE_BYTES;
            }
            return size;
        }
    }
}
