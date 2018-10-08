package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.PointSpriteGeometry;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointScaleRenderProperty;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.RenderPropertyChangedEvent;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.DelegatingRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.TextureGroup;
import io.opensphere.core.pipeline.util.TextureHandle;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MemoizingSupplier;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateController;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.DynamicViewer;

/**
 * Processor for {@link PointSpriteGeometry}s. This class determines the model
 * coordinates of input geometries and putting them in the cache for use by the
 * renderer.
 */
public class PointSpriteProcessor extends TextureProcessor<PointSpriteGeometry>
{
    /** Sensitivity of heading rotation for triggering point sprite rotation. */
    private static final double HEADING_SENSITIVITY = Math.toRadians(1);

    /** Comparator that determines geometry processing priority. */
    private final Comparator<? super PointSpriteGeometry> myPriorityComparator;

    /** The view altitude supplier. */
    private final MemoizingSupplier<Kilometers> myViewAltitudeSupplier;

    /** An executor that procrastinates before running tasks. */
    private final Executor myViewChangeExecutor = CommonTimer.createProcrastinatingExecutor(100);

    /** The last heading value for which rotation was handled. */
    private double myLastHandledHeading = Double.MAX_VALUE;

    /**
     * Construct a point processor.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public PointSpriteProcessor(ProcessorBuilder builder, GeometryRenderer<PointSpriteGeometry> renderer)
    {
        super(PointSpriteGeometry.class, builder, renderer, 0);

        Utilities.checkNull(builder.getPriorityComparator(), "builder.getPriorityComparator()");
        myPriorityComparator = builder.getPriorityComparator();

        myViewAltitudeSupplier = new MemoizingSupplier<>(new Supplier<Kilometers>()
        {
            @Override
            public Kilometers get()
            {
                GeographicPosition viewPosition = getMapContext().getProjection()
                        .convertToPosition(getViewer().getPosition().getLocation(), Altitude.ReferenceLevel.ELLIPSOID);
                return new Kilometers(viewPosition.getAlt().getKilometers());
            }
        });
    }

    @Override
    public void generateDryRunGeometries()
    {
        PointSpriteGeometry.Builder<GeographicPosition> builder = new PointSpriteGeometry.Builder<>();

        PointRenderProperties props = new DefaultPointRenderProperties(0, true, true, true);

        builder.setPosition(new GeographicPosition(LatLonAlt.createFromDegrees(0., 0.)));

        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = image.getGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 10, 10);
        builder.setImageManager(new ImageManager(null, new SingletonImageProvider(image)));

        Collection<PointSpriteGeometry> geoms = New.collection();
        props.setSize(10);
        geoms.add(new PointSpriteGeometry(builder, props, null));
        props.setSize(5);
        geoms.add(new PointSpriteGeometry(builder, props, null));

        receiveObjects(this, geoms, Collections.<PointSpriteGeometry>emptySet());
    }

    @Override
    public void handleProjectionChanged(ProjectionChangedEvent evt)
    {
        if (!sensitiveToProjectionChanges())
        {
            return;
        }

        Lock writeLock = getProjectionChangeLock().writeLock();
        writeLock.lock();
        try
        {
            // Clear must come first because super's implementation may trigger
            // drawing to occur.
            getCache().clearCacheAssociations(PointSpriteProcessor.SpriteModelCoordinates.class);
            super.handleProjectionChanged(evt);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    protected void cacheData(PointSpriteGeometry geo, AbstractRenderer.ModelData data)
    {
        PointSpriteProcessor.SpriteModelCoordinates modelData = (PointSpriteProcessor.SpriteModelCoordinates)((TextureModelData)data)
                .getModelData();
        getCache().putCacheAssociation(geo, modelData, PointSpriteProcessor.SpriteModelCoordinates.class,
                modelData.getSizeBytes(), 0L);
    }

    @Override
    protected void clearCachedData(Collection<? extends Geometry> geoms)
    {
        getCache().clearCacheAssociations(geoms, PointSpriteProcessor.SpriteModelCoordinates.class);
    }

    @Override
    protected SpriteModelCoordinates getCachedModelData(PointSpriteGeometry geo, TextureCoords imageTexCoords,
            AbstractRenderer.ModelData override)
    {
        SpriteModelCoordinates smc = (SpriteModelCoordinates)(override == null ? null
                : ((TextureModelData)override).getModelData());
        if (smc == null)
        {
            smc = getCache().getCacheAssociation(geo, PointSpriteProcessor.SpriteModelCoordinates.class);
        }
        return smc;
    }

    @Override
    protected Comparator<? super PointSpriteGeometry> getPriorityComparator()
    {
        return myPriorityComparator;
    }

    @Override
    protected void handleBlankImage(PointSpriteGeometry geom)
    {
    }

    @Override
    protected void handlePropertyChanged(RenderPropertyChangedEvent evt)
    {
        // Because the point size is stored in the display list, the display
        // list must be rebuilt. The regular check for whether the display
        // list needs to be rebuilt has no ability to check for render property
        // changes.
        if (evt.getRenderProperties() instanceof PointSizeRenderProperty)
        {
            GeometryRenderer<PointSpriteGeometry> renderer = getRenderer();
            if (getRenderer() instanceof DelegatingRenderer)
            {
                ((DelegatingRenderer<?>)renderer).setDirty();
            }
            getRepaintListener().repaint();
        }
        else
        {
            super.handlePropertyChanged(evt);
        }
    }

//    @Override
//    protected boolean isOnScreen(PointSpriteGeometry geom, boolean useTime)
//    {
//        if (!super.isOnScreen(geom, useTime))
//        {
//            return false;
//        }
//
// TODO: This is too slow when there are lots of dots.
//        Position position = geom.getPosition();
//        if (position instanceof GeographicPosition)
//        {
//            PointProcessor.ModelCoordinates modelData = (PointSpriteProcessor.ModelCoordinates)getModelData(geom);
//            Vector3d model = modelData.getVector();
//            return getViewer().isInView(model, 0f) && !isObscured(model);
//        }
//        else
//        {
//            return true;
//        }
//        return true;
//    }

    @Override
    protected TextureModelData processGeometry(PointSpriteGeometry geom, Projection projectionSnapshot,
            AbstractRenderer.ModelData override, TimeBudget timeBudget)
    {
        SpriteModelCoordinates modelData = (SpriteModelCoordinates)(override == null ? null
                : ((TextureModelData)override).getModelData());
        TextureGroup textureGroup = processTextureForGeometry(geom, override, timeBudget);

        if (modelData == null)
        {
            Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
            Vector3d modelCenter = projection == null ? Vector3d.ORIGIN : projection.getModelCenter();
            Vector3d model = getPositionConverter().convertPositionToModel(geom.getPosition(), projection, modelCenter);
            modelData = new SpriteModelCoordinates(model);
            setModelSizes(geom, textureGroup, modelData);
        }

        return new TextureModelData(modelData, textureGroup);
    }

    @Override
    protected void processTextureLoaded(Collection<? extends PointSpriteGeometry> textureLoaded,
            StateController<PointSpriteGeometry> controller)
    {
        if (textureLoaded.isEmpty())
        {
            return;
        }

        Lock readLock = getProjectionChangeLock().readLock();
        readLock.lock();
        try
        {
            for (PointSpriteGeometry geom : textureLoaded)
            {
                SpriteModelCoordinates modelData = getCache().getCacheAssociation(geom, SpriteModelCoordinates.class);
                if (modelData == null)
                {
                    Vector3d model = getPositionConverter().convertPositionToModel(geom.getPosition(), getProjectionSnapshot(),
                            getProjectionSnapshot().getModelCenter());
                    modelData = new SpriteModelCoordinates(model);
                    TextureGroup cachedTextureGroup = getCache().getCacheAssociation(geom.getImageManager(), TextureGroup.class);
                    setModelSizes(geom, cachedTextureGroup, modelData);
                    cacheData(geom, new TextureModelData(modelData, null));

                    setOnscreenDirty();
                }
            }
        }
        finally
        {
            readLock.unlock();
        }

        controller.changeState(textureLoaded, State.READY);
    }

    @Override
    protected void resetStateDueToProjectionChange()
    {
        // Reset state for all ready geometries in order to preserve interaction
        // between user operations & other geometry state.
        // Concessions in performance have to be made for correctness.
        Collection<PointSpriteGeometry> regularGeoms = getReadyGeometries();
        if (CollectionUtilities.hasContent(regularGeoms))
        {
            resetState(regularGeoms, TextureState.TEXTURE_LOADED);
        }
    }

    @Override
    protected boolean sensitiveToViewChanges()
    {
        return false;
    }

    @Override
    protected void handleViewChanged(final Viewer view, final ViewChangeSupport.ViewChangeType type)
    {
        Collection<PointSpriteGeometry> scalableGeoms = getScalableGeometries();
        if (!scalableGeoms.isEmpty())
        {
            myViewAltitudeSupplier.invalidate();
            clearCachedData(scalableGeoms);
            resetState(scalableGeoms, State.UNPROCESSED);
        }

        // Reset projection-sensitive geometries so they get re-processed
        if (type == ViewChangeSupport.ViewChangeType.NEW_VIEWER)
        {
            resetProjectionSensitiveGeometries(view, type);
        }
        else
        {
            myViewChangeExecutor.execute(() -> resetProjectionSensitiveGeometries(view, type));
        }

        super.handleViewChanged(view, type);
    }

    /**
     * Resets projection-sensitive geometries so they get re-processed.
     *
     * @param view the viewer
     * @param type the change type
     */
    private void resetProjectionSensitiveGeometries(Viewer view, ViewChangeSupport.ViewChangeType type)
    {
        if (view instanceof DynamicViewer)
        {
            double heading = ((DynamicViewer)view).getHeading();
            double delta = Math.abs(heading - myLastHandledHeading);
            if (delta >= HEADING_SENSITIVITY || myLastHandledHeading == Double.MAX_VALUE
                    || type == ViewChangeSupport.ViewChangeType.NEW_VIEWER)
            {
                myLastHandledHeading = heading;

                // When re-processing rotated images, we want to ensure they
                // aren't otherwise modified.
                Collection<PointSpriteGeometry> geoms = getGeometrySet();
                synchronized (geoms)
                {
                    Map<ImageManager, List<PointSpriteGeometry>> managersToGeoms = getImageManagersToGeoms();
                    List<PointSpriteGeometry> projectionSensitiveGeoms = New.list(geoms.size());

                    for (PointSpriteGeometry geom : geoms)
                    {
                        if (geom.isProjectionSensitive())
                        {
                            projectionSensitiveGeoms.add(geom);
                            managersToGeoms.remove(geom.getImageManager());
                        }
                    }

                    resetDueToImageUpdate(projectionSensitiveGeoms, State.UNPROCESSED);
                }
            }
        }
    }

