package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.processor.PointProcessor.ModelCoordinates;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelDataRetriever;
import io.opensphere.core.pipeline.renderer.buffered.PointDataBuffered.PointDataBufferedBlock;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.ShaderRendererUtilities;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.BufferUtilities;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;

/**
 * Wrapper for handling buffering of point geometry data.
 */
public class PointDataBuffered extends BufferObjectList<PointDataBufferedBlock>
{
    /**
     * Constructor.
     *
     * @param geometries The geometries.
     * @param highlight Flag indicating if the points should be highlighted.
     * @param pickManager The pick manager used to get pick colors.
     * @param dataRetriever Component that provides the model coordinates for
     *            the points.
     * @param projectionSnapshot If non-null this snapshot will be used when
     *            creating model data.
     * @param groupTimeSpan If non-null this will be used to upload time
     *            constraint information.
     *
     * @param <T> The geometry type.
     */
    public <T extends PointGeometry> PointDataBuffered(Collection<? extends T> geometries, boolean highlight,
            PickManager pickManager, ModelDataRetriever<T> dataRetriever, Projection projectionSnapshot, TimeSpan groupTimeSpan)
    {
        super(Collections.singletonList(new PointDataBufferedBlock(geometries, highlight, pickManager, dataRetriever,
                projectionSnapshot, groupTimeSpan)));
    }

    /** A block which backs part or all of the geometry. */
    protected static class PointDataBufferedBlock extends BufferObjectList<BufferObject>
    {
        /**
         * Create the buffer objects for this block.
         *
         * @param <T> The geometry type.
         * @param geometries The geometries. Non-point geometries will be
         *            ignored.
         * @param highlight Flag indicating if the points should be highlighted.
         * @param pickManager The pick manager used to get pick colors.
         * @param dataRetriever Component that provides the model coordinates
         *            for the points.
         * @param projectionSnapshot If non-null this snapshot will be used when
         *            creating model data.
         * @param groupTimeSpan If non-null this will be used to upload time
         *            constraint information.
         * @return The list of buffer objects.
         */
        private static <T extends PointGeometry> List<? extends BufferObject> getBufferObjects(Collection<? extends T> geometries,
                boolean highlight, PickManager pickManager, ModelDataRetriever<T> dataRetriever, Projection projectionSnapshot,
                TimeSpan groupTimeSpan)
        {
            List<BufferObject> bufferObjects = New.list(4);
            if (!geometries.isEmpty())
            {
                FloatBuffer modelCoords = BufferUtilities.newFloatBuffer(geometries.size() * 3);
                ByteBuffer colors = BufferUtilities.newByteBuffer(geometries.size() * 4);
                ByteBuffer pickColors = BufferUtilities.newByteBuffer(geometries.size() * 4);
                FloatBuffer timeIntervals = groupTimeSpan == null ? null : BufferUtilities.newFloatBuffer(geometries.size() * 2);
                for (T geom : geometries)
                {
                    ModelCoordinates coords = (ModelCoordinates)dataRetriever.getModelData(geom, projectionSnapshot,
                            (AbstractRenderer.ModelData)null, TimeBudget.INDEFINITE);
                    if (coords != null)
                    {
                        modelCoords.put(coords.toArray());
                        ColorBufferUtilities.getColors(geom, geom.getRenderProperties(), highlight, colors);
                        pickManager.getPickColor(geom, pickColors);

                        if (timeIntervals != null && groupTimeSpan != null)
                        {
                            TimeConstraint timeConstraint = geom.getConstraints() == null ? null
                                    : geom.getConstraints().getTimeConstraint();
                            float min;
                            float max;
                            if (timeConstraint == null)
                            {
                                min = -Float.MAX_VALUE;
                                max = Float.MAX_VALUE;
                            }
                            else
                            {
                                min = MathUtil.getModulatedFloat(
                                        timeConstraint.getTimeSpan().isUnboundedStart() ? groupTimeSpan.getStart()
                                                : timeConstraint.getTimeSpan().getStart(),
                                        groupTimeSpan.getStart(), groupTimeSpan.getEnd());
                                max = MathUtil.getModulatedFloat(
                                        timeConstraint.getTimeSpan().isUnboundedEnd() ? groupTimeSpan.getEnd()
                                                : timeConstraint.getTimeSpan().getEnd(),
                                        groupTimeSpan.getStart(), groupTimeSpan.getEnd());
                            }
                            timeIntervals.put(min);
                            timeIntervals.put(max);
                        }
                    }
                }
                bufferObjects.add(new ColorBufferObject(colors, 4, AbstractGeometry.RenderMode.DRAW));
                bufferObjects.add(new ColorBufferObject(pickColors, 4, AbstractGeometry.RenderMode.PICK));
                if (timeIntervals != null)
                {
                    bufferObjects.add(new VertexAttributeBufferObject(
                            ShaderRendererUtilities.INTERVAL_FILTER_VERTEX_TIME_ATTRIBUTE_NAME, timeIntervals));
                }
                bufferObjects.add(new VertexBufferObject(modelCoords, true));
            }
            return bufferObjects;
        }

        /**
         * Constructor.
         *
         * @param <T> The geometry type.
         * @param geometries The geometries. Non-point geometries will be
         *            ignored.
         * @param highlight Flag indicating if the points should be highlighted.
         * @param pickManager The pick manager used to get pick colors.
         * @param dataRetriever Component that provides the model coordinates
         *            for the points.
         * @param projectionSnapshot If non-null this snapshot will be used when
         *            creating model data.
         * @param groupTimeSpan If non-null this will be used to upload time
         *            constraint information.
         */
        public <T extends PointGeometry> PointDataBufferedBlock(Collection<? extends T> geometries, boolean highlight,
                PickManager pickManager, ModelDataRetriever<T> dataRetriever, Projection projectionSnapshot,
                TimeSpan groupTimeSpan)
        {
            super(getBufferObjects(geometries, highlight, pickManager, dataRetriever, projectionSnapshot, groupTimeSpan));
        }
    }
}
