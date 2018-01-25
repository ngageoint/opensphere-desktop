package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.processor.PolylineModelData;
import io.opensphere.core.pipeline.renderer.buffered.PolylineDataBuffered.PolylineDataBufferedBlock;
import io.opensphere.core.pipeline.util.ShaderRendererUtilities;
import io.opensphere.core.util.BufferUtilities;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;

/**
 * Wrapper for handling buffering of polyline geometry data.
 */
public class PolylineDataBuffered extends BufferObjectList<PolylineDataBufferedBlock>
{
    /**
     * Create buffer objects for each of the polyline pieces.
     *
     * @param data A collection of polylines pieces.
     * @param groupTimeSpan If non-null this will be used to upload time
     *            constraint information.
     * @return The buffered objects for the polyline pieces.
     */
    private static List<? extends PolylineDataBufferedBlock> getBufferObjects(Collection<PolylineModelData> data,
            TimeSpan groupTimeSpan)
    {
        List<PolylineDataBufferedBlock> blocks = New.list();
        for (PolylineModelData datum : data)
        {
            blocks.add(new PolylineDataBufferedBlock(datum, groupTimeSpan));
        }
        return blocks;
    }

    /**
     * Constructor.
     *
     * @param data A collection of polylines pieces.
     * @param groupTimeSpan If non-null this will be used to upload time
     *            constraint information.
     */
    public PolylineDataBuffered(Collection<PolylineModelData> data, TimeSpan groupTimeSpan)
    {
        super(getBufferObjects(data, groupTimeSpan));
    }

    /**
     * Constructor.
     *
     * @param data The model coordinate data.
     * @param groupTimeSpan If non-null this will be used to upload time
     *            constraint information.
     */
    public PolylineDataBuffered(PolylineModelData data, TimeSpan groupTimeSpan)
    {
        super(Collections.singletonList(new PolylineDataBufferedBlock(data, groupTimeSpan)));
    }

    /** A block which backs part or all of the geometry. */
    protected static class PolylineDataBufferedBlock extends BufferObjectList<BufferObject>
    {
        /**
         * Create the buffer objects for this polyline.
         *
         * @param data The model data.
         * @param groupTimeSpan If non-null this will be used to upload time
         *            constraint information.
         *
         * @return The list of buffer objects.
         */
        private static List<? extends BufferObject> getBufferObjects(PolylineModelData data, TimeSpan groupTimeSpan)
        {
            List<BufferObject> bufferObjects = New.list(2);

            TimeSpan[] timeSpans = data.getTimeSpans();
            if (groupTimeSpan != null && timeSpans != null)
            {
                long groupStart = groupTimeSpan.getStart();
                long groupEnd = groupTimeSpan.getEnd();
                int vectorCount = data.getVectorCount();
                FloatBuffer timeIntervals = BufferUtilities.newFloatBuffer(vectorCount * 2);
                for (int index = 0; index < vectorCount; ++index)
                {
                    float st = MathUtil.getModulatedFloat(timeSpans[index].getStart(groupStart), groupStart, groupEnd);
                    timeIntervals.put(st);
                    float en = MathUtil.getModulatedFloat(timeSpans[index].getEnd(groupEnd), groupStart, groupEnd);
                    timeIntervals.put(en);
                }
                bufferObjects.add(new VertexAttributeBufferObject(
                        ShaderRendererUtilities.INTERVAL_FILTER_VERTEX_TIME_ATTRIBUTE_NAME, timeIntervals));
            }
            bufferObjects.add(new VertexBufferObject(data.getBuffer(), true));

            return bufferObjects;
        }

        /**
         * Constructor.
         *
         * @param data The model coordinate data.
         * @param groupTimeSpan If non-null this will be used to upload time
         *            constraint information.
         */
        public PolylineDataBufferedBlock(PolylineModelData data, TimeSpan groupTimeSpan)
        {
            super(getBufferObjects(data, groupTimeSpan));
        }
    }
}