    /**
     * Gets the scalable geometries.
     *
     * @return the scalable geometries
     */
    private Collection<PointSpriteGeometry> getScalableGeometries()
    {
        Collection<PointSpriteGeometry> scalableGeoms;
        synchronized (getGeometrySet())
        {
            scalableGeoms = StreamUtilities.filter(getGeometrySet(), geom -> getScaleFunction(geom) != null);
        }
        return scalableGeoms;
    }

    /**
     * Sets the size and highlight size in the model data.
     *
     * @param geom the geometry
     * @param textureGroup the texture group
     * @param modelData the model data
     */
    private void setModelSizes(PointSpriteGeometry geom, TextureGroup textureGroup, SpriteModelCoordinates modelData)
    {
        // Calculate size
        float size = 0f;
        float highlightSize = 0f;
        if (geom.getRenderProperties().getSizeProperty() instanceof PointScaleRenderProperty)
        {
            PointScaleRenderProperty scaleProperty = (PointScaleRenderProperty)geom.getRenderProperties().getSizeProperty();
            Object textureKey = textureGroup != null ? textureGroup.getTextureMap().get(RenderMode.DRAW) : null;
            TextureHandle textureHandle = textureKey == null ? null
                    : getCache().getCacheAssociation(textureKey, TextureHandle.class);
            if (textureHandle != null)
            {
                size = textureHandle.getWidth() * scaleProperty.getSize();
                highlightSize = textureHandle.getWidth() * scaleProperty.getHighlightSize();
            }
        }
        else
        {
            size = geom.getRenderProperties().getSizeProperty().getSize();
            highlightSize = geom.getRenderProperties().getSizeProperty().getHighlightSize();
        }

        // Scale based on the view
        Function<Kilometers, Float> scaleFunction = getScaleFunction(geom);
        if (scaleFunction != null)
        {
            float viewScale = scaleFunction.apply(myViewAltitudeSupplier.get()).floatValue();
            size *= viewScale;
            highlightSize *= viewScale;
        }

        modelData.setSize(Math.round(size));
        modelData.setHighlightSize(Math.round(highlightSize));
    }

