package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.pipeline.processor.PolygonMeshData;
import io.opensphere.core.pipeline.processor.TileData;
import io.opensphere.core.pipeline.processor.TileData.TileMeshData;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.VectorBufferUtilities;
import io.opensphere.core.util.BufferUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Wrapper for handling buffering of tile geometry data.
 */
public class TileDataBuffered extends BufferObjectList<BufferObject>
{
    /**
     * Buffered blocks which come from petrified polygon meshes. These meshes
     * may span multiple projection snapshots; using a weak map ensures that
     * they will not be removed until all associated projection snapshots are no
     * longer in use.
     */
    private static final Map<PolygonMeshData, TileDataBufferedBlock> ourPetrifiedMeshData = New.weakMap();

    /**
     * The data from which this buffer was built. Once the buffers are bound
     * this should be cleared.
     */
    private TileData myDataSource;

    /** The image texture coordinates used to generate the tile data. */
    private final TextureCoords myImageTextureCoords;

    /**
     * The buffer objects which contain the vertices for the polygon mesh and
     * the indices which specify how to use the vertices.
     */
    private final BufferObjectList<TileDataBufferedBlock> myPolygonMeshObjects;

    /** The hash code of the projection form which my data originates. */
    private final int mySourceProjectionHash;

    /**
     * The buffer objects for the texture coordinates. These must match the
     * number and order of the mesh objects, <code>null</code> may be used when
     * there are no texture coordinates for the block.
     */
    private final BufferObjectList<TextureCoordinateBufferObject> myTextureCoordinateObjects;

    /**
     * Set up the client-side buffers.
     *
     * @param td The tile data to use.
     * @return The buffer objects.
     */
    private static List<? extends TileDataBufferedBlock> getBufferObjects(TileData td)
    {
        List<TileDataBufferedBlock> blocks = New.list(td.getMeshes().size());
        List<BufferObject> bufferObjects = New.list(2);
        for (TileMeshData mesh : td.getMeshes())
        {
            PolygonMeshData data = mesh.getMeshData();
            TileDataBufferedBlock block = null;
            if (data.isPetrified())
            {
                block = ourPetrifiedMeshData.get(data);
            }

            if (block == null)
            {
                FloatBuffer modelCoords;
                IntBuffer indices;
                modelCoords = VectorBufferUtilities.vec3dToFloatBuffer(data.getVertexData().getModelCoords());
                if (data.getModelIndices() != null)
                {
                    indices = BufferUtilities.toIntBuffer(data.getModelIndices());
                }
                else
                {
                    indices = BufferUtilities.newSequentialIntBuffer(0, data.getVertexData().getModelCoords().size());
                }

                bufferObjects.add(new VertexBufferObject(modelCoords, false));
                bufferObjects.add(new IndexBufferObject(indices));

                block = new TileDataBufferedBlock(bufferObjects, data.getTesseraVertexCount());
            }

            if (data.isPetrified())
            {
                ourPetrifiedMeshData.put(data, block);
            }

            blocks.add(block);
            bufferObjects.clear();
        }
        return blocks;
    }

    /**
     * Create the texture coordinate buffers from the given tile data.
     *
     * @param td The tile data which contains the texture coordinates.
     * @return The texture coordinates from the tile data's meshes.
     */
    private static List<? extends TextureCoordinateBufferObject> getTextureCoordinates(TileData td)
    {
        List<TextureCoordinateBufferObject> blocks = New.list(td.getMeshes().size());
        for (TileMeshData data : td.getMeshes())
        {
            if (data.getTextureCoords() == null || data.getTextureCoords().isEmpty())
            {
                blocks.add(null);
            }
            else
            {
                FloatBuffer textureCoords = VectorBufferUtilities.vec2dtoFloatBuffer(data.getTextureCoords());
                blocks.add(new TextureCoordinateBufferObject(textureCoords));
            }
        }
        return blocks;
    }

    /**
     * Construct the object.
     *
     * @param data the tile data
     */
    public TileDataBuffered(TileData data)
    {
        this(data.getImageTextureCoords(), new BufferObjectList<TileDataBufferedBlock>(getBufferObjects(data)),
                new BufferObjectList<TextureCoordinateBufferObject>(getTextureCoordinates(data)), data.getSourceProjectionHash());
        myDataSource = data;
    }

