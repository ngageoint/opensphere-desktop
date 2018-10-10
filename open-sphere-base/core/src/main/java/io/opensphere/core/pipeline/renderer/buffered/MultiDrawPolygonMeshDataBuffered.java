package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.processor.PolygonMeshData;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.ShaderRendererUtilities;
import io.opensphere.core.pipeline.util.VectorBufferUtilities;
import io.opensphere.core.util.BufferUtilities;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Wrapper for handling buffering of polygon mesh geometry data, packing
 * multiple geometries into each buffer.
 */
public class MultiDrawPolygonMeshDataBuffered extends BufferObjectList<BufferObject>
{
    /** How many vertices doth a quad have. */
    private static final int QUAD_VERTEX_COUNT = 4;

    /** How many vertices doth a triangle have. */
    private static final int TRIANGLE_VERTEX_COUNT = 3;

    /** The number of vertices per tessera. */
    private final int myTesseraVertexCount;

    /**
     * Populate time interval buffer for a geometry.
     *
     * @param groupTimeSpan The group time span.
     * @param geom The geom.
     * @param datum The datum.
     * @param timeIntervals The time intervals to add to.
     */
    private static void addTimeIntervals(TimeSpan groupTimeSpan, PolygonGeometry geom, PolygonMeshData datum,
            FloatBuffer timeIntervals)
    {
        long groupStart = groupTimeSpan.getStart();
        long groupEnd = groupTimeSpan.getEnd();

        float geomStart;
        float geomEnd;
        if (geom.getConstraints() != null && geom.getConstraints().getTimeConstraint() != null)
        {
            TimeSpan geomTimeSpan = geom.getConstraints().getTimeConstraint().getTimeSpan();
            geomStart = MathUtil.getModulatedFloat(geomTimeSpan.getStart(groupStart), groupStart, groupEnd);
            geomEnd = MathUtil.getModulatedFloat(geomTimeSpan.getEnd(groupEnd), groupStart, groupEnd);
        }
        else
        {
            geomStart = -Float.MAX_VALUE;
            geomEnd = Float.MAX_VALUE;
        }
        Utilities.times(datum.getVertexData().getModelCoords().size(), () ->
        {
            timeIntervals.put(geomStart);
            timeIntervals.put(geomEnd);
        });
    }