    /**
     * Gets the scale function of the geometry if there is one.
     *
     * @param geom the geometry
     * @return the scale function, or null
     */
    private static Function<Kilometers, Float> getScaleFunction(PointSpriteGeometry geom)
    {
        if (geom.getRenderProperties().getSizeProperty() instanceof PointScaleRenderProperty)
        {
            return ((PointScaleRenderProperty)geom.getRenderProperties().getSizeProperty()).getScaleFunction();
        }
        return null;
    }

    /**
     * Specialization of {@link PointProcessor.ModelCoordinates} to make caching
     * faster.
     */
    public static class SpriteModelCoordinates extends PointProcessor.ModelCoordinates
    {
        /** The size. */
        private int mySize;

        /** The highlight size. */
        private int myHighlightSize;

        /**
         * Constructor.
         *
         * @param vec The vector.
         */
        public SpriteModelCoordinates(Vector3d vec)
        {
            super(vec);
        }

        /**
         * Gets the size.
         *
         * @return the size
         */
        public int getSize()
        {
            return mySize;
        }

        /**
         * Sets the size.
         *
         * @param size the size
         */
        public void setSize(int size)
        {
            mySize = size;
        }

        /**
         * Gets the highlight size.
         *
         * @return the highlight size
         */
        public int getHighlightSize()
        {
            return myHighlightSize;
        }

        /**
         * Sets the highlight size.
         *
         * @param highlightSize the highlight size
         */
        public void setHighlightSize(int highlightSize)
        {
            myHighlightSize = highlightSize;
        }

        @Override
        public long getSizeBytes()
        {
            return super.getSizeBytes() + Constants.INT_SIZE_BYTES + Constants.INT_SIZE_BYTES;
        }

        @Override
        @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
        public boolean equals(Object obj)
        {
            return super.equals(obj);
        }

        @Override
        @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
        public int hashCode()
        {
            return super.hashCode();
        }
    }
}