    /**
     * Construct the object.
     *
     * @param imageTexCoords The image texture coordinates for the tile.
     * @param polygonMeshObjects The polygon mesh buffers.
     * @param textureCoordinateObjects The texture coordinate buffers.
     * @param sourceProjectionHash The hash code of the projection form which my
     *            data originates.
     */
    protected TileDataBuffered(TextureCoords imageTexCoords, BufferObjectList<TileDataBufferedBlock> polygonMeshObjects,
            BufferObjectList<TextureCoordinateBufferObject> textureCoordinateObjects, int sourceProjectionHash)
    {
        super(new BufferObjectList<>(CollectionUtilities.<BufferObject>concat(New.<BufferObject>listFactory(),
                polygonMeshObjects.getBufferObjects(), textureCoordinateObjects)));
        myImageTextureCoords = imageTexCoords;
        myPolygonMeshObjects = polygonMeshObjects;
        myTextureCoordinateObjects = textureCoordinateObjects;
        mySourceProjectionHash = sourceProjectionHash;
    }

    /**
     * Bind and draw the buffers for this tile.
     *
     * @param rc The OpenGL interface.
     * @return {@code true} if anything was drawn.
     */
    public boolean draw(RenderContext rc)
    {
        myDataSource = null;
        boolean result = false;
        for (int index = 0; index < myPolygonMeshObjects.size(); ++index)
        {
            TextureCoordinateBufferObject texCoords = myTextureCoordinateObjects.get(index);
            if (texCoords != null)
            {
                texCoords.bind(rc);
            }

            TileDataBufferedBlock block = myPolygonMeshObjects.get(index);
            result |= block.draw(rc);
        }
        return result;
    }

    /**
     * Get the dataSource.
     *
     * @return the dataSource
     */
    public TileData getDataSource()
    {
        return myDataSource;
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
     * Get the polygonMeshObjects.
     *
     * @return the polygonMeshObjects
     */
    public BufferObjectList<TileDataBufferedBlock> getPolygonMeshObjects()
    {
        return myPolygonMeshObjects;
    }

    @Override
    public long getSizeGPU()
    {
        return myPolygonMeshObjects.getSizeGPU() + myTextureCoordinateObjects.getSizeGPU();
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

    /**
     * Get the textureCoordinateObjects.
     *
     * @return the textureCoordinateObjects
     */
    public BufferObjectList<TextureCoordinateBufferObject> getTextureCoordinateObjects()
    {
        return myTextureCoordinateObjects;
    }

    /** A block which backs part or all of the tile. */
    protected static class TileDataBufferedBlock extends BufferObjectList<BufferObject>
    {
        /** How many vertices doth a quad have. */
        protected static final int QUAD_VERTEX_COUNT = 4;

        /** How many vertices doth a triangle have. */
        protected static final int TRIANGLE_VERTEX_COUNT = 3;

        /** The number of vertices in each tessera. */
        private final int myTesseraVertexCount;

        /**
         * Constructor.
         *
         * @param bufferObjects The buffer objects for this block.
         * @param tesseraVertexCount The number of vertices per tessera.
         */
        public TileDataBufferedBlock(List<? extends BufferObject> bufferObjects, int tesseraVertexCount)
        {
            super(bufferObjects);
            myTesseraVertexCount = tesseraVertexCount;
        }

        /**
         * Bind and draw the buffers for this block.
         *
         * @param rc The render context.
         * @return {@code true} if anything was drawn.
         */
        public boolean draw(RenderContext rc)
        {
            return super.draw(rc, getDrawMode());
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
         * Get the draw mode for this block.
         *
         * @return The draw mode.
         */
        protected int getDrawMode()
        {
            int drawMode;
            switch (getTesseraVertexCount())
            {
                case TRIANGLE_VERTEX_COUNT:
                    drawMode = GL.GL_TRIANGLES;
                    break;
                case QUAD_VERTEX_COUNT:
                    drawMode = GL2GL3.GL_QUADS;
                    break;
                default:
                    drawMode = GL2.GL_POLYGON;
                    break;
            }
            return drawMode;
        }
    }
}
