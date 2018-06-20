package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.processor.PolylineModelData;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.ShaderRendererUtilities;
import io.opensphere.core.util.BufferUtilities;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Wrapper for handling buffering of polyline geometry data that combines
 * multiple polylines into a single buffer.
 */
public class MultiDrawPolylineDataBuffered extends BufferObjectList<BufferObject>
{
    /**
     * Constructor.
     *
     * @param geometryToDataMap Map of polylines to corresponding model data.
     * @param highlight Flag indicating if the geometries should be highlighted.
     * @param pickManager The pick manager used to get pick colors.
     * @param groupTimeSpan If non-null this will be used to upload time
     *            constraint information.
     */
    public MultiDrawPolylineDataBuffered(
            Map<? extends PolylineGeometry, ? extends Collection<? extends PolylineModelData>> geometryToDataMap,
            boolean highlight, PickManager pickManager, TimeSpan groupTimeSpan)
    {
        super(getBufferObjects(geometryToDataMap, highlight, pickManager, groupTimeSpan));
    }

    /**
     * Create the buffer objects for some polylines.
     *
     * @param geometryToDataMap Map of polylines to corresponding model data.
     * @param highlight Flag indicating if the geometries should be highlighted.
     * @param pickManager The pick manager used to get pick colors.
     * @param groupTimeSpan If non-null this will be used to upload time
     *            constraint information.
     * @return The list of buffer objects.
     */
    private static List<? extends BufferObject> getBufferObjects(
            Map<? extends PolylineGeometry, ? extends Collection<? extends PolylineModelData>> geometryToDataMap,
            boolean highlight, PickManager pickManager, TimeSpan groupTimeSpan)
    {
        if (geometryToDataMap.isEmpty())
        {
            return Collections.emptyList();
        }
        List<BufferObject> bufferObjects = New.list(4);

        List<? extends PolylineModelData> data = geometryToDataMap.values().stream().flatMap(c -> c.stream())
                .collect(Collectors.toList());

        int vectorSum = data.stream().mapToInt(d -> d.getVectorCount()).sum();

        if (groupTimeSpan != null && data.stream().anyMatch(d -> d.getTimeSpans() != null))
        {
            long groupStart = groupTimeSpan.getStart();
            long groupEnd = groupTimeSpan.getEnd();
            FloatBuffer timeIntervals = BufferUtilities.newFloatBuffer(vectorSum * 2);

            for (PolylineModelData line : data)
            {
                TimeSpan[] timeSpans = line.getTimeSpans();
                if (timeSpans == null)
                {
                    timeIntervals.put(-Float.MAX_VALUE);
                    timeIntervals.put(Float.MAX_VALUE);
                }
                else
                {
                    for (int index = 0; index < line.getVectorCount(); ++index)
                    {
                        float st = MathUtil.getModulatedFloat(timeSpans[index].getStart(groupStart), groupStart, groupEnd);
                        timeIntervals.put(st);
                        float en = MathUtil.getModulatedFloat(timeSpans[index].getEnd(groupEnd), groupStart, groupEnd);
                        timeIntervals.put(en);
                    }
                }
            }
            bufferObjects.add(new VertexAttributeBufferObject(ShaderRendererUtilities.INTERVAL_FILTER_VERTEX_TIME_ATTRIBUTE_NAME,
                    timeIntervals));
        }

        ByteBuffer colors = BufferUtilities.newByteBuffer(vectorSum * 4);
        ByteBuffer pickColors = BufferUtilities.newByteBuffer(colors.capacity());
        ByteBuffer drawColor = BufferUtilities.newByteBuffer(4);
        ByteBuffer pickColor = BufferUtilities.newByteBuffer(4);
        for (Map.Entry<? extends PolylineGeometry, ? extends Collection<? extends PolylineModelData>> entry : geometryToDataMap
                .entrySet())
        {
            PolylineGeometry geom = entry.getKey();

            ColorBufferUtilities.getColors(geom, geom.getRenderProperties(), highlight, drawColor.rewind());
            pickManager.getPickColor(geom, pickColor.rewind());
            int geomVectorSum = entry.getValue().stream().mapToInt(d -> d.getVectorCount()).sum();
            Utilities.times(geomVectorSum, () ->
            {
                colors.put(drawColor.rewind());
                pickColors.put(pickColor.rewind());
            });
        }

        bufferObjects.add(new ColorBufferObject(colors, 4, RenderMode.DRAW));
        bufferObjects.add(new ColorBufferObject(pickColors, 4, RenderMode.PICK));

        FloatBuffer lineVertexBuffer = FloatBuffer.allocate(vectorSum * 3);
        IntBuffer lineIndexBuffer = IntBuffer.allocate(data.size());
        IntBuffer lineSizeBuffer = IntBuffer.allocate(data.size());
        for (PolylineModelData ld : data)
        {
            lineIndexBuffer.put(lineVertexBuffer.position() / 3);
            lineSizeBuffer.put(ld.getVectorCount());
            lineVertexBuffer.put(ld.getBuffer());
        }
        bufferObjects.add(new MultiDrawVertexBufferObject(lineVertexBuffer, lineIndexBuffer, lineSizeBuffer));

        return bufferObjects;
    }
}
