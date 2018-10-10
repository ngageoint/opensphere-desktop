package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.RenderPropertyChangedEvent;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MemoizingSupplier;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;

/**
 * Processor for {@link LabelGeometry}s. This class determines the model
 * coordinates of input geometries and putting them in the cache for use by the
 * renderer. For model coordinate calculations within this processor always use
 * the origin for the model center. Because all text is eventually rendered in
 * screen coordinates, there will never be a need to use the model center to
 * increase accuracy.
 */
@SuppressWarnings("PMD.GodClass")
public class LabelProcessor extends AbstractProcessor<LabelGeometry>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LabelProcessor.class);

    /**
     * Flag indicating if I have any geometries which are located by geographic
     * positions.
     */
    private volatile boolean myHandlingGeographicGeometries;

    /** The manager for determining label occlusion. */
    private final LabelOcclusionManager myLabelOcclusionManager;

    /** The view altitude supplier. */
    private final MemoizingSupplier<Kilometers> myViewAltitudeSupplier;

    /**
     * Construct a label processor.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public LabelProcessor(ProcessorBuilder builder, GeometryRenderer<LabelGeometry> renderer)
    {
        super(LabelGeometry.class, builder, renderer);
        myLabelOcclusionManager = builder.getLabelOcclusionManager();
        myViewAltitudeSupplier = new MemoizingSupplier<>(() ->
        {
            GeographicPosition viewPosition = getMapContext().getProjection()
                    .convertToPosition(getViewer().getPosition().getLocation(), Altitude.ReferenceLevel.ELLIPSOID);
            return new Kilometers(viewPosition.getAlt().getKilometers());
        });
    }

    @Override
    public void generateDryRunGeometries()
    {
        LabelGeometry.Builder<GeographicPosition> builder = new LabelGeometry.Builder<>();
        builder.setPosition(new GeographicPosition(LatLonAlt.createFromDegrees(0., 0.)));
        builder.setText("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
        builder.setFont(Font.SANS_SERIF + " 8");
        LabelRenderProperties renderProperties = new DefaultLabelRenderProperties(0, true, false);
        renderProperties.setColor(Color.RED);
        LabelGeometry geom = new LabelGeometry(builder, renderProperties, null);
        Collection<LabelGeometry> adds = Collections.singleton(geom);
        receiveObjects(this, adds, Collections.<LabelGeometry>emptyList());
    }

    @Override
    public Class<? extends Position> getPositionType()
    {
        return ScreenPosition.class;
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
            if (evt.isFullClear())
            {
                getCache().clearCacheAssociations(LabelAttachmentCoordinates.class, ModelCoordinates.class);
            }
            else
            {
                Collection<LabelGeometry> overlapping = null;
                for (Geometry geom : getGeometries())
                {
                    for (GeographicBoundingBox bounds : evt.getBounds())
                    {
                        if (bounds.contains(((LabelGeometry)geom).getPosition(), 0.))
                        {
                            overlapping = CollectionUtilities.lazyAdd((LabelGeometry)geom, overlapping);
                            break;
                        }
                    }
                }
                if (overlapping != null)
                {
                    getCache().clearCacheAssociations(overlapping, LabelAttachmentCoordinates.class, ModelCoordinates.class);
                }
            }

            super.handleProjectionChanged(evt);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public boolean sensitiveToProjectionChanges()
    {
        return myHandlingGeographicGeometries || super.sensitiveToProjectionChanges();
    }

    @Override
    protected void handlePropertyChanged(RenderPropertyChangedEvent evt)
    {
        super.handlePropertyChanged(evt);
        // Clear cache to handle changes to the font size
        clearCachedData(getOnscreenDrawableGeometries());
    }

    @Override
    protected void cacheData(LabelGeometry geo, AbstractRenderer.ModelData data)
    {
        getCache().putCacheAssociation(geo, (ModelCoordinates)data, ModelCoordinates.class, ModelCoordinates.SIZE_BYTES, 0L);
    }

    @Override
    protected void clearCachedData(Collection<? extends Geometry> geoms)
    {
        getCache().clearCacheAssociations(geoms, LabelAttachmentCoordinates.class, ModelCoordinates.class);
    }

    @Override
    protected void doReceiveObjects(Object source, Collection<? extends LabelGeometry> adds,
            Collection<? extends Geometry> removes)
    {
        // Check to see if we are handling geographic geometries before calling
        // super.doReceiveObjecs() to ensure that this is set correctly before
        // any geometry processing takes place.
        Collection<LabelGeometry> processorGeometries = getGeometrySet();
        synchronized (processorGeometries)
        {
            myHandlingGeographicGeometries = false;
            for (Geometry geom : processorGeometries)
            {
                if (GeographicPosition.class.isAssignableFrom(((LabelGeometry)geom).getPositionType()))
                {
                    myHandlingGeographicGeometries = true;
                    break;
                }
            }
        }
        super.doReceiveObjects(source, adds, removes);
    }

    @Override
    protected List<LabelGeometry> filterOnscreen(Collection<? extends LabelGeometry> objects)
    {
        List<LabelGeometry> onScreen = super.filterOnscreen(objects);
        myLabelOcclusionManager.reset(this);
        return onScreen;
    }

    /**
     * Get the attachment point in model coordinates.
     *
     * @param geom The geometry.
     * @param projectionSnapshot If non-null this snapshot will be used when
     *            creating model data.
     * @return The model coordinates.
     */
    protected Vector3d getAttachmentModelCoordinates(LabelGeometry geom, Projection projectionSnapshot)
    {
        Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
        LabelAttachmentCoordinates position = getCache().getCacheAssociation(geom, LabelAttachmentCoordinates.class);
        if (position == null)
        {
            Vector3d model = getPositionConverter().convertPositionToModel(geom.getPosition(), projection, Vector3d.ORIGIN);
            if (model == null)
            {
                LOGGER.error("Failed to convert geographic position to model position.");
                return null;
            }
            position = new LabelAttachmentCoordinates(model);
            getCache().putCacheAssociation(geom, position, LabelAttachmentCoordinates.class, Vector3d.SIZE_BYTES, 0L);
        }
        return position;
    }

    @Override
    protected ModelCoordinates getCachedData(LabelGeometry geometry, AbstractRenderer.ModelData override)
    {
        return getCache().getCacheAssociation(geometry, ModelCoordinates.class);
    }

    @Override
    protected void handleViewChanged(Viewer view, ViewChangeSupport.ViewChangeType type)
    {
        getCache().clearCacheAssociations(ModelCoordinates.class);
        setOnscreenDirty();
        Collection<LabelGeometry> scalableGeoms = getScalableGeometries();
        if (!scalableGeoms.isEmpty())
        {
            myViewAltitudeSupplier.invalidate();
            resetState(scalableGeoms, State.UNPROCESSED);
        }
        super.handleViewChanged(view, type);
    }

    @Override
    protected boolean isOnScreen(LabelGeometry geom, boolean useTime)
    {
        if (!super.isOnScreen(geom, useTime))
        {
            return false;
        }
        Position pos = geom.getPosition();
        if (pos instanceof ScreenPosition)
        {
            // Assume screen position labels are always on-screen.
            return true;
        }
        else if (pos instanceof GeographicPosition)
        {
            Vector3d viewerModel = getAttachmentModelCoordinates(geom, null);
            boolean isBelowGround = ((GeographicPosition)pos).getLatLonAlt().getAltM() < 0;
            boolean onScreen = (isBelowGround || !isObscured(viewerModel)) && getViewer().isInView(new Vector3d(viewerModel), 0f);

            if (onScreen)
            {
                ScreenBoundingBox box = getCache().getCacheAssociation(geom, ScreenBoundingBox.class);
                if (box != null)
                {
                    onScreen = !myLabelOcclusionManager.isOccluded(this, box);
                }
            }

            return onScreen;
        }
        else
        {
            return true;
        }
    }

    @Override
    protected AbstractRenderer.ModelData processGeometry(LabelGeometry label, Projection projectionSnapshot,
            AbstractRenderer.ModelData override, TimeBudget timeBudget)
    {
        if (override != null)
        {
            return override;
        }

        Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
        Vector3d modelCenter = projection == null ? Vector3d.ORIGIN : projection.getModelCenter();
        FontRenderContext frc = new FontRenderContext(null, false, false);

        // TODO: store the alignment offsets in the model coordinates and allow
        // the renderer to perform the offset.
        double hAlign = label.getHorizontalAlignment();
        double vAlign = label.getVerticalAlignment();
        Position pos = label.getPosition();
        Font drawFont = Font.decode(label.getRenderProperties().getFont());
        LabelLayout lay = labelBounds(drawFont, frc, label.getText());
        Vector3d window = null;
        if (pos instanceof ScreenPosition)
        {
            ScreenPosition sPos = (ScreenPosition)pos;
            ScreenPosition adjPos = new ScreenPosition(Math.round(sPos.getX() - hAlign * lay.rect.getWidth()),
                    Math.round(sPos.getY() + vAlign * lay.rect.getHeight()));

            window = getPositionConverter().convertPositionToModel(adjPos, modelCenter);
        }
        else if (pos instanceof GeographicPosition)
        {
            // get the world model coordinates.
            Vector3d model = getAttachmentModelCoordinates(label, projection);
            if (model == null)
            {
                return null;
            }

            window = getPositionConverter().convertModelToWindow(model, Vector3d.ORIGIN);
            window = new Vector3d(window.getX() - hAlign * lay.rect.getWidth(), window.getY() - vAlign * lay.rect.getHeight(),
                    0.0);

            // Cache the bounding box of the label for occlusion testing.
            ScreenPosition ul = new ScreenPosition(window.getX(), window.getY());
            ScreenPosition lr = new ScreenPosition(window.getX() + lay.rect.getWidth(), window.getY() + lay.rect.getHeight());
            getCache().putCacheAssociation(label, new ScreenBoundingBox(ul, lr), ScreenBoundingBox.class, 0, 0);
        }

        ModelCoordinates result = new ModelCoordinates(window);
        result.setBaselineDelta(lay.lineH);
        result.setFontSize(getFontSize(label));
        return result;
    }

    /**
     * Simple struct.
     */
    private static class LabelLayout
    {
        /** line height. */
        private double lineH;

        /** bounding Rectangle. */
        private Rectangle2D rect;
    }

    /**
     * Determines the bounds for a label with specified font and render context.
     *
     * @param drawFont the font the label will be drawn in
     * @param frc the font render context
     * @param txt the label
     * @return the label layout
     */
    private LabelLayout labelBounds(Font drawFont, FontRenderContext frc, String txt)
    {
        String[] lines = txt.split("\\n");
        double maxW = 0.0;
        double maxH = 0.0;
        for (String ln : lines)
        {
            Rectangle2D r = drawFont.createGlyphVector(frc, ln).getLogicalBounds();
            maxW = Math.max(maxW, r.getWidth());
            maxH = Math.max(maxH, r.getHeight());
        }
        LabelLayout lay = new LabelLayout();
        lay.lineH = maxH;
        lay.rect = new Rectangle2D.Double(0.0, 0.0, maxW, maxH * lines.length);
        return lay;
    }

    @Override
    protected boolean sensitiveToViewChanges()
    {
        return myHandlingGeographicGeometries || super.sensitiveToViewChanges();
    }

    @Override
    protected void setPositionType(Collection<? extends Geometry> adds)
    {
        // LabelProcessor is always ScreenPosition regardless of the geometries'
        // types.
    }

    /**
     * Gets the scalable geometries.
     *
     * @return the scalable geometries
     */
    private Collection<LabelGeometry> getScalableGeometries()
    {
        Collection<LabelGeometry> scalableGeoms;
        synchronized (getGeometrySet())
        {
            scalableGeoms = StreamUtilities.filter(getGeometrySet(), g -> g.getRenderProperties().getScaleFunction() != null);
        }
        return scalableGeoms;
    }

    /**
     * Gets the font size from the properties and view.
     *
     * @param geom the geometry
     * @return the font size
     */
    private int getFontSize(LabelGeometry geom)
    {
        float fontSize = AWTUtilities.getFontSize(geom.getRenderProperties().getFont());

        // Scale based on the view
        Function<Kilometers, Float> scaleFunction = geom.getRenderProperties().getScaleFunction();
        if (scaleFunction != null)
        {
            float viewScale = scaleFunction.apply(myViewAltitudeSupplier.get()).floatValue();
            fontSize *= viewScale;
        }

        return Math.round(fontSize);
    }

    /**
     * Wrapper class to make sure we get the right stuff out of the cache.
     */
    public static class ModelCoordinates implements AbstractRenderer.ModelData
    {
        /** The size of models in the cache in bits. */
        private static final int SIZE_BYTES = Vector3d.SIZE_BYTES + Constants.INT_SIZE_BYTES + Constants.INT_SIZE_BYTES;

        /** The model coordinates. */
        private final Vector3d myScreenModelCoords;

        /** The baseline coordinate delta. */
        private double baselineDelta;

        /** The font size. */
        private int myFontSize;

        /**
         * Constructor.
         *
         * @param screenModel The model coordinates matching the screen
         *            projection
         */
        public ModelCoordinates(Vector3d screenModel)
        {
            myScreenModelCoords = screenModel;
        }

        /**
         * Get the screenModelCoords.
         *
         * @return the screenModelCoords
         */
        public Vector3d getScreenModelCoords()
        {
            return myScreenModelCoords;
        }

        /**
         * Get the distance between baselines of consecutive lines. It is used
         * to lay out multi-line labels.
         *
         * @return the baseline delta
         */
        public double getBaselineDelta()
        {
            return baselineDelta;
        }

        /**
         * Set the baseline delta (cf. getBaselineDelta).
         *
         * @param d the baseline delta
         */
        public void setBaselineDelta(double d)
        {
            baselineDelta = d;
        }

        /**
         * Gets the font size.
         *
         * @return the font size
         */
        public int getFontSize()
        {
            return myFontSize;
        }

        /**
         * Sets the font size.
         *
         * @param fontSize the font size
         */
        public void setFontSize(int fontSize)
        {
            myFontSize = fontSize;
        }
    }

    /**
     * Subclass to make caching faster.
     */
    protected static class LabelAttachmentCoordinates extends Vector3d
    {
        /**
         * Constructor.
         *
         * @param model Model coordinates of the attachment point.
         */
        public LabelAttachmentCoordinates(Vector3d model)
        {
            super(model);
        }

        @Override
        public boolean equals(Object obj)
        {
            return Utilities.sameInstance(this, obj);
        }

        @Override
        public int hashCode()
        {
            return System.identityHashCode(this);
        }
    }
}