    /**
     * Create the buffer objects for this block.
     *
     * @param geometries The geometries.
     * @param data The polygon mesh data.
     * @param highlight Flag indicating if the geometries should be highlighted.
     * @param pickManager The pick manager used to get pick colors.
     * @param groupTimeSpan If non-null this will be used to upload time
     *            constraint information.
     * @return The list of buffer objects.
     */
    private static List<? extends BufferObject> getBufferObjects(Collection<? extends PolygonGeometry> geometries,
            Collection<? extends PolygonMeshData> data, boolean highlight, PickManager pickManager, TimeSpan groupTimeSpan)
    {
        if (geometries.size() != data.size())
        {
            throw new IllegalArgumentException("Geometries size must match model data size.");
        }

        List<BufferObject> bufferObjects = New.list(6);

        int vertexCount = data.stream().mapToInt(d -> d.getVertexData().getModelCoords().size()).sum();

        FloatBuffer modelCoords = BufferUtilities.newFloatBuffer(vertexCount * 3);
        FloatBuffer normals = data.stream().anyMatch(d -> d.getVertexData().getNormals() != null)
                ? BufferUtilities.newFloatBuffer(vertexCount * 3) : null;
                ByteBuffer colors = BufferUtilities.newByteBuffer(vertexCount * 4);
                ByteBuffer pickColors = BufferUtilities.newByteBuffer(vertexCount * 4);
                Collection<IntBuffer> indices = New.collection(geometries.size());
                ByteBuffer drawColor = ByteBuffer.allocate(4);
                ByteBuffer pickColor = ByteBuffer.allocate(4);
                FloatBuffer timeIntervals = groupTimeSpan == null ? null : BufferUtilities.newFloatBuffer(vertexCount * 2);

                int vertexIndex = 0;
                Iterator<? extends PolygonGeometry> geomIter = geometries.iterator();
                Iterator<? extends PolygonMeshData> dataIter = data.iterator();
                while (dataIter.hasNext())
                {
                    PolygonGeometry geom = geomIter.next();
                    PolygonMeshData datum = dataIter.next();
                    VectorBufferUtilities.vec3dToFloatBuffer(datum.getVertexData().getModelCoords(), modelCoords);

                    if (normals != null)
                    {
                        VectorBufferUtilities.vec3dToFloatBuffer(datum.getVertexData().getNormals(), normals);
                    }

                    ColorBufferUtilities.getColors(geom, geom.getRenderProperties().getFillColorRenderProperties(), highlight,
                            drawColor.rewind());

                    int geomVertexCount = datum.getVertexData().getModelCoords().size();
                    pickManager.getPickColor(geom, pickColor.rewind());
                    Utilities.times(geomVertexCount, () ->
                    {
                        colors.put(drawColor.rewind());
                        pickColors.put(pickColor.rewind());
                    });

                    if (timeIntervals != null && groupTimeSpan != null)
                    {
                        addTimeIntervals(groupTimeSpan, geom, datum, timeIntervals);
                    }

                    indices.add(getIndexBuffer(vertexIndex, datum, geomVertexCount));

                    vertexIndex += geomVertexCount;
                }

                if (normals != null)
                {
                    bufferObjects.add(new NormalBufferObject(normals));
                }

                bufferObjects.add(new ColorBufferObject(colors, 4, AbstractGeometry.RenderMode.DRAW));
                bufferObjects.add(new ColorBufferObject(pickColors, 4, AbstractGeometry.RenderMode.PICK));

                if (timeIntervals != null)
                {
                    bufferObjects.add(new VertexAttributeBufferObject(ShaderRendererUtilities.INTERVAL_FILTER_VERTEX_TIME_ATTRIBUTE_NAME,
                            timeIntervals));
                }
                bufferObjects.add(new VertexBufferObject(modelCoords, false));
                bufferObjects.add(new MultiDrawIndexBufferObject(indices));

                return bufferObjects;
    }

    /**
     * Create an index buffer for the given {@link PolygonMeshData}.
     *
     * @param offset The offset to add to the indices in the datum.
     * @param datum The geometry datum.
     * @param geomVertexCount The number of vertices in the geometry, to be used
     *            when no indices have been predefined.
     * @return The buffer.
     */
    private static IntBuffer getIndexBuffer(int offset, PolygonMeshData datum, int geomVertexCount)
    {
        int indexCount = datum.getModelIndices() == null ? geomVertexCount : datum.getModelIndices().size();
        IntBuffer indexBuffer = ByteBuffer.allocateDirect(indexCount * Integer.BYTES).order(ByteOrder.nativeOrder())
                .asIntBuffer();
        if (datum.getModelIndices() != null)
        {
            datum.getModelIndices().forEach(value ->
            {
                indexBuffer.put(value + offset);
                return true;
            });
        }
        else
        {
            BufferUtilities.addSequenceToBuffer(offset, indexCount, indexBuffer);
        }
        return indexBuffer.flip();
    }

    /**
     * Constructor.
     *
     * @param geometries The geometries.
     * @param meshData The mesh data.
     * @param highlight Flag indicating if the geometries should be highlighted.
     * @param pickManager The pick manager used to get pick colors.
     * @param groupTimeSpan If non-null this will be used to upload time
     *            constraint information.
     * @param tesseraVertexCount The number of vertices per tessera.
     */
    public MultiDrawPolygonMeshDataBuffered(Collection<? extends PolygonGeometry> geometries,
            Collection<? extends PolygonMeshData> meshData, boolean highlight, PickManager pickManager, TimeSpan groupTimeSpan,
            int tesseraVertexCount)
    {
        super(getBufferObjects(geometries, meshData, highlight, pickManager, groupTimeSpan));
        myTesseraVertexCount = tesseraVertexCount;
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
     * Get the draw mode for this polygon mesh.
     *
     * @return The draw mode.
     */
    protected int getDrawMode()
    {
        int drawMode;
        switch (myTesseraVertexCount)
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
