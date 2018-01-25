package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.PointSetGeometry;
import io.opensphere.core.model.ColorArrayList;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.processor.PointSetProcessor.ModelCoordinates;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelDataRetriever;
import io.opensphere.core.pipeline.renderer.buffered.PointSetDataBuffered.PointSetDataBufferedBlock;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;

/**
 * Wrapper for handling buffering of point set data.
 */
public class PointSetDataBuffered extends BufferObjectList<PointSetDataBufferedBlock>
{
    /**
     * Constructor.
     *
     * @param geom The geometry.
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
    public <T extends PointSetGeometry> PointSetDataBuffered(T geom, boolean highlight, PickManager pickManager,
            ModelDataRetriever<T> dataRetriever, Projection projectionSnapshot, TimeSpan groupTimeSpan)
    {
        super(Collections.singletonList(
                new PointSetDataBufferedBlock(geom, highlight, pickManager, dataRetriever, projectionSnapshot, groupTimeSpan)));
    }

    /** A block which backs part or all of the geometry. */
    protected static class PointSetDataBufferedBlock extends BufferObjectList<BufferObject>
    {
        /**
         * Create the buffer objects for this block.
         *
         * @param <T> The geometry type.
         * @param geom The geometry.
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
        private static <T extends PointSetGeometry> List<? extends BufferObject> getBufferObjects(T geom, boolean highlight,
                PickManager pickManager, ModelDataRetriever<T> dataRetriever, Projection projectionSnapshot,
                TimeSpan groupTimeSpan)
        {
            List<BufferObject> bufferObjects;

            ModelCoordinates coords = (ModelCoordinates)dataRetriever.getModelData(geom, projectionSnapshot,
                    (AbstractRenderer.ModelData)null, TimeBudget.INDEFINITE);
            if (coords == null)
            {
                bufferObjects = Collections.emptyList();
            }
            else
            {
                bufferObjects = New.list(2);
                if (geom.getColors() != null)
                {
                    ColorArrayList colorArrayList = ColorArrayList.getColorArrayList(geom.getColors());
                    ByteBuffer colors = ByteBuffer.wrap(colorArrayList.getBytes());
                    bufferObjects.add(
                            new ColorBufferObject(colors, colorArrayList.getBytesPerColor(), AbstractGeometry.RenderMode.DRAW));
                }

                FloatBuffer modelCoords = coords.getCoordinates();
                bufferObjects.add(new VertexBufferObject(modelCoords, true));
            }

            return bufferObjects;
        }

        /**
         * Constructor.
         *
         * @param <T> The geometry type.
         * @param geom The geometry.
         * @param highlight Flag indicating if the points should be highlighted.
         * @param pickManager The pick manager used to get pick colors.
         * @param dataRetriever Component that provides the model coordinates
         *            for the points.
         * @param projectionSnapshot If non-null this snapshot will be used when
         *            creating model data.
         * @param groupTimeSpan If non-null this will be used to upload time
         *            constraint information.
         */
        public <T extends PointSetGeometry> PointSetDataBufferedBlock(T geom, boolean highlight, PickManager pickManager,
                ModelDataRetriever<T> dataRetriever, Projection projectionSnapshot, TimeSpan groupTimeSpan)
        {
            super(getBufferObjects(geom, highlight, pickManager, dataRetriever, projectionSnapshot, groupTimeSpan));
        }
    }
}
