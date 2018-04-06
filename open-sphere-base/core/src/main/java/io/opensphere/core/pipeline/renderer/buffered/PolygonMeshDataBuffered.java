package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.model.ColorArrayList;
import io.opensphere.core.pipeline.processor.PolygonMeshData;
import io.opensphere.core.pipeline.renderer.buffered.PolygonMeshDataBuffered.PolygonMeshBufferedBlock;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.VectorBufferUtilities;
import io.opensphere.core.util.BufferUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Wrapper for handling buffering of polyline geometry data.
 */
public class PolygonMeshDataBuffered extends BufferObjectList<PolygonMeshBufferedBlock>
{
    /**
     * Constructor.
     *
     * @param data the mesh data
     */
    public PolygonMeshDataBuffered(PolygonMeshData data)
    {
        super(Collections.singletonList(new PolygonMeshBufferedBlock(data)));
    }

    /**
     * Bind and draw the buffers for this polygon mesh.
     *
     * @param rc The render context.
     * @return The draw mode.
     */
    public boolean draw(RenderContext rc)
    {
        return get(0).draw(rc);
    }

    /** A block which backs part or all of the geometry. */
    protected static class PolygonMeshBufferedBlock extends BufferObjectList<BufferObject>
    {
        /** How many vertices doth a quad have. */
        private static final int QUAD_VERTEX_COUNT = 4;

        /** How many vertices doth a triangle have. */
        private static final int TRIANGLE_VERTEX_COUNT = 3;

        /**
         * How many vertices does a single triangle have in a triangle strip.
         */
        private static final int TRIANGLE_STRIP_VERTEX_COUNT = 2;

        /** The tile data. */
        private final PolygonMeshData myPolygonMeshData;

        /**
         * Create the buffer objects for this block.
         *
         * @param data The polygon mesh data.
         * @return The list of buffer objects.
         */
        private static List<? extends BufferObject> getBufferObjects(PolygonMeshData data)
        {
            List<BufferObject> bufferObjects = New.list(4);

            FloatBuffer modelCoords = VectorBufferUtilities.vec3dToFloatBuffer(data.getVertexData().getModelCoords());

            if (data.getVertexData().getNormals() != null)
            {
                bufferObjects
                        .add(new NormalBufferObject(VectorBufferUtilities.vec3dToFloatBuffer(data.getVertexData().getNormals())));
            }
            if (data.getVertexData().getColors() != null && data.getVertexData().getTextureCoords() == null)
            {
                ColorArrayList colorArrayList = ColorArrayList.getColorArrayList(data.getVertexData().getColors());
                bufferObjects.add(new ColorBufferObject(ByteBuffer.wrap(colorArrayList.getBytes()),
                        colorArrayList.getBytesPerColor(), AbstractGeometry.RenderMode.DRAW));
            }
            else if (data.getVertexData().getTextureCoords() != null)
            {
                FloatBuffer textureCoords = VectorBufferUtilities.vec2dtoFloatBuffer(data.getVertexData().getTextureCoords());
                bufferObjects.add(new TextureCoordinateBufferObject(textureCoords));
            }

            IntBuffer indices;
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

            return bufferObjects;
        }

        /**
         * Constructor.
         *
         * @param data the tile data
         */
        public PolygonMeshBufferedBlock(PolygonMeshData data)
        {
            super(getBufferObjects(data));
            myPolygonMeshData = data;
        }

        /**
         * Bind and draw the buffers for this polygon mesh.
         *
         * @param rc The render context.
         * @return {@code true} if anything was drawn.
         */
        public boolean draw(RenderContext rc)
        {
            return draw(rc, getDrawMode());
        }

        /**
         * Get the polygon mesh data.
         *
         * @return The polygon mesh data.
         */
        public PolygonMeshData getPolygonMeshData()
        {
            return myPolygonMeshData;
        }

        /**
         * Get the draw mode for this polygon mesh.
         *
         * @return The draw mode.
         */
        protected int getDrawMode()
        {
            int drawMode;
            switch (myPolygonMeshData.getTesseraVertexCount())
            {
                case TRIANGLE_VERTEX_COUNT:
                    drawMode = GL.GL_TRIANGLES;
                    break;
                case QUAD_VERTEX_COUNT:
                    drawMode = GL2.GL_QUADS;
                    break;
                case TRIANGLE_STRIP_VERTEX_COUNT:
                    drawMode = GL2.GL_TRIANGLE_STRIP;
                    break;
                default:
                    drawMode = GL2.GL_POLYGON;
                    break;
            }
            return drawMode;
        }
    }
}
